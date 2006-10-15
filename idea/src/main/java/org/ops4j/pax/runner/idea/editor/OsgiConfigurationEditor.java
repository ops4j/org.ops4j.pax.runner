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
package org.ops4j.pax.runner.idea.editor;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.idea.config.OsgiRunConfiguration;
import org.ops4j.pax.runner.idea.forms.OsgiConfigEditorForm;
import org.jetbrains.annotations.NotNull;

public class OsgiConfigurationEditor extends SettingsEditor<OsgiRunConfiguration>
{
    private static final Logger m_logger = Logger.getLogger( OsgiConfigurationEditor.class );
    private OsgiConfigEditorForm m_form;

    public OsgiConfigurationEditor( OsgiRunConfiguration config )
    {
        m_form = new OsgiConfigEditorForm( config.getConfigBean() );
    }

    protected void resetEditorFrom( OsgiRunConfiguration runConfig )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "resetEditorForm(" + runConfig + ")" );
        }
        m_form.setData( runConfig.getConfigBean() );
    }

    protected void applyEditorTo( OsgiRunConfiguration runConfig )
        throws ConfigurationException
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "applyEditorTo( " + runConfig + ")" );
        }
        m_form.getData( runConfig.getConfigBean() );

    }

    @NotNull
    protected JComponent createEditor()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "createEditor()" );
        }
        return m_form.getMainPanel();
    }

    protected void disposeEditor()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "disposeEditor()" );
        }
    }
}
