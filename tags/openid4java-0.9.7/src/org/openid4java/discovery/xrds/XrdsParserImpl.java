package org.openid4java.discovery.xrds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.RuntimeDiscoveryException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

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
    private static final String OPENID_NS = "http://openid.net/xmlns/1.0";
    private static final String OPENID_ELEM_DELEGATE = "Delegate";


    public List parseXrds(String input, Set targetTypes) throws DiscoveryException
    {
        if (DEBUG)
            _log.debug("Parsing XRDS input for service types: " + targetTypes.toString());

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
        Set selectedServices = new HashSet();
        Node typeNode, serviceNode;
        for (int i = 0; i < types.getLength(); i++) {
            typeNode = types.item(i);
            String type = typeNode != null && typeNode.getFirstChild() != null && typeNode.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                typeNode.getFirstChild().getNodeValue() : null;
            if (type == null) continue;

            serviceNode = typeNode.getParentNode();
            if (serviceNode.getParentNode() != lastXRD) continue;

            if (targetTypes.contains(type))
                selectedServices.add(serviceNode);
            addServiceType(serviceTypes, serviceNode, type);
        }

        if (DEBUG)
            _log.debug("Found " + serviceTypes.size() + " services for the requested types.");

        // extract local IDs
        Map serviceLocalIDs = extractElementsByParent(XRD_NS, XRD_ELEM_LOCALID, selectedServices, document);
        Map serviceDelegates = extractElementsByParent(OPENID_NS, OPENID_ELEM_DELEGATE, selectedServices, document);

        // build XrdsServiceEndpoints for all URIs in the found services
        List result = new ArrayList();
        NodeList uris = document.getElementsByTagNameNS(XRD_NS, XRD_ELEM_URI);
        Node uriNode;
        for (int i = 0; i < uris.getLength(); i++) {
            uriNode = uris.item(i);
            if (uriNode == null || !selectedServices.contains(uriNode.getParentNode())) continue;

            String uri = uriNode.getFirstChild() != null && uriNode.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                uriNode.getFirstChild().getNodeValue() : null;

            serviceNode = uriNode.getParentNode();
            Set typeSet = (Set) serviceTypes.get(serviceNode);

            String localId = (String) serviceLocalIDs.get(serviceNode);
            String delegate = (String) serviceDelegates.get(serviceNode);

            XrdsServiceEndpoint endpoint = new XrdsServiceEndpoint(uri, typeSet, getPriority(serviceNode), getPriority(uriNode), localId, delegate, canonicalId);
            if (DEBUG)
                _log.debug("Discovered endpoint: \n" + endpoint);
            result.add(endpoint);
        }

        Collections.sort(result);
        return result;
    }

    private Map extractElementsByParent(String ns, String elem, Set parents, Document document)
    {
        Map result = new HashMap();
        NodeList nodes = document.getElementsByTagNameNS(ns, elem);
        Node node;
        for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            if (node == null || !parents.contains(node.getParentNode())) continue;

            String localId = node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.TEXT_NODE ?
                node.getFirstChild().getNodeValue() : null;

            result.put(node.getParentNode(), localId);
        }
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
            dbf.setExpandEntityReferences(false);

            dbf.setFeature("http://xml.org/sax/features/validation", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

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

            builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    throw new RuntimeDiscoveryException("External entity found in XRDS data");
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
        catch (RuntimeDiscoveryException rde)
        {
            throw new DiscoveryException(rde.getMessage());
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
