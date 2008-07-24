/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import java.util.regex.Pattern;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;

import org.openid4java.util.HttpCache;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.html.HtmlResult;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.discovery.yadis.YadisResult;
import org.openid4java.discovery.yadis.YadisException;
import org.openid4java.discovery.xri.XriDiscovery;
import org.openid4java.discovery.xri.LocalXriResolver;

import org.openid4java.util.IdPValidationDriver;
import org.openid4java.util.IdPInvalidException;

import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxri.xml.XRDS;
import org.openxri.xml.XRD;
import org.openxri.xml.Service;
import org.openxri.xml.SEPUri;

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

    private XriDiscovery _xriResolver = new LocalXriResolver();
    private YadisResolver _yadisResolver = new YadisResolver();
    private HtmlResolver _htmlResolver = new HtmlResolver();

    public Discovery()
    {
    }

    public void setXriResolver(XriDiscovery xriResolver)
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

    public static Identifier parseIdentifier(String identifier)
            throws DiscoveryException
    {
        return parseIdentifier(identifier, false);
    }

    public static Identifier parseIdentifier(String identifier,
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
                return new XriIdentifier(identifier);
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

    public List discover(Identifier identifier)
            throws DiscoveryException
    {
        List result = new ArrayList();

        if (identifier instanceof XriIdentifier)
        {
            _log.info("Starting discovery on XRI identifier: " + identifier);
            result = _xriResolver.discover((XriIdentifier) identifier);
        }
        else if (identifier instanceof UrlIdentifier)
        {
            _log.info("Starting discovery on URL identifier: " + identifier);

            UrlIdentifier urlId = (UrlIdentifier) identifier;

            HttpCache cache = new HttpCache();

            try
            {
                YadisResult yadis =
                        _yadisResolver.discover(urlId.toString(), cache);

                if (yadis.isSuccess())
                {
                    _log.info("Using Yadis normalized URL as claimedID: "
                            + yadis.getNormalizedUrl());

                    result = extractDiscoveryInformation(yadis.getXrds(),
                            new UrlIdentifier(yadis.getNormalizedUrl()) );
                }

            }
            catch (YadisException e)
            {
                _log.warn("Yadis discovery failed on " + urlId.toString() +
                          " : " + e.getMessage(), e.getCause());
            }

            // fall-back to HTML discovery
            if (result.size() == 0)
            {
                _log.info("No OpenID service endpoints discovered through Yadis;" +
                        " attempting HTML discovery...");

                result = extractDiscoveryInformation(
                        _htmlResolver.discover(urlId, cache));
            }
        }
        else
        {
            throw new DiscoveryException(
                    "Unknown identifier type: " + identifier.toString());
        }

        _log.info("Discovered " + result.size() + " OpenID endpoints.");

        try
        {
            result = IdPValidationDriver.performIdPValidation(identifier, result);
        }
        catch(IdPInvalidException iie)
        {
            throw new DiscoveryException(
                "Discovery failed to return results: " + iie);
        }

        _log.info("Discovered " + result.size() + " validated OpenID endpoints.");

        return result;
    }

    /**
     * Extracts OpenID discovery endpoints from a HTML discovery result.
     *
     * @param htmlResult    HTML discovery result.
     * @return              List of DiscoveryInformation endpoints.
     * @throws DiscoveryException when invalid information is discovered.
     */
    private List extractDiscoveryInformation(HtmlResult htmlResult)
            throws DiscoveryException
    {
        ArrayList htmlList = new ArrayList();

        if (htmlResult.getOP2Endpoint() != null)
        {
            DiscoveryInformation extracted = new DiscoveryInformation(
                        htmlResult.getOP2Endpoint(),
                        htmlResult.getClaimedId(),
                        htmlResult.getDelegate2(),
                        DiscoveryInformation.OPENID2);

            if (DEBUG)
                _log.debug("OpenID2-signon HTML discovery endpoint: " + extracted);

            htmlList.add(extracted);
        }

        if (htmlResult.getOP1Endpoint() != null)
        {
            DiscoveryInformation extracted = new DiscoveryInformation(
                        htmlResult.getOP1Endpoint(),
                        htmlResult.getClaimedId(),
                        htmlResult.getDelegate1(),
                        DiscoveryInformation.OPENID11);

            if (DEBUG)
                _log.debug("OpenID1-signon HTML discovery endpoint: " + extracted);

            htmlList.add(extracted);
        }

        return htmlList;
    }

    /**
     * Extracts OpenID discovery endpoints from a XRDS discovery result.
     *
     * @param xrds          The discovered XRDS document.
     * @param identifier    The identifier on which discovery was performed.
     * @return              A list of DiscoveryInformation endpoints.
     * @throws DiscoveryException when invalid information is discovered.
     */
    protected static List extractDiscoveryInformation(XRDS xrds,
                                                      Identifier claimedIdentifier)
            throws DiscoveryException
    {
        ArrayList opSelectList = new ArrayList();
        ArrayList signonList = new ArrayList();
        ArrayList openid1 = new ArrayList();

        XRD xrd = xrds.getFinalXRD();

        Service service;
        URL opEndpointUrl;

        // iterate through all services
        Iterator iterS = xrd.getPrioritizedServices().iterator();
        while (iterS.hasNext())
        {
            service = (Service) iterS.next();

            //iterate through all URIs in the service
            Iterator iter = service.getPrioritizedURIs().iterator();
            SEPUri sepUri;
            while (iter.hasNext())
            {
                sepUri = (SEPUri) iter.next();
                try
                {
                    opEndpointUrl = sepUri.getURI().toURL();
                }
                catch (MalformedURLException e)
                {
                    _log.warn("Ignoring malformed OP endpoint URL in XRDS file: "
                              + sepUri.toString(), e);
                    continue;
                }
                catch (IllegalArgumentException ee)
                {
                    _log.warn("Ignoring invalid OP endpoint URL in XRDS file: "
                              + sepUri.toString(), ee);
                    continue;
                }

                if (matchType(service, DiscoveryInformation.OPENID2_OP))
                {
                    DiscoveryInformation extracted =
                            new DiscoveryInformation(opEndpointUrl);

                    if (DEBUG) _log.debug("OpenID2-server XRDS discovery result:\n"
                            + extracted);

                    opSelectList.add(extracted);
                }

                if (matchType(service, DiscoveryInformation.OPENID2))
                {
                    DiscoveryInformation extracted =
                            new DiscoveryInformation(opEndpointUrl,
                                    claimedIdentifier,
                                    getDelegate(service, false),
                                    DiscoveryInformation.OPENID2);

                    if (DEBUG) _log.debug("OpenID2-signon XRDS discovery result:\n"
                            + extracted);

                    signonList.add(extracted);
                }

                if (matchType(service, DiscoveryInformation.OPENID10) ||
                        matchType(service, DiscoveryInformation.OPENID11))
                {
                    DiscoveryInformation extracted =
                            new DiscoveryInformation(opEndpointUrl,
                                    claimedIdentifier, getDelegate(service, true),
                                    DiscoveryInformation.OPENID11);

                    if (DEBUG) _log.debug("OpenID1-signon XRDS discovery result:\n"
                            + extracted);

                    openid1.add(extracted);
                }
            }
        }

        opSelectList.addAll(signonList);
        opSelectList.addAll(openid1);

        if (opSelectList.size() == 0)
            _log.info("No OpenID service types found in the XRDS.");

        return opSelectList;
    }

    public static String getDelegate(Service service, boolean compatibility)
    {
        String delegate = null;
        String delegateTag;
        String nsPattern;

        if (compatibility)
        {
            delegateTag = "Delegate";
            nsPattern = "http://openid\\.net/xmlns/1\\.0";
        }
        else
        {
            delegateTag = "LocalID";
            nsPattern = "xri://\\$xrd\\*\\(\\$v\\*2\\.0\\)";
        }

        Vector delegateTags = service.getOtherTagValues(delegateTag);
        for (int i = 0; delegateTags != null && i < delegateTags.size(); i++)
        {
            Element element = (Element) delegateTags.elementAt(i);

            if (Pattern.matches(nsPattern, element.getNamespaceURI()))
            {
                delegate = element.getFirstChild().getNodeValue();

                // todo: multiple delegate tags?
                if (DEBUG) _log.debug("Found delegate: " + delegate);
            }
        }

        return delegate;
    }

     // deprecated in open-xri, copied here to avoid warnings
    public static boolean matchType(Service service, String sVal)
    {
        for (int i = 0; i < service.getNumTypes(); i++)
        {
            if (service.getTypeAt(i).match(sVal))
                return true;
        }
        return false;
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
        List result = new ArrayList();

        // don't follow redirects when doing RP discovery
        YadisResult rpDiscovery = yadisResolver.discover(realm, 0);

        if ( rpDiscovery.isSuccess() )
        {
            result = extractRpDiscoveryInformation(rpDiscovery.getXrds());
        }

        return result;
    }

    protected static List extractRpDiscoveryInformation(XRDS xrds)
        throws DiscoveryException
    {
        ArrayList result = new ArrayList();

        XRD xrd = xrds.getFinalXRD();

        Service service;
        URL endpointUrl;

        // iterate through all services
        Iterator iterS = xrd.getPrioritizedServices().iterator();
        while (iterS.hasNext())
        {
            service = (Service) iterS.next();

            //iterate through all URIs in the service
            Iterator iter = service.getPrioritizedURIs().iterator();
            SEPUri sepUri;
            while (iter.hasNext())
            {
                sepUri = (SEPUri) iter.next();
                try
                {
                    endpointUrl = sepUri.getURI().toURL();
                }
                catch (MalformedURLException e)
                {
                    _log.warn("Ignoring malformed OP endpoint URL in XRDS file: "
                              + sepUri.toString(), e);
                    continue;
                }
                catch (IllegalArgumentException ee)
                {
                    _log.warn("Ignoring invalid OP endpoint URL in XRDS file: "
                              + sepUri.toString(), ee);
                    continue;
                }


                if (matchType(service, DiscoveryInformation.OPENID2_RP))
                {
                    DiscoveryInformation extracted =
                            new DiscoveryInformation(endpointUrl, null,
                                DiscoveryInformation.OPENID2_RP);

                    if (DEBUG) _log.debug("OpenID2-RP XRDS discovery result:\n"
                            + extracted);

                    result.add(extracted);
                }
            }
        }

        if (result.size() == 0)
            _log.info("No OpenID service types found in the XRDS.");

        return result;
    }

}
