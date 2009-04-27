/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Mike Smoot.
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.platform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.internal.CommandLineBuilder;

/**
 * A Java Runner that writes shell and batch scripts in the
 * runner directory that start the specified OSGi framework.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Mike Smoot (msmoot@ucsd.edu)
 */
public class ScriptJavaRunner
    implements StoppableJavaRunner
{

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog( ScriptJavaRunner.class );

    /**
     * Constructor.
     */
    public ScriptJavaRunner()
    {
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void exec( final String[] vmOptions,
                                   final String[] classpath,
                                   final String mainClass,
                                   final String[] programOptions,
                                   final String javaHome,
                                   final File workingDirectory )
        throws PlatformException
    {
        final StringBuilder cp = new StringBuilder();

        for( String path : classpath )
        {
            if( cp.length() != 0 )
            {
                cp.append( File.pathSeparator );
            }
            cp.append( path );
        }

        final CommandLineBuilder commandLine = new CommandLineBuilder()
            .append( "java" )
            .append( vmOptions )
            .append( "-cp" )
            .append( cp.toString() )
            .append( mainClass )
            .append( programOptions );

        LOG.debug( "Start command line [" + Arrays.toString( commandLine.toArray() ) + "]" );

        final String shell = getShellScript( commandLine );
        final String batch = getBatchScript( commandLine );

        try
        {
            LOG.debug( "Writing run scripts." );

            FileWriter sh = new FileWriter( new File( workingDirectory, "run.sh" ) );
            sh.write( shell );
            sh.close();

            FileWriter bat = new FileWriter( new File( workingDirectory, "run.bat" ) );
            bat.write( batch );
            bat.close();
        }
        catch( IOException e )
        {
            throw new PlatformException( "Could not write run scripts", e );
        }
    }

    public void shutdown()
    {
    }

    private String getShellScript( final CommandLineBuilder commandLine )
    {
        final String newline = "\n";
        final StringBuilder script = new StringBuilder();
        script.append( "#!/bin/sh" );
        script.append( newline );
        for( String s : commandLine.toArray() )
        {
            script.append( s );
            script.append( " " );
        }
        script.append( "\"$@\"" );
        script.append( newline );
        return script.toString();
    }

    private String getBatchScript( final CommandLineBuilder commandLine )
    {
        final String newline = "\r\n";
        final StringBuilder script = new StringBuilder();
        script.append( newline );
        for( String s : commandLine.toArray() )
        {
            script.append( s );
            script.append( " " );
        }
        script.append( "%*" );
        script.append( newline );
        return script.toString();
    }
}
