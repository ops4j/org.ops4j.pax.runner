package org.ops4j.pax.runner;

import static org.junit.Assert.*;
import org.junit.Test;

public class ConfigurationImplTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithNullConfigFile()
    {
        new ConfigurationImpl( null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithEmptyConfigFile()
    {
        new ConfigurationImpl( "" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithInexistentConfigURL()
    {
        new ConfigurationImpl( "aFile.properties" );
    }

    @Test( expected = IllegalArgumentException.class )
    public void constructorWithInexistentConfigClasspathURL()
    {
        new ConfigurationImpl( "classpath:aFile.properties" );
    }

    @Test
    public void constructorWithValidClasspathConfiguration()
    {
        Configuration config =
            new ConfigurationImpl( "classpath:org/ops4j/pax/runner/configuration/runner.properties" );
        assertEquals( "platform.test", "org.ops4j.pax.runner.platform.Activator", config.getProperty( "platform.test" ) );
    }

}
