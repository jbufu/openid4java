/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.html;

import org.openid4java.discovery.DiscoveryException;

/**
 * Html parser.
 * 
 * @author Sutra Zhou
 * @since 0.9.4
 * @see #parseHtml(String, HtmlResult)
 */
public interface HtmlParser
{
    /**
     * Parses the HTML data and stores in the result the discovered openid
     * information.
     * 
     * @param htmlData
     *            HTML data obtained from the URL identifier.
     * @param result
     *            The HTML result.
     * @throws DiscoveryException
     */
    void parseHtml(String htmlData, HtmlResult result) throws DiscoveryException;
}
