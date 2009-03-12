/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.runner.platform.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.PlatformContext;

public class PlatformContextImpl
    implements PlatformContext
{

    private static Log LOG = LogFactory.getLog( PlatformContextImpl.class );

    private List<BundleReference> m_bundles;
    private File m_workingDirectory;
    private Properties m_properties;
    private String m_systemPackages;
    private Configuration m_configuration;
    private String m_executionEnvironment;

    /**
     * {@inheritDoc}
     */
    public List<BundleReference> getBundles()
    {
        return m_bundles;
    }

    /**
     * {@inheritDoc}
     */
    public void setBundles( final List<BundleReference> bundles )
    {
        m_bundles = bundles;
    }

    /**
     * {@inheritDoc}
     */
    public File getWorkingDirectory()
    {
        return m_workingDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public void setWorkingDirectory( final File workingDirectory )
    {
        m_workingDirectory = workingDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties()
    {
        return m_properties;
    }

    /**
     * {@inheritDoc}
     */
    public void setProperties( final Properties properties )
    {
        m_properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    public String getSystemPackages()
    {
        return m_systemPackages;
    }

    /**
     * {@inheritDoc}
     */
    public void setSystemPackages( final String systemPackages )
    {
        m_systemPackages = systemPackages;
    }

    /**
     * {@inheritDoc}
     */
    public Configuration getConfiguration()
    {
        return m_configuration;
    }

    /**
     * {@inheritDoc}
     */
    public void setConfiguration( final Configuration configuration )
    {
        m_configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    public String getExecutionEnvironment()
    {
        return m_executionEnvironment;
    }

    /**
     * {@inheritDoc}
     */
    public void setExecutionEnvironment( String executionEnvironment )
    {
        m_executionEnvironment = executionEnvironment;
    }

    /**
     * {@inheritDoc}
     */
    public String normalizeAsPath( final File file )
    {
        return normalizePath( getWorkingDirectory(), file );
    }

    /**
     * {@inheritDoc}
     */
    public String normalizeAsUrl( final File file )
    {
        return "file:" + normalizePath( getWorkingDirectory(), file );
    }

    /**
     * {@inheritDoc}
     */
    public String normalizeAsUrl( final URL url )
    {
        if( "file".equals( url.getProtocol() ) )
        {
            return "file:" + normalizePath( getWorkingDirectory(), new File( url.getFile() ) );
        }
        return url.toExternalForm();
    }

    /**
     * Here we finally decide on actual paths showing up in generated config files and commandline args.
     *
     * @param baseFolder folder to be used. This is what we will cut off.
     * @param file       to be normalized.
     *
     * @return if file is a child of base then we will return the relative path. If not, the full path of file will be returned.
     */
    private String normalizePath( final File baseFolder, final File file )
    {
        String out = file.getAbsolutePath();
        try
        {
            if( baseFolder.equals( file ) )
            {
                out = ".";
            }
            else
            {
                String s1 = baseFolder.getCanonicalPath();
                String s2 = file.getCanonicalPath();
                if( s2.startsWith( s1 ) )
                {
                    out = s2.substring( s1.length() + 1 );
                }
                else
                {
                    out = s2;
                }
            }
        }
        catch( IOException e )
        {
            LOG.warn( "problem during normalizing path.", e );
        }
        return out;
    }

}
