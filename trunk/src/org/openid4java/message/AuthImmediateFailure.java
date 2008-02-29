/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;

import java.util.List;
import java.util.Arrays;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthImmediateFailure extends Message
{
    private static Log _log = LogFactory.getLog(AuthImmediateFailure.class);
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
        if (compatibility)
        {
            set("openid.mode", MODE_IDRES);
            set("openid.user_setup_url", url);
        }
        else
        {
            set("openid.mode", MODE_SETUP_NEEDED);
            set("openid.ns", OPENID2_NS);
        }

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

        fail.validate();

        return fail;
    }

    public static AuthImmediateFailure createAuthImmediateFailure(ParameterList params)
            throws MessageException
    {
        AuthImmediateFailure fail = new AuthImmediateFailure(params);

        fail.validate();

        if (DEBUG)
            _log.debug("Retrieved auth immediate failure from message parameters:\n"
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

    public void validate() throws MessageException
    {
        super.validate();

        boolean compatibility = ! isVersion2();
        String mode = getParameterValue("openid.mode");

        if (compatibility)
        {
            try
            {
                new URL(getUserSetupUrl());
            }
            catch (MalformedURLException e)
            {
                throw new MessageException(
                    "Invalid user_setup_url in auth failure response.",
                    OpenIDException.AUTH_ERROR, e);
            }

            if (! MODE_IDRES.equals(mode))
                throw new MessageException(
                    "Invalid openid.mode in auth failure response; " +
                    "expected " + MODE_IDRES + " found: " + mode,
                    OpenIDException.AUTH_ERROR);
        }
        else if (! MODE_SETUP_NEEDED.equals(mode))
            throw new MessageException(
                "Invalid openid.mode in auth failure response; " +
                "expected " + MODE_SETUP_NEEDED + "found: " + mode,
                OpenIDException.AUTH_ERROR);
    }
}
