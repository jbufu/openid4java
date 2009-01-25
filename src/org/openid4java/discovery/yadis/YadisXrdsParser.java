package org.openid4java.discovery.yadis;

import java.util.List;
import java.util.Set;

/**
 * XRDS parser for OpenID.
 */
public interface YadisXrdsParser
{
    /**
     * Parses a XRDS document and extracts the relevant information
     * about the specified endpoint types.
     *
     * @param xrdsInput the XRDS document in String format
     *                  discovered from an Identifier.
     * @param targetTypes Set of service endpoint types
     *                    that should be matched
     * @return a List of {@link XrdsServiceEndpoint}s
     *         extracted from the XRDS document
     */
    public List parseXrds(String input, Set targetTypes) throws YadisException;
    
}
