/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.discovery;

import net.openid.OpenIDException;

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
