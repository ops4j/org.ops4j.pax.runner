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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for provisioning table.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class ProvisionLabelProvider
    extends LabelProvider
    implements ITableLabelProvider
{

    /**
     * Expects every element to be a ProvisionURL.
     *
     * @see ITableLabelProvider#getColumnText(Object, int)
     */
    public String getColumnText( final Object element, final int columnIndex )
    {
        if( !( element instanceof ProvisionURL ) )
        {
            throw new IllegalArgumentException( "Elements must be instances of " + ProvisionURL.class.getName() );
        }
        final ProvisionURL provisionURL = (ProvisionURL) element;
        switch( columnIndex )
        {
            case 0:
                return provisionURL.getUrl();
            case 1:
                return provisionURL.isStart() ? "yes" : "no";
            case 2:
                return provisionURL.getStartLevel() == null ? "default" : provisionURL.getStartLevel().toString();
            case 3:
                return provisionURL.isUpdate() ? "yes" : "no";
            default:
                return null;
        }
    }

    /**
     * Images are not used.
     *
     * @see ITableLabelProvider#getColumnImage(Object, int)
     */
    public Image getColumnImage( final Object element, final int columnIndex )
    {
        return null;
    }

}
