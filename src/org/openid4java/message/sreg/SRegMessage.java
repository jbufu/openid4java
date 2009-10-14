/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.sreg;

import org.openid4java.message.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for the Simple Registration implementation.
 * <p>
 * Encapsulates:
 * <ul>
 * <li> the Type URI that identifies the Simple Registration extension
 * <li> a list of extension-specific parameters, with the
 * openid.<extension_alias> prefix removed
 * <li> methods for handling the extension-specific support of parameters with
 * multpile values
 * </ul>
 *
 * Considering that:
 * <ul>
 * <li>SREG 1.0 and SREG 1.1 use the same type URI
 * ("http://openid.net/sreg/1.0") in XRDS documents</li>
 * <li>The only differences between the two are the hardcoded "sreg" extension
 * alias in SREG1.0 and the openid.ns.<ext_alias> namespace declaration
 * in SREG1.1</li>
 * </ul>
 *
 * Support for Simple Registration is implemented as follows:
 * <ul>
 * <li>Both SREG1.0 and SREG1.1 are implemented using the same extension
 * framework</li>
 * <li>In OpenID2 messages only SREG1.1 is accepted, i.e. the namespace
 * declaration (openid.ns.<ext_alias>) MUST be present</li>
 * <li>For seamless inteoperation, the extension alias is forced to "sreg" for
 * OpenID 2 messages / SREG1.1</li>
 * </ul>
 *
 * @see Message MessageExtension
 * @author Marius Scurtescu, Johnny Bufu
 */
public class SRegMessage implements MessageExtension, MessageExtensionFactory
{
    private static Log _log = LogFactory.getLog(SRegMessage.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * The Simple Registration 1.0 namespace URI.
     */
    public static final String OPENID_NS_SREG = "http://openid.net/sreg/1.0";

    /**
     * The Simple Registration 1.1 namespace URI.
     */
    public static final String OPENID_NS_SREG11 = "http://openid.net/extensions/sreg/1.1";

    /**
     * The Simple Registration extension-specific parameters.
     * <p>
     * The openid.<extension_alias> prefix is not part of the parameter names
     */
    protected ParameterList _parameters;


    private String _typeUri = OPENID_NS_SREG;

    /**
     * Constructs an empty (no parameters) Simple Registration extension.
     */
    public SRegMessage()
    {
        _parameters = new ParameterList();

        if (DEBUG) _log.debug("Created empty SRegMessage.");
    }

    /**
     * Constructs an Simple Registration extension with a specified list of
     * parameters.
     * <p>
     * The parameter names in the list should not contain the
     * openid.<extension_alias>.
     */
    public SRegMessage(ParameterList params)
    {
        _parameters = params;

        if (DEBUG)
            _log.debug("Created SRegMessage from parameter list:\n" + params);
    }

    /**
     * Gets the Type URI that identifies the Simple Registration extension.
     */
    public String getTypeUri()
    {
        return _typeUri;
    }


    /**
     * Sets the SREG type URI. Hack to support both SREG 1.0 and 1.1,
     * until 1.1 spec gets fixed.
     */
    public void setTypeUri(String typeUri)
    {
        _typeUri = typeUri;
    }

    /**
     * Gets ParameterList containing the Simple Registration extension-specific
     * parameters.
     * <p>
     * The openid.<extension_alias> prefix is not part of the parameter names,
     * as it is handled internally by the Message class.
     * <p>
     * The openid.ns.<extension_type_uri> parameter is also handled by
     * the Message class.
     *
     * @see Message
     */
    public ParameterList getParameters()
    {
        return _parameters;
    }

    /**
     * Gets a the value of the parameter with the specified name.
     *
     * @param name      The name of the parameter,
     *                  without the openid.<extension_alias> prefix.
     * @return          The parameter value, or null if not found.
     */
    public String getParameterValue(String name)
    {
        return _parameters.getParameterValue(name);
    }

    /**
     * Sets the extension's parameters to the supplied list.
     * <p>
     * The parameter names in the list should not contain the
     * openid.<extension_alias> prefix.
     */
    public void setParameters(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Encodes a string value according to the conventions for supporting
     * multiple values for a parameter (commas and backslashes are escaped).
     *
     * @param       value   String value to be encoded.
     * @return              The encoded value.
     */
    public String multivalEncode(String value)
    {
        return value.replaceAll("\\\\", "\\\\\\\\").replaceAll(",","\\\\,");
    }

    /**
    * Decodes a string value according to the conventions for supporting
    * multiple values for a parameter (commas and backslashes are escaped).
    *
    * @param       value   String value to be decoded.
    * @return              The dencoded value.
    */
    public String multivalDecode(String value)
    {
        return value.replaceAll("\\\\,", ",").replaceAll("\\\\\\\\","\\\\");
    }

    /**
     * Simple Registration doesn't implement authentication services.
     *
     * @return false
     */
    public boolean providesIdentifier()
    {
        return false;
    }

    /**
     * Simple registration parameters are REQUIRED to be signed.
     *
     * @return true
     */
    public boolean signRequired()
    {
        return true;
    }


    /**
     * Instantiates the apropriate Simple Registration object
     * (request / response) for the supplied parameter list.
     *
     * @param parameterList         The Simple Registration specific parameters
     *                              (without the openid.<ext_alias> prefix)
     *                              extracted from the openid message.
     * @param isRequest             Indicates whether the parameters were
     *                              extracted from an OpenID request (true),
     *                              or from an OpenID response.
     * @return                      MessageExtension implementation for
     *                              the supplied extension parameters.
     * @throws MessageException     If a Simple Registration object could not be
     *                              instantiated from the supplied parameter list.
     */
    public MessageExtension getExtension(
            ParameterList parameterList, boolean isRequest)
            throws MessageException
    {
        if ( parameterList.hasParameter("required") ||
             parameterList.hasParameter("optional"))

            return SRegRequest.createSRegRequest(parameterList);

        else
            return SRegResponse.createSRegResponse(parameterList);

    }
}
