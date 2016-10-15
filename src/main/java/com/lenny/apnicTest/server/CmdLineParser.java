package com.lenny.apnicTest.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Lenny
 * Parses command lines in for of <Name>=<Value> that can then be referenced by name
 */
public class CmdLineParser
{
    /**
     * Helper class for individual arguments, and name value pair
     */
    public class CmdLineArg
    {
        private final String m_strArgName;
        private final String m_strArgValue;

        CmdLineArg(String strArgName, String strArgValue)
        {
            m_strArgName = strArgName;
            m_strArgValue = strArgValue;
        }

        /**
         * Get the argument name
         * @return name
         */
        public String name()
        {
            return m_strArgName;
        }

        /**
         * Get the raw argument value
         * @return value as string
         */
        public String value()
        {
            return m_strArgValue;
        }

        /**
         * Get teh value as an integer
         * @return the value parsed to an in or Ingteger.MIN_VALUE if the value is not an int
         */
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

        /**
         * Remoces quotes from a quoted string argument
         * @return Returns the string value with quotes stripped
         */
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

    /**
     * Construct the parser from a list of aommand line arguments
     * @param listArgs - list of args from main
     */
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

    /**
     * Return the CmdLineArg object for the argument name
     * @param strName - name of the command line argument
     * @return - the comand line argumgent or null if none found
     */
    public CmdLineArg getArg(String strName)
    {
        if(m_mapArgs.containsKey(strName))
        {
            return m_mapArgs.get(strName);
        }
        return null;
    }
}
