package org.ops4j.pax.runner.scanner.dir.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.runner.commons.file.FileUtils;

public class DirectoryListerTest
    extends ListerTest
{

    private File m_dir;

    @Before
    public void setUp()
    {
        m_dir = FileUtils.getFileFromClasspath( "dirscanner" );
    }

    Lister createLister( Pattern filter )
    {
        return new DirectoryLister( m_dir, filter );
    }

    URL asURL( String fileName )
        throws MalformedURLException
    {
        return new File( m_dir, fileName ).toURL();
    }

    @Test( expected = IllegalArgumentException.class )
    public void cosntructorWithNullDir()
        throws MalformedURLException
    {
        new DirectoryLister( null, ParserImpl.parseFilter( "*" ) ).list();
    }

    @Test( expected = IllegalArgumentException.class )
    public void cosntructorWithNullFilter()
        throws MalformedURLException
    {
        new DirectoryLister( m_dir, null ).list();
    }

}
