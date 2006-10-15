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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import org.apache.log4j.Logger;
import org.ops4j.pax.runner.idea.packages.PackageInfo;

public class PackageTreeCellRenderer
    implements TreeCellRenderer
{

    private static final Logger m_logger = Logger.getLogger( PackageTreeCellRenderer.class );

    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean hasFocus )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getTreeCellRendererComponent(" + tree + "," + value + ", " + selected + ", " + expanded
                            + ", " + leaf + ", " + row + ", " + hasFocus + ")"
            );
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();
        if( node.isRoot() )
        {
            return new JLabel( "<packages>" );
        }
        else
        {
            final PackageInfo metainfo = (PackageInfo) userObject;
            JCheckBox checkBox = new JCheckBox( metainfo.getPackageName() );
            checkBox.setSelected( metainfo.isExported() );
            checkBox.setBackground( tree.getBackground() );
            return checkBox;
        }
    }
}
