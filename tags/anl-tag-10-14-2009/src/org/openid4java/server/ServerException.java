/*
 * Copyright 2006-2008 Sxip Identity Corporation
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
        super(message, SERVER_ERROR);
    }

    public ServerException(String message, int code)
    {
        super(message, code);
    }

    public ServerException(String message, Throwable cause)
    {
        super(message, SERVER_ERROR, cause);
    }

    public ServerException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }

    public ServerException(Throwable cause)
    {
        super(cause);
    }

    public ServerException(int code, Throwable cause)
    {
        super(code, cause);
    }
}
