package org.openid4java.discovery.xri;

import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.XriIdentifier;
import org.openid4java.discovery.Identifier;

import java.util.List;

public interface XriResolver
{
    /**
     * Performs OpenID discovery on the supplied XRI identifier.
     *
     * @param xri   The XRI identifier
     * @return      A list of DiscoveryInformation, ordered the discovered
     *              priority.
     * @throws DiscoveryException if discovery failed.
     */
    public List discover(XriIdentifier xri) throws DiscoveryException;

    XriIdentifier parseIdentifier(String identifier) throws DiscoveryException;
}
