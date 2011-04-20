/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

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
