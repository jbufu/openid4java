/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.server;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.text.ParseException;

import org.openid4java.util.InternetDateFormat;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public abstract class AbstractNonceGeneratorTest extends TestCase
{
    protected InternetDateFormat _dateFormat = new InternetDateFormat();
    protected NonceGenerator _nonceGenerator;

    public AbstractNonceGeneratorTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        _nonceGenerator = createGenerator();
    }

    public abstract NonceGenerator createGenerator();

    public void testUniqueLoop()
    {
        Set seen = new HashSet();

        for (int i = 0; i < 100; i++)
        {
            String nonce = _nonceGenerator.next();

            if (seen.contains(nonce))
                fail("Double nonce!");

            seen.add(nonce);
        }
    }

    public void testUniqueSequential()
    {
        String nonce1 = _nonceGenerator.next();
        String nonce2 = _nonceGenerator.next();
        String nonce3 = _nonceGenerator.next();

        assertFalse(nonce1.equals(nonce2));
        assertFalse(nonce2.equals(nonce3));
    }

    public void testTimestamp() throws ParseException
    {
        String nonce = _nonceGenerator.next();

        Date nonceDate = _dateFormat.parse(nonce);

        assertNotNull(nonceDate);
        assertTrue(nonceDate.before(new Date()));
    }

    public static Test suite()
    {
        return new TestSuite(AbstractNonceGeneratorTest.class);
    }
}
