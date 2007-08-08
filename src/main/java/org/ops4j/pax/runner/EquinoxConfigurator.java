/*
 * Copyright 2007 Damian Golda.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.pom.BundleManager;
import org.xml.sax.SAXException;

/**
 * Reads configuration data for Equinox from text file.
 *
 * @author Damian.Golda@gmail.com
 */
public class EquinoxConfigurator
{

    private GroupArtifactVersion m_systemArtifact;

    private List m_defaultArtifacts;

    /**
     * Loads artifacts' data for use with equinox.
     *
     * First, reads equinox.txt from current directory. If not found, reads
     * /equinox.txt from classpath.
     *
     * @param workDir
     */
    public void load( File workDir )
    {
        File file = new File( workDir, "equinox.txt" );
        String resourceName = "/equinox.txt";
        load( file, resourceName );
    }

    void load( File file, String resourceName )
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream( file );
            if( load( in ) )
            {
                System.out.println( " Equinox Config: " + file.getAbsolutePath() );
                return;
            }
        } catch( IOException e )
        {
            // ignore exception and try to read resource from classpath
            /*
             * System.err.println("File " + file.getAbsolutePath() + " not
             * found, trying to read resource");
             */
        } finally
        {
            closeQuietly( in );
        }
//        System.out.println("Cannot configure Equinox using "
//                + file.getAbsolutePath());
        try
        {
            in = getClass().getResourceAsStream( resourceName );
            if( in != null )
            {
                if( load( in ) )
                {
                    System.out.println( " Equinox Config: " + resourceName );
                    return;
                }
            }
        } catch( IOException e )
        {
            // ignore, leave m_system and m_bundles set to nulls
        } finally
        {
            closeQuietly( in );
        }
        throw new IllegalStateException( "Resource " + resourceName + " not found." );
    }

    /**
     * @param in
     *
     * @return true if successfully read at least one artifact data.
     */
    private boolean load( InputStream in )
        throws IOException
    {

        BufferedReader reader = null;
        try
        {
            List bundles = new ArrayList();
            reader = new BufferedReader( new InputStreamReader( in ) );
            while( true )
            {
                String line = reader.readLine();
                // end of file
                if( line == null )
                {
                    break;
                }
                // ignore comments and empty lines
                if( line.startsWith( "#" ) || line.trim().length() == 0 )
                {
                    continue;
                }
                String[] segments = splitLine( line );
                if( segments != null && segments.length == 3 )
                {
                    GroupArtifactVersion bundle = new GroupArtifactVersion(
                        segments[ 0 ], segments[ 1 ], segments[ 2 ]
                    );
                    bundles.add( bundle );
                }
            }
            if( bundles.size() > 0 )
            {
                m_systemArtifact = (GroupArtifactVersion) bundles.remove( 0 );
                m_defaultArtifacts = bundles;
                return true;
            }
            return false;
        } finally
        {
            closeQuietly( reader );
        }
    }

    String[] splitLine( String line )
    {
        String[] segments = line.split( ":" );
        return segments;
    }

    public static void closeQuietly( InputStream in )
    {
        if( in != null )
        {
            try
            {
                in.close();
            } catch( IOException e )
            {
                // ignore
            }
        }
    }

    public static void closeQuietly( Reader reader )
    {
        if( reader != null )
        {
            try
            {
                reader.close();
            } catch( IOException e )
            {
                // ignore
            }
        }
    }

    public GroupArtifactVersion getSystemBundleArtifact()
    {
        return m_systemArtifact;

    }

    public List getBundleArtifacts()
    {
        return m_defaultArtifacts;
    }

    public File getSystemBundle( BundleManager bundleManager )
        throws IllegalArgumentException, IOException,
               ParserConfigurationException, SAXException
    {
        return bundleManager.getBundle( m_systemArtifact.groupId,
                                        m_systemArtifact.artifactId, m_systemArtifact.version
        );
    }

    public List getDefaultBundles( BundleManager bundleManager )
        throws IllegalArgumentException, IOException,
               ParserConfigurationException, SAXException
    {
        ArrayList result = new ArrayList();
        for( Iterator i = m_defaultArtifacts.iterator(); i.hasNext(); )
        {
            GroupArtifactVersion artifact = (GroupArtifactVersion) i.next();
            File bundle = bundleManager.getBundle( artifact.groupId, artifact.artifactId, artifact.version );
            result.add( bundle );
        }
        return result;
    }

    private static class GroupArtifactVersion
    {
        private String groupId;
        private String artifactId;
        private String version;

        public GroupArtifactVersion( String groupId, String artifactId, String version )
        {
            super();
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String toString()
        {
            return groupId + ":" + artifactId + ":" + version;
        }

        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            GroupArtifactVersion that = (GroupArtifactVersion) o;
            
            return artifactId.equals( that.artifactId )
                   && groupId.equals( that.groupId )
                   && version.equals( that.version
            );
        }

        public int hashCode()
        {
            int result;
            result = groupId.hashCode();
            result = 31 * result + artifactId.hashCode();
            result = 31 * result + version.hashCode();
            return result;
        }
    }
}
