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
import org.ops4j.pax.runner.provisioning.Provisioning;
import org.ops4j.pax.runner.pom.PomManager;
import org.ops4j.pax.runner.pom.BundleManager;
import org.xml.sax.SAXException;

/**
 * This class will download a Maven POM run that definition inside a OSGi container.
 */
public class Run
{
    public static File WORK_DIR;

    private static CmdLine m_cmdLine;

    public static void main( String[] args )
        throws IOException, ParserConfigurationException, SAXException
    {
        try
        {
            m_cmdLine = new CmdLine( args );
        } catch( IllegalArgumentException e )
        {
            String message = e.getMessage();
            if( message != null )
            {
                System.err.println( message );
            }
            System.err.println();
            System.err.println( "java -jar pax-runner.jar [options] <groupId> <artifactId> <version>" );
            System.err.println( "  or" );
            System.err.println( "java -jar pax-runner.jar [options] <pom-URL>" );
            System.err.println( "  or" );
            System.err.println( "java -jar pax-runner.jar [options] <provisioning-URL> (not support yet)" );
            System.err.println( "  or" );
            System.err.println( "java -jar pax-runner.jar [options]" );
            System.err.println( "\nOptions;" );
            System.err.println( "--platform=<platform>  -  The OSGi platform to use. Default: equinox" );
            System.err.println( "--clean                -  Do not load persisted state." );
            System.err.println( "--gui                  -  Load GUI (if supported by platform)" );
            System.err.println( "--no-md5               -  Disable MD5 checksum checks for downloads." );
            System.err.println( "--dir=<workdir>        -  Which directory to use. Default: runner/" );
            System.err.println( "--profile=<profile>    -  Which profile to run (if supported by platform)" );
            System.err.println( "--repository=<repo>    -  Which repository to download from." );
            System.err.println( "--proxy-username=<pwd> -  Username for the proxy." );
            System.err.println( "--proxy-password=<pwd> -  Username for the proxy." );
            System.err.println( "--repository-username=<pwd> -  Username for the repository server." );
            System.err.println( "--repository-password=<pwd> -  Username for the repository server." );
            System.err.println( "--help                 -  This message." );
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

        String workDir = m_cmdLine.getValue( "dir" );
        WORK_DIR = new File( workDir );
        WORK_DIR.mkdirs();
        System.out.println( "Working Dir: " + WORK_DIR );

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
        boolean noCheckMD5 = m_cmdLine.isSet( "no-md5" );
        Downloader downloader = new Downloader( repo, noCheckMD5 );
        List<File> bundles;
        Properties props;
        String urlValue = m_cmdLine.getValue( "url" );
        boolean useProvisioning = urlValue != null && urlValue.endsWith( ".zip" );
        if( useProvisioning )
        {
            Provisioning provisioning = new Provisioning( downloader );
            bundles = provisioning.getBundles( m_cmdLine );
            props = provisioning.getProperties( m_cmdLine );
        }
        else
        {
            PomManager pomManager = new PomManager( downloader );
            bundles = pomManager.getBundles( m_cmdLine );
            props = pomManager.getProperties( m_cmdLine );
        }
        BundleManager bundleManager = new BundleManager( downloader );
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
