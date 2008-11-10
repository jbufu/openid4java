/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

/*
 * Created on Mar 5, 2007
 */
package org.openid4java.util;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.AuthScope;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.client.protocol.RequestAddCookies;

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
    	
        DefaultHttpClient client = new ThreadSafeHttpClient();

        client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
        
        client.getParams().setIntParameter(
                ClientPNames.MAX_REDIRECTS, maxRedirects);
        
        client.getParams().setParameter(
        		ClientPNames.ALLOW_CIRCULAR_REDIRECTS, allowCircularRedirects);
        
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
        
        client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connTimeout);
        
        if (cookiePolicy == null)
        {
        	client.removeRequestInterceptorByClass(RequestAddCookies.class);
        	client.removeResponseInterceptorByClass(ResponseProcessCookies.class);
        }
        else
        {
        	client.getParams().setParameter(
				            		ClientPNames.COOKIE_POLICY, 
				                    cookiePolicy);
        }

        if (proxyProperties != null)
        {
        	
        	 final HttpHost proxy = new HttpHost(
        			 		proxyProperties.getProxyHostName(), 
                		 	proxyProperties.getProxyPort(), 
                		 	"http");
        	 
        	 client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        	 
        	 AuthScope scope = new AuthScope(proxyProperties.getProxyHostName(),
        			 					proxyProperties.getProxyPort());
        			 					
         	 client.getCredentialsProvider().setCredentials(scope, 
         			 						new UsernamePasswordCredentials(
         			 							proxyProperties.getUserName(),
         			 							proxyProperties.getPassword()));
        	 
        }

        return client;
    }
}

