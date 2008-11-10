
package org.openid4java.util;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

class ThreadSafeHttpClient extends DefaultHttpClient 
{
	
	public ThreadSafeHttpClient() 
	{
		super();
	}
	
    protected ClientConnectionManager createClientConnectionManager() 
	{
		
        SchemeRegistry registry = new SchemeRegistry();
        
        registry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager connManager = new ThreadSafeClientConnManager(getParams(), registry);
        
        return connManager;
    }
	
}
