/*
 * Copyright 2006 Niclas Hedhman.
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
package org.ops4j.pax.runner.util;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class XmlUtils
{

    private XmlUtils()
    {
        // utility class
    }

    public static Document parseDoc( File docFile )
        throws ParserConfigurationException,
               SAXException,
               IOException
    {
        FileInputStream fis = new FileInputStream( docFile );
        try
        {
            BufferedInputStream in = new BufferedInputStream( fis );
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource( in );
            Document document = builder.parse( source );
            return document;
        }
        finally
        {
            fis.close();
        }
    }

    public static String getElement( Node node, String elementName )
    {
        NodeList nl = node.getChildNodes();
        for( int i = 0; i < nl.getLength(); i++ )
        {
            Node n = nl.item( i );
            if( elementName.equals( n.getNodeName() ) )
            {
                Element elem = (Element) n;
                return elem.getTextContent();
            }
        }
        return null;
    }

    public static Element getElement( Document doc, String elementName )
    {
        Element current = doc.getDocumentElement();
        StringTokenizer st = new StringTokenizer( elementName, "/", false );
        while( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            current = (Element) current.getElementsByTagName( token ).item( 0 );
        }
        return current;
    }
}
