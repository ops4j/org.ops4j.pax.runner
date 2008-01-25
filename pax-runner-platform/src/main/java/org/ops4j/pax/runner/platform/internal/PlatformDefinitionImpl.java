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
package org.ops4j.pax.runner.platform.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.util.xml.XmlUtils;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.BundleReferenceBean;

/**
 * Implementation of platform definition that that reads definition form an xml.
 *
 * @author Alin Dreghiciu
 * @since August 25, 2007
 */
public class PlatformDefinitionImpl
    implements PlatformDefinition
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( PlatformDefinitionImpl.class );
    /**
     * System package name.
     */
    private String m_systemPackageName;
    /**
     * System package url.
     */
    private URL m_systemPackage;
    /**
     * A comma separated list of packages.
     */
    private String m_packages;
    /**
     * Mapping between profile name and extends.
     */
    private final Map<String, String> m_profiles;
    /**
     * Mapping between profile name and bundle refreneces.
     */
    private final Map<String, List<BundleReference>> m_bundles;
    /**
     * Name of the default profile.
     */
    private String m_defaultProfile;

    /**
     * Creates a new platform definition by reading an xml from an output stream.
     *
     * @param inputStream an xml input stream
     * @param startLevel  the start level that platform bundles should be started
     *
     * @throws java.io.IOException      re-thrown while parsing the input stream as xml
     * @throws javax.xml.parsers.ParserConfigurationException
     *                                  re-thrown while parsing the input stream as xml
     * @throws org.xml.sax.SAXException re-thrown while parsing the input stream as xml
     */
    public PlatformDefinitionImpl( final InputStream inputStream, final Integer startLevel )
        throws IOException, ParserConfigurationException, SAXException
    {
        NullArgumentException.validateNotNull( inputStream, "Input stream" );
        m_profiles = new HashMap<String, String>();
        m_bundles = new HashMap<String, List<BundleReference>>();

        final Document doc = XmlUtils.parseDoc( inputStream );
        m_systemPackageName = XmlUtils.getTextContentOfElement( doc, "name" );
        final String systemPackage = XmlUtils.getTextContentOfElement( doc, "system" );
        if( systemPackage == null )
        {
            throw new IOException( "Invalid syntax: system bundle url not defined" );
        }
        m_systemPackage = new URL( systemPackage );
        if( m_systemPackageName == null )
        {
            m_systemPackageName = systemPackage;
        }
        m_packages = XmlUtils.getTextContentOfElement( doc, "packages" );
        final List<Element> profiles = XmlUtils.getElements( doc, "profile" );
        if( profiles != null )
        {
            for( Element profile : profiles )
            {
                final String profileName = profile.getAttribute( "name" );
                final Boolean profileDefault = Boolean.valueOf( profile.getAttribute( "default" ) );
                String profileExtends = profile.getAttribute( "extends" );
                if( profileExtends != null && profileExtends.trim().length() == 0 )
                {
                    profileExtends = null;
                }
                if( profileName == null )
                {
                    throw new IOException( "Invalid syntax: all profiles must have a name" );
                }
                // if there is no other default profile first one is the default one
                if( m_defaultProfile == null || profileDefault )
                {
                    m_defaultProfile = profileName;
                }
                m_profiles.put( profileName, profileExtends );
                m_bundles.put( profileName, new ArrayList<BundleReference>() );
                final List<Element> bundles = XmlUtils.getElements( profile, "bundle" );
                if( bundles != null )
                {
                    for( Element bundle : bundles )
                    {
                        String name = XmlUtils.getTextContentOfElement( bundle, "name" );
                        final String urlSpec = XmlUtils.getTextContentOfElement( bundle, "url" );
                        if( urlSpec == null )
                        {
                            throw new IOException( "Invalid syntax: bundle url not defined in profile " + profileName );
                        }
                        final URL bundleURL = new URL( urlSpec );
                        if( name == null )
                        {
                            name = urlSpec;
                        }
                        m_bundles.get( profileName )
                            .add( new BundleReferenceBean( name, bundleURL, startLevel, true, false ) );
                    }
                }
            }
        }
        else
        {
            throw new IOException( "Invalid syntax: there should be at least one profile" );

        }
    }

    /**
     * @see PlatformDefinition#getSystemPackage()
     */
    public URL getSystemPackage()
    {
        return m_systemPackage;
    }

    /**
     * @see PlatformDefinition#getSystemPackageName()
     */
    public String getSystemPackageName()
    {
        return m_systemPackageName;
    }

    /**
     * @see PlatformDefinition#getPackages()
     */
    public String getPackages()
    {
        return m_packages;
    }

    /**
     * @see PlatformDefinition#getPlatformBundles(String)
     */
    public List<BundleReference> getPlatformBundles( final String profiles )
    {
        List<BundleReference> bundles = null;
        if( profiles == null || profiles.trim().length() == 0 )
        {
            return getPlatformBundles( m_defaultProfile );
        }
        final String[] segments = profiles.split( "," );
        for( String segment : segments )
        {
            if( m_profiles.containsKey( segment ) )
            {
                if( bundles == null )
                {
                    bundles = new ArrayList<BundleReference>();
                }
                final String extended = m_profiles.get( segment );
                if( extended != null )
                {
                    final List<BundleReference> references = getPlatformBundles( extended );
                    // eliminate duplicates
                    if( references != null && references.size() > 0 )
                    {
                        for( BundleReference reference : references )
                        {
                            if( !bundles.contains( reference ) )
                            {
                                bundles.add( reference );
                            }
                        }
                    }
                }
                final List<BundleReference> references = m_bundles.get( segment );
                // eliminate duplicates
                if( references != null && references.size() > 0 )
                {
                    for( BundleReference reference : references )
                    {
                        if( !bundles.contains( reference ) )
                        {
                            bundles.add( reference );
                        }
                    }
                }
            }
            else
            {
                // if an invalid profile just isse an warnning
                LOGGER.warn( "Invalid profile [" + segment + "]. Skipping." );
            }
        }
        // if no success with profiles and this was not a call for default profile then look at default profile
        if( bundles == null && !m_defaultProfile.equals( profiles ) )
        {
            bundles = getPlatformBundles( m_defaultProfile );
        }
        return bundles;
    }

}
