/*
 * Copyright 2006-2008 Sxip Identity Corporation
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
        super(message, DISCOVERY_ERROR);
    }

    public DiscoveryException(String message, int code)
    {
        super(message, code);
    }

    public DiscoveryException(Throwable cause)
    {
        super(DISCOVERY_ERROR, cause);
    }

    public DiscoveryException(int code, Throwable cause)
    {
        super(code, cause);
    }

    public DiscoveryException(String message, Throwable cause)
    {
        super(message, DISCOVERY_ERROR, cause);
    }

    public DiscoveryException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }
}
