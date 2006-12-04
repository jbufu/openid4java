/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DirectError extends Message
{
    public DirectError(String msg)
    {
        this(msg, false);
    }

    public DirectError(String msg, boolean compatibility)
    {
        set("error", msg);

        if (compatibility)
            set("ns", OPENID2_NS);
    }

    public DirectError(ParameterList params) throws MessageException
    {
        super(params);
    }

    public void setErrorMsg(String msg)
    {
        set("error", msg);
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
