/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.sreg;

import org.openid4java.message.ParameterList;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the extension for Simple Registration fetch responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class SRegResponse extends SRegMessage
{
    private static Log _log = LogFactory.getLog(SRegResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List SREG_FIELDS = Arrays.asList( new String[] {
            "nickname", "email", "fullname", "dob", "gender",
            "postcode", "country", "language", "timezone"
    });

    /**
     * Constructs a SReg Response with an empty parameter list.
     */
    protected SRegResponse()
    {
        if (DEBUG) _log.debug("Created empty fetch response.");
    }

    /**
     * Constructs a SReg Response with an empty parameter list.
     */
    public static SRegResponse createFetchResponse()
    {
        return new SRegResponse();
    }

    /**
     * Constructs a SReg Response from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected SRegResponse(ParameterList params)
    {
        _parameters = params;
    }

    public static SRegResponse createSRegResponse(ParameterList params)
            throws MessageException
    {
        SRegResponse resp = new SRegResponse(params);

        if (! resp.isValid())
            throw new MessageException("Invalid parameters for a SReg response");

        if (DEBUG)
            _log.debug("Created SReg response from parameter list:\n" + params);

        return resp;
    }

    /**
     * Creates a SRegResponse from a SRegRequest message and the data released
     * by the user.
     *
     * @param req               SRegRequest message.
     * @param userData          Map<String attributeName, String attributeValue> with the
     *                          data released by the user.
     * @return                  Properly formed SRegResponse.
     * @throws MessageException if any attribute-name in the userData map does not
     *                          correspond to an SREG field-name.
     */
    public static SRegResponse createSRegResponse(SRegRequest req, Map userData)
            throws MessageException
    {
        SRegResponse resp = new SRegResponse();

        List attributes = req.getAttributes();
        Iterator iter = attributes.iterator();
        while (iter.hasNext())
        {
            String attr = (String) iter.next();
            String value = (String) userData.get(attr);
            if (value != null)
                resp.addAttribute(attr, value);
        }

        return resp;
    }


    /**
     * Adds an attribute to the SReg response. The allowed attribute names are
     * the ones defined in the SReg specification: nickname, email, fullname,
     * dob, gender, postcode, country, language, timezone.
     *
     * @param       attr        An attribute name.
     * @param       value       The value of the attribute.
     */
    public void addAttribute(String attr, String value) throws MessageException
    {
        _parameters.set(new Parameter(attr, value));

        if (! SREG_FIELDS.contains(attr))
            throw new MessageException("Invalid attribute for SReg: " + attr);

        if (DEBUG)
            _log.debug("Added new attribute to SReg response: " + attr +
                       " value: " + value);
    }

    /**
     * Returns the value of an attribute.
     *
     * @param attr      The attribute name.
     * @return          The attribute value.
     */
    public String getAttributeValue(String attr)
    {
        return getParameterValue(attr);
    }

    /**
     * Gets a list of attribute names in the SReg response.
     */
    public List getAttributeNames()
    {
        List attributes = new ArrayList();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            attributes.add(((Parameter) it.next()).getKey());
        }

        return attributes;
    }

    /**
     * Gets a map with attribute names -> values.
     */
    public Map getAttributes()
    {
        Map attributes = new HashMap();

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String attr = ((Parameter) it.next()).getKey();
            attributes.put(attr, getAttributeValue(attr));
        }

        return attributes;
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

            if (! SREG_FIELDS.contains(paramName))
            {
                _log.warn("Invalid parameter name in SReg response: " + paramName);
                return false;
            }
        }

        return true;
    }
}
