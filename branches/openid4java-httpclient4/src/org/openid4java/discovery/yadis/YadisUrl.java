/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery.yadis;

import java.net.URL;
import java.net.MalformedURLException;

import org.openid4java.OpenIDException;
import org.openid4java.discovery.XriIdentifier;

/**
 * Wrapper class for various identifiers that are resolvable to URLs
 * and can be used as YadisURLs with the Yadis protocol.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class YadisUrl
{
    /**
     * A YadisURL is a regular URL, with a couple restrictions.
     */
    private URL _yadisUrl;

    /**
     * Constructs a URL object from a string;
     * needed by the YadisURL(String) constructor
     *
     * @param urlString         URL-type identifier in string format
     * @return                  URL object
     * @throws YadisException   if the provided string is not a valid URL
     */
    private static URL urlFromString(String urlString) throws YadisException
    {
        URL url;

        try
        {
            url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            throw new YadisException("Invalid URL: " + urlString,
                    OpenIDException.YADIS_INVALID_URL, e);
        }

        return url;
    }

    /**
     * Contructs a YadisURL from a string;
     * assumes the string to be a URL-type identifier
     *
     * @param urlString         URL-type identifier in string format
     * @throws YadisException   if the provided string cannot be a YadisUrl
     */
    public YadisUrl(String urlString) throws YadisException
    {
        this(urlFromString(urlString));

        if (! isValid(this._yadisUrl))
            throw new YadisException(
                "The scheme name of a Yadis URL must be 'http' or 'https'",
                OpenIDException.YADIS_INVALID_SCHEME);

    }

    /**
     * Constructs a YadisURL from a URL object;
     * insures the schema is HTTP or HTTPS
     *
     * @param urlId             URL identifier
     * @throws YadisException   tf the URL identifier is not a valid YadisURL
     */
    public YadisUrl(URL urlId) throws YadisException
    {
        if (isValid(urlId))
            _yadisUrl = urlId;
        else
            throw new YadisException(
                "The scheme name of a Yadis URL must be 'http' or 'https'",
                OpenIDException.YADIS_INVALID_SCHEME);
    }

    /**
     * Validates a URL against the requirements for a YadisUrl.
     * <p>
     * The URL must be absolute (the schema must be specified),
     * and the schema must be HTTP or HTTPS.
     *
     * @param url               the URL to be validated
     * @return                  true if the URL is a valid YadisUrl,
     *                          or false otherwise
     */
    private boolean isValid(URL url)
    {
        return url.getProtocol().equalsIgnoreCase("http") ||
                url.getProtocol().equalsIgnoreCase("https");
    }

    /**
     * Constructs a YadisURL from an XRI identifier.
     *
     * @param xriId             The XRI identifier
     */
    public YadisUrl(XriIdentifier xriId) throws YadisException
    {
        this(urlFromString(xriId.toURINormalForm()));
    }

    /**
     * Gets the URL to be used in Yadis transactions.
     */
    public URL getUrl()
    {
        return _yadisUrl;
    }

    /**
     * Gets a string representation of the YadisURL.
     */
    public String toString()
    {
        return _yadisUrl.toString();
    }
}
