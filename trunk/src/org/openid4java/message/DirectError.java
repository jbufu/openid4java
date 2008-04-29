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
public class DirectError extends Message
{
    private static Log _log = LogFactory.getLog(DirectError.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList( new String[] {
            "error"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "ns",
            "contact",
            "reference"
    });

    // exception that generated the error, if any
    private OpenIDException _exception;

    protected DirectError(String msg)
    {
        this(msg, false);
    }

    protected DirectError(String msg, boolean compatibility)
    {
        this(null, msg, compatibility);
    }

    protected DirectError(OpenIDException e, boolean compatibility)
    {
        this(e, e.getMessage(), compatibility);
    }

    protected DirectError(OpenIDException e, String msg, boolean compatibility)
    {
        set("error", msg);
        _exception = e;

        if ( ! compatibility )
            set("ns", OPENID2_NS);
    }

    protected DirectError(ParameterList params)
    {
        super(params);
    }


    public static DirectError createDirectError(OpenIDException e)
    {
        return createDirectError(e, false);
    }

    public static DirectError createDirectError(String msg)
    {
        return createDirectError(null, msg, false);
    }

    public static DirectError createDirectError(String msg, boolean compatibility)
    {
        return createDirectError(null, msg, compatibility);
    }

    public static DirectError createDirectError(OpenIDException e, boolean compatibility)
    {
        return createDirectError(e, e.getMessage(), compatibility);
    }

    public static DirectError createDirectError(OpenIDException e, String msg, boolean compatibility)
    {
        DirectError err = new DirectError(e, msg, compatibility);

        try
        {
            err.validate();
        }
        catch (MessageException ex)
        {
            _log.error("Invalid " + (compatibility? "OpenID1" : "OpenID2") +
                       " direct error message created for message: " + msg);
        }

        _log.debug("Created direct error message:\n" + err.keyValueFormEncoding());

        return err;
    }

    public static DirectError createDirectError(ParameterList params)
    {
        DirectError err = new DirectError(params);

        try
        {
            err.validate();
        }
        catch (MessageException e)
        {
            _log.error("Invalid direct error message created: "
                       + err.keyValueFormEncoding() );
        }

        _log.debug("Created direct error message:\n" + err.keyValueFormEncoding());

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

    public List getRequiredFields()
    {
        return requiredFields;
    }

    public boolean isVersion2()
    {
        return hasParameter("ns") &&
                OPENID2_NS.equals(getParameterValue("ns"));
    }

    public void setErrorMsg(String msg)
    {
        set("error", msg);
    }

    public String getErrorMsg()
    {
        return getParameterValue("error");
    }

    public void setContact(String contact)
    {
        set("contact", contact);
    }

    public void setReference(String reference)
    {
        set("reference", reference);
    }
}
