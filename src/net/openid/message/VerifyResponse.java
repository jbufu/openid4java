/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

import java.util.List;
import java.util.Arrays;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class VerifyResponse extends Message
{
    protected final static List requiredFields = Arrays.asList(new String[] {
            //todo: "mode", is mandatory, JanRain implementations ommit it, also see below
            "is_valid"
    });

    protected final static List optionalFields = Arrays.asList(new String[] {
            "ns",
            "invalidate_handle"
    });

    protected VerifyResponse(boolean compatibility)
    {
        set("mode", MODE_IDRES);
        setSignatureVerified(false);

        if (! compatibility)
            set("ns", OPENID2_NS);
    }

    protected VerifyResponse(ParameterList params)
    {
        super(params);
    }

    public static VerifyResponse createVerifyResponse(boolean compatibility)
            throws MessageException
    {
        VerifyResponse resp = new VerifyResponse(compatibility);

        if (! resp.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        return resp;
    }

    public static VerifyResponse createVerifyResponse(ParameterList params)
            throws MessageException
    {
        VerifyResponse resp = new VerifyResponse(params);

        if (! resp.isValid()) throw new MessageException(
                "Invalid set of parameters for the requested message type");

        return resp;
    }

    public List getRequiredFields()
    {
        return requiredFields;
    }

    public boolean isVersion2()
    {
        return hasParameter("ns") && OPENID2_NS.equals(getParameterValue("ns"));
    }

    public void setSignatureVerified(boolean verified)
    {
        set("is_valid", verified ? "true" : "false");
    }

    public boolean isSignatureVerified()
    {
        return "true".equals(getParameterValue("is_valid"));
    }

    public void setInvalidateHandle(String handle)
    {
        set("invalidate_handle", handle);
    }

    public String getInvalidateHandle()
    {
        return getParameterValue("invalidate_handle");
    }

    public boolean isValid()
    {
        if (! super.isValid()) return false;

        //todo: "mode" is mandatory, JanRain implementations ommit it, also see above
//        if (! MODE_IDRES.equals(getParameterValue("openid.mode")))
//            return false;

        return "true".equals(getParameterValue("is_valid")) ||
                "false".equals(getParameterValue("is_valid"));
    }
}
