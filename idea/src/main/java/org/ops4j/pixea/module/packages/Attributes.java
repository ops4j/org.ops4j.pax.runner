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
package org.ops4j.pixea.module.packages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

public class Attributes
{
    protected HashMap<String, String> m_attributes;
    private PackageInfo m_owner;

    public Attributes( HashMap<String, String> directives )
    {
        m_attributes = directives;
    }

    public String getAttributes( String name )
    {
        return m_attributes.get( name );
    }

    Set<Map.Entry<String,String>> getAllAttributes()
    {
        return Collections.unmodifiableSet( m_attributes.entrySet() );
    }


    public String toString()
    {
        StringBuffer result = new StringBuffer();
        boolean first = true;
        for( Map.Entry<String,String> entry : m_attributes.entrySet() )
        {
            if( ! first )
            {
                result.append( "; " );
            }
            first = false;
            String name = entry.getKey();
            String value = entry.getValue();
            result.append( name );
            result.append( "=\"" );
            result.append( value );
            result.append( "\"" );
        }
        return result.toString();
    }

    public void setOwner( PackageInfo owner )
    {
        m_owner = owner;
    }

    public void addAttribute( String name, String value )
    {
        m_owner.notifyChange();
        m_attributes.put( name, value );
    }

    public void removeAttribute( String name )
    {
        m_owner.notifyChange();
        m_attributes.remove( name );
    }
}
