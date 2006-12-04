/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.openid.server.NonceGenerator;
import net.openid.server.IncrementalNonceGenerator;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryNonceVerifierTest extends NonceVerifierTest
{
    public InMemoryNonceVerifierTest(String name)
    {
        super(name);
    }

    public NonceVerifier createVerifier(int maxAge)
    {
        return new InMemoryNonceVerifier(maxAge);
    }

    public void testNonceCleanup() throws Exception
    {
        super.testNonceCleanup();

        InMemoryNonceVerifier inMemoryVerifier = (InMemoryNonceVerifier) _nonceVerifier;

        assertEquals(1, inMemoryVerifier.size());
    }

    public static Test suite()
    {
        return new TestSuite(InMemoryNonceVerifierTest.class);
    }
}
