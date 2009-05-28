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

import org.eclipse.swt.widgets.Composite;

/**
 * Base class for Pax Cursor blocks that adds easy to use notification for the Tab that something was changed so the
 * Launch Dialog get's notified that it should apply (call) for changes.
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, December 16, 2007
 */
public class CursorTabBlock
    extends Composite
{

    /**
     * Pax Cursor Tab.
     */
    private CursorTab m_cursorTab;

    /**
     * @see Composite#Composite(Composite, int)
     */
    public CursorTabBlock( final Composite parent, final int style )
    {
        super( parent, style );
    }

    /**
     * Sets Pax Curstor Tab.
     *
     * @param cursorTab cursor tab
     */
    public void setCursorTab( final CursorTab cursorTab )
    {
        this.m_cursorTab = cursorTab;
    }

    /**
     * Notify that a change occured in this block. Delegates to Pax Cursor Tab.
     */
    protected void notifyUpdate()
    {
        if( m_cursorTab != null )
        {
            m_cursorTab.updateLaunchConfigurationDialog();
        }
    }

}