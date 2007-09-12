/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class OpenIDException extends Exception
{
    private int _errorCode;

    // error codes intended to help pinpoint the subsystem / cause of a failure
    public static final int OPENID_ERROR = 0;

    public static final int MESSAGE_ERROR = 100;
    public static final int ASSOC_ERROR = 200;

    public static final int DISCOVERY_ERROR = 300;
    public static final int HTML = 400;

    public static final int YADIS_ERROR = 500;
    public static final int YADIS_INVALID_URL = 502;
    public static final int YADIS_INVALID_SCHEME = 503;
    public static final int YADIS_HEAD_TRANSPORT_ERROR = 504;
    public static final int YADIS_HEAD_INVALID_RESPONSE = 505;
    public static final int YADIS_GET_ERROR = 506;
    public static final int YADIS_GET_TRANSPORT_ERROR = 507;
    public static final int YADIS_GET_INVALID_RESPONSE = 508;
    public static final int YADIS_GET_NO_XRDS = 509;
    public static final int YADIS_HTMLMETA_DOWNLOAD_ERROR = 510;
    public static final int YADIS_HTMLMETA_INVALID_RESPONSE = 511;
    public static final int YADIS_XRDS_DOWNLOAD_ERROR = 512;
    public static final int YADIS_XRDS_PARSING_ERROR = 513;
    public static final int YADIS_XRDS_SIZE_EXCEEDED = 514;

    public static final int XRI_ERROR = 600;

    public static final int SERVER_ERROR = 700;
    public static final int CONSUMER_ERROR = 800;
    public static final int INFOCARD_ERROR = 900;

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
        return Integer.toString(_errorCode) + ": " + super.getMessage();
    }
}
