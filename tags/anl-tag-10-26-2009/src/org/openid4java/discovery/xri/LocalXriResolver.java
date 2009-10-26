package org.openid4java.discovery.xri;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.XriIdentifier;
import org.openxri.XRI;
import org.openxri.resolve.Resolver;
import org.openxri.resolve.ResolverFlags;
import org.openxri.resolve.ResolverState;
import org.openxri.resolve.exception.PartialResolutionException;
import org.openxri.xml.CanonicalID;
import org.openxri.xml.SEPUri;
import org.openxri.xml.Service;
import org.openxri.xml.Status;
import org.openxri.xml.XRD;
import org.openxri.xml.XRDS;

public class LocalXriResolver implements XriResolver
{
    private static Log _log = LogFactory.getLog(LocalXriResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private Resolver _openXriResolver;
    

    public LocalXriResolver()
    {
        if (DEBUG) _log.debug("Initializing local XRI resolver...");

        try {

        	_openXriResolver = new Resolver(null);
		} catch (Exception e) {
			
			throw new RuntimeException("Cannot initialize OpenXRI Resolver: " + e.getMessage(), e);
		}
    }

    public Resolver getResolver() {
    	
    	return _openXriResolver;
    }
    
    public List discover(XriIdentifier xri) throws DiscoveryException
    {
        try
        {
        	ResolverFlags flags = new ResolverFlags();
        	flags.setCid(true);
        	flags.setRefs(true);
        	ResolverState state = new ResolverState();
            XRDS xrds = _openXriResolver.resolveAuthToXRDS(
            		new XRI(xri.getIdentifier()), flags, state);

            if (DEBUG) _log.debug("Retrieved XRDS:\n" + xrds.dump());

            XRD xrd = xrds.getFinalXRD();

            if (! xrd.getStatus().getCID().equals(Status.CID_VERIFIED))
            {
            	_log.error("Unverified CanonicalID: " + xrd.getCanonicalID() + " of: " + xri.getIdentifier());
            	throw new RuntimeException("Unverified CanonicalID: " + xrd.getCanonicalID() + " of: " + xri.getIdentifier());
            }

            CanonicalID canonical = xrd.getCanonicalID();
            if (canonical == null) throw new RuntimeException("Missing CanonicalID of: " + xri.getIdentifier());
            
            _log.info("XRI resolution succeeded on " + xri.toString());

            return extractDiscoveryInformation(xrds, xri, _openXriResolver);

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
        	ResolverFlags flags = new ResolverFlags();
        	flags.setCid(true);
        	flags.setRefs(true);
        	flags.setNoDefaultT(srvType != null);	// we don't want default SEPs, only ones that really have the service type we are looking for
        	ResolverState state = new ResolverState();

        	List services = xriResolver.selectServiceFromXRD(
        		new XRDS(),
        		baseXRD,
                new XRI(identifier.getIdentifier()),
                srvType,
                null, // sepMediaType
                flags,
                state
            );

            Identifier claimedIdentifier = null;
            URL opEndpointUrl;
            CanonicalID canonID;

            if (! baseXRD.getStatus().getCID().equals(Status.CID_VERIFIED)) {
            	_log.error("Unverified CanonicalID: " + baseXRD.getCanonicalID() + " of:" + identifier.getIdentifier());
            	return false;
            }
            
            if (wantCID)
            {
                canonID = baseXRD.getCanonicalID();

                if (canonID == null) {
                    _log.error("No CanonicalID found for " + srvType +
                            " after XRI resolution of: "
                            + identifier.getIdentifier());
                    return false;
                }

                claimedIdentifier = parseIdentifier(canonID.getValue());
                _log.info("Using canonicalID as claimedID: " +
                          claimedIdentifier.getIdentifier() +
                          " for " + srvType);
            }

            Iterator it = services.iterator();
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
