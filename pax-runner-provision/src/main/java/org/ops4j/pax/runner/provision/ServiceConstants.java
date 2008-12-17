package org.ops4j.pax.runner.provision;

/**
 * An enumeration of constants related to provision service.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public interface ServiceConstants
{

    /**
     * Scheme separator.
     */
    static final String SEPARATOR_SCHEME = ":";
    /**
     * Separator for options.
     */
    static String SEPARATOR_OPTION = "@";
    /**
     * Start option.
     */
    static String OPTION_NO_START = "nostart";
    /**
     * Update option.
     */
    static String OPTION_UPDATE = "update";

    /**
     * Start level configuration property name (used by scanners).
     */
    static final String PROPERTY_START_LEVEL = ".startLevel";
    /**
     * Start configuration property name (used by scanners).
     */
    static final String PROPERTY_START = ".start";

    /**
     * Update configuration property name (used by scanners).
     */
    static final String PROPERTY_UPDATE = ".update";

}
