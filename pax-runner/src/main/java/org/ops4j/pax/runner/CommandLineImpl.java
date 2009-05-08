/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ops4j.pax.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of Command Line.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class CommandLineImpl implements CommandLine
{

    /**
     * Option profix.
     */
    private static final String OPTION_PREFIX = "--";
    /**
     * Option pattern.
     */
    public static final Pattern OPTION_PATTERN = Pattern.compile( "(.*?)=(.*)" );
    /**
     * Default arguments file name.
     */
    private static final String DEFAULT_ARGS_FILE_NAME = "runner.args";

    /**
     * Default line comment for DEFAULT_ARGS_FILE_NAME files
     */
    private static final char LINE_COMMENT_PREFIX = '#';

    /**
     * Default character, the presence of this at the end of line indicates continuity 
     */
    private static final String LINE_CONTINUE_CHAR = "\\";

    /**
     * Options as properties.
     */
    private final Map<String, List<String>> m_options;
    /**
     * List of arguments.
     */
    private final List<String> m_arguments;
    /**
     * URL of configuration file (if any);
     */
    private final String m_localArgsURL;
    /**
     * URL of global configuration file (if any);
     */
    private final String m_globalArgsURL;

    /**
     * Creates a new Command line by parsing every argument into an option or argument.
     *
     * @param args an array of arguments to be parsed
     */
    public CommandLineImpl( final String... args )
    {
        m_options = new HashMap<String, List<String>>();
        m_arguments = new ArrayList<String>();
        parseArguments( args == null ? Collections.<String>emptyList() : Arrays.asList( args ) );

        final String argsURL = getOption( "args" );
        boolean useArgsFile = argsURL == null || !argsURL.equalsIgnoreCase( "false" );

        m_localArgsURL = useArgsFile ? parseLocalArgs() : null;
        m_globalArgsURL = useArgsFile ? parseGlobalArgs() : null;
    }

    /**
     * Parse arguments form local arguments. This can be specified by using a property named "args" or if not specified
     * a default ./runner.args will be searched.
     *
     * @return url of local args file or null if not set and no default found
     */
    private String parseLocalArgs()
    {
        String argsURL = getOption( "args" );
        if( argsURL == null )
        {
            // use a default args file if available
            final File defaultArgsFile = new File( DEFAULT_ARGS_FILE_NAME );
            if( defaultArgsFile.exists() )
            {
                try
                {
                    argsURL = defaultArgsFile.toURL().toExternalForm();
                }
                catch( MalformedURLException ignore )
                {
                    // ignore as this should not happen
                }
            }
        }
        if( argsURL != null )
        {
            try
            {
                parseArguments( readTextFile( new URL( argsURL ), true ) );
            }
            catch( IOException e )
            {
                throw new RuntimeException( "Arguments could not be read from [" + argsURL + "]", e );
            }
        }
        return argsURL;
    }

    /**
     * Parse arguments from global user arguments. This can be specified by using a property named "globalArgs" or if
     * not specified a default ${user.home}/.pax/runner/runner.args will be searched.
     *
     * @return url of global args file or null if not set and no default found
     */
    private String parseGlobalArgs()
    {
        String globalArgsURL = getOption( "globalArgs" );
        String userHome = System.getProperty( "user.home" );
        if( globalArgsURL == null && userHome != null )
        {
            // use a default
            final File defaultGlobalArgsFile = new File(
                userHome + File.separator + ".pax" + File.separator + "runner" + File.separator + DEFAULT_ARGS_FILE_NAME
            );
            if( defaultGlobalArgsFile.exists() )
            {
                try
                {
                    globalArgsURL = defaultGlobalArgsFile.toURL().toExternalForm();
                }
                catch( MalformedURLException ignore )
                {
                    // ignore as this should not happen
                }
            }
        }
        if( globalArgsURL != null )
        {
            try
            {
                parseArguments( readTextFile( new URL( globalArgsURL ), true ) );
            }
            catch( IOException e )
            {
                throw new RuntimeException( "Arguments could not be read from [" + globalArgsURL + "]", e );
            }
        }
        return globalArgsURL;
    }

    /**
     * Parses a list of arguments.
     *
     * @param args a list of arguments
     */
    private void parseArguments( List<String> args )
    {
        for( String arg : args )
        {
            if( arg.startsWith( OPTION_PREFIX ) )
            {
                parseOption( arg );
            }
            else
            {
                parseArgument( arg );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getOption( final String key )
    {
        final List<String> values = m_options.get( key );
        return values == null || values.size() == 0 ? null : values.get( 0 );
    }

    /**
     * {@inheritDoc}
     */
    public String[] getMultipleOption( final String key )
    {
        final List<String> values = m_options.get( key );
        return values == null || values.size() == 0 ? new String[0] : values.toArray( new String[values.size()] );
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getArguments()
    {
        return m_arguments;
    }

    /**
     * {@inheritDoc}
     */
    public String getArgumentsFileURL()
    {
        return m_localArgsURL;
    }

    /**
     * Parses an option of type --name=value
     *
     * @param arg a command line argument to be parsed
     */
    private void parseOption( final String arg )
    {
        String key = arg.substring( 2 ).trim();
        if( key != null && key.length() > 0 )
        {
            String value = null;
            final Matcher matcher = OPTION_PATTERN.matcher( key );
            if( matcher.matches() && matcher.groupCount() == 2 )
            {
                key = matcher.group( 1 );
                value = matcher.group( 2 );
            }
            if( value == null )
            {
                value = "true";
                if( key.startsWith( "no" ) && key.length() > 2 )
                {
                    String actualKey = key.substring( 2, 3 ).toLowerCase();
                    if( key.length() >= 3 )
                    {
                        key = actualKey + key.substring( 3 );
                    }
                    value = "false";
                }
            }
            List<String> values = m_options.get( key );
            if( values == null )
            {
                values = new ArrayList<String>();
                m_options.put( key, values );
            }
            values.add( value );
        }
    }

    /**
     * Parses an argument (does not start with --).
     *
     * @param arg a command line argument to be parsed
     */
    private void parseArgument( final String arg )
    {
        if( !m_arguments.contains( arg ) )
        {
            m_arguments.add( arg );
        }
    }

    /**
     * Reads content of a text files and returns every line as an entry to a List.
     *
     * @param fileURL        url of the file to be read
     * @param skipEmptyLines if empty lines should be skippied
     *
     * @return a list of strings, one entry for each line (depending if it should skip empty lines or not)
     *
     * @throws IOException re-thrown if an exception appear during processing of input stream
     */
    private static List<String> readTextFile( final URL fileURL, final boolean skipEmptyLines )
        throws IOException
    {
        final List<String> content = new ArrayList<String>();
        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader( new InputStreamReader( fileURL.openStream() ) );
            String line;
            StringBuffer entry = new StringBuffer();
            boolean readMore = false;
            while( ( line = bufferedReader.readLine() ) != null )
            {
                if( ( !skipEmptyLines || line.trim().length() > 0 ) && line.charAt( 0 ) != LINE_COMMENT_PREFIX )
                {
                    if (line.endsWith(LINE_CONTINUE_CHAR)) {
                        entry.append(line.substring(0, line.length() - 1));
                        continue;
                    } else {
                        entry.append(line);
                        content.add( entry.toString().trim() );
                        entry.delete(0, entry.length());
                        continue;
                    }
                }
                if (line.trim().length() == 0 && entry.length() > 0) {
                    content.add( entry.toString().trim() );
                    entry.delete(0, entry.length());
                }
            }
            if (entry != null && entry.length()>0) {
                content.add( entry.toString().trim() );
            }
        }
        finally
        {
            if( bufferedReader != null )
            {
                bufferedReader.close();
            }
        }
        return content;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if( m_localArgsURL != null )
        {
            builder.append( " and " ).append( m_localArgsURL );
        }
        if( m_globalArgsURL != null )
        {
            builder.append( " and " ).append( m_globalArgsURL );
        }
        if( builder.length() > 0 )
        {
            builder.insert( 0, "Using arguments from command line" );
        }
        else
        {
            builder.append( "Using only arguments from command line" );
        }
        return builder.toString();
    }

    private String toStringAdvanced()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Arguments: " );
        for( String entry : m_arguments )
        {
            builder
                .append( "[" )
                .append( entry )
                .append( "]" );
        }
        builder.append( "Options: " );
        for( Map.Entry<String, List<String>> entry : m_options.entrySet() )
        {
            builder
                .append( "[" )
                .append( entry.getKey() )
                .append( "=" )
                .append( entry.getValue() )
                .append( "]" );
        }
        return builder.toString();
    }

}
