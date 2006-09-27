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
package org.ops4j.pax.runner.idea.packages;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.apache.log4j.Logger;

public class PackageTracker
    implements RefactoringElementListenerProvider, RefactoringElementListener, PsiTreeChangeListener
{
    private static final Logger m_logger = Logger.getLogger( PackageTracker.class );
    private PackageManager m_packageManager;

    public PackageTracker( PackageManager packageManager )
    {
        m_packageManager = packageManager;
    }

    /**
     * Should return a listener for particular element. Invoked in read action.
     */
    public RefactoringElementListener getListener( PsiElement element )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "getListener(" + element + " )");
        }
        return this;
    }

    /**
     * Invoked in write action and command.
     */
    public void elementMoved( PsiElement newElement )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "elementMoved(" + newElement + " )" );
        }
    }

    /**
     * Invoked in write action and command.
     */
    public void elementRenamed( PsiElement newElement )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "elementRenamed( " + newElement +" )");
        }
    }

    /**
     * Invoked just before adding a child to the tree.<br>
     * Parent element is returned by <code>event.getParent()</code>.<br>
     * Added child is returned by <code>event.getChild</code>.
     *
     * @param event the event object describing the change.
     */
    public void beforeChildAddition( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "beforeChildAddition( " + event + " )");
            m_logger.debug( "          parent:" + event.getParent() );
            m_logger.debug( "           child:" + event.getChild() );
        }
    }

    /**
     * Invoked just before removal of a child from the tree.<br>
     * Child to be removed is returned by <code>event.getChild()</code>.<br>
     * Parent element is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void beforeChildRemoval( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "beforeChildRemoval( " + event + " )" );
            m_logger.debug( "          parent:" + event.getParent() );
            m_logger.debug( "           child:" + event.getChild() );
        }
    }

    /**
     * Invoked just before replacement of a child in the tree by another element.<br>
     * Child to be replaced is returned by <code>event.getOldChild()</code>.<br>
     * Parent element is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void beforeChildReplacement( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "beforeChildReplacement( " + event + " )");
            m_logger.debug( "          parent:" + event.getParent() );
            m_logger.debug( "        oldChild:" + event.getOldChild() );
            m_logger.debug( "        newChild:" + event.getNewChild() );
        }
    }

    /**
     * Invoked just before movement of a child in the tree by changing its parent or by changing its position in the same parent.<br>
     * Child to be moved is returned by <code>event.getChild()</code>.<br>
     * The old parent is returned by <code>event.getOldParent()</code>.<br>
     * The new parent is returned by <code>event.getNewParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void beforeChildMovement( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "beforeChildMovement( " + event + " )" );
            m_logger.debug( "           child:" + event.getChild() );
            m_logger.debug( "       oldParent:" + event.getOldParent() );
            m_logger.debug( "       newParent:" + event.getNewParent() );
        }
    }

    /**
     * Invoked before a mass change of children of the specified node.<br>
     * The parent the nodes of which are changing is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void beforeChildrenChange( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "beforeChildrenChange( " + event + " )");
            m_logger.debug( "          parent:" + event.getParent() );
        }
    }

    /**
     * Invoked just before changing of some property of an element.<br>
     * Element, whose property is to be changed is returned by <code>event.getElement()</code>.<br>
     * The property name is returned by <code>event.getPropertyName()</code>.<br>
     * The old property value is returned by <code>event.getOldValue()</code>.
     *
     * @param event the event object describing the change.
     */
    public void beforePropertyChange( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "beforePropertyChange( " + event + " )" );
            m_logger.debug( "         element:" + event.getElement() );
            m_logger.debug( "    propertyName:" + event.getPropertyName() );
            m_logger.debug( "        oldValue:" + event.getOldValue() );
            m_logger.debug( "        newValue:" + event.getNewValue() );
        }
    }

    /**
     * Invoked just after adding of a new child to the tree.<br>
     * The added child is returned by <code>event.getChild()</code>.<br>
     * Parent element is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void childAdded( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "childAdded( " + event + " )");
            m_logger.debug( "          parent:" + event.getParent() );
            m_logger.debug( "           child:" + event.getChild() );
        }
    }

    /**
     * Invoked just after removal of a child from the tree.<br>
     * The removed child is returned by <code>event.getChild()</code>. Note that
     * only <code>equals()</code>, <code>hashCode()</code>, <code>isValid()</code> methods
     * can be safely invoked for this element, because it's not valid anymore.<br>
     * Parent element is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void childRemoved( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "childRemoved( " + event + " )");
            m_logger.debug( "          parent:" + event.getParent() );
            m_logger.debug( "           child:" + event.getChild() );
        }
    }

    /**
     * Invoked just after replacement of a child in the tree by another element.<br>
     * The replaced child is returned by <code>event.getOldChild()</code>. Note that
     * only <code>equals()</code>, <code>hashCode()</code>, <code>isValid()</code> methods
     * can be safely invoked for this element, because it's not valid anymore.<br>
     * The new child is returned by <code>event.getNewChild()</code>.<br>
     * Parent element is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void childReplaced( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "childReplaced( " + event + " )" );
            m_logger.debug( "          parent:" + event.getParent() );
            m_logger.debug( "        oldChild:" + event.getOldChild() );
            m_logger.debug( "        newChild:" + event.getNewChild() );
        }
    }

    /**
     * Invoked after a mass change of children of the specified node.<br>
     * The parent the nodes of which have changed is returned by <code>event.getParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void childrenChanged( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "childrenChanged( " + event + " )");
            m_logger.debug( "          parent:" + event.getParent() );
        }

    }

    /**
     * Invoked just after movement of a child in the tree by changing its parent or by changing its position in the same parent.<br>
     * The moved child is returned by <code>event.getChild()</code>.<br>
     * The old parent is returned by <code>event.getOldParent()</code>.<br>
     * The new parent is returned by <code>event.getNewParent()</code>.
     *
     * @param event the event object describing the change.
     */
    public void childMoved( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "childMoved( " + event + " )" );
            m_logger.debug( "           child:" + event.getChild() );
            m_logger.debug( "       oldParent:" + event.getOldParent() );
            m_logger.debug( "       newParent:" + event.getNewParent() );
        }
    }

    /**
     * Invoked just after changing of some property of an element.<br>
     * Element, whose property has changed is returned by <code>event.getElement()</code>.<br>
     * The property name is returned by <code>event.getPropertyName()</code>.<br>
     * The old property value is returned by <code>event.getOldValue()</code>.<br>
     * The new property value is returned by <code>event.getNewValue()</code>.
     *
     * @param event the event object describing the change.
     */
    public void propertyChanged( PsiTreeChangeEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "propertyChanged( " + event + " )" );
            m_logger.debug( "         element:" + event.getElement() );
            m_logger.debug( "    propertyName:" + event.getPropertyName() );
            m_logger.debug( "        oldValue:" + event.getOldValue() );
            m_logger.debug( "        newValue:" + event.getNewValue() );
        }
    }
}
