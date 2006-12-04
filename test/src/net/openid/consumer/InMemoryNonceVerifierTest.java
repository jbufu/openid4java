/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;

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

    public static Test suite()
    {
        return new TestSuite(InMemoryNonceVerifierTest.class);
    }
}
