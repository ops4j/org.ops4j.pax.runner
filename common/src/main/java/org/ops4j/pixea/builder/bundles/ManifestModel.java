package org.ops4j.pax.builder.bundles;

public class ManifestModel
{

    private String m_category;
    private String m_symbolicName;
    private String m_bundleName;
    private String m_vendor;
    private String m_copyright;
    private String m_contactAddress;
    private String m_license;
    private String m_updateLocation;
    private String m_docUrl;
    private String m_version;
    private String m_description;

    public ManifestModel()
    {
    }

    public ManifestModel clone()
    {
        ManifestModel clone = new ManifestModel();
        clone.m_category = m_category;
        clone.m_symbolicName = m_symbolicName;
        clone.m_bundleName = m_bundleName;
        clone.m_vendor = m_vendor;
        clone.m_copyright = m_copyright;
        clone.m_contactAddress = m_contactAddress;
        clone.m_license = m_license;
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
}