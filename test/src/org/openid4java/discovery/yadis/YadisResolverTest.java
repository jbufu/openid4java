/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisResolverTest extends TestCase
{
    private YadisResolver _resolver;
    private int _servletPort;

    public static Server _server;

    public YadisResolverTest(final String testName) throws Exception
    {
        super(testName);

        _servletPort = Integer.parseInt(System.getProperty("SERVLET_PORT"));
    }

    public void setUp() throws Exception
    {
        _resolver = new YadisResolver();

        _server = new Server(_servletPort);

        Context context = new Context(_server, "/", Context.SESSIONS);
        context.addServlet(new ServletHolder(new YadisTestServlet()), "/*");

        _server.start();

    }

    protected void tearDown() throws Exception
    {
        _server.stop();
    }

    public void printResult(YadisResult result)
    {
        System.out.println("Yadis Status: " + result.getStatus() +
                " (" + result.getStatusMessage() + ")");
        System.out.println("YadisURL: " + result.getYadisUrl().getUrl());
        System.out.println("XRDS-Location: " + result.getXrdsLocation());
        System.out.println("Content-type: " + result.getContentType());
        System.out.println("XRDS:\n" + result.getXrds());
    }

    public static Test suite()
    {
        return new TestSuite(YadisResolverTest.class);
    }


    // --------------------- positive tests ------------------------------------

    public void testHeadersUrl()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simpleheaders");

        assertEquals(result.getStatusMessage(),
                YadisResult.OK, result.getStatus());
    }

    public void testHtmlUrl()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=simplehtml");

        assertEquals(result.getStatusMessage(),
                YadisResult.OK, result.getStatus());
    }

    public void testRedirectToHeaderResponse()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=redir_simpleheaders");

        assertEquals(result.getStatusMessage(),
                YadisResult.OK, result.getStatus());
    }

    public void testRedirectToHtmlResponse()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=redir_simplehtml");

        assertEquals(result.getStatusMessage(),
                YadisResult.OK, result.getStatus());
    }

    public void testRedirectToXrdsResponse()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=redir_simplexrds");

        assertEquals(result.getStatusMessage(),
                YadisResult.OK, result.getStatus());
    }


    public void testIncompleteHtmlParsing()
    {
        // stop reading from the received HTML body shortly after the Yadis tag
        _resolver.setMaxHtmlSize(146);

        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=simplehtml");

        assertEquals(result.getStatusMessage(),
                YadisResult.OK, result.getStatus());
    }

    // -------------------- error handling tests -------------------------------

    public void testInvalidUrl()
    {
        YadisResult result = _resolver.discover("bla.com");

        assertEquals(result.getStatusMessage(),
                YadisResult.INVALID_URL, result.getStatus());
    }

    public void testHeadTransportError() throws Exception
    {
        _server.stop();

        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?servertopped");

        assertEquals(result.getStatusMessage(),
                YadisResult.HEAD_TRANSPORT_ERROR, result.getStatus());
    }

    //public void testMultipleXrdsLocationInHeaders()
    //{
    //    YadisResult result = _resolver.discover("http://localhost:" +
    //            _servletPort + "/?headers=multiplexrdslocation");
    //
    //    assertEquals(result.getStatusMessage(),
    //            YadisResult.HEAD_INVALID_RESPONSE, result.getStatus());
    //
    //    // todo: jetty's HttpResponse.addHeader() doesn't actually set...
    //    assertEquals("should fail with multiple headers error",
    //            "Found more than one", result.getStatusMessage().substring(0,19));
    //}

    public  void testInvalidXrdsLocationInHeaders()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=invalidxrdslocation1");

        assertEquals(result.getStatusMessage(),
                YadisResult.HEAD_INVALID_RESPONSE, result.getStatus());

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=invalidxrdslocation2");

        assertEquals(result.getStatusMessage(),
                YadisResult.HEAD_INVALID_RESPONSE, result.getStatus());
    }

    public  void testInvalidXrdsLocationInGetHeaders()
    {
        YadisResult result;

        result = _resolver.discover("http://localhost:" + _servletPort +
                "/?headers=simplehtml&getheaders=invalidxrdslocation1");

        assertEquals(result.getStatusMessage(),
                YadisResult.GET_INVALID_RESPONSE, result.getStatus());

        result = _resolver.discover("http://localhost:" + _servletPort +
                "/?headers=simplehtml&getheaders=invalidxrdslocation2");

        assertEquals(result.getStatusMessage(),
                YadisResult.GET_INVALID_RESPONSE, result.getStatus());
    }

    public void testMultipleXrdsLocationInHtml()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=multiplexrdslocation");

        assertEquals(result.getStatusMessage(),
                YadisResult.HTMLMETA_INVALID_RESPONSE, result.getStatus());
    }

    public void testHtmlHeadElements()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=nohead");

        assertEquals(result.getStatusMessage(),
                YadisResult.HTMLMETA_INVALID_RESPONSE, result.getStatus());

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=twoheads");

        assertEquals(result.getStatusMessage(),
                YadisResult.HTMLMETA_INVALID_RESPONSE, result.getStatus());
    }

    public void testEmptyHtml()
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=empty");

        assertEquals(result.getStatusMessage(),
                YadisResult.HTMLMETA_DOWNLOAD_ERROR, result.getStatus());
    }

    public void testGetError() throws Exception
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=nonexistantfile");

        assertEquals(result.getStatusMessage(),
                YadisResult.GET_ERROR, result.getStatus());
    }


    // should make the server fail for the HTTP GET
    // but not for the HEAD that is tried first
    //public void testGetTransportError() throws Exception
    //{
    //    //_server.stop();
    //
    //    YadisResult result = _resolver.discover("http://localhost:" +
    //            _servletPort + "/?headers=simplehtml&html=failonget");
    //
    //    assertEquals(result.getStatusMessage(),
    //            YadisResult.GET_TRANSPORT_ERROR, result.getStatus());
    //}


    public void testXrdsSizeExceeded()
    {
        _resolver.setMaxXmlSize(10);

        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simpleheaders");

        assertEquals(result.getStatusMessage(),
                YadisResult.XRDS_SIZE_EXCEEDED, result.getStatus());

    }

    public void testMalformedXML()
    {
        YadisResult result;

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds1");

        assertEquals(result.getStatusMessage(),
                YadisResult.XRDS_PARSING_ERROR, result.getStatus());

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds2");

        assertEquals(result.getStatusMessage(),
                YadisResult.XRDS_PARSING_ERROR, result.getStatus());
    }

    public void testMalformedXRDSServiceURI()
    {
        YadisResult result;

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds3");

        assertEquals(result.getStatusMessage(),
                YadisResult.XRDS_PARSING_ERROR, result.getStatus());

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds4");

        assertEquals(result.getStatusMessage(),
                YadisResult.XRDS_PARSING_ERROR, result.getStatus());

        result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds5");

        assertEquals(result.getStatusMessage(),
                YadisResult.XRDS_PARSING_ERROR, result.getStatus());

    }
}
