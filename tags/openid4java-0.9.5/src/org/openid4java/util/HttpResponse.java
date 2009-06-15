/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.commons.httpclient.Header;

import java.util.*;

/**
 * Container class for HTTP responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class HttpResponse
{
    /**
     * The status code of the HTTP response.
     */
    private int _statusCode;

    /**
     * The status line of the HTTP response.
     */
    private String _statusLine;

    /**
     * The maximum HTTP redirects limit that was configured
     * when this HTTP response was obtained.
     */
    private int _maxRedirectsFollowed;

    /**
     * The final URI from where the document was obtained,
     * after following redirects.
     */
    private String _finalUri;

    /**
     * Map of header names  List of Header objects of the HTTP response.
     */
    private Map _responseHeaders;

    /**
     * The HTTP response body.
     */
    private String _body;

    /**
     * Flag to indicate if the HTTP response size exceeded the maximum
     * allowed by the (default) HttpRequestOptions.
     */
    private boolean _bodySizeExceeded = false;

    // todo: add timestamp

    /**
     * Constructs a new HttpResponse with the provided parameters.
     */
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
            String headerName;
            Header header;
            for (int i=0; i < responseHeaders.length; i++)
            {
                // HTTP header names are case-insensitive
                headerName = responseHeaders[i].getName().toLowerCase();
                header = responseHeaders[i];

                List headerList = (List) _responseHeaders.get(headerName);
                if (headerList != null)
                    headerList.add(responseHeaders[i]);
                else
                    _responseHeaders.put(headerName,
                        new ArrayList(Arrays.asList(new Header[] {header})));
            }
        }

        _body = body;
    }

    /**
     * Gets the status code of the HttpResponse.
     */
    public int getStatusCode()
    {
        return _statusCode;
    }

    /**
     * Gets the status line of the HttpResponse.
     */
    public String getStatusLine()
    {
        return _statusLine;
    }

    /**
     * Gets the maximum HTTP redirects limit that was configured
     * when this HTTP response was obtained.
     */
    public int getMaxRedirectsFollowed()
    {
        return _maxRedirectsFollowed;
    }

    /**
     * Gets the final URI from where the document was obtained,
     * after following redirects.
     */
    public String getFinalUri()
    {
        return _finalUri;
    }

    /**
     * Gets the first header matching the provided headerName parameter,
     * or null if no header with that name exists.
     */
    public Header getResponseHeader(String headerName)
    {
        List headerList = (List) _responseHeaders.get(headerName.toLowerCase());

        if (headerList != null && headerList.size() > 0)
            return (Header) headerList.get(0);
        else
            return null;
    }

    /**
     * Gets an array of Header objects for the provided headerName parameter.
     */
    public Header[] getResponseHeaders(String headerName)
    {
        List headerList = (List) _responseHeaders.get(headerName.toLowerCase());

        if (headerList != null)
            return (Header[]) headerList.toArray(new Header[headerList.size()]);
        else
            return new Header[]{}; // empty array, same as HttpClient's method
    }

    /**
     * Gets the HttpResponse body.
     */
    public String getBody()
    {
        return _body;
    }

    /**
     * Returns true if the HTTP response size exceeded the maximum
     * allowed by the (default) HttpRequestOptions.
     * @return
     */
    public boolean isBodySizeExceeded()
    {
        return _bodySizeExceeded;
    }


    /**
     * Sets the flag to indicate whether the HTTP response size exceeded
     * the maximum allowed by the (default) HttpRequestOptions.
     */
    public void setBodySizeExceeded(boolean bodySizeExceeded)
    {
        this._bodySizeExceeded = bodySizeExceeded;
    }
}
