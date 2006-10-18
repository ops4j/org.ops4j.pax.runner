package biz.aqute.lib.osgi;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EmbeddedResource
    implements Resource
{

    byte[] data;

    public EmbeddedResource( byte[] data )
    {
        this.data = data;
    }

    public InputStream openInputStream()
        throws FileNotFoundException
    {
        return new ByteArrayInputStream( data );
    }

    public void write( OutputStream out )
        throws IOException
    {
        out.write( data );
    }

    public String toString()
    {
        return ":" + data.length + ":";
    }

    public static void build( Jar jar, InputStream in )
        throws IOException
    {
        ZipInputStream jin = new ZipInputStream( in );
        ZipEntry entry = jin.getNextEntry();
        while( entry != null )
        {
            if( !entry.isDirectory() )
            {
                byte[] data = collect( jin, 0 );
                jar.putResource( entry.getName(), new EmbeddedResource( data ) );
            }
            entry = jin.getNextEntry();
        }
    }

    /**
     * Convenience method to turn an inputstream into a byte array. The method
     * uses a recursive algorithm to minimize memory usage.
     *
     * @param in     stream with data
     * @param offset where we are in the stream
     *
     * @return byte array filled with data
     * @throws java.io.IOException if an IO problem occurs.
     */
    static byte[] collect( InputStream in, int offset )
        throws IOException
    {
        byte[] result;
        byte[] buffer = new byte[10000];
        int size = in.read( buffer );
        if( size <= 0 )
        {
            return new byte[offset];
        }
        else
        {
            result = collect( in, offset + size );
        }
        System.arraycopy( buffer, 0, result, offset, size );
		return result;
	}

}
