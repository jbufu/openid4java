/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * VerifyRequest is a AuthSuccess with the openid.mode
 * switched to check_authentication.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class VerifyRequest extends AuthSuccess
{
    private static Log _log = LogFactory.getLog(VerifyRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String MODE_CHKAUTH = "check_authentication";

    protected VerifyRequest(AuthSuccess authResp)
    {
        super(convertAuthSuccessParams(authResp));
    }

    private static ParameterList convertAuthSuccessParams(AuthSuccess authResp)
    {
        ParameterList params = new ParameterList(authResp.getParameterMap());

        params.set(new Parameter("openid.mode", MODE_CHKAUTH));

        return params;
    }

    protected VerifyRequest(ParameterList params)
    {
        super(params);
    }

    public static VerifyRequest createVerifyRequest(AuthSuccess authResp)
            throws MessageException
    {
        VerifyRequest req = new VerifyRequest(authResp);

        if (! req.isValid()) throw new MessageException(
                "Invalid set of parameters for a verification request");

        if (DEBUG) _log.debug("Created verification request " +
                "from a positive auth response:\n" + req.keyValueFormEncoding());

        return req;
    }

    public static VerifyRequest createVerifyRequest(ParameterList params)
            throws MessageException
    {
        VerifyRequest req = new VerifyRequest(params);

        if (! req.isValid()) throw new MessageException(
                "Invalid set of parameters for a verification request");

        if (DEBUG) _log.debug("Created verification request:\n"
                              + req.keyValueFormEncoding());

        return req;
    }

    public String getHandle()
    {
        return getParameterValue("openid.assoc_handle");
    }

    public String getInvalidateHandle()
    {
        return getParameterValue("openid.invalidate_handle");
    }

    public boolean isValid()
    {
        if (! MODE_CHKAUTH.equals(getParameterValue("openid.mode")))
        {
            _log.warn("Invalid openid.mode in verification request: "
                      + getParameterValue("openid.mode"));
            return false;
        }

        set("openid.mode", MODE_IDRES);

        if (DEBUG) _log.debug("Delegating verification request validity check " +
                              "to auth response...");

        if (! super.isValid() )
        {
            _log.warn("Invalid verification request: " +
                      "related auth response verification failed.");
            return false;
        }

        set("openid.mode", MODE_CHKAUTH);

        return true;
    }
}
