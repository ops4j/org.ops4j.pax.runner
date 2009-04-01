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
package org.ops4j.pax.runner.scanner.pom.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.net.URLUtils;
import org.ops4j.pax.runner.commons.properties.SystemPropertyUtils;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.ProvisionSpec;
import org.ops4j.pax.runner.provision.ScannedBundle;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.ScannedFileBundle;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.pom.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;
import org.ops4j.util.xml.XmlUtils;

/**
 * A scanner that scans maven 2 pom files.
 *
 * @author Alin Dreghiciu
 * @since September 17, 2007
 */
public class PomScanner
    implements Scanner
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( PomScanner.class );
    /**
     * PropertyResolver used to resolve properties.
     */
    private PropertyResolver m_propertyResolver;

    /**
     * Creates a new file scanner.
     *
     * @param propertyResolver a propertyResolver; mandatory
     */
    public PomScanner( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
    }

    /**
     * Reads the bundles from the pom file specified by the urlSpec.
     * {@inheritDoc}
     */
    public List<ScannedBundle> scan( final ProvisionSpec provisionSpec )
        throws MalformedSpecificationException, ScannerException
    {
        NullArgumentException.validateNotNull( provisionSpec, "Provision spec" );

        LOGGER.debug( "Scanning [" + provisionSpec.getPath() + "]" );
        List<ScannedBundle> scannedBundles = new ArrayList<ScannedBundle>();
        ScannerConfiguration config = createConfiguration();
        InputStream inputStream = null;
        try
        {
            try
            {
                inputStream = URLUtils.prepareInputStream(
                    provisionSpec.getPathAsUrl(),
                    !config.getCertificateCheck()
                );
                final Document doc = XmlUtils.parseDoc( inputStream );
                final Integer defaultStartLevel = getDefaultStartLevel( provisionSpec, config );
                final Boolean defaultStart = getDefaultStart( provisionSpec, config );
                final Boolean defaultUpdate = getDefaultUpdate( provisionSpec, config );
                final String mainArtifactURL = composeURL( doc.getDocumentElement(), "packaging" );
                if( mainArtifactURL != null )
                {
                    scannedBundles.add(
                        new ScannedFileBundle( mainArtifactURL, defaultStartLevel, defaultStart, defaultUpdate )
                    );
                }
                // check out properties before processing dependencies
                final Element properties = XmlUtils.getElement( doc, "properties" );
                if( properties != null )
                {
                    List<Element> props = XmlUtils.getChildElements( properties );
                    for( Element property : props )
                    {
                        final String key = property.getNodeName();
                        final String value = getTextContent( property );
                        System.setProperty( key, value );
                    }
                }
                // check out dependencies
                final List<Element> dependencies = XmlUtils.getElements( doc, "dependencies/dependency" );
                if( dependencies != null )
                {
                    for( Element dependency : dependencies )
                    {
                        final String dependencyURL = composeURL( dependency, "type" );
                        if( dependencyURL != null )
                        {
                            final ScannedFileBundle scannedFileBundle = new ScannedFileBundle(
                                dependencyURL, defaultStartLevel, defaultStart, defaultUpdate
                            );
                            scannedBundles.add( scannedFileBundle );
                            LOGGER.debug( "Installing bundle [" + scannedFileBundle + "]" );
                        }
                    }
                }
            }
            finally
            {
                if( inputStream != null )
                {
                    inputStream.close();
                }
            }

        }
        catch( IOException e )
        {
            throw new ScannerException( "Could not parse the provision file", e );
        }
        catch( ParserConfigurationException e )
        {
            throw new ScannerException( "Could not parse the provision file", e );
        }
        catch( SAXException e )
        {
            throw new ScannerException( "Could not parse the provision file", e );
        }
        return scannedBundles;
    }

    /**
     * Retruns a maven url based on an element that contains group/artifact/version/type.
     *
     * @param parentElement   the element that contains the group/artifact/version/type
     * @param typeElementName name of the type element to be used
     *
     * @return a maven url
     *
     * @throws org.ops4j.pax.runner.provision.ScannerException
     *          if the element does not contain an artifact or group id
     */
    private String composeURL( Element parentElement, String typeElementName )
        throws ScannerException
    {
        Element element = XmlUtils.getElement( parentElement, "artifactId" );
        if( element == null )
        {
            throw new ScannerException( "Invalid pom file. Missing artifact id." );
        }
        final String artifactId = getTextContent( element );
        if( artifactId == null || artifactId.trim().length() == 0 )
        {
            throw new ScannerException( "Invalid pom file. Invalid artifact id." );
        }
        element = XmlUtils.getElement( parentElement, "groupId" );
        if( element == null )
        {
            throw new ScannerException( "Invalid pom file. Missing group id." );
        }
        final String groupId = getTextContent( element );
        if( groupId == null || groupId.trim().length() == 0 )
        {
            throw new ScannerException( "Invalid pom file. Invalid group id." );
        }
        element = XmlUtils.getElement( parentElement, "version" );
        String version = null;
        if( element != null )
        {
            version = getTextContent( element );
        }
        if( version != null && version.trim().length() == 0 )
        {
            version = null;
        }
        element = XmlUtils.getElement( parentElement, typeElementName );
        String type = null;
        if( element != null )
        {
            type = getTextContent( element );
        }
        if( type != null && ( type.trim().length() == 0 || type.trim().equalsIgnoreCase( "bundle" ) ) )
        {
            type = null;
        }
        // deploy any artifact type (jar, war, etc.) except pom
        if( type != null && type.equalsIgnoreCase( "pom" ) )
        {
            return null;
        }
        // verify scope
        element = XmlUtils.getElement( parentElement, "scope" );
        String scope = null;
        if( element != null )
        {
            scope = getTextContent( element );
        }
        // skip artifacts with test scopes
        if( scope != null && scope.equalsIgnoreCase( "test" ) )
        {
            return null;
        }
        final StringBuilder builder = new StringBuilder()
            .append( "mvn:" )
            .append( groupId )
            .append( "/" )
            .append( artifactId );
        if( version != null )
        {
            builder.append( "/" ).append( version );
            if( type != null )
            {
                builder.append( "/" ).append( type );
            }
        }
        return builder.toString();
    }

    private String getTextContent( Element element )
    {
        String text = XmlUtils.getTextContent( element );
        if( text != null )
        {
            text = SystemPropertyUtils.resolvePlaceholders( text );
            text = text.trim();
        }
        return text;
    }

    /**
     * Returns the default start level by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec provisioning spec
     * @param config        a configuration
     *
     * @return default start level or null if not set.
     */
    private Integer getDefaultStartLevel( ProvisionSpec provisionSpec, ScannerConfiguration config )
    {
        Integer startLevel = provisionSpec.getStartLevel();
        if( startLevel == null )
        {
            startLevel = config.getStartLevel();
        }
        return startLevel;
    }

    /**
     * Returns the default start by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec provisioning spec
     * @param config        a configuration
     *
     * @return default start or null if not set.
     */
    private Boolean getDefaultStart( final ProvisionSpec provisionSpec, final ScannerConfiguration config )
    {
        Boolean start = provisionSpec.shouldStart();
        if( start == null )
        {
            start = config.shouldStart();
        }
        return start;
    }

    /**
     * Returns the default update by first looking at the parser and if not set fallback to configuration.
     *
     * @param provisionSpec provisioning spec
     * @param config        a configuration
     *
     * @return default update or null if not set.
     */
    private Boolean getDefaultUpdate( final ProvisionSpec provisionSpec, final ScannerConfiguration config )
    {
        Boolean update = provisionSpec.shouldUpdate();
        if( update == null )
        {
            update = config.shouldUpdate();
        }
        return update;
    }

    /**
     * Sets the propertyResolver to use.
     *
     * @param propertyResolver a propertyResolver
     */
    public void setResolver( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull( propertyResolver, "PropertyResolver" );
        m_propertyResolver = propertyResolver;
    }

    /**
     * Creates a new configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_propertyResolver, ServiceConstants.PID );
    }

}
