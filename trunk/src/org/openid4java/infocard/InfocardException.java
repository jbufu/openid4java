/*
 * Copyright 2006-2007 Sxip Identity Corporation
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
        super(message);
    }

    public InfocardException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InfocardException(Throwable cause)
    {
        super(cause);
    }

}
