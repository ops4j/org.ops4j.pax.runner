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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.launcher.BundleLauncherHelper;
import org.eclipse.pde.internal.ui.launcher.LauncherUtils;
import org.eclipse.pde.internal.ui.launcher.VMHelper;
import org.eclipse.pde.ui.launcher.AbstractPDELaunchConfiguration;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;
import org.osgi.framework.Bundle;

/**
 * Pax Runner Eclipse Plugin Launch Configuraton ( Eclipse org.eclipse.pde.ui.osgiFrameworks extension point).
 *
 * @author Alin Dreghiciu
 * @since 0.1.0, Novermber 25, 2007
 */
public class LaunchConfiguration extends AbstractPDELaunchConfiguration
{

    /**
     * Adds the plugin itself to the class path, so it can be found by the process started by PDE.
     *
     * @see AbstractPDELaunchConfiguration#getClasspath(ILaunchConfiguration)
     */
    public String[] getClasspath( final ILaunchConfiguration configuration )
        throws CoreException
    {
        final List classpath = new ArrayList();
        // first find ourself
        classpath.add( getPaxCursorClasspath() );
        // and then take the original classpath, whatever that would be
        final String[] superClasspath = super.getClasspath( configuration );
        if( superClasspath != null )
        {
            for( int i = 0; i < superClasspath.length; i++ )
            {
                final String classpathEntry = superClasspath[ i ];
                if( classpathEntry != null )
                {
                    classpath.add( classpathEntry );
                }
            }
        }
        return (String[]) classpath.toArray( new String[classpath.size()] );
    }

    /**
     * Returns the classpath for Pax Runner Eclipse Plugin. First it looks for Pax Runner Eclipse plugin into the target
     * platform. If not found will fallback to finding the plugin into the platform itself. If still not found a core
     * exception is thrown to indicate to user that Pax Runner Eclipse Plugin is not available.
     *
     * @return classpath for Pax Runner Eclipse Plugin
     *
     * @throws org.eclipse.core.runtime.CoreException
     *          - If claspath cannopt be determined
     */
    private String getPaxCursorClasspath()
        throws CoreException
    {
        String classpath = null;
        // first try to find ourself and use the location for classpath
        final IPluginModelBase plugin = PluginRegistry.findModel( Utils.PLUGIN_ID );
        if( plugin != null )
        {
            classpath = plugin.getInstallLocation();
        }
        else
        {
            final Bundle bundle = Platform.getBundle( Utils.PLUGIN_ID );
            if( bundle != null )
            {
                try
                {
                    URL url = FileLocator.toFileURL( FileLocator.resolve( bundle.getEntry( "/" ) ) ); //$NON-NLS-1$
                    classpath = url.getFile();
                    if( classpath.startsWith( "file:" ) ) //$NON-NLS-1$
                    {
                        classpath = classpath.substring( 5 );
                    }
                    classpath = new File( classpath ).getAbsolutePath();
                    if( classpath.endsWith( "!" ) ) //$NON-NLS-1$
                    {
                        classpath = classpath.substring( 0, classpath.length() - 1 );
                    }
                }
                catch( IOException e )
                {
                    // do nothing now. We will throw later on.
                }
            }
        }
        if( classpath == null )
        {
            throw new CoreException(
                LauncherUtils.createErrorStatus( PDEUIMessages.WorkbenchLauncherConfigurationDelegate_noStartup )
            );
        }
        return classpath;
    }

    /**
     * Returns the list of arguments used to start Pax Runner.
     */
    public String[] getProgramArguments( final ILaunchConfiguration configuration )
        throws CoreException
    {

        final List options = new ArrayList();

        options.add( "--noDownloadFeedback" );
        options.add( "--absoluteFilePaths" );
        options.addAll( Utils.getUserOptions( super.getProgramArguments( configuration ) ) );
        options.addAll( Utils.getTargetPlatformOptions( configuration ) );
        options.addAll( Utils.getStartLevelOptions( configuration ) );
        options.addAll( Utils.getCleanOptions( configuration ) );
        options.addAll( Utils.getVMArgsOptions( configuration ) );
        options.addAll( Utils.getWorkingDirOptions( getWorkingDirectory( configuration ).getAbsolutePath() ) );
        options.addAll(
            Utils.getEnvPropertiesFile( getConfigDir( configuration ).toString(), getEnvironment( configuration ) )
        );
        options.addAll(
            Utils.getProvisioningFile( getConfigDir( configuration ).toString(),
                                       /*BundleLauncherHelper.*/getMergedMap( configuration )
            )
        );
        options.addAll( Utils.getPaxCursorTabOptions( configuration ) );

        return (String[]) options.toArray( new String[options.size()] );
    }
    
    /**
     * Helper method to call a static method that signature has changed between eclipse-3.4 and eclipse-3.5
     * @param configuration
     * @return
     * @throws CoreException
     */
	private static Map getMergedMap(ILaunchConfiguration configuration) throws CoreException {
//		//before 3.5:
//		return BundleLauncherHelper.getMergedMap(configuration);
//		//3.5 and after:
//		return BundleLauncherHelper.getMergedBundleMap(configuration, true);//for pax it is always osgi.
		Throwable except = null;
		try {
			try {
				Method getMergedMapMethod = BundleLauncherHelper.class.getMethod("getMergedMap", new Class[] {ILaunchConfiguration.class});
				if (getMergedMapMethod != null) {
					return (Map) getMergedMapMethod.invoke(null, new Object[] {configuration});
				}
			} catch (NoSuchMethodException nsme) {
				Method getMergedBundleMapMethod = BundleLauncherHelper.class.getMethod("getMergedBundleMap", new Class[] {ILaunchConfiguration.class, boolean.class});
				if (getMergedBundleMapMethod != null) {
					return (Map) getMergedBundleMapMethod.invoke(null, new Object[] {configuration, Boolean.TRUE});
				}
			}
		} catch (Throwable t) {
			except = t;
		}
		throw new CoreException(new Status(IStatus.ERROR, "org.ops4j.pax.runner.eclipse.core",
				"Unable to locate the appropriate BundleLauncherHelper.getMergedMap method.", except));
	}


    /**
     * Do not pass any VM argument as we pass them to Pax Runner as start option.
     *
     * @see Utils#getVMArgsOptions(ILaunchConfiguration)
     * @see AbstractPDELaunchConfiguration#getVMArguments(ILaunchConfiguration)
     */
    public String[] getVMArguments( final ILaunchConfiguration configuration )
    {
        return new String[]{ };
    }

    /**
     * @see AbstractPDELaunchConfiguration#launch(org.eclipse.debug.core.ILaunchConfiguration,
     *      String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
     */
    public IVMRunner getVMRunner( ILaunchConfiguration configuration, String mode )
        throws CoreException
    {
        IVMInstall launcher = VMHelper.createLauncher( configuration );
        final IVMRunner eclipseRunner = launcher.getVMRunner( mode );

        return new IVMRunner()
        {
            public void run( final VMRunnerConfiguration configuration, final ILaunch launch,
                             final IProgressMonitor monitor )
                throws CoreException
            {
                ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
                try
                {
                    Run.main( new JavaRunner()
                    {
                        public void exec( final String[] vmOptions,
                                          final String[] classpath,
                                          final String mainClass,
                                          final String[] programOptions,
                                          final String javaHome,
                                          final File workingDir )
                            throws PlatformException
                        {
                            VMRunnerConfiguration paxConfig = new VMRunnerConfiguration( mainClass, classpath );

                            paxConfig.setVMArguments( vmOptions );
                            paxConfig.setProgramArguments( programOptions );
                            paxConfig.setWorkingDirectory( configuration.getWorkingDirectory() );
                            paxConfig.setEnvironment( configuration.getEnvironment() );
                            paxConfig.setVMSpecificAttributesMap( configuration.getVMSpecificAttributesMap() );

                            try
                            {
                                eclipseRunner.run( paxConfig, launch, monitor );
                            }
                            catch( CoreException e )
                            {
                                throw new PlatformException( "Problem starting platform", e );
                            }
                        }
                    }, configuration.getProgramArguments()
                    );
                }
                catch( Exception e )
                {
                    throw new CoreException( LauncherUtils.createErrorStatus( e.getLocalizedMessage() ) );
                }
                finally
                {
                    Thread.currentThread().setContextClassLoader( oldCCL );
                }
            }
        };
    }

}
