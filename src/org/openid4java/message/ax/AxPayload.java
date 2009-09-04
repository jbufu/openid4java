package org.openid4java.message.ax;

import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @author jbufu
 */
public abstract class AxPayload extends AxMessage {

    private static Log _log = LogFactory.getLog(AxPayload.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private int _attrAliasCounter = 0;

    private synchronized String generateAlias()
    {
        return "attr" + Integer.toString(++ _attrAliasCounter);
    }

    /**
     * Adds an attribute to the attribute payload.
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
            _log.debug("Added new attribute to AX payload; type: " + typeUri
                       + " alias: " + alias + " count: " + count);
    }

    /**
     * Adds an attribute to the attribute payload, without the caller having to
     * specify an alias. An alias in the form "attrNN" will be automatically
     * generated.
     *
     * @param typeUri   The attribute type URI.
     * @param value     The attribute value.
     * @return          The generated attribute alias.
     */
    public String addAttribute(String typeUri, String value)
    {
        String alias = generateAlias();

        // not calling the other addAttribute - extra overhead in checks there
        _parameters.set(new Parameter("type." + alias, typeUri));
        _parameters.set(new Parameter("value." + alias, value));

        if (DEBUG)
            _log.debug("Added new attribute to the AX payload; type: " + typeUri
                       + " alias: " + alias);

        return alias;
    }

    /**
     * Adds the attributes in the supplied Map to the attribute payload.
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
     * Get typeURI value for the specified attribute alias.
     */
    public String getAttributeTypeUri(String alias) {
        return _parameters.getParameterValue("type." + alias);
    }

    /**
     * Gets the alias for an attribute type URI, if present.
     *
     * @param typeUri the attribyte type URI for which the alias is looked up
     * @return the attribute alias if present in the message, or null otherwise
     */
    public String getAttributeAlias(String typeUri)
    {
        if (typeUri == null)
            return null;
        Parameter param;
        Iterator it = _parameters.getParameters().iterator();
        while(it.hasNext())
        {
            param = (Parameter) it.next();
            if (param.getKey().startsWith("type.") && typeUri.equals(param.getValue()))
                return param.getKey().substring(5);
        }
        return null;
    }


    /**
     * Gets the (first) value for the specified attribute type URI.
     *
     * @param typeUri
     * @return
     */
    public String getAttributeValueByTypeUri(String typeUri)
    {
        return getAttributeValue(getAttributeAlias(typeUri));
    }

    /**
     * Returns a list with the attribute value(s) associated for the specified
     * attribute type URI.
     *
     * @param typeUri   The attribute type URI.
     * @return          List of attribute values.
     */
    public List getAttributeValuesByTypeUri(String typeUri)
    {
        return getAttributeValues(getAttributeAlias(typeUri));
    }

    /**
     * Gets the (first) value for the specified attribute alias.
     */
    public String getAttributeValue(String alias)
    {
        return (_parameters.hasParameter("count." + alias) && getCount(alias) > 0) ?
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
     * Gets the number of values provided in the attribute payload for the
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
     * Sets the number of values provided in the attribute payload for the
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

    protected boolean isValid()
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
                _log.warn("Invalid parameter name in AX payload: " + paramName);
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
                if (_parameters.hasParameterPrefix("value." + alias + "."))
                {
                    _log.warn("Count parameter not present for alias: " + alias
                              + "; value." + alias + ".[index] format is not allowed.");
                    return false;
                }
            }
            else // count.alias present
            {
                if (_parameters.hasParameter("value." + alias))
                {
                    _log.warn("Count parameter present for alias: " + alias
                              + "; should use value." + alias + ".[index] format.");
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
