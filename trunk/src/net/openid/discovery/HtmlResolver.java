/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.discovery;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
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
import java.net.MalformedURLException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class HtmlResolver
{
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

    public DiscoveryInformation discover(UrlIdentifier identifier)
            throws DiscoveryException
    {
        // results of the HTML discovery
        URL idpEndpoint = null;
        Identifier claimed;
        Identifier delegate = null;

        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.protocol.max-redirects",
                new Integer(_maxRedirects));
        client.getParams().setParameter("http.protocol.allow-circular-redirects",
                Boolean.TRUE);
        client.getParams().setSoTimeout(_socketTimeout);
        client.getHttpConnectionManager()
                .getParams().setConnectionTimeout(_connTimeout);

        GetMethod get = new GetMethod(identifier.getIdentifier());
        get.setFollowRedirects(true);

        try
        {
            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK)
                throw new DiscoveryException(
                        "GET failed on " + identifier.getIdentifier());

            claimed = new UrlIdentifier(get.getURI().toString());

            InputStream htmlInput = get.getResponseBodyAsStream();
            if (htmlInput == null)
                throw new DiscoveryException("Cannot download HTML mesage from "
                        + identifier.getIdentifier());

            byte data[] = new byte[_maxHtmlSize];

            int bytesRead = htmlInput.read(data);
            htmlInput.close();

            // parse and extract the needed info
            if (bytesRead <= 0)
                throw new DiscoveryException("No data read from the HTML message");

            Parser parser = Parser.createParser(new String(data, 0, bytesRead), null);

            NodeList heads = parser.parse(new TagNameFilter("HEAD"));
            if (heads.size() != 1)
                throw new DiscoveryException(
                        "HTML response must have exactly one HEAD element, " +
                                "found " + heads.size() + " : " + heads.toHtml());

            Node head = heads.elementAt(0);
            for (NodeIterator i = head.getChildren().elements();
                 i.hasMoreNodes();)
            {
                Node node = i.nextNode();
                if (node instanceof TagNode)
                {
                    TagNode link = (TagNode) node;
                    String rel = link.getAttribute("rel");
                    String href = link.getAttribute("href");

                    if ("openid.server".equals(rel))
                    {
                        if (idpEndpoint != null)
                            throw new DiscoveryException(
                                    "More than one openid.server entries found");
                        try
                        {
                            idpEndpoint = new URL(href);

                        } catch (MalformedURLException e)
                        {
                            throw new DiscoveryException(
                                    "Invalid openid.server URL: " + href);
                        }
                    }

                    if ("openid.delegate".equals(rel))
                    {
                        if (idpEndpoint != null)
                            throw new DiscoveryException(
                                    "More than one openid.delegate entries found");

                        delegate = new UrlIdentifier(href);
                    }
                }
            }

            return new DiscoveryInformation(idpEndpoint, claimed, delegate,
                    DiscoveryInformation.OPENID11);


        } catch (IOException e)
        {
            throw new DiscoveryException("Fatal transport error: ", e);
        } catch (ParserException e)
        {
            throw new DiscoveryException("Error parsing HTML message", e);
        }
        finally
        {
            get.releaseConnection();
        }

    }
}
