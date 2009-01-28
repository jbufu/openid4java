package org.openid4java.discovery.xrds;

import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.OpenIDException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.*;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author jbufu
 */
public class XrdsParserImpl implements XrdsParser
{
    private static final Log _log = LogFactory.getLog(XrdsParserImpl.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private static final String XRDS_SCHEMA = "xrds.xsd";
    private static final String XRD_SCHEMA = "xrd.xsd";
    private static final String XRD_NS = "xri://$xrd*($v*2.0)";
    private static final String XRD_ELEM_XRD = "XRD";
    private static final String XRD_ELEM_TYPE = "Type";
    private static final String XRD_ELEM_URI = "URI";
    private static final String XRD_ELEM_LOCALID = "LocalID";
    private static final String XRD_ELEM_CANONICALID = "CanonicalID";
    private static final String XRD_ATTR_PRIORITY = "priority";


    public List parseXrds(String input, Set targetTypes) throws DiscoveryException
    {
        if (DEBUG)
            _log.debug("Parsing XRDS input for service types: " + Arrays.toString(targetTypes.toArray()));

        Document document = parseXmlInput(input);

        NodeList XRDs = document.getElementsByTagNameNS(XRD_NS, XRD_ELEM_XRD);
        Node lastXRD;
        if (XRDs.getLength() < 1 || (lastXRD = XRDs.item(XRDs.getLength() - 1)) == null)
            throw new DiscoveryException("No XRD elements found.");

        // get the canonical ID, if any (needed for XRIs)
        String canonicalId = null;
        Node canonicalIdNode;
        NodeList canonicalIDs = document.getElementsByTagNameNS(XRD_NS, XRD_ELEM_CANONICALID);
        for (int i = 0; i < canonicalIDs.getLength(); i++) {
            canonicalIdNode = canonicalIDs.item(i);
            if (canonicalIdNode.getParentNode() != lastXRD) continue;
            if (canonicalId != null)
                throw new DiscoveryException("More than one Canonical ID found.");
            canonicalId = canonicalIdNode.getFirstChild() != null && canonicalIdNode.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                canonicalIdNode.getFirstChild().getNodeValue() : null;
        }



        // extract the services that match the specified target types
        NodeList types = document.getElementsByTagNameNS(XRD_NS, XRD_ELEM_TYPE);
        Map serviceTypes = new HashMap();
        Node typeNode, serviceNode;
        for (int i = 0; i < types.getLength(); i++) {
            typeNode = types.item(i);
            String type = typeNode != null && typeNode.getFirstChild() != null && typeNode.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                typeNode.getFirstChild().getNodeValue() : null;
            if (type == null || !targetTypes.contains(type)) continue;

            serviceNode = typeNode.getParentNode();
            if (serviceNode.getParentNode() != lastXRD) continue;

            addServiceType(serviceTypes, serviceNode, type);
        }

        if (DEBUG)
            _log.debug("Found " + serviceTypes.size() + " services for the requested types.");

        // extract local IDs
        NodeList localIDs = document.getElementsByTagNameNS(XRD_NS, XRD_ELEM_LOCALID);
        Map serviceLocalIDs = new HashMap();
        Node localIdNode;
        for (int i = 0; i < localIDs.getLength(); i++) {
            localIdNode = localIDs.item(i);
            if (localIdNode == null || !serviceTypes.containsKey(localIdNode.getParentNode())) continue;

            String localId = localIdNode.getFirstChild() != null && localIdNode.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                localIdNode.getFirstChild().getNodeValue() : null;

            serviceLocalIDs.put(localIdNode.getParentNode(), localId);
        }

        // build XrdsServiceEndpoints for all URIs in the found services
        List result = new ArrayList();
        NodeList uris = document.getElementsByTagNameNS(XRD_NS, XRD_ELEM_URI);
        Node uriNode;
        for (int i = 0; i < uris.getLength(); i++) {
            uriNode = uris.item(i);
            if (uriNode == null || !serviceTypes.containsKey(uriNode.getParentNode())) continue;

            String uri = uriNode.getFirstChild() != null && uriNode.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                uriNode.getFirstChild().getNodeValue() : null;

            serviceNode = uriNode.getParentNode();
            Set typeSet = (Set) serviceTypes.get(serviceNode);

            String localId = (String) serviceLocalIDs.get(serviceNode);

            XrdsServiceEndpoint endpoint = new XrdsServiceEndpoint(uri, typeSet, getPriority(serviceNode), getPriority(uriNode), localId, canonicalId);
            if (DEBUG)
                _log.debug("Discovered endpoint: \n" + endpoint);
            result.add(endpoint);
        }

        Collections.sort(result);
        return result;
    }

    private int getPriority(Node node)
    {
        if (node.hasAttributes())
        {
            Node priority = node.getAttributes().getNamedItem(XRD_ATTR_PRIORITY);
            if (priority != null)
                return Integer.parseInt(priority.getNodeValue());
            else
                return XrdsServiceEndpoint.LOWEST_PRIORITY;
        }

        return 0;
    }

    private Document parseXmlInput(String input) throws DiscoveryException
    {
        if (input == null)
            throw new DiscoveryException("Cannot read XML message",
                OpenIDException.XRDS_DOWNLOAD_ERROR);

        if (DEBUG)
            _log.debug("Parsing XRDS input: " + input);

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(true);
            dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            dbf.setAttribute(JAXP_SCHEMA_SOURCE, new Object[] {
                Discovery.class.getResourceAsStream(XRD_SCHEMA),
                Discovery.class.getResourceAsStream(XRDS_SCHEMA),
            });
            DocumentBuilder builder = dbf.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });

            return builder.parse(new ByteArrayInputStream(input.getBytes()));
        }
        catch (ParserConfigurationException e)
        {
            throw new DiscoveryException("Parser configuration error",
                    OpenIDException.XRDS_PARSING_ERROR, e);
        }
        catch (SAXException e)
        {
            throw new DiscoveryException("Error parsing XML document",
                    OpenIDException.XRDS_PARSING_ERROR, e);
        }
        catch (IOException e)
        {
            throw new DiscoveryException("Error reading XRDS document",
                    OpenIDException.XRDS_DOWNLOAD_ERROR, e);
        }
    }

    private void addServiceType(Map serviceTypes, Node serviceNode, String type)
    {
        Set types = (Set) serviceTypes.get(serviceNode);
        if (types == null)
        {
            types = new HashSet();
            serviceTypes.put(serviceNode, types);
        }
        types.add(type);
    }
}
