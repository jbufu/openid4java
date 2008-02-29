/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.html;

import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.discovery.DiscoveryException;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Holds information obtained by performing HTML discovery on an URL.
 */
public class HtmlResult
{
    private UrlIdentifier _claimedId;
    private URL _op1Endpoint;
    private String _delegate1;
    private URL _op2Endpoint;
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

    public void setEndpoint1(String op1Endpoint) throws DiscoveryException
    {
        URL url;
        try
        {
            url = new URL(op1Endpoint);

            _op1Endpoint = url;
        }
        catch (MalformedURLException e)
        {
            throw new DiscoveryException(
                    "Invalid openid.server URL: " + op1Endpoint);
        }

    }

    public URL getOP1Endpoint()
    {
        return _op1Endpoint;
    }

    public void setDelegate1(String delegate1)
    {
        _delegate1 = delegate1;
    }

    public String getDelegate1()
    {
        return _delegate1;
    }

    public void setEndpoint2(String op2Endpoint) throws DiscoveryException
    {
        URL url;
        try
        {
            url = new URL(op2Endpoint);

            _op2Endpoint = url;

        } catch (MalformedURLException e)
        {
            throw new DiscoveryException(
                    "Invalid openid2.provider URL: " + op2Endpoint);
        }

    }

    public URL getOP2Endpoint()
    {
        return _op2Endpoint;
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
                (_op2Endpoint != null ?
                        "\nOpenID2-endpoint:" + _op2Endpoint.toString() : "") +
                (_delegate2 != null ?
                        "\nOpenID2-localID:" + _delegate2 : "") +
                (_op1Endpoint != null ?
                        "\nOpenID1-endpoint:" + _op1Endpoint.toString() : "") +
                (_delegate1 != null ?
                        "\nOpenID1-delegate:" + _delegate1 : "");
    }
}
