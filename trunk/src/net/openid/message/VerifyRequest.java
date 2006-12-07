/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;


/**
 * VerifyRequest is a AuthSuccess with the openid.mode
 * switched to check_authentication.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class VerifyRequest extends AuthSuccess
{
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
                "Invalid set of parameters for the requested message type");

        return req;
    }

    public static VerifyRequest createVerifyRequest(ParameterList params)
            throws MessageException
    {
        VerifyRequest req = new VerifyRequest(params);

        if (! req.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

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
                return false;

        set("openid.mode", MODE_IDRES);

        if (! super.isValid() ) return false;

        set("openid.mode", MODE_CHKAUTH);

        return true;
    }
}
