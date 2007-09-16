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
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.PlatformContext;

public class PlatformContextImpl
    implements PlatformContext
{

    private List<LocalBundle> m_bundles;
    private File m_workingDirectory;
    private Properties m_properties;
    private String m_systemPackages;
    private Boolean m_shouldClean;
    private Configuration m_configuration;

    /**
     * {@inheritDoc}
     */
    public List<LocalBundle> getBundles()
    {
        return m_bundles;
    }

    /**
     * {@inheritDoc}
     */
    public void setBundles( final List<LocalBundle> bundles )
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
    public Boolean shouldClean()
    {
        return m_shouldClean;
    }

    /**
     * {@inheritDoc}
     */
    public void setShouldClean( final Boolean shouldClean )
    {
        m_shouldClean = shouldClean;
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

}
