/*
 * Copyright 2008 Alin Dreghiciu.
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
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.io.Pipe;
import org.ops4j.pax.runner.platform.internal.CommandLineBuilder;

/**
 * Default Java Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.6.1, December 09, 2008
 */
public class DefaultJavaRunner
    implements JavaRunner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( DefaultJavaRunner.class );

    /**
     * {@inheritDoc}
     */
    public void exec( final String[] vmOptions,
                      final String[] classpath,
                      final String mainClass,
                      final String[] programOptions,
                      final String javaHome,
                      final File workingDirectory )
        throws PlatformException
    {
        final CommandLineBuilder commandLine = new CommandLineBuilder()
            .append( getJavaExecutable( javaHome ) )
            .append( vmOptions )
            .append( "-cp" )
            .append( classpath )
            .append( mainClass )
            .append( programOptions );

        LOGGER.debug( "Start command line [" + Arrays.toString( commandLine.toArray() ) + "]" );

        executeProcess( commandLine.toArray(), workingDirectory );
    }

    /**
     * Executes the process that contains the platform. Separated to be able to override in unit tests.
     *
     * @param commandLine      an array that makes up the command line
     * @param workingDirectory the working directory for th eprocess
     *
     * @throws PlatformException re-thrown if something goes wrong with executing the process
     */
    void executeProcess( final String[] commandLine, final File workingDirectory )
        throws PlatformException
    {
        final Process frameworkProcess;
        try
        {
            frameworkProcess = Runtime.getRuntime().exec( commandLine, null, workingDirectory );
        }
        catch( IOException e )
        {
            throw new PlatformException( "Could not start up the process", e );
        }

        LOGGER.info( "Starting platform [" + this + "]. Runner has successfully finished his job!" );

        Thread shutdownHook = createShutdownHook( frameworkProcess );
        Runtime.getRuntime().addShutdownHook( shutdownHook );
        LOGGER.debug( "Added shutdown hook." );

        try
        {
            LOGGER.debug( "Waiting for framework exit." );
            frameworkProcess.waitFor();
        }
        catch( InterruptedException e )
        {
            LOGGER.debug( e );
        }
        finally
        {
            try
            {
                Runtime.getRuntime().removeShutdownHook( shutdownHook );
                LOGGER.debug( "Early shutdown." );
                shutdownHook.run();
            }
            catch( IllegalStateException e )
            {
                LOGGER.debug( "Shutdown already in progress." );
            }
        }
    }

    /**
     * Create helper thread to safely shutdown the external framework process
     *
     * @param process framework process
     *
     * @return stream handler
     */
    private Thread createShutdownHook( final Process process )
    {
        LOGGER.debug( "Wrapping stream I/O." );

        final Pipe errPipe = new Pipe( process.getErrorStream(), System.err ).start( "Error pipe" );
        final Pipe outPipe = new Pipe( process.getInputStream(), System.out ).start( "Out pipe" );
        final Pipe inPipe = new Pipe( process.getOutputStream(), System.in ).start( "In pipe" );

        return new Thread(
            new Runnable()
            {
                public void run()
                {
                    inPipe.stop();
                    outPipe.stop();
                    errPipe.stop();

                    try
                    {
                        process.destroy();
                    }
                    catch( Exception e )
                    {
                        // ignore if already shutting down
                    }
                }
            },
            "Pax-Runner shutdown hook"
        );
    }

    /**
     * Return path to java executable.
     *
     * @param javaHome java home directory
     *
     * @return path to java executable
     *
     * @throws PlatformException if java home could not be located
     */
    static String getJavaExecutable( final String javaHome )
        throws PlatformException
    {
        if( javaHome == null )
        {
            throw new PlatformException( "JAVA_HOME is not set." );
        }
        return javaHome + "/bin/java";
    }

}
