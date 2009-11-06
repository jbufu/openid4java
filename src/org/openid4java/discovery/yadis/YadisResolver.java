/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.apache.http.HttpStatus;
import org.apache.http.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Set;
import java.util.Collections;
import java.util.List;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.xrds.XrdsParser;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpRequestOptions;
import org.openid4java.util.HttpResponse;
import org.openid4java.util.OpenID4JavaUtils;

/**
 * Yadis discovery protocol implementation.
 * <p>
 * Yadis discovery protocol returns a Yadis Resource Descriptor (XRDS) document
 * associated with a Yadis Identifier (YadisID)
 * <p>
 * YadisIDs can be any type of identifiers that are resolvable to a URL form,
 * and in addition the URL form uses a HTTP or a HTTPS schema. Such an URL
 * is defined by the Yadis speficification as a YadisURL. This functionality
 * is implemented by the YadisURL helper class.
 * <p>
 * The discovery of the XRDS document is performed by the discover method
 * on a YadisUrl.
 * <p>
 * Internal parameters used during the discovery process :
 * <ul>
 * <li> max redirects (default 10): maximum number of redirects to be followed
 *      for YadisURL
 * </ul>
 *
 * @author Marius Scurtescu, Johnny Bufu, Sutra Zhou
 */
public class YadisResolver
{
    private static Log _log = LogFactory.getLog(YadisResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    // Yadis constants
    public static final String YADIS_XRDS_LOCATION = "X-XRDS-Location";
    private static final String YADIS_CONTENT_TYPE = "application/xrds+xml";
    private static final String YADIS_ACCEPT_HEADER =
            "text/html; q=0.3, application/xhtml+xml; q=0.5, " +
                    YADIS_CONTENT_TYPE;

    private static final String YADIS_HTML_PARSER_CLASS_NAME_KEY = "discovery.yadis.html.parser";
    private static final YadisHtmlParser YADIS_HTML_PARSER;

    private static final String XRDS_PARSER_CLASS_NAME_KEY = "discovery.xrds.parser";
    private static final XrdsParser XRDS_PARSER;

    static {
        String className = OpenID4JavaUtils.getProperty(YADIS_HTML_PARSER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(YADIS_HTML_PARSER_CLASS_NAME_KEY + ":" + className);
        try
        {
            YADIS_HTML_PARSER = (YadisHtmlParser) Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        className = OpenID4JavaUtils.getProperty(XRDS_PARSER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(XRDS_PARSER_CLASS_NAME_KEY + ":" + className);
        try
        {
            XRDS_PARSER = (XrdsParser) Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Maximum number of redirects to be followed for the HTTP calls.
     * Defalut 10.
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
     * Instantiates a YadisResolver with default values for the internal
     * parameters.
     */
    public YadisResolver()
    {

    }

    /**
     * Performs Relyin Party discovery on the supplied URL.
     *
     * @param url   RP's realm or return_to URL
     * @return      List of DiscoveryInformation entries discovered
     *              from the RP's endpoints
     */
    public List discoverRP(String url) throws DiscoveryException
    {
        return discover(url, 0, new HttpCache(),
            Collections.singleton(DiscoveryInformation.OPENID2_RP))
            .getDiscoveredInformation(Collections.singleton(DiscoveryInformation.OPENID2_RP));
    }

    /**
     * Performs Yadis discovery on the YadisURL.
     * <p>
     * <ul>
     * <li> tries to retrieve the XRDS location via a HEAD call on the Yadis URL
     * <li> retrieves the XRDS document with a GET on the above if available,
     *      or through a GET on the YadisURL otherwise
     * </ul>
     * <p>
     * The maximum number of redirects that are followed is determined by the
     * #_maxRedirects member field.
     *
     * @param url           YadisURL on which discovery will be performed
     * @return              List of DiscoveryInformation entries discovered
     *                      obtained from the URL Identifier.
     * @see YadisResult #discover(String, int, HttpCache)
     */
    public List discover(String url) throws DiscoveryException
    {
        return discover(url, _maxRedirects, new HttpCache() );
    }


    /**
     * Performs Yadis discovery on the YadisURL.
     * <p>
     * <ul>
     * <li> tries to retrieve the XRDS location via a HEAD call on the Yadis URL
     * <li> retrieves the XRDS document with a GET on the above if available,
     *      or through a GET on the YadisURL otherwise
     * </ul>
     * <p>
     * The maximum number of redirects that are followed is determined by the
     * #_maxRedirects member field.
     *
     * @param url           YadisURL on which discovery will be performed
     * @param cache         HttpCache object for optimizing HTTP requests.
     * @return              List of DiscoveryInformation entries discovered
     *                      obtained from the URL Identifier.
     * @see YadisResult #discover(String, int, HttpCache)
     */
    public List discover(String url, HttpCache cache) throws DiscoveryException
    {
        return discover(url, _maxRedirects, cache);
    }

    /**
     * Performs Yadis discovery on the YadisURL.
     * <p>
     * <ul>
     * <li> tries to retrieve the XRDS location via a HEAD call on the Yadis URL
     * <li> retrieves the XRDS document with a GET on the above if available,
     *      or through a GET on the YadisURL otherwise
     * </ul>
     *
     * @param url           YadisURL on which discovery will be performed
     * @param maxRedirects  The maximum number of redirects to be followed.
     * @return              List of DiscoveryInformation entries discovered
     *                      obtained from the URL Identifier.
     * @see YadisResult #discover(String, int, HttpCache)
     */
    public List discover(String url, int maxRedirects) throws DiscoveryException
    {
        return discover(url, maxRedirects, new HttpCache());
    }

    /**
     * Performs Yadis discovery on the YadisURL.
     * <p>
     * <ul>
     * <li> tries to retrieve the XRDS location via a HEAD call on the Yadis URL
     * <li> retrieves the XRDS document with a GET on the above if available,
     *      or through a GET on the YadisURL otherwise
     * </ul>
     *
     * @param url           YadisURL on which discovery will be performed
     * @param maxRedirects  The maximum number of redirects to be followed.
     * @param cache         HttpCache object for optimizing HTTP requests.
     * @return              List of DiscoveryInformation entries discovered
     *                      obtained from the URL Identifier.
     * @see YadisResult
     */
    public List discover(String url, int maxRedirects, HttpCache cache)
        throws DiscoveryException
    {
        return discover(url, maxRedirects, cache, DiscoveryInformation.OPENID_OP_TYPES)
            .getDiscoveredInformation(DiscoveryInformation.OPENID_OP_TYPES);
    }

    public YadisResult discover(String url, int maxRedirects, HttpCache cache, Set serviceTypes) throws DiscoveryException {
        YadisUrl yadisUrl = new YadisUrl(url);

        // try to retrieve the Yadis Descriptor URL with a HEAD call first
        YadisResult result = retrieveXrdsLocation(yadisUrl, false, cache, maxRedirects, serviceTypes);

        // try GET
        if (result.getXrdsLocation() == null)
            result = retrieveXrdsLocation(yadisUrl, true, cache, maxRedirects, serviceTypes);

        if (result.getXrdsLocation() != null)
        {
            retrieveXrdsDocument(result, cache, maxRedirects, serviceTypes);
        }
        else if (result.hasEndpoints())
        {
            // report the yadis url as the xrds location
            result.setXrdsLocation(url, OpenIDException.YADIS_INVALID_URL);
        }

        _log.info("Yadis discovered " + result.getEndpointCount() + " endpoints from: " + url);
        return result;
    }

    /**
     * Tries to retrieve the XRDS document via a GET call on XRDS location
     * provided in the result parameter.
     *
     * @param result        The YadisResult object containing a valid XRDS location.
     *                      It will be further populated with the Yadis discovery results.
     * @param cache        The HttpClient object to use for placing the call
     * @param maxRedirects
     */
    private void retrieveXrdsDocument(YadisResult result, HttpCache cache, int maxRedirects, Set serviceTypes)
        throws DiscoveryException {

        cache.getRequestOptions().setMaxRedirects(maxRedirects);

        try {
            HttpResponse resp = cache.get(result.getXrdsLocation().toString());

            if (resp == null || HttpStatus.SC_OK != resp.getStatusCode())
                throw new YadisException("GET failed on " + result.getXrdsLocation(),
                        OpenIDException.YADIS_GET_ERROR);

            // update xrds location, in case redirects were followed
            result.setXrdsLocation(resp.getFinalUri(), OpenIDException.YADIS_GET_INVALID_RESPONSE);

            Header contentType = resp.getResponseHeader("content-type");
            if ( contentType != null && contentType.getValue() != null)
                result.setContentType(contentType.getValue());

            if (resp.isBodySizeExceeded())
                throw new YadisException(
                    "More than " + cache.getRequestOptions().getMaxBodySize() +
                    " bytes in HTTP response body from " + result.getXrdsLocation(),
                    OpenIDException.YADIS_XRDS_SIZE_EXCEEDED);
            result.setEndpoints(XRDS_PARSER.parseXrds(resp.getBody(), serviceTypes));

        } catch (IOException e) {
            throw new YadisException("Fatal transport error: ",
                    OpenIDException.YADIS_GET_TRANSPORT_ERROR, e);
        }
    }

    /**
     * Parses the HTML input stream and scans for the Yadis XRDS location
     * in the HTML HEAD Meta tags.
     *
     * @param input             input data stream
     * @return String           the XRDS location URL, or null if not found
     * @throws YadisException   on parsing errors or Yadis protocal violations
     */
    private String getHtmlMeta(String input) throws YadisException
    {
        String xrdsLocation;

        if (input == null)
            throw new YadisException("Cannot download HTML message",
                    OpenIDException.YADIS_HTMLMETA_DOWNLOAD_ERROR);

        xrdsLocation = YADIS_HTML_PARSER.getHtmlMeta(input);
        if (DEBUG)
        {
            _log.debug("input:\n" + input);
            _log.debug("xrdsLocation: " + xrdsLocation);
        }
        return xrdsLocation;
    }

    /**
     * Tries to retrieve the XRDS location url by performing a cheap HEAD call
     * on the YadisURL.
     * <p>
     * The returned string should be validated before being used
     * as a XRDS-Location URL.
     *
     * @param cache         HttpClient object to use for placing the call
     * @param maxRedirects
     * @param url           The YadisURL
     * @param result        The location of the XRDS document and the normalized
     *                      Url will be returned in the YadisResult object.
     * <p>
     * The location of the XRDS document will be null if:
     *              <ul>
     *              <li> the returned status code is different than SC_OK
     *              <li> the Yadis header is not present
     *              <li> there was an HTTP-level error
     *                  (allows fallback to GET + HTML response)
     *              </ul>
     * @throws YadisException if:
     *          <ul>
     *          <li> there's a (lower level) transport error
     *          <li> there are more than one Yadis headers present
     *          </ul>
     */

    private YadisResult retrieveXrdsLocation(
        YadisUrl url, boolean useGet, HttpCache cache, int maxRedirects, Set serviceTypes)
        throws DiscoveryException
    {
        try
        {
            YadisResult result = new YadisResult();
            result.setYadisUrl(url);

            if (DEBUG) _log.debug(
                "Performing HTTP " + (useGet ? "GET" : "HEAD") +
                " on: " + url + " ...");

            HttpRequestOptions requestOptions = cache.getRequestOptions();
            requestOptions.setMaxRedirects(maxRedirects);
            if (useGet)
                requestOptions.addRequestHeader("Accept", YADIS_ACCEPT_HEADER);

            HttpResponse resp = useGet ?
                cache.get(url.getUrl().toString(), requestOptions) :
                cache.head(url.getUrl().toString(), requestOptions);

            Header[] locationHeaders = resp.getResponseHeaders(YADIS_XRDS_LOCATION);
            Header contentType = resp.getResponseHeader("content-type");

            if (HttpStatus.SC_OK != resp.getStatusCode())
            {
                // won't be able to recover from a GET error, throw
                if (useGet)
                    throw new YadisException("GET failed on " + url + " : " +
                        resp.getStatusCode() + ":" + resp.getStatusLine(),
                        OpenIDException.YADIS_GET_ERROR);

                // HEAD is optional, will fall-back to GET
                if (DEBUG)
                    _log.debug("Cannot retrieve " + YADIS_XRDS_LOCATION +
                        " using HEAD from " + url.getUrl().toString() +
                        "; status=" + resp.getStatusLine());
            }
            else if ((locationHeaders != null && locationHeaders.length > 1))
            {
                // fail if there are more than one YADIS_XRDS_LOCATION headers
                throw new YadisException("Found " + locationHeaders.length +
                    " " + YADIS_XRDS_LOCATION + " headers.",
                    useGet ? OpenIDException.YADIS_GET_INVALID_RESPONSE :
                        OpenIDException.YADIS_HEAD_INVALID_RESPONSE);
            }
            else if (locationHeaders != null && locationHeaders.length > 0)
            {
                // we have exactly one xrds location header
                result.setXrdsLocation(locationHeaders[0].getValue(),
                    useGet ? OpenIDException.YADIS_GET_INVALID_RESPONSE :
                        OpenIDException.YADIS_HEAD_INVALID_RESPONSE);
                result.setNormalizedUrl(resp.getFinalUri());
            }
            else if (contentType != null && contentType.getValue() != null &&
                     contentType.getValue().split(";")[0].equalsIgnoreCase(YADIS_CONTENT_TYPE) &&
                     resp.getBody() != null)
            {
                // no location, but got xrds document
                result.setNormalizedUrl(resp.getFinalUri());
                result.setContentType(contentType.getValue());
                if (resp.isBodySizeExceeded())
                    throw new YadisException(
                        "More than " + requestOptions.getMaxBodySize() +
                        " bytes in HTTP response body from " + url,
                        OpenIDException.YADIS_XRDS_SIZE_EXCEEDED);
                result.setEndpoints(XRDS_PARSER.parseXrds(resp.getBody(), serviceTypes));
            }
            else if (resp.getBody() != null)
            {
                // fall-back to html-meta, if present
                String xrdsLocation = getHtmlMeta(resp.getBody());
                if (xrdsLocation != null)
                {
                    result.setNormalizedUrl(resp.getFinalUri());
                    result.setXrdsLocation(xrdsLocation,
                        OpenIDException.YADIS_GET_INVALID_RESPONSE);
                }
            }

            return result;
        }
        catch (IOException e)
        {
            throw new YadisException("I/O transport error: ",
                    OpenIDException.YADIS_HEAD_TRANSPORT_ERROR, e);
        }
    }
}
