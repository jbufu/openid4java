/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.MessageException;
import net.openid.message.Parameter;
import net.openid.message.ParameterList;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implements the extension for Attribute Exchange store requests.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class StoreRequest extends AxMessage
{
    /**
     * Constructs a Store Request with an empty parameter list.
     */
    protected StoreRequest()
    {
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
     * the "openid.<alias>." prefix.
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
     * the "openid.<alias>." prefix.
     */
    public static StoreRequest createStoreRequest(ParameterList params)
            throws MessageException
    {
        StoreRequest req = new StoreRequest(params);

        if (! req.isValid())
            throw new MessageException("Invalid parameters for a store request");

        return req;
    }

    /**
     * Adds an attribute to the store request.
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

        String value = getParameterValue("value." + id);
        if (value != null)
        {
            List attrs = Arrays.asList(value.split(","));
            Iterator iter = attrs.iterator();
            while (iter.hasNext())
                decodedAttrs.add(multivalDecode((String) iter.next()));
        }
        return decodedAttrs;
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
        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (! paramName.startsWith("type.") &&
                    ! paramName.startsWith("value.") )
                return false;

            if ( paramName.startsWith("value.") &&
                    !_parameters.hasParameter("type." + paramName.substring(6)))
                return false;
        }

        return true;
    }
}
