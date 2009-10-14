/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.openid4java.OpenIDException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class ConfigException extends OpenIDException
{
    public ConfigException(String message)
    {
        super(message, CONFIG_ERROR);
    }

    public ConfigException(String message, int code)
    {
        super(message, code);
    }

    public ConfigException(String message, Throwable cause)
    {
        super(message, CONFIG_ERROR, cause);
    }

    public ConfigException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }

    public ConfigException(int code, Throwable cause)
    {
        super(code, cause);
    }
}
