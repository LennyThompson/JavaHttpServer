package com.lenny.apnicTest;

import com.lenny.apnicTest.server.Server;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    private static Pattern PORT_MATCHER = Pattern.compile("PORT=(\\d+)");
    public static void main(String[] args)
    {
        Optional<String> optFindPORT = Arrays.stream(args)
                                             .filter(arg -> PORT_MATCHER.matcher(arg).matches())
                                             .findFirst();
        if(!optFindPORT.isPresent())
        {
            System.out.print("Incorrect usage - use command line argument PORT=<port number> and try again.");
            return;
        }

        Matcher matchPort = PORT_MATCHER.matcher(optFindPORT.get());

        if(matchPort.matches())
        {
            int nPort = Integer.parseInt(matchPort.group(1));
            Thread runServer = new Thread()
            {

                public void run()
                {
                    try
                    {
                        Server server = Server.createServer(nPort);
                        server.initServer();
                    }
                    catch (IOException exc)
                    {

                    }
                }
            };

            runServer.start();

            while (true) ;
        }
        else
        {
            System.out.print("Incorrect usage - the command line argument PORT=<port number> port number must be an integer.");
        }

    }
}
