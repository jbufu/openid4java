/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.message.pape;

import org.openid4java.message.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for the OpenID Provider Authentication Policy extension
 * implementation.
 * <p>
 * Encapsulates:
 * <ul>
 * <li> the Type URI that identifies the OpenID Provider Authentication Policy
 * extension
 * <li> a list of extension-specific parameters, with the
 * openid.<extension_alias> prefix removed
 * </ul>
 *
 * @see Message MessageExtension
 * @author Marius Scurtescu, Johnny Bufu
 */
public class PapeMessage implements MessageExtension, MessageExtensionFactory
{
    private static Log _log = LogFactory.getLog(PapeMessage.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String PAPE_POLICY_PHISHING_RESISTANT =
        "http://schemas.openid.net/pape/policies/2007/06/phishing-resistant";
    public static final String PAPE_POLICY_MULTI_FACTOR =
        "http://schemas.openid.net/pape/policies/2007/06/multi-factor";
    public static final String PAPE_POLICY_MULTI_FACTOR_PHYSICAL =
        "http://schemas.openid.net/pape/policies/2007/06/multi-factor-physical";

    /**
     * The OpenID Provider Authentication Policy extension URI.
     */
    public static final String OPENID_NS_PAPE = "http://specs.openid.net/extensions/pape/1.0";

    /**
     * The OpenID Provider Authentication Policy extension-specific parameters.
     * <p>
     * The openid.<extension_alias> prefix is not part of the parameter names
     */
    protected ParameterList _parameters;

    /**
     * Constructs an empty (no parameters) OpenID Provider Authentication
     * Policy extension.
     */
    public PapeMessage()
    {
        _parameters = new ParameterList();

        if (DEBUG) _log.debug("Created empty PapeMessage.");
    }

    /**
     * Constructs an OpenID Provider Authentication Policy extension
     * with a specified list of parameters.
     * <p>
     * The parameter names in the list should not contain the
     * openid.<extension_alias>.
     */
    public PapeMessage(ParameterList params)
    {
        _parameters = params;

        if (DEBUG)
            _log.debug("Created PapeMessage from parameter list:\n" + params);
    }

    /**
     * Gets the Type URI that identifies the OpenID Provider Authentication
     * Policy extension.
     */
    public String getTypeUri()
    {
        return OPENID_NS_PAPE;
    }

    /**
     * Gets ParameterList containing the OpenID Provider Authentication
     * Policy extension-specific parameters.
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
     * Checks if the extension contains a parameter.
     *
     * @param name      The name of the parameter,
     *                  without the openid.<extension_alias> prefix.
     * @return          True if a parameter with the specified name exists,
     *                  false otherwise.
     */
    public boolean hasParameter(String name)
    {
        return _parameters.hasParameter(name);
    }

    /**
     * Sets the value for the parameter with the specified name.
     *
     * @param name      The name of the parameter,
     *                  without the openid.<extension_alias> prefix.
     */
    protected void set(String name, String value)
    {
        _parameters.set(new Parameter(name, value));
    }

    /**
     * Gets a the value of the parameter with the specified name.
     *
     * @param name      The name of the parameter,
     *                  without the openid.<extension_alias> prefix.
     * @return          The parameter value, or null if not found.
     */
    protected Parameter getParameter(String name)
    {
        return _parameters.getParameter(name);
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
     * OpenID Provider Authentication Policy extension doesn't implement
     * authentication services.
     *
     * @return false
     */
    public boolean providesIdentifier()
    {
        return false;
    }

    /**
     * Instantiates the apropriate OpenID Provider Authentication Policy
     * extension object (request / response) for the supplied parameter 
     * list.
     *
     * @param parameterList         The OpenID Provider Authentication Policy
     *                              extension specific parameters
     *                              (without the openid.<ext_alias> prefix)
     *                              extracted from the openid message.
     * @param isRequest             Indicates whether the parameters were
     *                              extracted from an OpenID request (true),
     *                              or from an OpenID response.
     * @return                      MessageExtension implementation for
     *                              the supplied extension parameters.
     * @throws MessageException     If a OpenID Provider Authentication Policy
     *                              extension object could not be
     *                              instantiated from the supplied parameter list.
     */
    public MessageExtension getExtension(
            ParameterList parameterList, boolean isRequest)
            throws MessageException
    {
        if ( parameterList.hasParameter("preferred_auth_policies") ||
             parameterList.hasParameter("max_auth_age"))

            return PapeRequest.createPapeRequest(parameterList);

        else if ( parameterList.hasParameter("auth_policies") ||
             parameterList.hasParameter("auth_age"))

            return PapeResponse.createPapeResponse(parameterList);

        else
            throw new MessageException("Invalid parameters for a PAPE message.");

    }
}
