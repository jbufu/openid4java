/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.discovery;

import java.net.URL;

/**
 * Holds information obtained by performing HTML discovery on an URL.
 */
public class HtmlResult
{
    private UrlIdentifier _claimedId;
    private URL _idp1Endpoint;
    private UrlIdentifier _delegate1;
    private URL _idp2Endpoint;
    private UrlIdentifier _delegate2;

    /**
     * Constructs an empty HtmlResult object.
     */
    public HtmlResult()
    {

    }

    /**
     * Sets the claimed identifier.
     */
    public void setClaimed(UrlIdentifier claimed)
    {
        _claimedId = claimed;
    }

    /**
     * Gets the claimed identifier.
     */
    public UrlIdentifier getClaimedId()
    {
        return _claimedId;
    }

    public void setEndpoint1(URL idp1Endpoint)
    {
        _idp1Endpoint = idp1Endpoint;
    }

    public URL getIdp1Endpoint()
    {
        return _idp1Endpoint;
    }

    public void setDelegate1(UrlIdentifier delegate1)
    {
        _delegate1 = delegate1;
    }

    public UrlIdentifier getDelegate1()
    {
        return _delegate1;
    }

    public void setEndpoint2(URL idp2Endpoint)
    {
        _idp2Endpoint = idp2Endpoint;
    }

    public URL getIdp2Endpoint()
    {
        return _idp2Endpoint;
    }

    public void setDelegate2(UrlIdentifier delegate2)
    {
        _delegate2 = delegate2;
    }

    public UrlIdentifier getDelegate2()
    {
        return _delegate2;
    }
}
