/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.message;

import net.openid.association.AssociationSessionType;

import java.util.List;
import java.util.Arrays;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AssociationError extends DirectError
{
    public static final String ASSOC_ERR = "unsopported-type";

    protected final static List requiredFields = Arrays.asList( new String[] {
            "ns",
            "error_code",
            "error",
            "session_type"
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "assoc_type"
    });

    protected AssociationError(String msg, AssociationSessionType type)
    {
        super(msg);

        set("ns", OPENID2_NS);
        set("error_code", ASSOC_ERR);
        set("session_type", type.getSessionType());
        set("assoc_type", type.getAssociationType());
    }

    protected AssociationError(ParameterList params)
    {
        super(params);
    }

    public static AssociationError createAssociationError(
            String msg, AssociationSessionType type)
    {
        AssociationError err = new AssociationError(msg, type);

        if (! err.isValid())
        {
            //todo: log
        }

        return err;
    }

    public static AssociationError createAssociationError(ParameterList params)
    {
        AssociationError err = new AssociationError(params);

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

    public void setAssociationSessionType(AssociationSessionType type)
    {
        set("session_type", type.getSessionType());
        set("assoc_type", type.getAssociationType());
    }

    public String getSessionType()
    {
        return getParameterValue("session_type");
    }

    public String getAssocType()
    {
        return getParameterValue("assoc_type");
    }

    private String getErrorCode()
    {
        return getParameterValue("error_code");
    }

    public boolean isValid()
    {
        if (!super.isValid()) return false;

        return ASSOC_ERR.equals(getErrorCode()) &&
                OPENID2_NS.equals(getParameter("ns"));

    }

}
