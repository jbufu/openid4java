/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message.ax;

import net.openid.message.MessageException;
import net.openid.message.Parameter;
import net.openid.message.ParameterList;

import java.util.Iterator;

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
    public StoreRequest()
    {
    }

    /**
     * Constructs a StoreRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<alias>." prefix.
     */
    public StoreRequest(ParameterList params) throws MessageException
    {
        _parameters = params;

        if (! isValid())
            throw new MessageException("Invalid parameters for a store request");

    }

    /**
     * Adds an attribute to the fetch request.
     *
     * @param       id          The identifier that will be associated with the
     *                          attribute name URI
     * @param       attrName    The attribute name URI
     * @param       value       The value of the attribute
     */
    public void addAttribute(String id, String attrName, String value)
    {
        _parameters.set(
                new Parameter("type." + id, attrName));
        _parameters.set(
                new Parameter("value." + id, value));
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
