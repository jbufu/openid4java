/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.ax;

import org.openid4java.message.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log _log = LogFactory.getLog(AxMessage.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * The Attribute Exchange Type URI.
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

        if (DEBUG) _log.debug("Created empty AXMessage.");
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

        if (DEBUG)
            _log.debug("Created AXMessage from parameter list:\n" + params);
    }

    /**
     * Gets the Type URI that identifies the Attribute Exchange extension.
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
     * Attribute exchange doesn't implement authentication services.
     *
     * @return false
     */
    public boolean providesIdentifier()
    {
        return false;
    }

    /**
     * Attribute exchange parameters are required to be signed.
     *
     * @return true
     */
    public boolean signRequired()
    {
        return true;
    }

    /**
     * Instantiates the apropriate Attribute Exchange object (fetch / store -
     * request / response) for the supplied parameter list.
     *
     * @param parameterList         The Attribute Exchange specific parameters
     *                              (without the openid.<ext_alias> prefix)
     *                              extracted from the openid message.
     * @param isRequest             Indicates whether the parameters were
     *                              extracted from an OpenID request (true),
     *                              or from an OpenID response.
     * @return                      MessageExtension implementation for
     *                              the supplied extension parameters.
     * @throws MessageException     If a Attribute Exchange object could not be
     *                              instantiated from the supplied parameter list.
     */
    public MessageExtension getExtension(
            ParameterList parameterList, boolean isRequest)
            throws MessageException
    {
        String axMode = null;
        if (parameterList.hasParameter("mode"))
        {
            axMode = parameterList.getParameterValue("mode");

            if ("fetch_request".equals(axMode))
                return FetchRequest.createFetchRequest(parameterList);

            else if ("fetch_response".equals(axMode))
                return FetchResponse.createFetchResponse(parameterList);

            else if ("store_request".equals(axMode))
                return StoreRequest.createStoreRequest(parameterList);

            else if ("store_response_success".equals(axMode) ||
                    "store_response_failure".equals(axMode))
                return StoreResponse.createStoreResponse(parameterList);
        }

        throw new MessageException("Invalid value for attribute exchange mode: "
                                   + axMode);
    }
}
