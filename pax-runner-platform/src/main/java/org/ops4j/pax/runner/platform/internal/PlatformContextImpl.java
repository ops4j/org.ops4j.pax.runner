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
import java.util.List;
import java.util.Properties;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.FilePathStrategy;
import org.ops4j.pax.runner.platform.PlatformContext;

public class PlatformContextImpl
    implements PlatformContext
{

    private List<BundleReference> m_bundles;
    private File m_workingDirectory;
    private Properties m_properties;
    private String m_systemPackages;
    private Configuration m_configuration;
    private String m_executionEnvironment;
    private FilePathStrategy m_filePathStrategy;

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
    public FilePathStrategy getFilePathStrategy()
    {
        return m_filePathStrategy;
    }

    /**
     * {@inheritDoc}
     */
    public void setFilePathStrategy( final FilePathStrategy filePathStrategy )
    {
        m_filePathStrategy = filePathStrategy;
    }

}
