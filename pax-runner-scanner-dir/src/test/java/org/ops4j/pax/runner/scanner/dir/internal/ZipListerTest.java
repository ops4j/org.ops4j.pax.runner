package org.ops4j.pax.runner.scanner.dir.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;

public class ZipListerTest extends ListerTest
{

    private ZipFile m_zip;
    private URL m_baseURL;

    @Before
    public void setUp()
        throws IOException
    {
        File file = FileUtils.getFileFromClasspath( "dirscanner.zip" );
        m_baseURL = file.toURL();
        m_zip = new ZipFile( file );
    }

    Lister createLister( Pattern filter )
    {
        return new ZipLister( m_baseURL, m_zip.entries(), filter );
    }

    URL asURL( String fileName )
        throws MalformedURLException
    {
        return new URL( "jar:" + m_baseURL.toExternalForm() + "!/" + fileName );
    }

    @Test( expected = IllegalArgumentException.class )
    public void cosntructorWithNullBaseURL()
        throws MalformedURLException
    {
        new ZipLister( null, m_zip.entries(), ParserImpl.parseFilter( "*" ) ).list();
    }

    @Test( expected = IllegalArgumentException.class )
    public void cosntructorWithNullZip()
        throws MalformedURLException
    {
        new ZipLister( m_baseURL, null, ParserImpl.parseFilter( "*" ) ).list();
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullFilter()
        throws MalformedURLException
    {
        new ZipLister( m_baseURL, m_zip.entries(), null ).list();
    }

}
