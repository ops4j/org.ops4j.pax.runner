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
package org.ops4j.pax.runner.idea.config;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Logger;

public class OsgiRunConfigurationFactory extends ConfigurationFactory
{
    private static final Logger m_logger = Logger.getLogger( OsgiRunConfigurationFactory.class );

    public OsgiRunConfigurationFactory( final ConfigurationType type )
    {
        super( type );
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "OsgiRunConfigurationFactory(" + type + ")" );
        }
    }

    public RunConfiguration createTemplateConfiguration( Project project )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "createTemplateConfiguration(" + project + ")" );
        }

        OsgiRunConfiguration template = new OsgiRunConfiguration( project, this, "unnamed" );
        return template;
    }
}
