package org.ops4j.pax.runner.platform.internal;

import java.io.File;
import static org.junit.Assert.*;
import org.junit.Test;
import org.ops4j.pax.runner.platform.PlatformContext;

/**
 * Path normalization in Platform Context unit tests.
 *
 * @author Toni Menzel (tonit)
 * @since 0.18.0, Mar 10, 2009
 */
public class PathNormalizeTest
{

    public String normalize( String workDir, String f )
    {
        PlatformContext context = new PlatformContextImpl();
        context.setWorkingDirectory( new File( workDir ) );
        return context.normalizeAsPath( new File( f ) );
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
        assertEquals( "/other/runner/config/myconf.ini", normalize( "/work/runner", "/other/runner/config/myconf.ini" ) );
    }
}
