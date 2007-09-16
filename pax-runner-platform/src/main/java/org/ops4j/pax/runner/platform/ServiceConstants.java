package org.ops4j.pax.runner.platform;

/**
 * An enumeration of constants related to platform service.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public interface ServiceConstants
{

    /**
     * Service PID used for configuration.
     */
    static final String PID = "org.ops4j.pax.runner.platform";
    /**
     * Definition URL configuration property name.
     */
    static final String CONFIG_DEFINITION_URL = PID + ".definitionURL";
    /**
     * Working directory configuration property name.
     */
    static final String CONFIG_WORKING_DIRECTORY = PID + ".workingDirectory";
    /**
     * Classpath configuration property name.
     */
    static final String CONFIG_CLASSPATH = PID + ".classpath";
    /**
     * Virtual Machine options configuration property name.
     */
    static final String CONFIG_VMOPTIONS = PID + ".vmOptions";
    /**
     * System packages configuration property name.
     */
    static final String CONFIG_SYSTEM_PACKAGES = PID + ".systemPackages";
    /**
     * Execution environment configuration property name.
     */
    static final String CONFIG_EXECUTION_ENV = PID + ".ee";
    /**
     * Profiles configuration property name.
     */
    static final String CONFIG_PROFILES = PID + ".profiles";
    /**
     * Framework profile configuration property name.
     */
    static final String CONFIG_FRAMEWORK_PROFILE = PID + ".frameworkProfile";
    /**
     * Platform start level configuration property name.
     */
    static final String CONFIG_START_LEVEL = PID + ".startLevel";
    /**
     * Platform bundles start level configuration property name.
     */
    static final String CONFIG_PROFILE_START_LEVEL = PID + ".profileStartLevel";
    /**
     * Installed bundles start level configuration property name.
     */
    static final String CONFIG_BUNDLE_START_LEVEL = PID + ".bundleStartLevel";
    /**
     * Java home configuration property name.
     */
    static final String CONFIG_JAVA_HOME = PID + ".javaHome";
    /**
     * Clean platform configuration property name.
     */
    static final String CONFIG_CLEAN = PID + ".clean";
    /**
     * Console configuration property name.
     */
    static final String CONFIG_CONSOLE = PID + ".console";
    /**
     * Overwrite configuration property name.
     */
    static final String CONFIG_OVERWRITE = PID + ".overwrite";
    /**
     * The service property name under which the platform name should be registered.
     */
    static final String PROPERTY_PROVIDER = "provider";
    /**
     * The servive property name under which the platform version should be registered.
     */
    static final String PROPERTY_PROVIDER_VERSION = "version";


}
