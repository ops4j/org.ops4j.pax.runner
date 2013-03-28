package org.ops4j.pax.runner.platform.jboss.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.platform.*;
import org.ops4j.util.collections.PropertiesWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author dpishchukhin
 */
public class JBossPlatformBuilderF100
        implements PlatformBuilder {
    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(JBossPlatformBuilderF100.class);
    /**
     * Provider name to be used in registration.
     */
    private static final String PROVIDER_NAME = "jboss";
    /**
     * Name of the main class from JBoss.
     */
    private static final String MAIN_CLASS_NAME = "org.jboss.osgi.spi.framework.OSGiBootstrap";

    /**
     * The directory name where the configuration will be stored.
     */
    private static final String CONFIG_DIRECTORY = "jboss";
    /**
     * Configuration file name.
     */
    private static final String CONFIG_INI = "jboss-config.ini";
    /**
     * Caching directory.
     */
    private static final String CACHE_DIRECTORY = "fwdir";
    /**
     * Profile name to be used when console should be started.
     */
    private static final String CONSOLE_PROFILE = "tui";
    /**
     * Separator for properties (bundles)
     */
    private static final String SEPARATOR = " ";
    /**
     * Current bundle context.
     */
    private final BundleContext m_bundleContext;
    /**
     * Supported version.
     */
    private final String m_version;

    /**
     * Create a new equinux platform builder.
     *
     * @param bundleContext a bundle context
     * @param version       supported version
     */
    public JBossPlatformBuilderF100(final BundleContext bundleContext, final String version) {
        NullArgumentException.validateNotNull(bundleContext, "Bundle context");
        NullArgumentException.validateNotNull(version, "Version");
        m_bundleContext = bundleContext;
        m_version = version;
    }

    public void prepare(PlatformContext context) throws PlatformException {
        NullArgumentException.validateNotNull(context, "Platform context");
        final List<BundleReference> bundles = context.getBundles();
        OutputStream os = null;
        try {
            final File workingDirectory = context.getWorkingDirectory();
            // make sure the configuration directory exists
            final File configDirectory = new File(workingDirectory, CONFIG_DIRECTORY);
            configDirectory.mkdirs();
            // make sure that the cache file exists
            //new File( configDirectory, CACHE_DIRECTORY ).mkdirs();
            // create the configuration file

            context.setAdditionalClasspath(new String[]{
                    context.getFilePathStrategy().normalizeAsPath(
                            new File(workingDirectory, CONFIG_DIRECTORY))
            });

            final File configFile = new File(configDirectory, CONFIG_INI);
            configFile.createNewFile();
            LOGGER.debug("Create JBoss configuration ini file [" + configFile + "]");
            final Configuration configuration = context.getConfiguration();

            os = new FileOutputStream(configFile);
            final PropertiesWriter writer = new PropertiesWriter(os, SEPARATOR);

            writeHeader(writer);

            writer.append("#############################");
            writer.append(" JBoss settings");
            writer.append("#############################");
            // varios settings
            {
                writer.append("org.osgi.framework.storage", "${osgi.home}/" + CACHE_DIRECTORY);
            }
            // use persisted state
            {
                final Boolean usePersistedState = configuration.usePersistedState();
                if (usePersistedState != null && !usePersistedState) {
                    writer.append("org.osgi.framework.storage.clean", "onFirstInit");
                }
            }
            // execution environments
            {
                writer.append(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, context.getExecutionEnvironment());
            }
            // boot delegation packages
            {
                final String bootDelegation = context.getConfiguration().getBootDelegation();
                if (bootDelegation != null) {
                    writer.append(Constants.FRAMEWORK_BOOTDELEGATION, bootDelegation);
                }
            }
            // system packages
            {
                writer.append(Constants.FRAMEWORK_SYSTEMPACKAGES, context.getSystemPackages());
            }
            // provisioned bundles
            {
                if (bundles != null && bundles.size() > 0) {
                    writer.append();
                    writer.append("#############################");
                    writer.append(" Client bundles to install");
                    writer.append("#############################");
                    appendBundles( writer, bundles, context );
                }
            }
            // user system properties
            {
                writer.append();
                writer.append("#############################");
                writer.append(" System properties");
                writer.append("#############################");
                appendProperties(writer, context.getProperties());
            }
            // bundle start level

            writer.write();
        } catch (IOException e) {
            throw new PlatformException("Could not create JBoss configuration file", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new PlatformException("Could not create knopflerfish configuration file", e);
                }
            }
        }
    }

    /**
     * Writes bundles to configuration file.
     *
     * @param writer            a property writer
     * @param bundles           bundles to write
     * @param context           platform context
     *
     * @throws java.net.MalformedURLException re-thrown from getting the file url
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *                                        if one of the bundles does not have a file
     */
    private void appendBundles( final PropertiesWriter writer,
                                final List<BundleReference> bundles,
                                final PlatformContext context )
        throws MalformedURLException, PlatformException
    {
        StringBuilder installBundles = new StringBuilder();
        StringBuilder startBundles = new StringBuilder();
        for( BundleReference reference : bundles )
        {
            URL url = reference.getURL();
            if( url == null )
            {
                throw new PlatformException( "The file from bundle to install cannot be null" );
            }

            final Boolean shouldStart = reference.shouldStart();
            if( shouldStart != null && shouldStart )
            {
                if (startBundles.length() > 0) {
                    startBundles.append(", \\\n");
                }
                // startBundles.append(context.getFilePathStrategy().normalizeAsUrl( url ));
                // FIXME: to normalized form
                startBundles.append(url.toString());
            }
            else
            {
                if (installBundles.length() > 0) {
                    installBundles.append(", \\\n");
                }
                // installBundles.append(context.getFilePathStrategy().normalizeAsUrl( url ));
                // FIXME: to normalized form
                installBundles.append(url.toString());
            }
        }
        if (installBundles.length() > 0) {
            writer.append( "org.jboss.osgi.auto.install", "\\\n" + installBundles.toString() );
        }
        if (startBundles.length() > 0) {
            writer.append( "org.jboss.osgi.auto.start", "\\\n" + startBundles.toString() );
        }
    }

    /**
     * Writes properties to configuration file.
     *
     * @param writer     a property writer
     * @param properties properties to be written; can be null
     */
    private void appendProperties(final PropertiesWriter writer, final Properties properties) {
        if (properties != null) {
            final Enumeration enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                final String key = (String) enumeration.nextElement();
                writer.append(key, properties.getProperty(key));
            }
        }
    }


    /**
     * Writes OPS4j header.
     *
     * @param writer a property writer
     */
    private void writeHeader(final PropertiesWriter writer) {
        writer.append("###############################################");
        writer.append("              ______  ________  __  __        #");
        writer.append("             / __  / /  __   / / / / /        #");
        writer.append("            /  ___/ /  __   / _\\ \\ _/         #");
        writer.append("           /  /    /  / /  / / _\\ \\           #");
        writer.append("          /__/    /__/ /__/ /_/ /_/           #");
        writer.append("                                              #");
        writer.append(" Pax Runner from OPS4J - http://www.ops4j.org #");
        writer.append("###############################################");
        writer.append();
    }

    public String getMainClassName() {
        return MAIN_CLASS_NAME;
    }

    public String[] getArguments(PlatformContext context) {
        NullArgumentException.validateNotNull(context, "Platform context");
        return new String[0];
    }

    public String[] getVMOptions(PlatformContext context) {
        NullArgumentException.validateNotNull(context, "Platform context");

        final Collection<String> vmOptions = new ArrayList<String>();
        final File workingDirectory = context.getWorkingDirectory();
        vmOptions.add(
                "-Dosgi.home="
                        + context.getFilePathStrategy().normalizeAsPath(
                        new File(workingDirectory, CONFIG_DIRECTORY)
                )
        );
        vmOptions.add(
                "-Djboss.osgi.framework.properties=" + CONFIG_INI
        );
        return vmOptions.toArray(new String[vmOptions.size()]);
    }

    public InputStream getDefinition(Configuration configuration) throws IOException {
        final String definitionFile = "META-INF/platform-jboss/definition-" + m_version + ".xml";
        final URL url = m_bundleContext.getBundle().getResource(definitionFile);
        if (url == null) {
            throw new FileNotFoundException(definitionFile + " could not be found");
        }
        return url.openStream();
    }

    public String getRequiredProfile(PlatformContext context) {
        final Boolean console = context.getConfiguration().startConsole();
        if (console == null || !console) {
            return null;
        } else {
            return CONSOLE_PROFILE;
        }
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "JBoss " + m_version;
    }

    public String getProviderName() {
        return PROVIDER_NAME;
    }

    public String getProviderVersion() {
        return m_version;
    }
}
