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
package org.ops4j.pax.repoman;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.ops4j.pax.model.repositories.Repository;
import org.ops4j.pax.model.repositories.RepositoryInfo;
import org.ops4j.pax.model.repositories.RepositoryType;
import org.ops4j.pax.model.bundles.BundleRef;
import org.ops4j.pax.model.bundles.BundleModel;
import org.ops4j.pax.model.bundles.BundleObserver;
import org.ops4j.pax.runner.ServiceManager;
import org.ops4j.pax.runner.ServiceException;

public class ObrRepository
    implements Repository, Runnable
{
    private RepositoryInfo m_info;
    private Thread m_thread;
    private ArrayList<BundleObserver> m_observers;
    private URL m_url;
    private boolean m_running;
    private List<BundleRef> m_bundles;

    public ObrRepository( URL location )
    {
        m_bundles = new ArrayList<BundleRef>();
        m_url = location;
        init();
    }

    public ObrRepository()
    {
        try
        {
            String location = "http://oscar-osgi.sourceforge.net/repository.xml";
            m_url = new URL(location);
            m_info = new RepositoryInfo( "Oscar Bundle Repository", location, RepositoryType.oscar );
            init();
        } catch( MalformedURLException e )
        {
            e.printStackTrace();  //TODO: Auto-generated, need attention.
        }
    }

    private void init()
    {
        m_observers = new ArrayList<BundleObserver>();
        m_thread = new Thread( this );
        m_thread.start();
    }

    public RepositoryInfo getInfo()
    {
        return m_info;
    }

    public BundleModel download( BundleRef bundleReference )
    {
        return null;
    }

    public void addBundleObserver( BundleObserver observer )
    {
        synchronized( m_observers )
        {
            m_observers.add( observer );
        }
    }

    public void removeBundleObserver( BundleObserver observer )
    {
        synchronized( m_observers )
        {
            m_observers.remove( observer );
        }
    }

    public void dispose()
    {
        m_running = false;
        m_thread.interrupt();
    }

    public void run()
    {
        while( m_running )
        {
            try
            {
                DownloadManager downloadManager = ServiceManager.getInstance().getService( DownloadManager.class );
                File file = downloadManager.download( m_url );
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse( file );
                Element root = doc.getDocumentElement();
//                Element repository = (Element) root.getElementsByTagName( "repository" ).item( 0 );
//                Element repoName = (Element) repository.getElementsByTagName( "name" ).item( 0 );
                NodeList bundleNodes = root.getElementsByTagName( "bundle" );
                List<BundleRef> bundles = new ArrayList<BundleRef>();
                for( int i = 0; i < bundleNodes.getLength(); i++ )
                {
                    Properties props = new Properties();
                    Element bundleElement = (Element) bundleNodes.item( i );
                    String name = getValue( bundleElement, "bundle-name", props );
                    String updateLocation = getValue( bundleElement, "bundle-updatelocation", props );

                    getValue( bundleElement, "bundle-description", props );
                    getValue( bundleElement, "bundle-sourceurl", props );
                    getValue( bundleElement, "bundle-version", props );
                    getValue( bundleElement, "bundle-docurl", props );
                    getValue( bundleElement, "bundle-category", props );
//
//                    NodeList importElements = bundleElement.getElementsByTagName( "import-package" );
//                    NodeList exportElements = bundleElement.getElementsByTagName( "export-package" );

                    BundleRef ref = new BundleRef( name, m_info, new URL( updateLocation ), props );
                    bundles.add( ref );
                }
                update( bundles );
                Thread.sleep( 3600000 );
            } catch( MalformedURLException e )
            {
                e.printStackTrace();
            } catch( ParserConfigurationException e )
            {
                e.printStackTrace();
            } catch( SAXException e )
            {
                e.printStackTrace();
            } catch( ServiceException e )
            {
                e.printStackTrace();
            } catch( IOException e )
            {
                e.printStackTrace();
            } catch( InterruptedException e )
            {
                // ignore
            }
        }
    }

    private void update( List<BundleRef> bundles )
    {
        List<BundleRef> removed = new ArrayList<BundleRef>();
        removed.addAll( m_bundles );
        removed.removeAll( bundles );
        List<BundleRef> added = new ArrayList<BundleRef>();
        added.addAll( bundles );
        added.removeAll( m_bundles );
        for( BundleRef ref : removed )
        {
            removeBundle( ref );
        }
        for( BundleRef ref : added )
        {
            addBundle( ref );
        }
    }

    private void addBundle( BundleRef ref )
    {
        m_bundles.add( ref );
        for( BundleObserver observer : m_observers )
        {
            observer.bundleAdded( ref );
        }
    }

    private void removeBundle( BundleRef ref )
    {
        m_bundles.add( ref );
        for( BundleObserver observer : m_observers )
        {
            observer.bundleAdded( ref );
        }
    }

    private String getValue( Element bundleElement, String field, Properties props )
    {
        String name = bundleElement.getTagName();
        NodeList nodes = bundleElement.getElementsByTagName( field );
        Element element = (Element) nodes.item( 0 );
        String content = element.getTextContent();
        props.put( name, content );
        return content;
    }
}
