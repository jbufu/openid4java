/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import net.openid.util.InternetDateFormat;
import net.openid.server.NonceGenerator;
import net.openid.server.IncrementalNonceGenerator;

import java.util.Date;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public abstract class NonceVerifierTest extends TestCase
{
    protected NonceVerifier _nonceVerifier;
    protected InternetDateFormat _dateFormat = new InternetDateFormat();
    public static final int MAX_AGE = 60;

    public NonceVerifierTest(String name)
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

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("idp1", nonce));
        assertEquals(NonceVerifier.SEEN, _nonceVerifier.seen("idp1", nonce));

        assertEquals(NonceVerifier.OK, _nonceVerifier.seen("idp2", nonce));
    }

    public void testMalformed()
    {
        assertEquals(NonceVerifier.INVALID_TIMESTAMP, _nonceVerifier.seen("idp1", "xyz"));
    }

    public void testExpired()
    {
        Date now = new Date();
        Date past = new Date(now.getTime() - (MAX_AGE + 1) * 1000);

        String nonce = _dateFormat.format(past) + "abc";

        assertEquals(NonceVerifier.TOO_OLD, _nonceVerifier.seen("idp1", nonce));
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
        return new TestSuite(NonceVerifierTest.class);
    }
}
