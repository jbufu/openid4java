/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.discovery;

import org.openxri.xml.*;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.TrustType;
import org.openxri.resolve.ResolverState;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;

import net.openid.yadis.YadisResolver;
import net.openid.yadis.YadisResult;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class Discovery
{
    private static Logger _log = Logger.getLogger(Discovery.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
    private static final Pattern XRI_PATTERN =
            Pattern.compile("^[!=@\\$\\+\\(]", Pattern.CASE_INSENSITIVE);

    final private static String ROOT_DEF_EQ_URI   = "http://equal.xri.net";
    final private static String ROOT_DEF_AT_URI   = "http://at.xri.net";
    final private static String ROOT_DEF_BANG_URI = "http://bang.xri.net";

    private Resolver _xriResolver = new Resolver();
    private YadisResolver _yadisResolver = new YadisResolver();
    private HtmlResolver _htmlResolver = new HtmlResolver();

    public Discovery()
    {
        if (DEBUG) _log.debug("Initializing Discovery object...");

        // populate the root with whatever trustType the user requested
        String trustParam = ";trust=none";

        XRD eqRoot = new XRD();
        Service eqAuthService = new Service();
        eqAuthService.addMediaType(Tags.CONTENT_TYPE_XRDS + trustParam, SEPElement.MATCH_ATTR_CONTENT, Boolean.FALSE);
        eqAuthService.addType(Tags.SERVICE_AUTH_RES);
        eqAuthService.addURI(ROOT_DEF_EQ_URI);
        eqRoot.addService(eqAuthService);

        XRD atRoot = new XRD();
        Service atAuthService = new Service();
        atAuthService.addMediaType(Tags.CONTENT_TYPE_XRDS + trustParam, SEPElement.MATCH_ATTR_CONTENT, Boolean.FALSE);
        atAuthService.addType(Tags.SERVICE_AUTH_RES);
        atAuthService.addURI(ROOT_DEF_AT_URI);
        atRoot.addService(atAuthService);

        XRD bangRoot = new XRD();
        Service bangAuthService = new Service();
        bangAuthService.addMediaType(Tags.CONTENT_TYPE_XRDS + trustParam, SEPElement.MATCH_ATTR_CONTENT, Boolean.FALSE);
        bangAuthService.addType(Tags.SERVICE_AUTH_RES);
        bangAuthService.addURI(ROOT_DEF_BANG_URI);
        bangRoot.addService(bangAuthService);

        _xriResolver.setAuthority("=", eqRoot);
        _xriResolver.setAuthority("@", atRoot);
        _xriResolver.setAuthority("!", bangRoot);
    }

    public void setXriResolver(Resolver xriResolver)
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
                return new UrlIdentifier(identifier);
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
                return new UrlIdentifier("http://" + identifier);
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
        return discover(parseIdentifier(identifier));
    }

    public List discover(Identifier identifier)
            throws DiscoveryException
    {
        List result = new ArrayList();

        if (identifier instanceof XriIdentifier)
        {
            _log.info("Starting discovery on XRI identifier: " + identifier);

            XRDS xrds;
            XriIdentifier xriIdentifier = (XriIdentifier) identifier;

            try
            {
                TrustType trustAll = new TrustType(TrustType.TRUST_NONE);
                xrds = _xriResolver.resolveAuthToXRDS(
                        xriIdentifier.getXriIdentifier(), trustAll, true, new ResolverState());

                if (DEBUG) _log.debug("Retrieved XRDS:\n" + xrds.dump());

                XRD xrd = xrds.getFinalXRD();
                CanonicalID canonical = xrd.getCanonicalidAt(0);

                if (isProviderAuthoritative(xrd.getProviderID(), canonical))
                {
                    _log.info("XRI resolution succeeded on " + identifier);
                    result = extractDiscoveryInformation(xrds, identifier);
                }
                else
                    _log.warn("ProviderID is not authoritative for the CanonicalID. " +
                            "Returning empty discovery result set.");
            }
            catch (Exception e)
            {
                throw new DiscoveryException(
                        "Cannot resolve XRI: " + identifier.toString(), e);
            }

        }
        else if (identifier instanceof UrlIdentifier)
        {
            _log.info("Starting discovery on URL identifier: " + identifier);

            UrlIdentifier urlId = (UrlIdentifier) identifier;

            YadisResult yadis = _yadisResolver.discover(urlId.getUrl().toString());

            if (YadisResult.OK == yadis.getStatus())
            {
                _log.info("Using Yadis normalized URL as claimedID: "
                        + yadis.getNormalizedUrl());

                result = extractDiscoveryInformation(yadis.getXrds(),
                        new UrlIdentifier(yadis.getNormalizedUrl()) );
            }

            if (result.size() == 0)
            {
                _log.info("No OpenID service endpoints discovered through Yadis;" +
                        " attempting HTML discovery...");

                result = extractDiscoveryInformation(_htmlResolver.discover(urlId));
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

    private List extractDiscoveryInformation(HtmlResult htmlResult)
            throws DiscoveryException
    {
        ArrayList htmlList = new ArrayList();

        if (htmlResult.getIdp2Endpoint() != null)
        {
            DiscoveryInformation extracted = new DiscoveryInformation(
                        htmlResult.getIdp2Endpoint(),
                        htmlResult.getClaimedId(),
                        htmlResult.getDelegate2(),
                        DiscoveryInformation.OPENID2);

            if (DEBUG)
                _log.debug("OpenID2-signon HTML discovery endpoint: " + extracted);

            htmlList.add(extracted);
        }

        if (htmlResult.getIdp1Endpoint() != null)
        {
            DiscoveryInformation extracted = new DiscoveryInformation(
                        htmlResult.getIdp1Endpoint(),
                        htmlResult.getClaimedId(),
                        htmlResult.getDelegate1(),
                        DiscoveryInformation.OPENID11);

            if (DEBUG)
                _log.debug("OpenID1-signon HTML discovery endpoint: " + extracted);

            htmlList.add(extracted);
        }

        return htmlList;
    }

    protected static List extractDiscoveryInformation(XRDS xrds,
                                                      Identifier identifier)
            throws DiscoveryException
    {
        ArrayList opSelectList = new ArrayList();
        ArrayList signonList = new ArrayList();
        ArrayList openid1 = new ArrayList();

        XRD xrd = xrds.getFinalXRD();

        Service service;
        URL opEndpointUrl;
        Identifier claimedIdentifier;
        CanonicalID canonicalId;
        String providerId;

        // iterate through all services
        Iterator iterS = xrd.getPrioritizedServices().iterator();
        while (iterS.hasNext())
        {
            service = (Service) iterS.next();

            //iterate through all URIs in the service
            Iterator iter = service.getPrioritizedURIs().iterator();
            while (iter.hasNext())
            {
                try
                {
                    opEndpointUrl = ((SEPUri) iter.next()).getURI().toURL();
                } catch (MalformedURLException e)
                {
                    continue;
                }

                if (matchType(service, DiscoveryInformation.OPENID2_OP))
                {
                    DiscoveryInformation extracted =
                            new DiscoveryInformation(opEndpointUrl);

                    if (DEBUG) _log.debug("OpenID2-server XRDS discovery result: "
                            + extracted);

                    opSelectList.add(extracted);
                }

                if (matchType(service, DiscoveryInformation.OPENID2))
                {
                    claimedIdentifier = identifier;
                    canonicalId = xrd.getCanonicalidAt(0);
                    providerId = xrd.getProviderID();

                    if (identifier instanceof XriIdentifier)
                    {
                        if (canonicalId == null)
                            throw new DiscoveryException(
                                    "No CanonicalID found after XRI resolution of: " +
                                            identifier.getIdentifier());

                        if (providerId == null || providerId.length() == 0)
                            throw new DiscoveryException(
                                    "No Provider ID found after XRI resolution of: " +
                                            identifier.getIdentifier());

                        claimedIdentifier = parseIdentifier(canonicalId.getValue());
                    }

                    DiscoveryInformation extracted =
                            new DiscoveryInformation(opEndpointUrl,
                                    claimedIdentifier,
                                    getDelegate(service, false),
                                    DiscoveryInformation.OPENID2);

                    if (DEBUG) _log.debug("OpenID2-signon XRDS discovery result: "
                            + extracted);

                    signonList.add(extracted);
                }

                if (matchType(service, DiscoveryInformation.OPENID10) ||
                        matchType(service, DiscoveryInformation.OPENID11))
                {
                    DiscoveryInformation extracted =
                            new DiscoveryInformation(opEndpointUrl,
                                    identifier, getDelegate(service, true),
                                    DiscoveryInformation.OPENID11);

                    if (DEBUG) _log.debug("OpenID1-signon XRDS discovery result: "
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
        for (int i = 0; i < service.getNumTypes(); i++) {
            SEPType type = service.getTypeAt(i);
            if(type.match(sVal)) return true;
        }
        return false;

    }

    private boolean isProviderAuthoritative(String providerId,
                                                   CanonicalID canonicalId)
    {
        // todo: also handle xri delegation / community names
        if (canonicalId == null || canonicalId.getValue() == null)
            return false;

        String auth = canonicalId.getValue().substring(0,1);
        XRD rootAuth = _xriResolver.getAuthority(auth);

        if ( ! rootAuth.getProviderID().equals(providerId) )
                return false;

        int lastbang = canonicalId.getValue().lastIndexOf("!");
        String parent = lastbang > -1 ?
                canonicalId.getValue().substring(0, lastbang) :
                canonicalId.getValue();

        String parentNoPrefix = parent.startsWith("xri://") ?
                parent.substring(6) : parent;

        return parentNoPrefix.equals(providerId);
    }
}
