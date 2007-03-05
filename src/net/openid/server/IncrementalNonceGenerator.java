/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.util.InternetDateFormat;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class IncrementalNonceGenerator implements NonceGenerator
{
    private static Logger _log = Logger.getLogger(IncrementalNonceGenerator.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private static InternetDateFormat _dateFormat = new InternetDateFormat();

    private String _timestamp = "";
    private int _counter = 0;

    public String next()
    {
        String currentTimestamp = getCurrentTimpestamp();

        if (_timestamp.equals(currentTimestamp))
        {
            _counter++;
        }
        else
        {
            _timestamp = currentTimestamp;
            _counter = 0;
        }

        String nonce = _timestamp + Integer.toString(_counter);

        if (DEBUG) _log.debug("Generated nonce: " + nonce);

        return nonce;

    }

    private String getCurrentTimpestamp()
    {
        Date now = new Date();

        return _dateFormat.format(now);
    }
}
