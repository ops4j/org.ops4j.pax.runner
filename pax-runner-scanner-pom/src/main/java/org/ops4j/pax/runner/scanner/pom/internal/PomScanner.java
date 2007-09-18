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
import org.ops4j.pax.runner.commons.Assert;
import org.ops4j.pax.runner.commons.resolver.Resolver;
import org.ops4j.pax.runner.commons.xml.XmlUtils;
import org.ops4j.pax.runner.provision.BundleReference;
import org.ops4j.pax.runner.provision.MalformedSpecificationException;
import org.ops4j.pax.runner.provision.Scanner;
import org.ops4j.pax.runner.provision.ScannerException;
import org.ops4j.pax.runner.provision.scanner.FileBundleReference;
import org.ops4j.pax.runner.provision.scanner.ScannerConfiguration;
import org.ops4j.pax.runner.provision.scanner.ScannerConfigurationImpl;
import org.ops4j.pax.runner.scanner.pom.ServiceConstants;

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
     * Resolver used to resolve properties.
     */
    private Resolver m_resolver;

    /**
     * Creates a new file scanner.
     *
     * @param resolver a resolver; mandatory
     */
    public PomScanner( final Resolver resolver )
    {
        Assert.notNull( "Resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * Reads the bundles from the pom file specified by the urlSpec.
     *
     * @param urlSpec url spec to the text file containing the bundle.
     */
    public List<BundleReference> scan( final String urlSpec )
        throws MalformedSpecificationException, ScannerException
    {
        LOGGER.debug( "Scanning [" + urlSpec + "]" );
        List<BundleReference> references = new ArrayList<BundleReference>();
        Parser parser = createParser( urlSpec );
        ScannerConfiguration config = createConfiguration();
        InputStream inputStream = null;
        try
        {
            try
            {
                inputStream = parser.getPomURL().openStream();
                final Document doc = XmlUtils.parseDoc( inputStream );
                final Integer defaultStartLevel = getDefaultStartLevel( parser, config );
                final Boolean defaultStart = getDefaultStart( parser, config );
                final String mainArtifactURL = composeURL( doc.getDocumentElement(), "packaging" );
                if( mainArtifactURL != null )
                {
                    references.add( new FileBundleReference( mainArtifactURL, defaultStartLevel, defaultStart ) );
                }
                final List<Element> dependencies = XmlUtils.getElements( doc, "dependencies/dependency" );
                if( dependencies != null )
                {
                    for( Element dependency : dependencies )
                    {
                        final String dependencyURL = composeURL( dependency, "type" );
                        if( dependencyURL != null )
                        {
                            references.add( new FileBundleReference( dependencyURL, defaultStartLevel, defaultStart ) );
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
        return references;
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
        final String artifactId = XmlUtils.getTextContent( element );
        if( artifactId == null || artifactId.trim().length() == 0 )
        {
            throw new ScannerException( "Invalid pom file. Invalid artifact id." );
        }
        element = XmlUtils.getElement( parentElement, "groupId" );
        if( element == null )
        {
            throw new ScannerException( "Invalid pom file. Missing group id." );
        }
        final String groupId = XmlUtils.getTextContent( element );
        if( groupId == null || groupId.trim().length() == 0 )
        {
            throw new ScannerException( "Invalid pom file. Invalid group id." );
        }
        element = XmlUtils.getElement( parentElement, "version" );
        String version = null;
        if( element != null )
        {
            version = XmlUtils.getTextContent( element );
        }
        if( version != null && version.trim().length() == 0 )
        {
            version = null;
        }
        element = XmlUtils.getElement( parentElement, typeElementName );
        String type = null;
        if( element != null )
        {
            type = XmlUtils.getTextContent( element );
        }
        if( type != null && ( type.trim().length() == 0 || type.trim().equalsIgnoreCase( "bundle" ) ) )
        {
            type = null;
        }
        // we only support jars
        if( type != null && !type.equalsIgnoreCase( "jar" ) )
        {
            return null;
        }
        // verify scope
        element = XmlUtils.getElement( parentElement, "scope" );
        String scope = null;
        if( element != null )
        {
            scope = XmlUtils.getTextContent( element );
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

    /**
     * Returns the default start level by first looking at the parser and if not set fallback to configuration.
     *
     * @param parser a parser
     * @param config a configuration
     *
     * @return default start level or null if nos set.
     */
    private Integer getDefaultStartLevel( Parser parser, ScannerConfiguration config )
    {
        Integer startLevel = parser.getStartLevel();
        if( startLevel == null )
        {
            startLevel = config.getStartLevel();
        }
        return startLevel;
    }

    /**
     * Returns the default start by first looking at the parser and if not set fallback to configuration.
     *
     * @param parser a parser
     * @param config a configuration
     *
     * @return default start level or null if nos set.
     */
    private Boolean getDefaultStart( final Parser parser, final ScannerConfiguration config )
    {
        Boolean start = parser.shouldStart();
        if( start == null )
        {
            start = config.shouldStart();
        }
        return start;
    }

    /**
     * Sets the resolver to use.
     *
     * @param resolver a resolver
     */
    public void setResolver( final Resolver resolver )
    {
        Assert.notNull( "Resolver", resolver );
        m_resolver = resolver;
    }

    /**
     * Creates a parser.
     *
     * @param urlSpec url spec to the text file containing the bundles.
     *
     * @return a parser
     *
     * @throws org.ops4j.pax.runner.provision.MalformedSpecificationException
     *          rethrown from parser
     */
    Parser createParser( final String urlSpec )
        throws MalformedSpecificationException
    {
        return new ParserImpl( urlSpec );
    }

    /**
     * Creates a new configuration.
     *
     * @return a configuration
     */
    ScannerConfiguration createConfiguration()
    {
        return new ScannerConfigurationImpl( m_resolver, ServiceConstants.PID );
    }

}
