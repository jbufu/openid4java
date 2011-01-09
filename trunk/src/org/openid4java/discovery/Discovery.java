/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import com.google.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.xri.XriResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.util.HttpFetcherFactory;
import org.openid4java.util.OpenID4JavaUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class Discovery
{
    private static Log _log = LogFactory.getLog(Discovery.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
    private static final Pattern XRI_PATTERN =
            Pattern.compile("^[!=@\\$\\+\\(]", Pattern.CASE_INSENSITIVE);

    private HtmlResolver _htmlResolver;
    private YadisResolver _yadisResolver;
    private XriResolver _xriResolver;

    private static final String XRI_RESOLVER_CLASS_NAME_KEY = "discovery.xri.resolver";

    public static XriResolver getXriResolver()
    {
        String className = OpenID4JavaUtils.getProperty(XRI_RESOLVER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(XRI_RESOLVER_CLASS_NAME_KEY + ":" + className);
        try {
            return (XriResolver) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing XRI resolver.", e);
        }
    }

    @Inject
    public Discovery(HtmlResolver htmlResolver, YadisResolver yadisResolver,
        XriResolver xriResolver)
    {
        _htmlResolver = htmlResolver;
        _yadisResolver = yadisResolver;
        _xriResolver = xriResolver;
    }

    public Discovery()
    {
      this(
          new HtmlResolver(new HttpFetcherFactory()),
          new YadisResolver(new HttpFetcherFactory()),
          getXriResolver());
    }

    public void setXriResolver(XriResolver xriResolver)
    {
        _xriResolver = xriResolver;
    }

    public void setYadisResolver(YadisResolver yadisResolver)
    {
        _yadisResolver = yadisResolver;
    }

    public void setHtmlResolver(HtmlResolver htmlResolver)
    {
        _htmlResolver = htmlResolver;
    }

    public Identifier parseIdentifier(String identifier)
            throws DiscoveryException
    {
        return parseIdentifier(identifier, false);
    }

    public Identifier parseIdentifier(String identifier,
                                             boolean removeFragment)
            throws DiscoveryException
    {
        try
        {
            // strip the xri:// prefix if it exists
            if (identifier.toLowerCase().startsWith("xri://"))
            {
                if (DEBUG) _log.debug("Dropping xri:// prefix from identifier: "
                        + identifier);
                identifier = identifier.substring(6);
            }

            if (URL_PATTERN.matcher(identifier).find())
            {
                if (DEBUG) _log.debug("Creating URL identifier for: " + identifier);
                return new UrlIdentifier(identifier, removeFragment);
            }
            else if (XRI_PATTERN.matcher(identifier).find())
            {
                if (DEBUG) _log.debug("Creating XRI identifier for: " + identifier);
                return _xriResolver.parseIdentifier(identifier);
            }
            else
            {
                if (DEBUG) _log.debug("Creating URL identifier (http:// prepended) for: "
                        + identifier);
                return new UrlIdentifier("http://" + identifier, removeFragment);
            }
        }
        catch (Exception e)
        {
            throw new DiscoveryException(
                    "Cannot parse identifier: " + identifier, e);
        }
    }

    public List discover(String identifier)
            throws DiscoveryException
    {
        return discover(parseIdentifier(identifier, true)); // remove fragment
    }

    public List discover(Identifier identifier) throws DiscoveryException
    {
        List result;

        if (identifier instanceof XriIdentifier)
        {
            _log.info("Starting discovery on XRI identifier: " + identifier);
            result = _xriResolver.discover((XriIdentifier) identifier);
        }
        else if (identifier instanceof UrlIdentifier)
        {
            _log.info("Starting discovery on URL identifier: " + identifier);

            UrlIdentifier urlId = (UrlIdentifier) identifier;

            result = _yadisResolver.discover(urlId.getIdentifier());

            // fall-back to HTML discovery
            if (result == null || result.size() == 0)
            {
                _log.info("No OpenID service endpoints discovered through Yadis;" +
                        " attempting HTML discovery...");

                result = _htmlResolver.discoverHtml(urlId);
            }
        }
        else
        {
            throw new DiscoveryException(
                    "Unknown identifier type: " + identifier.toString());
        }

        _log.info("Discovered " + result.size() + " OpenID endpoints.");

        return result;
    }

    /**
     * Performs discovery on the Relying Party's realm and returns a list of
     * OpenID 2.0 DiscoveryInformation entries.
     * <p>
     * Static method / caller must provide a YadisResolver so that
     * the OP doesn't have to instantiate a Discovery object.
     *
     * @param realm         RP's realm.
     * @param yadisResolver The YadisResolver instance to be used for discovery.
     * @return              List of OpenID 2.0 DiscoveryInformation endpoints.
     */
    public static List rpDiscovery(String realm, YadisResolver yadisResolver)
        throws DiscoveryException
    {
        // don't follow redirects when doing RP discovery
        return yadisResolver.discoverRP(realm);
    }

    /* visible for testing */
    public YadisResolver getYadisResolver()
    {
        return _yadisResolver;
    }
}
