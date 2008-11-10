/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard;

import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.OpenIDException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Models the OpenID Infocard token used to transport OpenID messages.
 * An OpenID token encapsulates an OpenID message in key-value form into
 * an <openid:OpenIDToken> element.
 * <p>
 * Provides functionality for OPs / Servers to create OpenID tokens from
 * OpenID messages, and for RPs / Consumers to parse received tokens into
 * OpenID messages.
 */
public class OpenIDToken
{
    private static Log _log = LogFactory.getLog(OpenIDToken.class);
    private static final boolean DEBUG = _log.isDebugEnabled();
             
    /**
     * Token type data structure.
     */
    private OpenIDTokenType _tokenType;

    /**
     * The encapsulated OpenID Message.
     */
    private Message _openidMessage;

    /**
     * Constructs an OpenID token encapsulating the provided OpenID Message.
     * Should be used on the OP/STS side to generate a RSTR.
     *
     * @param openidMessage     The OpenID message obtained from
     *                          ServerManager.authResponse().
     */
    public OpenIDToken(Message openidMessage)
    {
        setOpenIDMessage(openidMessage);

        if (DEBUG)
            _log.debug("Created " + _tokenType +" token");
    }

    /**
     * Parses the data posted by the selector into an OpenID token.
     * Should be used on the RP side.
     *
     * @param       xmlToken The "xmlToken" parameter posted by the selector.
     * @return      An OpenIDToken encapsulating the OpenID AuthResponse.
     */
    public static OpenIDToken createFromXmlToken(String xmlToken)
        throws InfocardException
    {
        if (xmlToken == null)
            throw new InfocardException("Error processing xmlToken: null value");

        if (DEBUG)
            _log.debug("Processing xmlToken: " + xmlToken);

        try
        {
            DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder =
                    documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(
                    new ByteArrayInputStream(xmlToken.getBytes("utf-8")));

            String keyValueForm;
            try
            {
                keyValueForm = document.getElementsByTagNameNS(
                    Message.OPENID2_NS, "OpenIDToken")
                    .item(0).getFirstChild().getNodeValue();
            }
            catch (Exception e)
            {
                throw new InfocardException(
                    "Error extracting OpenID message from the xmlToken", e);
            }

            Message message = Message.createMessage(
                ParameterList.createFromKeyValueForm(keyValueForm));

            return new OpenIDToken(message);

        // DOM exceptions :
        }
        catch (ParserConfigurationException e)
        {
            throw new InfocardException("Parser configuration error", e);
        }
        catch (SAXException e)
        {
            throw new InfocardException("Error parsing XML token document", e);
        }
        catch (IOException e)
        {
            throw new InfocardException("Error reading xmlToken document", e);
        }
        catch (OpenIDException e)
        {
            throw new InfocardException("Error building OpenID message from xmlToken", e);
        }
    }

    /**
     * Gets the OpenID message contained in the OpenID token.
     */
    public Message getOpenIDMessage()
    {
        return _openidMessage;
    }

    /**
     * Gets the OpenID message as a ParameterList.
     * @return  ParameterList containing the OpenID message.
     */
    public ParameterList getOpenIDParams()
    {
        return new ParameterList(_openidMessage.getParameterMap());
    }

    /**
     * Sets the OpenID Message to encapsulate into the token.
     */
    public void setOpenIDMessage(Message openidMessage)
    {
        this._openidMessage = openidMessage;

        if (OpenIDTokenType.OPENID20_TOKEN.toString().equals(
                    openidMessage.getParameterValue("openid.ns")))
            _tokenType = OpenIDTokenType.OPENID20_TOKEN;

        else
            _tokenType = OpenIDTokenType.OPENID11_TOKEN;
    }

    /**
     * Gets the OpenID token type.
     *
     * @see org.openid4java.infocard.OpenIDTokenType
     */
    public OpenIDTokenType getTokenType()
    {
        return _tokenType;
    }

    /**
     * Generates the XML string representation of the OpenID token.
     */
    public String getToken()
    {
        StringBuffer token = new StringBuffer();

        token.append("<openid:OpenIDToken xmlns:openid=\"" +
                        Message.OPENID2_NS + "\">");

        token.append(_openidMessage.keyValueFormEncoding());

        token.append("</openid:OpenIDToken>");

        return token.toString();
    }
}
