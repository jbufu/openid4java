/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;
   
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Arrays;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AuthFailure extends Message
{
    private static Logger _log = Logger.getLogger(AuthFailure.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList( new String[] {
            "openid.mode"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "openid.ns"
    });

    public AuthFailure(boolean compatibility, String returnTo)
    {
        set("openid.mode", MODE_CANCEL);

        if (! compatibility)
            set("openid.ns", OPENID2_NS);

        _destinationUrl = returnTo;
    }

    protected AuthFailure(ParameterList params)
    {
        super(params);
    }

    public static AuthFailure createAuthFailure(ParameterList params) throws
            MessageException
    {
        AuthFailure fail = new AuthFailure(params);

        if (! fail.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        if (DEBUG)
            _log.debug("Retrieved auth failure from message parameters: "
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

    public boolean isValid()
    {
        if (!super.isValid()) return false;

        return MODE_CANCEL.equals(getParameterValue("openid.mode"));
    }
}
