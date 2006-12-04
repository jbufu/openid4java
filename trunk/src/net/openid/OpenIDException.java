/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class OpenIDException extends Exception
{
    public OpenIDException(String message)
    {
        super(message);
    }

    public OpenIDException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OpenIDException(Throwable cause)
    {
        super(cause);
    }
}
