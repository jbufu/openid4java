/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Holds information obtained by performing HTML discovery on an URL.
 */
public class HtmlResult
{
    private UrlIdentifier _claimedId;
    private URL _idp1Endpoint;
    private String _delegate1;
    private URL _idp2Endpoint;
    private String _delegate2;

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

    public void setEndpoint1(String idp1Endpoint) throws DiscoveryException
    {
        URL url;
        try
        {
            url = new URL(idp1Endpoint);

            _idp1Endpoint = url;
        }
        catch (MalformedURLException e)
        {
            throw new DiscoveryException(
                    "Invalid openid.server URL: " + idp1Endpoint);
        }

    }

    public URL getIdp1Endpoint()
    {
        return _idp1Endpoint;
    }

    public void setDelegate1(String delegate1)
    {
        _delegate1 = delegate1;
    }

    public String getDelegate1()
    {
        return _delegate1;
    }

    public void setEndpoint2(String idp2Endpoint) throws DiscoveryException
    {
        URL url;
        try
        {
            url = new URL(idp2Endpoint);

            _idp2Endpoint = url;

        } catch (MalformedURLException e)
        {
            throw new DiscoveryException(
                    "Invalid openid2.provider URL: " + idp2Endpoint);
        }

    }

    public URL getIdp2Endpoint()
    {
        return _idp2Endpoint;
    }

    public void setDelegate2(String delegate2)
    {
        _delegate2 = delegate2;
    }

    public String getDelegate2()
    {
        return _delegate2;
    }

    public String toString()
    {
        return "ClaimedID:" + _claimedId +
                (_idp2Endpoint != null ?
                        "\nOpenID2-endpoint:" + _idp2Endpoint.toString() : "") +
                (_delegate2 != null ?
                        "\nOpenID2-localID:" + _delegate2 : "") +
                (_idp1Endpoint != null ?
                        "\nOpenID1-endpoint:" + _idp1Endpoint.toString() : "") +
                (_delegate1 != null ?
                        "\nOpenID1-delegate:" + _delegate1 : "");
    }
}
