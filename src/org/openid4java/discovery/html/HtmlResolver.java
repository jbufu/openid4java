/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery.html;

import org.apache.commons.httpclient.HttpStatus;
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
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpResponse;
import org.openid4java.util.HttpRequestOptions;
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

            parseHtml(resp.getBody(), result);
        }
        catch (IOException e)
        {
            throw new DiscoveryException("Fatal transport error: ",
                    OpenIDException.DISCOVERY_HTML_GET_ERROR, e);
        }

        _log.info("HTML discovery completed on: " + identifier);

        return result;
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
