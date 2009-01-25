/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.servlet.ServletException;


/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DiscoveryTest extends TestCase
{
    private String _testDataPath;

    public DiscoveryTest(String name) throws ServletException
    {
        super(name);

        _testDataPath = System.getProperty("TEST_DATA");

        if (_testDataPath == null)
            throw new ServletException("TEST_DATA path not initialized");
    }

    public void testParseUrl() throws DiscoveryException
    {
        assertTrue(Discovery.parseIdentifier("http://example.com") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("HTTP://EXAMPLE.COM") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("http://example.com/a/b?q=1#end") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("https://example.com") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("HTTPS://EXAMPLE.COM") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("https://example.com/a/b?q=1#end") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("HttpS://Example.Com") instanceof UrlIdentifier);
    }

    public void testParseUrlNoProtocol() throws DiscoveryException
    {
        assertTrue(Discovery.parseIdentifier("example.com") instanceof UrlIdentifier);
        assertTrue(Discovery.parseIdentifier("example.com/a/b?q=1#end") instanceof UrlIdentifier);

        UrlIdentifier identifier = (UrlIdentifier) Discovery.parseIdentifier("example.com");
        assertEquals("http", identifier.getUrl().getProtocol());
    }

    public void testParseXri() throws DiscoveryException
    {
        assertTrue(Discovery.parseIdentifier("xri://=example") instanceof XriIdentifier);
        assertTrue(Discovery.parseIdentifier("xri://example") instanceof UrlIdentifier);
    }

    public void testParseXriNoProtocol() throws DiscoveryException
    {
        assertTrue(Discovery.parseIdentifier("=example") instanceof XriIdentifier);
        assertTrue(Discovery.parseIdentifier("@example") instanceof XriIdentifier);
        assertTrue(Discovery.parseIdentifier("$example") instanceof XriIdentifier);
        assertTrue(Discovery.parseIdentifier("+example") instanceof XriIdentifier);
        assertTrue(Discovery.parseIdentifier("!!1234") instanceof XriIdentifier);
    }

    //todo: tests for multiple discovered services / priorities
    //todo: XRI path+query / service selection
    //http://openid.net/pipermail/general/2006-October/000512.html

    public static Test suite()
    {
        return new TestSuite(DiscoveryTest.class);
    }
}
