/*
 * Copyright 2007 Alin Dreghiciu, Stuart McCulloch.
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
package org.ops4j.pax.runner.platform.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.xml.sax.SAXException;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.BundleReference;
import org.ops4j.pax.runner.platform.Configuration;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.LocalBundle;
import org.ops4j.pax.runner.platform.LocalSystemFile;
import org.ops4j.pax.runner.platform.Platform;
import org.ops4j.pax.runner.platform.PlatformBuilder;
import org.ops4j.pax.runner.platform.PlatformContext;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.SystemFileReference;
import org.ops4j.util.property.DictionaryPropertyResolver;
import org.ops4j.util.property.PropertyResolver;

/**
 * Handles the workflow of creating the platform. Concrete platforms should implement only the PlatformBuilder
 * interface.
 * TODO Add unit tests
 *
 * @author Alin Dreghiciu, Stuart McCulloch
 * @since August 19, 2007
 */
public class PlatformImpl
    implements Platform
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( PlatformImpl.class );
    /**
     * relative location of ee packages root.
     */
    private static final String EE_FILES_ROOT = "META-INF/platform/ee/";
    /**
     * Concrete platform builder as equinox, felix, kf.
     */
    private final PlatformBuilder m_platformBuilder;
    /**
     * PropertyResolver to be used.Injected to allow a Managed Service implementation.
     */
    private PropertyResolver m_propertyResolver;

    /**
     * Creates a new platform.
     *
     * @param platformBuilder concrete platform builder; mandatory
     */
    public PlatformImpl( final PlatformBuilder platformBuilder )
    {
        NullArgumentException.validateNotNull( platformBuilder, "Platform builder" );
        m_platformBuilder = platformBuilder;
    }

    /**
     * Sets the propertyResolver to use.
     *
     * @param propertyResolver a propertyResolver
     */
    public void setResolver( final PropertyResolver propertyResolver )
    {
        m_propertyResolver = propertyResolver;
    }

    /**
     * @see Platform#start(java.util.List,java.util.List,java.util.Properties,Dictionary,JavaRunner)
     */
    public void start( final List<SystemFileReference> systemFiles, final List<BundleReference> bundles,
                       final Properties properties, final Dictionary config, final JavaRunner javaRunner )
        throws PlatformException
    {
        LOGGER.info( "Preparing framework [" + this + "]" );

        // we should fail fast so let's do first what is easy
        final String mainClassName = m_platformBuilder.getMainClassName();
        if( mainClassName == null || mainClassName.trim().length() == 0 )
        {
            throw new PlatformException( "Main class of the platform cannot be null or empty" );
        }
        // create context
        final PlatformContext context = mandatory( "Platform context", createPlatformContext() );
        // set platform properties
        context.setProperties( properties );
        // create a configuration to look up the rest of the properties
        final Configuration configuration = mandatory( "Configuration", createConfiguration( config ) );
        context.setConfiguration( configuration );
        // create the platform definition from the configured url or from the platform builder
        final PlatformDefinition definition = mandatory( "Definition", createPlatformDefinition( configuration ) );
        LOGGER.debug( "Using platform definition [" + definition + "]" );
        // check out if the wrking folder must be removed first (clean start)
        final Boolean clean = configuration.isCleanStart();
        if( clean != null && clean )
        {
            FileUtils.delete( new File( configuration.getWorkingDirectory() ) );
        }
        // create a working directory on the file system
        final File workDir = mandatory( "Working dir", createWorkingDir( configuration.getWorkingDirectory() ) );
        LOGGER.debug( "Using working directory [" + workDir + "]" );
        context.setWorkingDirectory( workDir );
        final Boolean overwriteBundles = configuration.isOverwrite();
        final Boolean overwriteUserBundles = configuration.isOverwriteUserBundles();
        final Boolean overwriteSystemBundles = configuration.isOverwriteSystemBundles();
        final Boolean downloadFeeback = configuration.isDownloadFeedback();
        LOGGER.info( "Downloading bundles..." );
        // download system package
        LOGGER.debug( "Download system package" );
        final File systemFile = downloadSystemFile(
            workDir, definition, overwriteBundles || overwriteSystemBundles, downloadFeeback
        );
        LOGGER.debug( "Download additional system libraries" );
        final List<LocalSystemFile> locaSystemFiles =
            downloadSystemFiles( workDir, systemFiles, overwriteBundles || overwriteSystemBundles, downloadFeeback );
        // download the rest of the bundles
        final List<LocalBundle> bundlesToInstall = new ArrayList<LocalBundle>();
        LOGGER.debug( "Download platform bundles" );
        bundlesToInstall.addAll(
            downloadPlatformBundles(
                workDir, definition, context, overwriteBundles || overwriteSystemBundles, downloadFeeback
            )
        );
        LOGGER.debug( "Download bundles" );
        bundlesToInstall.addAll(
            downloadBundles(
                workDir, bundles, overwriteBundles || overwriteUserBundles, downloadFeeback
            )
        );
        context.setBundles( bundlesToInstall );
        final ExecutionEnvironment ee = new ExecutionEnvironment( configuration.getExecutionEnvironment() );
        context.setSystemPackages(
            createPackageList( ee.getSystemPackages(), configuration.getSystemPackages(), definition.getPackages() )
        );
        context.setExecutionEnvironment( ee.getExecutionEnvironment() );

        // and then ask the platform builder to prepare platform for start up (e.g. create configuration file)
        m_platformBuilder.prepare( context );

        final CommandLineBuilder vmOptions = new CommandLineBuilder();
        vmOptions.append( configuration.getVMOptions() );
        vmOptions.append( m_platformBuilder.getVMOptions( context ) );

        final String[] classpath = buildClassPath( systemFile, locaSystemFiles, configuration );

        final CommandLineBuilder programOptions = new CommandLineBuilder();
        programOptions.append( m_platformBuilder.getArguments( context ) );
        programOptions.append( getFrameworkOptions() );

        JavaRunner runner = javaRunner;
        if( runner == null )
        {
            runner = new DefaultJavaRunner();
        }
        final String javaHome = configuration.getJavaHome();

        LOGGER.debug( "Using " + runner.getClass() + " [" + mainClassName + "]" );
        LOGGER.debug( "VM options:       [" + Arrays.toString( vmOptions.toArray() ) + "]" );
        LOGGER.debug( "Classpath:        [" + Arrays.toString( classpath ) + "]" );
        LOGGER.debug( "Platform options: [" + Arrays.toString( programOptions.toArray() ) + "]" );
        LOGGER.debug( "Java home:        [" + javaHome + "]" );
        LOGGER.debug( "Working dir:      [" + workDir + "]" );

        runner.exec(
            vmOptions.toArray(),
            classpath,
            mainClassName,
            programOptions.toArray(),
            javaHome,
            workDir
        );
    }

    /**
     * Builds the classpath java startup option out of specified system files (prepended/appended), framework jar and
     * classpath option.
     *
     * @param systemFile    framework system files
     * @param systemFiles   local system files references
     * @param configuration configuration to get the classpath option
     *
     * @return array of classpath entries
     */
    private String[] buildClassPath( final File systemFile,
                                     final List<LocalSystemFile> systemFiles,
                                     final Configuration configuration )
    {
        final StringBuilder prepend = new StringBuilder();
        final StringBuilder append = new StringBuilder();
        for( LocalSystemFile ref : systemFiles )
        {
            if( ref.getSystemFileReference().shouldPrepend() )
            {
                if( prepend.length() != 0 )
                {
                    prepend.append( File.pathSeparator );
                }
                prepend.append( ref.getFile().getAbsolutePath() );
            }
            else
            {
                if( append.length() != 0 )
                {
                    append.append( File.pathSeparator );
                }
                append.append( ref.getFile().getAbsolutePath() );
            }
        }
        if( prepend.length() != 0 )
        {
            prepend.append( File.pathSeparator );
        }
        if( append.length() != 0 )
        {
            append.insert( 0, File.pathSeparator );
        }
        final StringBuilder classPath = new StringBuilder();
        classPath.append( prepend );
        classPath.append( systemFile.getAbsolutePath() );
        classPath.append( append );
        classPath.append( configuration.getClasspath() );

        return classPath.toString().split( File.pathSeparator );
    }

    /**
     * Returns an array of framework options if set as system properties.
     *
     * @return framework options.
     */
    private String[] getFrameworkOptions()
    {
        // TODO check out if this should not be a platform start parameter
        // TODO what the hack are framework options?
        String[] options = new String[0];
        final String property = System.getProperty( "FRAMEWORK_OPTS" );
        if( property != null )
        {
            options = property.split( " " );
        }
        return options;
    }

    /**
     * Downloads the bundles that will be installed to the working directory.
     *
     * @param bundles         url of bundles to be installed
     * @param workDir         the directory where to download bundles
     * @param overwrite       if the bundles should be overwritten
     * @param downloadFeeback whether or not downloading process should display fne grained progres info
     *
     * @return a list of downloaded files
     *
     * @throws PlatformException re-thrown
     */
    private List<LocalBundle> downloadBundles( final File workDir, final List<BundleReference> bundles,
                                               final Boolean overwrite, final boolean downloadFeeback )
        throws PlatformException
    {
        final List<LocalBundle> localBundles = new ArrayList<LocalBundle>();
        if( bundles != null )
        {
            for( BundleReference reference : bundles )
            {
                final URL url = reference.getURL();
                if( url == null )
                {
                    throw new PlatformException( "Invalid url in bundle refrence [" + reference + "]" );
                }
                localBundles.add(
                    new LocalBundleImpl(
                        reference,
                        download(
                            workDir,
                            url,
                            reference.getName(),
                            overwrite || reference.shouldUpdate(),
                            true,
                            downloadFeeback
                        )
                    )
                );
            }
        }
        return localBundles;
    }

    /**
     * Downsloads platform bundles to working dir.
     *
     * @param workDir         the directory where to download bundles
     * @param definition      to take the system package
     * @param platformContext current platform context
     * @param overwrite       if the bundles should be overwritten
     * @param downloadFeeback whether or not downloading process should display fne grained progres info
     *
     * @return a list of downloaded files
     *
     * @throws PlatformException re-thrown
     */
    private List<LocalBundle> downloadPlatformBundles( final File workDir, final PlatformDefinition definition,
                                                       final PlatformContext platformContext, final Boolean overwrite,
                                                       final boolean downloadFeeback )
        throws PlatformException
    {
        final StringBuilder profiles = new StringBuilder();
        final String userProfiles = platformContext.getConfiguration().getProfiles();
        if( userProfiles != null && userProfiles.trim().length() > 0 )
        {
            profiles.append( userProfiles );
        }
        final String builderProfile = m_platformBuilder.getRequiredProfile( platformContext );
        if( builderProfile != null && builderProfile.trim().length() > 0 )
        {
            if( profiles.length() > 0 )
            {
                profiles.append( "," );
            }
            profiles.append( builderProfile );
        }
        return downloadBundles(
            workDir, definition.getPlatformBundles( profiles.toString() ), overwrite, downloadFeeback
        );
    }

    /**
     * Downloads the system file.
     *
     * @param workDir         the directory where to download bundles
     * @param definition      to take the system package
     * @param overwrite       if the bundles should be overwritten
     * @param downloadFeeback whether or not downloading process should display fne grained progres info
     *
     * @return the system file
     *
     * @throws PlatformException re-thrown
     */
    private File downloadSystemFile( final File workDir, final PlatformDefinition definition, final Boolean overwrite,
                                     final boolean downloadFeeback )
        throws PlatformException
    {
        return download(
            workDir, definition.getSystemPackage(), definition.getSystemPackageName(), overwrite, false, downloadFeeback
        );
    }

    /**
     * Downloads additional system files that will be added to the classpath.
     *
     * @param workDir         the directory where to download bundles
     * @param systemFiles     list of system files references
     * @param overwrite       if the systemFiles should be overwritten
     * @param downloadFeeback whether or not downloading process should display fne grained progres info
     *
     * @return the system file
     *
     * @throws PlatformException re-thrown
     */
    private List<LocalSystemFile> downloadSystemFiles( final File workDir, final List<SystemFileReference> systemFiles,
                                                       final Boolean overwrite, final boolean downloadFeeback )
        throws PlatformException
    {
        final List<LocalSystemFile> downloaded = new ArrayList<LocalSystemFile>();
        if( systemFiles != null )
        {
            for( SystemFileReference reference : systemFiles )
            {
                downloaded.add(
                    new LocalSystemFileImpl(
                        reference,
                        download( workDir, reference.getURL(), reference.getName(), overwrite, false, downloadFeeback )
                    )
                );
            }
        }
        return downloaded;
    }

    /**
     * Downloads files from urls.
     *
     * @param workDir         the directory where to download bundles
     * @param url             of the file to be downloaded
     * @param displayName     to be shown during download
     * @param overwrite       if the bundles should be overwritten
     * @param checkAttributes whether or not to check attributes in the manifest
     * @param downloadFeeback whether or not downloading process should display fne grained progres info
     *
     * @return the File corresponding to the downloaded file.
     *
     * @throws PlatformException if the url could not be downloaded
     */
    private File download( final File workDir, final URL url, final String displayName, final Boolean overwrite,
                           final boolean checkAttributes, final boolean downloadFeeback )
        throws PlatformException
    {
        LOGGER.debug( "Downloading [" + url + "]" );
        File downloadedBundlesFile = new File( workDir, "bundles/downloaded_bundles.properties" );
        Properties fileNamesForUrls = loadProperties( downloadedBundlesFile );

        String downloadedFileName = fileNamesForUrls.getProperty( url.toExternalForm() );
        String hashFileName = "" + url.toExternalForm().hashCode();
        if( downloadedFileName == null )
        {
            // destination will be made based on the hashcode of the url to be downloaded
            downloadedFileName = hashFileName + ".jar";

        }
        File destination = new File( workDir, "bundles/" + downloadedFileName );

        // download the bundle only if is a forced overwrite or the file does not exist or the file is there but is
        // invalid
        boolean forceOverwrite = overwrite || !destination.exists();
        if( !forceOverwrite )
        {
            try
            {
                String newFileName = validateBundleAndGetFilename( url, destination, hashFileName, checkAttributes );
                if( !destination.getName().equals( newFileName ) )
                {
                    throw new PlatformException( "File " + destination + " should have name " + newFileName );
                }
            }
            catch( PlatformException ignore )
            {
                forceOverwrite = true;
            }
        }
        if( forceOverwrite )
        {
            try
            {
                LOGGER.debug( "Creating new file at destination: " + destination.getAbsolutePath() );
                destination.getParentFile().mkdirs();
                destination.createNewFile();
                BufferedOutputStream os = null;
                try
                {
                    os = new BufferedOutputStream( new FileOutputStream( destination ) );
                    StreamUtils.ProgressBar progressBar = null;
                    if( LOGGER.isInfoEnabled() )
                    {
                        if( downloadFeeback )
                        {
                            progressBar = new StreamUtils.FineGrainedProgressBar( displayName );
                        }
                        else
                        {
                            progressBar = new StreamUtils.CoarseGrainedProgressBar( displayName );
                        }
                    }
                    StreamUtils.streamCopy( url, os, progressBar );
                    LOGGER.debug( "Succesfully downloaded to [" + destination + "]" );
                }
                finally
                {
                    if( os != null )
                    {
                        os.close();
                    }
                }
            }
            catch( IOException e )
            {
                throw new PlatformException( "[" + url + "] could not be downloaded", e );
            }
        }

        wrapNonBundleJar( destination, url );
        String newFileName = validateBundleAndGetFilename( url, destination, hashFileName, checkAttributes );
        File newDestination = new File( destination.getParentFile(), newFileName );
        if( !newFileName.equals( destination.getName() ) )
        {
            if( newDestination.exists() )
            {
                if( !newDestination.delete() )
                {
                    throw new PlatformException( "Cannot delete " + newDestination );
                }
            }
            if( !destination.renameTo( newDestination ) )
            {
                throw new PlatformException( "Cannot rename " + destination + " to " + newDestination );
            }
            fileNamesForUrls.setProperty( url.toExternalForm(), newFileName );
            saveProperties( fileNamesForUrls, downloadedBundlesFile );
        }

        return newDestination;
    }

    private Properties loadProperties( File file )
    {
        Properties properties = new Properties();
        FileInputStream in = null;
        try
        {
            in = new FileInputStream( file );
            properties.load( in );
            return properties;
        }
        catch( IOException e )
        {
            return properties;
        }
        finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                }
                catch( IOException e )
                {
                    // ignore
                }
            }
        }
    }

    private void saveProperties( Properties properties, File file )
        throws PlatformException
    {
        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream( file );
            properties.store( os, "" );
        }
        catch( IOException e )
        {
            throw new PlatformException( "Cannot store properties " + file, e );
        }
        finally
        {
            if( os != null )
            {
                try
                {
                    os.close();
                }
                catch( IOException e )
                {
                    // ignore
                }
            }

        }
    }

    /**
     * Validate that the file is an valid bundle. A valid bundle will be a loadable jar file that has manifest and the
     * manifest contains at least an entry for Bundle-SymboliName.
     *
     * @param url                       original url from where the bundle was created.
     * @param file                      file to be validated
     * @param defaultBundleSymbolicName default bundle symbolic name to be used if manifest does not have a bundle
     *                                  symbolic name
     * @param checkAttributes           whether or not to check attributes in the manifest
     *
     * @return file name based on bundle symbolic name and version
     *
     * @throws PlatformException if the jar is not a valid bundle
     */
    String validateBundleAndGetFilename( final URL url,
                                         final File file,
                                         final String defaultBundleSymbolicName,
                                         final boolean checkAttributes )
        throws PlatformException
    {
        JarFile jar = null;
        try
        {
            // verify that is a valid jar. Do not verify that is signed (the false param).
            jar = new JarFile( file, false );
            final Manifest manifest = jar.getManifest();
            if( manifest == null )
            {
                throw new PlatformException( "[" + url + "] is not a valid bundle" );
            }
            String bundleSymbolicName = manifest.getMainAttributes().getValue( Constants.BUNDLE_SYMBOLICNAME );
            String bundleName = manifest.getMainAttributes().getValue( Constants.BUNDLE_NAME );
            String bundleVersion = manifest.getMainAttributes().getValue( Constants.BUNDLE_VERSION );
            if( checkAttributes )
            {
                if( bundleSymbolicName == null && bundleName == null )
                {
                    throw new PlatformException( "[" + url + "] is not a valid bundle" );
                }
            }
            if( bundleSymbolicName == null )
            {
                bundleSymbolicName = defaultBundleSymbolicName;
            }
            else
            {
                // remove directives like "; singleton:=true"  
                int semicolonPos = bundleSymbolicName.indexOf( ";" );
                if( semicolonPos > 0 )
                {
                    bundleSymbolicName = bundleSymbolicName.substring( 0, semicolonPos );
                }
            }
            if( bundleVersion == null )
            {
                bundleVersion = "0.0.0";
            }
            return bundleSymbolicName + "_" + bundleVersion + ".jar";
        }
        catch( IOException e )
        {
            throw new PlatformException( "[" + url + "] is not a valid bundle", e );
        }
        finally
        {
            if( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch( IOException ignore )
                {
                    // just ignore as this is less probably to happen.
                }
            }
        }
    }

    /**
     * This pipes
     *
     * @param file
     * @param url  the original url for reference purposes
     */
    private void wrapNonBundleJar( File file, URL url )
        throws PlatformException
    {
        JarFile jar = null;
        try
        {
            // verify that is a valid jar. Do not verify that is signed (the false param).

            jar = new JarFile( file, false );
            final Manifest manifest = jar.getManifest();
            if( manifest == null )
            {
                wrap( file, url );
            }
            String bundleSymbolicName = manifest.getMainAttributes().getValue( Constants.BUNDLE_SYMBOLICNAME );

            if( bundleSymbolicName == null )
            {
                wrap( file, url );
            }


        }
        catch( IOException e )
        {
           throw new PlatformException( "[" + url + "] is not a jar.", e );
        }
        finally
        {
            if( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch( IOException ignore )
                {
                    // just ignore as this is less probably to happen.
                }
            }
        }
    }

    private void wrap( File file, URL url )
        throws PlatformException
    {
        BufferedOutputStream bout = null;
        BufferedInputStream bin = null;

        try
        {
            LOGGER.debug( "Auto .. [" + url + "] .. " );
            String symbolicName = url.toExternalForm().replaceAll( "[^a-zA-Z_0-9.-]", "_" );
            URL wrapped = new URL( "wrap:" + file.toURL().toExternalForm() + "$Bundle-SymbolicName=" + symbolicName );

            File tmp = File.createTempFile( file.getName(), "tmp" );
            bout = new BufferedOutputStream( new FileOutputStream( tmp ) );
            StreamUtils.streamCopy( wrapped.openStream(), bout, null );
            bout.close();

            // write it back to original location (overwrite)
            bout = new BufferedOutputStream( new FileOutputStream( file ) );
            bin = new BufferedInputStream( new FileInputStream( tmp ) );
            StreamUtils.streamCopy( bin, bout, null );
            bout.close();

            // delete temporary file
            tmp.delete();

            LOGGER.debug( "Automatically wrapped [" + url + "] to a bundle." );
        }
        catch( IOException e )
        {
             throw new PlatformException( "Tried to convert [" + url + "] to a bundle but failed.", e );
        }
        finally
        {
            try
            {
                if( bin != null )
                {
                    bin.close();
                }

            }
            catch( IOException ioE )
            {

            }

            try
            {
                if( bout != null )
                {
                    bout.close();
                }

            }
            catch( IOException ioE )
            {

            }
        }
    }

    /**
     * Creates a working directory.
     *
     * @param path path to working directory.
     *
     * @return a working directory.
     */
    private File createWorkingDir( final String path )
    {
        File workDir = new File( path );
        workDir.mkdirs();
        return workDir;
    }

    /**
     * Returns a comma separated list of system packages, constructed from:<br/>
     * 1. execution envoronment packages<br/>
     * 2. + additional packages from systemPackages option<br/>
     * 3. + list of packages contributed by the platform (see felix case)
     *
     * @param eePackages       execution environment packages
     * @param userPackages     user defined packages
     * @param platformPackages platform defined packages
     *
     * @return comma separated list of packages
     */
    private String createPackageList( final String eePackages,
                                      final String userPackages,
                                      final String platformPackages )
    {
        final StringBuilder packages = new StringBuilder();
        packages.append( eePackages );

        // append used defined packages
        if( userPackages != null && userPackages.trim().length() > 0 )
        {
            if( packages.length() > 0 )
            {
                packages.append( "," );
            }
            packages.append( userPackages );
        }
        // append platform specific packages
        if( platformPackages != null && platformPackages.trim().length() > 0 )
        {
            if( packages.length() > 0 )
            {
                packages.append( "," );
            }
            packages.append( platformPackages );
        }
        return packages.toString();
    }

    /**
     * Configuration factory method.
     *
     * @param config service configuration properties
     *
     * @return a configuration
     */
    Configuration createConfiguration( final Dictionary config )
    {
        PropertyResolver propertyResolver = m_propertyResolver;
        if( config != null )
        {
            propertyResolver = new DictionaryPropertyResolver( config, m_propertyResolver );
        }
        return new ConfigurationImpl( propertyResolver );
    }

    /**
     * Platform definition factory method. First looks for a configured definition url. If not found will use the
     * platform builder.
     *
     * @param configuration a platform configuration
     *
     * @return a platform definition
     *
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *          in case of an invalid platform definition
     */
    PlatformDefinition createPlatformDefinition( final Configuration configuration )
        throws PlatformException
    {
        NullArgumentException.validateNotNull( configuration, "Configuration" );
        try
        {
            final URL definitionURL = configuration.getDefinitionURL();
            InputStream inputStream = null;
            if( definitionURL != null )
            {
                inputStream = definitionURL.openStream();
            }
            if( inputStream == null )
            {
                inputStream = m_platformBuilder.getDefinition();
            }
            return new PlatformDefinitionImpl( inputStream, configuration.getProfileStartLevel() );
        }
        catch( IOException e )
        {
            throw new PlatformException( "Invalid platform definition", e );
        }
        catch( ParserConfigurationException e )
        {
            throw new PlatformException( "Invalid platform definition", e );
        }
        catch( SAXException e )
        {
            throw new PlatformException( "Invalid platform definition", e );
        }
    }

    /**
     * Platform context context factory methods.
     *
     * @return a platform context.
     */
    PlatformContext createPlatformContext()
    {
        return new PlatformContextImpl();
    }

    /**
     * Check if the variable is not null. If null throw an illegal argument exception.
     *
     * @param object the object to be checked
     * @param name   a nem to be used when making up the exception message
     *
     * @return the passed object if not null
     */
    private <T> T mandatory( final String name, final T object )
    {
        if( object == null )
        {
            throw new IllegalStateException( name + " cannot be null" );
        }
        return object;
    }

    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return m_platformBuilder.toString();
    }

}
