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
package org.ops4j.pax.runner.repositories;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class EclipseUpdateRepository extends DefaultMutableTreeNode
    implements MutableTreeNode, Repository
{

    public EclipseUpdateRepository( RepositoryInfo repository )
    {
        super( repository, true );
    }

    public String getUrl()
    {
        return ( (RepositoryInfo) getUserObject() ).getUrl();
    }

    public RepositoryInfo getInfo()
    {
        return (RepositoryInfo) getUserObject();
    }

    public void download( BundleInfo bundle )
    {
        //TODO: Auto-generated, need attention.
    }
}
