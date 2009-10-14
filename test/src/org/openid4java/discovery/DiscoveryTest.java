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
    private Discovery _discovery;

    public DiscoveryTest(String name) throws ServletException
    {
        super(name);

        _testDataPath = System.getProperty("TEST_DATA");
        _discovery = new Discovery();
        if (_testDataPath == null)
            throw new ServletException("TEST_DATA path not initialized");
    }

    public void testParseUrl() throws DiscoveryException
    {
        assertTrue(_discovery.parseIdentifier("http://example.com") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("HTTP://EXAMPLE.COM") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("http://example.com/a/b?q=1#end") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("https://example.com") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("HTTPS://EXAMPLE.COM") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("https://example.com/a/b?q=1#end") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("HttpS://Example.Com") instanceof UrlIdentifier);
    }

    public void testParseUrlNoProtocol() throws DiscoveryException
    {
        assertTrue(_discovery.parseIdentifier("example.com") instanceof UrlIdentifier);
        assertTrue(_discovery.parseIdentifier("example.com/a/b?q=1#end") instanceof UrlIdentifier);

        UrlIdentifier identifier = (UrlIdentifier) _discovery.parseIdentifier("example.com");
        assertEquals("http", identifier.getUrl().getProtocol());
    }

    public void testParseXri() throws DiscoveryException
    {
        assertTrue(_discovery.parseIdentifier("xri://=example") instanceof XriIdentifier);
        assertTrue(_discovery.parseIdentifier("xri://example") instanceof UrlIdentifier);
    }

    public void testParseXriNoProtocol() throws DiscoveryException
    {
        assertTrue(_discovery.parseIdentifier("=example") instanceof XriIdentifier);
        assertTrue(_discovery.parseIdentifier("@example") instanceof XriIdentifier);
        assertTrue(_discovery.parseIdentifier("$example") instanceof XriIdentifier);
        assertTrue(_discovery.parseIdentifier("+example") instanceof XriIdentifier);
        assertTrue(_discovery.parseIdentifier("!!1234") instanceof XriIdentifier);
    }

    //todo: tests for multiple discovered services / priorities
    //todo: XRI path+query / service selection
    //http://openid.net/pipermail/general/2006-October/000512.html

    public static Test suite()
    {
        return new TestSuite(DiscoveryTest.class);
    }
}
