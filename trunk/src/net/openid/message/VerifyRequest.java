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

    public VerifyRequest(AuthSuccess authResp) throws MessageException
    {
        super(convertAuthSuccessParams(authResp));
    }

    private static ParameterList convertAuthSuccessParams(AuthSuccess authResp)
    {
        ParameterList params = new ParameterList(authResp.getParameterMap());

        params.set(new Parameter("openid.mode", MODE_CHKAUTH));

        return params;
    }

    public VerifyRequest(ParameterList params) throws MessageException
    {
        super(params);
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
