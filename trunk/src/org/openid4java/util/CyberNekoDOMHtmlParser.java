/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.cyberneko.html.HTMLTagBalancingListener;
import org.cyberneko.html.parsers.DOMParser;
import org.openid4java.OpenIDException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.html.HtmlParser;
import org.openid4java.discovery.html.HtmlResult;
import org.openid4java.discovery.yadis.YadisException;
import org.openid4java.discovery.yadis.YadisParser;
import org.openid4java.discovery.yadis.YadisResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Sutra Zhou
 * 
 */
public class CyberNekoDOMHtmlParser implements HtmlParser, YadisParser
{
    private static Log _log = LogFactory.getLog(CyberNekoDOMHtmlParser.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /*
     * (non-Javadoc)
     * 
     * @see org.openid4java.discovery.html.HtmlParser#parse(java.lang.String,
     *      org.openid4java.discovery.html.HtmlResult)
     */
    public void parseHtml(String htmlData, HtmlResult result)
            throws DiscoveryException
    {
        if (DEBUG)
            _log.debug("Parsing HTML data:\n" + htmlData);

        Document doc = this.parse(htmlData);
        NodeList heads = doc.getElementsByTagName("head");
        if (heads.getLength() != 1)
            throw new DiscoveryException(
                    "HTML response must have exactly one HEAD element, "
                            + "found " + heads.getLength() + " : "
                            + heads.toString(),
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
        Node head = heads.item(0);
        NodeList headChildNodes = head.getChildNodes();
        for (int i = 0, len = headChildNodes.getLength(); i < len; i++)
        {
            Node node = headChildNodes.item(i);
            if (node.getNodeName().equalsIgnoreCase("link"))
            {
                String[] link = parseNodeLink(node);
                if (link != null)
                {
                    setResult(link[0], link[1], result);
                }
            }
        }

        if (DEBUG)
            _log.debug("HTML discovery result:\n" + result);
    }

    /**
     * Parse attributes <code>rel</code> and <code>href</code> from a
     * <code>link</code> node.
     * 
     * @param link
     * @return a string array with two elements, the first element is
     *         <code>rel</code>, the second one is <code>href</code>. If
     *         one of <code>rel</code> and <code>href</code> is null, return
     *         null.
     */
    private String[] parseNodeLink(Node link)
    {
        String href = null, rel = null;
        NamedNodeMap attributes = link.getAttributes();
        if (attributes != null)
        {
            Node hrefNode = attributes.getNamedItem("href");
            if (hrefNode != null)
            {
                href = hrefNode.getNodeValue();
            }

            Node relNode = attributes.getNamedItem("rel");
            if (relNode != null)
            {
                rel = relNode.getNodeValue();
            }
        }
        return (rel == null || href == null) ? null
                : new String[] { rel, href };
    }

    /**
     * Set the result from <code>rel</code> and <code>href</code> that
     * parsed from node <code>link</code>.
     * 
     * @param rel
     *            the <code>rel</code>
     * @param href
     *            the <code>href</code>
     * @param result
     *            the result to set
     * @throws DiscoveryException
     *             if the value has been setted yet, that is to say, find more
     *             than one entries with the same name(attribute value of
     *             <code>rel</code>).
     */
    private void setResult(String rel, String href, HtmlResult result)
            throws DiscoveryException
    {
        List relations = Arrays.asList(rel.split(" "));

        // openid.server
        if (relations.contains("openid.server"))
        {
            if (result.getOP1Endpoint() != null)
                throw new DiscoveryException(
                        "More than one openid.server entries found",
                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

            if (DEBUG)
                _log.debug("Found OpenID1 endpoint: " + href);

            result.setEndpoint1(href);
        }

        // openid.delegate
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

        // openid2.provider
        if (relations.contains("openid2.provider"))
        {
            if (result.getOP2Endpoint() != null)
                throw new DiscoveryException(
                        "More than one openid.server entries found",
                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);

            if (DEBUG)
                _log.debug("Found OpenID2 endpoint: " + href);

            result.setEndpoint2(href);
        }

        // openid2.local_id
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

    /**
     * Parse the html string into a DOM document.
     * 
     * @param htmlData
     *            the html string to parse
     * @return a doument
     * @throws DiscoveryException
     *             if parse failed.
     */
    private Document parse(String htmlData) throws DiscoveryException
    {
    	OpenID4JavaDOMParser parser = new OpenID4JavaDOMParser();
        try
        {
            parser.parse(this.createInputSource(htmlData));
        }
        catch (Exception e)
        {
            throw new DiscoveryException("Error parsing HTML message",
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR, e);
        }

        if (parser.isIgnoredHeadStartElement())
        {
            throw new DiscoveryException(
                    "HTML response must have exactly one HEAD element.",
                    OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
        }

        return parser.getDocument();
    }

    /**
     * Create an InputSource form a String.
     * 
     * @param s
     *            the String
     * @return an InputSource
     * @throws NullPointerException
     *             if s is null.
     */
    private InputSource createInputSource(String s)
    {
        try
        {
            return new InputSource(
                    new ByteArrayInputStream(s.getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openid4java.discovery.yadis.YadisParser#getHtmlMeta(java.lang.String)
     */
    public String getHtmlMeta(String input) throws YadisException
    {
        String xrdsLocation = null;
        OpenID4JavaDOMParser parser = new OpenID4JavaDOMParser();
        try
        {
            parser.parse(this.createInputSource(input));
        }
        catch (Exception e)
        {
            throw new YadisException("Error parsing HTML message",
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, e);
        }

        if (parser.isIgnoredHeadStartElement())
        {
        	throw new YadisException("HTML response must have exactly one HEAD element.",
        			OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }

        Document doc = parser.getDocument();
        if (DEBUG)
        {
            _log.debug("document:\n" + toXmlString(doc));
        }
        NodeList heads = doc.getElementsByTagName("head");

        if (heads.getLength() != 1)
            throw new YadisException(
                    "HTML response must have exactly one HEAD element, "
                            + "found " + heads.getLength() + " : "
                            + heads.toString(),
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

        Node head = heads.item(0);
        NodeList headChildNodes = head.getChildNodes();
        for (int i = 0, len = headChildNodes.getLength(); i < len; i++)
        {
            Node node = headChildNodes.item(i);
            if (DEBUG)
            {
                _log.debug("Node name: " + node.getNodeName());
            }
            if (node.getNodeName().equalsIgnoreCase("meta"))
            {
                NamedNodeMap attributes = node.getAttributes();
                if (attributes == null)
                {
                    _log.debug("attributes is null.");
                    continue;
                }

                Node httpEquivNode = attributes.getNamedItem("http-equiv");
                if (httpEquivNode == null)
                {
                    _log.debug("httpEquivNode is null.");
                    continue;
                }

                String httpEquiv = httpEquivNode.getNodeValue();
                if (httpEquiv != null
                        && httpEquiv
                                .equalsIgnoreCase(YadisResolver.YADIS_XRDS_LOCATION))
                {
                    if (xrdsLocation != null)
                        throw new YadisException(
                                "More than one "
                                        + YadisResolver.YADIS_XRDS_LOCATION
                                        + "META tags found in HEAD: "
                                        + head.toString(),
                                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

                    xrdsLocation = attributes.getNamedItem("content")
                            .getNodeValue();
                    if (DEBUG)
                        _log.debug("Found " + YadisResolver.YADIS_XRDS_LOCATION
                                + "META tags.");
                }
            }
        }
        return xrdsLocation;
    }

    private static String toXmlString(Document doc)
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer;
        try
        {
            transformer = factory.newTransformer();
        }
        catch (TransformerConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        DOMSource source = new DOMSource(doc);
        StringWriter xmlString = new StringWriter();
        StreamResult streamResult = new StreamResult(xmlString);
        try
        {
            transformer.transform(source, streamResult);
        }
        catch (TransformerException e)
        {
            throw new RuntimeException(e);
        }
        return xmlString.toString();
    }
}

class OpenID4JavaDOMParser extends DOMParser implements HTMLTagBalancingListener
{

    private boolean ignoredHeadStartElement;

    public boolean isIgnoredHeadStartElement()
    {
        return ignoredHeadStartElement;
    }

    public void ignoredEndElement(QName element, Augmentations augs)
    {
        // Do nothing.
    }

    public void ignoredStartElement(QName element, XMLAttributes attrs, Augmentations augs)
    {
        if (element.rawname.equals("HEAD"))
        {
            this.ignoredHeadStartElement = true;
        }
    }
}