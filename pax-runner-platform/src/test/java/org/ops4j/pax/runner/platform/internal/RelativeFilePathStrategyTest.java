package org.ops4j.pax.runner.platform.internal;

import java.io.File;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Path normalization in Platform Context unit tests.
 *
 * @author Toni Menzel (tonit)
 * @since 0.18.0, Mar 10, 2009
 */
public class RelativeFilePathStrategyTest
{

    public String normalize( String workDir, String f )
    {
        return new RelativeFilePathStrategy( new File( workDir ) ).normalizeAsPath( new File( f ) );
    }

    @Test
    public void testSame()
    {
        assertEquals( ".", normalize( "runner", "runner" ) );
        assertEquals( ".", normalize( "/foo/runner", "/foo/runner" ) );
        assertEquals( ".", normalize( "/foo/runner/", "/foo/runner" ) );
        assertEquals( ".", normalize( "c:/foo/runner", "c:/foo/runner" ) );
    }

    @Test
    public void testNormal()
    {
        assertEquals( "config/myconf.ini", normalize( "/work/runner", "/work/runner/config/myconf.ini" ) );
    }

    @Test
    public void testNotParent()
    {
        String root = normalize( ".", "/" );

        assertEquals( root + "other/runner/config/myconf.ini",
                      normalize( "/work/runner", "/other/runner/config/myconf.ini" )
        );
    }
}
