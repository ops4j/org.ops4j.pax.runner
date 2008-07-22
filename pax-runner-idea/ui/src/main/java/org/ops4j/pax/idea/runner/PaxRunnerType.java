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
package org.ops4j.pax.idea.runner;

import javax.swing.Icon;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.idea.runner.config.OsgiRunConfigurationFactory;

public class PaxRunnerType
    implements ApplicationComponent, ConfigurationType
{

    private static final Log m_logger = LogFactory.getLog( PaxRunnerType.class );

    private ConfigurationFactory m_factory;

    public PaxRunnerType()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "PaxRunnerType()" );
        }
    }

    public void initComponent()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "initComponent()" );
        }
        m_factory = new OsgiRunConfigurationFactory( this );
    }

    public void disposeComponent()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "disposeComponent()" );
        }
        // TODO: insert component disposal logic here
    }

    public String getComponentName()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getComponentName()" );
        }
        return "#org.ops4j.osgidea.runner.config.OsgiRunConfigurationFactory";
    }

    public String getDisplayName()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getDisplayName()" );
        }
        return "OSGi";
    }

    public String getConfigurationTypeDescription()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getConfigurationTypeDescription()" );
        }
        return "Pax Runner starts any OSGI containers.";
    }

    public Icon getIcon()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getIcon()" );
        }
        return null;
    }

    public ConfigurationFactory[] getConfigurationFactories()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getConfigurationFactories()" );
        }
        return new ConfigurationFactory[]{ m_factory };
    }
}
