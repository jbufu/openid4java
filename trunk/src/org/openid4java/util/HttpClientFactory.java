/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

/*
 * Created on Mar 5, 2007
 */
package org.openid4java.util;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * This class handles all HTTPClient connections for the
 * org.openid4java packages.
 *
 * @author Kevin
 */
public class HttpClientFactory
{
    private HttpClientFactory() {}

    /**
     * proxy properties for HTTPClient calls
     */
    private static ProxyProperties proxyProperties = null;

    private static boolean multiThreadedHttpClient = true;

    public static ProxyProperties getProxyProperties()
    {
        return proxyProperties;
    }

    public static void setProxyProperties(ProxyProperties proxyProperties)
    {
        HttpClientFactory.proxyProperties = proxyProperties;
    }

    public static boolean isMultiThreadedHttpClient() {
        return multiThreadedHttpClient;
    }

    /**
     * Configures the type of HttpClient's constructed by the factory.
     *
     * @param multiThreadedHttpClient if true, MultiThreadedHttpConnectionManager's are constructed;
     *                                if false - SimpleHttpConnectionManager's.
     *
     */
    public static void setMultiThreadedHttpClient(boolean multiThreadedHttpClient) {
        HttpClientFactory.multiThreadedHttpClient = multiThreadedHttpClient;
    }

    public static HttpClient getInstance(int maxRedirects,
                                         Boolean allowCircularRedirects,
                                         int connTimeout, int socketTimeout,
                                         String cookiePolicy)
    {
        HttpConnectionManager connManager;
        if (multiThreadedHttpClient)
            connManager = new MultiThreadedHttpConnectionManager();
        else
            connManager = new SimpleHttpConnectionManager();

        HttpClient client = new HttpClient(connManager);

        client.getParams().setParameter(
                "http.protocol.max-redirects", new Integer(maxRedirects));
        client.getParams().setParameter(
                "http.protocol.allow-circular-redirects", allowCircularRedirects);
        client.getParams().setSoTimeout(socketTimeout);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(connTimeout);
        client.getParams().setParameter("http.protocol.cookie-policy",
                cookiePolicy);

        if (proxyProperties != null)
        {
            HostConfiguration hostConf = client.getHostConfiguration();

            hostConf.setProxy(proxyProperties.getProxyHostName(), proxyProperties.getProxyPort());

            //now set headers for auth
            AuthScope authScope = new AuthScope(AuthScope.ANY_HOST,
                    AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
            Credentials credentials = proxyProperties.getCredentials();
            if (credentials != null)
            {
                client.getState().setProxyCredentials(authScope, credentials);
            }
        }

        return client;
    }
}

