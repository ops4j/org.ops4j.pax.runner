package org.ops4j.pax.runner.util;

public final class NullArgumentException extends IllegalArgumentException
{

    private static final long serialVersionUID = 1L;

    private NullArgumentException( String msg )
    {
        super( msg );
    }

    public static final void validateNotNull( Object obj, String objVarName )
        throws IllegalArgumentException
    {
        if( obj == null )
        {
            throw new NullArgumentException( "[" + objVarName + "] argument must not be [null]." );
        }
    }

    public static final void validateNotEmpty( String str, String strVarName )
        throws IllegalArgumentException
    {
        validateNotNull( str, strVarName );

        if( str.length() == 0 )
        {
            throw new IllegalArgumentException( "[" + strVarName + "] argument must not be empty." );
        }
    }
}
