/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryNonceVerifierTest extends AbstractNonceVerifierTest
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
