/*
 * Copyright 2006-2007 Sxip Identity Corporation
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
        super(message);
    }

    public AssociationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AssociationException(Throwable cause)
    {
        super(cause);
    }
}
