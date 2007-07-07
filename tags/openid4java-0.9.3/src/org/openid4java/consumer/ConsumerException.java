/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.openid4java.OpenIDException;

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
