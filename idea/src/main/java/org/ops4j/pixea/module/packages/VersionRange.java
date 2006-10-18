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

public class VersionRange
{
    private String m_lower;
    private String m_upper;

    public VersionRange( String lower, String upper )
    {
        m_lower = lower;
        m_upper = upper;
    }

    public String getLower()
    {
        return m_lower;
    }

    public String getUpper()
    {
        return m_upper;
    }

    public void setLower( String lower )
    {
        m_lower = lower;
    }

    public void setUpper( String upper )
    {
        m_upper = upper;
    }
}
