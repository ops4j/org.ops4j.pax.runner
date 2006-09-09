package org.ops4j.pax.runner.idea.editor;/*
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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.idea.config.ConfigBean;
import org.ops4j.pax.runner.repositories.BundleInfo;
import org.ops4j.pax.runner.repositories.RepositoryInfo;
import org.ops4j.pax.runner.repositories.RepositoryObserver;

public class AddBundleForm extends DialogWrapper
    implements RepositoryObserver
{

    private static final Logger m_logger = Logger.getLogger( AddBundleForm.class );

    private JPanel m_mainPanel;

    private JButton m_addRepository;
    private JButton m_removeRepository;
    private JTree m_bundles;
    private DefaultMutableTreeNode m_rootNode;
    private ConfigBean m_config;
    private List<RepositoryInfo> m_repositories;

    /**
     * @param parent parent component whicg is used to canculate heavy weight window ancestor.
     */
    public AddBundleForm( Component parent, final ConfigBean config )
    {
        super( parent, true );
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "addBundleForm( " + parent + ")" );
        }
        m_config = config;
        m_config.addRepositoryObserver( this );
        m_repositories = config.getRepositories();
        init();
        initTree( config );
        initActions( config );
    }

    private void initActions( final ConfigBean config )
    {
        m_addRepository.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                new AddRepositoryForm( m_mainPanel, config ).show();
            }
        } );
    }

    private void initTree( ConfigBean config )
    {
        m_rootNode = new DefaultMutableTreeNode();
        RepositoryNodeFactory factory = ApplicationManager.getApplication().getComponent( RepositoryNodeFactory.class );
        List<RepositoryInfo> repos = m_config.getRepositories();
        for( RepositoryInfo repo : repos )
        {
            DefaultMutableTreeNode node = factory.createNode( repo, config );
            m_rootNode.add( node );
        }
        m_bundles.setModel( new DefaultTreeModel( m_rootNode ) );
    }

    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the
     * center of the dialog's content pane. The implementation can return <code>null</code>
     * value. In this case there will be no options panel.
     */
    protected JComponent createCenterPanel()
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "createCenterPanel()" );
        }
        return m_mainPanel;
    }

    /**
     * This method is invoked by default implementation of "OK" action. It just closes dialog
     * with <code>OK_EXIT_CODE</code>. This is convenient place to override functionality of "OK" action.
     * Note that the method does nothing if "OK" action isn't enabled.
     */
    protected void doOKAction()
    {
        TreePath[] selection = m_bundles.getSelectionPaths();
        for( TreePath select : selection )
        {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) select.getLastPathComponent();
            BundleInfo bundle = (BundleInfo) treeNode.getUserObject();
            m_config.addBundle( bundle );
        }
        super.doOKAction();
    }

    /**
     * Dispose the wrapped and releases all resources allocated be the wrapper to help
     * more effecient garbage collection. You should never invoke this method twice or
     * invoke any method of the wrapper after invocation of <code>dispose</code>.
     */
    protected void dispose()
    {
        m_config.removeRepositoryObserver( this );
        super.dispose();
    }

    public void repositoryAdded( RepositoryInfo repoInfo )
    {
        //TODO: Auto-generated, need attention.

    }

    public void repositoryRemoved( RepositoryInfo repoInfo )
    {
        //TODO: Auto-generated, need attention.

    }

    public List<RepositoryInfo> getRepositories()
    {
        return m_repositories;
    }

    public void setRepositories( List<RepositoryInfo> repositories )
    {
        m_repositories = repositories;
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
        m_mainPanel.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new GridLayoutManager( 2, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        m_mainPanel.add( panel1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        final JPanel panel2 = new JPanel();
        panel2.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel1.add( panel2, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints
                                                     .SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                           | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                                                                                  null,
                                                                                                                  null,
                                                                                                                  0,
                                                                                                                  false
        )
        );
        m_addRepository = new JButton();
        m_addRepository.setText( "Add Repository..." );
        panel2.add( m_addRepository, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                          GridConstraints.FILL_HORIZONTAL, GridConstraints
            .SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                                          null, 0, false
        )
        );
        m_removeRepository = new JButton();
        m_removeRepository.setText( "Remove Repository..." );
        panel2.add( m_removeRepository, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                             GridConstraints.FILL_HORIZONTAL, GridConstraints
            .SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                                          null, 0, false
        )
        );
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add( scrollPane1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                                                      GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                                 | GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                                 null, null, null, 0, false
        )
        );
        m_bundles = new JTree();
        m_bundles.setRootVisible( false );
        m_bundles.setShowsRootHandles( true );
        scrollPane1.setViewportView( m_bundles );
    }

    public JComponent $$$getRootComponent$$$()
    {
        return m_mainPanel;
    }
}
