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
package org.ops4j.pax.runner.internal;

import org.ops4j.pax.runner.pom.Model;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;

public class PomBuilderImpl implements PomBuilder
{

    private static final Logger m_logger = Logger.getLogger( PomBuilderImpl.class.getName() );

    public Model parse( InputStream in )
        throws PomException
    {
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance( Model.class );
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement jaxbModel = (JAXBElement) unmarshaller.unmarshal( in );
            return (Model) jaxbModel.getValue();
        } catch( JAXBException e )
        {
            m_logger.log( Level.SEVERE, "Unable to parse POM.", e );
            throw new PomException( "Unable to parse POM.", e );
        }
    }
}
