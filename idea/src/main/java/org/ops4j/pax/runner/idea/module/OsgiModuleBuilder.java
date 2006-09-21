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
package org.ops4j.pax.runner.idea.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.apache.log4j.Logger;

public class OsgiModuleBuilder extends JavaModuleBuilder
{
    private static final Logger m_logger = Logger.getLogger( OsgiModuleBuilder.class );

    private ModuleType m_type;
    private ManifestBean m_manifest;

    public OsgiModuleBuilder( ModuleType type )
    {
        m_type = type;
        m_manifest = new ManifestBean();
    }

    public void setupRootModel( ModifiableRootModel modifiableRootModel )
    throws ConfigurationException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "OsgiModuleBuilder.modifiableRootModel()" );
        }
    }

    public ModuleType getModuleType()
    {
        return m_type;
    }

    public ManifestBean getManifest()
    {
        return m_manifest;
    }
}
