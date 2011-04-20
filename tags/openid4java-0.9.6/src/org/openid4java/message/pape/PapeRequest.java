/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message.pape;

import org.openid4java.message.MessageException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.OpenIDException;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the extension for OpenID Provider Authentication Policy requests.
 *
 * @see PapeMessage Message
 * @author Marius Scurtescu, Johnny Bufu
 */
public class PapeRequest extends PapeMessage
{
    private static Log _log = LogFactory.getLog(PapeRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List PAPE_FIELDS = Arrays.asList( new String[] {
            "preferred_auth_policies", "preferred_auth_level_types", "max_auth_age"
    });

    /**
     * Constructs a Pape Request with an empty parameter list.
     */
    protected PapeRequest()
    {
        set("preferred_auth_policies", "");

        if (DEBUG) _log.debug("Created empty Pape request.");
    }

    /**
     * Constructs a Pape Request with an empty parameter list.
     */
    public static PapeRequest createPapeRequest()
    {
        return new PapeRequest();
    }

    /**
     * Constructs a PapeRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    protected PapeRequest(ParameterList params)
    {
        super(params);
    }

    /**
     * Constructs a PapeRequest from a parameter list.
     * <p>
     * The parameter list can be extracted from a received message with the
     * getExtensionParams method of the Message class, and MUST NOT contain
     * the "openid.<extension_alias>." prefix.
     */
    public static PapeRequest createPapeRequest(ParameterList params)
            throws MessageException
    {
        PapeRequest req = new PapeRequest(params);

        req.validate();

        if (DEBUG)
            _log.debug("Created PAPE request from parameter list:\n" + params);

        return req;
    }

    /**
     * Gets the preferred_auth_policies parameter value.
     */
    public String getPreferredAuthPolicies()
    {
        return getParameterValue("preferred_auth_policies");
    }

    /**
     * Sets a new value for the preferred_auth_policies parameter.
     *
     * The previous value of the parameter will be owerwritten.
     *
     * @param policyUris    Space separated list of authentication policy
     *                      URIs to be set.
     * @see #addPreferredAuthPolicy(String)
     */
    public void setPreferredAuthPolicies(String policyUris)
    {
        // todo: enforce that policyUri is a valid URI?

        set("preferred_auth_policies", policyUris);
    }

    /**
     * Adds an authentication policy URI to the preferred_auth_policies
     * parameter.
     *
     * @param policyUri     The authentication policy URI to be set.
     * @see #setPreferredAuthPolicies(String)
     */
    public void addPreferredAuthPolicy(String policyUri)
    {
        // todo: check that policyUri isn't already in the list?

        String policies = getPreferredAuthPolicies();

        if (policies == null || policies.length() == 0)
            setPreferredAuthPolicies(policyUri);

        else
            setPreferredAuthPolicies(policies + " " + policyUri);
    }

    /**
     * Gets a list with the preferred_auth_policies. An empty list is
     * returned if no authentication policies exist.
     *
     */
    public List getPreferredAuthPoliciesList()
    {
        String policies = getParameterValue("preferred_auth_policies");

        if (policies != null)
            return Arrays.asList(policies.split(" "));
        else
            return new ArrayList();
    }

    /**
     * Sets the max_auth_age parameter.
     *
     * @param seconds   The number of seconds within which the OP is
     *                  requested to have actively authenticated the user.
     */
    public void setMaxAuthAge(int seconds)
    {
        set("max_auth_age", Integer.toString(seconds));
    }

    /**
     * Gets the max_auth_age parameter.
     *
     * @return          The number of seconds within which the OP is
     *                  requested to have actively authenticated the user,
     *                  or -1 if max_auth_age is not present in the request.
     */
    public int getMaxAuthAge()
    {
        String maxAuthAge = getParameterValue("max_auth_age");

        if (maxAuthAge != null)
            return Integer.parseInt(maxAuthAge);
        else
            return -1;
    }

    /**
     * Checks the validity of the extension.
     * <p>
     * Used when constructing a extension from a parameter list.
     *
     * @throws MessageException if the PapeRequest is not valid.
     */
    public void validate() throws MessageException
    {
        if (! _parameters.hasParameter("preferred_auth_policies"))
        {
            throw new MessageException(
                "preferred_auth_policies is required in a PAPE request.",
                OpenIDException.PAPE_ERROR);
        }

        Iterator it = _parameters.getParameters().iterator();
        while (it.hasNext())
        {
            String paramName = ((Parameter) it.next()).getKey();
            if (! PAPE_FIELDS.contains(paramName) && ! paramName.startsWith(PapeMessage.AUTH_LEVEL_NS_PREFIX))
            {
                throw new MessageException(
                    "Invalid parameter name in PAPE request: " + paramName,
                    OpenIDException.PAPE_ERROR);
            }
        }
    }

    public void addPreferredCustomAuthLevel(String authLevelTypeUri)
    {
        String alias = addAuthLevelExtension(authLevelTypeUri);
        String preferred = getParameterValue("preferred_auth_level_types");
        set("preferred_auth_level_types", preferred == null ? alias : preferred + " " + alias);
    }
}
