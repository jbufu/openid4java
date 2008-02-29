/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openid4java.OpenIDException;
import org.openid4java.util.HttpRequestOptions;
import org.openid4java.util.HttpCache;

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

/*
    public void printResult(YadisResult result)
    {
        System.out.println("Yadis Status: " + result.isSuccess() +
                " (" + result.getStatusMessage() + ")");
        System.out.println("YadisURL: " + result.getYadisUrl().getUrl());
        System.out.println("XRDS-Location: " + result.getXrdsLocation());
        System.out.println("Content-type: " + result.getContentType());
        System.out.println("XRDS:\n" + result.getXrds());
    }
*/

    public static Test suite()
    {
        return new TestSuite(YadisResolverTest.class);
    }


    // --------------------- positive tests ------------------------------------

    public void testHeadersUrl() throws YadisException
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simpleheaders");

        assertTrue(result.isSuccess());
    }

    public void testHtmlUrl() throws YadisException
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=simplehtml");

        assertTrue(result.isSuccess());
    }

    public void testRedirectToHeaderResponse() throws YadisException
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=redir_simpleheaders");

        assertTrue(result.isSuccess());
    }

    public void testRedirectToHtmlResponse() throws YadisException
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=redir_simplehtml");

        assertTrue(result.isSuccess());
    }

    public void testRedirectToXrdsResponse() throws YadisException
    {
        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=redir_simplexrds");

        assertTrue(result.isSuccess());
    }


    public void testIncompleteHtmlParsing() throws YadisException
    {
        // stop reading from the received HTML body shortly after the Yadis tag
        HttpCache cache = new HttpCache();
        HttpRequestOptions requestOptions = cache.getRequestOptions();
        requestOptions.setMaxBodySize(350);
        cache.setDefaultRequestOptions(requestOptions);

        YadisResult result = _resolver.discover("http://localhost:" +
                _servletPort + "/?html=simplehtml", cache);

        assertTrue(result.isSuccess());
    }

    // -------------------- error handling tests -------------------------------

    public void testInvalidUrl()
    {
        try
        {
            _resolver.discover("bla.com");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_INVALID_URL);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                    OpenIDException.YADIS_INVALID_URL, expected.getErrorCode());
        }

    }

    public void testHeadTransportError() throws Exception
    {
        _server.stop();

        try
        {
            _resolver.discover("http://localhost:" + _servletPort +
                               "/?servertopped");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_HEAD_TRANSPORT_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                    OpenIDException.YADIS_HEAD_TRANSPORT_ERROR, expected.getErrorCode());
        }
    }

    //public void testMultipleXrdsLocationInHeaders()
    //{
    //    YadisResult result = _resolver.discover("http://localhost:" +
    //            _servletPort + "/?headers=multiplexrdslocation");
    //
    //    assertEquals(result.getStatusMessage(),
    //            OpenIDException.YADIS_HEAD_INVALID_RESPONSE, result.isSuccess());
    //
    //    // todo: jetty's HttpResponse.addHeader() doesn't actually set...
    //    assertEquals("should fail with multiple headers error",
    //            "Found more than one", result.getStatusMessage().substring(0,19));
    //}

    public  void testInvalidXrdsLocationInHeaders()
    {
        try
        {
            _resolver.discover("http://localhost:" + _servletPort +
                               "/?headers=invalidxrdslocation1");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_HEAD_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                    OpenIDException.YADIS_HEAD_INVALID_RESPONSE, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" + _servletPort +
                               "/?headers=invalidxrdslocation2");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_HEAD_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HEAD_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public  void testInvalidXrdsLocationInGetHeaders()
    {
        try
        {
            _resolver.discover("http://localhost:" + _servletPort +
                "/?headers=simplehtml&getheaders=invalidxrdslocation1");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_GET_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                    OpenIDException.YADIS_GET_INVALID_RESPONSE, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" + _servletPort +
                "/?headers=simplehtml&getheaders=invalidxrdslocation2");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_GET_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_GET_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testMultipleXrdsLocationInHtml()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=multiplexrdslocation");

            fail("Should have failed with error code " +
                 OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testHtmlHeadElements()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=nohead");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=twoheads");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testEmptyHtml()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=empty");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_HTMLMETA_DOWNLOAD_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testGetError() throws Exception
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=nonexistantfile");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_GET_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_GET_ERROR, expected.getErrorCode());
        }
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
    //    assertEquals(expected.getMessage(),
    //            OpenIDException.YADIS_GET_TRANSPORT_ERROR, expected.getErrorCode());
    //}


    public void testXrdsSizeExceeded()
    {
        HttpRequestOptions requestOptions = new HttpRequestOptions();
        requestOptions.setMaxBodySize(10);

        HttpCache cache = new HttpCache();
        cache.setDefaultRequestOptions(requestOptions);

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simpleheaders", cache);

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_SIZE_EXCEEDED);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_XRDS_SIZE_EXCEEDED, expected.getErrorCode());
        }
    }

    public void testMalformedXML()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds1");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_PARSING_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_XRDS_PARSING_ERROR, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds2");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_PARSING_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_XRDS_PARSING_ERROR, expected.getErrorCode());
        }
    }

    public void testMalformedXRDSServiceURI()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds3");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_PARSING_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_XRDS_PARSING_ERROR, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds4");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_PARSING_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_XRDS_PARSING_ERROR, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds5");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_PARSING_ERROR);
        }
        catch (YadisException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_XRDS_PARSING_ERROR, expected.getErrorCode());
        }

    }
}
