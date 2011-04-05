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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.*;
import org.ops4j.util.property.DictionaryPropertyResolver;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.Constants;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
    public void start( final List<SystemFileReference> systemFiles,
                       final List<BundleReference> bundles,
                       final Properties properties,
                       final Dictionary config,
                       final JavaRunner javaRunner )
        throws PlatformException
    {
        LOGGER.info( "Preparing framework [" + this + "]" );

        // we should fail fast so let's do first what is easy
        final String mainClassName = m_platformBuilder.getMainClassName();
        if ( mainClassName == null || mainClassName.trim().length() == 0 )
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

        // create a working directory on the file system
        final File workDir = mandatory( "Working dir", createWorkingDir( configuration.getWorkingDirectory() ) );
        LOGGER.debug( "Using working directory [" + workDir + "]" );

        context.setWorkingDirectory( workDir );
        // set file path strategy
        if ( configuration.useAbsoluteFilePaths() )
        {
            context.setFilePathStrategy( new AbsoluteFilePathStrategy() );
        }
        else
        {
            context.setFilePathStrategy( new RelativeFilePathStrategy( workDir ) );
        }

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
        final List<LocalSystemFile> localSystemFiles = downloadSystemFiles(
            workDir, systemFiles, overwriteBundles || overwriteSystemBundles, downloadFeeback
        );
        // download the rest of the bundles
        final List<BundleReference> bundlesToInstall = new ArrayList<BundleReference>();
        LOGGER.debug( "Download platform bundles" );
        bundlesToInstall.addAll(
            downloadPlatformBundles(
                workDir,
                definition,
                context,
                overwriteBundles || overwriteSystemBundles,
                downloadFeeback,
                configuration.validateBundles(),
                configuration.skipInvalidBundles()
            )
        );
        LOGGER.debug( "Download bundles" );
        bundlesToInstall.addAll(
            downloadBundles(
                workDir,
                bundles,
                overwriteBundles || overwriteUserBundles,
                downloadFeeback,
                configuration.isAutoWrap(),
                configuration.keepOriginalUrls(),
                configuration.validateBundles(),
                configuration.skipInvalidBundles()
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
        if ( configuration.keepOriginalUrls() )
        {
            vmOptions.append( "-Djava.protocol.handler.pkgs=org.ops4j.pax.url" );
        }

        final String[] classpath = buildClassPath( systemFile, localSystemFiles, configuration, context );

        final CommandLineBuilder programOptions = new CommandLineBuilder();
        programOptions.append( m_platformBuilder.getArguments( context ) );
        programOptions.append( getFrameworkOptions() );

        JavaRunner runner = javaRunner;
        if ( runner == null )
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
     * @param context       platform context
     *
     * @return array of classpath entries
     */
    private String[] buildClassPath( final File systemFile,
                                     final List<LocalSystemFile> systemFiles,
                                     final Configuration configuration,
                                     final PlatformContext context )
    {
        final StringBuilder prepend = new StringBuilder();
        final StringBuilder append = new StringBuilder();

        for ( LocalSystemFile ref : systemFiles )
        {
            if ( ref.getSystemFileReference().shouldPrepend() )
            {
                if ( prepend.length() != 0 )
                {
                    prepend.append( File.pathSeparator );
                }
                prepend.append( context.getFilePathStrategy().normalizeAsPath( ref.getFile() ) );
            }
            else
            {
                if ( append.length() != 0 )
                {
                    append.append( File.pathSeparator );
                }
                append.append( context.getFilePathStrategy().normalizeAsPath( ref.getFile() ) );
            }
        }
        if ( prepend.length() != 0 )
        {
            prepend.append( File.pathSeparator );
        }
        if ( append.length() != 0 )
        {
            append.insert( 0, File.pathSeparator );
        }
        final StringBuilder classPath = new StringBuilder();
        classPath.append( prepend );
        classPath.append( context.getFilePathStrategy().normalizeAsPath( systemFile ) );
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
        if ( property != null )
        {
            options = property.split( " " );
        }
        return options;
    }

    /**
     * Downloads the bundles that will be installed to the working directory.
     *
     * @param bundles            url of bundles to be installed
     * @param workDir            the directory where to download bundles
     * @param overwrite          if the bundles should be overwritten
     * @param downloadFeeback    whether or not downloading process should display fne grained progres info
     * @param autoWrap           wheather or not auto wrapping should take place
     * @param keepOriginalUrls   if the provisioned bundles should be cached or not
     * @param validateBundles    if downloaded bundles osgi headers should be checked
     * @param skipInvalidBundles if invalid bundles (failing validation) should be skipped
     *
     * @return a list of downloaded files
     *
     * @throws PlatformException re-thrown
     */
    private List<BundleReference> downloadBundles( final File workDir,
                                                   final List<BundleReference> bundles,
                                                   final Boolean overwrite,
                                                   final boolean downloadFeeback,
                                                   final boolean autoWrap,
                                                   final boolean keepOriginalUrls,
                                                   final boolean validateBundles,
                                                   final boolean skipInvalidBundles )
        throws PlatformException
    {
        final List<BundleReference> localBundles = new ArrayList<BundleReference>();
        if ( bundles != null )
        {
            for ( BundleReference reference : bundles )
            {
                URL url = reference.getURL();
                if ( url == null )
                {
                    throw new PlatformException( "Invalid url in bundle reference [" + reference + "]" );
                }
                if ( autoWrap )
                {
                    try
                    {
                        final String urlToWrap = url.toExternalForm();
                        if ( !urlToWrap.startsWith( "wrap:" ) )
                        {
                            url = new URL( "wrap:" + urlToWrap );
                        }
                    }
                    catch ( MalformedURLException e )
                    {
                        LOGGER.warn( "Could not auto wrap url [" + url + "] due to: " + e.getMessage() );
                    }
                }
                if ( keepOriginalUrls )
                {
                    localBundles.add( reference );
                }
                else
                {
                    final File bundleFile = download(
                        workDir,
                        url,
                        reference.getName(),
                        overwrite || reference.shouldUpdate(),
                        validateBundles,
                        !skipInvalidBundles,
                        downloadFeeback
                    );
                    if ( bundleFile != null )
                    {
                        localBundles.add( new LocalBundleReference( reference, bundleFile ) );
                    }
                    else
                    {
                        LOGGER.info( "Bundle [" + url + "] skipped from provisioning as it is invalid" );
                    }
                }
            }
        }
        return localBundles;
    }

    /**
     * Downsloads platform bundles to working dir.
     *
     * @param workDir            the directory where to download bundles
     * @param definition         to take the system package
     * @param platformContext    current platform context
     * @param overwrite          if the bundles should be overwritten
     * @param downloadFeeback    whether or not downloading process should display fine grained progres info
     * @param validateBundles    if downloaded bundles osgi headers should be checked
     * @param skipInvalidBundles if invalid bundles (failing validation) should be skipped
     *
     * @return a list of downloaded files
     *
     * @throws PlatformException re-thrown
     */
    private List<BundleReference> downloadPlatformBundles( final File workDir,
                                                           final PlatformDefinition definition,
                                                           final PlatformContext platformContext,
                                                           final Boolean overwrite,
                                                           final boolean downloadFeeback,
                                                           final boolean validateBundles,
                                                           final boolean skipInvalidBundles )
        throws PlatformException
    {
        final StringBuilder profiles = new StringBuilder();
        final String userProfiles = platformContext.getConfiguration().getProfiles();
        if ( userProfiles != null && userProfiles.trim().length() > 0 )
        {
            profiles.append( userProfiles );
        }
        final String builderProfile = m_platformBuilder.getRequiredProfile( platformContext );
        if ( builderProfile != null && builderProfile.trim().length() > 0 )
        {
            if ( profiles.length() > 0 )
            {
                profiles.append( "," );
            }
            profiles.append( builderProfile );
        }
        return downloadBundles(
            workDir,
            definition.getPlatformBundles( profiles.toString() ),
            overwrite,
            downloadFeeback,
            false, // do not autowrap, as framework related bundles are mostly alreay bundles,
            false, // framework bundles are always downloaded
            validateBundles,
            skipInvalidBundles
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
    private File downloadSystemFile( final File workDir,
                                     final PlatformDefinition definition,
                                     final Boolean overwrite,
                                     final boolean downloadFeeback )
        throws PlatformException
    {
        return download(
            workDir,
            definition.getSystemPackage(),
            definition.getSystemPackageName(),
            overwrite,
            false, // do not validate as osgi bundle
            true,  // fail on validation
            downloadFeeback
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
    private List<LocalSystemFile> downloadSystemFiles( final File workDir,
                                                       final List<SystemFileReference> systemFiles,
                                                       final Boolean overwrite,
                                                       final boolean downloadFeeback )
        throws PlatformException
    {
        final List<LocalSystemFile> downloaded = new ArrayList<LocalSystemFile>();
        if ( systemFiles != null )
        {
            for ( SystemFileReference reference : systemFiles )
            {
                downloaded.add(
                    new LocalSystemFileImpl(
                        reference,
                        download(
                            workDir,
                            reference.getURL(),
                            reference.getName(),
                            overwrite,
                            false, // do not validate as osgi bundle
                            true,  // fail on validation
                            downloadFeeback
                        )
                    )
                );
            }
        }
        return downloaded;
    }

    /**
     * Downloads files from urls.
     *
     * @param workDir          the directory where to download bundles
     * @param url              of the file to be downloaded
     * @param displayName      to be shown during download
     * @param overwrite        if the bundles should be overwritten
     * @param checkAttributes  whether or not to check attributes in the manifest
     * @param failOnValidation if validation fails should or not fail with an exception (or just return null)
     * @param downloadFeeback  whether or not downloading process should display fine grained progres info
     *
     * @return the File corresponding to the downloaded file, or null if the bundle is invalid (not an osgi bundle)
     *
     * @throws PlatformException if the url could not be downloaded
     */
    private File download( final File workDir,
                           final URL url,
                           final String displayName,
                           final Boolean overwrite,
                           final boolean checkAttributes,
                           final boolean failOnValidation,
                           final boolean downloadFeeback )
        throws PlatformException
    {
        LOGGER.debug( "Downloading [" + url + "]" );
        File downloadedBundlesFile = new File( workDir, "bundles/downloaded_bundles.properties" );
        Properties fileNamesForUrls = loadProperties( downloadedBundlesFile );

        String downloadedFileName = fileNamesForUrls.getProperty( url.toExternalForm() );
        String hashFileName = "" + url.toExternalForm().hashCode();
        if ( downloadedFileName == null )
        {
            // destination will be made based on the hashcode of the url to be downloaded
            downloadedFileName = hashFileName + ".jar";

        }
        File destination = new File( workDir, "bundles/" + downloadedFileName );

        // download the bundle only if is a forced overwrite or the file does not exist or the file is there but is
        // invalid
        boolean forceOverwrite = overwrite || !destination.exists();
        if ( !forceOverwrite )
        {
            try
            {
                String cachingName = determineCachingName( destination, hashFileName );
                if ( !destination.getName().equals( cachingName ) )
                {
                    throw new PlatformException( "File " + destination + " should have name " + cachingName );
                }
            }
            catch ( PlatformException ignore )
            {
                forceOverwrite = true;
            }
        }
        if ( forceOverwrite )
        {
            try
            {
                LOGGER.debug( "Creating new file at destination: " + destination.getAbsolutePath() );
                destination.getParentFile().mkdirs();
                destination.createNewFile();
                FileOutputStream os = null;
                try
                {
                    os = new FileOutputStream(destination);
                    FileChannel fileChannel = os.getChannel();
                    StreamUtils.ProgressBar progressBar = null;
                    if ( LOGGER.isInfoEnabled() )
                    {
                        if ( downloadFeeback )
                        {
                            progressBar = new StreamUtils.FineGrainedProgressBar( displayName );
                        }
                        else
                        {
                            progressBar = new StreamUtils.CoarseGrainedProgressBar( displayName );
                        }
                    }
                    StreamUtils.streamCopy( url, fileChannel, progressBar );
                    fileChannel.close();
                    LOGGER.debug( "Succesfully downloaded to [" + destination + "]" );
                }
                finally
                {
                    if ( os != null )
                    {
                        os.close();
                    }
                }
            }
            catch ( IOException e )
            {
                throw new PlatformException( "[" + url + "] could not be downloaded", e );
            }
        }
        if ( checkAttributes )
        {
            try
            {
                validateBundle( url, destination );
            }
            catch ( PlatformException e )
            {
                if ( failOnValidation )
                {
                    throw e;
                }
                return null;
            }
        }
        String cachingName = determineCachingName( destination, hashFileName );
        File newDestination = new File( destination.getParentFile(), cachingName );
        if ( !cachingName.equals( destination.getName() ) )
        {
            if ( newDestination.exists() )
            {
                if ( !newDestination.delete() )
                {
                    throw new PlatformException( "Cannot delete " + newDestination );
                }
            }
            if ( !destination.renameTo( newDestination ) )
            {
                throw new PlatformException( "Cannot rename " + destination + " to " + newDestination );
            }
            fileNamesForUrls.setProperty( url.toExternalForm(), cachingName );
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
        catch ( IOException e )
        {
            return properties;
        }
        finally
        {
            if ( in != null )
            {
                try
                {
                    in.close();
                }
                catch ( IOException e )
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
        catch ( IOException e )
        {
            throw new PlatformException( "Cannot store properties " + file, e );
        }
        finally
        {
            if ( os != null )
            {
                try
                {
                    os.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }

        }
    }

    /**
     * Validate that the file is an valid bundle.
     * A valid bundle will be a loadable jar file that has manifest and the manifest contains at least an entry for
     * Bundle-SymboliName or Bundle-Name (R3).
     *
     * @param url  original url from where the bundle was created.
     * @param file file to be validated
     *
     * @throws PlatformException if the jar is not a valid bundle
     */
    void validateBundle( final URL url,
                         final File file )
        throws PlatformException
    {
        String bundleSymbolicName = null;
        String bundleName = null;
        JarFile jar = null;
        try
        {
            // verify that is a valid jar. Do not verify that is signed (the false param).
            jar = new JarFile( file, false );
            final Manifest manifest = jar.getManifest();
            if ( manifest == null )
            {
                throw new PlatformException( "[" + url + "] is not a valid bundle" );
            }
            bundleSymbolicName = manifest.getMainAttributes().getValue( Constants.BUNDLE_SYMBOLICNAME );
            bundleName = manifest.getMainAttributes().getValue( Constants.BUNDLE_NAME );
        }
        catch ( IOException e )
        {
            throw new PlatformException( "[" + url + "] is not a valid bundle", e );
        }
        finally
        {
            if ( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch ( IOException ignore )
                {
                    // just ignore as this is less probably to happen.
                }
            }
        }
        if ( bundleSymbolicName == null && bundleName == null )
        {
            throw new PlatformException( "[" + url + "] is not a valid bundle" );
        }
    }

    /**
     * Determine name to be used for caching on local file system.
     *
     * @param file                      file to be validated
     * @param defaultBundleSymbolicName default bundle symbolic name to be used if manifest does not have a bundle
     *                                  symbolic name
     *
     * @return file name based on bundle symbolic name and version
     */
    String determineCachingName( final File file,
                                 final String defaultBundleSymbolicName )
    {
        String bundleSymbolicName = null;
        String bundleVersion = null;
        JarFile jar = null;
        try
        {
            // verify that is a valid jar. Do not verify that is signed (the false param).
            jar = new JarFile( file, false );
            final Manifest manifest = jar.getManifest();
            if ( manifest != null )
            {
                bundleSymbolicName = manifest.getMainAttributes().getValue( Constants.BUNDLE_SYMBOLICNAME );
                bundleVersion = manifest.getMainAttributes().getValue( Constants.BUNDLE_VERSION );
            }
        }
        catch ( IOException ignore )
        {
            // just ignore
        }
        finally
        {
            if ( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch ( IOException ignore )
                {
                    // just ignore as this is less probably to happen.
                }
            }
        }
        if ( bundleSymbolicName == null )
        {
            bundleSymbolicName = defaultBundleSymbolicName;
        }
        else
        {
            // remove directives like "; singleton:=true"
            int semicolonPos = bundleSymbolicName.indexOf( ";" );
            if ( semicolonPos > 0 )
            {
                bundleSymbolicName = bundleSymbolicName.substring( 0, semicolonPos );
            }
        }
        if ( bundleVersion == null )
        {
            bundleVersion = "0.0.0";
        }
        return bundleSymbolicName + "_" + bundleVersion + ".jar";
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
        if ( userPackages != null && userPackages.trim().length() > 0 )
        {
            if ( packages.length() > 0 )
            {
                packages.append( "," );
            }
            packages.append( userPackages );
        }
        // append platform specific packages
        if ( platformPackages != null && platformPackages.trim().length() > 0 )
        {
            if ( packages.length() > 0 )
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
        if ( config != null )
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
            if ( definitionURL != null )
            {
                LOGGER.debug( "loading definition from url " + definitionURL.toExternalForm() );
                inputStream = definitionURL.openStream();
            }
            if ( inputStream == null )
            {
                LOGGER.debug( "loading definition from builder." );
                inputStream = m_platformBuilder.getDefinition( configuration );
            }
            return new PlatformDefinitionImpl( inputStream, configuration.getProfileStartLevel() );
        }
        catch ( IOException e )
        {
            throw new PlatformException( "Invalid platform definition", e );
        }
        catch ( ParserConfigurationException e )
        {
            throw new PlatformException( "Invalid platform definition", e );
        }
        catch ( SAXException e )
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
        if ( object == null )
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
