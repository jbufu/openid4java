/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Date;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class EhcacheNonceVerifier extends AbstractNonceVerifier
{
    private Cache _cache;

    public EhcacheNonceVerifier(int maxAge)
    {
        super(maxAge);
    }

    public void setCache(Cache cache)
    {
        if (cache.getTimeToLiveSeconds() != _maxAge)
        {
            throw new IllegalArgumentException("Max Age: " + _maxAge + ", same expected for cache, but found: " + cache.getTimeToLiveSeconds());
        }

        if (cache.getTimeToLiveSeconds() != cache.getTimeToIdleSeconds())
        {
            throw new IllegalArgumentException("Cache must have same timeToLive (" + cache.getTimeToLiveSeconds() + ") as timeToIdle (" + cache.getTimeToIdleSeconds() + ")");
        }

        _cache = cache;
    }

    protected int seen(Date now, String idpUrl, String nonce)
    {
        String pair = idpUrl + '#' + nonce;
        Element element = new Element(pair, pair);

        if (_cache.get(pair) != null)
            return SEEN;

        _cache.put(element);

        return OK;
    }
}
