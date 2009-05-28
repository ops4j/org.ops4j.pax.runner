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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.ops4j.pax.cursor.shared.Attribute;

/**
 * Options Composite of Pax Cursor (upper group).
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class OptionsBlock
    extends CursorTabBlock
{

    private final Combo m_logCombo;
    private final Button m_overwriteSystemButton;
    private final Button m_overwriteUserButton;
    private final Button m_overwriteAllButton;
    private final Button m_configProfileButton;
    private final Button m_logProfileButton;
    private final Button m_obrProfileButton;
    private final Button m_webProfileButton;
    private final Button m_warProfileButton;
    private final Button m_springProfileButton;    
    private final Button m_dsProfileButton;    

    /**
     * @see Composite#Composite(Composite, int)
     */
    public OptionsBlock( final Composite parent, final int style )
    {
        super( parent, style );
        final GridLayout blockLayout = new GridLayout();
        blockLayout.numColumns = 2;
        setLayout( blockLayout );

        final Group optionsGroup = new Group( this, SWT.NONE );
        optionsGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
        optionsGroup.setText( "Options:" );
        final GridLayout optionsGroupLayout = new GridLayout();
        optionsGroupLayout.numColumns = 4;
        optionsGroup.setLayout( optionsGroupLayout );

        final SelectionListener updateNotifier = new SelectionAdapter()
        {
            public void widgetSelected( final SelectionEvent e )
            {
                notifyUpdate();
            }
        };

        final Label overwriteLabel = new Label( optionsGroup, SWT.NONE );
        overwriteLabel.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false ) );
        overwriteLabel.setText( "Overwrite:" );

        m_overwriteAllButton = new Button( optionsGroup, SWT.CHECK );
        m_overwriteAllButton.addSelectionListener( updateNotifier );
        m_overwriteAllButton.setText( "All" );

        m_overwriteUserButton = new Button( optionsGroup, SWT.CHECK );
        m_overwriteUserButton.addSelectionListener( updateNotifier );
        m_overwriteUserButton.setText( "User" );

        m_overwriteSystemButton = new Button( optionsGroup, SWT.CHECK );
        m_overwriteSystemButton.addSelectionListener( updateNotifier );
        m_overwriteSystemButton.setText( "System" );

        final Label logLabel = new Label( optionsGroup, SWT.NONE );
        logLabel.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false ) );
        logLabel.setText( "Log:" );

        m_logCombo = new Combo( optionsGroup, SWT.READ_ONLY );
        m_logCombo.addSelectionListener( updateNotifier );
        m_logCombo.setItems( new String[]{ "TRACE", "DEBUG", "INFO", "WARN", "ERROR" } );
        final GridData logComboLayout = new GridData( SWT.LEFT, SWT.TOP, false, false );
        m_logCombo.setLayoutData( logComboLayout );

        final Group profilesGroup = new Group( this, SWT.NONE );
        profilesGroup.setLayoutData( new GridData( SWT.LEFT, SWT.FILL, false, false ) );
        profilesGroup.setText( "Profiles:" );
        final GridLayout profileGroupLayout = new GridLayout();
        profileGroupLayout.numColumns = 4;
        profilesGroup.setLayout( profileGroupLayout );

        m_logProfileButton = new Button( profilesGroup, SWT.CHECK );
        m_logProfileButton.addSelectionListener( updateNotifier );
        m_logProfileButton.setText( "log" );

        m_webProfileButton = new Button( profilesGroup, SWT.CHECK );
        m_webProfileButton.addSelectionListener( updateNotifier );
        m_webProfileButton.setText( "web" );

        m_warProfileButton = new Button( profilesGroup, SWT.CHECK );
        m_warProfileButton.addSelectionListener( updateNotifier );
        m_warProfileButton.setText( "war" );

        m_springProfileButton = new Button(profilesGroup, SWT.CHECK);
        m_springProfileButton.addSelectionListener( updateNotifier );
        m_springProfileButton.setText("spring");

        m_configProfileButton = new Button( profilesGroup, SWT.CHECK );
        m_configProfileButton.addSelectionListener( updateNotifier );
        m_configProfileButton.setText( "config" );

        m_obrProfileButton = new Button( profilesGroup, SWT.CHECK );
        m_obrProfileButton.addSelectionListener( updateNotifier );
        m_obrProfileButton.setText( "obr" );
        
        m_dsProfileButton = new Button( profilesGroup, SWT.CHECK );
        m_dsProfileButton.addSelectionListener( updateNotifier );
        m_dsProfileButton.setText( "ds" );
        
        new Label(profilesGroup, SWT.NONE);
    }

    /**
     * Initialize block.
     *
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom( final ILaunchConfiguration configuration )
    {
        // default values
        m_overwriteAllButton.setSelection( false );
        m_overwriteUserButton.setSelection( false );
        m_overwriteSystemButton.setSelection( false );
        m_logCombo.setText( "INFO" );
        try
        {
            // get overwrite options
            m_overwriteAllButton.setSelection( configuration.getAttribute( Attribute.OVERWRITE_ALL, false ) );
            m_overwriteUserButton.setSelection( configuration.getAttribute( Attribute.OVERWRITE_USER, false ) );
            m_overwriteSystemButton.setSelection( configuration.getAttribute( Attribute.OVERWRITE_SYSTEM, false ) );
            // get log option
            m_logCombo.setText( configuration.getAttribute( Attribute.LOG_LEVEL, "INFO" ) );
            // get profiles option
            final List profiles = configuration.getAttribute( Attribute.PROFILES, new ArrayList() );
            if( profiles != null && profiles.size() > 0 )
            {
                m_configProfileButton.setSelection( profiles.contains( "config" ) );
                m_logProfileButton.setSelection( profiles.contains( "log" ) );
                m_obrProfileButton.setSelection( profiles.contains( "obr" ) );
                m_webProfileButton.setSelection( profiles.contains( "web" ) );
                m_warProfileButton.setSelection( profiles.contains( "war" ) );
                m_springProfileButton.setSelection( profiles.contains( "spring-dm" ) );                
                m_dsProfileButton.setSelection( profiles.contains( "ds" ) );                
            }
        }
        catch( CoreException ignore )
        {
            // DebugUIPlugin.log(ignore.getStatus());
        }
    }

    /**
     * Saves block configurations attributes.
     *
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply( final ILaunchConfigurationWorkingCopy configuration )
    {
        // save overwrite options
        configuration.setAttribute( Attribute.OVERWRITE_ALL, m_overwriteAllButton.getSelection() );
        configuration.setAttribute( Attribute.OVERWRITE_USER, m_overwriteUserButton.getSelection() );
        configuration.setAttribute( Attribute.OVERWRITE_SYSTEM, m_overwriteSystemButton.getSelection() );
        // save log option
        configuration.setAttribute( Attribute.LOG_LEVEL, m_logCombo.getText() );
        // save profiles option
        final StringBuffer profilesArg = new StringBuffer( "--profiles=" );
        List profiles = new ArrayList();
        if( m_configProfileButton.getSelection() )
        {
            profiles.add( "config" );
            profilesArg.append( "config," );
        }
        if( m_logProfileButton.getSelection() )
        {
            profiles.add( "log" );
            profilesArg.append( "log," );
        }
        if( m_obrProfileButton.getSelection() )
        {
            profiles.add( "obr" );
            profilesArg.append( "obr," );
        }
        if( m_webProfileButton.getSelection() )
        {
            profiles.add( "web" );
            profilesArg.append( "web," );
        }
        if( m_warProfileButton.getSelection() )
        {
            profiles.add( "war" );
            profilesArg.append( "war," );
        }
        if( m_springProfileButton.getSelection() )
        {
            profiles.add( "spring-dm" );
            profilesArg.append( "spring-dm," );
        }
        if( m_dsProfileButton.getSelection() )
        {
            profiles.add( "ds" );
            profilesArg.append( "ds," );
        }
        // maybe there is no profile selected so then remove the configuration attribute
        if( profiles.size() == 0 )
        {
            profiles = null;
        }
        configuration.setAttribute( Attribute.PROFILES, profiles );
        // finally, save arguments list
        try
        {
            final List arguments = configuration.getAttribute( Attribute.RUN_ARGUMENTS, new ArrayList() );
            arguments.add( "--overwrite=" + m_overwriteAllButton.getSelection() );
            arguments.add( "--overwriteUserBundles=" + m_overwriteUserButton.getSelection() );
            arguments.add( "--overwriteSystemBundles=" + m_overwriteSystemButton.getSelection() );
            if( m_logCombo.getText() != null && m_logCombo.getText().trim().length() > 0 )
            {
                arguments.add( "--log=" + m_logCombo.getText() );
            }
            if( profiles != null )
            {
                arguments.add( profilesArg.substring( 0, profilesArg.length() - 1 ) );
            }
            configuration.setAttribute( Attribute.RUN_ARGUMENTS, arguments );
        }
        catch( CoreException ignore )
        {
            // DebugUIPlugin.log(ignore.getStatus());;
        }
    }

}
