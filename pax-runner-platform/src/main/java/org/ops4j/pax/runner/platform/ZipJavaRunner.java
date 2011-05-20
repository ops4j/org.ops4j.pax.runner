/*
 * Copyright 2009 Toni Menzel.
 * Copyright 2009 Alin Dreghiciu.
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.io.StreamUtils;

/**
 * Extends {@link ScriptJavaRunner} and in plus zips the cvontent of working directory.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.20.0, April 27, 2009
 */
public class ZipJavaRunner
    extends ScriptJavaRunner
{

    /**
     * JCL.
     */
    private static final Log LOG = LogFactory.getLog( ZipJavaRunner.class );

    /**
     * {@inheritDoc}
     */
    public void exec( final String[] vmOptions,
                      final String[] classpath,
                      final String mainClass,
                      final String[] programOptions,
                      final String javaHome,
                      final File workingDir,
                      String[] environmentVariables
                      )
        throws PlatformException
    {
    	super.exec( vmOptions, classpath, mainClass, programOptions, javaHome, workingDir, environmentVariables );
        LOG.info( "Now writing distribution zip.." );
        ZipOutputStream dest = null;
        try
        {
            File destFile = new File( "paxrunner-" + workingDir.getName() + ".zip" );
            dest = new ZipOutputStream( new FileOutputStream( destFile ) );
            int baseIdx = workingDir.getCanonicalFile().getParentFile().getCanonicalPath().length() + 1;
            add( baseIdx, workingDir, dest );
            // delete folder:
            workingDir.deleteOnExit();
            LOG.info( "Distribution written: " + destFile.getName() );
        }
        catch( FileNotFoundException e )
        {
            throw new PlatformException( e.getMessage(), e );
        }
        catch( IOException e )
        {
            throw new PlatformException( e.getMessage(), e );
        }
        finally
        {
            try
            {
                if( dest != null )
                {
                    dest.close();
                }
            }
            catch( IOException e )
            {
                // quiet.
            }
        }
    }

    /**
     * Adds a file to zip
     *
     * @param baseIdx base index
     * @param file    file to add
     * @param dest    destination zip
     */
    private void add( final int baseIdx,
                      final File file,
                      final ZipOutputStream dest )
        throws IOException
    {
        if( file.isFile() )
        {
            String art = file.getCanonicalPath().substring( baseIdx );
            ZipEntry entry = new ZipEntry( art );
            dest.putNextEntry( entry );
            StreamUtils.copyStream( new FileInputStream( file ), dest, false );
        }
        else if( file.isDirectory() )
        {
            for( File f : file.listFiles() )
            {
                add( baseIdx, f, dest );
            }
        }
    }

}
