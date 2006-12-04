/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.ParameterList;
import net.openid.message.MessageException;
import net.openid.message.Parameter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * Implements the extension for Attribute Exchange fetch responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class FetchResponse extends AxMessage
{
    /**
     * Constructs a Fetch Response with an empty parameter list.
     */
    public FetchResponse()
    {
    }

    /**
     * Constructs a FetchResponse from a parameter list.
     * <p>
     * The parameter list should be extracted from a received message with the
     * getExtensionParams method of the Message class.
     */
    public FetchResponse(ParameterList params) throws MessageException
    {
        _parameters = params;

        if (! isValid())
            throw new MessageException("Invalid parameters for a fetch response");
    }

    /**
     * Adds an attribute to the fetch response.
     *
     * @param       id          The identifier that will be associated with the
     *                          attribute name URI
     * @param       attrName    The attribute name URI
     * @param       value       The value of the attribute
     */
    public void addAttribute(String id, String attrName, String value)
    {
        _parameters.set(new Parameter("type." + id, attrName));

        // todo: check for possible multiple values for this attribute / param
        _parameters.set(new Parameter("value." + id, value));
    }

    /**
     * Sets the optional 'update_url' parameter where the IdP will later re-post
     * fetch-response updates to the values of the requested attributes.
     *
     * @param       updateUrl   The URL where the RP accepts later updates
     *                          to the requested attributes.
     */
    public void setUpdateUrl(String updateUrl) throws MessageException
    {
        try
        {
            new URL(updateUrl);
        } catch (MalformedURLException e)
        {
            throw new MessageException("Invalid update_url: " + updateUrl);
        }

        _parameters.set(new Parameter("update_url", updateUrl));
    }

    /**
     * Gets the optional 'update_url' parameter if available, or null otherwise.
     */
    public String getUpdateUrl()
    {
        return _parameters.hasParameter("update_url") ?
                _parameters.getParameterValue("update_url") : null;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @return      True if the extension is valid, false otherwise.
     */
    private boolean isValid()
    {
        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (! paramName.startsWith("type.") &&
                    ! paramName.startsWith("value.") &&
                    ! paramName.equals("update_url"))
                return false;

            if ( paramName.startsWith("value.") &&
                    !_parameters.hasParameter("type." + paramName.substring(6)))
                return false;
        }

        return true;
    }


}
