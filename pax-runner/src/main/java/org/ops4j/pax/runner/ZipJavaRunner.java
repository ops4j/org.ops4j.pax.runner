package org.ops4j.pax.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.ScriptJavaRunner;
import org.ops4j.io.StreamUtils;

/**
 * @author Toni Menzel (tonit)
 * @since Apr 27, 2009
 */
public class ZipJavaRunner extends ScriptJavaRunner implements JavaRunner
{

    private static final Log LOG = LogFactory.getLog( ZipJavaRunner.class );

    public void exec( String[] vmOptions, String[] classpath, String mainClass, String[] programOptions,
                      String javaHome, File workingDir )
        throws PlatformException
    {
        super.exec( vmOptions, classpath, mainClass, programOptions, javaHome, workingDir );
        // zip it up:
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

    private void add( int baseIdx, File file, ZipOutputStream dest )
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
