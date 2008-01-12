package org.ops4j.pax.runner.scanner.dir.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import org.ops4j.pax.runner.commons.Assert;

/**
 * Implementation of lister that list content of a zip file.
 *
 * @author Alin Dreghiciu
 * @since September 04, 2007
 */
public class ZipLister
    implements Lister
{

    /**
     * The url of the file from which the zip file was constructed.
     */
    private URL m_baseURL;
    /**
     * The root zip entties to be listed.
     */
    private final Enumeration<? extends ZipEntry> m_zipEntries;
    /**
     * File name filter.
     */
    private final Pattern m_filter;

    /**
     * Creates a zip lister.
     *
     * @param baseURL    the url from which the zip file was created
     * @param zipEntries the zip file to be listed.
     * @param filter     filter to be used to filter entries from the zip
     */
    public ZipLister( final URL baseURL, final Enumeration<? extends ZipEntry> zipEntries, final Pattern filter )
    {
        Assert.notNull( "Base url", baseURL );
        Assert.notNull( "Zip entries", zipEntries );
        Assert.notNull( "Filter", filter );
        m_baseURL = baseURL;
        m_zipEntries = zipEntries;
        m_filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> list()
        throws MalformedURLException
    {
        final List<URL> content = new ArrayList<URL>();
        // then we filter them based on configured filter
        while( m_zipEntries.hasMoreElements() )
        {
            final ZipEntry entry = m_zipEntries.nextElement();
            final String fileName = entry.getName();
            if( !entry.isDirectory() && m_filter.matcher( fileName ).matches() )
            {
                content.add( new URL( "jar:" + m_baseURL.toExternalForm() + "!/" + fileName ) );
            }
        }

        return content;
    }

}
