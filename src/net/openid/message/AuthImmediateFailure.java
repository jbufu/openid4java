/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Arrays;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthImmediateFailure extends Message
{
    private static Logger _log = Logger.getLogger(AuthImmediateFailure.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList( new String[] {
            "openid.mode"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "openid.ns",
            "openid.user_setup_url"
    });

    protected AuthImmediateFailure(String url, String returnTo,
                                   boolean compatibility)
    {
        set("openid.mode", MODE_IDRES);

        if (compatibility)
            set("openid.user_setup_url", url);
        else
            set("openid.ns", OPENID2_NS);

        _destinationUrl = returnTo;
    }

    protected AuthImmediateFailure(ParameterList params)
    {
        super(params);
    }

    public static AuthImmediateFailure createAuthImmediateFailure(
            String url, String returnTo, boolean compatibility)
            throws MessageException
    {
        AuthImmediateFailure fail = new AuthImmediateFailure(url, returnTo, compatibility);

        if (! fail.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        return fail;
    }

    public static AuthImmediateFailure createAuthImmediateFailure(ParameterList params)
            throws MessageException
    {
        AuthImmediateFailure fail = new AuthImmediateFailure(params);

        if (! fail.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        if (DEBUG)
            _log.debug("Retrieved auth immediate failure from message parameters: "
                       + fail.keyValueFormEncoding());

        return fail;
    }

    public List getRequiredFields()
    {
        return requiredFields;
    }

    public boolean isVersion2()
    {
        return hasParameter("openid.ns") &&
                OPENID2_NS.equals(getParameterValue("openid.ns"));
    }

    public String getUserSetupUrl()
    {
        return getParameterValue("openid.user_setup_url");
    }

    public boolean isValid()
    {
        if (!super.isValid()) return false;

        boolean compatibility = ! isVersion2();

        if (compatibility)
        {
            try
            {
                new URL(getUserSetupUrl());
            }
            catch (MalformedURLException e)
            {
                _log.error("Error verifying auth immediate response validity.", e);
                return false;
            }
        }

        return MODE_IDRES.equals(getParameterValue("openid.mode"));
    }

}
