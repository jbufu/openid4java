package org.openid4java.discovery.xrds;

import org.openid4java.discovery.DiscoveryException;

import java.util.List;
import java.util.Set;

/**
 * XRDS parser for OpenID.
 */
public interface XrdsParser
{
    /**
     * Parses a XRDS document and extracts the relevant information
     * for the specified service endpoint types.
     *
     * @param xrdsInput the XRDS document in String format
     *                  discovered from an Identifier.
     * @param targetTypes Set of service endpoint types
     *                    that should be matched
     * @return a List of {@link XrdsServiceEndpoint}s
     *         extracted from the XRDS document,
     *         in the proper, sorted order
     */
    public List parseXrds(String input, Set targetTypes) throws DiscoveryException;
    
}
