/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.builder.bundles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.ops4j.pax.builder.ActivatorModel;

public class BundleModel
{

    private ManifestModel m_manifest;
    private Properties m_properties;
    private File m_destinationFile;
    private List<File> m_sources;
    private ActivatorModel m_activator;

    public BundleModel()
    {
        m_properties = new Properties();
        m_sources = new ArrayList<File>();
        m_manifest = new ManifestModel();
    }

    public Properties getProperties()
    {
        return m_properties;
    }

    public File getDestinationFile()
    {
        return m_destinationFile;
    }

    public File[] getSources()
    {
        return m_sources.toArray( new File[0] );
    }

    public ManifestModel getManifest()
    {
        return m_manifest;
    }

    public void setManifest( ManifestModel manifest )
    {
        m_manifest = manifest;
    }

    public void addProperty( String name, String value )
    {
        if( value == null )
        {
            m_properties.remove( name );
        }
        else
        {
            m_properties.put( name, value );
        }
    }

    public void removeProperty( String name )
    {
        m_properties.remove( name );
    }

    public void setDestinationFile( File destinationFile )
    {
        m_destinationFile = destinationFile;
    }

    public void addSource( File source )
    {
        m_sources.add( source );
    }

    public void removeSource( File source )
    {
        m_sources.remove( source );
    }

    public ActivatorModel getActivator()
    {
        return m_activator;
    }

    public void setActivator( ActivatorModel activator )
    {
        m_activator = activator;
    }
}
