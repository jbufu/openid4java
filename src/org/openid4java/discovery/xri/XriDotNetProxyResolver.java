package org.openid4java.discovery.xri;

import com.google.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.XriIdentifier;
import org.openid4java.discovery.xrds.XrdsParser;
import org.openid4java.discovery.xrds.XrdsServiceEndpoint;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpFetcher;
import org.openid4java.util.HttpFetcherFactory;
import org.openid4java.util.HttpRequestOptions;
import org.openid4java.util.HttpResponse;
import org.openid4java.util.OpenID4JavaUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author jbufu
 */
public class XriDotNetProxyResolver implements XriResolver
{
    private static Log _log = LogFactory.getLog(XriDotNetProxyResolver.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private final HttpFetcher _httpFetcher;

    private final static String PROXY_URL = "https://xri.net/";
    private static final String XRDS_QUERY = "_xrd_r=application/xrds+xml";

    private static final String XRDS_PARSER_CLASS_NAME_KEY = "discovery.xrds.parser";
    private static final XrdsParser XRDS_PARSER;
    static {
        String className = OpenID4JavaUtils.getProperty(XRDS_PARSER_CLASS_NAME_KEY);
        if (DEBUG) _log.debug(XRDS_PARSER_CLASS_NAME_KEY + ":" + className);
        try
        {
            XRDS_PARSER = (XrdsParser) Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor for Guice installations. The default implementation
     * of the {@link HttpFetcherFactory} returns {@link HttpCache}s.
     */
    @Inject
    public XriDotNetProxyResolver(HttpFetcherFactory httpFetcherfactory) {
      _httpFetcher = httpFetcherfactory.createFetcher(
          HttpRequestOptions.getDefaultOptionsForDiscovery());
    }

    /**
     * Public constructor for non-guice installations. In this case,
     * we use the {@link HttpCache}-creating {@link HttpFetcherFactory}.
     */
    public XriDotNetProxyResolver()
    {
      this(new HttpFetcherFactory());
    }

    public List discover(XriIdentifier xri) throws DiscoveryException
    {
        String hxri = PROXY_URL + xri.getIdentifier() + "?" + XRDS_QUERY;
        _log.info("Performing discovery on HXRI: " + hxri);

        try
        {
            HttpResponse resp = _httpFetcher.get(hxri);
            if (resp == null || HttpStatus.SC_OK != resp.getStatusCode())
                throw new DiscoveryException("Error retrieving HXRI: " + hxri);

            Set targetTypes = DiscoveryInformation.OPENID_OP_TYPES;

            List endpoints = XRDS_PARSER.parseXrds(resp.getBody(), targetTypes);

            List results = new ArrayList();

            Iterator endpointIter = endpoints.iterator();
            while (endpointIter.hasNext())
            {
                XrdsServiceEndpoint endpoint = (XrdsServiceEndpoint) endpointIter.next();
                Iterator typesIter = endpoint.getTypes().iterator();
                while (typesIter.hasNext()) {
                    String type = (String) typesIter.next();
                    if (!targetTypes.contains(type)) continue;
                    try {
                        results.add(new DiscoveryInformation(
                            new URL(endpoint.getUri()),
                            parseIdentifier(endpoint.getCanonicalId()),
                            DiscoveryInformation.OPENID2.equals(type) ? endpoint.getLocalId() :
                            DiscoveryInformation.OPENID1_SIGNON_TYPES.contains(type) ? endpoint.getDelegate() : null,
                            type));
                    } catch (MalformedURLException e) {
                        throw new DiscoveryException("Invalid endpoint URL discovered: " + endpoint.getUri());
                    }
                }
            }
            return results;
        }
        catch (IOException e)
        {
            throw new DiscoveryException("Error performing discovery on HXRI: " + hxri);
        }
    }

    public XriIdentifier parseIdentifier(String identifier) throws DiscoveryException
    {
        // todo: http://code.google.com/p/openid4java/issues/detail?id=63
        _log.warn("Creating XRI identifier with the friendly XRI identifier as the IRI/URI normal forms.");
        return new XriIdentifier(identifier, identifier, identifier);
    }
}
