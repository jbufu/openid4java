/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.openid4java.OpenIDException;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class IdPInvalidException extends OpenIDException
{
    public IdPInvalidException(String message)
    {
        super(message, IDP_INVALID_ERROR);
    }

    public IdPInvalidException(String message, int code)
    {
        super(message, code);
    }

    public IdPInvalidException(String message, Throwable cause)
    {
        super(message, IDP_INVALID_ERROR, cause);
    }

    public IdPInvalidException(String message, int code, Throwable cause)
    {
        super(message, code, cause);
    }

    public IdPInvalidException(int code, Throwable cause)
    {
        super(code, cause);
    }
}
