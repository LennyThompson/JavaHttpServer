package com.lenny.apnicTest.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server
{
    HttpServer m_httpServer;

    private Server(int nPortNo) throws IOException
    {
        m_httpServer = HttpServer.create(new InetSocketAddress(nPortNo), 0);
    }

    public static Server createServer(int nPort)
    {
        Server serverNew = null;
        if(nPort > 1023)    // No particular privileges required
        {
            try
            {
                serverNew = new Server(nPort);
            }
            catch(IOException exc)
            {

            }
        }

        return serverNew;
    }

    public void initServer() throws IOException
    {
        m_httpServer.createContext("/", new FileSystemHandler());
        m_httpServer.setExecutor(null);
        m_httpServer.start();
    }

    private class FileSystemHandler implements HttpHandler
    {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException
        {
            String strTemp = httpExchange.getRequestURI().getPath();
            System.out.println(String.format("Recieved request: %s Method: %s", strTemp, httpExchange.getRequestMethod()));
            switch(httpExchange.getRequestMethod())
            {
                case "GET":
                    handleGetRequest(httpExchange);
                    break;
                case "PUT":
                case "POST":
                case "DELETE":
                case "":
                default:
                    handleIncorrectRequest(httpExchange);
                    break;
            }
        }

        private void handleGetRequest(HttpExchange httpExchange) throws IOException
        {
            File dirRoot = Paths.get("").toAbsolutePath().toFile();
            String strResponse = "";
            if(httpExchange.getRequestURI().getPath().compareTo("/") == 0)
            {
                FileSystemHtmlBuilder.getHtmlDirectoryResponse(dirRoot, dirRoot, httpExchange);
            }
            else
            {
                File fileRequest = new File(dirRoot, httpExchange.getRequestURI().getPath());
                if(fileRequest.isDirectory())
                {
                    FileSystemHtmlBuilder.getHtmlDirectoryResponse(dirRoot, fileRequest, httpExchange);
                }
                else
                {
                    FileSystemHtmlBuilder.getFileContent(fileRequest, httpExchange);
                }
            }
            httpExchange.getResponseBody().flush();
            httpExchange.close();
        }

        private void handleIncorrectRequest(HttpExchange httpExchange)
        {
        }
    }
}
