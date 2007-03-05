/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.MessageException;
import net.openid.message.Parameter;
import net.openid.message.ParameterList;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

/**
 * Implements the extension for Attribute Exchange fetch requests.
 *
 * @see AxMessage Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public class FetchRequest extends AxMessage
{
    private static Logger _log = Logger.getLogger(FetchRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Constructs a Fetch Request with an empty parameter list.
     */
    protected FetchRequest()
    {
        _parameters.set(new Parameter("mode", "fetch_request"));

        if (DEBUG) _log.debug("Created empty fetch request.");
    }

    /**
     * Constructs a Fetch Request with an empty parameter list.
     */
    public static FetchRequest createFetchRequest()
    {
        return new FetchRequest();
    }

    /**
     * Constructs a FetchRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected FetchRequest(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Constructs a FetchRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static FetchRequest createFetchRequest(ParameterList params)
            throws MessageException
    {
        FetchRequest req = new FetchRequest(params);

        if (! req.isValid())
            throw new MessageException("Invalid parameters for a fetch request");

        if (DEBUG)
            _log.debug("Created fetch request from parameter list: " + params);

        return req;
    }

    /**
     * Adds an attribute to the fetch request.
     *
     * @param       alias       The attribute alias that will be associated
     *                          with the attribute type URI
     * @param       typeUri     The attribute type URI
     * @param       required    If true, marks the attribute as 'required';
     *                          'if_available' otherwise.
     * @param       count       The number of attribute values requested.
     */
    public void addAttribute(String alias, String typeUri,
                             boolean required, int count)
    {
        _parameters.set(new Parameter("type." + alias, typeUri));

        String level = required ? "required" : "if_available";

        //todo: rename param -> levelParam
        Parameter param = _parameters.getParameter(level);
        Parameter newParam;

        if (param == null)
        {
            newParam = new Parameter(level, multivalEncode(alias));
        }
        else
        {
            newParam = new Parameter(level,
                    param.getValue() + "," + multivalEncode(alias));
            _parameters.removeParameters(level);
        }

        _parameters.set(newParam);

        if (count > 1)
            _parameters.set(
                    new Parameter("count." + alias, Integer.toString(count)));

        if (DEBUG) _log.debug("Added new attribute to fetch request; type: "
                              + typeUri + " alias: " + alias + " count: "
                              + count + " required: " + required);
    }

    /**
     * Adds an attribute to the fetch request, with a default value-count of 1.
     *
     * @see #addAttribute(String, String, boolean, int)
     */
    public void addAttribute(String alias, String typeUri, boolean required)
    {
        addAttribute(alias, typeUri, required, 1);
    }

    /**
     * Sets the desired number of attribute vaules requested for the specified
     * attribute alias.
     *
     * @param alias     The attribute alias.
     */
    public void setCount(String alias, int count)
    {
        if (count > 1)
            _parameters.set(
                    new Parameter("count." + alias, Integer.toString(count)));
    }

    /**
     * Returns the number of values requested for the specified attribute alias,
     * or 1 (the default number) if the count.< alias > parameter is
     * absent.
     *
     * @param alias     The attribute alias.
     */
    public int getCount(String alias)
    {
        if (_parameters.hasParameter("count." + alias))
            return Integer.parseInt(_parameters.getParameterValue("count." + alias));
        else
            return 1;
    }

    /**
     * Sets the optional 'update_url' parameter where the OP can later re-post
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

        if (DEBUG) _log.debug("Setting fetch request update_url: " + updateUrl);

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
     * Returns a map with the requested attributes.
     *
     * @param       required    If set to true the list of 'required' attributes
     *                          is returned, otherwise the list of 'if_available'
     *                          attributes.
     * @return      Map of attribute aliases -> attribute type URIs.
     */
    public Map getAttributes(boolean required)
    {
        HashMap reqAttrs = new LinkedHashMap();

        String level = required ? "required" : "if_available";

        Parameter param = _parameters.getParameter(level);
        if (param != null)
        {
            String[] values = param.getValue().split(",");
            for (int i = 0; i < values.length; i++)
            {
                String alias = multivalDecode(values[i]);
                reqAttrs.put(alias,
                        _parameters.getParameterValue("type." + alias));
            }
        }

        return reqAttrs;
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
                ! _parameters.hasParameter("if_available") )
        {
            _log.warn("One of 'required' or 'if_available' parameters must be present.");
            return false;
        }

        if ( ! _parameters.hasParameter("mode") ||
                ! "fetch_request".equals(_parameters.getParameterValue("mode")))
        {
            _log.warn("Invalid mode value in fetch_request: "
                      + _parameters.getParameterValue("mode"));
            return false;
        }

        if (_parameters.hasParameter("required"))
        {
            String[] values = _parameters.getParameterValue("required").split(",");
            for (int i = 0; i < values.length; i++)
            {
                String value = multivalDecode(values[i]);
                if ( ! _parameters.hasParameter("type." + value) )
                {
                    _log.warn("Type missing for attribute alias: " + value);
                    return false;
                }
            }
        }

        if ( _parameters.hasParameter("if_available"))
        {
            String[] values = _parameters.getParameterValue("if_available").split(",");
            for (int i = 0; i < values.length; i++)
            {
                String value = multivalDecode(values[i]);
                if ( ! _parameters.hasParameter("type." + value) )
                {
                    _log.warn("Type missing for attribute alias: " + value);
                    return false;
                }
            }
        }

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();
            if (! paramName.equals("mode") &&
                    ! paramName.startsWith("type.") &&
                    ! paramName.equals("required") &&
                    ! paramName.equals("if_available") &&
                    ! paramName.equals("update_url"))
            {
                _log.warn("Invalid parameter name in fetch request: " + paramName);
                return false;
            }
        }

        return true;
    }
}
