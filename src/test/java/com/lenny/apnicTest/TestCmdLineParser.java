package com.lenny.apnicTest;

import com.lenny.apnicTest.server.CmdLineParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCmdLineParser
{
    @Test
    public void testSimpleCmdLine()
    {
        String[] listArgs = new String[]{ "PORT=1234" };
        CmdLineParser cmdLineParser = new CmdLineParser(listArgs);
        assertNotNull(cmdLineParser.getArg("PORT"));
        assertEquals("1234", cmdLineParser.getArg("PORT").value());
        assertEquals(1234, cmdLineParser.getArg("PORT").asIntValue());
    }

    @Test
    public void testSimpleFailCmdLine()
    {
        String[] listArgs = new String[]{ "PORT=port1" };
        CmdLineParser cmdLineParser = new CmdLineParser(listArgs);
        assertNotNull(cmdLineParser.getArg("PORT"));
        assertEquals("port1", cmdLineParser.getArg("PORT").value());
        assertEquals(Integer.MIN_VALUE, cmdLineParser.getArg("PORT").asIntValue());
    }
}
