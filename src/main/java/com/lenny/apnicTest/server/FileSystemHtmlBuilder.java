package com.lenny.apnicTest.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Type with static helper methods to generate html for file system obejcts
 */
public class FileSystemHtmlBuilder
{
    private static int HTTP_200 = 200;
    /**
     * Template for generate html around file system directory listing
     * Expanded by String.format(HTML_TEMPLATE, strDirectories, strFiles)
     */
    private static String HTML_TEMPLATE = "<!DOCTYPE html>\n" +
                                              "<html lang=\"en\">\n" +
                                              "<head>\n" +
                                              "    <meta charset=\"UTF-8\">\n" +
                                              "    <title>Title</title>\n" +
                                              "    <link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\"\n" +
                                              "      rel=\"stylesheet\">" +
                                              "</head>\n" +
                                              "<body>\n" +
                                              "<h1>Current directory: %s</h1>\n" +
                                              "<h1>Directories</h1>\n" +
                                              "<ul style=\"list-style-type:none\">%s</ul>\n" +
                                              "<h1>Files</h1>\n" +
                                              "<ul style=\"list-style-type:none\">%s</ul>\n" +
                                              "</body>\n" +
                                              "</html>";
    /**
     * Templates for generating individual file or directory listings into the HTML_TEMPLATE
     * Will generate anchor links for relative directory paths and files
     */
    private static String DIR_UP_LISTING = ".. (one level up)";
    private static String HTML_DIR_LISTING = "<li><i class=\"material-icons\" style=\"color:blue\">folder_open</i><a id=\"dir-%s\" href=\"/%s\">%s</a></li>\n";
    private static String HTML_DIR_LISTING_DISABLE = "<li><i class=\"material-icons\" style=\"color:light_gray\">folder_open</i><i class=\"material-icons\" style=\"color:pink\">lock</i>%s</li>\n";
    private static String HTML_FILE_LISTING = "<li><i class=\"material-icons\" style=\"color:orange\">format_align_justify</i><a id=\"file-%s\" href=\"/%s\">%s</a></li>\n";
    private static String HTML_FILE_LISTING_DISABLE = "<li><i class=\"material-icons\" style=\"color:gray\">format_align_justify</i><i class=\"material-icons\" style=\"color:pink\">lock</i>%s</li>\n";
    private static String HTML_DIR_ERROR = "<p style=\"color:red\">ERROR: %s is not a directory!</p>\n";
    private static String HTML_FILE_ERROR = "<p style=\"color:red\">ERROR: %s is not a file!</p>\n";

    /**
     * Get html representing the filesystem at the dirSrc, with relative path links from the root
     * @param dirRoot - the root directory for links
     * @param dirSrc - the source directory to list
     * @return - the html representing the fielsystem in HTML_TEMPLATE format
     */
    public static String getHtmlFileSystem(File dirRoot, File dirSrc)
    {
        List<String> listDirs = getHtmlDirectoryStructure(dirRoot, dirSrc);
        String strOutputDirs = listDirs.stream()
            .collect(Collectors.joining());
        List<String> listFiles = getHtmlFileStructure(dirRoot, dirSrc);
        String strOutputFiles = listFiles.stream()
            .collect(Collectors.joining());

        return String.format(HTML_TEMPLATE, dirSrc.getPath(), strOutputDirs, strOutputFiles);
    }

    /**
     * Generate the html response into HttpExchange object for the root and src directories provided.
     * @param dirRoot - root directory for relative paths
     * @param dirSrc - directory to list
     * @param httpExchange - exchange object conatining the request / reposnse
     * @return - true is successful
     * @throws IOException
     */
    public static boolean getHtmlDirectoryResponse(File dirRoot, File dirSrc, HttpExchange httpExchange) throws IOException
    {
        String strResponse = FileSystemHtmlBuilder.getHtmlFileSystem(dirRoot, dirSrc);
        httpExchange.sendResponseHeaders(HTTP_200, strResponse.length());
        httpExchange.getResponseBody().write(strResponse.getBytes());

        return true;
    }

    /**
     * Convert the subdirectories of dirSrc into a list of html elements with anchor links to directories
     * relative to the dirSrc
     * @param dirRoot - root directory for relative paths
     * @param dirSrc - directory to list subdirectories
     * @return - List of html elements (fragments)
     */
    public static List<String> getHtmlDirectoryStructure(File dirRoot, File dirSrc)
    {
        if(dirSrc.isDirectory())
        {
            List<String> listDirs = new ArrayList<>();
            if
            (
                dirSrc.getParent()  != null
                &&
                !dirSrc.getParent().isEmpty()
            )
            {
                listDirs.add(buildDirectoryLink(new File(dirSrc.getParent()), dirRoot, true));
            }
            listDirs.addAll(
                Arrays.stream(
                dirSrc.listFiles(new FileFilter()
                {
                    @Override
                    public boolean accept(File file)
                    {
                        return file.isDirectory();
                    }
                })
               )
            .map(dir -> buildDirectoryLink(dir, dirRoot))
            .collect(Collectors.toList())
            );
            return listDirs;
        }
        return null;
    }

    /**
     * Convert the files of dirSrc into a list of html elements with anchor links to files
     * relative to the dirSrc
     * @param dirRoot - root directory for relative paths
     * @param dirSrc - directory to list files
     * @return - List of html elements (fragments)
     */
    public static List<String> getHtmlFileStructure(File dirRoot, File dirSrc)
    {
        if(dirSrc.isDirectory())
        {
            return Arrays.stream(
                dirSrc.listFiles(new FileFilter()
                {
                    @Override
                    public boolean accept(File file)
                    {
                        return file.isFile();
                    }
                })
             )
             .map(dir -> buildFileLink(dir, dirRoot))
             .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Helper function for getHtmlDirectoryStructure. Made public for unit testing
     * @param dirRoot - root directory for relative paths
     * @param dirFor - directory to build link for
     * @return - Html element (fragment)
     */
    public static String buildDirectoryLink(File dirFor, File dirRoot)
    {
        return buildDirectoryLink(dirFor, dirRoot, false);
    }
    public static String buildDirectoryLink(File dirFor, File dirRoot, boolean bUpOneLevel)
    {
        if(dirFor.isDirectory())
        {
            Path pathFromRoot = Paths.get(dirRoot.getPath()).relativize(Paths.get(dirFor.getPath()));
            if(dirFor.canRead())
            {
                return String.format
                          (
                              HTML_DIR_LISTING,
                              pathFromRoot.getFileName(),
                              replaceBackslash(pathFromRoot.toString()),
                              bUpOneLevel ? ".. (one level up)" : pathFromRoot.getFileName()
                          );
            }
            else
            {
                return String.format
                          (
                              HTML_DIR_LISTING_DISABLE,
                              pathFromRoot.getFileName()
                          );
            }
        }
        return String.format(HTML_DIR_ERROR, dirFor.getName());
    }

    /**
     * Helper function for getHtmlFileStructure. Made public for unit testing
     * @param dirRoot - root directory for relative paths
     * @param fileFor - file to build link for
     * @return - Html element (fragment)
     */
    public static String buildFileLink(File fileFor, File dirRoot)
    {
        if(fileFor.isFile())
        {
            Path pathFromRoot = Paths.get(dirRoot.getPath()).relativize(Paths.get(fileFor.getPath()));
            if(fileFor.canRead())
            {
                return String.format
                          (
                              HTML_FILE_LISTING,
                              fileFor.getName(),
                              replaceBackslash(pathFromRoot.toString()),
                              fileFor.getName()
                          );
            }
            else
            {
                return String.format
                          (
                              HTML_FILE_LISTING_DISABLE,
                              fileFor.getName()
                          );
            }
        }
        return String.format(HTML_FILE_ERROR, fileFor.getName());
    }

    /**
     * Helper function.
     * @param strFrom
     * @return
     */
    private static String replaceBackslash(String strFrom)
    {
        return strFrom.replace('\\', '/');
    }

    /**
     * Stream the file from fileRequest into the HttpExchange object
     * This is a very naive implementation that should
     * (i) Chunk large files
     * (ii) Determine if the content of the file is suitable (ie only serve text, other with provide summary of file)
     * The exchange response is set to 404 if the file does not exist or cannot be read.
     * @param fileRequest - file to serve
     * @param httpExchange - exchange object to stream to
     * @return true on success
     * @throws IOException
     */
    public static boolean getFileContent(File fileRequest, HttpExchange httpExchange) throws IOException
    {
        if
        (
            fileRequest.isFile()
            &&
           fileRequest.canRead()
        )
        {
            httpExchange.sendResponseHeaders(HTTP_200, fileRequest.length());
            InputStream inputStream = new FileInputStream(fileRequest);
            byte[] byteBuffer = new byte[4096];
            int nBytesRead = 0;
            while((nBytesRead = inputStream.read(byteBuffer, 0, byteBuffer.length)) > 0)
            {
                httpExchange.getResponseBody().write(byteBuffer, 0, nBytesRead);
                httpExchange.getResponseBody().flush();
            }
            return true;
        }
        return false;
    }
}
