package com.lenny.apnicTest.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;

/**
 * Wrapper class for HttpServer
 */
public class Server
{
    HttpServer m_httpServer;

    /**
     * Private constructor to ensure that creation paraters are consistent (currently only the port number)
     * @param nPortNo - port to attach the HttpServer to.
     * @throws IOException
     */
    private Server(int nPortNo) throws IOException
    {
        m_httpServer = HttpServer.create(new InetSocketAddress(nPortNo), 0);
    }

    /**
     * Factory method to create a Server instance given a port number.
     * TODO - confirm the port number is realistic (within a specific range, and not already taken)
     * @param nPort - the port number
     * @return New server instance, or null
     */
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

    /**
     * Initialise the server to start listening
     * @throws IOException
     */
    public void initServer() throws IOException
    {
        // Only handler is for root requests - all requests will be routed through this handler

        m_httpServer.createContext("/", new FileSystemHandler());
        m_httpServer.setExecutor(null);
        m_httpServer.start();
    }

    /**
     * Helper class for handler for requests.
     * Interprets all requests as paths to files or directories
     * Will respond with an error if the method is not 'GET'.
     */
    private class FileSystemHandler implements HttpHandler
    {
        /**
         * Implementation of the interface - handle requests
         * @param httpExchange - Contains the request / reponse
         * @throws IOException
         */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException
        {
            // 'Log' the request...

            System.out.println(
                String.format("Recieved request: %s Method: %s",
                              httpExchange.getRequestURI().getPath(),
                              httpExchange.getRequestMethod())
              );

            // ONly respond to 'GET' requests

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

        /**
         * Helper to respond to request.
         * @param httpExchange - request / response object
         * @throws IOException
         */
        private void handleGetRequest(HttpExchange httpExchange) throws IOException
        {
            File dirRoot = Paths.get("").toAbsolutePath().toFile();
            String strResponse = "";
            // Treat a root request differently
            if(httpExchange.getRequestURI().getPath().compareTo("/") == 0)
            {
                FileSystemHtmlBuilder.getHtmlDirectoryResponse(dirRoot, dirRoot, httpExchange);
            }
            else
            {
                // Otherwise determine if the request is for file or a directory

                File fileRequest = new File(dirRoot, httpExchange.getRequestURI().getPath());
                if(fileRequest.exists())
                {
                    if (fileRequest.isDirectory())
                    {
                        // Respond w3ith directory content

                        FileSystemHtmlBuilder.getHtmlDirectoryResponse(dirRoot, fileRequest, httpExchange);
                    }
                    else
                    {
                        // Respond with file content

                        FileSystemHtmlBuilder.getFileContent(fileRequest, httpExchange);
                    }
                }
                else
                {
                    // No such directory or file - respond with an error

                    handleIncorrectRequest(httpExchange);
                    return;
                }
            }
            httpExchange.getResponseBody().flush();
            httpExchange.close();
        }

        private void handleIncorrectRequest(HttpExchange httpExchange)
        {
            // TODO...
        }
    }
}
