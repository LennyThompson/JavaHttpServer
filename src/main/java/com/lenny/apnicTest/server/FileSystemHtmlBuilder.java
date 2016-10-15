package com.lenny.apnicTest.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemHtmlBuilder
{
    private static String HTML_TEMPLATE = "<!DOCTYPE html>\n" +
                                              "<html lang=\"en\">\n" +
                                              "<head>\n" +
                                              "    <meta charset=\"UTF-8\">\n" +
                                              "    <title>Title</title>\n" +
                                              "    <link href=\"https://fonts.googleapis.com/icon?family=Material+Icons\"\n" +
                                              "      rel=\"stylesheet\">" +
                                              "</head>\n" +
                                              "<body>\n" +
                                              "<h1>Directories</h1>\n" +
                                              "<ul style=\"list-style-type:none\">%s</ul>\n" +
                                              "<h1>Files</h1>\n" +
                                              "<ul style=\"list-style-type:none\">%s</ul>\n" +
                                              "</body>\n" +
                                              "</html>";
    private static String HTML_DIR_LISTING = "<li><i class=\"material-icons\" style=\"color:blue\">folder_open</i><a href=\"/%s\">%s</a></li>\n";
    private static String HTML_DIR_LISTING_DISABLE = "<li><i class=\"material-icons\" style=\"color:light_gray\">folder_open</i><i class=\"material-icons\">lock</i><a href=\"/%s\">%s</a></li>\n";
    private static String HTML_FILE_LISTING = "<li><i class=\"material-icons\" style=\"color:orange\">format_align_justify</i><a href=\"/%s\">%s</a></li>\n";
    private static String HTML_FILE_LISTING_DISABLE = "<li><i class=\"material-icons\" style=\"color:gray\">format_align_justify</i><a href=\"/%s\">%s</a></li>\n";
    private static String HTML_DIR_ERROR = "<p>ERROR: %s is not a directory!</p>\n";
    private static String HTML_FILE_ERROR = "<p>ERROR: %s is not a file!</p>\n";

    public static String getHtmlFileSystem(File dirRoot, File dirSrc)
    {
        List<String> listDirs = getHtmlDirectoryStructure(dirRoot, dirSrc);
        String strOutputDirs = listDirs.stream()
            .collect(Collectors.joining());
        List<String> listFiles = getHtmlFileStructure(dirRoot, dirSrc);
        String strOutputFiles = listFiles.stream()
            .collect(Collectors.joining());

        return String.format(HTML_TEMPLATE, strOutputDirs, strOutputFiles);
    }

    public static boolean getHtmlDirectoryResponse(File dirRoot, File dirSrc, HttpExchange httpExchange) throws IOException
    {
        String strResponse = FileSystemHtmlBuilder.getHtmlFileSystem(dirRoot, dirSrc);
        httpExchange.sendResponseHeaders(200, strResponse.length());
        httpExchange.getResponseBody().write(strResponse.getBytes());

        return true;
    }

    public static List<String> getHtmlDirectoryStructure(File dirRoot, File dirSrc)
    {
        if(dirSrc.isDirectory())
        {
            return Arrays.stream(
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
            .collect(Collectors.toList());
        }
        return null;
    }

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

    public static String buildDirectoryLink(File dirFor, File dirRoot)
    {
        if(dirFor.isDirectory())
        {
            Path pathFromRoot = Paths.get(dirRoot.getPath()).relativize(Paths.get(dirFor.getPath()));
            if(dirFor.canRead())
            {
                return String.format(HTML_DIR_LISTING, replaceBackslash(pathFromRoot.toString()), pathFromRoot
                                                                                                      .getFileName());
            }
            else
            {
                return String.format(HTML_DIR_LISTING_DISABLE, replaceBackslash(pathFromRoot.toString()), pathFromRoot
                                                                                                      .getFileName());
            }
        }
        return String.format(HTML_DIR_ERROR, dirFor.getName());
    }

    public static String buildFileLink(File fileFor, File dirRoot)
    {
        if(fileFor.isFile())
        {
            Path pathFromRoot = Paths.get(dirRoot.getPath()).relativize(Paths.get(fileFor.getPath()));
            if(fileFor.canRead())
            {
                return String.format(HTML_FILE_LISTING, replaceBackslash(pathFromRoot.toString()), fileFor.getName());
            }
            else
            {
                return String.format(HTML_FILE_LISTING_DISABLE, replaceBackslash(pathFromRoot.toString()), fileFor.getName());
            }
        }
        return String.format(HTML_FILE_ERROR, fileFor.getName());
    }

    private static String replaceBackslash(String strFrom)
    {
        return strFrom.replace('\\', '/');
    }

    public static boolean getFileContent(File fileRequest, HttpExchange httpExchange) throws IOException
    {
        if
        (
            fileRequest.isFile()
            &&
           fileRequest.canRead()
        )
        {
            httpExchange.sendResponseHeaders(200, fileRequest.length());
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
        httpExchange.sendResponseHeaders(404, 0);
        return false;
    }
}
