/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ConsumerException extends OpenIDException
{

    public ConsumerException(String message)
    {
        super(message);
    }

    public ConsumerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ConsumerException(Throwable cause)
    {
        super(cause);
    }

}
