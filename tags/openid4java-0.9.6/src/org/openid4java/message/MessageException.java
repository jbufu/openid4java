/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.openid4java.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class MessageException extends OpenIDException
{
    public MessageException(String message)
    {
        super(message, MESSAGE_ERROR);
    }

    public MessageException(String message, int code)
    {
        super(message, code);
    }

    public MessageException(Throwable cause)
    {
        super(MESSAGE_ERROR, cause);
    }

    public MessageException(int code, Throwable cause)
    {
        super(code, cause);
    }

    public MessageException(String message, Throwable cause)
    {
        super(message, MESSAGE_ERROR, cause);
    }

    public MessageException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }
}
