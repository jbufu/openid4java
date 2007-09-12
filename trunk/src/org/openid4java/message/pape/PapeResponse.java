/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.message.pape;

import org.openid4java.message.ParameterList;
import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the extension for OpenID Provider Authentication Policy responses.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class PapeResponse extends PapeMessage
{
    private static Log _log = LogFactory.getLog(PapeResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String AUTH_AGE_UNKNOWN = "none";

    protected final static List PAPE_FIELDS = Arrays.asList( new String[] {
            "auth_policies", "auth_age", "nist_auth_level"
    });



    /**
     * Constructs a Pape Response with an empty parameter list.
     */
    protected PapeResponse()
    {
        set("auth_policies", "");

        if (DEBUG) _log.debug("Created empty PAPE response.");
    }

    /**
     * Constructs a Pape Response with an empty parameter list.
     */
    public static PapeResponse createPapeResponse()
    {
        return new PapeResponse();
    }

    /**
     * Constructs a Pape Response from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected PapeResponse(ParameterList params)
    {
        _parameters = params;
    }

    public static PapeResponse createPapeResponse(ParameterList params)
            throws MessageException
    {
        PapeResponse resp = new PapeResponse(params);

        if (! resp.isValid())
            throw new MessageException("Invalid parameters for a PAPE response");

        if (DEBUG)
            _log.debug("Created PAPE response from parameter list:\n" + params);

        return resp;
    }

    /**
     * Gets the auth_policies parameter value.
     */
    public String getAuthPolicies()
    {
        return getParameterValue("auth_policies");
    }

    /**
     * Sets a new value for the auth_policies parameter.
     *
     * The previous value of the parameter will be owerwritten.
     *
     * @param policyUris    Space separated list of authentication policy
     *                      URIs to be set.
     * @see #addAuthPolicy(String)
     */
    public void setAuthPolicies(String policyUris)
    {
        // todo: enforce that policyUri is a valid URI?

        set("auth_policies", policyUris);
    }

    /**
     * Adds an authentication policy URI to the auth_policies
     * parameter.
     *
     * @param policyUri     The authentication policy URI to be set.
     * @see #setAuthPolicies(String)
     */
    public void addAuthPolicy(String policyUri)
    {
        // todo: check that policyUri isn't already in the list?

        String policies = getAuthPolicies();

        if (policies == null || policies.length() == 0)
            setAuthPolicies(policyUri);

        else
            setAuthPolicies(policies + " " + policyUri);
    }

    /**
     * Gets a list with the auth_policies. An empty list is
     * returned if no authentication policies exist.
     */
    public List getAuthPoliciesList()
    {
        String policies = getParameterValue("auth_policies");

        if (policies != null)
            return Arrays.asList(policies.split(" "));
        else
            return new ArrayList();
    }

    /**
     * Sets the auth_age parameter.
     *
     * @param seconds   The number of seconds since the user was actively
     *                  authenticated by the OP, or -1 if the auth_age
     *                  is unknown.
     */
    public void setAuthAge(int seconds)
    {
        // todo: have a timestamp field; convert it to auth_age when sending?

        if (-1 == seconds)
            set("auth_age", AUTH_AGE_UNKNOWN);
        else
            set("auth_age", Integer.toString(seconds));
    }

    /**
     * Gets the value of the auth_age parameter.
     *
     * @return          The number of seconds since the user was actively
     *                  authenticated by the OP. For the special value
     *                  "unknown" 0 is returned; if the parameter is not
     *                  present, -1 is returned.
     */
    public int getAuthAge()
    {
        String authAge = getParameterValue("auth_age");

        if (authAge == null)
            return -1;

        else if (AUTH_AGE_UNKNOWN.equals(authAge))
            return 0;
        
        else
            return Integer.parseInt(authAge);
    }

    /**
     * Gets the value of the nist_auth_level parameter.
     * <p>
     * NIST levels are integers between 1 and 4 inclusive. Level 0 is
     * used to signify that the OP recognizes the parameter and the
     * user authentication did not meet the requirements of Level 1.
     *
     * @return          The NIST level, or -1 if the parameter is not set.
     */
    public int getNistAuthLevel()
    {
        String level = getParameterValue("nist_auth_level");

        if ( level != null && level.length() > 0 )
            return Integer.parseInt(level);
        else
            return -1;
    }

    public void setNistAuthLevel(int level) throws MessageException
    {
        if (level < 0 || level > 4)
            throw new MessageException("Invalid NIST level: " + level);

        set("nist_auth_level", Integer.toString(level));
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
        if (! _parameters.hasParameter("auth_policies"))
        {
            _log.warn("auth_policies is required in a PAPE response.");
            return false;
        }

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();

            if (! PAPE_FIELDS.contains(paramName))
            {
                _log.warn("Invalid parameter name in PAPE response: " + paramName);
                return false;
            }
        }

        return true;
    }
}
