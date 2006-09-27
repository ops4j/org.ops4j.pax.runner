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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import javax.swing.JComponent;
import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;

public class OsgiModuleTypeStep extends ModuleWizardStep
{

    private WizardContext m_wizardContext;
    private OsgiModuleBuilder m_moduleBuilder;
    private Icon m_icon;
    private String m_helpId;
    private OsgiModuleTypeForm m_form;

    public OsgiModuleTypeStep( WizardContext wizardContext, OsgiModuleBuilder moduleBuilder, Icon icon, String helpId )
    {
        super();
        m_wizardContext = wizardContext;
        m_moduleBuilder = moduleBuilder;
        m_icon = icon;
        m_helpId = helpId;
    }

    public JComponent getComponent()
    {
        m_form = new OsgiModuleTypeForm();
        m_form.setData( m_moduleBuilder.getManifest() );
        JComponent panel = new OsgiModuleTypeForm().$$$getRootComponent$$$();
        return panel;
    }

    public void updateDataModel()
    {
        DataBeanManifest manifest = m_moduleBuilder.getManifest();
        m_form.getData( manifest );
    }

    public Icon getIcon()
    {
        return m_icon;
    }

    @NonNls
    public String getHelpId()
    {
        return m_helpId;
    }
}
