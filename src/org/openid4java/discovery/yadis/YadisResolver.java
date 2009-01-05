/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.openxri.xml.XRDS;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Header;
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
    private static final YadisHtmlParser YADIS_HTML_PARSER;

    static {
        String className = OpenID4JavaUtils.getProperty(YADIS_PARSER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(YADIS_PARSER_CLASS_NAME_KEY + ":" + className);
        try
        {
            YADIS_HTML_PARSER = (YadisHtmlParser) Class.forName(className).newInstance();
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
        YadisUrl yadisUrl = new YadisUrl(url);

        // try to retrieve the Yadis Descriptor URL with a HEAD call first
        YadisResult result = retrieveXrdsLocation(yadisUrl, false, cache, maxRedirects);

        // try GET
        if (result.getXrdsLocation() == null)
            result = retrieveXrdsLocation(yadisUrl, true, cache, maxRedirects);

        if (result.getXrdsLocation() != null)
        {
            retrieveXrdsDocument(result, cache, maxRedirects);
        }
        else if (result.getXrds() != null)
        {
            // report the yadis url as the xrds location
            result.setXrdsLocation(url, OpenIDException.YADIS_INVALID_URL);
        }
        else
        {
            throw new YadisException("Cannot retrieve XRDS for " + url,
                    OpenIDException.YADIS_GET_NO_XRDS);
        }

        result.setSuccess(true);
        _log.info("Yadis discovery succeeded on " + url);

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
    private void retrieveXrdsDocument(YadisResult result, HttpCache cache, int maxRedirects)
        throws YadisException {

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
            result.setXrds(parseXrds(resp.getBody()));

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

    private YadisResult retrieveXrdsLocation(YadisUrl url, boolean useGet,
                                             HttpCache cache, int maxRedirects)
            throws YadisException
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
                result.setXrds(parseXrds(resp.getBody()));
            }
            else if (resp.getBody() != null)
            {
                // fall-back to html-meta
                result.setNormalizedUrl(resp.getFinalUri());
                result.setXrdsLocation(getHtmlMeta(resp.getBody()),
                    OpenIDException.YADIS_GET_INVALID_RESPONSE);
            }

            return result;
        }
        catch (HttpException e)
        {
            throw new YadisException("HTTP error during HEAD request on: " + url,
                    OpenIDException.YADIS_HEAD_TRANSPORT_ERROR, e);
        }
        catch (IOException e)
        {
            throw new YadisException("I/O transport error: ",
                    OpenIDException.YADIS_HEAD_TRANSPORT_ERROR, e);
        }
    }
}
