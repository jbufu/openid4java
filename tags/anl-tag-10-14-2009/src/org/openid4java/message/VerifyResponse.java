/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;

import java.util.List;
import java.util.Arrays;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class VerifyResponse extends Message
{
    private static Log _log = LogFactory.getLog(VerifyResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList(new String[] {
            "is_valid"
    });

    protected final static List optionalFields = Arrays.asList(new String[] {
            "ns",
            "invalidate_handle"
    });

    protected VerifyResponse(boolean compatibility)
    {
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

        resp.validate();

        if (DEBUG) _log.debug("Created verification response:\n"
                              + resp.keyValueFormEncoding());

        return resp;
    }

    public static VerifyResponse createVerifyResponse(ParameterList params)
            throws MessageException
    {
        VerifyResponse resp = new VerifyResponse(params);

        resp.validate();

        if (DEBUG) _log.debug("Created verification response:\n"
                              + resp.keyValueFormEncoding());

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
        if (DEBUG)
            _log.debug("Setting is_valid to: " + verified);
        
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

    public void validate() throws MessageException
    {
        super.validate();

        if (! "true".equals(getParameterValue("is_valid")) &&
                ! "false".equals(getParameterValue("is_valid")) )
        {
            throw new MessageException(
                "Invalid is_valid value in verification response: "
                 + getParameterValue("is_valid"),
                OpenIDException.VERIFY_ERROR);
        }
    }
}
