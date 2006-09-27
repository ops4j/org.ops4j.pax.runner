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

import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.runner.idea.packages.PackageInfo;

public class DataBeanImport
{
    private ArrayList<PackageInfo> m_packages;

    public DataBeanImport()
    {
        m_packages = new ArrayList<PackageInfo>();
    }

    public DataBeanImport clone()
    {
        DataBeanImport clone = new DataBeanImport();
        clone.m_packages.addAll( m_packages );
        return clone;
    }

    public void addPackage( PackageInfo packInfo )
    {
        m_packages.add( packInfo );
    }

    public void removePackage( PackageInfo packInfo )
    {
        m_packages.remove( packInfo );
    }

    public List getPackages()
    {
        return m_packages;
    }

}
