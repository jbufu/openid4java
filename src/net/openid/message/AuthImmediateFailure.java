/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

import java.util.List;
import java.util.Arrays;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthImmediateFailure extends Message
{
    protected final static List requiredFields = Arrays.asList( new String[] {
            "openid.mode"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "openid.ns",
            "openid.user_setup_url"
    });

    public AuthImmediateFailure(String url, boolean compatibility)
    {
        set("openid.mode", MODE_IDRES);

        if (compatibility)
            set("openid.user_setup_url", url);
        else
            set("openid.ns", OPENID2_NS);
    }

    public AuthImmediateFailure(ParameterList params) throws MessageException
    {
        super(params);
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

        try
        {
            new URL(getUserSetupUrl());
        }
        catch (MalformedURLException e)
        {
            return false;
        }

        if (compatibility && ! hasParameter("openid.user_setup_url"))
            return false;

        return MODE_IDRES.equals(getParameterValue("openid.mode"));
    }

}
