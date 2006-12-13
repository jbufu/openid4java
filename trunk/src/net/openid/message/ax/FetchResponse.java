/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.ParameterList;
import net.openid.message.MessageException;
import net.openid.message.Parameter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

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
    protected FetchResponse()
    {
    }

    /**
     * Constructs a Fetch Response with an empty parameter list.
     */
    public static FetchResponse createFetchResponse()
    {
        return new FetchResponse();
    }

    /**
     * Constructs a FetchResponse from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<alias>." prefix.
     */
    protected FetchResponse(ParameterList params)
    {
        _parameters = params;
    }

    public static FetchResponse createFetchResponse(ParameterList params)
            throws MessageException
    {
        FetchResponse resp = new FetchResponse(params);

        if (! resp.isValid())
            throw new MessageException("Invalid parameters for a fetch response");

        return resp;
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
        Parameter existing = _parameters.getParameter(id);
        Parameter newParam;

        if (existing == null)
        {
            _parameters.set(new Parameter("type." + id, attrName));
            newParam = new Parameter("value." + id, value);
        }
        else
        {
            // assume the existing one is of the same typeURI
            newParam = new Parameter("value." + id,
                    existing.getValue() + "," + multivalEncode(value));
            _parameters.removeParameters("value." + id);
        }

        _parameters.set(newParam);
    }

    /**
     * Returns a list with the attribute value(s) associated with the specified
     * alias.
     *
     * @param id        Attribute alias.
     * @return          List of attribute values.
     */
    public List getAttributeValues(String id)
    {
        List decodedAttrs = new ArrayList();

        List attrs = Arrays.asList(getParameterValue("value." + id).split(","));
        Iterator iter = attrs.iterator();
        while (iter.hasNext())
            decodedAttrs.add(multivalDecode((String) iter.next()));

        return decodedAttrs;
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
