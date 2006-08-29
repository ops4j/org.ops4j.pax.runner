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
package org.ops4j.pax.runner;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class RepositoryAggregator
    implements Repository
{
    private List<Downloader> m_downloaders;

    public RepositoryAggregator( String[] repositories, boolean noCheckMD5 )
        throws MalformedURLException
    {
        m_downloaders = new ArrayList<Downloader>();
        for( String repository : repositories )
        {
            m_downloaders.add( new Downloader( repository, noCheckMD5 ) );
        }
    }

    public void download( String relativePath, File destination, boolean force )
        throws IOException
    {
        // Better algorithm than a simple try each sequentially.
        for( Repository repo : m_downloaders )
        {
            try
            {
                repo.download( relativePath, destination, force );
                return;
            } catch( IOException e )
            {
                // Ok, try next.
            }
        }
        throw new IOException( "Unable to find " + relativePath );
    }
}
