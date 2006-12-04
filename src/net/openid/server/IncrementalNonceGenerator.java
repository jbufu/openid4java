/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.util.InternetDateFormat;

import java.util.Date;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class IncrementalNonceGenerator implements NonceGenerator
{
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

        return _timestamp + Integer.toString(_counter);
    }

    private String getCurrentTimpestamp()
    {
        Date now = new Date();

        return _dateFormat.format(now);
    }
}
