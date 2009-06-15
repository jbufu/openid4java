/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

/*
 * Created on Mar 5, 2007
 */
package org.openid4java.util;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * This class handles all HTTPClient connections for the
 * org.openid4java packages.
 * <p/>
 *
 * @author Kevin
 */
public class HttpClientFactory
{
    /**
     * proxy properties for HTTPClient calls
     */
    private static ProxyProperties proxyProperties = null;

    public static ProxyProperties getProxyProperties()
    {
        return proxyProperties;
    }

    public static void setProxyProperties(ProxyProperties properties)
    {
        proxyProperties = properties;
    }

    public static HttpClient getInstance(int maxRedirects,
                                         Boolean allowCircularRedirects,
                                         int connTimeout, int socketTimeout,
                                         String cookiePolicy)
    {
        HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

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
            client.getState().setProxyCredentials(authScope,
                    new UsernamePasswordCredentials(
                            proxyProperties.getUserName(),
                            proxyProperties.getPassword()));
        }

        return client;
    }
}

