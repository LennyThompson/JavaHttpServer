package com.lenny.apnicTest.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CmdLineParser
{
    public class CmdLineArg
    {
        private final String m_strArgName;
        private final String m_strArgValue;

        CmdLineArg(String strArgName, String strArgValue)
        {
            m_strArgName = strArgName;
            m_strArgValue = strArgValue;
        }

        public String name()
        {
            return m_strArgName;
        }
        public String value()
        {
            return m_strArgValue;
        }
        public int asIntValue()
        {
            try
            {
                return Integer.parseInt(m_strArgValue);
            }
            catch (NumberFormatException exc)
            {
                return Integer.MIN_VALUE;
            }
        }
        public String asQuotedString()
        {

            if(!m_strArgValue.isEmpty() && m_strArgValue.charAt(0) == '"')
            {
                return m_strArgValue.substring(1, m_strArgValue.length() - 2);
            }
            return m_strArgValue;
        }
    }
    private static Pattern ARG_PARSER = Pattern.compile("(.*)=(.*)");
    private Map<String, CmdLineArg> m_mapArgs;
    public CmdLineParser(String[] listArgs)
    {
        m_mapArgs = Arrays.stream(listArgs)
            .filter(strArg -> ARG_PARSER.matcher(strArg).matches())
            .map(strArg -> {
                Matcher matchArg = ARG_PARSER.matcher(strArg);
                if(matchArg.matches())
                {
                    return new CmdLineArg(matchArg.group(1), matchArg.group(2));
                }
                return new CmdLineArg("ERROR", strArg);
            })
            .collect(Collectors.toMap(CmdLineArg::name, cmdLineArg -> cmdLineArg ));
    }

    public CmdLineArg getArg(String strName)
    {
        if(m_mapArgs.containsKey(strName))
        {
            return m_mapArgs.get(strName);
        }
        return null;
    }
}

// https://github.com/LennyThompson/JavaHttpServer.git