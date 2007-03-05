/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.discovery;

import java.net.URL;
import java.io.Serializable;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DiscoveryInformation implements Serializable
{
    URL _idpEndpoint;
    Identifier _claimedIdentifier;
    Identifier _delegateIdentifier;
    String _version;

    public final static String OPENID10 = "http://openid.net/signon/1.0";
    public final static String OPENID11 = "http://openid.net/signon/1.1";
    public final static String OPENID2 = "http://specs.openid.net/auth/2.0/signon";
    public final static String OPENID2_OP = "http://specs.openid.net/auth/2.0/server";


    public DiscoveryInformation(URL idpEndpoint) throws DiscoveryException
    {
        this(idpEndpoint, null, OPENID2_OP);
    }

    public DiscoveryInformation(URL idpEndpoint, Identifier claimedIdentifier)
            throws DiscoveryException
    {
        this(idpEndpoint, claimedIdentifier, OPENID2);
    }

    public DiscoveryInformation(URL idpEndpoint, Identifier claimedIdentifier,
                                String version)
            throws DiscoveryException
    {
        this(idpEndpoint, claimedIdentifier, null, version);
    }

    public DiscoveryInformation(URL idpEndpoint, Identifier claimedIdentifier,
                                Identifier delegateIdentifier)
            throws DiscoveryException
    {
        this(idpEndpoint, claimedIdentifier, delegateIdentifier, OPENID2);
    }

    public DiscoveryInformation(URL idpEndpoint, Identifier claimedIdentifier,
                                Identifier delegateIdentifier, String version)
            throws DiscoveryException
    {
        _idpEndpoint = idpEndpoint;
        _claimedIdentifier = claimedIdentifier;
        _version = version;
        _delegateIdentifier = delegateIdentifier;

        if (_idpEndpoint == null)
            throw new DiscoveryException("Null OpenID Provider endpoint.");

        if (_delegateIdentifier != null && _claimedIdentifier == null)
            throw new DiscoveryException("Claimed ID must be present " +
                    "if delegated ID is used.");
    }

    public boolean hasClaimedIdentifier()
    {
        return _claimedIdentifier != null;
    }

    public boolean hasDelegateIdentifier()
    {
        return _delegateIdentifier != null;
    }

    public URL getIdpEndpoint()
    {
        return _idpEndpoint;
    }

    public Identifier getClaimedIdentifier()
    {
        return _claimedIdentifier;
    }

    public Identifier getDelegateIdentifier()
    {
        return _delegateIdentifier;
    }

    public String getVersion()
    {
        return _version;
    }

    public void setVersion(String version)
    {
        this._version = version;
    }

    public boolean isVersion2()
    {
        return OPENID2.equals(_version) || OPENID2_OP.equals(_version);
    }

    public String toString()
    {
        return (isVersion2() ? "OpenID2" : "OpenID1") 
                + "\nOP-endpoint:" + _idpEndpoint
                + "\nClaimedID:" + _claimedIdentifier
                + "\nDelegate:" + _delegateIdentifier;
    }
}
