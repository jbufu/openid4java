/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import java.net.URL;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class DiscoveryInformation implements Serializable
{
    /**
     * The OP endpoint URL.
     */
    URL _opEndpoint;

    /**
     * The claimed identifier, i.e. the user's identity key.
     */
    Identifier _claimedIdentifier;

    /**
     * The delegate, or OP-Local identifier.
     * The key through which the OP remembers the user's account.
     */
    String _delegate;

    /**
     * The OpenID protocol version, or target service type discovered through Yadis.
     */
    String _version;

    /**
     * All service types discovered for the endpoint.
     */
    Set _types;

    public final static String OPENID10 = "http://openid.net/signon/1.0";
    public final static String OPENID11 = "http://openid.net/signon/1.1";
    public final static String OPENID2 = "http://specs.openid.net/auth/2.0/signon";
    public final static String OPENID2_OP = "http://specs.openid.net/auth/2.0/server";
    public final static String OPENID2_RP = "http://specs.openid.net/auth/2.0/return_to";

    public static final Set OPENID1_SIGNON_TYPES = new HashSet() {{
        add(DiscoveryInformation.OPENID10);
        add(DiscoveryInformation.OPENID11);
    }};

    public static final Set OPENID_SIGNON_TYPES = new HashSet() {{
        addAll(DiscoveryInformation.OPENID1_SIGNON_TYPES);
        add(DiscoveryInformation.OPENID2);
    }};

    public static final Set OPENID_OP_TYPES = new HashSet() {{
        addAll(OPENID_SIGNON_TYPES);
        add(DiscoveryInformation.OPENID2_OP);
    }};

    public static final Set OPENID_TYPES = new HashSet() {{
        addAll(OPENID_OP_TYPES);
        add(DiscoveryInformation.OPENID2_RP);
    }};

    public static boolean isOpenIDType(String type)
    {
        return OPENID_TYPES.contains(type);
    }

    public DiscoveryInformation(URL opEndpoint) throws DiscoveryException
    {
        this(opEndpoint, null, OPENID2_OP);
    }

    public DiscoveryInformation(URL opEndpoint, Identifier claimedIdentifier)
            throws DiscoveryException
    {
        this(opEndpoint, claimedIdentifier, OPENID2);
    }

    public DiscoveryInformation(URL opEndpoint, Identifier claimedIdentifier,
                                String version)
            throws DiscoveryException
    {
        this(opEndpoint, claimedIdentifier, null, version);
    }

    public DiscoveryInformation(URL opEndpoint, Identifier claimedIdentifier,
                                String delegate, String version)
            throws DiscoveryException
    {
        this(opEndpoint, claimedIdentifier, delegate, version, null);
    }

    public DiscoveryInformation(URL opEndpoint, Identifier claimedIdentifier,
                                String delegate, String version, Set types)
            throws DiscoveryException
    {
        if (opEndpoint == null)
            throw new DiscoveryException("Null OpenID Provider endpoint.");
        _opEndpoint = opEndpoint;
        _claimedIdentifier = claimedIdentifier;
        _delegate = delegate;
        _version = version;
        _types = types;
    }

    public boolean hasClaimedIdentifier()
    {
        return _claimedIdentifier != null;
    }

    public boolean hasDelegateIdentifier()
    {
        return _delegate != null;
    }

    public URL getOPEndpoint()
    {
        return _opEndpoint;
    }

    public Identifier getClaimedIdentifier()
    {
        return _claimedIdentifier;
    }

    public String getDelegateIdentifier()
    {
        return _delegate;
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

    public Set getTypes()
    {
        return _types;
    }

    public void setTypes(Set types)
    {
        this._types = types;
    }

    public boolean hasType(String type)
    {
        return _types != null && _types.contains(type);
    }

    public String toString()
    {
        return (isVersion2() ? "OpenID2" : "OpenID1")
                + "\nOP-endpoint:" + _opEndpoint
            + "\nClaimedID:" + _claimedIdentifier
                + "\nDelegate:" + _delegate;
    }
}
