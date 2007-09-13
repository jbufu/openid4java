/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery.html;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.Parser;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.NodeIterator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openid4java.util.HttpClientFactory;
import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class HtmlResolver
{
    private static Log _log = LogFactory.getLog(HtmlResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Maximum number of redirects to be followed for the HTTP calls.
     */
    private int _maxRedirects = 10;

    /**
     * Maximum length (in bytes) to read when parsing a HTML response.
     */
    private int _maxHtmlSize = 100000;

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
     * Performs HTML discovery on the supplied URL identifier.
     *
     * @param identifier        The URL identifier.
     * @return                  HTML discovery data obtained from the URL.
     */
    public HtmlResult discover(UrlIdentifier identifier)
            throws DiscoveryException
    {
        // initialize the results of the HTML discovery
        HtmlResult result = new HtmlResult();

        // get the HTML data (and set the claimed identifier)
        String htmlData = call(identifier.getUrl(), result);

        parseHtml(htmlData, result);

        _log.info("HTML discovery succeeded on: " + identifier);

        return result;
    }

    /**
     * Performs a HTTP call on the provided URL identifier.
     *
     * @param url       The URL identifier.
     * @param result    The HTML discovery result, in which the claimed
     *                  identifier is set to the input URL after following
     *                  redirects.
     * @return          The retrieved HTML data.
     */
    private String call(URL url, HtmlResult result) throws DiscoveryException
    {
        HttpClient client = HttpClientFactory.getInstance(
                _maxRedirects, Boolean.TRUE, _socketTimeout, _connTimeout,
                CookiePolicy.IGNORE_COOKIES);

        GetMethod get = new GetMethod(url.toString());
        get.setFollowRedirects(true);

        try
        {
            if (DEBUG) _log.debug("Fetching " + url + "...");

            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK)
                throw new DiscoveryException( "GET failed on " + url +
                    " Received status code: " + statusCode,
                    OpenIDException.DISCOVERY_HTML_GET_ERROR);

            result.setClaimed( new UrlIdentifier(get.getURI().toString()) );

            InputStream htmlInput = get.getResponseBodyAsStream();
            if (htmlInput == null)
                throw new DiscoveryException(
                    "Cannot open inputstream for GET response from " + url,
                    OpenIDException.DISCOVERY_HTML_GET_ERROR);

            byte data[] = new byte[_maxHtmlSize];

            int totalRead = 0;
            int currentRead;
            while (totalRead < _maxHtmlSize)
            {
                currentRead = htmlInput.read(data, totalRead, _maxHtmlSize - totalRead);

                if (currentRead == -1) break;

                totalRead += currentRead;
            }

            htmlInput.close();

            if (totalRead <= 0)
                throw new DiscoveryException("No HTML data read from " + url,
                    OpenIDException.DISCOVERY_HTML_NODATA_ERROR);

            if (DEBUG) _log.debug("Read " + totalRead + " bytes.");

            return new String(data, 0, totalRead);

        } catch (IOException e)
        {
            throw new DiscoveryException("Fatal transport error: ",
                OpenIDException.DISCOVERY_HTML_GET_ERROR, e);
        }
        finally
        {
            get.releaseConnection();
        }
    }

    /**
     * Parses the HTML data and stores in the result the discovered
     * openid information.
     *
     * @param htmlData          HTML data obtained from the URL identifier.
     * @param result            The HTML result.
     */
    private void parseHtml(String htmlData, HtmlResult result)
            throws DiscoveryException
    {
        URL op1Endpoint = null;
        URL op2Endpoint = null;

        if (DEBUG) _log.debug("Parsing HTML data:\n" + htmlData);

        try
        {
            Parser parser = Parser.createParser(htmlData, null);

            NodeList heads = parser.parse(new TagNameFilter("HEAD"));
            if (heads.size() != 1)
                throw new DiscoveryException(
                    "HTML response must have exactly one HEAD element, " +
                    "found " + heads.size() + " : " + heads.toHtml(),
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
            Node head = heads.elementAt(0);
            for (NodeIterator i = head.getChildren().elements();
                 i.hasMoreNodes();)
            {
                Node node = i.nextNode();
                if (node instanceof TagNode)
                {
                    TagNode link = (TagNode) node;
                    String href = link.getAttribute("href");

                    String rel = link.getAttribute("rel");
                    if (rel == null) continue;
                    List relations = Arrays.asList(rel.split(" "));
                    if (relations == null) continue;

                    if (relations.contains("openid.server"))
                    {
                        if (result.getOP1Endpoint() != null)
                            throw new DiscoveryException(
                                "More than one openid.server entries found",
                                OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

                        if (DEBUG)
                            _log.debug("Found OpenID1 endpoint: " + op1Endpoint);

                        result.setEndpoint1(href);
                    }

                    if (relations.contains("openid.delegate"))
                    {
                        if (result.getDelegate1() != null)
                            throw new DiscoveryException(
                                "More than one openid.delegate entries found",
                                OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

                        if (DEBUG)
                            _log.debug("Found OpenID1 delegate: " + href);

                        result.setDelegate1(href);
                    }
                    if (relations.contains("openid2.provider"))
                    {
                        if (result.getOP2Endpoint() != null)
                            throw new DiscoveryException(
                                "More than one openid.server entries found",
                                OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

                        if (DEBUG)
                            _log.debug("Found OpenID2 endpoint: " + op2Endpoint);

                        result.setEndpoint2(href);
                    }

                    if (relations.contains("openid2.local_id"))
                    {
                        if (result.getDelegate2() != null)
                            throw new DiscoveryException(
                                "More than one openid2.local_id entries found",
                                OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

                        if (DEBUG)
                            _log.debug("Found OpenID2 localID: " + href);

                        result.setDelegate2(href);
                    }
                }
            }

            if (DEBUG) _log.debug("HTML discovery result:\n" + result);
        }
        catch (ParserException e)
        {
            throw new DiscoveryException("Error parsing HTML message",
                OpenIDException.DISCOVERY_HTML_PARSE_ERROR, e);
        }
    }
}
