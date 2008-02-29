/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.openid4java.discovery.DiscoveryException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisException extends DiscoveryException
{
    public YadisException(String message)
    {
        super(message, YADIS_ERROR);
    }

    public YadisException(String message, int code)
    {
        super(message, code);
    }

    public YadisException(Throwable cause)
    {
        super(YADIS_ERROR, cause);
    }

    public YadisException(int code, Throwable cause)
    {
        super(code, cause);
    }

    public YadisException(String message, Throwable cause)
    {
        super(message, YADIS_ERROR, cause);
    }

    public YadisException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }
}
