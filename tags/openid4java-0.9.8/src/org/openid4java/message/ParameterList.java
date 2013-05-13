/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;
import java.net.URLDecoder;

/**
 * A list of parameters that are part of an OpenID message. Please note that you can have multiple parameters with
 * the same name.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ParameterList implements Serializable
{
    private static Log _log = LogFactory.getLog(ParameterList.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    Map _parameterMap;

    public ParameterList()
    {
        _parameterMap  = new LinkedHashMap();

        if (DEBUG) _log.debug("Created empty parameter list.");
    }

    public ParameterList(ParameterList that)
    {
        if (DEBUG) _log.debug("Cloning parameter list:\n" + that);

        this._parameterMap = new LinkedHashMap(that._parameterMap);
    }

    /**
     * Constructs a ParameterList from a Map of parameters, ideally obtained
     * with ServletRequest.getParameterMap(). The parameter keys and values
     * must be in URL-decoded format.
     *
     * @param parameterMap  Map<String,String[]> or Map<String,String>
     */
    public ParameterList(Map parameterMap)
    {
        _parameterMap  = new LinkedHashMap();

        Iterator keysIter = parameterMap.keySet().iterator();
        while (keysIter.hasNext())
        {
            String name = (String) keysIter.next();
            Object v = parameterMap.get(name);

            String value;
            if (v instanceof String[])
            {
                String[] values = (String[]) v;
                if (values.length > 1 && name.startsWith("openid."))
                    throw new IllegalArgumentException(
                            "Multiple parameters with the same name: " + Arrays.toString(values));

                value = values.length > 0 ? values[0] : null;
            }
            else if (v instanceof String)
            {
                value = (String) v;
            }
            else
            {
                value="";
                _log.error("Can extract parameter value; unexpected type: " +
                    v.getClass().getName());
            }

            set(new Parameter(name, value));
        }

        if (DEBUG) _log.debug("Creating parameter list:\n" + this);
    }

    public void copyOf(ParameterList that)
    {
        if (DEBUG) _log.debug("Copying parameter list:\n" + that);

        this._parameterMap = new LinkedHashMap(that._parameterMap);
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        final ParameterList that = (ParameterList) obj;

        return _parameterMap.equals(that._parameterMap);
    }

    public int hashCode()
    {
        return _parameterMap.hashCode();
    }

    public void set(Parameter parameter)
    {
        _parameterMap.put(parameter.getKey(), parameter);
    }

    public void addParams(ParameterList params)
    {
        Iterator iter = params.getParameters().iterator();

        while (iter.hasNext())
            set((Parameter) iter.next());
    }

    public Parameter getParameter(String name)
    {
        return (Parameter) _parameterMap.get(name);
    }

    public String getParameterValue(String name)
    {
        Parameter param = getParameter(name);

        return param != null ? param.getValue() : null;
    }

    public List getParameters()
    {
        return new ArrayList(_parameterMap.values());
    }

    public void removeParameters(String name)
    {
        _parameterMap.remove(name);
    }

    public boolean hasParameter(String name)
    {
        return _parameterMap.containsKey(name);
    }

    public boolean hasParameterPrefix(String prefix) {
        Iterator keysIter = _parameterMap.keySet().iterator();
        while (keysIter.hasNext())
        {
            if (((String)keysIter.next()).startsWith(prefix))
                return true;
        }
        return false;
    }

    /**
     * Create a parameter list based on a URL encoded HTTP query string.
     */
    public static ParameterList createFromQueryString(String queryString) throws MessageException
    {
        if (DEBUG) _log.debug("Creating parameter list from query string: " + queryString);

        ParameterList parameterList = new ParameterList();

        StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
        while (tokenizer.hasMoreTokens())
        {
            String keyValue = tokenizer.nextToken();
            int posEqual = keyValue.indexOf('=');

            if (posEqual == -1)
                throw new MessageException("Invalid query parameter, = missing: " + keyValue);

            try
            {
                String key   = URLDecoder.decode(keyValue.substring(0, posEqual), "UTF-8");
                String value = URLDecoder.decode(keyValue.substring(posEqual + 1), "UTF-8");

                parameterList.set(new Parameter(key, value));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new MessageException("Cannot URL decode query parameter: " + keyValue, e);
            }
        }

        return parameterList;
    }

    public static ParameterList createFromKeyValueForm(String keyValueForm) throws MessageException
    {
        if (DEBUG) _log.debug("Creating parameter list from key-value form:\n" + keyValueForm);

        ParameterList parameterList = new ParameterList();

        StringTokenizer tokenizer = new StringTokenizer(keyValueForm, "\n");
        while (tokenizer.hasMoreTokens())
        {
            String keyValue = tokenizer.nextToken();
            int posColon = keyValue.indexOf(':');

            if (posColon == -1)
                throw new MessageException("Invalid Key-Value form, colon missing: " + keyValue);

            String key   = keyValue.substring(0, posColon);
            String value = keyValue.substring(posColon + 1);

            parameterList.set(new Parameter(key, value));
        }

        return parameterList;
    }

    /**
     * @return The key-value form encoding of for this ParameterList.
     */
    public String toString()
    {
        StringBuffer allParams = new StringBuffer("");

        List parameters = getParameters();
        Iterator iterator = parameters.iterator();
        while (iterator.hasNext())
        {
            Parameter parameter = (Parameter) iterator.next();
            allParams.append(parameter.getKey());
            allParams.append(':');
            allParams.append(parameter.getValue());
            allParams.append('\n');
        }

        return allParams.toString();
    }
}
