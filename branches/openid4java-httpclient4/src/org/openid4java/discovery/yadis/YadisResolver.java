/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.openxri.xml.XRDS;
import org.apache.http.HttpStatus;
import org.apache.http.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.openid4java.OpenIDException;
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

    private static final String YADIS_PARSER_CLASS_NAME_KEY = "discovery.yadis.parser";
    private static final YadisParser YADIS_PARSER;

    static {
        String className = OpenID4JavaUtils.getProperty(YADIS_PARSER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(YADIS_PARSER_CLASS_NAME_KEY + ":" + className);
        try
        {
            YADIS_PARSER = (YadisParser) Class.forName(className).newInstance();
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
     * @return              YadisResult containing discovered information,
     *                      which includes the XRDS document associated with
     *                      the Yadis URL and additional meta-information
     * @see YadisResult #discover(String, int, HttpCache)
     */
    public YadisResult discover(String url) throws YadisException
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
     * @return              YadisResult containing discovered information,
     *                      which includes the XRDS document associated with
     *                      the Yadis URL and additional meta-information
     * @see YadisResult #discover(String, int, HttpCache)
     */
    public YadisResult discover(String url, HttpCache cache) throws YadisException
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
     * @return              YadisResult containing discovered information,
     *                      which includes the XRDS document associated with
     *                      the Yadis URL and additional meta-information
     * @see YadisResult #discover(String, int, HttpCache)
     */
    public YadisResult discover(String url, int maxRedirects) throws YadisException
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
     * @return              YadisResult containing discovered information,
     *                      which includes the XRDS document associated with
     *                      the Yadis URL and additional meta-information
     * @see YadisResult
     */
    public YadisResult discover(String url, int maxRedirects, HttpCache cache)
        throws YadisException
    {
        // initialize the result
        YadisResult result = new YadisResult();

        YadisUrl yadisUrl = new YadisUrl(url);
        result.setYadisUrl(yadisUrl);

        // try to retrieve the Yadis Descriptor URL with a HEAD call first
        headXrdsUrl(cache, maxRedirects, yadisUrl, result);

        getXrds(cache, maxRedirects, result, false);

        result.setSuccess(true);

        _log.info("Yadis discovery succeeded on " + url);

        return result;
    }

    /**
     * Tries to retrieve the XRDS document via a GET call on the provided URL.
     *
     * @param cache        The HttpClient object to use for placing the call
     * @param maxRedirects
     *@param result        The YadisResult object where discovery results
     *                      will be stored
     * @param secondCall    true on the second (recursive) call @throws YadisException   (hard-fail) if:
     *                      <ul>
     *                      <li>there's a transport error
     *                      <li>the returned status code is different than SC_OK
     *                      </ul>
     */
    private void getXrds(HttpCache cache, int maxRedirects, YadisResult result, boolean secondCall)
            throws YadisException
    {
        // fall-back to the Yadis-URL if we don't know the X-XRDS-Location yet
        String getUrl = result.getXrdsLocation() != null ?
                result.getXrdsLocation().toString() :
                result.getYadisUrl().toString();

        // set the accept header
        Map requestHeaders = new HashMap();
        requestHeaders.put("Accept", YADIS_ACCEPT_HEADER);

        HttpRequestOptions requestOptions = cache.getRequestOptions();
        requestOptions.setRequestHeaders(requestHeaders);
        requestOptions.setMaxRedirects(maxRedirects);

        // todo: remove from cache if XRDS is received on first call

        try
        {
            if (DEBUG)
                _log.debug("Performing HTTP GET #" + (secondCall ? "2" : "1") +
                    " on: " + getUrl + " ...");

            HttpResponse resp = cache.get(getUrl, requestOptions);

            if (resp == null || HttpStatus.SC_OK != resp.getStatusCode())
                throw new YadisException("GET failed on " + getUrl,
                        OpenIDException.YADIS_GET_ERROR);

            // store the normalized / after redirects URL, if not already set
            if (result.getNormalizedUrl() == null)
                    result.setNormalizedUrl(resp.getFinalUri());

            // check first if there's a XRDS download stream available
            Header contentType = resp.getResponseHeader("content-type");
            if ( contentType != null && contentType.getValue() != null &&
                    contentType.getValue()
                    .split(";")[0].equalsIgnoreCase(YADIS_CONTENT_TYPE) )
            {
                if (resp.isBodySizeExceeded())
                    throw new YadisException(
                        "More than " + requestOptions.getMaxBodySize() +
                        " bytes in HTTP response body from " + getUrl,
                        OpenIDException.YADIS_XRDS_SIZE_EXCEEDED);

                XRDS xrds = parseXrds(resp.getBody());

                // todo: only if not set? could be different if redirects were followed
                // if (result.getXrdsLocation == null)
                result.setXrdsLocation(resp.getFinalUri(),
                        OpenIDException.YADIS_GET_INVALID_RESPONSE);

                result.setContentType(YADIS_ACCEPT_HEADER);

                result.setXrds(xrds);

                if (DEBUG)
                    _log.debug("Retrieved Yadis discovery result:\n" + result.dump());
            }
            // try further only if this is not the second GET call already
            else if ( !secondCall )
            {
                // fail if there are more than one YADIS_XRDS_LOCATION headers
                if (resp.getResponseHeaders(YADIS_XRDS_LOCATION).length > 1)
                    throw new YadisException("Found more than one " +
                            YADIS_XRDS_LOCATION + " headers: " +
                            resp.getResponseHeaders(YADIS_XRDS_LOCATION),
                            OpenIDException.YADIS_GET_INVALID_RESPONSE);

                String xrdsLocation = null;
                if (resp.getResponseHeader(YADIS_XRDS_LOCATION) != null)
                {
                    xrdsLocation = resp.getResponseHeader(YADIS_XRDS_LOCATION).getValue();
                    if (DEBUG)
                        _log.debug("Found " + YADIS_XRDS_LOCATION + " header.");
                }

                // XRDS location fallback to <HTML><HEAD><META...
                if (xrdsLocation == null)
                    xrdsLocation = getHtmlMeta(resp.getBody());

                result.setXrdsLocation(xrdsLocation,
                        OpenIDException.YADIS_GET_INVALID_RESPONSE);

                getXrds(cache, maxRedirects, result, true);

            }
            else // secondCall == true
            {
                throw new YadisException("Cannot retrieve XRDS for " +
                        result.getYadisUrl().getUrl().toString(),
                        OpenIDException.YADIS_GET_NO_XRDS);
            }
        }
        catch (IOException e)
        {
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
        String xrdsLocation = null;

        if (input == null)
            throw new YadisException("Cannot download HTML message",
                    OpenIDException.YADIS_HTMLMETA_DOWNLOAD_ERROR);

        xrdsLocation = YADIS_PARSER.getHtmlMeta(input);
        if (DEBUG)
        {
            _log.debug("input:\n" + input);
            _log.debug("xrdsLocation: " + xrdsLocation);
        }
        return xrdsLocation;
    }

    /**
     * Parses the input stream and build a XRDS document from it.
     *
     * @param input         input data stream
     * @return XRDS         Yadis Resource Descriptor (XRDS) document
     * @throws YadisException if the input stream does not contain a valid XRDS
     */
    private XRDS parseXrds(String input) throws YadisException
    {
        XRDS xrds;

        if (input == null)
            throw new YadisException("Cannot read XML message",
                    OpenIDException.YADIS_XRDS_DOWNLOAD_ERROR);

        try
        {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(
                    new ByteArrayInputStream(input.getBytes()));

            xrds = new XRDS(document.getDocumentElement(), false);

            if (DEBUG)
                _log.debug("Retrieved Yadis / XRDS:\n" + xrds.dump());

        // DOM exceptions :
        } catch (ParserConfigurationException e)
        {
            throw new YadisException("Parser configuration error",
                    OpenIDException.YADIS_XRDS_PARSING_ERROR, e);
        } catch (SAXException e)
        {
            throw new YadisException("Error parsing XML document",
                    OpenIDException.YADIS_XRDS_PARSING_ERROR, e);
        // XRDS exceptions :
        } catch (ParseException e)
        {
            // this one seems to come only XRD.fromDom() trying to parse a date
            throw new YadisException("Error parsing XML DATE field",
                    OpenIDException.YADIS_XRDS_PARSING_ERROR, e);
        } catch (URISyntaxException e)
        {
            throw new YadisException("Invalid URI specified in XRDS document",
                    OpenIDException.YADIS_XRDS_PARSING_ERROR, e);
        } catch (IOException e)
        {
            throw new YadisException("Error reading XRDS document",
                    OpenIDException.YADIS_XRDS_DOWNLOAD_ERROR, e);
        }

        return xrds;
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
    private void headXrdsUrl(HttpCache cache, int maxRedirects,
                             YadisUrl url, YadisResult result)
            throws YadisException
    {
        try
        {
            if (DEBUG) _log.debug("Performing HTTP HEAD on: " + url + " ...");

            HttpRequestOptions requestOptions = new HttpRequestOptions();
            requestOptions.setMaxRedirects(maxRedirects);

            HttpResponse resp = cache.head(url.getUrl().toString(), requestOptions);
            if (HttpStatus.SC_OK != resp.getStatusCode())
            {
                if (DEBUG)
                    _log.debug("Cannot retrieve " + YADIS_XRDS_LOCATION +
                        " using HEAD from " + url.getUrl().toString() +
                        "; status=" + resp.getStatusLine());
                return;
            }

            // fail if there are more than one YADIS_XRDS_LOCATION headers
            if (resp.getResponseHeaders(YADIS_XRDS_LOCATION).length > 1)
                throw new YadisException("Found more than one " +
                        YADIS_XRDS_LOCATION + " headers: " +
                        resp.getResponseHeaders(YADIS_XRDS_LOCATION),
                        OpenIDException.YADIS_HEAD_INVALID_RESPONSE);

            Header xrdsLocation = resp.getResponseHeader(YADIS_XRDS_LOCATION);

            if (xrdsLocation != null)
            {
                result.setXrdsLocation(xrdsLocation.getValue(),
                        OpenIDException.YADIS_HEAD_INVALID_RESPONSE);
                result.setNormalizedUrl(resp.getFinalUri());
            }

        }
        catch (IOException e)
        {
            throw new YadisException("I/O transport error: ",
                    OpenIDException.YADIS_HEAD_TRANSPORT_ERROR, e);
        }
    }
}
