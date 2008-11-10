/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import java.util.Map;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.params.CoreConnectionPNames;

/**
 * Container class for the various options associated with HTTP requests.
 *
 * @see org.openid4java.util.HttpCache
 * @author Marius Scurtescu, Johnny Bufu
 */
public class HttpRequestOptions
{
    /**
     * HTTP connect timeout, in milliseconds. Default 3000 miliseconds.
     */
    private int _connTimeout = 3000;

    /**
     * HTTP socket (read) timeout, in milliseconds. Default 5000 miliseconds.
     */
    private int _socketTimeout = 5000;

    /**
     * Maximum number of redirects to be followed for the HTTP calls.
     * Defalut 10.
     */
    private int _maxRedirects = 10;

    /**
     * Maximum size in bytes to be retrieved for the response body.
     * Default 100,000 bytes.
     */
    private int _maxBodySize = 100000;

    /**
     * Map with HTTP request headers to be used when placing the HTTP request.
     */
    private Map _requestHeaders;

    /**
     * If set to false, a new HTTP request will be placed even if a cached copy
     * exists. This applies to the internal HttpCache, not the HTTP protocol
     * cache-control mechanisms.
     *
     * @see org.openid4java.util.HttpCache
     */
    private boolean _useCache = true;

    /**
     * If HttpRequestOptions' content type matches a cached HttpResponse's
     * content type, the cache copy is returned; otherwise a new HTTP request
     * is placed.
     */
    private String _contentType = null;


    /**
     * Constructs a set of HTTP request options with the default values.
     */
    public HttpRequestOptions()
    {
    }

    /**
     * Creates a new HttpRequestOptions object as a clone of the provided
     * parameter.
     *
     * @param other     HttpRequestOptions instance to be cloned.
     */
    public HttpRequestOptions(HttpRequestOptions other)
    {
        this._connTimeout = other._connTimeout;
        this._socketTimeout = other._socketTimeout;
        this._maxRedirects = other._maxRedirects;
        this._maxBodySize = other._maxBodySize;
        if (other._requestHeaders != null)
            this._requestHeaders = new HashMap(other._requestHeaders);
        this._useCache = other._useCache;
        this._contentType = other._contentType;
    }

    /**
     * Gets the HTTP connect timeout, in milliseconds.
     */
    public int getConnTimeout()
    {
        return _connTimeout;
    }

    /**
     * Sets the HTTP connect timeout, in milliseconds.
     */
    public void setConnTimeout(int connTimeout)
    {
        this._connTimeout = connTimeout;
    }

    /**
     * Gets the HTTP socket (read) timeout, in milliseconds.
     */
    public int getSocketTimeout()
    {
        return _socketTimeout;
    }

    /**
     * Sets HTTP socket (read) timeout, in milliseconds.
     */
    public void setSocketTimeout(int socketTimeout)
    {
        this._socketTimeout = socketTimeout;
    }

    /**
     * Gets the internal limit configured for the maximum number of redirects
     * to be followed for the HTTP calls.
     */
    public int getMaxRedirects()
    {
        return _maxRedirects;
    }

    /**
     * Sets the maximum number of redirects to be followed for the HTTP calls.
     */
    public void setMaxRedirects(int maxRedirects)
    {
        this._maxRedirects = maxRedirects;
    }

    /**
     * Gets configuration parameter for the maximum HTTP body size
     * that will be downloaded.
     */
    public int getMaxBodySize()
    {
        return _maxBodySize;
    }

    /**
     * Sets the maximum HTTP body size that will be downloaded.
     */
    public void setMaxBodySize(int maxBodySize)
    {
        this._maxBodySize = maxBodySize;
    }

    /**
     * Gets the HTTP request headers that will be used when placing
     * HTTP requests using the options in this object.
     */
    public Map getRequestHeaders()
    {
        return _requestHeaders;
    }

    /**
     * Sets the HTTP request headers that will be used when placing
     * HTTP requests using the options in this object.
     */
    public void setRequestHeaders(Map requestHeaders)
    {
        this._requestHeaders = requestHeaders;
    }

    /**
     * Returns true if a cached copy can be used when placing HTTP requests
     * using the options in this object. This applies to the internally
     * implemented HTTP cache, NOT to the HTTP protocol cache-control.
     */
    public boolean isUseCache()
    {
        return _useCache;
    }

    /**
     * Sets the flag for allowing cached copy to be used when placing
     * HTTP requests using the options in this object. This applies
     * to the internally implemented HTTP cache, NOT to the HTTP protocol
     * cache-control.
     */
    public void setUseCache(boolean useCache)
    {
        this._useCache = useCache;
    }

    /**
     * Gets the required content-type for the HTTP response. If this option
     * matches the content-type of a cached response, the cached copy is used;
     * otherwise a new HTTP request is made.
     */
    public String getContentType()
    {
        return _contentType;
    }

    /**
     * Sets the required content-type for the HTTP response. If this option
     * matches the content-type of a cached response, the cached copy is used;
     * otherwise a new HTTP request is made.
     */
    public void setContentType(String contentType)
    {
        this._contentType = contentType;
    }
    
    static public void setRequestOptions(HttpClient client, HttpRequestOptions options)
    {
        client.getParams().setIntParameter(
                ClientPNames.MAX_REDIRECTS, options.getMaxRedirects());
        
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, options.getSocketTimeout());
        
        client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, options.getConnTimeout());
	
    }
}
