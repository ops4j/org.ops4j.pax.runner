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

import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.idea.config.ConfigBean;
import org.ops4j.pax.runner.repositories.BundleInfo;
import org.ops4j.pax.runner.repositories.BundleObserver;

public class OsgiConfigEditorForm
    implements BundleObserver
{

    private static final Logger m_logger = Logger.getLogger( OsgiConfigEditorForm.class );

    private JPanel m_mainPanel;
    private JButton m_add;
    private JButton m_remove;

    private JList m_platforms;
    private JList m_bundles;

    private JTextField m_proxyHost;
    private JTextField m_proxyPort;
    private JTextField m_proxyUser;
    private JPasswordField m_proxyPass;
    private JCheckBox m_startGui;
    private JCheckBox m_runClean;
    private DefaultListModel m_bundleModel;
    private DefaultListModel m_platformModel;
    private JTextField m_vmArguments;
    private JTable m_systemProperties;
    private JTable m_bundleProperties;
    private JTextArea m_description;
    private JTextField m_workDir;
    private JButton m_selectDir;
    private JTextField m_jdk;
    private JButton m_selectJdk;
    private AddBundleForm m_addBundleForm;

    public OsgiConfigEditorForm( final ConfigBean config )
    {
        m_add.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                if( m_addBundleForm == null )
                {
                    m_addBundleForm = new AddBundleForm( m_mainPanel, config );
                }
                m_addBundleForm.show();
            }
        }
        );
        String[] columnNames = { "Property", "Value" };
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers( columnNames );
        m_bundleProperties.setModel( tableModel );
        m_bundleProperties.setShowGrid( true );
        m_bundleProperties.setEnabled( false );
        config.addBundleObserver( this );
        m_bundleModel = new DefaultListModel();
        m_bundles.setModel( m_bundleModel );
    }

    public JComponent getMainPanel()
    {
        return m_mainPanel;
    }

    public void setData( ConfigBean data )
    {
        m_startGui.setSelected( data.isStartGui() );
        m_runClean.setSelected( data.isRunClean() );
        m_vmArguments.setText( data.getVmArguments() );
        m_proxyHost.setText( data.getProxyHost() );
        m_proxyPort.setText( data.getProxyPort() );
        m_proxyUser.setText( data.getProxyUser() );
        m_proxyPass.setText( data.getProxyPass() );
        updatePlatformList( data );
        updateSystemPropertiesTable( data );
        m_workDir.setText( data.getWorkDir().getAbsolutePath() );
        ProjectJdk jdk = data.getJdk();
        String jdkName = jdk.getName();
        m_jdk.setText( jdkName );
        updateBundlesList( data );
    }

    private void updateBundlesList( ConfigBean data )
    {
        m_bundleModel = new DefaultListModel();
        for( BundleInfo bundle : data.getBundles() )
        {
            m_bundleModel.addElement( bundle );
        }
        m_bundles.setModel( m_bundleModel );
    }

    private void updateSystemPropertiesTable( ConfigBean data )
    {
        String[] columnNames = { "Property", "Value" };
        DefaultTableModel tableModel2 = new DefaultTableModel();
        tableModel2.setColumnIdentifiers( columnNames );
        for( Map.Entry<String, String> entry : data.getSystemProperties().entrySet() )
        {
            String key = entry.getKey();
            String value = entry.getValue();
            tableModel2.addRow( new String[]{ key, value } );
        }
        m_systemProperties.setModel( tableModel2 );
        m_systemProperties.setEnabled( true );
    }

    private void updatePlatformList( ConfigBean data )
    {
        String selected = data.getSelectedPlatform();
        m_platformModel = new DefaultListModel();
        ArrayList<String> platforms = data.getPlatforms();
        int count = 0;
        for( String platform : platforms )
        {
            m_platformModel.addElement( platform );
            if( selected.equals( platform ) )
            {
                m_platforms.setSelectedIndex( count );
            }
            count++;
        }
        m_platforms.setModel( m_platformModel );
    }

    public void getData( ConfigBean data )
    {
        data.setStartGui( m_startGui.isSelected() );
        data.setRunClean( m_runClean.isSelected() );
        data.setVmArguments( m_vmArguments.getText() );
        data.setProxyHost( m_proxyHost.getText() );
        data.setProxyPort( m_proxyPort.getText() );
        data.setProxyUser( m_proxyUser.getText() );
        data.setProxyPass( m_proxyPass.getText() );

// TODO: Unclear how to handle the Repositories.
//        List<RepositoryInfo> repositories = m_addBundleForm.getRepositories();
//        data.setRepositories( repositories );

        retrieveSystemProperties( data );
        retrieveBundles( data );

        String workDir = m_workDir.getText();
        data.setWorkDir( new File( workDir ) );

        // TODO: The ProjectJDK is not being retrieved back into the ConfigBean.
    }

    private void retrieveBundles( ConfigBean data )
    {
        List<BundleInfo> bundles = new ArrayList<BundleInfo>();
        int size = m_bundleModel.size();
        for( int i = 0 ; i < size ; i++ )
        {
            BundleInfo bundle = (BundleInfo) m_bundleModel.get( i );
            bundles.add( bundle );
        }
        data.setBundles( bundles );
    }

    private void retrieveSystemProperties( ConfigBean data )
    {
        TableModel model = m_systemProperties.getModel();
        Map<String, String> sysProps = new HashMap<String, String>();
        int rowCount = model.getRowCount();
        for( int row = 0; row < rowCount; row++ )
        {
            String key = (String) model.getValueAt( row, 0 );
            String value = (String) model.getValueAt( row, 1 );
            sysProps.put( key, value );
        }
        data.setSystemProperties( sysProps );
    }

    public boolean isModified( ConfigBean data )
    {
        if( m_startGui.isSelected() != data.isStartGui() )
        {
            return true;
        }
        if( m_runClean.isSelected() != data.isRunClean() )
        {
            return true;
        }
        if( m_vmArguments.getText() != null
            ? !m_vmArguments.getText().equals( data.getVmArguments() )
            : data.getVmArguments() != null )
        {
            return true;
        }
        if( m_proxyHost.getText() != null
            ? !m_proxyHost.getText().equals( data.getProxyHost() )
            : data.getProxyHost() != null )
        {
            return true;
        }
        if( m_proxyPort.getText() != null
            ? !m_proxyPort.getText().equals( data.getProxyPort() )
            : data.getProxyPort() != null )
        {
            return true;
        }
        if( m_proxyUser.getText() != null
            ? !m_proxyUser.getText().equals( data.getProxyUser() )
            : data.getProxyUser() != null )
        {
            return true;
        }
        if( m_proxyPass.getText() != null
            ? !m_proxyPass.getText().equals( data.getProxyPass() )
            : data.getProxyPass() != null )
        {
            return true;
        }
        return false;
    }

    public void bundleAdded( BundleInfo bundle )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "bundleAdded( " + bundle + " )" );
        }
        m_bundleModel.addElement( bundle );
    }

    public void bundleRemoved( BundleInfo bundle )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "bundleRemoved( " + bundle + " )" );
        }
        m_bundleModel.removeElement( bundle );
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     */
    private void $$$setupUI$$$()
    {
        m_mainPanel = new JPanel();
        m_mainPanel.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new GridLayoutManager( 7, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        m_mainPanel.add( panel1, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        final JPanel panel2 = new JPanel();
        panel2.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel2, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        panel2.setBorder( BorderFactory.createTitledBorder( "Platform" ) );
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add( scrollPane1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        m_platforms = new JList();
        scrollPane1.setViewportView( m_platforms );
        final JPanel panel3 = new JPanel();
        panel3.setLayout( new GridLayoutManager( 3, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel3, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        panel3.setBorder( BorderFactory.createTitledBorder( "Options" ) );
        m_startGui = new JCheckBox();
        m_startGui.setText( "Start GUI" );
        panel3.add( m_startGui, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                         .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                               null, null, 0, false
        )
        );
        m_runClean = new JCheckBox();
        m_runClean.setText( "Run Clean" );
        panel3.add( m_runClean, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                         .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                                                               null, null, 0, false
        )
        );
        final JScrollPane scrollPane2 = new JScrollPane();
        panel1.add( scrollPane2, new GridConstraints( 5, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        scrollPane2.setBorder( BorderFactory.createTitledBorder( "System Properties" ) );
        m_systemProperties = new JTable();
        m_systemProperties.setEnabled( true );
        scrollPane2.setViewportView( m_systemProperties );
        final JPanel panel4 = new JPanel();
        panel4.setLayout( new GridLayoutManager( 4, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel4, new GridConstraints( 6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        panel4.setBorder( BorderFactory.createTitledBorder( "Proxy" ) );
        final JLabel label1 = new JLabel();
        label1.setText( "Port:" );
        panel4.add( label1, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label2 = new JLabel();
        label2.setText( "Username:" );
        panel4.add( label2, new GridConstraints( 2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JLabel label3 = new JLabel();
        label3.setText( "Password:" );
        panel4.add( label3, new GridConstraints( 3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        m_proxyHost = new JTextField();
        panel4.add( m_proxyHost, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                      null, 0, false
        )
        );
        m_proxyPort = new JTextField();
        panel4.add( m_proxyPort, new GridConstraints( 1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                      null, 0, false
        )
        );
        m_proxyUser = new JTextField();
        panel4.add( m_proxyUser, new GridConstraints( 2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                      null, 0, false
        )
        );
        m_proxyPass = new JPasswordField();
        panel4.add( m_proxyPass, new GridConstraints( 3, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                      null, 0, false
        )
        );
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment( 10 );
        label4.setHorizontalTextPosition( 10 );
        label4.setText( "Host:" );
        panel4.add( label4, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        final JPanel panel5 = new JPanel();
        panel5.setLayout( new GridLayoutManager( 1, 3, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel5, new GridConstraints( 2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        final JLabel label5 = new JLabel();
        label5.setText( "Working Dir:" );
        panel5.add( label5, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        m_workDir = new JTextField();
        panel5.add( m_workDir, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                    GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_WANT_GROW,
                                                    GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ),
                                                    null, 0, false
        )
        );
        m_selectDir = new JButton();
        m_selectDir.setText( "..." );
        panel5.add( m_selectDir, new GridConstraints( 0, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_HORIZONTAL, GridConstraints
            .SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                                          null, 0, false
        )
        );
        final JPanel panel6 = new JPanel();
        panel6.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel6, new GridConstraints( 4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        final JLabel label6 = new JLabel();
        label6.setText( "VM Arguments:" );
        panel6.add( label6, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        m_vmArguments = new JTextField();
        panel6.add( m_vmArguments, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                        GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_WANT_GROW,
                                                        GridConstraints.SIZEPOLICY_FIXED, null,
                                                        new Dimension( 150, -1 ), null, 0, false
        )
        );
        final JPanel panel7 = new JPanel();
        panel7.setLayout( new GridLayoutManager( 1, 3, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel7, new GridConstraints( 3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        final JLabel label7 = new JLabel();
        label7.setText( "JDK:" );
        panel7.add( label7, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                 GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                                                 null, null, null, 0, false
        )
        );
        m_jdk = new JTextField();
        m_jdk.setEditable( false );
        panel7.add( m_jdk, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                                                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 150, -1 ), null,
                                                0, false
        )
        );
        m_selectJdk = new JButton();
        m_selectJdk.setText( "..." );
        panel7.add( m_selectJdk, new GridConstraints( 0, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_HORIZONTAL, GridConstraints
            .SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                                          null, 0, false
        )
        );
        final JPanel panel8 = new JPanel();
        panel8.setLayout( new GridLayoutManager( 4, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        m_mainPanel.add( panel8, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        panel8.setBorder( BorderFactory.createTitledBorder( "Bundles" ) );
        final JPanel panel9 = new JPanel();
        panel9.setLayout( new GridLayoutManager( 1, 5, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel8.add( panel9, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        m_add = new JButton();
        m_add.setText( "Add..." );
        panel9.add( m_add, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 GridConstraints.SIZEPOLICY_FIXED, null,
                                                                                 null, null, 0, false
        )
        );
        m_remove = new JButton();
        m_remove.setText( "Remove..." );
        panel9.add( m_remove, new GridConstraints( 0, 3, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                   GridConstraints.FILL_HORIZONTAL, GridConstraints
            .SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                                          null, 0, false
        )
        );
        final JScrollPane scrollPane3 = new JScrollPane();
        panel8.add( scrollPane3, new GridConstraints( 3, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        scrollPane3.setBorder( BorderFactory.createTitledBorder( "Properties" ) );
        m_bundleProperties = new JTable();
        scrollPane3.setViewportView( m_bundleProperties );
        final JScrollPane scrollPane4 = new JScrollPane();
        panel8.add( scrollPane4, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        m_bundles = new JList();
        scrollPane4.setViewportView( m_bundles );
        final JPanel panel10 = new JPanel();
        panel10.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel8.add( panel10, new GridConstraints( 2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                      .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                            | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                   null,
                                                                                                                   null,
                                                                                                                   0,
                                                                                                                   false
        )
        );
        panel10.setBorder( BorderFactory.createTitledBorder( "Description" ) );
        m_description = new JTextArea();
        panel10.add( m_description, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                         GridConstraints.FILL_BOTH,
                                                         GridConstraints.SIZEPOLICY_WANT_GROW,
                                                         GridConstraints.SIZEPOLICY_WANT_GROW, null,
                                                         new Dimension( 150, 50 ), null, 0, false
        )
        );
    }

    public JComponent $$$getRootComponent$$$()
    {
        return m_mainPanel;
    }
}
