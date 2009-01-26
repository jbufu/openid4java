package org.openid4java.discovery.xri;

import org.openid4java.discovery.*;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.TrustType;
import org.openxri.resolve.ResolverState;
import org.openxri.resolve.exception.PartialResolutionException;
import org.openxri.xml.*;
import org.openxri.XRI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

public class LocalXriResolver implements XriResolver
{
    private static Log _log = LogFactory.getLog(LocalXriResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    final private static String ROOT_DEF_EQ_URI   = "http://equal.xri.net";
    final private static String ROOT_DEF_AT_URI   = "http://at.xri.net";
    final private static String ROOT_DEF_BANG_URI = "http://bang.xri.net";

    private Resolver _openXriResolver = new Resolver();

    public LocalXriResolver()
    {
        if (DEBUG) _log.debug("Initializing local XRI resolver...");

        // populate the root with whatever trustType the user requested
        String trustParam = ";trust=none";

        XRD eqRoot = new XRD();
        Service eqAuthService = new Service();
        eqAuthService.addMediaType(Tags.CONTENT_TYPE_XRDS + trustParam, SEPElement.MATCH_ATTR_CONTENT, Boolean.FALSE);
        eqAuthService.addType(Tags.SERVICE_AUTH_RES);
        eqAuthService.addURI(ROOT_DEF_EQ_URI);
        eqRoot.setProviderID("xri://=");
        eqRoot.addService(eqAuthService);

        XRD atRoot = new XRD();
        Service atAuthService = new Service();
        atAuthService.addMediaType(Tags.CONTENT_TYPE_XRDS + trustParam, SEPElement.MATCH_ATTR_CONTENT, Boolean.FALSE);
        atAuthService.addType(Tags.SERVICE_AUTH_RES);
        atAuthService.addURI(ROOT_DEF_AT_URI);
        atRoot.setProviderID("xri://@");
        atRoot.addService(atAuthService);

        XRD bangRoot = new XRD();
        Service bangAuthService = new Service();
        bangAuthService.addMediaType(Tags.CONTENT_TYPE_XRDS + trustParam, SEPElement.MATCH_ATTR_CONTENT, Boolean.FALSE);
        bangAuthService.addType(Tags.SERVICE_AUTH_RES);
        bangAuthService.addURI(ROOT_DEF_BANG_URI);
        bangRoot.setProviderID("xri://!");
        bangRoot.addService(bangAuthService);

        _openXriResolver.setAuthority("=", eqRoot);
        _openXriResolver.setAuthority("@", atRoot);
        _openXriResolver.setAuthority("!", bangRoot);

    }

    public List discover(XriIdentifier xri) throws DiscoveryException
    {
        try
        {
            TrustType trustAll = new TrustType(TrustType.TRUST_NONE);
            XRDS xrds = _openXriResolver.resolveAuthToXRDS(
                    xri.toString(), trustAll, true, new ResolverState());

            if (DEBUG) _log.debug("Retrieved XRDS:\n" + xrds.dump());

            XRD xrd = xrds.getFinalXRD();
            CanonicalID canonical = xrd.getCanonicalidAt(0);

            // todo: this is not the right place to put isProviderAuthoritative
            if (isProviderAuthoritative(xrd.getProviderID(), canonical))
            {
                _log.info("XRI resolution succeeded on " + xri);
                return extractDiscoveryInformation(xrds, xri, _openXriResolver);
            }
            else
            {
                _log.warn("ProviderID is not authoritative for the CanonicalID. " +
                        "Returning empty discovery result set.");
                return new ArrayList();
            }
        }
        catch (Exception e)
        {
            throw new DiscoveryException(
                    "Cannot resolve XRI: " + xri, e);
        }
    }

    public XriIdentifier parseIdentifier(String identifier) throws DiscoveryException {

        XRI xri = new XRI(identifier);
        return new XriIdentifier(identifier, xri.toIRINormalForm(), xri.toURINormalForm());
    }

    private boolean isProviderAuthoritative(String providerId,
                                                   CanonicalID canonicalId)
    {
        // todo: also handle xri delegation / community names
        // todo: isProviderAuthoritative does not work on multi-level i-names
        if (canonicalId == null || canonicalId.getValue() == null)
            return false;

        String auth = canonicalId.getValue().substring(0,1);
        XRD rootAuth = _openXriResolver.getAuthority(auth);

        if ( ! rootAuth.getProviderID().equals(providerId) )
                return false;

        int lastbang = canonicalId.getValue().lastIndexOf("!");
        String parent = lastbang > -1 ?
                canonicalId.getValue().substring(0, lastbang) :
                canonicalId.getValue();

        String parentNoPrefix = parent.startsWith("xri://") ?
                parent.substring(6) : parent;

        String providerIDNoPrefix = providerId.startsWith("xri://") ?
                providerId.substring(6) : providerId;

        return parentNoPrefix.equals(providerIDNoPrefix);
    }
    
    // --- XRI discovery patch from William Tan ---

    /**
     * Extracts OpenID discovery endpoints from a XRDS discovery result
     * for XRI identifiers.
     *
     * @param xrds          The discovered XRDS document.
     * @param identifier    The identifier on which discovery was performed.
     * @param xriResolver   The XRI resolver to use for extraction of OpenID
     *                      service endpoints.
     * @return              A list of DiscoveryInformation endpoints.
     * @throws DiscoveryException when invalid information is discovered.
     */
    protected List extractDiscoveryInformation(XRDS xrds,
                                                      XriIdentifier identifier,
                                                      Resolver xriResolver)
            throws DiscoveryException
    {
        ArrayList endpoints = new ArrayList();

        XRD xrd = xrds.getFinalXRD();

        // try OP Identifier
        extractDiscoveryInformationOpenID(
            xriResolver,
            endpoints,
            xrd,
            identifier,
            DiscoveryInformation.OPENID2_OP,
            false // no CID
        );

        // OpenID 2 signon
        extractDiscoveryInformationOpenID(
            xriResolver,
            endpoints,
            xrd,
            identifier,
            DiscoveryInformation.OPENID2, // sepType
            true // want CID
        );

        // OpenID 1.x
        extractDiscoveryInformationOpenID(
            xriResolver,
            endpoints,
            xrd,
            identifier,
            DiscoveryInformation.OPENID11,
            true // wantCID
        );

        extractDiscoveryInformationOpenID(
            xriResolver,
            endpoints,
            xrd,
            identifier,
            DiscoveryInformation.OPENID10,
            true // wantCID
        );

        if (endpoints.size() == 0)
            _log.info("No OpenID service types found in the XRDS.");

        return endpoints;
    }

    protected boolean extractDiscoveryInformationOpenID(
            Resolver xriResolver, ArrayList out, XRD baseXRD,
            XriIdentifier identifier, String srvType, boolean wantCID)
    {
        try
        {
            XRDS tmpXRDS = xriResolver.selectServiceFromXRD(
                baseXRD,
                new XRI(identifier.toString()),
                new TrustType(),
                srvType,
                null, // sepMediaType
                true, // followRefs
                new ResolverState()
            );

            Identifier claimedIdentifier = null;
            URL opEndpointUrl;
            CanonicalID canonID;

            XRD tmpXRD = tmpXRDS.getFinalXRD();

            if (wantCID)
            {
                canonID = tmpXRD.getCanonicalidAt(0);

                if (canonID == null) {
                    _log.error("No CanonicalID found for " + srvType +
                            " after XRI resolution of: "
                            + identifier.getIdentifier());
                    return false;
                }

                // todo: canonicalID verification?
                // canonical ID is/should be an XRI?
                claimedIdentifier = parseIdentifier(canonID.getValue());
                _log.info("Using canonicalID as claimedID: " +
                          claimedIdentifier.getIdentifier() +
                          " for " + srvType);
            }

            Iterator it = tmpXRD.getSelectedServices().getList().iterator();
            while (it.hasNext())
            {
                Service srv = (Service)it.next();
                Iterator itURI = srv.getPrioritizedURIs().iterator();
                SEPUri sepURI;
                while (itURI.hasNext())
                {
                    sepURI = (SEPUri) itURI.next();
                    try
                    {
                        String urlString = xriResolver.constructURI(
                                sepURI.getURI(),
                                sepURI.getAppend(),
                                new XRI(identifier.toString()));

                        opEndpointUrl = new URL(urlString);

                        DiscoveryInformation extracted =
                                new DiscoveryInformation(
                                        opEndpointUrl,
                                        wantCID ? claimedIdentifier : null,
                                        null,
                                        srvType);

                        _log.info("Added " + srvType +
                                  " endpoint: " + opEndpointUrl);

                        out.add(extracted);
                    }
                    catch (MalformedURLException mue)
                    {
                        _log.warn("Ignoring malformed OP endpoint URL in XRDS file: "
                                  + sepURI.toString(), mue);
                    }
                    catch (IllegalArgumentException ee)
                    {
                        _log.warn("Ignoring invalid OP endpoint URL in XRDS file: "
                                  + sepURI.toString(), ee);
                    }

                }
            }

            return true;
        }
        catch (PartialResolutionException e)
        {
            _log.error("XRI resolution failed for " + srvType, e);
        }
        catch (DiscoveryException de)
        {
            _log.error("XRDS discovery failed for " + srvType, de);
        }

        return false;
    }

    // --- end XRI discovery patch from William Tan ---

}
