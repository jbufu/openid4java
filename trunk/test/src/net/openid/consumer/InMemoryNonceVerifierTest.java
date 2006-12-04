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
    private NonceGenerator _nonceGenerator = new IncrementalNonceGenerator();

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
        NonceVerifier verifier = createVerifier(1);

        assertEquals(NonceVerifier.OK, verifier.seen("http://example.com", _nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, verifier.seen("http://example.com", _nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, verifier.seen("http://example.com", _nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, verifier.seen("http://example.com", _nonceGenerator.next()));

        assertEquals(NonceVerifier.OK, verifier.seen("http://example.net", _nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, verifier.seen("http://example.net", _nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, verifier.seen("http://example.net", _nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, verifier.seen("http://example.net", _nonceGenerator.next()));

        Thread.sleep(1000);

        assertEquals(NonceVerifier.OK, verifier.seen("http://example.org", _nonceGenerator.next()));
    }

    public static Test suite()
    {
        return new TestSuite(InMemoryNonceVerifierTest.class);
    }
}
