package org.ops4j.pax.runner.scanner.dir.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

public abstract class ListerTest
{

    abstract Lister createLister( Pattern filter );

    abstract URL asURL( String fileName )
        throws MalformedURLException;

    void verifyContent( List<URL> actual, URL... expected )
    {
        Assert.assertNotNull( "Returned list is null", actual );
        Assert.assertEquals( "Number of urls", expected.length, actual.size() );
        for ( URL url : expected )
        {
            Assert.assertTrue( "Missing expected: " + url.toExternalForm(), actual.contains( url ) );
        }
    }

    @Test
    public void filter01()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "bundle1.jar" ) ).list(),
            asURL( "bundle1.jar" )
        );
    }

    @Test
    public void filter02()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "bundle1.*" ) ).list(),
            asURL( "bundle1.jar" )
        );
    }

    @Test
    public void filter03()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "bundle*.jar" ) ).list(),
            asURL( "bundle1.jar" ),
            asURL( "bundle2.jar" )
        );
    }

    @Test
    public void filter04()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "*.jar" ) ).list(),
            asURL( "bundle1.jar" ),
            asURL( "bundle2.jar" )
        );
    }

    @Test
    public void filter05()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( ".jar" ) ).list()
        );
    }

    @Test
    public void filter06()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "**" ) ).list(),
            asURL( "bundle1.jar" ),
            asURL( "bundle2.jar" ),
            asURL( "subdir/bundle3.jar" ),
            asURL( "subdir/subdir/bundle4.jar" )
        );
    }

    @Test
    public void filter07()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "**/*.jar" ) ).list(),
            asURL( "subdir/bundle3.jar" ),
            asURL( "subdir/subdir/bundle4.jar" )
        );
    }

    @Test
    public void filter08()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "*" ) ).list(),
            asURL( "bundle1.jar" ),
            asURL( "bundle2.jar" )
        );
    }

    @Test
    public void filter09()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "subdir/**" ) ).list(),
            asURL( "subdir/bundle3.jar" ),
            asURL( "subdir/subdir/bundle4.jar" )
        );
    }

    @Test
    public void filter10()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "subdir/*" ) ).list(),
            asURL( "subdir/bundle3.jar" )
        );
    }

    @Test
    public void filter11()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "subdir" ) ).list()
        );
    }

    @Test
    public void filter12()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "**/subdir" ) ).list()
        );
    }

    @Test
    public void filter13()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "**/subdir/*" ) ).list(),
            asURL( "subdir/subdir/bundle4.jar" )
        );
    }

    @Test
    public void filter14()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "**subdir/*" ) ).list(),
            asURL( "subdir/bundle3.jar" ),
            asURL( "subdir/subdir/bundle4.jar" )
        );
    }

    @Test
    public void filter15()
        throws MalformedURLException
    {
        verifyContent(
            createLister( ParserImpl.parseFilter( "**/*" ) ).list(),
            asURL( "subdir/bundle3.jar" ),
            asURL( "subdir/subdir/bundle4.jar" )
        );
    }

}
