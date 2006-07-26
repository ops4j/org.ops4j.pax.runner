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
package org.ops4j.pax.demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/** This class will download a Pax Demo Descriptor and start that demo.
 *
 * The Pax Demo Descriptors are Maven artifacts. The groupId MUST be org.ops4j.pax.demo, and the artifactId MUST be the
 * demo-name which is given on the command line. Version must be provided as the second argument. ONLY the OPS4J
 * repository is supported at the moment.
 *
 * Pax Demo Descriptor is a Properties file and contains the following information;
 * <table>
 * <tr><th>Property</th><th>Format</th><th>Description</th></tr>
 * <tr>
 *   <td>name</td><td>String</td><td>Name of the Demo</td>
 * </tr>
 * <tr>
 *   <td>useFramework</td><td>String</td><td>Which framework to use. Currently supported: Equinox</td>
 * </tr>
 * <tr>
 *   <td>bundle.N</td><td>[groupId], [artifactId], [version]</td><td>Each bundle with a different N.</td>
 * </tr>
 * <tr>
 *   <td>startlevel</td><td>String value of integer</td><td>The default startlevel of the framework.</td>
 * </tr>
 * </table>
 */
public class Run
{

    public static void main( String[] args )
        throws IOException
    {
        if( args.length < 2 )
        {
            System.err.println( "java -jar runner.jar <demo-name> <version>");
            System.exit(1);
        }
        String demoName = args[ 0 ];
        String version = args[1];

        String filename = demoName + "-" + version + ".demo";
        URL url = new URL( "http://repository.ops4j.org/maven2/org/ops4j/pax/demo/" + demoName + "/" + version + "/" + filename);
        InputStream in = url.openStream();
        Properties props = new Properties();
        try
        {
            if( ! (in instanceof BufferedInputStream ) )
            {
                in = new BufferedInputStream( in );
            }
            props.load( in );
        } finally
        {
            in.close();
        }
        String frameworkToUse = props.getProperty( "useFramework" );
        if( "equinox".equals( frameworkToUse.toLowerCase() ) )
        {
            Runnable wrapper = new EquinoxRunner( props );
            wrapper.run();
        }
        else
        {
            System.err.println( "Framework not supported: " + frameworkToUse );
            System.exit( 1 );
        }
    }
}
