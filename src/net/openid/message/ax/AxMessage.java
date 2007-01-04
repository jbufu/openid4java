/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.*;

import java.util.Iterator;

/**
 * Base class for the Attribute Exchange implementation.
 * <p>
 * Encapsulates:
 * <ul>
 * <li> the Type URI that identifies the Attribute Exchange extension
 * <li> a list of extension-specific parameters, with the
 * openid.<extension_alias> prefix removed
 * <li> methods for handling the extension-specific support of parameters with
 * multpile values
 * </ul>
 *
 * @see Message MessageExtension
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AxMessage implements MessageExtension, MessageExtensionFactory
{
    /**
     * The Attribute Exchange Type URI
     */
    public static final String OPENID_NS_AX = "http://openid.net/srv/ax/1.0";

    /**
     * The Attribute Exchange extension-specific parameters.
     * <p>
     * The openid.<extension_alias> prefix is not part of the parameter names
     */
    protected ParameterList _parameters;

    /**
     * Constructs an empty (no parameters) Attribute Exchange extension.
     */
    public AxMessage()
    {
        _parameters = new ParameterList();
    }

    /**
     * Constructs an Attribute Exchange extension with a specified list of
     * parameters.
     * <p>
     * The parameter names in the list should not contain the
     * openid.<extension_alias>.
     */
    public AxMessage(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Gets the Type URI that identifies the Attribute Exchange extension,
     * or null if it is not a valid URI.
     */
    public String getTypeUri()
    {
        return OPENID_NS_AX;
    }

    /**
     * Gets ParameterList containing the Attribute Exchange extension-specific
     * parameters.
     * <p>
     * The openid.<extension_alias> prefix is not part of the parameter names,
     * as it is handled internally by the Message class.
     * <p>
     * The openid.ns. parameter is also handled by the Message class.
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
     *                  without the openid.<alias> prefix.
     * @return          The parameter value, or null if not found.
     */
    public String getParameterValue(String name)
    {
        return _parameters.getParameterValue(name);
    }

    /**
     * Constructs an Attribute Exchange extension with a specified list of
     * parameters.
     * <p>
     * The parameter names in the list should not contain the
     * openid.<extension_alias>.
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
     * Attribute exchange doesn't implement authentication services.
     *
     * @return false
     */
    public boolean providesIdentifier()
    {
        return false;
    }

    public MessageExtension createRequest(ParameterList parameterList) throws MessageException
    {
        if (parameterList.hasParameter("if_available") || parameterList.hasParameter("update_url"))
        {
            return FetchRequest.createFetchRequest(parameterList);
        }
        else
        {
            return StoreRequest.createStoreRequest(parameterList);
        }
    }

    public MessageExtension createResponse(ParameterList parameterList) throws MessageException
    {
        Iterator parameters = parameterList.getParameters().iterator();
        while (parameters.hasNext())
        {
            Parameter parameter = (Parameter) parameters.next();

            if (parameter.getKey().startsWith("type."))
            {
                return FetchResponse.createFetchResponse(parameterList);
            }
        }

        return StoreResponse.createStoreResponse(parameterList);
    }
}
