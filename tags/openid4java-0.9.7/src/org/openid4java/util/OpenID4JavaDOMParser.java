/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.cyberneko.html.HTMLTagBalancingListener;
import org.cyberneko.html.parsers.DOMParser;
import org.openid4java.discovery.RuntimeDiscoveryException;
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLHtmlElement;
import org.xml.sax.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * A DOMParser extends from Cyberneko HTML.
 * <p>
 * This extended parser marks that a(or more) HTML element <code>head</code> is
 * ignored while parsing.
 * </p>
 * 
 * @author Sutra Zhou
 * @see <a href="http://nekohtml.sourceforge.net/index.html">NekoHTML</a>
 * @since 0.9.4
 */
public class OpenID4JavaDOMParser extends DOMParser implements HTMLTagBalancingListener
{
    /**
     * Create an InputSource form a String.
     * 
     * @param s
     *            the String
     * @return an InputSource
     * @throws NullPointerException
     *             if s is null.
     */
    public static InputSource createInputSource(String s)
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

    /**
     * Transform the document to string.
     * 
     * @param doc the document
     * @return a string
     * @throws TransformerException If an unrecoverable error occurs
     *   during the course of the transformation.
     */
    public static String toXmlString(Document doc) throws TransformerException
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
        transformer.transform(source, streamResult);
        return xmlString.toString();
    }

    private boolean ignoredHeadStartElement;

    /**
     * @see <a href="http://nekohtml.sourceforge.net/settings.html">NekoHTML | Parser Settings</a>
     */
    public OpenID4JavaDOMParser() {
        try
        {
            this.setFeature("http://xml.org/sax/features/namespaces", false);
            this.setFeature("http://xml.org/sax/features/validation", false);
            this.setFeature("http://xml.org/sax/features/external-general-entities", false);
            this.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            this.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            this.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            this.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    throw new RuntimeDiscoveryException("External entity found in input data" );
                }
            });
        }
        catch (SAXNotRecognizedException e)
        {
            // Do nothing as this exception will not happen.
        }
        catch (SAXNotSupportedException e)
        {
            // Do nothing as this exception will not happen.
        }
    }

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
        if (element.rawname.equals("HEAD")
                && this.fCurrentNode instanceof HTMLHtmlElement)
        {
            this.ignoredHeadStartElement = true;
        }
    }
}