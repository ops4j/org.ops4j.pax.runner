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
    implements JavaRunner
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

    public void exec( String[] vmOptions, String[] classpath, String mainClass, String[] programOptions, String javaHome, File workingDirectory, String[] environmentVariables )
        throws PlatformException
    {
        final StringBuilder batchCp = new StringBuilder();
        final StringBuilder shellCp = new StringBuilder();

        for( String path : classpath ) {
            // create Windows-specific cp
            if( batchCp.length() != 0 ) {
                batchCp.append( ';' );
            }
            batchCp.append( path );

            // create UNIX-specific cp
            if( shellCp.length() != 0 ) {
                shellCp.append( ':' );
            }
            shellCp.append( path );
        }

        final CommandLineBuilder batchCommandLine = new CommandLineBuilder()
            .append( "java" )
            .append( vmOptions )
            .append( "-cp" )
            .append( batchCp.toString() )
            .append( mainClass )
            .append( programOptions );
        final CommandLineBuilder shellCommandLine = new CommandLineBuilder()
            .append( "java" )
            .append( vmOptions )
            .append( "-cp" )
            .append( shellCp.toString() )
            .append( mainClass )
            .append( programOptions );

        LOG.debug( "Start UNIX command line [" + Arrays.toString( shellCommandLine.toArray() ) + "]" );
        LOG.debug( "Start WIN command line [" + Arrays.toString( batchCommandLine.toArray() ) + "]" );

        final String shell = getShellScript( shellCommandLine, environmentVariables );
        final String batch = getBatchScript( batchCommandLine, environmentVariables );

        try {
            LOG.debug( "Writing run scripts.." );

            FileWriter sh = new FileWriter( new File( workingDirectory, "run.sh" ) );
            sh.write( shell );
            sh.close();

            FileWriter bat = new FileWriter( new File( workingDirectory, "run.bat" ) );
            bat.write( batch );
            bat.close();
            LOG.debug( "Success writing run scripts." );
        } catch( IOException e ) {
            throw new PlatformException( "Could not write run scripts", e );
        }
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
        exec( vmOptions, classpath, mainClass, programOptions, javaHome, workingDirectory, new String[ 0 ] );
    }

    /**
     * Create a *nix script.
     *
     * @param commandLine          command line builder
     * @param environmentVariables
     *
     * @return shell script
     */
    private String getShellScript( final CommandLineBuilder commandLine, String[] environmentVariables )
    {
        final String newline = "\n";
        final StringBuilder script = new StringBuilder();
        script.append( "#!/bin/sh" );
        script.append( newline );

        if (environmentVariables != null ) {
	        for( String env : environmentVariables ) {
	            script.append( env );
	            script.append( newline );
	        }
        }

        for( String s : commandLine.toArray() ) {
            script.append( s );
            script.append( " " );
        }
        script.append( "\"$@\"" );
        script.append( newline );
        return script.toString();
    }

    /**
     * Create a windows script.
     *
     * @param commandLine          command line builder
     * @param environmentVariables
     *
     * @return batch script
     */
    private String getBatchScript( final CommandLineBuilder commandLine, String[] environmentVariables )
    {
        final String newline = "\r\n";
        final StringBuilder script = new StringBuilder();
        script.append( newline );
        if (environmentVariables != null ) {
	        for( String env : environmentVariables ) {
	            script.append( "SET " );
	            script.append( env );
	            script.append( newline );
	        }
        }
        for( String s : commandLine.toArray() ) {
            script.append( s );
            script.append( " " );
        }
        script.append( "%*" );
        script.append( newline );
        return script.toString();
    }

}
