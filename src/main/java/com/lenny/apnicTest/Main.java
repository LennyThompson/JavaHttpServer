package com.lenny.apnicTest;

import com.lenny.apnicTest.server.CmdLineParser;
import com.lenny.apnicTest.server.Server;

import java.io.IOException;

public class Main
{
    private static String PORT_ARG = "PORT";
    public static void main(String[] args) throws IOException
    {
        CmdLineParser cmdLineParser = new CmdLineParser(args);
        if(cmdLineParser.getArg(PORT_ARG) == null)
        {
            System.out.print("Incorrect usage - use command line argument PORT=<port number> and try again.");
            return;
        }

        Thread runServer = null;
        if(cmdLineParser.getArg(PORT_ARG).asIntValue() != Integer.MIN_VALUE)
        {
            runServer = new Thread()
            {

                public void run()
                {
                    try
                    {
                        Server server = Server.createServer(cmdLineParser.getArg(PORT_ARG).asIntValue());
                        server.initServer();
                    }
                    catch (IOException exc)
                    {

                    }
                }
            };

            runServer.start();

        }
        else
        {
            System.out.print("Incorrect usage - the command line argument PORT=<port number> port number must be an integer.");
        }
        // Terminate on any command line input.

        System.out.println("Enter any character to terminate.");
        System.in.read();
    }
}
