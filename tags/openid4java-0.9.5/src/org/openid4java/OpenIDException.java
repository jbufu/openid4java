/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class OpenIDException extends Exception
{
    private int _errorCode;

    // error codes intended to help pinpoint the subsystem / cause of a failure
    public static final int OPENID_ERROR = 0x0000;

    public static final int MESSAGE_ERROR = 0x0100;
    public static final int ASSOC_ERROR = 0x0200;
    public static final int AUTH_ERROR = 0x0300;
    public static final int AUTH_REALM_ERROR = 0x0301;
    public static final int VERIFY_ERROR = 0x0400;

    public static final int DISCOVERY_ERROR = 0x0500;

    public static final int DISCOVERY_HTML_ERROR = 0x0600;
    public static final int DISCOVERY_HTML_GET_ERROR = 0x0601;
    public static final int DISCOVERY_HTML_NODATA_ERROR = 0x0602;
    public static final int DISCOVERY_HTML_PARSE_ERROR = 0x0603;

    public static final int YADIS_ERROR = 0x0700;
    public static final int YADIS_INVALID_URL = 0x0702;
    public static final int YADIS_INVALID_SCHEME = 0x0703;
    public static final int YADIS_HEAD_TRANSPORT_ERROR = 0x0704;
    public static final int YADIS_HEAD_INVALID_RESPONSE = 0x0705;
    public static final int YADIS_GET_ERROR = 0x0706;
    public static final int YADIS_GET_TRANSPORT_ERROR = 0x0707;
    public static final int YADIS_GET_INVALID_RESPONSE = 0x0708;
    public static final int YADIS_GET_NO_XRDS = 0x0709;
    public static final int YADIS_HTMLMETA_DOWNLOAD_ERROR = 0x070A;
    public static final int YADIS_HTMLMETA_INVALID_RESPONSE = 0x070B;
    public static final int XRDS_DOWNLOAD_ERROR = 0x070C;
    public static final int XRDS_PARSING_ERROR = 0x070D;
    public static final int YADIS_XRDS_SIZE_EXCEEDED = 0x070E;

    public static final int XRI_ERROR = 0x0800;

    public static final int SERVER_ERROR = 0x0900;
    public static final int CONSUMER_ERROR = 0x0A00;
    public static final int INFOCARD_ERROR = 0x0B00;

    public static final int EXTENSION_ERROR = 0x0C00;
    public static final int AX_ERROR = 0x0C10;
    public static final int SREG_ERROR = 0x0C20;
    public static final int PAPE_ERROR = 0x0C30;

    public OpenIDException(String message)
    {
        this(message, OPENID_ERROR);
    }

    public OpenIDException(String message, int code)
    {
        super(message);

        _errorCode = code;
    }

    public OpenIDException(String message, Throwable cause)
    {
        this(message, OPENID_ERROR, cause);
    }

    public OpenIDException(String message, int code, Throwable cause)
    {
        super(message, cause);

        _errorCode = code;
    }

    public OpenIDException(Throwable cause)
    {
        this(OPENID_ERROR, cause);
    }
    public OpenIDException(int code, Throwable cause)
    {
        super(cause);

        _errorCode = code;
    }

    public int getErrorCode()
    {
        return _errorCode;
    }

    public void setErrorCode(int errorCode)
    {
        this._errorCode = errorCode;
    }

    // override getMessage() to prefix with the error code
    public String getMessage()
    {
        return "0x" + Integer.toHexString(_errorCode) + ": " + super.getMessage();
    }
}
