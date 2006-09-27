package org.ops4j.pax.runner.idea.module;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public class DataBeanManifest
    implements JDOMExternalizable
{

    private String m_category;
    private String m_symbolicName;
    private String m_bundleName;
    private String m_vendor;
    private String m_copyright;
    private String m_contactAddress;
    private String m_license;
    private String m_activator;
    private String m_updateLocation;
    private String m_docUrl;
    private String m_version;
    private String m_description;

    public DataBeanManifest()
    {
    }

    public DataBeanManifest clone()
    {
        DataBeanManifest clone = new DataBeanManifest();
        clone.m_category = m_category;
        clone.m_symbolicName = m_symbolicName;
        clone.m_bundleName = m_bundleName;
        clone.m_vendor = m_vendor;
        clone.m_copyright = m_copyright;
        clone.m_contactAddress = m_contactAddress;
        clone.m_license = m_license;
        clone.m_activator = m_activator;
        clone.m_updateLocation = m_updateLocation;
        clone.m_docUrl = m_docUrl;
        clone.m_version = m_version;
        clone.m_description = m_description;
        return clone;
    }

    public String getCategory()
    {
        return m_category;
    }

    public void setCategory( final String category )
    {
        m_category = category;
    }

    public String getSymbolicName()
    {
        return m_symbolicName;
    }

    public void setSymbolicName( final String symbolicName )
    {
        m_symbolicName = symbolicName;
    }

    public String getBundleName()
    {
        return m_bundleName;
    }

    public void setBundleName( final String bundleName )
    {
        m_bundleName = bundleName;
    }

    public String getVendor()
    {
        return m_vendor;
    }

    public void setVendor( final String vendor )
    {
        m_vendor = vendor;
    }

    public String getCopyright()
    {
        return m_copyright;
    }

    public void setCopyright( final String copyright )
    {
        m_copyright = copyright;
    }

    public String getContactAddress()
    {
        return m_contactAddress;
    }

    public void setContactAddress( final String contactAddress )
    {
        m_contactAddress = contactAddress;
    }

    public String getLicense()
    {
        return m_license;
    }

    public void setLicense( final String license )
    {
        m_license = license;
    }

    public String getActivator()
    {
        return m_activator;
    }

    public void setActivator( final String activator )
    {
        m_activator = activator;
    }

    public String getUpdateLocation()
    {
        return m_updateLocation;
    }

    public void setUpdateLocation( final String updateLocation )
    {
        m_updateLocation = updateLocation;
    }

    public String getDocUrl()
    {
        return m_docUrl;
    }

    public void setDocUrl( final String docUrl )
    {
        m_docUrl = docUrl;
    }

    public String getVersion()
    {
        return m_version;
    }

    public void setVersion( final String version )
    {
        m_version = version;
    }

    public String getDescription()
    {
        return m_description;
    }

    public void setDescription( final String description )
    {
        m_description = description;
    }

    public void readExternal( Element element )
        throws InvalidDataException
    {
        m_activator = getValue( element, "bundleactivator" );
        m_bundleName = getValue( element, "bundlename" );
        m_category = getValue( element, "bundlecategory" );
        m_contactAddress = getValue( element, "contactaddress" );
        m_copyright = getValue( element, "copyright" );
        m_description = getValue( element, "description" );
        m_docUrl = getValue( element, "docurl" );
        m_license = getValue( element, "license" );
        m_symbolicName = getValue( element, "symbolicname" );
        m_updateLocation = getValue( element, "updatelocation" );
        m_vendor = getValue( element, "vendor" );
        m_version = getValue( element, "version" );
    }

    public void writeExternal( Element element )
        throws WriteExternalException
    {
        setValue( element, "bundleactivator", m_activator );
        setValue( element, "bundlename", m_bundleName );
        setValue( element, "bundlecategory", m_category );
        setValue( element, "contactaddress", m_contactAddress );
        setValue( element, "copyright", m_copyright );
        setValue( element, "description", m_description );
        setValue( element, "docurl", m_docUrl );
        setValue( element, "license", m_license );
        setValue( element, "symbolicname", m_symbolicName );
        setValue( element, "updatelocation", m_updateLocation );
        setValue( element, "vendor", m_vendor );
        setValue( element, "version", m_version );
    }

    private void setValue( Element element, String tag, String value )
    {
        Element child = new Element( tag );
        child.setText( value );
        element.addContent( child );
    }

    private String getValue( Element element, String tagname )
    {
        Element child = element.getChild( tagname );
        return child.getText();
    }
}