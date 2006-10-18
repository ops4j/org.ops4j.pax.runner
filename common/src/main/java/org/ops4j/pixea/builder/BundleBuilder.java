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
package org.ops4j.pax.builder;

import biz.aqute.lib.osgi.Builder;
import biz.aqute.lib.osgi.Jar;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.ops4j.pax.builder.bundles.BundleModel;

public class BundleBuilder
{

    private BundleModel m_model;

    public BundleBuilder( BundleModel model )
    {
        m_model = model;
    }

    public Jar createJar()
        throws BuildException
    {
        Properties props = m_model.getProperties();
        File jarDest = m_model.getDestinationFile();
        File[] sources = m_model.getSources();
        if( !props.containsKey( "Export-Package" ) )
        {
            props.put( "Export-Package", "*" );
        }
        File target = new File( jarDest.getPath() );
        Builder builder = new Builder();
        try
        {
            builder.setJar( target );
        } catch( IOException e )
        {
            throw new BuildException( "Unable to use " + target, e );
        }
        builder.setProperties( props );
        builder.setSourcepath( sources );
        try
        {
            return builder.build();
        } catch( Exception e )
        {
            throw new BuildException( "Unable to build() " + m_model, e );
        }
    }
}
