/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.association;

import org.openid4java.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AssociationException extends OpenIDException
{
    public AssociationException(String message)
    {
        super(message, ASSOC_ERROR);
    }

    public AssociationException(String message, int code)
    {
        super(message, code);
    }

    public AssociationException(String message, Throwable cause)
    {
        super(message, ASSOC_ERROR, cause);
    }

    public AssociationException(Throwable cause)
    {
        super(cause);
    }

    public AssociationException(int code, Throwable cause)
    {
        super(code, cause);
    }

    public AssociationException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }
}
