/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.yadis;

import net.openid.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisException extends OpenIDException
{
    private int statusCode;

    public YadisException(String message)
    {
        super(message);
        statusCode = YadisResult.UNKNOWN_ERROR;
    }

    public YadisException(String message, int status)
    {
        super(message);
        statusCode = status;
    }

    public YadisException(Throwable cause)
    {
        super(cause);
        statusCode = YadisResult.UNKNOWN_ERROR;
    }

    public YadisException(Throwable cause, int status)
    {
        super(cause);
        statusCode = status;
    }

    public YadisException(String message, Throwable cause)
    {
        super(message, cause);
        statusCode = YadisResult.UNKNOWN_ERROR;
    }

    public YadisException(String message, int status, Throwable cause)
    {
        super(message, cause);
        statusCode = status;
    }

    public int getStatusCode()
    {
        return statusCode;
    }
}
