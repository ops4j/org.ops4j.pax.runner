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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.commons.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Default implementation of Settings.
 *
 * @author Alin Dreghiciu
 * @see org.ops4j.pax.runner.handler.mvn.internal.Settings
 * @since August 10, 2007
 */
public class SettingsImpl
    implements Settings
{

    /**
     * Path of local repository tag.
     */
    private static final String LOCAL_REPOSITORY_TAG = "localRepository";
    /**
     * Path to server tag.
     */
    private static final String SERVER_TAG = "servers/server";
    /**
     * Path to repository tag.
     */
    private static final String REPOSITORY_TAG = "repositories/repository";
    /**
     * Maven Central repository.
     */
    private static final String CENTRAL_REPOSITORY = "http://repo1.maven.org/maven2";

    /**
     * The settings.xml DOM Document. Null if there is no settings.xml.
     */
    private Document m_document;
    /**
     * The settings.xml file url. Can be null if no settings.xml was resolved.
     */
    private URL m_settingsURL;
    /**
     * The local repository spec.
     */
    private String m_localRepository;
    /**
     * Comma separated list of repositories. Null if there is no settings xml or settings.xml does not contain
     * repositories.
     */
    private String m_repositories;

    /**
     * Creates new settings with the following resolution:<br/>
     * 1. looks for the specified url
     * 2. if not found looks for ${user.home}/.m2/settings.xml
     * 3. if not found looks for ${maven.home}/conf/settings.xml
     * 4. if not found looks for ${M2_HOME}/conf/settings.xml
     *
     * @param settingsURL prefered settings.xml file
     */
    public SettingsImpl( final URL settingsURL )
    {
        m_settingsURL = settingsURL;
        if ( m_settingsURL == null )
        {
            m_settingsURL = safeGetFile( System.getProperty( "user.home" ) + "/.m2/settings.xml" );
            if ( m_settingsURL == null )
            {
                m_settingsURL = safeGetFile( System.getProperty( "maven.home" ) + "/conf/settings.xml" );
                if ( m_settingsURL == null )
                {
                    try
                    {
                        m_settingsURL = safeGetFile( System.getenv( "M2_HOME" ) + "/conf/settings.xml" );
                    }
                    catch ( Error e )
                    {
                        // ignore error - probably running on Java 1.4.x
                    }
                }
            }
        }
    }

    /**
     * Returns the local repository directory from settings.xml. If there is no settings.xml file it will return the
     * hardcoded standard location: ${user.home}/.m2/repository
     *
     * @return the local repository directory
     */
    public String getLocalRepository()
    {
        if ( m_localRepository == null )
        {
            readSettings();
            if ( m_document != null )
            {
                Element settingsElement = XmlUtils.getElement( m_document, LOCAL_REPOSITORY_TAG );
                if ( settingsElement != null )
                {
                    m_localRepository = XmlUtils.getTextContent( settingsElement );
                }
            }
            if ( m_localRepository == null )
            {
                m_localRepository = System.getProperty( "user.home" ) + "/.m2/repository";
            }
        }
        return m_localRepository;
    }

    /**
     * Gets the list of repositories from settings.xml including the central repository.
     * If there is no settings.xml file or there are no repositories in settings.xml the list returned will include
     * only the central repository..
     * If there are repositories in settings.xml and those repositories have user and password the user and password
     * will be included in the repository url as for example http://user:password@repository.ops4j.org/maven2.
     *
     * @return a comma separated list of repositories from settings.xml
     */
    public String getRepositories()
    {
        if ( m_repositories == null )
        {
            readSettings();
            if ( m_document != null )
            {
                Map<String, String> repositories = null;
                List<String> order = null;
                List<Element> repos = XmlUtils.getElements( m_document, REPOSITORY_TAG );
                // first look for repositories
                if ( repos != null )
                {
                    for ( Element repo : repos )
                    {
                        Element element = XmlUtils.getElement( repo, "id" );
                        if ( element != null )
                        {
                            String id = XmlUtils.getTextContent( element );
                            if ( id != null )
                            {
                                element = XmlUtils.getElement( repo, "layout" );
                                String layout = null;
                                if ( element != null )
                                {
                                    layout = XmlUtils.getTextContent( element );
                                }
                                // take only repositories with a default layout (skip legacy ones)
                                if ( layout == null || "default".equals( layout ) )
                                {
                                    element = XmlUtils.getElement( repo, "url" );
                                    if ( element != null )
                                    {
                                        String url = XmlUtils.getTextContent( element );
                                        if ( url != null )
                                        {
                                            if ( repositories == null )
                                            {
                                                repositories = new HashMap<String, String>();
                                                order = new ArrayList<String>();
                                            }
                                            repositories.put( id, url );
                                            order.add( id );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // then look for user / passwords but only if we have repositories
                if ( repositories != null )
                {
                    List<Element> servers = XmlUtils.getElements( m_document, SERVER_TAG );
                    if ( servers != null )
                    {
                        for ( Element server : servers )
                        {
                            Element element = XmlUtils.getElement( server, "id" );
                            if ( element != null )
                            {
                                String id = XmlUtils.getTextContent( element );
                                // if we do not find a corresponding repository don't go furter
                                String repository = repositories.get( id );
                                if ( repository != null && repository.contains( "://" ) )
                                {
                                    element = XmlUtils.getElement( server, "username" );
                                    if ( element != null )
                                    {
                                        String username = XmlUtils.getTextContent( element );
                                        // if there is no username stop the search
                                        if ( username != null )
                                        {
                                            element = XmlUtils.getElement( server, "password" );
                                            if ( element != null )
                                            {
                                                String password = XmlUtils.getTextContent( element );
                                                if ( password != null )
                                                {
                                                    username = username + ":" + password;
                                                }
                                            }
                                            repositories.put( id,
                                                              repository.replaceFirst( "://", "://" + username + "@" )
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // build the list of repositories
                    final StringBuilder builder = new StringBuilder();
                    for ( String repositoryId : order )
                    {
                        if ( builder.length() > 0 )
                        {
                            builder.append( "," );
                        }
                        builder.append( repositories.get( repositoryId ) );
                    }
                    m_repositories = builder.toString();
                }
            }
            if ( m_repositories == null )
            {
                m_repositories = CENTRAL_REPOSITORY;
            }
            else
            {
                m_repositories = m_repositories + "," + CENTRAL_REPOSITORY;
            }
        }
        return m_repositories;
    }

    /**
     * Reads the settings.xml file. All exceptions raised during acces or parsing are rethrown as RuntimeException.
     */
    private void readSettings()
    {
        if ( m_document == null && m_settingsURL != null )
        {
            try
            {
                m_document = XmlUtils.parseDoc( m_settingsURL.openStream() );
            }
            catch ( ParserConfigurationException e )
            {
                throw new RuntimeException( "Could not parse settings [" + m_settingsURL + "]", e );
            }
            catch ( SAXException e )
            {
                throw new RuntimeException( "Could not parse settings [" + m_settingsURL + "]", e );
            }
            catch ( IOException e )
            {
                throw new RuntimeException( "Could not parse settings [" + m_settingsURL + "]", e );
            }
        }

    }

    /**
     * Looks for the file denoted by file path and returns the corresponding file if the file exists, is a file and can
     * be read. Otherwise returns null.
     *
     * @param filePath the path to the file
     *
     * @return the file denoted by the file path if can be read or null otherwise
     */
    private static URL safeGetFile( final String filePath )
    {
        if ( filePath != null )
        {
            File file = new File( filePath );
            if ( file.exists() && file.canRead() && file.isFile() )
            {
                try
                {
                    return file.toURL();
                }
                catch ( MalformedURLException e )
                {
                    // do nothing
                }
            }
        }
        return null;
    }

}
