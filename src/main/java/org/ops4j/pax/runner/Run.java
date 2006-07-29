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

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class will download a Maven POM run that definition inside a OSGi container.
 */
public class Run
{

    private static CmdLine m_cmdLine;

    public static void main( String[] args )
        throws IOException, ParserConfigurationException, SAXException
    {
        try
        {
            m_cmdLine = new CmdLine( args );
        } catch( IllegalArgumentException e )
        {
            System.err.println( e.getMessage() );
            System.err.println();
            System.err.println( "java -jar pax-runner.jar [options] <groupId> <artifactId> <version>" );
            System.err.println( "  or" );
            System.err.println( "java -jar pax-runner.jar [options] <URL>" );
            System.err.println( "  or" );
            System.err.println( "java -jar pax-runner.jar [options]" );
            System.err.println( "\nOptions;" );
            System.err.println( "--platform=<platform>  -  The OSGi platform to use. Default: equinox" );
            System.err.println( "--clean                -  Do not load persisted state." );
            System.err.println( "--gui                  -  Load GUI (if supported by platform)" );
            System.err.println( "--profile              -  Which profile to run (if supported by platform)" );
            System.err.println( "--repository=<repo>    -  Which repository to download from." );
            System.err.println( "--proxy-username=<pwd> -  Username for the proxy." );
            System.err.println( "--proxy-password=<pwd> -  Username for the proxy." );
            System.err.println( "--repository-username=<pwd> -  Username for the repository server." );
            System.err.println( "--repository-password=<pwd> -  Username for the repository server." );
            System.exit( 1 );
        }

        System.out.println( "    ______  ________  __  __" );
        System.out.println( "   / __  / /  __   / / / / /" );
        System.out.println( "  /  ___/ /  __   / _\\ \\ _/" );
        System.out.println( " /  /    /  / /  / / _\\ \\" );
        System.out.println( "/__/    /__/ /__/ /_/ /_/" );
        System.out.println();
        System.out.println( "Pax Runner from OPS4J - http://www.ops4j.org" );
        System.out.println( "--------------------------------------------" );
        System.out.println();

        System.out.println( "Current Dir: " + System.getProperty( "user.dir" ) );

        Authenticator auth = new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if( getRequestorType() == Authenticator.RequestorType.PROXY )
                {
                    String userName = m_cmdLine.getValue( "proxy-username" );
                    char[] password = m_cmdLine.getValue( "proxy-password" ).toCharArray();
                    return new PasswordAuthentication( userName, password );
                }
                if( getRequestorType() == Authenticator.RequestorType.SERVER )
                {
                    String userName = m_cmdLine.getValue( "repository-username" );
                    char[] password = m_cmdLine.getValue( "repository-password" ).toCharArray();
                    return new PasswordAuthentication( userName, password );
                }
                return null;
            }
        };
        Authenticator.setDefault( auth );
        
        String repo = m_cmdLine.getValue( "repository" );
        if( ! repo.endsWith( "/" ) )
        {
            repo = repo + "/";
        }
        Downloader downloader = new Downloader( repo );
        PomManager pomManager = new PomManager( downloader );

        Document pom = pomManager.retrievePom( m_cmdLine );
        pomManager.info( pom );
        Properties props = DomUtils.parseProperties( pom );
        Element dependencies = DomUtils.getElement( pom, "dependencies" );
        BundleManager bundleManager = new BundleManager( downloader );
        List<File> bundles = bundleManager.getBundles( dependencies );
        String platform = m_cmdLine.getValue( "platform" ).toLowerCase();
        System.out.println( "\n   Platform: " + platform );
        if( "equinox".equals( platform ) )
        {
            Runnable wrapper = new EquinoxRunner( m_cmdLine, props, bundles, bundleManager );
            wrapper.run();
        }
        else if( "felix".equals( platform ) )
        {
            Runnable wrapper = new FelixRunner( m_cmdLine, props, bundles, bundleManager );
            wrapper.run();
        }
        else if( "knopflerfish".equals( platform ) )
        {
            Runnable wrapper = new KnopflerfishRunner( m_cmdLine, props, bundles, bundleManager );
            wrapper.run();
        }
        else
        {
            System.err.println( "Platform '" + platform + "' is currently not supported." );
            System.exit( 2 );
        }
        System.exit(0);
    }
}
