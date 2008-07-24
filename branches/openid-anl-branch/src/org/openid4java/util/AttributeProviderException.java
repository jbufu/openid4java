/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.openid4java.OpenIDException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class AttributeProviderException extends OpenIDException
{
    public AttributeProviderException(String message)
    {
        super(message, ATTRIBUTE_PROVIDER_ERROR);
    }

    public AttributeProviderException(String message, int code)
    {
        super(message, code);
    }

    public AttributeProviderException(String message, Throwable cause)
    {
        super(message, ATTRIBUTE_PROVIDER_ERROR, cause);
    }

    public AttributeProviderException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }

    public AttributeProviderException(int code, Throwable cause)
    {
        super(code, cause);
    }
}
