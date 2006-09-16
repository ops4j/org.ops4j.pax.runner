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

import com.intellij.openapi.module.ModuleType;
import javax.swing.Icon;

public class OsgiModuleType extends ModuleType<OsgiModuleBuilder>
{

    private static final String OSGI_MODULE_TYPE = "org.ops4j.pax.runner.idea.module.OsgiModuleType";
    private static OsgiModuleType m_instance = new OsgiModuleType();

    private OsgiModuleType()
    {
        super( OSGI_MODULE_TYPE );
    }

    public static OsgiModuleType getInstance()
    {
        return m_instance;
    }

    public OsgiModuleBuilder createModuleBuilder()
    {
        OsgiModuleBuilder builder = new OsgiModuleBuilder( this );
        return builder;
    }

    public String getName()
    {
        return "OSGi Bundle";
    }

    public String getDescription()
    {
        return "OSGi Bundle type. See http://www.osgi.org for information about OSGi.";
    }

    public Icon getBigIcon()
    {
        return null;
    }

    public Icon getNodeIcon( boolean isOpened )
    {
        return null;
    }
}
