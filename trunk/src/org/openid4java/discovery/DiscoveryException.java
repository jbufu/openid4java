/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import org.openid4java.OpenIDException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DiscoveryException extends OpenIDException
{
    public DiscoveryException(String message)
    {
        super(message);
    }

    public DiscoveryException(Throwable cause)
    {
        super(cause);
    }

    public DiscoveryException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
