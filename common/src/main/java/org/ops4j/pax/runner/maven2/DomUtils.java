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
package org.ops4j.pax.runner.maven2;

import java.util.Properties;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomUtils
{

    public static Properties parseProperties( Document document )
    {
        Element propsElement = getElement( document, "properties" );
        Properties props = new Properties();
        if( propsElement == null )
        {
            return props;
        }
        NodeList childNodes = propsElement.getChildNodes();
        for( int i = 0; i < childNodes.getLength(); i++ )
        {
            Node node = childNodes.item( i );
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                Element element = (Element) node;
                props.put( element.getTagName(), element.getTextContent() );
            }
        }
        return props;
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
}
