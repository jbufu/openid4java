/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.infocard;

import org.openid4java.OpenIDException;

/**
 * @author Johnny Bufu
 */
public class InfocardException extends OpenIDException
{
    public InfocardException(String message)
    {
        super(message, INFOCARD_ERROR);
    }

    public InfocardException(String message, int code)
    {
        super(message, code);
    }

    public InfocardException(String message, Throwable cause)
    {
        super(message, INFOCARD_ERROR, cause);
    }

    public InfocardException(Throwable cause)
    {
        super(INFOCARD_ERROR, cause);
    }
}
