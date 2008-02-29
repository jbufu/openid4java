/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisUrlTest extends TestCase
{
    public YadisUrlTest(String name)
    {
        super(name);
    }

    // test the string constructor
    public void testUrl() throws YadisException
    {
        assertNotNull(new YadisUrl("http://example.com") );
        assertNotNull(new YadisUrl("HTTP://EXAMPLE.COM"));
        assertNotNull(new YadisUrl("http://example.com/a/b?q=1#end"));
        assertNotNull(new YadisUrl("https://example.com"));
        assertNotNull(new YadisUrl("HTTPS://EXAMPLE.COM"));
        assertNotNull(new YadisUrl("https://example.com/a/b?q=1#end"));
        assertNotNull(new YadisUrl("HttpS://Example.Com"));
    }


    public void testUrlNoProtocol() throws YadisException
    {
        try
        {
            new YadisUrl("example.com");
            fail("A YadisException should be raised " +
                    "if the protocol was not specified");
        } catch (YadisException expected) {
            assertTrue(true);
        }
        try
        {
            new YadisUrl("example.com/a/b?q=1#end");
            fail("A YadisException should be raised " +
                    "if the protocol was not specified");
        } catch (YadisException expected) {
            assertTrue(true);
        }
    }

    public void testUrlProtocol() throws YadisException
    {
        try
        {
            new YadisUrl("ftp://example.com");
            new YadisUrl("nntp://example.com");
            new YadisUrl("file:///tmp/somefile");
            new YadisUrl("smth://example.com/a/b?q=1#end");
            fail("A YadisException should be raised " +
                    "if the protocol is not HTTP or HTTPS");
        } catch (YadisException expected) {
            assertTrue(true);
        }
    }

    public static Test suite()
    {
        return new TestSuite(YadisUrlTest.class);
    }
}
