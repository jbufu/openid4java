/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.UrlIdentifier;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.xrds.XrdsServiceEndpoint;
import org.openid4java.OpenIDException;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

/**
 * The results of Yadis discovery performed on a YadisURL,
 * represented through a stripped-down XRDS model,
 * containing only the those discovery information pieces
 * that are relevant for OpenID.
 * <p>
 * The payload is represented by the XRDS document. Along with it other
 * meta-information is contained, which can be useful while consuming
 * the results of Yadis discoveries.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisResult
{
    private static Log _log = LogFactory.getLog(YadisResult.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * XRDS endpoints obtained by performing Yadis discovery on the YadisURL.
     */
    private List _endpoints;

    /**
     * The content-type of the XRDS response.
     */
    private String _contentType;

    /**
     * The YadisURL on which discovery was performed.
     */
    private YadisUrl _yadisUrl;

    /**
     * The result of following redirects from the request_uri
     */
    private String  _normalizedUrl;

    /**
     * The URL from where the XRDS document was retrieved.
     */
    private URL _xrdsLocation;

    /**
     * The throwable or exception that caused the failure, if available.
     */
    private Throwable _failureCause;

    /**
     * Sets the YadisURL on which discovery will be performed.
     */
    public void setYadisUrl(YadisUrl url)
    {
        _yadisUrl = url;
    }

    /**
     * Gets the YadisUrl on which discovery is to be performed.
     */
    public YadisUrl getYadisUrl()
    {
        return _yadisUrl;
    }

    /**
     * Sets the Yadis Resource Descriptor (XRDS) location
     * found during discovery.
     * <p>
     * The XRDS location can be the same as the YadisUrl, or different if
     * redirects are followed during discovery, or if delegation is used.
     *
     * @param xrdsLocation      The Resource Descriptor URL
     *                          from where the XRDS is downloaded
     * @param onFailError       The error code which will be set in the result
     *                          if the XRDS location is not valid
     */
    public void setXrdsLocation(String xrdsLocation, int onFailError)
            throws YadisException
    {
        URL xrdsUrl = null;
        boolean validXrdsUrl = true;

        try
        {
            xrdsUrl = new URL(xrdsLocation);
        }
        catch (MalformedURLException e)
        {
            validXrdsUrl = false;
        }

        // perform the required checks on the discovered URL
        if (xrdsUrl == null || ! validXrdsUrl ||
                ( ! xrdsUrl.getProtocol().equals("http") &&
                ! xrdsUrl.getProtocol().equals("https")) )
            throw new YadisException("A Yadis Resource Descriptor URL" +
                    " MUST be an absolute URL and " +
                    "it must be HTTP or HTTPS; found: " + xrdsLocation,
                    onFailError);

        if (DEBUG)
            _log.debug("Setting X-XRDS-Location for yadis result: " + xrdsLocation);

        _xrdsLocation = xrdsUrl;
    }

    /**
     * Gets the Yadis Resource Descriptor (XRDS) location
     */
    public URL getXrdsLocation()
    {
        return _xrdsLocation;
    }

    /**
     * Sets the OpenID XRDS endpoints discovered from an identifier.
     */
    public void setEndpoints(List endpoints)
    {
        _endpoints = endpoints;
    }

    /**
     * Gets the OpenID XRDS endpoints discovered from an identifier.
     */
    public List getEndpoints()
    {
        return _endpoints;
    }

    public int getEndpointCount()
    {
        return _endpoints == null ? 0 : _endpoints.size();
    }

    public List getDiscoveredInformation(Set targetTypes) throws DiscoveryException
    {
        List result = new ArrayList();

        if (hasEndpoints()) 
        {
            XrdsServiceEndpoint endpoint;
            Iterator endpointsIter = _endpoints.iterator();
            while (endpointsIter.hasNext()) {
                endpoint = (XrdsServiceEndpoint) endpointsIter.next();
                Iterator typesIter = endpoint.getTypes().iterator();
                while (typesIter.hasNext()) {
                    String type = (String) typesIter.next();
                    if (!targetTypes.contains(type)) continue;
                    try {
                        result.add(new DiscoveryInformation(
                            new URL(endpoint.getUri()),
                            DiscoveryInformation.OPENID_SIGNON_TYPES.contains(type) ?
                                new UrlIdentifier(_normalizedUrl) : null,
                            DiscoveryInformation.OPENID2.equals(type) ? endpoint.getLocalId() :
                            DiscoveryInformation.OPENID1_SIGNON_TYPES.contains(type) ? endpoint.getDelegate() : null,
                            type,
                            endpoint.getTypes()));
                    } catch (MalformedURLException e) {
                        throw new YadisException("Invalid endpoint URL discovered: " + endpoint.getUri(), OpenIDException.YADIS_INVALID_URL);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @return true if the YadisResult has at least one XRDS endpoint,
     *         false otherwise.
     */
    public boolean hasEndpoints()
    {
        return _endpoints != null && ! _endpoints.isEmpty();
    }

    /**
     * Gets the result of following redirects on the YadisURL
     */
    public String getNormalizedUrl()
    {
        return _normalizedUrl;
    }

    /**
     * Sets the result of following redirects on the YadisURL
     */
    public void setNormalizedUrl(String _normalizedUrl)
    {
        this._normalizedUrl = _normalizedUrl;
    }

    /**
     * Sets the content-type of the response from which the XRDS was extracted.
     *
     * @param type          The content-type of the HTTP response
     *                      that contained the XRDS document
     */
    public void setContentType(String type)
    {
        _contentType = type;
    }

    /**
     * Gets the content-type of the response from which the XRDS was extracted.
     *
     * @return              The content-type of the HTTP response
     *                      that contained the XRDS document
     */
    public String getContentType()
    {
        return _contentType;
    }

    /**
     * Sets the throwable or exception that caused the failure of the Yadis
     * discovery, if one was thrown and intercepted
     */
    public void setFailureCause(Throwable e)
    {
        this._failureCause = e;
    }

    /**
     * Gets the throwable (or exception) that caused the failure of the Yadis
     * discovery, if one was thrown and intercepted
     */
    public Throwable getFailureCause()
    {
        return _failureCause;
    }

    public String dump()
    {
        StringBuffer dump = new StringBuffer();

        dump.append("YadisURL:").append(_yadisUrl);
        dump.append("\nNormalizedURL:").append(_normalizedUrl);
        dump.append("\nX-XRDS-Location:").append(_xrdsLocation);
        dump.append("\nContent-type:").append(_contentType);

        if (_endpoints != null)
        {
            dump.append("\nXRDS:");
            XrdsServiceEndpoint endpoint;
            Iterator iter = _endpoints.iterator();
            while(iter.hasNext())
            {
                endpoint = (XrdsServiceEndpoint) iter.next();
                dump.append("\n\tType: ").append(endpoint.getTypes().toArray());
                dump.append("\n\tServicePriority: ").append(endpoint.getServicePriority());
                dump.append("\n\tUriPriority: ").append(endpoint.getUriPriority());
                dump.append("\n\tURI: ").append(endpoint.getUri());
            }
        }

        return dump.toString();
    }
}
