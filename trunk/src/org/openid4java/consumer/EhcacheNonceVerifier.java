/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class EhcacheNonceVerifier extends AbstractNonceVerifier
{
    private static Log _log = LogFactory.getLog(EhcacheNonceVerifier.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private Cache _cache;

    public EhcacheNonceVerifier(int maxAge)
    {
        super(maxAge);
    }

    public void setCache(Cache cache)
    {
        if (cache.getTimeToLiveSeconds() != _maxAgeSeconds)
        {
            throw new IllegalArgumentException("Max Age: " + _maxAgeSeconds + ", same expected for cache, but found: " + cache.getTimeToLiveSeconds());
        }

        if (cache.getTimeToLiveSeconds() != cache.getTimeToIdleSeconds())
        {
            throw new IllegalArgumentException("Cache must have same timeToLive (" + cache.getTimeToLiveSeconds() + ") as timeToIdle (" + cache.getTimeToIdleSeconds() + ")");
        }

        _cache = cache;
    }

    protected int seen(Date now, String opUrl, String nonce)
    {
        String pair = opUrl + '#' + nonce;
        Element element = new Element(pair, pair);

        if (_cache.get(pair) != null)
        {
            _log.error("Possible replay attack! Already seen nonce: " + nonce);
            return SEEN;
        }

        _cache.put(element);

        if (DEBUG) _log.debug("Nonce verified: " + nonce);

        return OK;
    }
}
