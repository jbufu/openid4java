/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.openid4java.discovery.html.CyberNekoDOMHtmlParserTest;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sutra Zhou
 * 
 */
public class CyberNekoDOMYadisHtmlParserTest extends TestCase
{
    private CyberNekoDOMYadisHtmlParser parser;

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        parser = new CyberNekoDOMYadisHtmlParser();
    }

    /**
     * Test method for
     * {@link org.openid4java.discovery.yadis.CyberNekoDOMYadisHtmlParser#getHtmlMeta(java.lang.String)}
     * .
     * 
     * @throws IOException
     * @throws YadisException
     */
    public final void testGetHtmlMetaIssue83() throws IOException, YadisException
    {
        String htmlData = getResourceAsString("issue83.html");
        String s = parser.getHtmlMeta(htmlData);
        assertEquals("http://edevil.livejournal.com/data/yadis", s);
    }

    public void testParseHtmlMetaXXE() throws Exception {
        parser.getHtmlMeta(IOUtils.toString(CyberNekoDOMHtmlParserTest.class.getResourceAsStream(
                "identityPageWithExternalEntityReference.html")));
        // don't fail trying to read "/path/to/some/file" from the input data
    }

    /**
     * Read the resource as string.
     * 
     * @param name
     *            the resource name
     * @return a string
     * @throws IOException
     *             if an I/O error occurs
     */
    private String getResourceAsString(String name) throws IOException
    {
        InputStream inputStream = CyberNekoDOMYadisHtmlParserTest.class.getResourceAsStream(name);
        try
        {
            return IOUtils.toString(inputStream);
        } finally
        {
            inputStream.close();
        }
    }
}
