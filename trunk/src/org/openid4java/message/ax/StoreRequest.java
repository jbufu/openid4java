/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.ax;

import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the extension for Attribute Exchange store requests.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class StoreRequest extends AxMessage
{
    private static Log _log = LogFactory.getLog(StoreRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    protected StoreRequest()
    {
        _parameters.set(new Parameter("mode", "store_request"));

        if (DEBUG) _log.debug("Created empty store request.");
    }

    /**
     * Constructs a Store Request with an empty parameter list.
     */
    public static StoreRequest createStoreRequest()
    {
        return new StoreRequest();
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected StoreRequest(ParameterList params)
    {
        _parameters = params;
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static StoreRequest createStoreRequest(ParameterList params)
            throws MessageException
    {
        StoreRequest req = new StoreRequest(params);

        if (! req.isValid())
            throw new MessageException("Invalid parameters for a store request");

        if (DEBUG)
            _log.debug("Created store request from parameter list:\n" + params);

        return req;
    }

    /**
     * Adds an attribute to the store request.
     *
     * @param       alias       The identifier that will be associated with the
     *                          attribute name URI
     * @param       typeUri     The attribute name URI
     * @param       value       The value of the attribute
     */
    public void addAttribute(String alias, String typeUri, String value)
        throws MessageException
    {
        if ( alias.indexOf(',') > -1 || alias.indexOf('.') > -1 ||
             alias.indexOf(':') > -1 || alias.indexOf('\n') > -1 )
            throw new MessageException(
                "Characters [.,:\\n] are not allowed in attribute aliases: " + alias);

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
            _log.debug("Added new attribute to store request; type: " + typeUri
                       + " alias: " + alias + " count: " + count);
    }

    /**
     * Returns a list with the attribute value(s) associated with the specified
     * alias.
     *
     * @param   alias       Attribute alias.
     * @return              List of attribute values.
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
     * Gets the (first) for the specified attribute alias.
     */
    public String getAttributeValue(String alias)
    {
        return getCount(alias) > 1 ?
            getParameterValue("value." + alias + ".1") :
            getParameterValue("value." + alias);
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

            if (paramName.startsWith("type."))
            {
                String alias = paramName.substring(5);

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

            if (paramName.startsWith("type."))
            {
                String alias = paramName.substring(5);

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
     * specified attribute alias. The value must be greater than 1.
     *
     * @param alias     The attribute alias.
     * @param count     The number of values.
     */
    private void setCount(String alias, int count)
    {
        if (count > 1)
            _parameters.set(new Parameter("count." + alias, Integer.toString(count)));
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
        if ( ! _parameters.hasParameter("mode") ||
                ! "store_request".equals(_parameters.getParameterValue("mode")))
        {
            _log.warn("Invalid mode value in store_request: "
                      + _parameters.getParameterValue("mode"));
            return false;
        }

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (! paramName.equals("mode") &&
                    ! paramName.startsWith("type.") &&
                    ! paramName.startsWith("value.") &&
                    ! paramName.startsWith("count.") )
            {
                _log.warn("Invalid parameter name in store request: " + paramName);
                //return false;
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

                if (count <= 0)
                {
                    _log.warn("Invalid value for count." + alias + ": " + count);
                    return false;
                }

                for (int i = 1; i <= count; i++)
                {
                    if (! _parameters.hasParameter("value." + alias + "." +
                            Integer.toString(i)))
                    {
                        _log.warn("Value missing for alias: "
                                  + alias + "." + Integer.toString(i));
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
