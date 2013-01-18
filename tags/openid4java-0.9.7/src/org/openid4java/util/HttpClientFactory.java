/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

/*
 * Created on Mar 5, 2007
 */
package org.openid4java.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;

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
    	return getInstance(maxRedirects, allowCircularRedirects, connTimeout, socketTimeout, cookiePolicy, null, null);
    }
    
    public static HttpClient getInstance(int maxRedirects,
                                         Boolean allowCircularRedirects,
                                         int connTimeout, int socketTimeout,
                                         String cookiePolicy, SSLContext sslContext,
                                         X509HostnameVerifier hostnameVerifier)
    {
        HttpParams httpParams = new BasicHttpParams();

        SchemeRegistry registry = new SchemeRegistry();

        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        SSLSocketFactory sslSocketFactory;
        if (null == sslContext)
        {
        	sslSocketFactory = SSLSocketFactory.getSocketFactory();
        }
        else
        {
        	sslSocketFactory = new SSLSocketFactory(sslContext);
        }
        if (null != hostnameVerifier)
        {
        	sslSocketFactory.setHostnameVerifier(hostnameVerifier);
        }
        registry.register(new Scheme("https", 443, sslSocketFactory));
        
        ClientConnectionManager connManager;
        if (multiThreadedHttpClient)
            connManager = new ThreadSafeClientConnManager(registry);
        else
            connManager = new SingleClientConnManager(registry);

        DefaultHttpClient client = new DefaultHttpClient(connManager, httpParams);

        client.getParams().setParameter(AllClientPNames.MAX_REDIRECTS,
                                        new Integer(maxRedirects));
        client.getParams().setParameter(AllClientPNames.ALLOW_CIRCULAR_REDIRECTS,
                                        allowCircularRedirects);
        client.getParams().setParameter(AllClientPNames.SO_TIMEOUT,
        								   new Integer(socketTimeout));
        client.getParams().setParameter(AllClientPNames.CONNECTION_TIMEOUT,
				   					   new Integer(connTimeout));

        if (cookiePolicy == null)
        {
            client.setCookieStore(null);
        }
        else
        {
            client.getParams().setParameter(AllClientPNames.COOKIE_POLICY,
                    cookiePolicy);
        }
        

        if (proxyProperties != null)
        {
            HttpHost proxy = new HttpHost(
                    proxyProperties.getProxyHostName(), 
                    proxyProperties.getProxyPort()); 

	        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            //now set headers for auth
            AuthScope authScope = new AuthScope(AuthScope.ANY_HOST,
                    AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME);
            Credentials credentials = proxyProperties.getCredentials();
            client.getCredentialsProvider().setCredentials(authScope, credentials);
        }

        return client;
    }
}

