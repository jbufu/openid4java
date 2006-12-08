/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.discovery;

import org.openxri.xml.*;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.TrustType;
import org.openxri.resolve.ResolverState;
import org.w3c.dom.Element;

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
    private static final Pattern URL_PATTERN =
            Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
    private static final Pattern XRI_PATTERN =
            Pattern.compile("^(xri://|[!=@\\$\\+])", Pattern.CASE_INSENSITIVE);

    final private static String ROOT_DEF_EQ_URI   = "http://equal.xri.net";
    final private static String ROOT_DEF_AT_URI   = "http://at.xri.net";
    final private static String ROOT_DEF_BANG_URI = "http://bang.xri.net";

    private Resolver _xriResolver = new Resolver();
    private YadisResolver _yadisResolver = new YadisResolver();
    private HtmlResolver _htmlResolver = new HtmlResolver();

    public Discovery()
    {
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
            if (URL_PATTERN.matcher(identifier).find())
            {
                return new UrlIdentifier(identifier);
            }
            else if (XRI_PATTERN.matcher(identifier).find())
            {
                return new XriIdentifier(identifier);
            }
            else
            {
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

        if (identifier instanceof XriIdentifier)
        {
            XRDS xrds;
            XriIdentifier xriIdentifier = (XriIdentifier) identifier;

            try
            {
                TrustType trustAll = new TrustType(TrustType.TRUST_NONE);
                xrds = _xriResolver.resolveAuthToXRDS(
                        xriIdentifier.getXriIdentifier(), trustAll, true, new ResolverState());

                XRD xrd = xrds.getFinalXRD();
                CanonicalID canonical = xrd.getCanonicalidAt(0);

/*
                if (! isProviderAuthoritative(xrd.getProviderID(), canonical))
                {
                    Identifier canonicalId =
                            new XriIdentifier(canonical.getValue());
                    XRDS newXrds = _xriResolver.resolveAuthToXRDS(
                            canonicalId.getIdentifier(), trustAll, true, new ResolverState());

                    //todo: not sure what to compare from user / canonical XRDS
                    // provider id / canonical id ?
                    // service endpoints ?
                    // everything ?
                }
*/
            }
            catch (Exception e)
            {
                throw new DiscoveryException(
                        "Cannot resolve XRI: " + identifier.toString(), e);
            }

            return extractDiscoveryInformation(xrds, identifier);
        }
        else if (identifier instanceof UrlIdentifier)
        {
            UrlIdentifier urlId = (UrlIdentifier) identifier;

            YadisResult yadis = _yadisResolver.discover(urlId.getUrlIdentifier().toString());

            if (YadisResult.OK == yadis.getStatus())
            {
                return extractDiscoveryInformation(yadis);
            }
            else
            {
                //todo: log yadis discovery failure
                //throw new DiscoveryException(
                //        "Error while performing Yadis discovery on: "
                //                + identifier.getIdentifier() + " : "
                //                + yadis.getStatusMessage());

                // attempt HTML-based discovery
                DiscoveryInformation info = _htmlResolver.discover(urlId);
                List result = new ArrayList();
                result.add(info);

                return result;
            }
        }
        else
        {
            throw new DiscoveryException(
                    "Unknown identifier type: " + identifier.toString());
        }
    }

    protected static List extractDiscoveryInformation(YadisResult yadisResult)
            throws DiscoveryException
    {
        // the Yadis normalized URL becomes the claimed ID
        return extractDiscoveryInformation(
                yadisResult.getXrds(),
                new UrlIdentifier(yadisResult.getNormalizedUrl()) );
    }

    protected static List extractDiscoveryInformation(XRDS xrds,
                                                      Identifier identifier)
            throws DiscoveryException
    {
        ArrayList idpSelectList = new ArrayList();
        ArrayList signonList = new ArrayList();
        ArrayList openid1 = new ArrayList();

        XRD xrd = xrds.getFinalXRD();

        Iterator iter = xrd.getPrioritizedServices().iterator();
        while (iter.hasNext())
        {
            Service service = (Service) iter.next();

            DiscoveryInformation discovery = extractDiscoveryInformation(
                    service, identifier,
                    xrd.getCanonicalidAt(0), xrd.getProviderID());

            if (discovery == null)
                continue;

            String version = discovery.getVersion();

            if (DiscoveryInformation.OPENID2_OP.equals(version))
                idpSelectList.add(discovery);
            else if (DiscoveryInformation.OPENID2.equals(version))
                signonList.add(discovery);
            else if (DiscoveryInformation.OPENID10.equals(version) ||
                    DiscoveryInformation.OPENID11.equals(version))
                openid1.add(discovery);
        }

        idpSelectList.addAll(signonList);
        idpSelectList.addAll(openid1);
        return idpSelectList;
    }


    protected static DiscoveryInformation extractDiscoveryInformation(
            Service service, Identifier identifier,
            CanonicalID canonicalId, String providerId)
            throws DiscoveryException
    {
        URL idpEndpointUrl;
        Identifier claimedIdentifier = null;
        Identifier delegateIdentifier = null;
        String version;

        if (matchType(service, DiscoveryInformation.OPENID2_OP))
        {
            version = DiscoveryInformation.OPENID2_OP;
            idpEndpointUrl = getOPEndpoint(service);
        }
        else if (matchType(service, DiscoveryInformation.OPENID2))
        {
            version = DiscoveryInformation.OPENID2;
            idpEndpointUrl = getOPEndpoint(service);

            claimedIdentifier = identifier;

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

            delegateIdentifier = getDelegate(service);
        }
        else if (matchType(service, DiscoveryInformation.OPENID10) ||
                matchType(service, DiscoveryInformation.OPENID11))
        {
            version = DiscoveryInformation.OPENID11;
            idpEndpointUrl = getOPEndpoint(service);
            claimedIdentifier = identifier;
            delegateIdentifier = getDelegate(service);
        }
        else
        {
            return null;
        }

        return new DiscoveryInformation(idpEndpointUrl, claimedIdentifier,
                                        delegateIdentifier, version);
    }

    public static URL getOPEndpoint(Service service) throws DiscoveryException
    {
        try
        {
            // todo: use all service endpoints
            return service.getURIAt(0).getURI().toURL();
        }
        catch (MalformedURLException e)
        {
            throw new DiscoveryException("Bad IdP Endpoint URL!", e);
        }
    }

    public static Identifier getDelegate(Service service) throws DiscoveryException
    {
        Identifier delegate = null;

        Vector delegateTags = service.getOtherTagValues("Delegate");
        for (int i = 0; delegateTags != null && i < delegateTags.size(); i++)
        {
            Element element = (Element) delegateTags.elementAt(i);

            if ("http://openid.net/xmlns/1.0"
                    .equals(element.getNamespaceURI()))
            {
                String delegateStr = element.getFirstChild().getNodeValue();
                delegate = parseIdentifier(delegateStr);
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

    private static boolean isProviderAuthoritative(String providerId,
                                                   CanonicalID canonicalId)
    {
        if (canonicalId != null && canonicalId.getValue() != null)
        {
            // from JanRain's implementation:
            int lastbang = canonicalId.getValue().lastIndexOf("!");
            String parent  = lastbang > -1 ?
                    canonicalId.getValue().substring(0, lastbang) :
                    canonicalId.getValue();

            return parent.equals(providerId);
        }
        else
        {
            return false;
        }
    }
}
