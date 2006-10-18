package biz.aqute.lib.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipResource
    implements Resource
{

    private ZipFile zip;

    private ZipEntry entry;

    ZipResource( ZipFile zip, ZipEntry entry )
    {
        this.zip = zip;
        this.entry = entry;
    }

    public InputStream openInputStream()
        throws IOException
    {
        return zip.getInputStream( entry );
    }

    public String toString()
    {
        return ":" + entry.getName() + ":";
    }

    public static void build( Jar jar, File file )
        throws IOException
    {

        try
        {
            ZipFile zip = new ZipFile( file );
            for( Enumeration e = zip.entries(); e.hasMoreElements(); )
            {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if( !entry.isDirectory() )
                {
                    jar.putResource( entry.getName(), new ZipResource( zip, entry ) );
                }
            }
        } catch( FileNotFoundException e )
        {
            throw new IllegalArgumentException( "Problem opening JAR: " + file.getAbsolutePath() );
        }
    }

    public void write( OutputStream out )
        throws IOException
    {
        FileResource.copy( this, out );
    }
}
