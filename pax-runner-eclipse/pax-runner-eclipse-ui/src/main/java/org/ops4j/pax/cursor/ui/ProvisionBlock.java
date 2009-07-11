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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.ops4j.pax.cursor.shared.Attribute;

/**
 * Provisioning Composite of Pax Runner Eclipse Plugin (bottom group).
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class ProvisionBlock
    extends CursorTabBlock
{

    private final CheckboxTableViewer m_tableViewer;
    private final Button m_editButton;
    private final Button m_deleteButton;

    private File m_lastUsedDir;

    /**
     * @see Composite#Composite(Composite, int)
     */
    public ProvisionBlock( Composite parent, int style )
    {
        super( parent, style );
        final GridLayout gridLayout = new GridLayout();
        setLayout( gridLayout );

        final Group provisioningGroup = new Group( this, SWT.NONE );
        provisioningGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        provisioningGroup.setText( "Provisioning:" );
        final GridLayout provisioningGridLayout = new GridLayout();
        provisioningGridLayout.marginWidth = 0;
        provisioningGridLayout.marginHeight = 0;
        provisioningGridLayout.numColumns = 2;
        provisioningGroup.setLayout( provisioningGridLayout );

        final Composite tableComposite = new Composite( provisioningGroup, SWT.NONE );
        final GridData tableGridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        tableComposite.setLayoutData( tableGridData );
        final GridLayout tableGridLayout = new GridLayout();
        tableGridLayout.marginWidth = 0;
        tableGridLayout.marginHeight = 0;
        tableComposite.setLayout( tableGridLayout );

        m_tableViewer = CheckboxTableViewer.newCheckList( tableComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER );
        m_tableViewer.setContentProvider( new ProvisionContentProvider() );
        m_tableViewer.setLabelProvider( new ProvisionLabelProvider() );
        m_tableViewer.addCheckStateListener(
            new ICheckStateListener()
            {
                public void checkStateChanged( final CheckStateChangedEvent event )
                {
                    notifyUpdate();
                }
            }
        );
        m_tableViewer.addSelectionChangedListener(
            new ISelectionChangedListener()
            {
                public void selectionChanged( SelectionChangedEvent event )
                {
                    onTableSelectionChanged( event );
                }
            }
        );
        m_tableViewer.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                if( !m_tableViewer.getSelection().isEmpty() )
                {
                    onEditButtonSelected();
                }
            }
        }
        );

        final Table table = m_tableViewer.getTable();
        table.setLinesVisible( true );
        table.setHeaderVisible( true );
        table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        final TableColumn urlTableColumn = new TableColumn( table, SWT.NONE );

        urlTableColumn.setWidth( 425 );
        urlTableColumn.setText( "Provision from" );

        final TableColumn startTableColumn = new TableColumn( table, SWT.CENTER );
        startTableColumn.setWidth( 50 );
        startTableColumn.setText( "Start" );

        final TableColumn levelTableColumn = new TableColumn( table, SWT.CENTER );
        levelTableColumn.setWidth( 70 );
        levelTableColumn.setText( "Level" );

        final TableColumn updateTableColumn = new TableColumn( table, SWT.CENTER );
        updateTableColumn.setWidth( 50 );
        updateTableColumn.setText( "Update" );

        final ProvisionCellModifier cellModifier = new ProvisionCellModifier();
        cellModifier.setModifyListener(
            new ProvisionCellModifier.ModifyListener()
            {
                public void modified( Object element )
                {
                    m_tableViewer.update( element, null );
                    notifyUpdate();
                }
            }
        );
        m_tableViewer.setCellModifier( cellModifier );
        m_tableViewer.setCellEditors(
            new CellEditor[]
                {
                    null,
                    new CheckboxCellEditor( table ),
                    new TextCellEditor( table ),
                    new CheckboxCellEditor( table )
                }
        );
        m_tableViewer.setColumnProperties( new String[]{ "url", "start", "level", "update" } );

        final Composite buttonsComposite = new Composite( provisioningGroup, SWT.NONE );
        final GridData butonsGridData = new GridData( SWT.LEFT, SWT.TOP, false, false );
        buttonsComposite.setLayoutData( butonsGridData );
        buttonsComposite.setLayout( new GridLayout() );

        final Button addButton = new Button( buttonsComposite, SWT.NONE );
        addButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        addButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onAddButtonSelected();
                }
            }
        );
        addButton.setText( "Add URL" );

        final Button addBundleButton = new Button( buttonsComposite, SWT.NONE );
        addBundleButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        addBundleButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onAddSingleFileButtonSelected( "scan-bundle", new String[]{ "*.jar", "*.*" },
                                                   new String[]{ "Any Jar", "Any File" }
                    );
                }
            }
        );
        addBundleButton.setText( "Add Bundle..." );

        final Button addFileButton = new Button( buttonsComposite, SWT.NONE );
        addFileButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        addFileButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onAddSingleFileButtonSelected( "scan-file", new String[]{ "*.bundles", "*.txt", "*.*" },
                                                   new String[]{ "Provision File", "TXT Provision File", "Any File" }
                    );
                }
            }
        );
        addFileButton.setText( "Add File..." );

        final Button addPomButton = new Button( buttonsComposite, SWT.NONE );
        addPomButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        addPomButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onAddSingleFileButtonSelected( "scan-pom", new String[]{ "pom.xml", "*.pom", "*.*" },
                                                   new String[]{ "Maven POM", "Maven Repository POM", "Any File" }
                    );
                }
            }
        );
        addPomButton.setText( "Add POM..." );

        final Button addDirButton = new Button( buttonsComposite, SWT.NONE );
        addDirButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        addDirButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onAddDirButtonSelected();
                }
            }
        );
        addDirButton.setText( "Add Dir..." );

        final Button addMavenButton = new Button( buttonsComposite, SWT.NONE );
        addMavenButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        addMavenButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onAddMavenButtonSelected();
                }
            }
        );
        addMavenButton.setText( "Add Maven..." );

        m_editButton = new Button( buttonsComposite, SWT.NONE );
        m_editButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        m_editButton.setEnabled( false );
        m_editButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onEditButtonSelected();
                }
            }
        );
        m_editButton.setText( "Edit..." );

        m_deleteButton = new Button( buttonsComposite, SWT.NONE );
        m_deleteButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        m_deleteButton.setEnabled( false );
        m_deleteButton.addSelectionListener(
            new SelectionAdapter()
            {
                public void widgetSelected( final SelectionEvent e )
                {
                    onRemoveButtonSelected();
                }
            }
        );
        m_deleteButton.setText( "Delete" );
    }

    /**
     * Handles add button push.
     */
    protected void onAddButtonSelected()
    {
        InputDialog dialog = new InputDialog( getShell(), "Add bundle from URL", "Bundle URL (e.g. OBR):", null, null );
        if( dialog.open() != Window.OK )
        {
            return;
        }
        addURL( dialog.getValue() );
    }

    /**
     * Handles add bundle/file/pom button push.
     */
    private void onAddSingleFileButtonSelected( final String scanner, final String[] filterExtensions,
                                                final String[] filterNames )
    {
        final FileDialog fileDialog = new FileDialog( getShell(), SWT.OPEN );
        fileDialog.setText( "File Selection" );
        fileDialog.setFilterExtensions( filterExtensions );
        fileDialog.setFilterNames( filterNames );
        if( m_lastUsedDir != null )
        {
            fileDialog.setFilterPath( m_lastUsedDir.getAbsolutePath() );
        }
        final String selected = fileDialog.open();

        if( selected != null )
        {
            final File selectedFile = new File( selected );
            if( selectedFile.exists() && selectedFile.isFile() )
            {
                m_lastUsedDir = selectedFile.getParentFile();
                try
                {
                    addURL( scanner + ":" + selectedFile.getCanonicalFile().toURL().toExternalForm() );
                }
                catch( MalformedURLException ignore )
                {
                    // DebugUIPlugin.log(ignore.getMessage());
                }
                catch( IOException ignore )
                {
                    // DebugUIPlugin.log(ignore.getMessage());
                }
            }
        }
    }

    /**
     * Handles add dir button push.
     */
    protected void onAddDirButtonSelected()
    {
        final DirectoryDialog dialog = new DirectoryDialog( getShell() );
        dialog.setText( PDEUIMessages.BaseBlock_dirSelection );
        dialog.setMessage( PDEUIMessages.BaseBlock_dirChoose );
        if( m_lastUsedDir != null )
        {
            dialog.setFilterPath( m_lastUsedDir.getAbsolutePath() );
        }
        final String selected = dialog.open();

        if( selected != null )
        {
            final File selectedFile = new File( selected );
            if( selectedFile.exists() && selectedFile.isDirectory() )
            {
                m_lastUsedDir = selectedFile;
                try
                {
                    addURL( "scan-dir:" + selectedFile.getCanonicalFile().toURL().toExternalForm() );
                }
                catch( MalformedURLException ignore )
                {
                    // DebugUIPlugin.log(ignore.getMessage());
                }
                catch( IOException ignore )
                {
                    // DebugUIPlugin.log(ignore.getMessage());
                }
            }
        }
    }

    private void onAddMavenButtonSelected()
    {
        MessageDialog.openInformation( getShell(), "Information",
                                       "Not yet implemented, but will allow selection of a Maven artifact from local or remote repository"
        );
    }

    /**
     * Handles edit button push.
     */
    protected void onEditButtonSelected()
    {
        final IStructuredSelection sel = (IStructuredSelection) m_tableViewer.getSelection();
        if( sel == null || sel.isEmpty() )
        {
            return;
        }
        final ProvisionURL provisionURL = (ProvisionURL) sel.iterator().next();
        InputDialog dialog =
            new InputDialog( getShell(), "Add", "Provision from", provisionURL.getUrl(), null );
        if( dialog.open() != Window.OK )
        {
            return;
        }
        updateURL( provisionURL, dialog.getValue() );
    }

    /**
     * Handles delete button push.
     */
    private void onRemoveButtonSelected()
    {
        final IStructuredSelection sel = (IStructuredSelection) m_tableViewer.getSelection();
        if( sel == null || sel.isEmpty() )
        {
            return;
        }
        m_tableViewer.getControl().setRedraw( false );
        for( Iterator i = sel.iterator(); i.hasNext(); )
        {
            m_tableViewer.remove( i.next() );
        }
        m_tableViewer.getControl().setRedraw( true );
    }

    /**
     * Handles a change on a selection in provision table.
     *
     * @param event selection changed event
     */
    private void onTableSelectionChanged( final SelectionChangedEvent event )
    {
        int size = ( (IStructuredSelection) event.getSelection() ).size();
        m_editButton.setEnabled( size == 1 );
        m_deleteButton.setEnabled( size > 0 );
    }

    /**
     * Creates a provision url.
     *
     * @param url created url
     */
    private void addURL( final String url )
    {
        ProvisionURL provisionURL = new ProvisionURL( url, true, true, null, false );
        m_tableViewer.add( provisionURL );
        m_tableViewer.setChecked( provisionURL, provisionURL.isSelected() );
        notifyUpdate();
    }

    /**
     * Updates a provision url after edit.
     *
     * @param provisionURL to be updated
     * @param url          new url
     */
    private void updateURL( final ProvisionURL provisionURL, final String url )
    {
        provisionURL.setUrl( url );
        m_tableViewer.update( provisionURL, null );
        notifyUpdate();
    }

    /**
     * Initialize block.
     *
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom( final ILaunchConfiguration configuration )
    {
        try
        {
            final Map toRestore = (Map) configuration.getAttribute( Attribute.PROVISION_ITEMS, new HashMap() );
            final List provisionURLs = new ArrayList();
            final List selectedURLs = new ArrayList();
            for( Iterator iterator = toRestore.entrySet().iterator(); iterator.hasNext(); )
            {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final ProvisionURL provisionURL = new ProvisionURL();
                provisionURL.setUrl( (String) entry.getKey() );
                if( entry.getValue() != null )
                {
                    final String[] options = ( (String) entry.getValue() ).split( "@" );
                    // first option: selected - boolean
                    if( options.length >= 1 )
                    {
                        provisionURL.setSelected( Boolean.valueOf( options[ 0 ] ).booleanValue() );
                    }
                    // second option: start - boolean
                    if( options.length >= 2 )
                    {
                        provisionURL.setStart( Boolean.valueOf( options[ 1 ] ).booleanValue() );
                    }
                    // third option: start level - integer ("null" if not specified)
                    if( options.length >= 3 && !options[ 2 ].equals( "null" ) )
                    {
                        try
                        {
                            provisionURL.setStartLevel( Integer.valueOf( options[ 2 ] ) );
                        }
                        catch( NumberFormatException ignore )
                        {
                            // ignore. actually it should not happen
                        }
                    }
                    // forth option: update - boolean
                    if( options.length >= 4 )
                    {
                        provisionURL.setUpdate( Boolean.valueOf( options[ 3 ] ).booleanValue() );
                    }
                }
                provisionURLs.add( provisionURL );
                if( provisionURL.isSelected() )
                {
                    selectedURLs.add( provisionURL );
                }
            }
            m_tableViewer.setInput( provisionURLs );
            if( selectedURLs.size() > 0 )
            {
                m_tableViewer.setCheckedElements( selectedURLs.toArray() );
            }
        }
        catch( CoreException ignore )
        {
            // DebugUIPlugin.log(ignore.getStatus());
        }
    }

    /**
     * Saves block configurations attributes.<br/>
     * It will save a map where the key is the url and the value are the other options as
     * selected@start@start_level@update.
     *
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply( final ILaunchConfigurationWorkingCopy configuration )
    {
        // finally, save arguments list
        List arguments = null;
        try
        {
            arguments = configuration.getAttribute( Attribute.RUN_ARGUMENTS, (List) null );
        }
        catch( CoreException ignore )
        {
            // DebugUIPlugin.log(ignore.getStatus());;
        }
        final TableItem[] items = m_tableViewer.getTable().getItems();
        Map toSave = null;
        if( items != null && items.length > 0 )
        {
            if( arguments == null )
            {
                arguments = new ArrayList();
            }
            toSave = new HashMap( items.length );
            for( int i = 0; i < items.length; i++ )
            {
                final ProvisionURL provisionURL = (ProvisionURL) items[ i ].getData();
                StringBuffer options = new StringBuffer()
                    .append( m_tableViewer.getChecked( provisionURL ) )
                    .append( "@" ).append( provisionURL.isStart() )
                    .append( "@" ).append( provisionURL.getStartLevel() )
                    .append( "@" ).append( provisionURL.isUpdate() );
                toSave.put( provisionURL.getUrl(), options.toString() );
                if( provisionURL.isSelected() )
                {
                    final StringBuffer provisionFrom = new StringBuffer( provisionURL.getUrl() );
                    if( provisionURL.getStartLevel() != null )
                    {
                        provisionFrom.append( "@" ).append( provisionURL.getStartLevel() );
                    }
                    if( !provisionURL.isStart() )
                    {
                        provisionFrom.append( "@nostart" );
                    }
                    if( provisionURL.isUpdate() )
                    {
                        provisionFrom.append( "@update" );
                    }
                    arguments.add( provisionFrom.toString() );
                }
            }
        }
        configuration.setAttribute( Attribute.PROVISION_ITEMS, toSave );
        configuration.setAttribute( Attribute.RUN_ARGUMENTS, arguments );
    }

}
