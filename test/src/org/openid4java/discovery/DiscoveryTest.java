/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.servlet.ServletException;

import org.w3c.dom.Document;
import org.openxri.xml.XRDS;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.List;

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

    private XRDS createXrds(String xmlFileName) throws Exception
    {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        documentBuilderFactory.setNamespaceAware(true);
        assertTrue(documentBuilderFactory.isNamespaceAware());

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        assertTrue(documentBuilder.isNamespaceAware());


        InputStream inputStream = new BufferedInputStream(
                new FileInputStream(_testDataPath + "/discovery/" + xmlFileName));

        Document document = documentBuilder.parse(inputStream);

        return new XRDS(document.getDocumentElement(), false);
    }

    //todo: tests for multiple discovered services / priorities
    //todo: XRI path+query / service selection
    //http://openid.net/pipermail/general/2006-October/000512.html

    public void testExtractDiscoveryInformationDelegate() throws Exception
    {
        XRDS xrds = createXrds("exampleDelegate.xml");
        Identifier identifier = new UrlIdentifier("http://user.example.com");

        List services = Discovery.extractDiscoveryInformation(xrds, identifier);
        DiscoveryInformation discoveryInformation =
                (DiscoveryInformation) services.iterator().next();

        assertTrue(discoveryInformation.hasClaimedIdentifier());
        assertTrue(discoveryInformation.hasDelegateIdentifier());

        assertEquals("http://op.example.com", discoveryInformation.getOPEndpoint().toExternalForm());
        assertEquals("http://user.example.com/", discoveryInformation.getClaimedIdentifier().getIdentifier());
        assertEquals("http://op.example.com/user", discoveryInformation.getDelegateIdentifier());
    }

    public void testExtractDiscoveryInformationDelegate1() throws Exception
    {
        XRDS xrds = createXrds("exampleDelegate1.xml");
        Identifier identifier = new UrlIdentifier("http://user.example.com");

        List services = Discovery.extractDiscoveryInformation(xrds, identifier);
        DiscoveryInformation discoveryInformation =
                (DiscoveryInformation) services.iterator().next();

        assertTrue(discoveryInformation.hasClaimedIdentifier());
        assertTrue(discoveryInformation.hasDelegateIdentifier());

        assertEquals("http://op.example.com", discoveryInformation.getOPEndpoint().toExternalForm());
        assertEquals("http://user.example.com/", discoveryInformation.getClaimedIdentifier().getIdentifier());
        assertEquals("http://op.example.com/user", discoveryInformation.getDelegateIdentifier());
    }

    public void testExtractDiscoveryInformationClaimed() throws Exception
    {
        XRDS xrds = createXrds("exampleClaimed.xml");
        Identifier identifier = new UrlIdentifier("http://user.example.com");

        List services = Discovery.extractDiscoveryInformation(xrds, identifier);
        DiscoveryInformation discoveryInformation =
                (DiscoveryInformation) services.iterator().next();

        assertTrue(discoveryInformation.hasClaimedIdentifier());
        assertFalse(discoveryInformation.hasDelegateIdentifier());

        assertEquals("http://op.example.com", discoveryInformation.getOPEndpoint().toExternalForm());
        assertEquals("http://user.example.com/", discoveryInformation.getClaimedIdentifier().getIdentifier());
    }

    public void testExtractDiscoveryInformationOP() throws Exception
    {
        XRDS xrds = createXrds("exampleOP.xml");
        Identifier identifier = new UrlIdentifier("http://op.example.com");

        List services = Discovery.extractDiscoveryInformation(xrds, identifier);
        DiscoveryInformation discoveryInformation =
                (DiscoveryInformation) services.iterator().next();

        assertFalse(discoveryInformation.hasClaimedIdentifier());
        assertFalse(discoveryInformation.hasDelegateIdentifier());

        assertEquals("http://op.example.com", discoveryInformation.getOPEndpoint().toExternalForm());
    }

    public static Test suite()
    {
        return new TestSuite(DiscoveryTest.class);
    }
}
