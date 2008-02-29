/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import org.openxri.xml.XRDS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * The results of Yadis discovery performed on a YadisURL.
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
     * The XRDS document obtained by performing Yadis discovery on the YadisURL.
     */
    private XRDS _xrds;

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
     * Flag indicating whether the discovery was successfull.
     */
    private boolean _success = false;

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
     * Sets the Yadis Resource Descriptor (XRDS)
     *
     * @param xrds          The XRDS document associated with the YadisURL
     *                      obtained through Yadis discovery
     */
    public void setXrds(XRDS xrds)
    {
        _xrds = xrds;
    }

    /**
     * Gets the Yadis Resource Descriptor (XRDS) document.
     *
     * @return              The XRDS document associated with the YadisURL
     *                      obtained through Yadis discovery
     */
    public XRDS getXrds()
    {
        return _xrds;
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
     * Returns true if Yadis discovery succeeded.
     *
     */
    public boolean isSuccess()
    {
        return _success;
    }

    /**
     * Sets the Yadis discovery success flag.
     *
     * @param   status  True if Yadis succeeded, false otherwise.
     */
    public void setSuccess(boolean status)
    {
        this._success = status;
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
        dump.append("\nXRDS:\n").append(_xrds.dump());

        return dump.toString();
    }
}
