/*
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
package org.ops4j.pax.cursor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.launcher.LaunchArgumentsHelper;
import org.eclipse.pde.internal.ui.launcher.LauncherUtils;
import org.eclipse.pde.ui.launcher.IPDELauncherConstants;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Utilities related to launching Pax Runner.
 *
 * @author Alin Dreghiciu
 * @since 0.1.0
 */
class Utils
{

    /**
     * Plugin id.
     */
    static final String PLUGIN_ID = "org.ops4j.pax.runner.eclipse.core";

    /**
     * Utility class.
     */
    private Utils()
    {
        // utility class
    }

    /**
     * Determine options specified by user as program arguments. It takes only options starting with "--" and "scan-"
     * as there can be leftovers from Equinox default params.
     *
     * @param userArgs array of user specified arguments
     *
     * @return list of options
     */
    public static List getUserOptions( final String[] userArgs )
    {

        final List options = new ArrayList();

        if( userArgs != null && userArgs.length > 0 )
        {
            for( int i = 0; i < userArgs.length; i++ )
            {
                final String arg = userArgs[ i ];
                if( supportedArgument( arg ) )
                {
                    options.add( arg );
                }
            }
        }
        return options;
    }

    /**
     * Returns true if the argument is supported, meaning that it should start with "--" and should not be a
     * platform/version argument.
     *
     * @param arg the argument
     *
     * @return true if supported
     */
    private static boolean supportedArgument( final String arg )
    {
        return arg != null
               && ( ( arg.startsWith( "--" ) && arg.length() > 2 ) || arg.startsWith( "scan-" ) )
               && !arg.startsWith( "--p=" ) && !arg.startsWith( "--platform=" ) && !arg.startsWith( "--v=" )
               && !arg.startsWith( "--version=" );
    }

    /**
     * Determine the platform and version options. This is using the trick of setting the framework id in the extension
     * configuration exactelly to the options to be passed to Pax Runner.
     *
     * @param configuration launch configuration
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getTargetPlatformOptions( final ILaunchConfiguration configuration )
        throws CoreException
    {

        final List options = new ArrayList();

        final String[] platformArgs =
            configuration.getAttribute( IPDELauncherConstants.OSGI_FRAMEWORK_ID, "" ).split( " " );
        if( platformArgs != null && platformArgs.length > 0 )
        {
            for( int i = 0; i < platformArgs.length; i++ )
            {
                final String arg = platformArgs[ i ];
                if( arg != null && arg.startsWith( "--" ) )
                {
                    options.add( arg );
                }
            }
        }

        return options;
    }

    /**
     * Determine start level options. Eclipse has only one start level (default) so it does not make any distinction
     * between bundle start level and framework start level so we set both to the same value.
     * Based on user setting on "Bundles/Default start level/Default auto-start".
     *
     * @param configuration launch configuration
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getStartLevelOptions( final ILaunchConfiguration configuration )
        throws CoreException
    {

        final List options = new ArrayList();

        // handle the start level
        final int start = configuration.getAttribute( IPDELauncherConstants.DEFAULT_START_LEVEL, 4 );
        options.add( "--bundleStartLevel=" + start );
        options.add( "--startLevel=" + start );
        // handle auto start
        options.add( "--start=" + configuration.getAttribute( IPDELauncherConstants.DEFAULT_AUTO_START, true ) );

        return options;
    }

    /**
     * Determine if there is a clean start = the runner directory will be removed prior running.
     * Based on the user setting on "Settings/Clear the configuration area before launching.
     *
     * @param configuration launch configuration
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getCleanOptions( final ILaunchConfiguration configuration )
        throws CoreException
    {

        final List options = new ArrayList();

        if( configuration.getAttribute( IPDELauncherConstants.CONFIG_CLEAR_AREA, false ) )
        {
            options.add( "--clean" );
        }

        return options;
    }

    /**
     * Determine virtual machine arguments. As Pax Runner is just a proxy the vm  args set by user are not for Pax Runer
     * itself. We pass them as arguments to pax runner.
     * Based on the user setting on "Arguments/VM Arguments".
     *
     * @param configuration launch configuration
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getVMArgsOptions( final ILaunchConfiguration configuration )
        throws CoreException
    {

        final List options = new ArrayList();

        final String[] vmArgs = new ExecutionArguments(
            LaunchArgumentsHelper.getUserVMArguments( configuration ), ""
        ).getVMArgumentsArray();
        if( vmArgs != null && vmArgs.length > 0 )
        {
            final StringBuffer buffer = new StringBuffer();
            for( int i = 0; i < vmArgs.length; i++ )
            {
                final String arg = vmArgs[ i ];
                if( arg != null )
                {
                    buffer.append( arg ).append( " " );
                }
            }
            options.add( "--vmOptions=" + buffer.toString().trim() );
        }

        return options;
    }

    /**
     * Determine working directory as the specified configuration location plus a sub directory "runner". It has to be a
     * sub directory as in the root directory we may create some files and in case that is a "clean" start that folder
     * will be removed by runner.
     * Based on the user setting on "Settings/Configuration Area".
     *
     * @param configDirLocation path to configuration area directory
     *
     * @return list of options
     */
    public static List getWorkingDirOptions( final String configDirLocation )
    {

        final List options = new ArrayList();

        if( configDirLocation != null && configDirLocation.trim().length() > 0 )
        {
            options.add(
                "--workingDirectory=" + new Path( configDirLocation ).append( "runner" ).addTrailingSeparator()
            );
        }

        return options;
    }

    /**
     * Creates a file that contains the environment properties set by user on "Environment". The file will be named
     * "runner.props" and will be placed in the configuration area. The created file will be sent as provision url
     * using scan-file scanner.
     *
     * @param configDirLocation path to configuration area directory
     * @param envProperties     an array of environment properties
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getEnvPropertiesFile( final String configDirLocation, final String[] envProperties )
        throws CoreException
    {

        final List options = new ArrayList();

        if( envProperties != null && envProperties.length > 0 )
        {
            final File propFile = new Path( configDirLocation ).append( "runner.props" ).toFile();
            BufferedWriter writer = null;
            try
            {
                writer = new BufferedWriter( new FileWriter( propFile ) );
                for( int i = 0; i < envProperties.length; i++ )
                {
                    final String envVar = envProperties[ i ];
                    // the environment properties should already be in a format
                    // key=value
                    if( envVar != null && envVar.length() > 0 && envVar.indexOf( "=" ) > 0 )
                    {
                        writer.write( "-D" + envVar );
                        writer.newLine();
                    }
                }
                options.add( "scan-file:" + propFile.getCanonicalFile().toURL().toExternalForm() );
            }
            catch( MalformedURLException e )
            {
                throw new CoreException( LauncherUtils.createErrorStatus( "Cannot create environment file" ) );
            }
            catch( IOException e )
            {
                throw new CoreException( LauncherUtils.createErrorStatus( "Cannot create environment file" ) );
            }
            finally
            {
                if( writer != null )
                {
                    try
                    {
                        writer.flush();
                        writer.close();
                    }
                    catch( IOException e )
                    {
                        // do nothing
                        PDECore.logException( e );
                    }
                }
            }
        }

        return options;
    }

    /**
     * Creates a provisioning file with the bundles to be installed. The file will be named "runner.bundles" and will
     * be placed in the configuration area. The created file will be sent as provision url using scan-file scanner.
     *
     * @param configDirLocation path to configuration area directory
     * @param bundleModels      a map between bundle models (IPluginModelBase) and start
     *                          information (string in format startlevel:start)
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getProvisioningFile( final String configDirLocation, final Map bundleModels )
        throws CoreException
    {

        final List options = new ArrayList();

        if( bundleModels != null && bundleModels.size() > 0 )
        {
            final File provisioningFile = new Path( configDirLocation ).append( "runner.bundles" ).toFile();
            BufferedWriter writer = null;
            try
            {
                writer = new BufferedWriter( new FileWriter( provisioningFile ) );
                final Map bundles = new HashMap();
                final List modelsToBeJared = new ArrayList();
                Iterator iter = bundleModels.keySet().iterator();
                while( iter.hasNext() )
                {
                    final IPluginModelBase model = (IPluginModelBase) iter.next();
                    // skip ourself
                    if( model != null && !PLUGIN_ID.equals( model.getPluginBase().getId() ) )
                    {
                        File bundle = getInstallLocation( model );

                        if( bundle != null && bundle.isDirectory() )
                        {
                            modelsToBeJared.add( model );
                        }
                        else
                        {
                            bundles.put( bundle, model );
                        }
                    }
                }
                bundles.putAll( createBundleJars( configDirLocation, modelsToBeJared ) );
                iter = bundles.entrySet().iterator();
                while( iter.hasNext() )
                {
                    final Map.Entry entry = (Map.Entry) iter.next();
                    final File bundle = (File) entry.getKey();
                    final IPluginModelBase model = (IPluginModelBase) entry.getValue();
                    // if is still a directory, just skip it
                    if( bundle != null && !bundle.isDirectory() )
                    {
                        writer.write( bundle.getCanonicalFile().toURL().toExternalForm() );
                        writer.write(
                            getBundleStartLevelOptions(
                                ( (String) bundleModels.get( model ) ).split( ":" )
                            )
                        );
                        writer.newLine();
                    }
                }
                options.add( "scan-file:" + provisioningFile.getCanonicalFile().toURL().toExternalForm() );
            }
            catch( MalformedURLException e )
            {
                throw new CoreException( LauncherUtils
                    .createErrorStatus( "Cannot create environment file" )
                );
            }
            catch( IOException e )
            {
                throw new CoreException( LauncherUtils
                    .createErrorStatus( "Cannot create environment file" )
                );
            }
            finally
            {
                if( writer != null )
                {
                    try
                    {
                        writer.flush();
                        writer.close();
                    }
                    catch( IOException e )
                    {
                        // do nothing
                        PDECore.logException( e );
                    }
                }
            }
        }
        return options;
    }

    /**
     * Get the current install location for the plugin.
     *
     * @param model model of the plugin to be exported
     *
     * @return current install location of the plugin
     */
    private static File getInstallLocation( final IPluginModelBase model )
        throws IOException
    {
        File bundle = new File( model.getInstallLocation() );

        // optimization for read-only imported bundles (no need to re-bundle)
        File buildPropertiesFile = new File( bundle, "build.properties" );
        if( buildPropertiesFile.exists() )
        {
            Properties buildProperties = new Properties();
            FileInputStream in = new FileInputStream( buildPropertiesFile );
            buildProperties.load( in );
            in.close();

            String installLocation = buildProperties.getProperty( "install.location" );
            if( null != installLocation )
            {
                try
                {
                    File installedJar = new File( new URI( installLocation ) );
                    if( installedJar.isFile() )
                    {
                        bundle = installedJar;
                    }
                }
                catch( Exception e )
                {
                }
            }
        }

        return bundle;
    }

    /**
     * Creates the bundle jar for the models using PDE export functionality.
     *
     * @param configDirLocation path to configuration area directory
     * @param models            list of models of the plugins to be exported
     *
     * @return a map of exported bundles toegether with corrsponding plugin model
     *
     * @throws org.eclipse.core.runtime.CoreException
     *          in case of an InterruptedException while waiting for jar process
     *          to finish, or if the jar cannot be found after the process ends
     */
    private static Map createBundleJars( final String configDirLocation, final List models )
        throws CoreException
    {
        // prepare the information about bundles to be exported
        final FeatureExportInfo info = new FeatureExportInfo();
        info.toDirectory = true;
        info.useJarFormat = true;
        info.destinationDirectory = configDirLocation;
        info.items = models.toArray( new Object[models.size()] );
        // then schedule the export as a job
        final Job job = createExportPluginJob( info );
        job.setUser( true );
        job.schedule();
        job.setProperty( IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ );
        // and wait for export to finish
        try
        {
            job.join();
        }
        catch( InterruptedException e )
        {
            throw new CoreException( LauncherUtils.createErrorStatus( "Interrupted while creating bundles" ) );
        }
        // and finally find out the exported bundles
        final Map bundles = new HashMap();
        final Iterator iter = models.iterator();
        while( iter.hasNext() )
        {
            final IPluginModelBase model = (IPluginModelBase) iter.next();
            // find the generated bundle. This is done by using the id of the plugin
            // and version. And pray god it works :)

            // sometimes the version declared in the manifest is not correct.
            // for example 1.0.0.qualifier
            // in that case it seems that some type of timestamp is used: testosgi_1.0.0.200906041801.jar
            // 'qualifier' is a special case processed here: org.eclipse.pde.internal.build.BuildScriptGenerator#generateFeatures
            // and here org.eclipse.pde.internal.build.site.QualifierReplacer#replaceQualifierInVersion
            // for now we just look at the lastModified date on the located jars and use the most recent one.
            // TODO is there another way to find the generated bundle?
            String bundleFileName = null;

            if( model.getPluginBase().getVersion() != null
                && model.getPluginBase().getVersion().contains( "qualifier" ) )
            {
                final File[] files = new Path( configDirLocation )
                    .addTrailingSeparator()
                    .append( "plugins" )
                    .toFile()
                    .listFiles();

                File bestOne = null;
                for( int i = 0; i < files.length; i++ )
                {
                    File f = files[ i ];
                    if( f.isFile() && f.getName().startsWith( bundleFileName ) && f.getName().endsWith( ".jar" ) )
                    {
                        //look at the timestamp:
                        if( bestOne == null || bestOne.lastModified() <= f.lastModified() )
                        {
                            bestOne = f;//f is more recent
                        }
                    }
                }
                if( bestOne != null )
                {
                    bundleFileName = bestOne.getAbsolutePath();
                }
            }

            // as last resort (and in versions <= 3.4.2)
            if( bundleFileName == null )
            {
                //forced version. or 3.4 and older behavior:
                bundleFileName = new Path( configDirLocation )
                    .addTrailingSeparator()
                    .append( "plugins" )
                    .addTrailingSeparator()
                    .append( model.getPluginBase().getId() + "_" + model.getPluginBase().getVersion() )
                    .addFileExtension( "jar" )
                    .toFile()
                    .getAbsolutePath();
            }

            final File bundle = new File( bundleFileName );
            if( !bundle.exists() || !bundle.isFile() )
            {
                throw new CoreException(
                    LauncherUtils.createErrorStatus(
                        "Cannot locate generated bundle for " + model.getPluginBase().getId()
                        + " version " + model.getPluginBase().getVersion()
                    )
                );
            }
            bundles.put( bundle, model );
        }
        return bundles;
    }

    /**
     * Helper method to return the appropriate Job object that exports the plugin.
     * It is different in eclipse-3.4 and eclipse-3.5
     *
     * @param info
     */
    private static Job createExportPluginJob( FeatureExportInfo info )
        throws CoreException
    {
//    	//3.5 and after:
//        return new org.eclipse.pde.internal.core.exports.PluginExportOperation(info, "export for exec");
//        //before 3.5:
//        return new org.eclipse.pde.internal.ui.build.PluginExportJob(info);
        //also available in 3.4: return new org.eclipse.pde.internal.core.exports.PluginExportOperation(info);
        Throwable except = null;
        try
        {//look for the class specific to 3.4 first.
            Class PluginExportJobClass = Class.forName( "org.eclipse.pde.internal.ui.build.PluginExportJob" );
            Constructor cons = PluginExportJobClass.getConstructor( new Class[]{ FeatureExportInfo.class } );
            return (Job) cons.newInstance( new Object[]{ info } );
        }
        catch( ClassNotFoundException cnfe )
        {
            try
            {
                Class pluginExportOperationClass =
                    Class.forName( "org.eclipse.pde.internal.core.exports.PluginExportOperation" );
                Constructor cons =
                    pluginExportOperationClass.getConstructor( new Class[]{ FeatureExportInfo.class, String.class } );
                return (Job) cons.newInstance( new Object[]{ info, "Export OSGI Bundles for execution" } );
            }
            catch( Throwable t )
            {
                except = t;
            }
        }
        catch( Throwable t )
        {
            except = t;
        }
        throw new CoreException( new Status( IStatus.ERROR, "org.ops4j.pax.runner.eclipse.core",
                                             "Unable to locate the appropriate ExportPluginJob instance.", except
        )
        );
    }

    /**
     * Extracts start level options from plugin data.
     *
     * @param startData and array of two strings. First is the start level and second
     *                  the auto start.
     *
     * @return start level options ala Pax Runner
     */
    private static String getBundleStartLevelOptions( final String[] startData )
    {
        final StringBuffer startOptions = new StringBuffer();
        if( startData != null && startData.length > 0 )
        {
            // first is the start level
            if( startData[ 0 ] != null && startData[ 0 ].length() > 0 )
            {
                final String trimmed = startData[ 0 ].trim();
                // we skip default as pax runner handles
                // that by itself
                if( !"default".equals( trimmed ) )
                {
                    startOptions.append( "@" ).append( trimmed );
                }
            }
            // second is auto start
            if( startData.length > 1 && startData[ 1 ] != null
                && startData[ 1 ].length() > 0 )
            {
                final String trimmed = startData[ 1 ].trim();
                // we skip default as pax runner handles
                // that by itself
                if( !"default".equals( trimmed ) && !Boolean.valueOf( trimmed ).booleanValue() )
                {
                    startOptions.append( "@nostart" );
                }
            }
        }
        return startOptions.toString();
    }

    /**
     * Looks for the pax cursor run arguments configuration attribute. If Pax Cursor Tab is installed this configuration
     * argument is a list with Pax Runner ready to use options. So, if the list is present just use the list.
     *
     * @param configuration launch configuration
     *
     * @return list of options
     *
     * @throws CoreException re-thrown
     */
    public static List getPaxCursorTabOptions( final ILaunchConfiguration configuration )
        throws CoreException
    {

        List options = new ArrayList();

        try
        {
            options = configuration.getAttribute( "org.ops4j.pax.cursor.runArguments", new ArrayList() );
        }
        catch( CoreException ignore )
        {
            // DebugUIPlugin.log(ignore.getStatus());;
        }

        return options;
    }

}
