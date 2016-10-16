package com.lenny.apnicTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class TestFileSystemE2E
{
    public static final String USERNAME = "LennyThompson";
    public static final String ACCESS_KEY = "b90ba3c3-2747-4524-8d2d-5c81b63d94ce";
    public static final String SAUCE_URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@localhost:4445/wd/hub";

    private static String CLASSSPATH = "/target/classes";
    private static String JAVA_CMDLINE = "java";
    private static String CLASSPATH_CMDLINE = "-cp";
    private static String PORT_CMDLINE = "PORT=5555";
    private static String WEBDRIVER_INIT = "webdriver.chrome.driver";
    private static String WEBDRIVER_PATH = "C:/WebDriver/Chrome/chromedriver.exe";
    private static String LOCALHOST_URL = "localhost:5555";

    Process m_processHttpServer;
    @Before
    public void startProcess() throws IOException
    {
        String strPath = Paths.get("").toAbsolutePath().toString();
        String strClasspath = strPath + CLASSSPATH;
        String strClassName = Main.class.getCanonicalName();
        ProcessBuilder builder = new ProcessBuilder(JAVA_CMDLINE, CLASSPATH_CMDLINE, strClasspath, strClassName, PORT_CMDLINE);
        m_processHttpServer = builder.start();
    }

    @After
    public void endProcess()
    {
        if(m_processHttpServer != null)
        {
            m_processHttpServer.destroy();
            m_processHttpServer = null;
        }
    }

    /**
     * Simple test to ensure the first page is loaded correctly.
     * Will run at the root of the project, so can simply look for stuff that is there...
     */
    @Test
    public void testStartSelenium() throws MalformedURLException
    {
        DesiredCapabilities caps = DesiredCapabilities.chrome();

        WebDriver webDriver = new RemoteWebDriver(new URL(SAUCE_URL), caps);
        webDriver.navigate().to(LOCALHOST_URL);
        assertEquals("Title", webDriver.getTitle());
        List<WebElement> listLinks = webDriver.findElements(By.tagName("a"));
        assertFalse(listLinks.isEmpty());
        for(WebElement eleLnk : listLinks)
        {
            assertNotNull(eleLnk.getAttribute("href"));
        }

        WebElement webElement = webDriver.findElement(By.id("dir-target"));
        assertNotNull(webElement);
        assertEquals("target", webElement.getText());

        webElement = webDriver.findElement(By.id("file-pom.xml"));
        assertNotNull(webElement);
        assertEquals("pom.xml", webElement.getText());

        webDriver.close();
    }

    /**
     * Test that navigating to subdirectory updates the page.
     */
    @Test
    public void testGotoSubdirectorySelenium()
    {
        System.setProperty(WEBDRIVER_INIT, WEBDRIVER_PATH);
        WebDriver webDriver = new ChromeDriver();
        webDriver.navigate().to(LOCALHOST_URL);
        assertEquals("Title", webDriver.getTitle());

        webDriver.findElement(By.id("dir-target")).click();

        WebElement webElement = webDriver.findElement(By.id("dir-classes"));
        assertNotNull(webElement);
        assertEquals("classes", webElement.getText());

        webDriver.close();
    }
}
