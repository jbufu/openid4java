/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.server;

import org.openid4java.OpenIDException;

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
