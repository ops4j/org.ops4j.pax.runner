/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2006 Alin Dreghiciu. 
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
package org.ops4j.pax.runner.pom;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.ops4j.pax.runner.util.XmlUtils;

public class DomUtils
{

    private DomUtils()
    {
        // utility class
    }

    public static Properties parseProperties( Document document )
    {
        Element propsElement = XmlUtils.getElement( document, "properties" );
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

}
