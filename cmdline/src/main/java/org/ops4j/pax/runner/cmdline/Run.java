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
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.*;
import org.ops4j.pax.model.repositories.RepositoryInfo;
import org.ops4j.pax.model.repositories.RepositoryType;
import org.ops4j.pax.pomparser.PomInfo;
import org.xml.sax.SAXException;

/**
 * This class will download a Maven PomInfo run that definition inside a OSGi container.
 */
public class Run
{

    public static File WORK_DIR;

    public static void main( String[] args )
        throws Exception, ParserConfigurationException, SAXException, ServiceException
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
            System.err.println( "java -jar pixea-runner.jar [options] <groupId> <artifactId> <version>" );
            System.err.println( "  or" );
            System.err.println( "java -jar pixea-runner.jar [options] <pom-URL>" );
            System.err.println( "  or" );
            System.err.println( "java -jar pixea-runner.jar [options] <provisioning-URL> (not support yet)" );
            System.err.println( "  or" );
            System.err.println( "java -jar pixea-runner.jar [options]" );
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

        extractRepositories( cmdLine, options );
        boolean noCheckMD5 = cmdLine.isSet( "no-md5" );
        options.setNoMd5Checks( noCheckMD5 );

        String version = cmdLine.getValue( "version" );
        String group = cmdLine.getValue( "group" );
        String artifact = cmdLine.getValue( "artifact" );
        String platform = cmdLine.getValue( "platform" ).toLowerCase();
        options.setSelectedPlatform( platform );
        PomInfo pomInfo = new PomInfo( group, artifact, version );
        PaxRunner paxRunner = ServiceManager.getInstance().getService( PaxRunner.class );
        paxRunner.fillOptionsWithPomData( pomInfo, options );
        paxRunner.run( options );
        System.exit( 0 );
    }

    /**
     * Repository commandline format;
     *
     * [type]:[name]:[url]
     * maven2:Ops4JStd:http://repository.ops4j.org/maven2/
     *
     * @param cmdLine
     * @param options
     */
    private static void extractRepositories( CmdLine cmdLine, RunnerOptions options )
    {
        String repoValue = cmdLine.getValue( "repository" );
        StringTokenizer st = new StringTokenizer( repoValue, ", ", false );
        while( st.hasMoreTokens() )
        {
            String repo = st.nextToken();
            if( !repo.endsWith( "/" ) )
            {
                repo = repo + "/";
            }
            extractRepoParams( repo, options );
        }
    }

    private static void extractRepoParams( String repo, RunnerOptions options )
    {
        int pos1 = repo.indexOf( ':' );
        int pos2 = repo.indexOf( ':', pos1 + 1 );
        if( pos1 == -1 || pos2 <= pos1 )
        {
            throw new IllegalArgumentException( "Repository is specified as [type]:[name]:[url], and not '" + repo );
        }
        String type = repo.substring( 0, pos1 );
        String name = repo.substring( pos1 + 1, pos2 );
        String url = repo.substring( pos2 + 1 );
        RepositoryInfo repoInfo = new RepositoryInfo( name, url, RepositoryType.valueOf( type ) );
        options.addRepository( repoInfo );
    }
}
