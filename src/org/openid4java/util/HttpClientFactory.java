/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

/*
 * Created on Mar 5, 2007
 */
package org.openid4java.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;

/**
 * This class handles all HTTPClient connections for the
 * org.openid4java packages.
 *
 * @author Kevin
 */
public class HttpClientFactory {
    private HttpClientFactory() {
    }

    /**
     * proxy properties for HTTPClient calls
     */
    private static ProxyProperties proxyProperties = null;

    private static boolean multiThreadedHttpClient = true;

    public static ProxyProperties getProxyProperties() {
        return proxyProperties;
    }

    public static void setProxyProperties(ProxyProperties proxyProperties) {
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
     */
    public static void setMultiThreadedHttpClient(boolean multiThreadedHttpClient) {
        HttpClientFactory.multiThreadedHttpClient = multiThreadedHttpClient;
    }

    public static HttpClient getInstance(int maxRedirects,
                                         Boolean allowCircularRedirects,
                                         int connTimeout, int socketTimeout,
                                         String cookiePolicy) {
        return getInstance(maxRedirects, allowCircularRedirects, connTimeout, socketTimeout, cookiePolicy, null, null);
    }

    public static HttpClient getInstance(int maxRedirects,
                                         Boolean allowCircularRedirects,
                                         int connTimeout, int socketTimeout,
                                         String cookiePolicy, SSLContext sslContext,
                                         X509HostnameVerifier hostnameVerifier) {

        HttpClientBuilder     httpClientBuilder    = HttpClients.custom();
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

        requestConfigBuilder.setCircularRedirectsAllowed(allowCircularRedirects)
                            .setMaxRedirects(maxRedirects)
                            .setConnectTimeout(connTimeout)
                            .setSocketTimeout(socketTimeout);
        if (cookiePolicy != null)
            requestConfigBuilder.setCookieSpec(cookiePolicy);

        if (sslContext != null)
            httpClientBuilder.setSSLContext(sslContext);

        if(hostnameVerifier != null)
            httpClientBuilder.setHostnameVerifier(hostnameVerifier);

        if (proxyProperties != null) {

            requestConfigBuilder.setProxy(new HttpHost(
                    proxyProperties.getProxyHostName(),
                    proxyProperties.getProxyPort()));

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST,
                            AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME),
                    new UsernamePasswordCredentials(proxyProperties.getUserName(), proxyProperties.getPassword()));

            httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
        }

        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

        return httpClientBuilder.build();
    }
}

