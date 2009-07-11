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
package org.ops4j.pax.cursor.ui;

import java.util.List;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.pde.internal.ui.IHelpContextIds;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.ui.launcher.AbstractLauncherTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.ops4j.pax.cursor.shared.Attribute;

/**
 * Pax Runner Eclipse Plugin Tab is an extension on org.eclipse.debug.ui.launchConfigurationTabs that adds extra options
 * for Pax Runner Eclipse Plugin.
 * The extra options are lose coupled with Pax Runner Eclipse Plugin, as if this Cursor does not exist or is not
 * installed it will have no influence on Pax Runner Eclipse Plugin. The only connection between the two is through a
 * configuration attribute that is created by this tab on succesfull save (apply), atribute that contains a list of
 * options that Pax Runner Eclipse Plugin will read and if present will send them unchanged to Pax Runner.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class CursorTab
    extends AbstractLauncherTab
{

    private ProvisionBlock m_provisionBlock;
    private OptionsBlock m_optionsBlock;
    private Image m_image;
    private boolean m_initializing;

    public CursorTab()
    {
        m_initializing = false;
        m_image = PDEPluginImages.DESC_PLUGINS_FRAGMENTS.createImage();
    }

    public void createControl( final Composite parent )
    {
        Composite container = new Composite( parent, SWT.NONE );
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        container.setLayout( gridLayout );
        container.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        m_optionsBlock = new OptionsBlock( container, SWT.NONE );
        m_optionsBlock.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        m_provisionBlock = new ProvisionBlock( container, SWT.NONE );
        m_provisionBlock.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        m_optionsBlock.setCursorTab( this );
        m_provisionBlock.setCursorTab( this );
        Dialog.applyDialogFont( container );
        setControl( container );

        PlatformUI.getWorkbench().getHelpSystem().setHelp( getControl(), IHelpContextIds.LAUNCHER_CONFIGURATION );
    }

    /**
     * @see ILaunchConfigurationTab#getName()
     */
    public String getName()
    {
        return "Pax Runner";
    }

    /**
     * Initialize by delegating to component blocks.
     *
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom( final ILaunchConfiguration configuration )
    {
        m_initializing = true;
        try
        {
            m_optionsBlock.initializeFrom( configuration );
            m_provisionBlock.initializeFrom( configuration );
        }
        finally
        {
            m_initializing = false;
        }
    }

    /**
     * Saves the configuration attributes by delegating to component blocks. First it will save a "null" Pax Runner
     * Eclipse Plugin options so every block will have the chance to add it's own options.
     *
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply( final ILaunchConfigurationWorkingCopy configuration )
    {
        // reset Pax Runner arguments
        configuration.setAttribute( Attribute.RUN_ARGUMENTS, (List) null );
        // delegate to each block
        m_optionsBlock.performApply( configuration );
        m_provisionBlock.performApply( configuration );
    }

    /**
     * Does nothing, as there are no defaults to be set.
     *
     * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults( final ILaunchConfigurationWorkingCopy configuration )
    {
        // no initial values
    }

    public Image getImage()
    {
        return m_image;
    }

    public void dispose()
    {
        if( m_image != null )
        {
            m_image.dispose();
        }
    }

    public void updateLaunchConfigurationDialog()
    {
        if( !m_initializing )
        {
            super.updateLaunchConfigurationDialog();
        }
    }

    /**
     * Does nothing as there is no validation required. If URL's are wrong will be checked by pax Runner.
     *
     * @see AbstractLauncherTab#validateTab()
     */
    public void validateTab()
    {
        // no validation required
    }

}
