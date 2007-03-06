/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.ParameterList;
import net.openid.message.MessageException;
import net.openid.message.Parameter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Implements the extension for Attribute Exchange fetch responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class FetchResponse extends AxMessage
{
    private static Logger _log = Logger.getLogger(FetchResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Constructs a Fetch Response with an empty parameter list.
     */
    protected FetchResponse()
    {
        _parameters.set(new Parameter("mode", "fetch_response"));

        if (DEBUG) _log.debug("Created empty fetch response.");
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
     * the "openid.<extension_alias>." prefix.
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

        if (DEBUG)
            _log.debug("Created fetch response from parameter list:\n" + params);

        return resp;
    }

    /**
     * Adds an attribute to the fetch response.
     *
     * @param       alias       The alias identifier that will be associated
     *                          with the attribute type URI.
     * @param       typeUri     The attribute type URI.
     * @param       value       The value of the attribute.
     */
    public void addAttribute(String alias, String typeUri, String value)
    {
        int count = getCount(alias);

        String index = "";

        switch(count)
        {
            case 0:
                _parameters.set(new Parameter("type." + alias, typeUri));
                break;

            case 1:
                // rename the existing one
                _parameters.set(new Parameter("value." + alias + ".1",
                        getParameterValue("value." + alias)));
                _parameters.removeParameters("value." + alias);
                index = ".2";
                break;

            default:
                index = "." +Integer.toString(count + 1);
        }

        _parameters.set(new Parameter("value." + alias + index, value));
        setCount(alias, ++count);

        if (DEBUG)
            _log.debug("Added new attribute to fetch response; type: " + typeUri
                       + " alias: " + alias + " count: " + count);
    }

    /**
     * Returns a list with the attribute value(s) associated for the specified
     * attribute alias.
     *
     * @param alias     The attribute alias.
     * @return          List of attribute values.
     */
    public List getAttributeValues(String alias)
    {
        List values = new ArrayList();

        if (! _parameters.hasParameter("count." + alias))
            values.add(getParameterValue("value." + alias));
        else
            for (int i = 1; i <= getCount(alias); i++)
                values.add(getParameterValue("value." + alias + "." + Integer.toString(i)));

        return values;
    }

    /**
     * Gets a list of attribute aliases.
     */
    public List getAttributeAliases()
    {
        List aliases = new ArrayList();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (paramName.startsWith("value."))
            {
                String alias;
                if (paramName.endsWith("."))
                    alias = paramName.substring(6, paramName.length() - 1);
                else
                    alias = paramName.substring(6);

                if ( ! aliases.contains(alias) )
                    aliases.add(alias);
            }
        }

        return aliases;
    }

    /**
     * Gets a map with attribute aliases -> list of values.
     */
    public Map getAttributes()
    {
        Map attributes = new HashMap();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (paramName.startsWith("value."))
            {
                String alias;
                if (paramName.endsWith("."))
                    alias = paramName.substring(6, paramName.length() - 1);
                else
                    alias = paramName.substring(6);

                if ( ! attributes.containsKey(alias) )
                    attributes.put(alias, getAttributeValues(alias));
            }
        }

        return attributes;
    }

    /**
     * Gets the number of values provided in the fetch response for the
     * specified attribute alias.
     *
     * @param alias     The attribute alias.
     */
    public int getCount(String alias)
    {
        if (_parameters.hasParameter("count." + alias))
            return Integer.parseInt(_parameters.getParameterValue("count." + alias));

        else if (_parameters.hasParameter("value." + alias))
            return 1;

        else
            return 0;
    }

    /**
     * Sets the number of values provided in the fetch response for the
     * specified attribute alias.
     *
     * @param alias     The attribute alias.
     * @param count     The number of values.
     */
    private void setCount(String alias, int count)
    {
        // make sure that count.< alias >.1 is removed
        _parameters.removeParameters("count." + alias);

        if (count > 1)
            _parameters.set(new Parameter("count." + alias, Integer.toString(count)));
    }

    /**
     * Sets the optional 'update_url' parameter where the OP can later re-post
     * fetch-response updates for the values of the requested attributes.
     *
     * @param       updateUrl   The URL where the RP accepts later updates
     *                          for the requested attributes.
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

        if (DEBUG) _log.debug("Setting fetch response update_url: " + updateUrl);

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

            if (! paramName.equals("mode") &&
                    ! paramName.startsWith("type.") &&
                    ! paramName.startsWith("count.") &&
                    ! paramName.startsWith("value.") &&
                    ! paramName.equals("update_url"))
            {
                _log.warn("Invalid parameter name in fetch response: " + paramName);
                return false;
            }
        }

        return checkAttributes();
    }

    private boolean checkAttributes()
    {
        List aliases = getAttributeAliases();

        Iterator it = aliases.iterator();
        while (it.hasNext())
        {
            String alias = (String) it.next();

            if (! _parameters.hasParameter("type." + alias))
            {
                _log.warn("Type missing for attribute alias: " + alias);
                return false;
            }

            if ( ! _parameters.hasParameter("count." + alias) )
            {
                if ( ! _parameters.hasParameter("value." + alias) )
                {
                    _log.warn("Value missing for attribute alias: " + alias);
                    return false;
                }
            }
            else // count.alias present
            {
                if (_parameters.hasParameter("value." + alias))
                {
                    _log.warn("Count parameter present for alias: " + alias
                              + "; should use " + alias + ".[index] format");
                    return false;
                }

                int count = getCount(alias);

                for (int i = 1; i <= count; i++)
                    if (! _parameters.hasParameter("value." + alias + "." +
                            Integer.toString(i)))
                    {
                        _log.warn("Value missing for alias: "
                                  + alias + "." + Integer.toString(i));
                        return false;
                    }
            }
        }

        return true;
    }
}
