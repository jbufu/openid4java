/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import com.google.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.openid4java.OpenIDException;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.association.DiffieHellmanSession;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.*;
import org.openid4java.server.IncrementalNonceGenerator;
import org.openid4java.server.NonceGenerator;
import org.openid4java.server.RealmVerifier;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpFetcher;
import org.openid4java.util.HttpFetcherFactory;
import org.openid4java.util.HttpRequestOptions;
import org.openid4java.util.HttpResponse;

import javax.crypto.spec.DHParameterSpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Manages OpenID communications with an OpenID Provider (Server).
 * <p>
 * The Consumer site needs to have the same instance of this class throughout
 * the lifecycle of a OpenID authentication session.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ConsumerManager
{
    private static Log _log = LogFactory.getLog(ConsumerManager.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Discovery process manager.
     */
    private Discovery _discovery;

    /**
     * Direct pointer to HttpFetcher, for association and signature
     * verification requests.
     */
    private HttpFetcher _httpFetcher;

    /**
     * Store for keeping track of the established associations.
     */
    private ConsumerAssociationStore _associations = new InMemoryConsumerAssociationStore();

    /**
     * Consumer-side nonce generator, needed for compatibility with OpenID 1.1.
     */
    private NonceGenerator _consumerNonceGenerator = new IncrementalNonceGenerator();

    /**
     * Private association store used for signing consumer nonces when operating
     * in compatibility (v1.x) mode.
     */
    private ConsumerAssociationStore _privateAssociations = new InMemoryConsumerAssociationStore();

    /**
     * Verifier for the nonces in authentication responses;
     * prevents replay attacks.
     */
    private NonceVerifier _nonceVerifier = new InMemoryNonceVerifier(60);

    // --- association preferences ---

    /**
     * Maximum number of attmpts for establishing an association.
     */
    private int _maxAssocAttempts = 4;

    /**
     * Flag for enabling or disabling stateless mode.
     */
    private boolean _allowStateless = true;

    /**
     * The lowest encryption level session accepted for association sessions.
     */
    private AssociationSessionType _minAssocSessEnc
            = AssociationSessionType.NO_ENCRYPTION_SHA1MAC;

    /**
     * The preferred association session type; will be attempted first.
     */
    private AssociationSessionType _prefAssocSessEnc;

    /**
     * Parameters (modulus and generator) for the Diffie-Hellman sessions.
     */
    private DHParameterSpec _dhParams = DiffieHellmanSession.getDefaultParameter();

    /**
     * Timeout (in seconds) for keeping track of failed association attempts.
     * Default 5 minutes.
     */
    private int _failedAssocExpire = 300;

    /**
     * Interval before the expiration of an association (in seconds)
     * in which the association should not be used, in order to avoid
     * the expiration from occurring in the middle of an authentication
     * transaction. Default: 300s.
     */
    private int _preExpiryAssocLockInterval = 300;


    // --- authentication preferences ---

    /**
     * Flag for generating checkid_immediate authentication requests.
     */
    private boolean _immediateAuth = false;

    /**
     * Used to perform verify realms against return_to URLs.
     */
    private RealmVerifier _realmVerifier;

    /**
     * Instantiates a ConsumerManager with default settings.
     */
    public ConsumerManager()
    {
        this(
            new RealmVerifierFactory(new YadisResolver(new HttpFetcherFactory())),
            new Discovery(),  // uses HttpCache internally
            new HttpFetcherFactory());
    }

    @Inject
    public ConsumerManager(RealmVerifierFactory realmFactory, Discovery discovery,
        HttpFetcherFactory httpFetcherFactory)
    {
        _realmVerifier = realmFactory.getRealmVerifierForConsumer();
        // don't verify own (RP) identity, disable RP discovery
        _realmVerifier.setEnforceRpId(false);

        _discovery = discovery;
        _httpFetcher = httpFetcherFactory.createFetcher(HttpRequestOptions.getDefaultOptionsForOpCalls());

        if (Association.isHmacSha256Supported())
            _prefAssocSessEnc = AssociationSessionType.DH_SHA256;
        else
            _prefAssocSessEnc = AssociationSessionType.DH_SHA1;
    }

    /**
     * Returns discovery process manager.
     *
     * @return discovery process manager.
     */
    public Discovery getDiscovery()
    {
        return _discovery;
    }

    /**
     * Sets discovery process manager.
     *
     * @param discovery discovery process manager.
     */
    public void setDiscovery(Discovery discovery)
    {
        _discovery = discovery;
    }

    /**
     * Gets the association store that holds established associations with
     * OpenID providers.
     *
     * @see ConsumerAssociationStore
     */
    public ConsumerAssociationStore getAssociations()
    {
        return _associations;
    }

    /**
     * Configures the ConsumerAssociationStore that will be used to store the
     * associations established with OpenID providers.
     *
     * @param associations              ConsumerAssociationStore implementation
     * @see ConsumerAssociationStore
     */
    @Inject
    public void setAssociations(ConsumerAssociationStore associations)
    {
        this._associations = associations;
    }

    /**
     * @return the configured nonce generator for consumer nonces
     */
    public NonceGenerator getConsumerNonceGenerator() {
        return _consumerNonceGenerator;
    }

    /**
     * Sets the nonce generator to be used for consumer nonces.
     * Default implementation is a sequential/integer number generator, which may not be appropriate for cluster deployments.
     *
     * @param consumerNonceGenerator
     */
    public void setConsumerNonceGenerator(NonceGenerator consumerNonceGenerator) {
        this._consumerNonceGenerator = consumerNonceGenerator;
    }

    /**
     * Gets the NonceVerifier implementation used to keep track of the nonces
     * that have been seen in authentication response messages.
     *
     * @see NonceVerifier
     */
    public NonceVerifier getNonceVerifier()
    {
        return _nonceVerifier;
    }

    /**
     * Configures the NonceVerifier that will be used to keep track of the
     * nonces in the authentication response messages.
     *
     * @param nonceVerifier         NonceVerifier implementation
     * @see NonceVerifier
     */
    @Inject
    public void setNonceVerifier(NonceVerifier nonceVerifier)
    {
        this._nonceVerifier = nonceVerifier;
    }

    /**
     * Sets the Diffie-Hellman base parameters that will be used for encoding
     * the MAC key exchange.
     * <p>
     * If not provided the default set specified by the Diffie-Hellman algorithm
     * will be used.
     *
     * @param dhParams      Object encapsulating modulus and generator numbers
     * @see DHParameterSpec DiffieHellmanSession
     */
    public void setDHParams(DHParameterSpec dhParams)
    {
        this._dhParams = dhParams;
    }

    /**
     * Gets the Diffie-Hellman base parameters (modulus and generator).
     *
     * @see DHParameterSpec DiffieHellmanSession
     */
    public DHParameterSpec getDHParams()
    {
        return _dhParams;
    }

    /**
     * Maximum number of attempts (HTTP calls) the RP is willing to make
     * for trying to establish an association with the OP.
     *
     * Default: 4;
     * 0 = don't use associations
     *
     * Associations and stateless mode cannot be both disabled at the same time.
     */
    public void setMaxAssocAttempts(int maxAssocAttempts)
    {
        if (maxAssocAttempts > 0 || _allowStateless)
            this._maxAssocAttempts = maxAssocAttempts;
        else
            throw new IllegalArgumentException(
                    "Associations and stateless mode " +
                    "cannot be both disabled at the same time.");

        if (_maxAssocAttempts == 0) _log.info("Associations disabled.");
    }

    /**
     * Gets the value configured for the maximum number of association attempts
     * that will be performed for a given OpenID provider.
     * <p>
     * If an association cannot be established after this number of attempts the
     * ConsumerManager will fallback to stateless mode, provided the
     * #allowStateless preference is enabled.
     * <p>
     * See also: {@link #allowStateless(boolean)} {@link #statelessAllowed()}
     */
    public int getMaxAssocAttempts()
    {
        return _maxAssocAttempts;
    }

    /**
     * Flag used to enable / disable the use of stateless mode.
     * <p>
     * Default: enabled.
     * <p>
     * Associations and stateless mode cannot be both disabled at the same time.
     * @deprecated
     * @see #setAllowStateless(boolean)
     */
    public void allowStateless(boolean allowStateless)
    {
        setAllowStateless(allowStateless);
    }

    /**
     * Flag used to enable / disable the use of stateless mode.
     * <p>
     * Default: enabled.
     * <p>
     * Associations and stateless mode cannot be both disabled at the same time.
     */
    public void setAllowStateless(boolean allowStateless)
    {
        if (_allowStateless || _maxAssocAttempts > 0)
            this._allowStateless = allowStateless;
        else
            throw new IllegalArgumentException(
                    "Associations and stateless mode " +
                    "cannot be both disabled at the same time.");
    }

    /**
     * Returns true if the ConsumerManager is configured to fallback to
     * stateless mode when failing to associate with an OpenID Provider.
     *
     * @deprecated
     * @see #isAllowStateless()
     */
    public boolean statelessAllowed()
    {
        return _allowStateless;
    }

    /**
     * Returns true if the ConsumerManager is configured to fallback to
     * stateless mode when failing to associate with an OpenID Provider.
     */
    public boolean isAllowStateless()
    {
        return _allowStateless;
    }

    /**
     * Configures the minimum level of encryption accepted for association
     * sessions.
     * <p>
     * Default: no-encryption session, SHA1 MAC association.
     * <p>
     * See also: {@link #allowStateless(boolean)}
     */
    public void setMinAssocSessEnc(AssociationSessionType minAssocSessEnc)
    {
        this._minAssocSessEnc = minAssocSessEnc;
    }

    /**
     * Gets the minimum level of encryption that will be accepted for
     * association sessions.
     * <p>
     * Default: no-encryption session, SHA1 MAC association
     * <p>
     */
    public AssociationSessionType getMinAssocSessEnc()
    {
        return _minAssocSessEnc;
    }

    /**
     * Sets the preferred encryption type for the association sessions.
     * <p>
     * Default: DH-SHA256
     */
    public void setPrefAssocSessEnc(AssociationSessionType prefAssocSessEnc)
    {
        this._prefAssocSessEnc = prefAssocSessEnc;
    }

    /**
     * Gets the preferred encryption type for the association sessions.
     */
    public AssociationSessionType getPrefAssocSessEnc()
    {
        return _prefAssocSessEnc;
    }

    /**
     * Sets the expiration timeout (in seconds) for keeping track of failed
     * association attempts.
     * <p>
     * If an association cannot be establish with an OP, subsequesnt
     * authentication request to that OP will not try to establish an
     * association within the timeout period configured here.
     * <p>
     * Default: 300s
     * 0 = disabled (attempt to establish an association with every
     *               authentication request)
     *
     * @param _failedAssocExpire    time in seconds to remember failed
     *                              association attempts
     */
    public void setFailedAssocExpire(int _failedAssocExpire)
    {
        this._failedAssocExpire = _failedAssocExpire;
    }

    /**
     * Gets the timeout (in seconds) configured for keeping track of failed
     * association attempts.
     * <p>
     * See also: {@link #setFailedAssocExpire(int)}
     */
    public int getFailedAssocExpire()
    {
        return _failedAssocExpire;
    }

    /**
     * Gets the interval before the expiration of an association
     * (in seconds) in which the association should not be used,
     * in order to avoid the expiration from occurring in the middle
     * of a authentication transaction. Default: 300s.
     */
    public int getPreExpiryAssocLockInterval()
    {
        return _preExpiryAssocLockInterval;
    }

    /**
     * Sets the interval before the expiration of an association
     * (in seconds) in which the association should not be used,
     * in order to avoid the expiration from occurring in the middle
     * of a authentication transaction. Default: 300s.
     *
     * @param preExpiryAssocLockInterval    The number of seconds for the
     *                                      pre-expiry lock inteval.
     */
    public void setPreExpiryAssocLockInterval(int preExpiryAssocLockInterval)
    {
        this._preExpiryAssocLockInterval = preExpiryAssocLockInterval;
    }

    /**
     * Configures the authentication request mode:
     * checkid_immediate (true) or checkid_setup (false).
     * <p>
     * Default: false / checkid_setup
     */
    public void setImmediateAuth(boolean _immediateAuth)
    {
        this._immediateAuth = _immediateAuth;
    }

    /**
     * Returns true if the ConsumerManager is configured to attempt
     * checkid_immediate authentication requests.
     * <p>
     * Default: false
     */
    public boolean isImmediateAuth()
    {
        return _immediateAuth;
    }

    /**
     * Gets the RealmVerifier used to verify realms against return_to URLs.
     */
    public RealmVerifier getRealmVerifier()
    {
        return _realmVerifier;
    }

    /**
     * Sets the RealmVerifier used to verify realms against return_to URLs.
     */
    public void setRealmVerifier(RealmVerifier realmVerifier)
    {
        this._realmVerifier = realmVerifier;
    }

    /**
     * Gets the max age (in seconds) configured for keeping track of nonces.
     * <p>
     * Nonces older than the max age will be removed from the store and
     * authentication responses will be considered failures.
     */
    public int getMaxNonceAge()
    {
        return _nonceVerifier.getMaxAge();
    }

    /**
     * Sets the max age (in seconds) configured for keeping track of nonces.
     * <p>
     * Nonces older than the max age will be removed from the store and
     * authentication responses will be considered failures.
     */
    public void setMaxNonceAge(int ageSeconds)
    {
        _nonceVerifier.setMaxAge(ageSeconds);
    }

    /**
     * Does discovery on an identifier. It delegates the call to its
     * discovery manager.
     *
     * @return      A List of {@link DiscoveryInformation} objects.
     *              The list could be empty if no discovery information can
     *              be retrieved.
     *
     * @throws DiscoveryException if the discovery process runs into errors.
     */
    public List discover(String identifier) throws DiscoveryException
    {
        return _discovery.discover(identifier);
    }

    /**
     * Configures a private association store for signing consumer nonces.
     * <p>
     * Consumer nonces are needed to prevent replay attacks in compatibility
     * mode, because OpenID 1.x Providers to not attach nonces to
     * authentication responses.
     * <p>
     * One way for the Consumer to know that a consumer nonce in an
     * authentication response was indeed issued by itself (and thus prevent
     * denial of service attacks), is by signing them.
     *
     * @param associations     The association store to be used for signing consumer nonces;
     *                  signing can be deactivated by setting this to null.
     *                  Signing is enabled by default.
     */
    public void setPrivateAssociationStore(ConsumerAssociationStore associations)
            throws ConsumerException
    {
        if (associations == null)
            throw new ConsumerException(
                    "Cannot set null private association store, " +
                    "needed for consumer nonces.");

        _privateAssociations = associations;
    }

    /**
     * Gets the private association store used for signing consumer nonces.
     *
     * @see #setPrivateAssociationStore(ConsumerAssociationStore)
     */
    public ConsumerAssociationStore getPrivateAssociationStore()
    {
        return _privateAssociations;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        _httpFetcher.getDefaultRequestOptions()
                .setConnTimeout(connectTimeout);
    }

    public void setSocketTimeout(int socketTimeout)
    {
        _httpFetcher.getDefaultRequestOptions()
                .setSocketTimeout(socketTimeout);
    }

    public void setMaxRedirects(int maxRedirects)
    {
        _httpFetcher.getDefaultRequestOptions()
                .setMaxRedirects(maxRedirects);
    }

    /**
     * Makes a HTTP call to the specified URL with the parameters specified
     * in the Message.
     *
     * @param url       URL endpoint for the HTTP call
     * @param request   Message containing the parameters
     * @param response  ParameterList that will hold the parameters received in
     *                  the HTTP response
     * @return          the status code of the HTTP call
     */
    private int call(String url, Message request, ParameterList response)
            throws MessageException
    {
        int responseCode = -1;

        try
        {

            if (DEBUG) _log.debug("Performing HTTP POST on " + url);
            HttpResponse resp = _httpFetcher.post(url, request.getParameterMap());
            responseCode = resp.getStatusCode();

            String postResponse = resp.getBody();
            response.copyOf(ParameterList.createFromKeyValueForm(postResponse));

            if (DEBUG) _log.debug("Retrived response:\n" + postResponse);
        }
        catch (IOException e)
        {
            _log.error("Error talking to " + url +
                    " response code: " + responseCode, e);
        }

        return responseCode;
    }

    /**
     * Tries to establish an association with on of the service endpoints in
     * the list of DiscoveryInformation.
     * <p>
     * Iterates over the items in the discoveries parameter a maximum of
     * #_maxAssocAttempts times trying to esablish an association.
     *
     * @param discoveries       The DiscoveryInformation list obtained by
     *                          performing dicovery on the User-supplied OpenID
     *                          identifier. Should be ordered by the priority
     *                          of the service endpoints.
     * @return                  The DiscoveryInformation instance with which
     *                          an association was established, or the one
     *                          with the highest priority if association failed.
     *
     * @see Discovery#discover(org.openid4java.discovery.Identifier)
     */
    public DiscoveryInformation associate(List discoveries)
    {
        DiscoveryInformation discovered;
        Association assoc;

        int attemptsLeft = _maxAssocAttempts;
        Iterator itr = discoveries.iterator();
        while (itr.hasNext() && attemptsLeft > 0)
        {
            discovered = (DiscoveryInformation) itr.next();
            attemptsLeft -= associate(discovered, attemptsLeft);

            // check if an association was established
            assoc = _associations.load(discovered.getOPEndpoint().toString());

            if ( assoc != null &&
                    ! Association.FAILED_ASSOC_HANDLE.equals(assoc.getHandle()))
                return discovered;
        }

        if (discoveries.size() > 0)
        {
            // no association established, return the first service endpoint
            DiscoveryInformation d0 = (DiscoveryInformation) discoveries.get(0);
            _log.warn("Association failed; using first entry: " +
                      d0.getOPEndpoint());

            return d0;
        }
        else
        {
            _log.error("Association attempt, but no discovery endpoints provided.");
            return null;
        }
    }

    /**
     * Tries to establish an association with the OpenID Provider.
     * <p>
     * The resulting association information will be kept on storage for later
     * use at verification stage. If there exists an association for the opUrl
     * that is not near expiration, will not construct new association.
     *
     * @param discovered    DiscoveryInformation obtained during the discovery
     * @return              The number of association attempts performed.
     */
    private int associate(DiscoveryInformation discovered, int maxAttempts)
    {
        if (_maxAssocAttempts == 0) return 0; // associations disabled

        URL opUrl = discovered.getOPEndpoint();
        String opEndpoint = opUrl.toString();

        _log.info("Trying to associate with " + opEndpoint +
                " attempts left: " + maxAttempts);

        // check if there's an already established association
        Association a = _associations.load(opEndpoint);
        if ( a != null &&
                (Association.FAILED_ASSOC_HANDLE.equals(a.getHandle()) ||
                a.getExpiry().getTime() - System.currentTimeMillis() > _preExpiryAssocLockInterval * 1000) )
        {
            _log.info("Found an existing association: " + a.getHandle());
            return 0;
        }

        String handle = Association.FAILED_ASSOC_HANDLE;

        // build a list of association types, with the preferred one at the end
        LinkedHashMap requests = new LinkedHashMap();

        if (discovered.isVersion2())
        {
            requests.put(AssociationSessionType.NO_ENCRYPTION_SHA1MAC, null);
            requests.put(AssociationSessionType.NO_ENCRYPTION_SHA256MAC, null);
            requests.put(AssociationSessionType.DH_SHA1, null);
            requests.put(AssociationSessionType.DH_SHA256, null);
        }
        else
        {
            requests.put(AssociationSessionType.NO_ENCRYPTION_COMPAT_SHA1MAC, null);
            requests.put(AssociationSessionType.DH_COMPAT_SHA1, null);
        }

        if (_prefAssocSessEnc.isVersion2() == discovered.isVersion2()) {
        	requests.remove(_prefAssocSessEnc);
            requests.put(_prefAssocSessEnc, null);
        }

        // build a stack of Association Request objects
        // and keep only the allowed by the configured preferences
        // the most-desirable entry is always at the top of the stack
        Stack reqStack = new Stack();
        Iterator iter = requests.keySet().iterator();
        while(iter.hasNext())
        {
            AssociationSessionType type = (AssociationSessionType) iter.next();

            // create the appropriate Association Request
            AssociationRequest newReq = createAssociationRequest(type, opUrl);
            if (newReq != null) reqStack.push(newReq);
        }

        // perform the association attempts
        int attemptsLeft = maxAttempts;
        LinkedHashMap alreadyTried = new LinkedHashMap();
        while (attemptsLeft > 0 && ! reqStack.empty())
        {
            try
            {
                attemptsLeft--;
                AssociationRequest assocReq =
                        (AssociationRequest) reqStack.pop();

                if (DEBUG)
                    _log.debug("Trying association type: " + assocReq.getType());

                // was this association / session type attempted already?
                if (alreadyTried.keySet().contains(assocReq.getType()))
                {
                    if (DEBUG) _log.debug("Already tried.");
                    continue;
                }

                // mark the current request type as already tried
                alreadyTried.put(assocReq.getType(), null);

                ParameterList respParams = new ParameterList();
                int status = call(opEndpoint, assocReq, respParams);

                // process the response
                if (status == HttpStatus.SC_OK) // success response
                {
                    AssociationResponse assocResp;

                    assocResp = AssociationResponse
                            .createAssociationResponse(respParams);

                    // valid association response
                    Association assoc =
                            assocResp.getAssociation(assocReq.getDHSess());
                    handle = assoc.getHandle();

                    AssociationSessionType respType = assocResp.getType();
                    if ( respType.equals(assocReq.getType()) ||
                            // v1 OPs may return a success no-encryption resp
                            ( ! discovered.isVersion2() &&
                              respType.getHAlgorithm() == null &&
                              createAssociationRequest(respType,opUrl) != null))
                    {
                        // store the association and do no try alternatives
                        _associations.save(opEndpoint, assoc);
                        _log.info("Associated with " + discovered.getOPEndpoint()
                                + " handle: " + assoc.getHandle());
                        break;
                    }
                    else
                        _log.info("Discarding association response, " +
                                  "not matching consumer criteria");
                }
                else if (status == HttpStatus.SC_BAD_REQUEST) // error response
                {
                    _log.info("Association attempt failed.");

                    // retrieve fallback sess/assoc/encryption params set by OP
                    // and queue a new attempt
                    AssociationError assocErr =
                            AssociationError.createAssociationError(respParams);

                    AssociationSessionType opType =
                            AssociationSessionType.create(
                                    assocErr.getSessionType(),
                                    assocErr.getAssocType());

                    if (alreadyTried.keySet().contains(opType))
                        continue;

                    // create the appropriate Association Request
                    AssociationRequest newReq =
                            createAssociationRequest(opType, opUrl);

                    if (newReq != null)
                    {
                        if (DEBUG) _log.debug("Retrieved association type " +
                                              "from the association error: " +
                                              newReq.getType());

                        reqStack.push(newReq);
                    }
                }
            }
            catch (OpenIDException e)
            {
                _log.error("Error encountered during association attempt.", e);
            }
        }

        // store OPs with which an association could not be established
        // so that association attempts are not performed with each auth request
        if (Association.FAILED_ASSOC_HANDLE.equals(handle)
                && _failedAssocExpire > 0)
            _associations.save(opEndpoint,
                    Association.getFailedAssociation(_failedAssocExpire));

        return maxAttempts - attemptsLeft;
    }

    /**
     * Constructs an Association Request message of the specified session and
     * association type, taking into account the user preferences (encryption
     * level, default Diffie-Hellman parameters).
     *
     * @param type      The type of the association (session and association)
     * @param opUrl    The OP for which the association request is created
     * @return          An AssociationRequest message ready to be sent back
     *                  to the OpenID Provider, or null if an association
     *                  of the requested type cannot be built.
     */
    private AssociationRequest createAssociationRequest(
            AssociationSessionType type, URL opUrl)
    {
        try
        {
            if (_minAssocSessEnc.isBetter(type))
                return null;

            AssociationRequest assocReq = null;

            DiffieHellmanSession dhSess;
            if (type.getHAlgorithm() != null) // DH session
            {
                dhSess = DiffieHellmanSession.create(type, _dhParams);
                if (DiffieHellmanSession.isDhSupported(type)
                    && Association.isHmacSupported(type.getAssociationType()))
                    assocReq = AssociationRequest.createAssociationRequest(type, dhSess);
            }

            else if ( opUrl.getProtocol().equals("https") && // no-enc sess
                     Association.isHmacSupported(type.getAssociationType()))
                    assocReq = AssociationRequest.createAssociationRequest(type);

            if (assocReq == null)
                _log.warn("Could not create association of type: " + type);

            return assocReq;
        }
        catch (OpenIDException e)
        {
            _log.error("Error trying to create association request.", e);
            return null;
        }
    }

    /**
     * Builds a authentication request message for the user specified in the
     * discovery information provided as a parameter.
     * <p>
     * If the discoveries parameter contains more than one entry, it will
     * iterate over them trying to establish an association. If an association
     * cannot be established, the first entry is used with stateless mode.
     *
     * @see #associate(java.util.List)
     * @param discoveries       The DiscoveryInformation list obtained by
     *                          performing dicovery on the User-supplied OpenID
     *                          identifier. Should be ordered by the priority
     *                          of the service endpoints.
     * @param returnToUrl       The URL on the Consumer site where the OpenID
     *                          Provider will return the user after generating
     *                          the authentication response. <br>
     *                          Null if the Consumer does not with to for the
     *                          End User to be returned to it (something else
     *                          useful will have been performed via an
     *                          extension). <br>
     *                          Must not be null in OpenID 1.x compatibility
     *                          mode.
     * @return                  Authentication request message to be sent to the
     *                          OpenID Provider.
     */
    public AuthRequest authenticate(List discoveries,
                                    String returnToUrl)
            throws ConsumerException, MessageException
    {
        return authenticate(discoveries, returnToUrl, returnToUrl);
    }


    /**
     * Builds a authentication request message for the user specified in the
     * discovery information provided as a parameter.
     * <p>
     * If the discoveries parameter contains more than one entry, it will
     * iterate over them trying to establish an association. If an association
     * cannot be established, the first entry is used with stateless mode.
     *
     * @see #associate(java.util.List)
     * @param discoveries       The DiscoveryInformation list obtained by
     *                          performing dicovery on the User-supplied OpenID
     *                          identifier. Should be ordered by the priority
     *                          of the service endpoints.
     * @param returnToUrl       The URL on the Consumer site where the OpenID
     *                          Provider will return the user after generating
     *                          the authentication response. <br>
     *                          Null if the Consumer does not with to for the
     *                          End User to be returned to it (something else
     *                          useful will have been performed via an
     *                          extension). <br>
     *                          Must not be null in OpenID 1.x compatibility
     *                          mode.
     * @param realm             The URL pattern that will be presented to the
     *                          user when he/she will be asked to authorize the
     *                          authentication transaction. Must be a super-set
     *                          of the @returnToUrl.
     * @return                  Authentication request message to be sent to the
     *                          OpenID Provider.
     */
    public AuthRequest authenticate(List discoveries,
                                    String returnToUrl, String realm)
            throws ConsumerException, MessageException
    {
        // try to associate with one OP in the discovered list
        DiscoveryInformation discovered = associate(discoveries);

        return authenticate(discovered, returnToUrl, realm);
    }

    /**
     * Builds a authentication request message for the user specified in the
     * discovery information provided as a parameter.
     *
     * @param discovered        A DiscoveryInformation endpoint from the list
     *                          obtained by performing dicovery on the
     *                          User-supplied OpenID identifier.
     * @param returnToUrl       The URL on the Consumer site where the OpenID
     *                          Provider will return the user after generating
     *                          the authentication response. <br>
     *                          Null if the Consumer does not with to for the
     *                          End User to be returned to it (something else
     *                          useful will have been performed via an
     *                          extension). <br>
     *                          Must not be null in OpenID 1.x compatibility
     *                          mode.
     * @return                  Authentication request message to be sent to the
     *                          OpenID Provider.
     */
    public AuthRequest authenticate(DiscoveryInformation discovered,
                                    String returnToUrl)
            throws MessageException, ConsumerException
    {
        return authenticate(discovered, returnToUrl, returnToUrl);
    }

    /**
     * Builds a authentication request message for the user specified in the
     * discovery information provided as a parameter.
     *
     * @param discovered        A DiscoveryInformation endpoint from the list
     *                          obtained by performing dicovery on the
     *                          User-supplied OpenID identifier.
     * @param returnToUrl       The URL on the Consumer site where the OpenID
     *                          Provider will return the user after generating
     *                          the authentication response. <br>
     *                          Null if the Consumer does not with to for the
     *                          End User to be returned to it (something else
     *                          useful will have been performed via an
     *                          extension). <br>
     *                          Must not be null in OpenID 1.x compatibility
     *                          mode.
     * @param realm             The URL pattern that will be presented to the
     *                          user when he/she will be asked to authorize the
     *                          authentication transaction. Must be a super-set
     *                          of the @returnToUrl.
     * @return                  Authentication request message to be sent to the
     *                          OpenID Provider.
     */
    public AuthRequest authenticate(DiscoveryInformation discovered,
                                    String returnToUrl, String realm)
            throws MessageException, ConsumerException
    {
        if (discovered == null)
            throw new ConsumerException("Authentication cannot continue: " +
                    "no discovery information provided.");

        Association assoc =
                _associations.load(discovered.getOPEndpoint().toString());

        if (assoc == null)
        {
            associate(discovered, _maxAssocAttempts);
            assoc = _associations.load(discovered.getOPEndpoint().toString());
        }

        String handle = assoc != null ?
                assoc.getHandle() : Association.FAILED_ASSOC_HANDLE;

        // get the Claimed ID and Delegate ID (aka OP-specific identifier)
        String claimedId, delegate;
        if (discovered.hasClaimedIdentifier())
        {
            claimedId = discovered.getClaimedIdentifier().getIdentifier();
            delegate = discovered.hasDelegateIdentifier() ?
                       discovered.getDelegateIdentifier() : claimedId;
        }
        else
        {
            claimedId = AuthRequest.SELECT_ID;
            delegate = AuthRequest.SELECT_ID;
        }

        // stateless mode disabled ?
        if ( !_allowStateless && Association.FAILED_ASSOC_HANDLE.equals(handle))
            throw new ConsumerException("Authentication cannot be performed: " +
                    "no association available and stateless mode is disabled");

        _log.info("Creating authentication request for" +
                " OP-endpoint: " + discovered.getOPEndpoint() +
                " claimedID: " + claimedId +
                " OP-specific ID: " + delegate);

        if (! discovered.isVersion2())
            returnToUrl = insertConsumerNonce(discovered.getOPEndpoint().toString(), returnToUrl);

        AuthRequest authReq = AuthRequest.createAuthRequest(claimedId, delegate,
                ! discovered.isVersion2(), returnToUrl, handle, realm, _realmVerifier);

        authReq.setOPEndpoint(discovered.getOPEndpoint());

        // ignore the immediate flag for OP-directed identifier selection
        if (! AuthRequest.SELECT_ID.equals(claimedId))
            authReq.setImmediate(_immediateAuth);

        return authReq;
    }

    /**
     * Performs verification on the Authentication Response (assertion)
     * received from the OpenID Provider.
     * <p>
     * Three verification steps are performed:
     * <ul>
     * <li> nonce:                  the same assertion will not be accepted more
     *                              than once
     * <li> signatures:             verifies that the message was indeed sent
     *                              by the OpenID Provider that was contacted
     *                              earlier after discovery
     * <li> discovered information: the information contained in the assertion
     *                              matches the one obtained during the
     *                              discovery (the OpenID Provider is
     *                              authoritative for the claimed identifier;
     *                              the received assertion is not meaningful
     *                              otherwise
     * </ul>
     *
     * @param receivingUrl  The URL where the Consumer (Relying Party) has
     *                      accepted the incoming message.
     * @param response      ParameterList of the authentication response
     *                      being verified.
     * @param discovered    Previously discovered information (which can
     *                      therefore be trusted) obtained during the discovery
     *                      phase; this should be stored and retrieved by the RP
     *                      in the user's session.
     *
     * @return              A VerificationResult, containing a verified
     *                      identifier; the verified identifier is null if
     *                      the verification failed).
     */
    public VerificationResult verify(String receivingUrl,
                                     ParameterList response,
                                     DiscoveryInformation discovered)
            throws MessageException, DiscoveryException, AssociationException
    {
        VerificationResult result = new VerificationResult();
        _log.info("Verifying authentication response...");

        // non-immediate negative response
        if ( "cancel".equals(response.getParameterValue("openid.mode")) )
        {
            result.setAuthResponse(AuthFailure.createAuthFailure(response));
            _log.info("Received auth failure.");
            return result;
        }

        // immediate negative response
        if ( "setup_needed".equals(response.getParameterValue("openid.mode")) ||
                ("id_res".equals(response.getParameterValue("openid.mode"))
                && response.hasParameter("openid.user_setup_url") ) )
        {
            AuthImmediateFailure fail =
                    AuthImmediateFailure.createAuthImmediateFailure(response);
            result.setAuthResponse(fail);
            result.setOPSetupUrl(fail.getUserSetupUrl());
            _log.info("Received auth immediate failure.");
            return result;
        }

        AuthSuccess authResp = AuthSuccess.createAuthSuccess(response);
        _log.info("Received positive auth response.");

        result.setAuthResponse(authResp);

        // [1/4] return_to verification
        if (! verifyReturnTo(receivingUrl, authResp))
        {
            result.setStatusMsg("Return_To URL verification failed.");
            _log.error("Return_To URL verification failed.");
            return result;
        }

        // [2/4] : discovered info verification
        discovered = verifyDiscovered(authResp, discovered);
        if (discovered == null || ! discovered.hasClaimedIdentifier())
        {
            result.setStatusMsg("Discovered information verification failed.");
            _log.error("Discovered information verification failed.");
            return result;
        }

        // [3/4] : nonce verification
        if (! verifyNonce(authResp, discovered))
        {
            result.setStatusMsg("Nonce verification failed.");
            _log.error("Nonce verification failed.");
            return result;
        }

        // [4/4] : signature verification
        return (verifySignature(authResp, discovered, result));
    }

    /**
     * Verifies that the URL where the Consumer (Relying Party) received the
     * authentication response matches the value of the "openid.return_to"
     * parameter in the authentication response.
     *
     * @param receivingUrl      The URL where the Consumer received the
     *                          authentication response.
     * @param response          The authentication response.
     * @return                  True if the two URLs match, false otherwise.
     */
    public boolean verifyReturnTo(String receivingUrl, AuthSuccess response)
    {
        if (DEBUG)
            _log.debug("Verifying return URL; receiving: " + receivingUrl +
                    "\nmessage: " + response.getReturnTo());

        URL receiving;
        URL returnTo;
        try
        {
            receiving = new URL(receivingUrl);
            returnTo = new URL(response.getReturnTo());
        }
        catch (MalformedURLException e)
        {
            _log.error("Invalid return URL.", e);
            return false;
        }

        // [1/2] schema, authority (includes port) and path

        // deal manually with the trailing slash in the path
        StringBuffer receivingPath = new StringBuffer(receiving.getPath());
        if ( receivingPath.length() > 0 &&
                receivingPath.charAt(receivingPath.length() -1) != '/')
            receivingPath.append('/');

        StringBuffer returnToPath = new StringBuffer(returnTo.getPath());
        if ( returnToPath.length() > 0 &&
                returnToPath.charAt(returnToPath.length() -1) != '/')
            returnToPath.append('/');

        if ( ! receiving.getProtocol().equals(returnTo.getProtocol()) ||
                ! receiving.getAuthority().equals(returnTo.getAuthority()) ||
                ! receivingPath.toString().equals(returnToPath.toString()) )
        {
            if (DEBUG)
                _log.debug("Return URL schema, authority or " +
                           "path verification failed.");
            return false;
        }

        // [2/2] query parameters
        try
        {
            Map returnToParams = extractQueryParams(returnTo);
            Map receivingParams = extractQueryParams(receiving);

            if (returnToParams == null) return true;

            if (receivingParams == null)
            {
                if (DEBUG)
                    _log.debug("Return URL query parameters verification failed.");
                return false;
            }

            Iterator iter = returnToParams.keySet().iterator();
            while (iter.hasNext())
            {
                String key = (String) iter.next();
                List receivingValues = (List) receivingParams.get(key);
                List returnToValues = (List) returnToParams.get(key);

                if ( receivingValues == null ||
                        receivingValues.size() != returnToValues.size() ||
                        ! receivingValues.containsAll( returnToValues ) )
                {
                    if (DEBUG)
                        _log.debug("Return URL query parameters verification failed.");
                    return false;
                }
            }
        }
        catch (UnsupportedEncodingException e)
        {
            _log.error("Error verifying return URL query parameters.", e);
            return false;
        }

        return true;
    }

    /**
     * Returns a Map(key, List(values)) with the URL's query params, or null if
     * the URL doesn't have a query string.
     */
    public Map extractQueryParams(URL url) throws UnsupportedEncodingException
    {
        if (url.getQuery() == null) return null;

        Map paramsMap = new HashMap();

        List paramList = Arrays.asList(url.getQuery().split("&"));

        Iterator iter = paramList.iterator();
        while (iter.hasNext())
        {
            String keyValue = (String) iter.next();
            int equalPos = keyValue.indexOf("=");

            String key = equalPos > -1 ?
                    URLDecoder.decode(keyValue.substring(0, equalPos), "UTF-8") :
                    URLDecoder.decode(keyValue, "UTF-8");
            String value;
            if (equalPos <= -1)
                value = null;
            else if (equalPos + 1 > keyValue.length())
                value = "";
            else
                value = URLDecoder.decode(keyValue.substring(equalPos + 1), "UTF-8");

            List existingValues = (List) paramsMap.get(key);
            if (existingValues == null)
            {
                List newValues = new ArrayList();
                newValues.add(value);
                paramsMap.put(key, newValues);
            }
            else
                existingValues.add(value);
        }

        return paramsMap;
    }

    /**
     * Verifies the nonce in an authentication response.
     *
     * @param authResp      The authentication response containing the nonce
     *                      to be verified.
     * @param discovered    The discovery information associated with the
     *                      authentication transaction.
     * @return              True if the nonce is valid, false otherwise.
     */
    public boolean verifyNonce(AuthSuccess authResp,
                               DiscoveryInformation discovered)
    {
        String nonce = authResp.getNonce();

        if (nonce == null) // compatibility mode
            nonce = extractConsumerNonce(authResp.getReturnTo(),
                    discovered.getOPEndpoint().toString());

        if (nonce == null) return false;

        // using the same nonce verifier for both server and consumer nonces
        return (NonceVerifier.OK == _nonceVerifier.seen(
                discovered.getOPEndpoint().toString(), nonce));
    }

    /**
     * Inserts a consumer-side nonce as a custom parameter in the return_to
     * parameter of the authentication request.
     * <p>
     * Needed for preventing replay attack when running compatibility mode.
     * OpenID 1.1 OpenID Providers do not generate nonces in authentication
     * responses.
     *
     * @param opUrl             The endpoint to be used for private association.
     * @param returnTo          The return_to URL to which a custom nonce
     *                          parameter will be added.
     * @return                  The return_to URL containing the nonce.
     */
    public String insertConsumerNonce(String opUrl, String returnTo)
    {
        String nonce = _consumerNonceGenerator.next();

        returnTo += (returnTo.indexOf('?') != -1) ? '&' : '?';

        Association privateAssoc = _privateAssociations.load(opUrl);
        if( privateAssoc == null )
        {
			try
			{
				if (DEBUG) _log.debug( "Creating private association for opUrl " + opUrl);
				privateAssoc = Association.generate(
				      getPrefAssocSessEnc().getAssociationType(), "", _failedAssocExpire);
				_privateAssociations.save( opUrl, privateAssoc );
			}
			catch ( AssociationException e )
			{
				_log.error("Cannot initialize private association.", e);
				return null;
			}
        }

        try
        {
            returnTo += "openid.rpnonce=" + URLEncoder.encode(nonce, "UTF-8");

            returnTo += "&openid.rpsig=" +
                    URLEncoder.encode(privateAssoc.sign(returnTo),
                            "UTF-8");

            _log.info("Inserted consumer nonce: " + nonce);

            if (DEBUG) _log.debug("return_to:" + returnTo);
        }
        catch (Exception e)
        {
            _log.error("Error inserting consumre nonce.", e);
            return null;
        }

        return returnTo;
    }

    /**
     * Extracts the consumer-side nonce from the return_to parameter in
     * authentication response from a OpenID 1.1 Provider.
     *
     * @param returnTo      return_to URL from the authentication response
     * @param opUrl         URL for the appropriate OP endpoint
     * @return              The nonce found in the return_to URL, or null if
     *                      it wasn't found.
     */
    public String extractConsumerNonce(String returnTo, String opUrl)
    {
        if (DEBUG)
            _log.debug("Extracting consumer nonce...");

        String nonce = null;
        String signature = null;

        URL returnToUrl;
        try
        {
            returnToUrl = new URL(returnTo);
        }
        catch (MalformedURLException e)
        {
            _log.error("Invalid return_to: " + returnTo, e);
            return null;
        }

        String query = returnToUrl.getQuery();
        if (query == null) {
            _log.error("Missing nonce in return_to query parameters, required for v1 responses");
            return null;
        }

        String[] params = query.split("&");

        for (int i=0; i < params.length; i++)
        {
            String keyVal[] = params[i].split("=", 2);

            try
            {
                if (keyVal.length == 2 && "openid.rpnonce".equals(keyVal[0]))
                {
                    nonce = URLDecoder.decode(keyVal[1], "UTF-8");
                    if (DEBUG) _log.debug("Extracted consumer nonce: " + nonce);
                }

                if (keyVal.length == 2 && "openid.rpsig".equals(keyVal[0]))
                {
                    signature = URLDecoder.decode(keyVal[1], "UTF-8");
                    if (DEBUG) _log.debug("Extracted consumer nonce signature: "
                                          + signature);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                _log.error("Error extracting consumer nonce / signarure.", e);
                return null;
            }
        }

        // check the signature
        if (signature == null)
        {
            _log.error("Null consumer nonce signature.");
            return null;
        }

        String signed = returnTo.substring(0, returnTo.indexOf("&openid.rpsig="));
        if (DEBUG) _log.debug("Consumer signed text:\n" + signed);

        try
        {
            if (DEBUG) _log.debug( "Loading private association for opUrl " + opUrl );
            Association privateAssoc = _privateAssociations.load(opUrl);
            if( privateAssoc == null )
            {
                _log.error("Null private association.");
                return null;
            }

            if (privateAssoc.verifySignature(signed, signature))
            {
                _log.info("Consumer nonce signature verified.");
                return nonce;
            }

            else
            {
                _log.error("Consumer nonce signature failed.");
                return null;
            }
        }
        catch (AssociationException e)
        {
            _log.error("Error verifying consumer nonce signature.", e);
            return null;
        }
    }

    /**
     * Verifies the dicovery information matches the data received in a
     * authentication response from an OpenID Provider.
     *
     * @param authResp      The authentication response to be verified.
     * @param discovered    The discovery information obtained earlier during
     *                      the discovery stage, associated with the
     *                      identifier(s) in the request. Stateless operation
     *                      is assumed if null.
     * @return              The discovery information associated with the
     *                      claimed identifier, that can be used further in
     *                      the verification process. Null if the discovery
     *                      on the claimed identifier does not match the data
     *                      in the assertion.
     */
    private DiscoveryInformation verifyDiscovered(AuthSuccess authResp,
                                        DiscoveryInformation discovered)
            throws DiscoveryException
    {
        if (authResp == null || authResp.getIdentity() == null)
        {
            _log.info("Assertion is not about an identifier");
            return null;
        }

        if (authResp.isVersion2())
            return verifyDiscovered2(authResp, discovered);
        else
            return verifyDiscovered1(authResp, discovered);
    }

    /**
     * Verifies the discovered information associated with a OpenID 1.x
     * response.
     *
     * @param authResp      The authentication response to be verified.
     * @param discovered    The discovery information obtained earlier during
     *                      the discovery stage, associated with the
     *                      identifier(s) in the request. Stateless operation
     *                      is assumed if null.
     * @return              The discovery information associated with the
     *                      claimed identifier, that can be used further in
     *                      the verification process. Null if the discovery
     *                      on the claimed identifier does not match the data
     *                      in the assertion.
     */
    private DiscoveryInformation verifyDiscovered1(AuthSuccess authResp,
                                        DiscoveryInformation discovered)
            throws DiscoveryException
    {
        if ( authResp == null || authResp.isVersion2() ||
             authResp.getIdentity() == null )
        {
            if (DEBUG)
                _log.error("Invalid authentication response: " +
                           "cannot verify v1 discovered information");
            return null;
        }

        // asserted identifier in the AuthResponse
        String assertId = authResp.getIdentity();

        if ( discovered != null && ! discovered.isVersion2() &&
             discovered.getClaimedIdentifier() != null )
        {
            // statefull mode
            if (DEBUG)
                _log.debug("Verifying discovered information " +
                           "for OpenID1 assertion about ClaimedID: " +
                           discovered.getClaimedIdentifier().getIdentifier());

            String discoveredId = discovered.hasDelegateIdentifier() ?
                discovered.getDelegateIdentifier() :
                discovered.getClaimedIdentifier().getIdentifier();

            if (assertId.equals(discoveredId))
                return discovered;
        }

        // stateless, bare response, or the user changed the ID at the OP
        _log.info("Proceeding with stateless mode / bare response verification...");

        DiscoveryInformation firstServiceMatch = null;

        // assuming openid.identity is the claimedId
        // (delegation can't work with stateless/bare resp v1 operation)
        if (DEBUG) _log.debug(
            "Performing discovery on the ClaimedID in the assertion: " + assertId);
        List discoveries = _discovery.discover(assertId);

        Iterator iter = discoveries.iterator();
        while (iter.hasNext())
        {
            DiscoveryInformation service = (DiscoveryInformation) iter.next();

            if (service.isVersion2() || // only interested in v1
                ! service.hasClaimedIdentifier() || // need a claimedId
                service.hasDelegateIdentifier() || // not allowing delegates
                ! assertId.equals(service.getClaimedIdentifier().getIdentifier()))
                continue;

            if (DEBUG) _log.debug("Found matching service: " + service);

            // keep the first endpoint that matches
            if (firstServiceMatch == null)
                firstServiceMatch = service;

            Association assoc = _associations.load(
                service.getOPEndpoint().toString(),
                authResp.getHandle());

            // don't look further if there is an association with this endpoint
            if (assoc != null)
            {
                if (DEBUG)
                    _log.debug("Found existing association for  " + service +
                        " Not looking for another service endpoint.");
                return service;
            }
        }

        if (firstServiceMatch == null)
            _log.error("No service element found to match " +
                "the identifier in the assertion.");

        return firstServiceMatch;
    }

    /**
     * Verifies the discovered information associated with a OpenID 2.0
     * response.
     *
     * @param authResp      The authentication response to be verified.
     * @param discovered    The discovery information obtained earlier during
     *                      the discovery stage, associated with the
     *                      identifier(s) in the request. Stateless operation
     *                      is assumed if null.
     * @return              The discovery information associated with the
     *                      claimed identifier, that can be used further in
     *                      the verification process. Null if the discovery
     *                      on the claimed identifier does not match the data
     *                      in the assertion.
     */
    private DiscoveryInformation verifyDiscovered2(AuthSuccess authResp,
                                        DiscoveryInformation discovered)
            throws DiscoveryException
    {
        if (authResp == null || ! authResp.isVersion2() ||
                authResp.getIdentity() == null || authResp.getClaimed() == null)
        {
            if (DEBUG)
                _log.debug("Discovered information doesn't match " +
                           "auth response / version");
            return null;
        }

        // asserted identifier in the AuthResponse
        String assertId = authResp.getIdentity();

        // claimed identifier in the AuthResponse
        Identifier respClaimed =
            _discovery.parseIdentifier(authResp.getClaimed(), true);

        // the OP endpoint sent in the response
        String respEndpoint = authResp.getOpEndpoint();

        if (DEBUG)
            _log.debug("Verifying discovered information for OpenID2 assertion " +
                       "about ClaimedID: " + respClaimed.getIdentifier());


        // was the claimed identifier in the assertion previously discovered?
        if (discovered != null && discovered.hasClaimedIdentifier() &&
                discovered.getClaimedIdentifier().equals(respClaimed) )
        {
            // OP-endpoint, OP-specific ID and protocol version must match
            String opSpecific = discovered.hasDelegateIdentifier() ?
                    discovered.getDelegateIdentifier() :
                    discovered.getClaimedIdentifier().getIdentifier();

            if ( opSpecific.equals(assertId) &&
                    discovered.isVersion2() &&
                    discovered.getOPEndpoint().toString().equals(respEndpoint))
            {
                if (DEBUG) _log.debug(
                        "ClaimedID in the assertion was previously discovered: "
                        + respClaimed);
                return discovered;
            }
        }

        // stateless, bare response, or the user changed the ID at the OP
        DiscoveryInformation firstServiceMatch = null;

        // perform discovery on the claim identifier in the assertion
        if(DEBUG) _log.debug(
                "Performing discovery on the ClaimedID in the assertion: "
                 + respClaimed);
        List discoveries = _discovery.discover(respClaimed);

        // find the newly discovered service endpoint that matches the assertion
        // - OP endpoint, OP-specific ID and protocol version must match
        // - prefer (first = highest priority) endpoint with an association
        if (DEBUG)
            _log.debug("Looking for a service element to match " +
                       "the ClaimedID and OP endpoint in the assertion...");
        Iterator iter = discoveries.iterator();
        while (iter.hasNext())
        {
            DiscoveryInformation service = (DiscoveryInformation) iter.next();

            if (DiscoveryInformation.OPENID2_OP.equals(service.getVersion()))
                continue;

            String opSpecific = service.hasDelegateIdentifier() ?
                    service.getDelegateIdentifier() :
                    service.getClaimedIdentifier().getIdentifier();

            if ( ! opSpecific.equals(assertId) ||
                    ! service.isVersion2() ||
                    ! service.getOPEndpoint().toString().equals(respEndpoint) )
                continue;

            // keep the first endpoint that matches
            if (firstServiceMatch == null)
            {
                if (DEBUG) _log.debug("Found matching service: " + service);
                firstServiceMatch = service;
            }

            Association assoc = _associations.load(
                    service.getOPEndpoint().toString(),
                    authResp.getHandle());

            // don't look further if there is an association with this endpoint
            if (assoc != null)
            {
                if (DEBUG)
                    _log.debug("Found existing association, " +
                               "not looking for another service endpoint.");
                return service;
            }
        }

        if (firstServiceMatch == null)
            _log.error("No service element found to match " +
                       "the ClaimedID / OP-endpoint in the assertion.");

        return firstServiceMatch;
    }

    /**
     * Verifies the signature in a authentication response message.
     *
     * @param authResp      Authentication response to be verified.
     * @param discovered    The discovery information obtained earlier during
     *                      the discovery stage.
     * @return              True if the verification succeeded, false otherwise.
     */
    private VerificationResult verifySignature(AuthSuccess authResp,
                                               DiscoveryInformation discovered,
                                               VerificationResult result)
        throws AssociationException, MessageException, DiscoveryException
    {
        if (discovered == null || authResp == null)
        {
            _log.error("Can't verify signature: " +
                       "null assertion or discovered information.");

            result.setStatusMsg("Can't verify signature: " +
                       "null assertion or discovered information.");

            return result;
        }

        Identifier claimedId = discovered.isVersion2() ?
            _discovery.parseIdentifier(authResp.getClaimed()) : //may have frag
            discovered.getClaimedIdentifier(); //assert id may be delegate in v1

        String handle = authResp.getHandle();
        URL op = discovered.getOPEndpoint();
        Association assoc = _associations.load(op.toString(), handle);

        if (assoc != null) // association available, local verification
        {
            _log.info("Found association: " + assoc.getHandle() +
                      " verifying signature locally...");
            String text = authResp.getSignedText();
            String signature = authResp.getSignature();

            if (assoc.verifySignature(text, signature))
            {
                result.setVerifiedId(claimedId);
                if (DEBUG) _log.debug("Local signature verification succeeded.");
            }
            else
            {
                result.setStatusMsg("Local signature verification failed");
                if (DEBUG)
                    _log.debug("Local signature verification failed.");
            }

        }
        else // no association, verify with the OP
        {
            _log.info("No association found, " +
                      "contacting the OP for direct verification...");

            VerifyRequest vrfy = VerifyRequest.createVerifyRequest(authResp);

            ParameterList responseParams = new ParameterList();

            int respCode = call(op.toString(), vrfy, responseParams);
            if (HttpStatus.SC_OK == respCode)
            {
                VerifyResponse vrfyResp =
                        VerifyResponse.createVerifyResponse(responseParams);

                vrfyResp.validate();

                if (vrfyResp.isSignatureVerified())
                {
                    // process the optional invalidate_handle first
                    String invalidateHandle = vrfyResp.getInvalidateHandle();
                    if (invalidateHandle != null)
                        _associations.remove(op.toString(), invalidateHandle);

                    result.setVerifiedId(claimedId);
                    if (DEBUG)
                        _log.debug("Direct signature verification succeeded " +
                                   "with OP: " + op);
                }
                else
                {
                    if (DEBUG)
                        _log.debug("Direct signature verification failed " +
                                "with OP: " + op);
                    result.setStatusMsg("Direct signature verification failed.");
                }
            }
            else
            {
                DirectError err = DirectError.createDirectError(responseParams);

                if (DEBUG) _log.debug("Error verifying signature with the OP: "
                       + op + " error message: " + err.keyValueFormEncoding());

                result.setStatusMsg("Error verifying signature with the OP: "
                                    + err.getErrorMsg());
            }
        }

        Identifier verifiedID = result.getVerifiedId();
        if (verifiedID != null)
            _log.info("Verification succeeded for: " + verifiedID);

        else
            _log.error("Verification failed for: " + authResp.getClaimed()
                       + " reason: " + result.getStatusMsg());

        return result;
    }

    /* visible for testing */
    HttpFetcher getHttpFetcher()
    {
        return _httpFetcher;
    }
}
