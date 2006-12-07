/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.message;

import java.util.List;
import java.util.Arrays;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DirectError extends Message
{
    protected final static List requiredFields = Arrays.asList( new String[] {
            "ns",
            "error"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "contat",
            "reference"
    });

    protected DirectError(String msg)
    {
        this(msg, false);
    }

    protected DirectError(String msg, boolean compatibility)
    {
        set("error", msg);

        if (compatibility)
            set("ns", OPENID2_NS);
    }

    protected DirectError(ParameterList params)
    {
        super(params);
    }


    public static DirectError createDirectError(String msg)
    {
        return createDirectError(msg, false);
    }

    public static DirectError createDirectError(String msg, boolean compatibility)
    {
        DirectError err = new DirectError(msg, compatibility);

        if (! err.isValid())
        {
            //todo: log
        }

        return err;
    }

    public static DirectError createDirectError(ParameterList params)
    {
        DirectError err = new DirectError(params);

        if (! err.isValid())
        {
            //todo: log
        }

        return err;
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

    public void setContact(String contact)
    {
        set("contact", contact);
    }

    public void setReference(String reference)
    {
        set("reference", reference);
    }
}
