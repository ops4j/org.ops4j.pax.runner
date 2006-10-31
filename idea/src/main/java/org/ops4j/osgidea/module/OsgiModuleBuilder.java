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
package org.ops4j.osgidea.module;

import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.ops4j.osgidea.operations.FileOps;
import org.ops4j.osgidea.builder.bundles.ManifestModel;

public class OsgiModuleBuilder extends JavaModuleBuilder
{

    private static final Logger m_logger = Logger.getLogger( OsgiModuleBuilder.class );

    private ModuleType m_type;
    private ManifestModel m_manifest;

    public OsgiModuleBuilder( ModuleType type )
    {
        m_type = type;
        m_manifest = new ManifestModel();
    }

    public void setupRootModel( ModifiableRootModel modifiableRootModel )
        throws ConfigurationException
    {
        super.setupRootModel( modifiableRootModel );
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "setupRootModel( " + modifiableRootModel + " )" );
        }
        createActivatorSkeleton( modifiableRootModel );
    }

    public ModuleType getModuleType()
    {
        return m_type;
    }

    public ManifestModel getManifest()
    {
        return m_manifest;
    }

    public void setManifest( ManifestModel manifest )
    {
        m_manifest = manifest;
    }

    private void createActivatorSkeleton( ModifiableRootModel modifiableRootModel )
    {
        VirtualFile[] sourceRoots = modifiableRootModel.getSourceRoots();
        if( sourceRoots.length > 0 )
        {
            VirtualFile sourceRoot = sourceRoots[ 0 ];
            try
            {
                String activatorClass = m_manifest.getSymbolicName() + ".internal.Activator";
                InputStream data = getActivatorSkeleton( activatorClass );
                if( data != null )
                {
                    int lastDotPos = activatorClass.lastIndexOf( '.' );
                    String activatorFileName = activatorClass.substring( lastDotPos + 1 ) + ".java";
                    String activatorPackage = activatorClass.substring( 0, lastDotPos );
                    activatorPackage = activatorPackage.replace( '.', '/' );
                    VirtualFile packageDir = FileOps.createNestedChildDir( sourceRoot, activatorPackage );
                    FileOps.createFile( packageDir, activatorFileName, data );
                }
            } catch( IOException e )
            {
                e.printStackTrace();  //TODO: Auto-generated, need attention.
            }
        }
    }

    private InputStream getActivatorSkeleton( String activatorClassname )
        throws UnsupportedEncodingException
    {
        if( activatorClassname.length() == 0 )
        {
            return null;
        }
        int pos = activatorClassname.lastIndexOf( '.' );
        if( pos < 0 )
        {
            return null;
        }
        String packageName = activatorClassname.substring( 0, pos );
        String classname = activatorClassname.substring( pos + 1 );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter( baos, "UTF-8" );
        PrintWriter pw = new PrintWriter( osw, true );
        printLicense( pw );
        pw.print( "package " );
        pw.print( packageName );
        pw.println( ";" );
        pw.println();
        pw.println( "import org.osgi.framework.BundleActivator;" );
        pw.println( "import org.osgi.framework.BundleContext;" );
        pw.println();
        pw.print( "public class " );
        pw.println( classname );
        pw.println( "    implements BundleActivator" );
        pw.println( "{" );
        pw.println();
        pw.println( "    public void start( BundleContext context )" );
        pw.println( "    {" );
        pw.println( "        //TODO: Create the bundle resources." );
        pw.println( "        //This method should return as fast as possible." );
        pw.println( "    }" );
        pw.println();
        pw.println( "    public void stop( BundleContext context )" );
        pw.println( "    {" );
        pw.println( "        //TODO: Release all resource, threads and object instances in use." );
        pw.println( "    }" );
        pw.println( "}" );
        pw.flush();
        pw.close();
        byte[] data = baos.toByteArray();
        return new ByteArrayInputStream( data );
    }

    private void printLicense( PrintWriter pw )
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get( Calendar.YEAR );
        String license = m_manifest.getLicense();
        String copyrightOwner = m_manifest.getCopyright();
        if( copyrightOwner.trim().length() == 0 )
        {
            copyrightOwner = m_manifest.getVendor();
        }
        pw.println( "/*" );
        pw.print( "* Copyright " );
        pw.print( year );
        pw.print( " " );
        pw.print( copyrightOwner );
        pw.println( "." );
        pw.println( "*" );
        if( license.toLowerCase().indexOf( "apache" ) >= 0 )
        {
            pw.println( "* Licensed  under the  Apache License,  Version 2.0  (the \"License\");" );
            pw.println( "* you may not use  this file  except in  compliance with the License." );
            pw.println( "* You may obtain a copy of the License at" );
            pw.println( "*" );
            pw.println( "*   http://www.apache.org/licenses/LICENSE-2.0" );
            pw.println( "*" );
            pw.println( "* Unless required by applicable law or agreed to in writing, software" );
            pw.println( "* distributed  under the  License is distributed on an \"AS IS\" BASIS," );
            pw.println( "* WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or" );
            pw.println( "* implied." );
            pw.println( "*" );
            pw.println( "* See the License for the specific language governing permissions and" );
            pw.println( "* limitations under the License." );
        }
        else
        {
            pw.println( license );
        }
        pw.println( "*/\n" );
    }
}
