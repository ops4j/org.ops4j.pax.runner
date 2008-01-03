/*
 * Copyright 2007 Alin Dreghiciu.
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
package org.ops4j.pax.runner.handler.mvn.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.url.URLUtils;
import org.ops4j.pax.runner.commons.xml.XmlUtils;

/**
 * An URLConnextion that supports mvn: protocol.<br/>
 * Syntax:<br>
 * mvn:[repository_url!]groupId/artifactId[/version[/type]]<br/>
 * where:<br/>
 * - repository_url = an url that points to a maven 2 repository; optional, if not sepecified the repositories are
 * resolved based on the repository/localRepository.<br/>
 * - groupId = group id of maven artifact; mandatory<br/>
 * - artifactId = artifact id of maven artifact; mandatory<br/>
 * - version = version of maven artifact; optional, if not specified uses LATEST and will try to resolve the version
 * from available maven metadata. If version is a SNAPSHOT version, SNAPSHOT will be resolved from available maven
 * metadata<br/>
 * - type = type of maven artifact; optional, if not specified uses JAR<br/>
 * Examples:<br>
 * mvn:http://repository.ops4j.org/maven2!org.ops4j.pax.runner/runner/0.4.0 - an artifact from an http repository<br/>
 * mvn:http://user:password@repository.ops4j.org/maven2!org.ops4j.pax.runner/runner/0.4.0 - an artifact from an http
 * repository with authentication<br/>
 * mvn:file://c:/localRepo!org.ops4j.pax.runner/runner/0.4.0 - an artifact from a directory<br/>
 * mvn:jar:file://c:/repo.zip!/repository!org.ops4j.pax.runner/runner/0.4.0 - an artifact from a zip file<br/>
 * mvn:org.ops4j.pax.runner/runner/0.4.0 - an artifact that will be resolved based on the configured repositories<br/>
 * <br/>
 * The service can be configured in two ways: via configuration admin if available and via framework/system properties
 * where the configuration via config admin has priority.<br/>
 * Service configuration:<br/>
 * - org.ops4j.pax.runner.handler.mvn.settings = the path to settings.xml;<br/>
 * - org.ops4j.pax.runner.handler.mvn.localRepository = the path to local repository directory;<br>
 * - org.ops4j.pax.runner.handler.mvn.repository =  a comma separated list for repositories urls;<br/>
 * - org.ops4j.pax.runner.handler.mvn.certicateCheck = true/false if the SSL certificate check should be done.
 * Default false.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.mvn.internal.Handler
 * @since August 10, 2007
 */
public class Connection
    extends URLConnection
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( Connection.class );

    /**
     * Parsed url.
     */
    private Parser m_parser;
    /**
     * Service configuration.
     */
    private final Configuration m_configuration;

    /**
     * Creates a new connection.
     *
     * @param url           the url; cannot be null.
     * @param configuration service configuration; cannot be null
     *
     * @throws MalformedURLException in case of a malformed url
     */
    public Connection( final URL url, final Configuration configuration )
        throws MalformedURLException
    {
        super( url );
        Assert.notNull( "URL cannot be null", url );
        Assert.notNull( "Service configuration", configuration );
        m_configuration = configuration;
        m_parser = new Parser( url.getPath() );
    }

    /**
     * Does nothing.
     *
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect()
    {
        // do nothing
    }

    /**
     * Returns the input stream denoted by the url.<br/>
     * If the url does not contain a repository the resource is searched in every repository if available, in the order
     * provided by the repository setting.
     *
     * @return the input stream for the resource denoted by url
     *
     * @throws IOException in case of an exception during accessing the resource
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream()
        throws IOException
    {
        connect();
        List<URL> repositories = m_configuration.getRepositories();
        // if url does not have a repository
        if( m_parser.getRepositoryURL() == null && repositories != null )
        {
            for( URL repositoryURL : repositories )
            {
                try
                {
                    return resolveFromRepository( repositoryURL );
                }
                catch( IOException ignore )
                {
                    // go on with next repository
                    LOGGER.debug( "Could not locate in [" + repositoryURL + "]" );
                    LOGGER.trace( "Reason: [" + ignore.getMessage() + "]" );
                }
            }
            // no artifact found 
            throw new RuntimeException( "URL [" + url.toExternalForm() + "] could not be resolved." );
        }
        // if url has a repository
        return resolveFromRepository( m_parser.getRepositoryURL() );
    }

    /**
     * Resolves the artifact by resolving an eventual latest or snapshot version.
     *
     * @param repositoryURL the url of the repository to download from.
     *
     * @return an input stream to the artifact
     *
     * @throws IOException if the artifact could not be resolved
     */
    private InputStream resolveFromRepository( final URL repositoryURL )
        throws IOException
    {
        if( m_parser.getVersion().contains( "LATEST" ) )
        {
            return resolveLatestVersion( repositoryURL );
        }
        else if( m_parser.getVersion().endsWith( "SNAPSHOT" ) )
        {
            return resolveSnapshotVersion( repositoryURL, m_parser.getVersion() );
        }
        else
        {
            return prepareInputStream( repositoryURL, m_parser.getArtifactPath() );
        }
    }

    /**
     * Resolves the latest version of the artifact. First try to get the local metadata and then remote metadata.
     *
     * @param repositoryURL the url of the repository to download from.
     *
     * @return an input stream to the artifact
     *
     * @throws IOException if the artifact could not be resolved
     */
    private InputStream resolveLatestVersion( final URL repositoryURL )
        throws IOException
    {
        InputStream inputStream;
        try
        {
            // first try to get the artifact local metadata
            inputStream = prepareInputStream( repositoryURL, m_parser.getArtifactLocalMetdataPath() );
        }
        catch( IOException ignore )
        {
            // if not found then try to get the artifact metadata
            inputStream = prepareInputStream( repositoryURL, m_parser.getArtifactMetdataPath() );
        }
        try
        {
            final Document doc = XmlUtils.parseDoc( inputStream );
            final String version = XmlUtils.getTextContentOfElement( doc, "versioning/versions/version[last]" );
            if( version != null )
            {
                if( version.endsWith( "SNAPSHOT" ) )
                {
                    return resolveSnapshotVersion( repositoryURL, version );
                }
                else
                {
                    return prepareInputStream( repositoryURL, m_parser.getArtifactPath( version ) );
                }
            }
        }
        catch( ParserConfigurationException e )
        {
            throw initIOException( "Maven metadata [" + url.toExternalForm() + "] could not be parsed.", e );
        }
        catch( SAXException e )
        {
            throw initIOException( "Maven metadata [" + url.toExternalForm() + "] could not be parsed.", e );
        }
        throw new IOException( "LATEST version could not be resolved." );
    }

    /**
     * Resolves snapshot version of the artifact by trying first to get the artifact as is (with SNAPSHOT in name) and
     * then by reading the actual version from maven-metadata.xml. Accesing first directly is suitable for a local
     * repository but could happen to have this also in a remote or custom repository as for example a zipped local
     * repository.
     *
     * @param repositoryURL the url of the repository to download from.
     * @param version       snapshot version to resolve
     *
     * @return an input stream to the artifact
     *
     * @throws IOException if the artifact could not be resolved
     */
    private InputStream resolveSnapshotVersion( final URL repositoryURL, final String version )
        throws IOException
    {
        // first try direct access
        try
        {
            return prepareInputStream( repositoryURL, m_parser.getArtifactPath( version ) );
        }
        catch( IOException ignore )
        {
            // lets try then to download maven-metadata.xml that contains the snapshot version information
            Document doc;
            final String metadataPath = m_parser.getVersionMetadataPath( version );
            try
            {
                doc = XmlUtils.parseDoc(
                    prepareInputStream( repositoryURL, metadataPath )
                );
            }
            catch( ParserConfigurationException e )
            {
                throw initIOException( "Maven metadata [" + metadataPath + "] from repository [" + repositoryURL
                                       + "] could not be parsed.", e
                );
            }
            catch( SAXException e )
            {
                throw initIOException( "Maven metadata [" + metadataPath + "] from repository [" + repositoryURL
                                       + "] could not be parsed.", e
                );
            }
            catch( IOException e )
            {
                throw initIOException( "Maven metadata [" + metadataPath + "] from repository [" + repositoryURL
                                       + "] could not be downloaded.", e
                );
            }
            String timestamp = XmlUtils.getTextContentOfElement( doc, "versioning/snapshot/timestamp" );
            String buildNumber = XmlUtils.getTextContentOfElement( doc, "versioning/snapshot/buildNumber" );
            if( timestamp != null && buildNumber != null )
            {
                return prepareInputStream( repositoryURL, m_parser.getSnapshotPath( version, timestamp, buildNumber ) );
            }
            throw new IOException( "SNAPSHOT version could not be resolved." );
        }
    }

    /**
     * @param repositoryURL url to reporsitory
     * @param path          a path to the artifact jar file
     *
     * @return prepared input stream
     *
     * @throws IOException re-thrown
     * @see org.ops4j.pax.runner.commons.url.URLUtils#prepareInputStream(java.net.URL,boolean)
     */
    private InputStream prepareInputStream( URL repositoryURL, final String path )
        throws IOException
    {
        String repository = repositoryURL.toExternalForm();
        if( !repository.endsWith( Parser.FILE_SEPARATOR ) )
        {
            repository = repository + Parser.FILE_SEPARATOR;
        }
        return URLUtils.prepareInputStream( new URL( repository + path ), !m_configuration.getCertificateCheck() );
    }

    /**
     * Creates an IOException with a message and a cause.
     *
     * @param message exception message
     * @param cause   exception cause
     *
     * @return the created IO Exception
     */
    private IOException initIOException( final String message, final Exception cause )
    {
        IOException exception = new IOException( message );
        exception.initCause( cause );
        return exception;
    }

}
