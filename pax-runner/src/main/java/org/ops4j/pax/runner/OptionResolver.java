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

}
