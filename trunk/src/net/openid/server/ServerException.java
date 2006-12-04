/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ServerException extends OpenIDException
{
    public ServerException(String message)
    {
        super(message);
    }

    public ServerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServerException(Throwable cause)
    {
        super(cause);
    }

}
