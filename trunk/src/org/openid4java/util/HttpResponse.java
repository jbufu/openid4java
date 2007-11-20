/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.commons.httpclient.Header;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class HttpResponse
{
    private int _statusCode;
    private String _statusLine;

    private int _maxRedirectsFollowed;
    private String _finalUri;

    private Map _responseHeaders;
    private String _body;

    // todo: add timestamp

    public HttpResponse(int statusCode, String statusLine,
                        int redirectsFollowed, String finalUri,
                        Header[] responseHeaders, String body)
    {
        _statusCode = statusCode;
        _statusLine = statusLine;

        _maxRedirectsFollowed = redirectsFollowed;
        _finalUri = finalUri;

        _responseHeaders = new HashMap();
        if (responseHeaders != null)
        {
            for (int i=0; i < responseHeaders.length; i++)
                _responseHeaders.put(
                        responseHeaders[i].getName(), responseHeaders[i]);
        }

        _body = body;
    }

    public int getStatusCode()
    {
        return _statusCode;
    }

    public String getStatusLine()
    {
        return _statusLine;
    }

    public int getMaxRedirectsFollowed()
    {
        return _maxRedirectsFollowed;
    }

    public String getFinalUri()
    {
        return _finalUri;
    }

    public Header getResponseHeader(String headerName)
    {
        Header[] headers = getResponseHeaders(headerName);

        if (headers != null && headers.length >0)
            return headers[0];
        else
            return null;
    }

    public Header[] getResponseHeaders(String headerName)
    {
        return (Header[]) _responseHeaders.get(headerName);
    }

    public String getBody()
    {
        return _body;
    }
}
