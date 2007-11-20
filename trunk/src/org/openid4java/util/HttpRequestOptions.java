/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.util;

import java.util.Map;

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

    public int getMaxBodySize()
    {
        return _maxBodySize;
    }

    public void setMaxBodySize(int maxBodySize)
    {
        this._maxBodySize = maxBodySize;
    }


    public Map getRequestHeaders()
    {
        return _requestHeaders;
    }

    public void setRequestHeaders(Map requestHeaders)
    {
        this._requestHeaders = requestHeaders;
    }


    public boolean isUseCache()
    {
        return _useCache;
    }

    public void setUseCache(boolean useCache)
    {
        this._useCache = useCache;
    }


    public String getContentType()
    {
        return _contentType;
    }

    public void setContentType(String contentType)
    {
        this._contentType = contentType;
    }
}
