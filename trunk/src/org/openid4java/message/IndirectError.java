/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class IndirectError extends Message
{
    private static Log _log = LogFactory.getLog(IndirectError.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected IndirectError(String msg, String returnTo)
    {
        this(msg, returnTo, false);
    }
    protected IndirectError(String msg, String returnTo, boolean compatibility)
    {
        this(null, msg, returnTo, compatibility);
    }

    protected IndirectError(OpenIDException e, String msg, String returnTo, boolean compatibility)
    {
        set("openid.mode", "error");
        set("openid.error", msg);
        _destinationUrl = returnTo;
        _exception = e;

        if (! compatibility)
            set("openid.ns", OPENID2_NS);
    }

    // exception that generated the error, if any
    private OpenIDException _exception;

    protected IndirectError(ParameterList params)
    {
        super(params);
    }

    public static IndirectError createIndirectError(OpenIDException e, String returnTo)
    {
        return createIndirectError(e, returnTo, false);
    }

    public static IndirectError createIndirectError(String msg, String returnTo)
    {
        return createIndirectError(msg, returnTo, false);
    }

    public static IndirectError createIndirectError(OpenIDException e,
                                                    String returnTo,
                                                    boolean compatibility)
    {
        return createIndirectError(e, e.getMessage(), returnTo, compatibility);
    }


    public static IndirectError createIndirectError(String msg, String returnTo,
                                                    boolean compatibility)
    {
        return createIndirectError(null, msg, returnTo, compatibility);
    }

    public static IndirectError createIndirectError(OpenIDException e,
                                                    String msg, String returnTo,
                                                    boolean compatibility)
    {
        IndirectError err = new IndirectError(e, msg, returnTo, compatibility);

        try
        {
            err.validate();
        }
        catch (MessageException ex)
        {
            _log.error("Invalid " + (compatibility? "OpenID1" : "OpenID2") +
                       " indirect error message created for message: " + msg);
        }

        _log.debug("Created indirect error message:\n" + err.keyValueFormEncoding());

        return err;
    }

    public static IndirectError createIndirectError(ParameterList params)
    {
        IndirectError err = new IndirectError(params);

        try
        {
            err.validate();
        }
        catch (MessageException e)
        {
            _log.error("Invalid direct error message created: "
                       + err.keyValueFormEncoding() );
        }

        _log.debug("Created indirect error message:\n" + err.keyValueFormEncoding());

        return err;
    }

    public OpenIDException getException()
    {
        return _exception;
    }

    public void setException(OpenIDException e)
    {
        this._exception = e;
    }

    public void setErrorMsg(String msg)
    {
        set("openid.error", msg);
    }

    public String getErrorMsg()
    {
        return getParameterValue("openid.error");
    }

    public void setContact(String contact)
    {
        set("openid.contact", contact);
    }

    public String getContact()
    {
        return getParameterValue("openid.contact");
    }

    public void setReference(String reference)
    {
        set("openid.reference", reference);
    }

    public String getReference()
    {
        return getParameterValue("openid.reference");
    }

}
