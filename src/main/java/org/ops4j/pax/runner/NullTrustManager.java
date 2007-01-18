package org.ops4j.pax.runner;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * A null trust manager that will accept any certificate. I.e. this
 * class performs NO TRUST MANAGEMENT and simply serves as a mechanism
 * through which https connections can be established with the same notion
 * of trust as a http connection (i.e. none).
 */
public final class NullTrustManager
    implements X509TrustManager
{

    /**
     * Empty certificate sequence.
     */
    private static final X509Certificate[] EMPTY_CERTS = new X509Certificate[0];

    /**
     * Null implementation.
     *
     * @param certs    the supplied certs (ignored)
     * @param authType the supplied type (ignored)
     */
    public void checkServerTrusted( final X509Certificate[] certs, final String authType )
    {
    }

    /**
     * Null implementation.
     *
     * @param certs    the supplied certs (ignored)
     * @param authType the supplied type (ignored)
     */
    public void checkClientTrusted( final X509Certificate[] certs, final String authType )
    {
    }

    /**
     * Null implementation.
     *
     * @return an empty certificate array
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return EMPTY_CERTS;
    }
}
