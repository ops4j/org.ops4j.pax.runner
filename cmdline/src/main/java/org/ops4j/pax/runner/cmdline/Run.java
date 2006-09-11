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
package org.ops4j.pax.runner.cmdline;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.state.Bundle;
import org.ops4j.pax.runner.exec.EquinoxRunner;
import org.ops4j.pax.runner.exec.FelixRunner;
import org.ops4j.pax.runner.exec.KnopflerfishRunner;
import org.ops4j.pax.runner.RunnerOptionsImpl;
import org.ops4j.pax.runner.PomInfo;
import org.ops4j.pax.runner.repositories.Repository;
import org.ops4j.pax.runner.internal.RunnerOptions;
import org.ops4j.pax.runner.maven2.BundleManager;
import org.ops4j.pax.runner.maven2.PomManagerImpl;
import org.xml.sax.SAXException;

/**
 * This class will download a Maven PomInfo run that definition inside a OSGi container.
 */
public class Run
{
    public static File WORK_DIR;

    public static void main( String[] args )
        throws IOException, ParserConfigurationException, SAXException
    {
        CmdLine cmdLine = null;
        try
        {
            cmdLine = new CmdLine( args );
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
            System.err.println( "--repository=<repositories> - Which repositories to download from. Comma-separated." );
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

        RunnerOptions options = new RunnerOptionsImpl();
        String workDir = cmdLine.getValue( "dir" );
        options.setWorkDir( new File( workDir ) );
        System.out.println( "Working Dir: " + workDir );

        final CmdLine cmdLine2 = cmdLine;
        Authenticator auth = new Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                if( getRequestorType() == RequestorType.PROXY )
                {
                    String userName = cmdLine2.getValue( "proxy-username" );
                    char[] password = cmdLine2.getValue( "proxy-password" ).toCharArray();
                    return new PasswordAuthentication( userName, password );
                }
                if( getRequestorType() == RequestorType.SERVER )
                {
                    String userName = cmdLine2.getValue( "repository-username" );
                    char[] password = cmdLine2.getValue( "repository-password" ).toCharArray();
                    return new PasswordAuthentication( userName, password );
                }
                return null;
            }
        };
        Authenticator.setDefault( auth );

        String[] repositories = extractRepositories( cmdLine );
        boolean noCheckMD5 = cmdLine.isSet( "no-md5" );
        options.setNoMd5Checks( noCheckMD5 );

        List<Bundle> bundles;
        Properties props;
        String urlValue = cmdLine.getValue( "url" );

        String version = cmdLine.getValue( "version" );
        String group = cmdLine.getValue( "group" );
        String artifact = cmdLine.getValue( "artifact" );
        PomInfo pomInfo = new PomInfo( artifact, group, version );
        URL baseUrl = new URL( cmdLine.getValue( "repository" ) );
        PomManagerImpl pomManager = PomManagerImpl.getInstance( baseUrl, options );
        bundles = pomManager.getBundles( pomInfo );
        props = pomManager.getProperties( pomInfo );

        Repository repository = null;
        BundleManager bundleManager = new BundleManager( repository, options );
        String platform = cmdLine.getValue( "platform" ).toLowerCase();
        System.out.println( "\n   Platform: " + platform );
        if( "equinox".equals( platform ) )
        {
            Runnable wrapper = new EquinoxRunner( options, props, bundles, bundleManager );
            wrapper.run();
        }
        else if( "felix".equals( platform ) )
        {
            Runnable wrapper = new FelixRunner( options, props, bundles, bundleManager );
            wrapper.run();
        }
        else if( "knopflerfish".equals( platform ) )
        {
            Runnable wrapper = new KnopflerfishRunner( options, props, bundles, bundleManager );
            wrapper.run();
        }
        else
        {
            System.err.println( "Platform '" + platform + "' is currently not supported." );
            System.exit( 2 );
        }
        System.exit(0);
    }

    private static String[] extractRepositories( CmdLine cmdLine )
    {
        String repoValue = cmdLine.getValue( "repository" );
        StringTokenizer st = new StringTokenizer( repoValue, ", ", false );
        ArrayList repos = new ArrayList();
        while( st.hasMoreTokens() )
        {
            String repo = st.nextToken();
            if( ! repo.endsWith( "/" ) )
            {
                repo = repo + "/";
            }
            repos.add( repo );
        }
        String[] result = new String[ repos.size() ];
        repos.toArray( result );
        return result;
    }
}