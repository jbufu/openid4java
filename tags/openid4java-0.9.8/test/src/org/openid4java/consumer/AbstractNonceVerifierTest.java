/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.openid4java.util.InternetDateFormat;
import org.openid4java.server.NonceGenerator;
import org.openid4java.server.IncrementalNonceGenerator;

import java.util.Date;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public abstract class AbstractNonceVerifierTest extends TestCase
{
    protected NonceVerifier _nonceVerifier;
    protected InternetDateFormat _dateFormat = new InternetDateFormat();
    public static final int MAX_AGE = 60;

    public AbstractNonceVerifierTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        _nonceVerifier = createVerifier(MAX_AGE);
    }

    public abstract NonceVerifier createVerifier(int maxAge);

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSeen()
    {
        String nonce = _dateFormat.format(new Date()) + "abc";

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("op1", nonce));
        assertEquals(NonceVerifier.SEEN, _nonceVerifier.seen("op1", nonce));

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("op2", nonce));
    }

    public void testMalformed()
    {
        assertEquals(NonceVerifier.INVALID_TIMESTAMP, _nonceVerifier.seen("op1", "xyz"));
    }

    public void testExpired()
    {
        Date now = new Date();
        Date past = new Date(now.getTime() - 1000L * (MAX_AGE + 1));

        String nonce = _dateFormat.format(past) + "abc";

        assertEquals(NonceVerifier.TOO_OLD, _nonceVerifier.seen("op1", nonce));
    }

    public void testNonceCleanup() throws Exception
    {
        NonceGenerator nonceGenerator = new IncrementalNonceGenerator();
        _nonceVerifier = createVerifier(1);

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.com", nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.com", nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.com", nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.com", nonceGenerator.next()));

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.net", nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.net", nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.net", nonceGenerator.next()));
        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.net", nonceGenerator.next()));

        Thread.sleep(1000);

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("http://example.org", nonceGenerator.next()));
    }

    public static Test suite()
    {
        return new TestSuite(AbstractNonceVerifierTest.class);
    }
}
