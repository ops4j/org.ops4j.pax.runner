package org.ops4j.pax.runner;

/**
 * Resolvs options by name.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public interface OptionResolver
{

    /**
     * Returns the value of option.
     *
     * @param name option name
     *
     * @return found value.
     */
    String get( String name );

    /**
     * Returns the value of option. If value is null thrwos IllegalArgumentException.
     *
     * @param name option name
     *
     * @return found value.
     */
    String getMandatory( String name );

    /**
     * Returns the value of option that can be specified more times under the same name (as an array of values).
     *
     * @param name option name
     *
     * @return found values, or empty aray if option not present.
     */
    String[] getMultiple( String name );

}
