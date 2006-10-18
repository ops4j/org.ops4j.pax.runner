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
package org.ops4j.pax.runner.repositories;

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class BundleInfo
{
    private static final String BUNDLE_CATEGORY = "Bundle-Category";
    private static final String BUNDLE_CLASSPATH = "Bundle-ClassPath";
    private static final String BUNDLE_COPYRIGHT = "Bundle-Copyright";
    private static final String BUNDLE_DESCRIPTION = "Bundle-Description";
    private static final String BUNDLE_NAME = "Bundle-Name";
    private static final String BUNDLE_NATIVECODE = "Bundle-NativeCode";
    private static final String EXPORT_PACKAGE = "Export-Package";
    private static final String EXPORT_SERVICE = "Export-Service";
    private static final String IMPORT_PACKAGE = "Import-Package";
    private static final String DYNAMICIMPORT_PACKAGE = "DynamicImport-Package";
    private static final String IMPORT_SERVICE = "Import-Service";
    private static final String BUNDLE_VENDOR = "Bundle-Vendor";
    private static final String BUNDLE_VERSION = "Bundle-Version";
    private static final String BUNDLE_DOCURL = "Bundle-DocURL";
    private static final String BUNDLE_CONTACTADDRESS = "Bundle-ContactAddress";
    private static final String BUNDLE_ACTIVATOR = "Bundle-Activator";
    private static final String BUNDLE_UPDATELOCATION = "Bundle-UpdateLocation";
    private static final String BUNDLE_REQUIREDEXECUTIONENVIRONMENT = "Bundle-RequiredExecutionEnvironment";
    private static final String BUNDLE_SYMBOLICNAME = "Bundle-SymbolicName";
    private static final String BUNDLE_LOCALIZATION = "Bundle-Localization";
    private static final String REQUIRE_BUNDLE = "Require-Bundle";
    private static final String FRAGMENT_HOST = "Fragment-Host";
    private static final String BUNDLE_MANIFESTVERSION = "Bundle-ManifestVersion";
    private static final String BUNDLE_URL = "Bundle-URL";
    private static final String BUNDLE_SOURCE = "Bundle-Source";
    private static final String BUNDLE_DATE = "Bundle-Date";
    private static final String METADATA_LOCATION = "Metadata-Location";
    private static final String SERVICE_COMPONENT = "Service-Component";

    private BundleRef m_reference;
    private String m_activator;
    private String m_category;
    private String m_classpath;
    private String m_contactAddress;
    private String m_copyright;
    private String m_date;
    private String m_description;
    private String m_docUrl;
    private String m_localization;
    private String m_manifestVersion;
    private String m_name;
    private String m_nativeCode;
    private String m_requiredExecutionEnvironment;
    private String m_source;
    private String m_symbolicName;
    private String m_updateLocation;
    private String m_url;
    private String m_vendor;
    private String m_version;
    private String m_exportPackage;
    private String m_importPackage;
    private String m_exportService;
    private String m_importService;
    private String m_dynamicImportPackage;
    private String m_fragmentHost;
    private String m_requireBundle;
    private String m_metadataLocation;
    private String m_serviceComponent;

    public BundleInfo( BundleRef reference )
    {
        m_reference = reference;
    }

    public void extractData( Manifest manifest )
    {
        Attributes attributes = manifest.getMainAttributes();
        for( Map.Entry entry : attributes.entrySet() )
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if( BUNDLE_ACTIVATOR.equals( key ) )
            {
                m_activator = value;
            }
            else if( BUNDLE_CATEGORY.equals( key ) )
            {
                m_category = value;
            }
            else if( BUNDLE_CLASSPATH.equals( key ) )
            {
                m_classpath = value;
            }
            else if( BUNDLE_CONTACTADDRESS.equals( key ) )
            {
                m_contactAddress = value;
            }
            else if( BUNDLE_COPYRIGHT.equals( key ) )
            {
                m_copyright = value;
            }
            else if( BUNDLE_DATE.equals( key ) )
            {
                m_date = value;
            }
            else if( BUNDLE_DESCRIPTION.equals( key ) )
            {
                m_description = value;
            }
            else if( BUNDLE_DOCURL.equals( key ) )
            {
                m_docUrl = value;
            }
            else if( BUNDLE_LOCALIZATION.equals( key ) )
            {
                m_localization = value;
            }
            else if( BUNDLE_MANIFESTVERSION.equals( key ) )
            {
                m_manifestVersion = value;
            }
            else if( BUNDLE_NAME.equals( key ) )
            {
                m_name = value;
            }
            else if( BUNDLE_NATIVECODE.equals( key ) )
            {
                m_nativeCode = value;
            }
            else if( BUNDLE_REQUIREDEXECUTIONENVIRONMENT.equals( key ) )
            {
                m_requiredExecutionEnvironment = value;
            }
            else if( BUNDLE_SOURCE.equals( key ) )
            {
                m_source = value;
            }
            else if( BUNDLE_SYMBOLICNAME.equals( key ) )
            {
                m_symbolicName = value;
            }
            else if( BUNDLE_UPDATELOCATION.equals( key ) )
            {
                m_updateLocation = value;
            }
            else if( BUNDLE_URL.equals( key ) )
            {
                m_url = value;
            }
            else if( BUNDLE_VENDOR.equals( key ) )
            {
                m_vendor = value;
            }
            else if( BUNDLE_VERSION.equals( key ) )
            {
                m_version = value;
            }
            else if( EXPORT_PACKAGE.equals( key ) )
            {
                m_exportPackage = value;
            }
            else if( IMPORT_PACKAGE.equals( key ) )
            {
                m_importPackage = value;
            }
            else if( EXPORT_SERVICE.equals( key ) )
            {
                m_exportService = value;
            }
            else if( IMPORT_SERVICE.equals( key ) )
            {
                m_importService = value;
            }
            else if( DYNAMICIMPORT_PACKAGE.equals( key ) )
            {
                m_dynamicImportPackage = value;
            }
            else if( FRAGMENT_HOST.equals( key ) )
            {
                m_fragmentHost = value;
            }
            else if( REQUIRE_BUNDLE.equals( key ) )
            {
                m_requireBundle = value;
            }
            else if( METADATA_LOCATION.equals( key ) )
            {
                m_metadataLocation = value;
            }
            else if( SERVICE_COMPONENT.equals( key ) )
            {
                m_serviceComponent = value;
            }
        }
    }

    public BundleRef getReference()
    {
        return m_reference;
    }

    public String getActivator()
    {
        return m_activator;
    }

    public String getCategory()
    {
        return m_category;
    }

    public String getClasspath()
    {
        return m_classpath;
    }

    public String getContactAddress()
    {
        return m_contactAddress;
    }

    public String getCopyright()
    {
        return m_copyright;
    }

    public String getDate()
    {
        return m_date;
    }

    public String getDescription()
    {
        return m_description;
    }

    public String getDocUrl()
    {
        return m_docUrl;
    }

    public String getLocalization()
    {
        return m_localization;
    }

    public String getManifestVersion()
    {
        return m_manifestVersion;
    }

    public String getName()
    {
        return m_name;
    }

    public String getNativeCode()
    {
        return m_nativeCode;
    }

    public String getRequiredExecutionEnvironment()
    {
        return m_requiredExecutionEnvironment;
    }

    public String getSource()
    {
        return m_source;
    }

    public String getSymbolicName()
    {
        return m_symbolicName;
    }

    public String getUpdateLocation()
    {
        return m_updateLocation;
    }

    public String getUrl()
    {
        return m_url;
    }

    public String getVendor()
    {
        return m_vendor;
    }

    public String getVersion()
    {
        return m_version;
    }

    public String getExportPackage()
    {
        return m_exportPackage;
    }

    public String getImportPackage()
    {
        return m_importPackage;
    }

    public String getExportService()
    {
        return m_exportService;
    }

    public String getImportService()
    {
        return m_importService;
    }

    public String getDynamicImportPackage()
    {
        return m_dynamicImportPackage;
    }

    public String getFragmentHost()
    {
        return m_fragmentHost;
    }

    public String getRequireBundle()
    {
        return m_requireBundle;
    }

    public String getMetadataLocation()
    {
        return m_metadataLocation;
    }

    public String getServiceComponent()
    {
        return m_serviceComponent;
    }
}
