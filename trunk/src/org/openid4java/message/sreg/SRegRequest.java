/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.sreg;

import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the extension for Simple Registration fetch requests.
 *
 * @see SRegMessage Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public class SRegRequest extends SRegMessage
{
    private static Log _log = LogFactory.getLog(SRegRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Constructs a SReg Request with an empty parameter list.
     */
    protected SRegRequest()
    {
        if (DEBUG) _log.debug("Created empty SReg request.");
    }

    /**
     * Constructs a SReg Request with an empty parameter list.
     */
    public static SRegRequest createFetchRequest()
    {
        return new SRegRequest();
    }

    /**
     * Constructs a SRegRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected SRegRequest(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Constructs a SRegRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static SRegRequest createSRegRequest(ParameterList params)
            throws MessageException
    {
        SRegRequest req = new SRegRequest(params);

        if (! req.isValid())
            throw new MessageException("Invalid parameters for a SReg request");

        if (DEBUG)
            _log.debug("Created SReg request from parameter list:\n" + params);

        return req;
    }

    /**
     * Adds an attribute to the SReg request.
     *
     * @param       attr        A requested attribute name.
     * @param       required    If true, marks the attribute as 'required';
     *                          'if_available' otherwise.
     */
    public void addAttribute(String attr, boolean required)
    {
        String level = required ? "required" : "optional";

        Parameter levelParam = _parameters.getParameter(level);
        Parameter newParam;

        if (levelParam == null)
        {
            newParam = new Parameter(level, multivalEncode(attr));
        }
        else
        {
            newParam = new Parameter(level,
                    levelParam.getValue() + "," + multivalEncode(attr));
            _parameters.removeParameters(level);
        }

        _parameters.set(newParam);

        if (DEBUG) _log.debug("Added new attribute to SReg request: " + attr +
                              " required: " + required);
    }

    /**
     * Returns a map with the requested attributes.
     *
     * @param       required    If set to true the list of 'required' attributes
     *                          is returned, otherwise the list of 'optional'
     *                          attributes.
     * @return      List of attribute names.
     */
    public List getAttributes(boolean required)
    {
        List attributes = new ArrayList();

        String level = required ? "required" : "optional";

        Parameter param = _parameters.getParameter(level);
        if (param != null)
        {
            String[] values = param.getValue().split(",");
            for (int i = 0; i < values.length; i++)
            {
                String attr = multivalDecode(values[i]);
                attributes.add(attr);
            }
        }

        return attributes;
    }

    /**
     * Gets all requested attributes (required and optional).
     *
     * @return      List of attribute names.
     */
    public List getAttributes()
    {
        List attributes = getAttributes(true);
        attributes.addAll(getAttributes(false));

        return attributes;
    }

    /**
     * Sets the optional policy URL.
     *
     * @param   policyUrl   A URL which the Consumer provides to give the
     *                      End User a place to read about the how the profile
     *                      data will be used. The Identity Provider SHOULD
     *                      display this URL to the End User if it is given.
     */
    public void setPolicyUrl(String policyUrl) throws MessageException {
        try
        {
            new URL(policyUrl);
        } catch (MalformedURLException e)
        {
            throw new MessageException("Invalid policy_url: " + policyUrl);
        }

        if (DEBUG) _log.debug("Setting SReg request policy_url: " + policyUrl);

        _parameters.set(new Parameter("policy_url", policyUrl));
    }

    /**
     * Gets the optional policy URL parameter if available, or null otherwise.
     */
    public String getUpdateUrl()
    {
        return _parameters.hasParameter("policy_url") ?
                _parameters.getParameterValue("policy_url") : null;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return      True if the extension is valid, false otherwise.
     */
    public boolean isValid()
    {
        if ( ! _parameters.hasParameter("required") &&
                ! _parameters.hasParameter("optional") )
        {
            _log.warn("One of 'required' or 'optional' parameters must be present.");
            return false;
        }

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();
            if (! paramName.equals("required") &&
                    ! paramName.equals("optional") &&
                    ! paramName.equals("policy_url"))
            {
                _log.warn("Invalid parameter name in SReg request: " + paramName);
//                return false;
            }
        }

        return true;
    }
}
