/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.html;

import org.apache.http.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpResponse;
import org.openid4java.util.HttpRequestOptions;
import org.openid4java.util.OpenID4JavaUtils;
import org.openid4java.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu, Sutra Zhou
 */
public class HtmlResolver
{
    private static Log _log = LogFactory.getLog(HtmlResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private static final String HTML_PARSER_CLASS_NAME_KEY = "discovery.html.parser";
    private static final HtmlParser HTML_PARSER;

    static {
        String className = OpenID4JavaUtils.getProperty(HTML_PARSER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(HTML_PARSER_CLASS_NAME_KEY + ":" + className);
        try
        {
            HTML_PARSER = (HtmlParser) Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Maximum number of redirects to be followed for the HTTP calls.
     */
    private int _maxRedirects = 10;

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
     * Performs HTML discovery on the supplied URL identifier.
     *
     * @param identifier        The URL identifier.
     * @return                  HTML discovery data obtained from the URL.
     *
     * @see #discover(UrlIdentifier, HttpCache)
     */
    public HtmlResult discover(UrlIdentifier identifier)
            throws DiscoveryException
    {
        return discover(identifier, new HttpCache());
    }

    /**
     * Performs HTML discovery on the supplied URL identifier.
     *
     * @param identifier        The URL identifier.
     * @param cache             HttpClient object to use for placing the call
     * @return                  HTML discovery data obtained from the URL.
     */
    public HtmlResult discover(UrlIdentifier identifier, HttpCache cache)
            throws DiscoveryException
    {
        // initialize the results of the HTML discovery
        HtmlResult result = new HtmlResult();

        HttpRequestOptions requestOptions = cache.getRequestOptions();
        requestOptions.setContentType("text/html");

        try
        {
            HttpResponse resp = cache.get(identifier.toString(), requestOptions);

            if (HttpStatus.SC_OK != resp.getStatusCode())
                throw new DiscoveryException( "GET failed on " +
                    identifier.toString() +
                    " Received status code: " + resp.getStatusCode(),
                    OpenIDException.DISCOVERY_HTML_GET_ERROR);

            result.setClaimed( new UrlIdentifier(resp.getFinalUri()) );

            if (resp.getBody() == null)
                throw new DiscoveryException(
                        "No HTML data read from " + identifier.toString(),
                OpenIDException.DISCOVERY_HTML_NODATA_ERROR);

            HTML_PARSER.parseHtml(resp.getBody(), result);
        }
        catch (IOException e)
        {
            throw new DiscoveryException("Fatal transport error: ",
                    OpenIDException.DISCOVERY_HTML_GET_ERROR, e);
        }

        _log.info("HTML discovery completed on: " + identifier);

        return result;
    }
}
