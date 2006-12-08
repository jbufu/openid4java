/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class EhcacheNonceVerifierTest extends NonceVerifierTest
{
    private CacheManager _cacheManager;

    public EhcacheNonceVerifierTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        _cacheManager = new CacheManager();

        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();

        _cacheManager = null;
    }

    public NonceVerifier createVerifier(int maxAge)
    {
        _cacheManager.removalAll();
        _cacheManager.addCache(new Cache("testCache", 100, false, false, maxAge, maxAge));

        EhcacheNonceVerifier nonceVerifier = new EhcacheNonceVerifier(maxAge);
        nonceVerifier.setCache(_cacheManager.getCache("testCache"));

        return nonceVerifier;
    }

    public static Test suite()
    {
        return new TestSuite(EhcacheNonceVerifierTest.class);
    }
}
