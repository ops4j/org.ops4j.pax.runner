package org.ops4j.pax.runner.scanner.dir.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
     * The root directory to be listed.
     */
    private final ZipFile m_zip;
    /**
     * File name filter.
     */
    private final Pattern m_filter;

    /**
     * Creates a zip lister.
     *
     * @param baseURL the url from which the zip file was created
     * @param zip     the zip file to be listed.
     * @param filter  filter to be used to filter entries from the zip
     */
    public ZipLister( final URL baseURL, final ZipFile zip, final Pattern filter )
    {
        Assert.notNull( "Base url", baseURL );
        Assert.notNull( "Zip file", zip );
        Assert.notNull( "Filter", filter );
        m_baseURL = baseURL;
        m_zip = zip;
        m_filter = filter;
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> list()
        throws MalformedURLException
    {
        final List<URL> content = new ArrayList<URL>();
        // first we get all files
        final Enumeration<? extends ZipEntry> entries = m_zip.entries();
        // then we filter them based on configured filter
        while ( entries.hasMoreElements() )
        {
            final ZipEntry entry = entries.nextElement();
            final String fileName = entry.getName();
            if ( !entry.isDirectory() && m_filter.matcher( fileName ).matches() )
            {
                content.add( new URL( "jar:" + m_baseURL.toExternalForm() + "!/" + fileName ) );
            }
        }

        return content;
    }

}
