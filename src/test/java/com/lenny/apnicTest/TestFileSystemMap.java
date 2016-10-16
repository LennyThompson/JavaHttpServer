package com.lenny.apnicTest;

import com.lenny.apnicTest.server.FileSystemHtmlBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestFileSystemMap
{
    @Before
    public void initFileSystem() throws IOException
    {
        // Add a little directory structure...

        new File("root").mkdir();
        new File("root/dir_top_1").mkdir();
        new File("root/dir_top_2").mkdir();
        new File("root/dir_top_1/dir_mid_1").mkdir();
        new File("root/dir_top_1/dir_mid_1/dir_bot_1").mkdir();
        new File("root/dir_top_2/dir_mid_1").mkdir();
        new File("root/dir_top_2/dir_mid_2").mkdir();

        // And add some files

        new File("root/index.html").createNewFile();
        new File("root/dir_top_2/index.html").createNewFile();
        new File("root/dir_top_1/dir_mid_1/dir_bot_1/index.html").createNewFile();
        new File("root/dir_top_2/dir_mid_2/index.html").createNewFile();
    }

    @After
    public void removeFileSystem()
    {
        // Remove the files

        new File("root/dir_top_2/dir_mid_2/index.html").delete();
        new File("root/dir_top_1/dir_mid_1/dir_bot_1/index.html").delete();
        new File("root/dir_top_2/index.html").delete();
        new File("root/index.html").delete();

        // Remove the directory

        new File("root/dir_top_2/dir_mid_2").delete();
        new File("root/dir_top_2/dir_mid_1").delete();
        new File("root/dir_top_1/dir_mid_1/dir_bot_1").delete();
        new File("root/dir_top_1/dir_mid_1").delete();
        new File("root/dir_top_2").delete();
        new File("root/dir_top_1").delete();
        new File("root").delete();
    }

    @Test
    public void testBuildDirectoryLink()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = Paths.get("root/dir_top_1").toFile();
        String strTemp = FileSystemHtmlBuilder.buildDirectoryLink(dirSrc, dirRoot);

        assertTrue(!strTemp.isEmpty());
        assertEquals("<li><i class=\"material-icons\" style=\"color:blue\">folder_open</i><a id=\"dir-dir_top_1\" href=\"/dir_top_1\">dir_top_1</a></li>\n", strTemp);
    }

    @Test
    public void testBuildDeepDirectoryLink()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = Paths.get("root/dir_top_1/dir_mid_1/dir_bot_1").toFile();
        String strTemp = FileSystemHtmlBuilder.buildDirectoryLink(dirSrc, dirRoot);

        assertTrue(!strTemp.isEmpty());
        assertEquals("<li><i class=\"material-icons\" style=\"color:blue\">folder_open</i><a id=\"dir-dir_bot_1\" href=\"/dir_top_1/dir_mid_1/dir_bot_1\">dir_bot_1</a></li>\n", strTemp);
    }

    @Test
    public void testBuildDirectoryLinkError()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = new File("root/index.html");
        String strTemp = FileSystemHtmlBuilder.buildDirectoryLink(dirSrc, dirRoot);

        assertTrue(!strTemp.isEmpty());
        assertEquals("<p style=\"color:red\">ERROR: index.html is not a directory!</p>\n", strTemp);
    }

    @Test
    public void testBuildFileLink()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = new File("root/index.html");
        String strTemp = FileSystemHtmlBuilder.buildFileLink(dirSrc, dirRoot);

        assertTrue(!strTemp.isEmpty());
        assertEquals("<li><i class=\"material-icons\" style=\"color:orange\">format_align_justify</i><a id=\"file-index.html\" href=\"/index.html\">index.html</a></li>\n", strTemp);
    }

    @Test
    public void testBuildFileLinkError()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = new File("root/dir_top_2");
        String strTemp = FileSystemHtmlBuilder.buildFileLink(dirSrc, dirRoot);

        assertTrue(!strTemp.isEmpty());
        assertEquals("<p style=\"color:red\">ERROR: dir_top_2 is not a file!</p>\n", strTemp);
    }

    @Test
    public void testSubDirectorySearch()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = new File("root/dir_top_2");
        List<String> listHTML = FileSystemHtmlBuilder.getHtmlDirectoryStructure(dirRoot, dirSrc);

        assertTrue(listHTML.size() == 3);
    }

    @Test
    public void testHtmlOutput()
    {
        File dirRoot = Paths.get("root").toFile();
        File dirSrc = new File("root/dir_top_2");
        String strHTML = FileSystemHtmlBuilder.getHtmlFileSystem(dirRoot, dirSrc);

        assertTrue(!strHTML.isEmpty());
    }

}
