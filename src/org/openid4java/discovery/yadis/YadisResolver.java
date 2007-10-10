/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.htmlparser.Parser;
import org.htmlparser.Node;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.openxri.xml.XRDS;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.openid4java.util.HttpClientFactory;
import org.openid4java.OpenIDException;

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
 * <li> max HTML size (default 100,000 bytes): maximum size (in bytes)
 *      that will be retrived and parsed while trying to extract
 *      the XRDS location from the <HEAD><META> tag
 * <li> max XML size (default 100,000 bytes): maximum size (in bytes) that will
 *      be accepted for XRDS document
 * <li> HTTP connect timeout (default 3 seconds)
 * <li> HTTP read timeout (default 5 seconds)
 * </ul>
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisResolver
{
    private static Log _log = LogFactory.getLog(YadisResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    // Yadis constants
    private static final String YADIS_XRDS_LOCATION = "X-XRDS-Location";
    private static final String YADIS_CONTENT_TYPE = "application/xrds+xml";
    private static final String YADIS_ACCEPT_HEADER =
            "text/html; q=0.3, application/xhtml+xml; q=0.5, " +
                    YADIS_CONTENT_TYPE;

    /**
     * Maximum number of redirects to be followed for the HTTP calls.
     * Defalut 10.
     */
    private int _maxRedirects = 10;

    /**
     * Maximum length (in bytes) to read when parsing a HTML response.
     */
    private int _maxHtmlSize = 100000;

    /**
     * Maximum size in bytes for the XRDS document.
     */
    private int _maxXmlSize = 100000;

    /**
     * HTTP connect timeout, in milliseconds.
     */
    private int _connTimeout = 3000;

    /**
     * HTTP socket (read) timeout, in milliseconds.
     */
    private int _socketTimeout = 5000;

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
     * Gets the aximum length (in bytes) to read when parsing a HTML response.
     */
    public int getMaxHtmlSize()
    {
        return _maxHtmlSize;
    }

    /**
     * Sets maximum length (in bytes) to read when parsing a HTML response.
     */
    public void setMaxHtmlSize(int maxHtmlSize)
    {
        this._maxHtmlSize = maxHtmlSize;
    }

    /**
     * Gets the maximum size in bytes for the XRDS document.
     */
    public int getMaxXmlSize()
    {
        return _maxXmlSize;
    }

    /**
     * Sets the maximum size in bytes for the XRDS document.
     */
    public void setMaxXmlSize(int maxXmlSize)
    {
        this._maxXmlSize = maxXmlSize;
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
     * @see YadisResult, #discover(String, int)
     */
    public YadisResult discover(String url) throws YadisException
    {
        return discover(url, _maxRedirects);
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
     * @see YadisResult
     */
    public YadisResult discover(String url, int maxRedirects)
        throws YadisException
    {
        HttpClient client = HttpClientFactory.getInstance(
                maxRedirects, Boolean.TRUE, _socketTimeout, _connTimeout,
                CookiePolicy.IGNORE_COOKIES);

        // initialize the result
        YadisResult result = new YadisResult();

        YadisUrl yadisUrl = new YadisUrl(url);
        result.setYadisUrl(yadisUrl);

        // try to retrieve the Yadis Descriptor URL with a HEAD call first
        headXrdsUrl(client, yadisUrl, result);

        getXrds(client, result, false);

        result.setSuccess(true);

        _log.info("Yadis discovery succeeded on " + url);

        return result;
    }

    /**
     * Tries to retrieve the XRDS document via a GET call on the provided URL.
     *
     * @param client        The HttpClient object to use for placing the call
     * @param result        The YadisResult object where discovery results
     *                      will be stored
     * @param secondCall    true on the second (recursive) call
     * @throws YadisException   (hard-fail) if:
     *                      <ul>
     *                      <li>there's a transport error
     *                      <li>the returned status code is different than SC_OK
     *                      </ul>
     */
    private void getXrds(HttpClient client, YadisResult result, boolean secondCall)
            throws YadisException
    {
        // fall-back to the Yadis-URL if we don't know the X-XRDS-Location yet
        String getUrl = result.getXrdsLocation() != null ?
                result.getXrdsLocation().toString() :
                result.getYadisUrl().toString();

        GetMethod get = new GetMethod(getUrl);
        get.setFollowRedirects(true);

        // set the accept header
        get.setRequestHeader("Accept", YADIS_ACCEPT_HEADER);

        try
        {
            if (DEBUG)
                _log.debug("Performing HTTP GET #" + (secondCall ? "2" : "1") +
                    " on: " + getUrl + " ...");

            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK)
                throw new YadisException("GET failed on " + getUrl,
                        OpenIDException.YADIS_GET_ERROR);

            // store the normalized / after redirects URL, if not already set
            if (result.getNormalizedUrl() == null)
                    result.setNormalizedUrl(get.getURI().toString());

            // check first if there's a XRDS download stream available
            Header contentType = get.getResponseHeader("content-type");
            if ( contentType != null && contentType.getValue() != null &&
                    contentType.getValue()
                    .split(";")[0].equalsIgnoreCase(YADIS_CONTENT_TYPE) )
            {
                XRDS xrds = parseXrds(get.getResponseBodyAsStream());

                // todo: only if not set? could be different if redirects were followed
                // if (result.getXrdsLocation == null)
                result.setXrdsLocation(get.getURI().toString(),
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
                if (get.getResponseHeaders(YADIS_XRDS_LOCATION).length > 1)
                    throw new YadisException("Found more than one " +
                            YADIS_XRDS_LOCATION + " headers: " +
                            get.getResponseHeaders(YADIS_XRDS_LOCATION),
                            OpenIDException.YADIS_GET_INVALID_RESPONSE);

                String xrdsLocation = null;
                if (get.getResponseHeader(YADIS_XRDS_LOCATION) != null)
                {
                    xrdsLocation = get.getResponseHeader(YADIS_XRDS_LOCATION).getValue();
                    if (DEBUG)
                        _log.debug("Found " + YADIS_XRDS_LOCATION + " header.");
                }

                // XRDS location fallback to <HTML><HEAD><META...
                if (xrdsLocation == null)
                    xrdsLocation = getHtmlMeta(get.getResponseBodyAsStream());

                result.setXrdsLocation(xrdsLocation,
                        OpenIDException.YADIS_GET_INVALID_RESPONSE);

                getXrds(client, result, true);

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
        finally
        {
            get.releaseConnection();
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
    private String getHtmlMeta(InputStream input) throws YadisException
    {
        String xrdsLocation = null;

        if (input == null)
            throw new YadisException("Cannot download HTML message",
                    OpenIDException.YADIS_HTMLMETA_DOWNLOAD_ERROR);

        try
        {
            byte data[] = new byte[_maxHtmlSize];

            int bytesRead = input.read(data);
            input.close();

            // parse and extract the needed info
            if (bytesRead <= 0)
                throw new YadisException("No data read from the HTML message",
                        OpenIDException.YADIS_HTMLMETA_DOWNLOAD_ERROR);

            Parser parser = Parser.createParser(new String(data, 0, bytesRead), null);
            NodeList heads = parser.parse(new TagNameFilter("HEAD"));

            if (heads.size() != 1)
                throw new YadisException(
                        "HTML response must have exactly one HEAD element, " +
                                "found " + heads.size() + " : "
                                + heads.toHtml(),
                        OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

            Node head = heads.elementAt(0);
            for (NodeIterator i = head.getChildren().elements();
                 i.hasMoreNodes();)
            {
                Node node = i.nextNode();
                if (node instanceof MetaTag)
                {
                    MetaTag meta = (MetaTag) node;
                    if ( meta.getHttpEquiv() != null &&
                            meta.getHttpEquiv().equalsIgnoreCase(YADIS_XRDS_LOCATION) )
                    {
                        if ( xrdsLocation != null )
                            throw new YadisException(
                                "More than one " + YADIS_XRDS_LOCATION +
                                "META tags found in HEAD: " + head.toHtml(),
                                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

                        xrdsLocation = meta.getMetaContent();
                        if (DEBUG)
                            _log.debug("Found " + YADIS_XRDS_LOCATION + "META tags.");
                    }
                }
            }
        } catch (IOException e)
        {
            throw new YadisException("I/O error while reading HTML message",
                    OpenIDException.YADIS_HTMLMETA_DOWNLOAD_ERROR, e);
        } catch (ParserException pe)
        {
            throw new YadisException("Error parsing HTML message",
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, pe);
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
    private XRDS parseXrds(InputStream input) throws YadisException
    {
        XRDS xrds;

        if (input == null)
            throw new YadisException("Cannot read XML message",
                    OpenIDException.YADIS_XRDS_DOWNLOAD_ERROR);

        try
        {
            byte[] data = new byte[_maxXmlSize];
            int bytesRead = input.read(data);

            if (input.read() != -1)
                throw new YadisException("XRDS stream exceeds max allowed size: "
                         + _maxXmlSize, OpenIDException.YADIS_XRDS_SIZE_EXCEEDED);
            input.close();

            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(
                    new ByteArrayInputStream(data, 0, bytesRead));

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
     * @param client        The HttpClient object to use for placing the call
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
     *
     * @throws YadisException if:
     *          <ul>
     *          <li> there's a (lower level) transport error
     *          <li> there are more than one Yadis headers present
     *          </ul>
     */
    private void headXrdsUrl(HttpClient client, YadisUrl url, YadisResult result)
            throws YadisException
    {
        HeadMethod head = new HeadMethod(url.getUrl().toString());
        head.setFollowRedirects(true);

        try
        {
            if (DEBUG) _log.debug("Performing HTTP HEAD on: " + url + " ...");

            int statusCode = client.executeMethod(head);
            if (statusCode != HttpStatus.SC_OK)
            {
                if (DEBUG)
                    _log.debug("Cannot retrieve " + YADIS_XRDS_LOCATION +
                        " using HEAD from " + url.getUrl().toString() +
                        "; status=" + head.getStatusLine());
                return;
            }

            // fail if there are more than one YADIS_XRDS_LOCATION headers
            if (head.getResponseHeaders(YADIS_XRDS_LOCATION).length > 1)
                throw new YadisException("Found more than one " +
                        YADIS_XRDS_LOCATION + " headers: " +
                        head.getResponseHeaders(YADIS_XRDS_LOCATION),
                        OpenIDException.YADIS_HEAD_INVALID_RESPONSE);

            Header xrdsLocation = head.getResponseHeader(YADIS_XRDS_LOCATION);

            if (xrdsLocation != null)
            {
                result.setXrdsLocation(xrdsLocation.getValue(),
                        OpenIDException.YADIS_HEAD_INVALID_RESPONSE);
                result.setNormalizedUrl(head.getURI().toString());
            }

        }
        catch (HttpException e)
        {
            _log.error("HTTP error during HEAD request on: " + url, e);
        }
        catch (IOException e)
        {
            throw new YadisException("I/O transport error: ",
                    OpenIDException.YADIS_HEAD_TRANSPORT_ERROR, e);
        }
        finally
        {
            head.releaseConnection();
        }
    }
}
