package com.lenny.apnicTest.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Wrapper class for HttpServer
 */
public class Server
{
    /**
     * Define some basic HTTP error codes and response strings
     */
    private static int HTTP_404 = 404;
    private static String HTTP_ACCESS_DENIED = "Error 404: Access to %s denied";
    private static int HTTP_405 = 405;
    private static String HTTP_ILLEGAL_METHOD = "Error 405: Method %s not supported";
    private static final String FAVICON_ICO = "favicon.ico";

    private HttpServer m_httpServer;
    private int m_nPort;

    /**
     * Private constructor to ensure that creation paraters are consistent (currently only the port number)
     * @param nPortNo - port to attach the HttpServer to.
     * @throws IOException
     */
    private Server(int nPortNo) throws IOException
    {
        m_httpServer = HttpServer.create(new InetSocketAddress(nPortNo), 0);
        m_nPort = nPortNo;
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
        System.out.format
                (
                        "Starting new HttpServer on port: %d at root: %s\n",
                        m_nPort,
                        Paths.get("").toAbsolutePath().toString()
                );

        // Only handler is for root requests - all requests will be routed through this handler

        m_httpServer.createContext("/", new FileSystemHandler());
        m_httpServer.setExecutor(null);
        m_httpServer.start();
    }

    /**
     * Terminate the http server - assumes it is safe to stop a server that has never been started...
     */
    public void endServer()
    {
        if(m_httpServer != null)
        {
            System.out.format
                    (
                        "Terminating HttpServer on port: %d at root: %s\n",
                        m_nPort,
                        Paths.get("").toAbsolutePath().toString()
                    );
            m_httpServer.stop(10);
            m_httpServer = null;
            m_nPort  = 0;
        }
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
                    handleIncorrectRequest(httpExchange, HTTP_405, HTTP_ILLEGAL_METHOD, httpExchange.getRequestMethod());
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
            File fileCurr = Paths.get("").toAbsolutePath().toFile();
            Path pathRoot = Paths.get(fileCurr.getPath()).getRoot();
            File dirRoot = pathRoot.toAbsolutePath().toFile();
            String strResponse = "";
            // Treat a root request differently
            if(httpExchange.getRequestURI().getPath().compareTo("/") == 0 && httpExchange.getRequestURI().getQuery() == null)
            {
                File dirInit = Paths.get("").toAbsolutePath().toFile();
                FileSystemHtmlBuilder.getHtmlDirectoryResponse(dirRoot, dirInit, httpExchange);
            }
            else if(httpExchange.getRequestURI().getPath().indexOf(FAVICON_ICO)< 0)
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

                        if(!FileSystemHtmlBuilder.getFileContent(fileRequest, httpExchange))
                        {
                            handleIncorrectRequest(httpExchange, HTTP_404, HTTP_ACCESS_DENIED, fileRequest.getName());
                            return;
                        }
                    }
                }
                else
                {
                    // No such directory or file - respond with an error

                    handleIncorrectRequest(httpExchange, HTTP_404, HTTP_ACCESS_DENIED, fileRequest.getName());
                    return;
                }
            }
            httpExchange.getResponseBody().flush();
            httpExchange.close();
        }

        private void handleIncorrectRequest
            (
                HttpExchange httpExchange,
                int nErrorNo,
                String strErrorResponse,
                String strErrorExtra
            ) throws IOException
        {
            String strMsg = strErrorExtra.isEmpty() ? strErrorResponse : String.format(strErrorResponse, strErrorExtra);
            httpExchange.sendResponseHeaders(nErrorNo, strMsg.length());
            httpExchange.getResponseBody().write(strMsg.getBytes());
            httpExchange.getResponseBody().flush();
            httpExchange.close();
        }
    }
}
