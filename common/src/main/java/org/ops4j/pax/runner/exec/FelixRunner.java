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
package org.ops4j.pax.runner.exec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.ops4j.pax.runner.DownloadManager;
import org.ops4j.pax.runner.PomInfo;
import org.ops4j.pax.runner.Runner;
import org.ops4j.pax.runner.ServiceException;
import org.ops4j.pax.runner.ServiceManager;
import org.ops4j.pax.runner.internal.RunnerOptions;
import org.ops4j.pax.runner.state.Bundle;
import org.ops4j.pax.runner.state.BundleState;
import org.ops4j.pax.runner.utils.FileUtils;
import org.ops4j.pax.runner.utils.Pipe;
import org.xml.sax.SAXException;

public class FelixRunner
    implements Runner
{

    private static final String GROUPID = "org.apache.felix";

    private static final String VERSION = "0.8.0";

    private Properties m_props;
    private static final String SYSTEM_PACKAGES = "javax.accessibility, " +
                                                  "javax.activity, " +
                                                  "javax.crypto, " +
                                                  "javax.crypto.interfaces, " +
                                                  "javax.crypto.spec, " +
                                                  "javax.imageio, " +
                                                  "javax.imageio.event, " +
                                                  "javax.imageio.metadata, " +
                                                  "javax.imageio.plugins.bmp, " +
                                                  "javax.imageio.plugins.jpeg, " +
                                                  "javax.imageio.spi, " +
                                                  "javax.imageio.stream, " +
                                                  "javax.management, " +
                                                  "javax.management.loading, " +
                                                  "javax.management.modelmbean, " +
                                                  "javax.management.monitor, " +
                                                  "javax.management.openmbean, " +
                                                  "javax.management.relation, " +
                                                  "javax.management.remote, " +
                                                  "javax.management.remote.rmi, " +
                                                  "javax.management.timer, " +
                                                  "javax.naming, " +
                                                  "javax.naming.directory, " +
                                                  "javax.naming.event, " +
                                                  "javax.naming.ldap, " +
                                                  "javax.naming.spi, " +
                                                  "javax.net, " +
                                                  "javax.net.ssl, " +
                                                  "javax.print, " +
                                                  "javax.print.attribute, " +
                                                  "javax.print.attribute.standard, " +
                                                  "javax.print.event, " +
                                                  "javax.rmi, " +
                                                  "javax.rmi.CORBA, " +
                                                  "javax.rmi.ssl, " +
                                                  "javax.security.auth, " +
                                                  "javax.security.auth.callback, " +
                                                  "javax.security.auth.kerberos, " +
                                                  "javax.security.auth.login, " +
                                                  "javax.security.auth.spi, " +
                                                  "javax.security.auth.x500, " +
                                                  "javax.security.cert, " +
                                                  "javax.security.sasl, " +
                                                  "javax.sound.midi, " +
                                                  "javax.sound.midi.spi, " +
                                                  "javax.sound.sampled, " +
                                                  "javax.sound.sampled.spi, " +
                                                  "javax.sql, " +
                                                  "javax.sql.rowset, " +
                                                  "javax.sql.rowset.serial, " +
                                                  "javax.sql.rowset.spi, " +
                                                  "javax.swing, " +
                                                  "javax.swing.border, " +
                                                  "javax.swing.colorchooser, " +
                                                  "javax.swing.event, " +
                                                  "javax.swing.filechooser, " +
                                                  "javax.swing.plaf, " +
                                                  "javax.swing.plaf.basic, " +
                                                  "javax.swing.plaf.metal, " +
                                                  "javax.swing.plaf.multi, " +
                                                  "javax.swing.plaf.synth, " +
                                                  "javax.swing.table, " +
                                                  "javax.swing.text, " +
                                                  "javax.swing.text.html, " +
                                                  "javax.swing.text.html.parser, " +
                                                  "javax.swing.text.rtf, " +
                                                  "javax.swing.tree, " +
                                                  "javax.swing.undo, " +
                                                  "javax.transaction, " +
                                                  "javax.transaction.xa, " +
                                                  "javax.xml, " +
                                                  "javax.xml.datatype, " +
                                                  "javax.xml.namespace, " +
                                                  "javax.xml.parsers, " +
                                                  "javax.xml.transform, " +
                                                  "javax.xml.transform.dom, " +
                                                  "javax.xml.transform.sax, " +
                                                  "javax.xml.transform.stream, " +
                                                  "javax.xml.validation, " +
                                                  "javax.xml.xpath, " +
                                                  "org.ietf.jgss, " +
                                                  "org.omg.CORBA, " +
                                                  "org.omg.CORBA_2_3, " +
                                                  "org.omg.CORBA_2_3.portable, " +
                                                  "org.omg.CORBA.DynAnyPackage, " +
                                                  "org.omg.CORBA.ORBPackage, " +
                                                  "org.omg.CORBA.portable, " +
                                                  "org.omg.CORBA.TypeCodePackage, " +
                                                  "org.omg.CosNaming, " +
                                                  "org.omg.CosNaming.NamingContextExtPackage, " +
                                                  "org.omg.CosNaming.NamingContextPackage, " +
                                                  "org.omg.Dynamic, " +
                                                  "org.omg.DynamicAny, " +
                                                  "org.omg.DynamicAny.DynAnyFactoryPackage, " +
                                                  "org.omg.DynamicAny.DynAnyPackage, " +
                                                  "org.omg.IOP, " +
                                                  "org.omg.IOP.CodecFactoryPackage, " +
                                                  "org.omg.IOP.CodecPackage, " +
                                                  "org.omg.Messaging, " +
                                                  "org.omg.PortableInterceptor, " +
                                                  "org.omg.PortableInterceptor.ORBInitInfoPackage, " +
                                                  "org.omg.PortableServer, " +
                                                  "org.omg.PortableServer.CurrentPackage, " +
                                                  "org.omg.PortableServer.POAManagerPackage, " +
                                                  "org.omg.PortableServer.POAPackage, " +
                                                  "org.omg.PortableServer.portable, " +
                                                  "org.omg.PortableServer.ServantLocatorPackage, " +
                                                  "org.omg.SendingContext, " +
                                                  "org.omg.stub.java.rmi, " +
                                                  "org.osgi.framework; version=1.3.0, " +
                                                  "org.osgi.service.condpermadmin; version=1.0.0, " +
                                                  "org.osgi.service.packageadmin; version=1.2.0, " +
                                                  "org.osgi.service.permissionadmin; version=1.2.0, " +
                                                  "org.osgi.service.startlevel; version=1.0.0, " +
                                                  "org.osgi.service.url; version=1.0.0, " +
                                                  "org.osgi.util.tracker; version=1.3.1, " +
                                                  "org.w3c.dom, " +
                                                  "org.w3c.dom.bootstrap, " +
                                                  "org.w3c.dom.events, " +
                                                  "org.w3c.dom.ls, " +
                                                  "org.xml.sax, " +
                                                  "org.xml.sax.ext, " +
                                                  "org.xml.sax.helpers";

    public FelixRunner( Properties props )
        throws IOException, ParserConfigurationException, SAXException, ServiceException
    {
        m_props = props;
    }

    private void createConfigFile( RunnerOptions options, List<Bundle> bundles, File osgi, File framework, File system )
        throws IOException
    {
        File confDir = new File( options.getWorkDir(), "conf" );
        confDir.mkdirs();
        File file = new File( confDir, "config.properties" );
        Writer out = FileUtils.openPropertyFile( file );
        try
        {
            FileUtils.writeProperty( out, "org.osgi.framework.system.packages", SYSTEM_PACKAGES );
            String profile = options.getProfile();
            if( profile != null )
            {
                FileUtils.writeProperty( out, "felix.cache.profile", profile );
            }
            FileUtils.writeProperty( out, "felix.startlevel.framework", "1" );
            FileUtils.writeProperty( out, "felix.startlevel.bundle", "3" );
            FileUtils.writeProperty( out, "obr.repository.url",
                                     "http://bundles.osgi.org/obr/browse?_xml=1&cmd=repository"
            );
            boolean first = true;
            StringBuffer buf = new StringBuffer();
            for( Bundle bundle : bundles )
            {
                if( !first )
                {
                    buf.append( ", \\\n    " );
                }
                first = false;
                buf.append( bundle.getBundleData().getAbsolutePath() );
            }
            FileUtils.writeProperty( out, "felix.auto.start.3", buf.toString() );
            for( Map.Entry entry : m_props.entrySet() )
            {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                FileUtils.writeProperty( out, key, value );
            }
            out.flush();
        } finally
        {
            out.close();
        }
    }

    private void runIt( RunnerOptions options, File system )
        throws IOException, InterruptedException
    {
        Runtime runtime = Runtime.getRuntime();

        String javaHome = System.getProperty( "java.home" );
        if( javaHome == null )
        {
            javaHome = System.getenv().get( "JAVA_HOME" );
        }
        if( javaHome == null )
        {
            System.err.println( "JAVA_HOME is not set." );
        }
        else
        {
            File workDir = options.getWorkDir();
            String[] cmd =
                {
                    javaHome + "/bin/java",
                    "-Dfelix.config.properties=" + workDir.getAbsolutePath() + "conf/config.properties",
                    "-jar",
                    system.getAbsolutePath(),
                };
            Process process = runtime.exec( cmd, null, options.getWorkDir() );
            InputStream err = process.getErrorStream();
            InputStream out = process.getInputStream();
            OutputStream in = process.getOutputStream();
            Pipe errPipe = new Pipe( err, System.err );
            errPipe.start();
            Pipe outPipe = new Pipe( out, System.out );
            outPipe.start();
            Pipe inPipe = new Pipe( System.in, in );
            inPipe.start();
            process.waitFor();
            inPipe.stop();
            outPipe.stop();
            errPipe.stop();
        }
    }

    public void execute( RunnerOptions options, List<Bundle> bundles )
        throws ServiceException, IOException
    {
        DownloadManager downloadManager = ServiceManager.getInstance().getService( DownloadManager.class );
        PomInfo shellPom = new PomInfo( GROUPID, "org.apache.felix.shell", VERSION );
        File system1 = downloadManager.download( shellPom );
        bundles.add( new Bundle( system1, 0, BundleState.START ) );
        PomInfo obrPom = new PomInfo( GROUPID, "org.apache.felix.bundlerepository", VERSION );
        File system2 = downloadManager.download( obrPom );
        bundles.add( new Bundle( system2, 0, BundleState.START ) );
        PomInfo tuiPom = new PomInfo( GROUPID, "org.apache.felix.shell.tui", VERSION );
        File system3 = downloadManager.download( tuiPom );
        bundles.add( new Bundle( system3, 0, BundleState.START ) );
        if( options.isStartGui() )
        {
            PomInfo guiPom = new PomInfo( GROUPID, "org.apache.felix.shell.gui", VERSION );
            File system4 = downloadManager.download( guiPom );
            bundles.add( new Bundle( system4, 0, BundleState.START ) );
            PomInfo guiPluginPom = new PomInfo( GROUPID, "org.apache.felix.shell.gui.plugin", VERSION );
            File system5 = downloadManager.download( guiPluginPom );
            bundles.add( new Bundle( system5, 0, BundleState.START ) );
        }
        PomInfo systemPom = new PomInfo( GROUPID, "org.apache.felix.main", VERSION );
        File system = downloadManager.download( systemPom );
        PomInfo frameworkPom = new PomInfo( GROUPID, "org.osgi.felix.framework", VERSION );
        File framework = downloadManager.download( frameworkPom );
        PomInfo osgiPom = new PomInfo( GROUPID, "org.osgi.core", VERSION );
        File osgi = downloadManager.download( osgiPom );
        try
        {
            createConfigFile( options, bundles, osgi, framework, system );
            runIt( options, system );
        } catch( MalformedURLException e )
        {
            e.printStackTrace();
        } catch( IOException e )
        {
            e.printStackTrace();
        } catch( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}
