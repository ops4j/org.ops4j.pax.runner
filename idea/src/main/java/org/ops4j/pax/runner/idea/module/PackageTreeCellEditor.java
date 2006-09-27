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

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.idea.packages.PackageInfo;

public class PackageTreeCellEditor extends AbstractCellEditor
    implements TreeCellEditor
{
    private static final Logger m_logger = Logger.getLogger( PackageTreeCellEditor.class );

    private Object m_value;

    /**
     * Returns the value contained in the editor.
     *
     * @return the value contained in the editor
     */
    public Object getCellEditorValue()
    {
        return m_value;
    }

    /**
     * Sets an initial <I>value</I> for the editor.  This will cause
     * the editor to stopEditing and lose any partially edited value
     * if the editor is editing when this method is called. <p>
     *
     * Returns the component that should be added to the client's
     * Component hierarchy.  Once installed in the client's hierarchy
     * this component will then be able to draw and receive user input.
     *
     * @param    tree        the JTree that is asking the editor to edit;
     * this parameter can be null
     * @param    value        the value of the cell to be edited
     * @param    isSelected    true is the cell is to be renderer with
     * selection highlighting
     * @param    expanded    true if the node is expanded
     * @param    leaf        true if the node is a leaf node
     * @param    row        the row index of the node being edited
     * @return the component for editing
     */
    public Component getTreeCellEditorComponent( JTree tree, Object value, boolean isSelected, boolean expanded,
                                                 boolean leaf, int row )
    {
        m_value = value;
        m_logger.debug( "getTreeCellEditorComponent(" + tree + "," + value + ", " + isSelected + ", " + expanded
                        + ", " + leaf + ", " + row + ")"
        );
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();
        if( node.isRoot() )
        {
            return new JLabel( "<packages>" );
        }
        else
        {
            final PackageInfo pakkage = (PackageInfo) userObject;
            JCheckBox checkBox = new JCheckBox( pakkage.getPackageName() );
            checkBox.setBackground( tree.getBackground() );
            checkBox.setEnabled( true );
            checkBox.addChangeListener( new ChangeListener()
            {

                public void stateChanged( ChangeEvent e )
                {
                    JCheckBox cb = (JCheckBox) e.getSource();
                    pakkage.setExported( cb.isSelected() );
                }
            } );
            return checkBox;
        }
    }
}
