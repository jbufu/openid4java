/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.message.ax;

import org.openid4java.message.ParameterList;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the extension for Attribute Exchange fetch responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class FetchResponse extends AxMessage
{
    private static Log _log = LogFactory.getLog(FetchResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private int _attrAliasGen = 0;

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
     * Creates a FetchResponse from a FetchRequest message and the data released
     * by the user.
     *
     * @param req               FetchRequest message.
     * @param userData          The userData may be a Map<String alias, String value>
     *                          or a Map<String alias, List<String> values>. The attribute values
     *                          are provided by the calling application. If a list of values is
     *                          specified per attribute, at most n will be sent, where n is the
     *                          number of attribute values requested in the FetchRequest.
     * @return                  Properly formed FetchResponse.
     */
    public static FetchResponse createFetchResponse(FetchRequest req, Map userData)
        throws MessageException
    {
        FetchResponse resp = new FetchResponse();

        // go through each requested attribute
        Map attributes = req.getAttributes();

        for (Iterator i = attributes.keySet().iterator(); i.hasNext(); )
        {
            String alias = (String) i.next();

            // find attribute in userData
            Object value = userData.get(alias);

            // if the value isn't there, skip over it
            if (value == null)
            {
                continue;
            }

            // if the value is a string, add the single attribute to the response
            if (value instanceof String)
            {
                resp.addAttribute(alias, (String) attributes.get(alias), (String)value);
            }

            // if the value is a list (of string) iteratively add each attribute to the response
            else if (value instanceof List)
            {
                Iterator values = ((List)value).iterator();

                // only send up the the maximum requested number
                int max = req.getCount(alias);
                int count;
                for (count = 0; count < max && values.hasNext(); count++)
                {
                    // don't add null values to the response
                    String val = (String)values.next();
                    if (val == null)
                    {
                        count--; // disregard this as a value as we are skipping over it
                        continue;
                    }
                    resp.addAttribute(alias, (String) attributes.get(alias), val);
                }
            }
        }

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
            _log.debug("Added new attribute to fetch response; type: " + typeUri
                       + " alias: " + alias + " count: " + count);
    }

    /**
     * Adds an attribute to the FetchResponse, without the caller having to
     * specify an alias. An alias in the form "attrNN" will be automatically 
     * generated.
     *
     * @param typeUri   The attribute type URI.
     * @param value     The attribute value.
     * @return          The generated attribute alias.
     */
    public String addAttribute(String typeUri, String value)
    {
        _attrAliasGen += 1;

        String alias = "attr" + _attrAliasGen;

        // not calling the other addAttribute - extra overhead in checks there
        _parameters.set(new Parameter("type." + alias, typeUri));
        _parameters.set(new Parameter("value." + alias, value));

        if (DEBUG)
            _log.debug("Added new attribute to fetch response; type: " + typeUri
                       + " alias: " + alias);

        return alias;
    }

    /**
     * Adds the attributes in the supplied Map to the FetchResponse.
     * A requested count of 1 is assumed for each attribute in the map.
     *
     * @param attributes    Map<String typeURI, String value>.
     */
    public void addAttributes(Map attributes)
    {
        String typeUri;
        Iterator iter = attributes.keySet().iterator();
        while (iter.hasNext())
        {
            typeUri = (String) iter.next();
            addAttribute(typeUri, (String) attributes.get(typeUri));
        }
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
     * Gets a map with attribute aliases -> attribute type URI.
     */
    public Map getAttributeTypes()
    {
        Map typeUris = new HashMap();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            Parameter param = (Parameter) it.next();
            String paramName = param.getKey();
            String paramType = param.getValue();

            if (paramName.startsWith("type."))
            {
                String alias = paramName.substring(5);

                if ( ! typeUris.containsKey(alias) )
                    typeUris.put(alias, paramType);
            }
        }

        return typeUris;
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
            _parameters.set(
                new Parameter("count." + alias, Integer.toString(count)));
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
        }
        catch (MalformedURLException e)
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
        if ( ! _parameters.hasParameter("mode") ||
                ! "fetch_response".equals(_parameters.getParameterValue("mode")))
        {
            _log.warn("Invalid mode value in fetch_reponse: "
                      + _parameters.getParameterValue("mode"));
            return false;
        }

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

                if (count < 0)
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
