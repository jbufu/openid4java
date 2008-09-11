/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.html.HtmlResult;

/**
 * @author Sutra Zhou
 * 
 */
public class CyberNekoDOMHtmlParserTest extends TestCase
{
    private CyberNekoDOMHtmlParser parser;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        parser = new CyberNekoDOMHtmlParser();
    }

    /**
     * Test method for
     * {@link org.openid4java.util.CyberNekoDOMHtmlParser#parseHtml(java.lang.String, org.openid4java.discovery.html.HtmlResult)}
     * .
     * 
     * @throws IOException
     * @throws DiscoveryException
     */
    public void testParseHtml() throws IOException, DiscoveryException
    {
        String htmlData = IOUtils.toString(this.getClass().getResourceAsStream(
                "identityPage.html"));
        HtmlResult result = new HtmlResult();
        parser.parseHtml(htmlData, result);
        assertEquals("http://www.example.com:8080/openidserver/users/myusername", result
                .getDelegate1());
        System.out.println(result.getOP1Endpoint());
        assertEquals("http://www.example.com:8080/openidserver/openid.server", result
                .getOP1Endpoint().toExternalForm());
    }

    /**
     * Test method for
     * {@link org.openid4java.util.CyberNekoDOMHtmlParser#parseHtml(java.lang.String, org.openid4java.discovery.html.HtmlResult)}
     * .
     * 
     * @throws IOException
     * @throws DiscoveryException
     */
    public void testParseHtmlWithXmlNamespace() throws IOException,
            DiscoveryException
    {
        String htmlData = IOUtils.toString(this.getClass().getResourceAsStream(
                "identityPage-with-xml-namespace.html"));
        HtmlResult result = new HtmlResult();
        parser.parseHtml(htmlData, result);
        assertEquals("http://www.example.com:8080/openidserver/users/myusername", result
                .getDelegate1());
        assertEquals("http://www.example.com:8080/openidserver/openid.server", result
                .getOP1Endpoint().toExternalForm());
    }

}
