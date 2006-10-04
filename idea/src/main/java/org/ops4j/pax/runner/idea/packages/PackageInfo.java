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

import java.util.HashMap;

public class PackageInfo
    implements Comparable
{

    protected String m_packageName;
    protected VersionRange m_versionRange;
    protected Directives m_directives;
    protected Attributes m_attributes;
    protected boolean m_exported;
    protected PackageInfo m_parent;
    protected Object m_extra;
    protected boolean m_changed;
    private boolean m_readOnly;

    public PackageInfo( String packageName, PackageInfo parent )
    {
        this( packageName, parent, null, false, null, null);
    }

    public PackageInfo( String packageName, PackageInfo parent, VersionRange versionRange, boolean exported,
                        Directives directives, Attributes attributes )
    {
        m_exported = exported;
        if( m_exported || versionRange == null )
        {
            versionRange = new VersionRange( "", "" );
        }
        if( attributes == null )
        {
            attributes = new Attributes( new HashMap<String, String>());
        }
        if( directives == null )
        {
            directives = new Directives( new HashMap<String, String>() );
        }
        m_attributes = attributes;
        m_directives = directives;
        m_versionRange = versionRange;
        m_packageName = packageName;
        m_parent = parent;
        m_changed = false;
    }

    public String getPackageName()
    {
        return m_packageName;
    }

    public PackageInfo getParent()
    {
        return m_parent;
    }

    public boolean isExported()
    {
        return m_exported;
    }

    public String getLowerVersion()
    {
        return m_versionRange.getLower();
    }

    public String getUpperVersion()
    {
        return m_versionRange.getUpper();
    }

    public VersionRange getVersionRange()
    {
        return m_versionRange;
    }

    public Directives getDirectives()
    {
        return m_directives;
    }

    public Attributes getAttributes()
    {
        return m_attributes;
    }

    public Object getExtra()
    {
        return m_extra;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     *
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     *
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param o the Object to be compared.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo( Object o )
    {
        if( o instanceof PackageInfo )
        {
            PackageInfo other = (PackageInfo) o;
            return m_packageName.compareTo( other.m_packageName );
        }
        return -1;
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();
        result.append( m_packageName );
        if( m_exported )
        {
            String version = m_versionRange.toString();
            result.append(  "version=" );
            result.append( version );
            result.append( "; ");
            result.append( m_directives );
            result.append( "; ");
            result.append( m_attributes );
        }
        return result.toString();
    }

    public boolean hasChanged()
    {
        return m_changed;
    }

    public void resetChange()
    {
        m_changed = false;
    }

    public void setExported( boolean exported )
    {
        if( m_readOnly )
        {
            return;
        }
        m_exported = exported;
        m_changed = true;
    }

    public void setLowerVersion( String version )
    {
        if( m_readOnly )
        {
            return;
        }
        m_versionRange.setLower( version );
        m_changed = true;
    }

    public void setUpperVersion( String version )
    {
        if( m_readOnly )
        {
            return;
        }
        m_versionRange.setUpper( version );
        m_changed = true;
    }

    public void setExtra( Object extra )
    {
        if( m_readOnly )
        {
            return;
        }
        m_extra = extra;
    }

    public void makeReadOnly()
    {
        m_readOnly = true;
    }

    public void notifyChange()
    {
        m_changed = true;
    }
}
