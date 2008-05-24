/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.html;

import org.openid4java.discovery.DiscoveryException;

/**
 * @author Sutra Zhou
 * 
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
