/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpFetcher;
import org.openid4java.util.HttpFetcherFactory;
import org.openid4java.util.HttpRequestOptions;

import java.util.Collections;
import java.util.List;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisResolverTest extends TestCase
{
    private int _servletPort;

    private YadisResolver _resolver;

    public static Server _server;

    static
    {
        System.getProperties().put("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "trace");
    }

    public YadisResolverTest(final String testName) throws Exception
    {
        super(testName);

        _servletPort = Integer.parseInt(System.getProperty("SERVLET_PORT"));
    }

    public void setUp() throws Exception
    {

        _resolver = new YadisResolver(new HttpFetcherFactory());

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

    public void testHeadersUrl() throws DiscoveryException
    {
        YadisResult result = _resolver.discover("http://localhost:" + _servletPort + "/?headers=simpleheaders",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
    }

    public void testHeadersUrlToXmlContentTypeDocument() throws DiscoveryException
    {
        YadisResult result = _resolver.discover("http://localhost:" + _servletPort + "/?headers=simpleheaders_xml",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
    }

    public void testHtmlUrl() throws DiscoveryException
    {
        YadisResult result = _resolver.discover("http://localhost:" + _servletPort + "/?html=simplehtml",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
    }

    public void testRedirectToHeaderResponse() throws DiscoveryException
    {
        YadisResult result = _resolver.discover("http://localhost:" + _servletPort + "/?headers=redir_simpleheaders",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
    }

    public void testRedirectToHtmlResponse() throws DiscoveryException
    {
        YadisResult result = _resolver.discover("http://localhost:" + _servletPort + "/?headers=redir_simplehtml",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
    }

    public void testRedirectToXrdsResponse() throws DiscoveryException
    {
        YadisResult result = _resolver.discover("http://localhost:" + _servletPort + "/?headers=redir_simplexrds",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
    }


    public void testIncompleteHtmlParsing() throws DiscoveryException
    {
        // stop reading from the received HTML body shortly after the Yadis tag
        HttpFetcher cache = new HttpCache();
        HttpRequestOptions requestOptions = cache.getRequestOptions();
        requestOptions.setMaxBodySize(350);
        cache.setDefaultRequestOptions(requestOptions);

        YadisResolver resolver = new YadisResolver(cache);
        YadisResult result = resolver.discover("http://localhost:" + _servletPort + "/?html=simplehtml",
            10, Collections.singleton("http://example.com/"));

        assertTrue(result.getEndpoints().size() > 0);
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
        catch (DiscoveryException expected)
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
        catch (DiscoveryException expected)
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
        catch (DiscoveryException expected)
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
        catch (DiscoveryException expected)
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
        catch (DiscoveryException expected)
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
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testHtmlHeadElementsNoHead()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=nohead");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testHtmlHeadElementsTwoHeads() {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=twoheads");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE, expected.getErrorCode());
        }
    }

    public void testHtmlHeadElementsExtraHeadInBody() {
        try
        {
            YadisResult result = _resolver.discover("http://localhost:" +_servletPort + "/?html=extraheadinbody",
                10, Collections.singleton("http://example.com/"));

            assertTrue("Discovery should have ignored a html/body/head; " +
                       " we only care about spurious html/head's", result.getEndpoints().size() == 1);

        }
        catch (DiscoveryException e)
        {
            fail("Discovery should have ignored a html/body/head; " +
                       " we only care about spurious html/head's");
        }
    }

    public void testHtmlHeadNoMeta() throws DiscoveryException
    {
        List result = _resolver.discover("http://localhost:" + _servletPort + "/?html=headnometa");

        assertEquals("Should have discovered no endpoints; found: " + result.size(), result.size(), 0);
    }

    public void testEmptyHtml()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?html=empty");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_HTMLMETA_INVALID_RESPONSE);
        }
        catch (DiscoveryException expected)
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

        HttpFetcher cache = new HttpCache();
        cache.setDefaultRequestOptions(requestOptions);

        YadisResolver resolver = new YadisResolver(cache);

        try
        {
            resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simpleheaders");

            fail("Should have failed with error code " +
                OpenIDException.YADIS_XRDS_SIZE_EXCEEDED);
        }
        catch (DiscoveryException expected)
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
                OpenIDException.XRDS_PARSING_ERROR);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.XRDS_PARSING_ERROR, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds2");

            fail("Should have failed with error code " +
                OpenIDException.XRDS_PARSING_ERROR);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.XRDS_PARSING_ERROR, expected.getErrorCode());
        }
    }

    public void testMalformedXRDSServiceURI()
    {
        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds3");

            fail("Should have failed with error code " +
                OpenIDException.XRDS_PARSING_ERROR);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.XRDS_PARSING_ERROR, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds4");

            fail("Should have failed with error code " +
                OpenIDException.XRDS_PARSING_ERROR);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.XRDS_PARSING_ERROR, expected.getErrorCode());
        }

        try
        {
            _resolver.discover("http://localhost:" +
                _servletPort + "/?headers=simplexrds&xrds=malformedxrds5");

            // "bla bla" matches xrd.xsd's URIPriorityAppendPattern type, so this one won't be a parse error; hmm...
            fail("Should have failed with error code " +
                OpenIDException.YADIS_INVALID_URL);
        }
        catch (DiscoveryException expected)
        {
            assertEquals(expected.getMessage(),
                OpenIDException.YADIS_INVALID_URL, expected.getErrorCode());
        }
    }

    public void testXrdsOpenidDelegate() throws Exception
    {
        List result;
        try
        {
            result = _resolver.discover("http://localhost:" + _servletPort + "/?headers=simplexrds&xrds=xrdsdelegate");
            assertEquals("Should have discovered one endpoint: ", result.size(), 1);
            DiscoveryInformation info = (DiscoveryInformation) result.get(0);
            assertNotNull("Should have discovered an openid:Delegate.", info.getDelegateIdentifier());
        }
        catch (DiscoveryException e)
        {
            fail("Discovery failed on xrdsdelegate: " + e.getMessage());
        }
    }

    public void testEmptyUri() throws Exception
    {
        // empty string is a valid java.net.URI...

        YadisResult yadis = _resolver.discover("http://localhost:" + _servletPort + "/?headers=simplexrds&xrds=malformedxrds6",
            10, Collections.singleton("http://example.com/"));

        assertTrue("XRDS with an empty URI is valid; Yadis should have succeeded",
                   yadis.getEndpoints().size() > 0);

        // also run through Discovery.extractDiscoveryInformation()
        ConsumerManager manager = new ConsumerManager();

        List results = manager.discover("http://localhost:" +
            _servletPort + "/?headers=simplexrds&xrds=malformedxrds6");

        assertEquals("No discovery information should have been returned for an empty URI",
                     0, results.size());

    }

    public void testParseXmlXXE() throws Exception
    {
        try {
            YadisResult yadis = _resolver.discover("http://localhost:" + _servletPort + "/?headers=simplexrds&xrds=xrdsxxe",
                    10, Collections.singleton("http://example.com/"));
            fail("Discovery on XRDS data containing external entity references should have failed.");
        } catch (DiscoveryException expected) {
            assertEquals("0x500: External entity found in XRDS data", expected.getMessage());
        }
    }
}
