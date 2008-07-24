/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.html.dom.HTMLDocumentImpl;
import org.openid4java.OpenIDException;
import org.openid4java.discovery.yadis.YadisException;
import org.openid4java.discovery.yadis.YadisParser;
import org.openid4java.discovery.yadis.YadisResolver;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLHeadElement;
import org.w3c.dom.html.HTMLMetaElement;

/**
 * @author Sutra Zhou
 * 
 */
public class CyberNekoDOMYadisParser implements YadisParser
{
    private static final Log _log = LogFactory.getLog(CyberNekoDOMYadisParser.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /*
     * (non-Javadoc)
     * 
     * @see org.openid4java.discovery.yadis.YadisParser#getHtmlMeta(java.lang.String)
     */
    public String getHtmlMeta(String input) throws YadisException
    {
        String xrdsLocation = null;

        HTMLDocumentImpl doc = this.parseDocument(input);
        if (DEBUG)
        {
            _log.debug("document:\n" + OpenID4JavaDOMParser.toXmlString(doc));
        }

        NodeList heads = doc.getElementsByTagName("head");
        if (heads.getLength() != 1)
            throw new YadisException(
                    "HTML response must have exactly one HEAD element, "
                            + "found " + heads.getLength() + " : "
                            + heads.toString(),
                    OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

        HTMLHeadElement head = (HTMLHeadElement) doc.getHead();
        NodeList metaElements = head.getElementsByTagName("META");
        for (int i = 0, len = metaElements.getLength(); i < len; i++)
        {
        	HTMLMetaElement metaElement = (HTMLMetaElement) metaElements.item(i);

            String httpEquiv = metaElement.getHttpEquiv();
            if (YadisResolver.YADIS_XRDS_LOCATION.equalsIgnoreCase(httpEquiv))
            {
                if (xrdsLocation != null)
                    throw new YadisException(
                            "More than one "
                                    + YadisResolver.YADIS_XRDS_LOCATION
                                    + "META tags found in HEAD: "
                                    + head.toString(),
                            OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);

                xrdsLocation = metaElement.getContent();
                if (DEBUG)
                    _log.debug("Found " + YadisResolver.YADIS_XRDS_LOCATION
                            + "META tags.");
            }
        }
        return xrdsLocation;
    }

    private HTMLDocumentImpl parseDocument(String htmlData) throws YadisException
    {
        OpenID4JavaDOMParser parser = new OpenID4JavaDOMParser();
        try
        {
            parser.parse(OpenID4JavaDOMParser.createInputSource(htmlData));
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

        return (HTMLDocumentImpl) parser.getDocument();
    }
}
