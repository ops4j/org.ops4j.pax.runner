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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

/**
 * Cell Modifier for provisioning table.<br/>
 * Expects the modified elements to be Provisioning URLS's and column have the "right" properties set.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 17, 2007
 */
public class ProvisionCellModifier implements ICellModifier
{

    /**
     * Modify listener.
     */
    private ModifyListener m_modifyListener;

    /**
     * All cells beside "url" are modifiable.
     *
     * @see ICellModifier#canModify(Object, String)
     */
    public boolean canModify( Object element, String property )
    {
        return !"url".equals( property );
    }

    /**
     * @see ICellModifier#getValue(Object, String)
     */
    public Object getValue( final Object element, final String property )
    {
        if( element instanceof ProvisionURL )
        {
            final ProvisionURL provisionURL = (ProvisionURL) element;
            if( "start".equals( property ) )
            {
                return Boolean.valueOf( provisionURL.isStart() );
            }
            if( "level".equals( property ) )
            {
                if( provisionURL.getStartLevel() == null )
                {
                    return "";
                }
                return provisionURL.getStartLevel().toString();
            }
            if( "update".equals( property ) )
            {
                return Boolean.valueOf( provisionURL.isUpdate() );
            }
        }
        return null;
    }

    /**
     * @see ICellModifier#modify(Object, String, Object)
     */
    public void modify( final Object element, final String property, final Object value )
    {
        final TableItem item = (TableItem) element;
        if( item.getData() instanceof ProvisionURL )
        {
            final ProvisionURL provisionURL = (ProvisionURL) item.getData();
            if( "start".equals( property ) )
            {
                provisionURL.setStart( ( (Boolean) value ).booleanValue() );
            }
            if( "level".equals( property ) )
            {
                if( value == null || ( (String) value ).trim().length() == 0 )
                {
                    provisionURL.setStartLevel( null );
                }
                else
                {
                    try
                    {
                        provisionURL.setStartLevel( new Integer( (String) value ) );
                    } catch( NumberFormatException ignore )
                    {
                        // just not set the value
                    }
                }
            }
            if( "update".equals( property ) )
            {
                provisionURL.setUpdate( ( (Boolean) value ).booleanValue() );
            }
            if( m_modifyListener != null )
            {
                m_modifyListener.modified( item.getData() );
            }
        }
    }

    /**
     * Sets the modify listener.
     *
     * @param modifyListener a listener
     */
    public void setModifyListener( final ModifyListener modifyListener )
    {
        m_modifyListener = modifyListener;
    }

    /**
     * A modify listener is notified on succesfull update of a modified cell.
     */
    public interface ModifyListener
    {

        /**
         * Notification on succesfull update.
         *
         * @param element modified element.
         */
        void modified( Object element );

    }

}
