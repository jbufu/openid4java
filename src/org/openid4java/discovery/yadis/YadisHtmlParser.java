/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

/**
 * Yadis parser.
 * 
 * @author Sutra Zhou
 * @since 0.9.4
 * @see #getHtmlMeta(String)
 */
public interface YadisHtmlParser
{
    /**
     * Parses the HTML input stream and scans for the Yadis XRDS location in the
     * HTML HEAD Meta tags.
     * 
     * @param input
     *            input data stream
     * @return String the XRDS location URL, or null if not found
     * @throws YadisException
     *             on parsing errors or Yadis protocal violations
     */
    String getHtmlMeta(String input) throws YadisException;
}
