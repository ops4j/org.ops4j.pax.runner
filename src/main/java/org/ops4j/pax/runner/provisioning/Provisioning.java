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
package org.ops4j.pax.runner.provisioning;

import org.ops4j.pax.runner.Downloader;
import org.ops4j.pax.runner.CmdLine;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class Provisioning
{
    private Downloader m_downloader;

    public Provisioning( Downloader downloader )
    {
        m_downloader = downloader;
    }

    public List<File> getBundles( CmdLine m_cmdLine )
    {
        // TODO: Initial Provisioning specification in R4 Compendium is to be implemented from here.

        throw new UnsupportedOperationException( "Initial Provisioning is not supported yet!" );
    }

    public Properties getProperties( CmdLine m_cmdLine )
    {
        return new Properties();
    }
}
