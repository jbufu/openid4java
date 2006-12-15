/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import net.openid.message.*;
import net.openid.association.Association;
import net.openid.association.DiffieHellmanSession;
import net.openid.association.AssociationException;
import net.openid.association.AssociationSessionType;
import net.openid.discovery.Identifier;
import net.openid.discovery.Discovery;
import net.openid.discovery.DiscoveryException;
import net.openid.discovery.DiscoveryInformation;
import net.openid.server.NonceGenerator;
import net.openid.server.IncrementalNonceGenerator;
import net.openid.server.RealmVerifier;
import net.openid.OpenIDException;

import javax.crypto.spec.DHParameterSpec;
import java.net.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    /**
     * Discovery process manager.
     */
    Discovery _discovery = new Discovery();

    /**
     * Store for keeping track of the established associations.
     */
    private ConsumerAssociationStore _associations;

    /**
     * Consumer-side nonce generator, needed for compatibility with OpenID 1.1.
     */
    private static NonceGenerator _consumerNonceGenerator = new IncrementalNonceGenerator();

    /**
     * Private association used for signing consumer nonces when operating in
     * compatibility (v1.x) mode.
     */
    private static Association _privateAssociation;

    /**
     * Verifier for the nonces in authentication responses;
     * prevents replay attacks.
     */
    private NonceVerifier _nonceVerifier;

    /**
     * Handles HTTP calls to the Server / OpenID Provider.
     */
    private HttpClient _httpClient;


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
     * Flag for allowing / disallowing no-encryption association session
     * over plain HTTP.
     */
    private boolean _allowNoEncHttpSess = false;

    /**
     * Parameters (modulus and generator) for the Diffie-Hellman sessions.
     */
    DHParameterSpec _dhParams = DiffieHellmanSession.getDefaultParameter();

    /**
     * Timeout (in seconds) for keeping track of failed association attempts.
     * Default 5 minutes.
     */
    private int _failedAssocExpire = 300;


    // --- authentication preferences ---

    /**
     * Flag for generating checkid_immediate authentication requests.
     */
    private boolean _immediateAuth = false;

    /**
     * Used to perform verify realms against return_to URLs.
     */
    private RealmVerifier _realmVerifier;


    // --- verification preferences ---

    /**
     * Connect timeout for HTTP calls in miliseconds. Default 10s
     */
    private int _connectTimeout = 10000;

    /**
     * Socket (read) timeout for HTTP calls in miliseconds. Default 10s.
     */
    private int _socketTimeout = 10000;

    /**
     * Maximum number of redirects to be followed. Default 0.
     */
    private int _maxRedirects = 0;


    /**
     * Instantiates a ConsumerManager with default settings.
     */
    public ConsumerManager()
    {
        _httpClient = new HttpClient();
        _realmVerifier = new RealmVerifier();

        if (Association.isHmacSha256Supported())
            _prefAssocSessEnc = AssociationSessionType.DH_SHA256;
        else
            _prefAssocSessEnc = AssociationSessionType.DH_SHA1;

        try
        {
            // initialize the private association for compat consumer nonces
            _privateAssociation = Association.generate(
                    getPrefAssocSessEnc().getAssociationType(), "", 0);
        } catch (AssociationException e)
        {
            // todo: log
        }
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
    public void setAssociations(ConsumerAssociationStore associations)
    {
        this._associations = associations;
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
     * for trying to establish an association with the IdP.
     *
     * Default: 4;
     * 0 = don't use associations
     *
     * Associations and stateless mode cannot be both disabled at the same time.
     */
    public void setMaxAssocAttempts(int maxAssocAttempts)
            throws ConsumerException
    {
        if (maxAssocAttempts > 0 || _allowStateless)
            this._maxAssocAttempts = maxAssocAttempts;
        else
            throw new IllegalArgumentException(
                    "Associations and stateless mode " +
                    "cannot be both disabled at the same time.");
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
     */
    public void allowStateless(boolean useStateless)
    {
        if (_allowStateless || _maxAssocAttempts > 0)
            this._allowStateless = useStateless;
        else
            throw new IllegalArgumentException(
                    "Associations and stateless mode " +
                    "cannot be both disabled at the same time.");
    }

    /**
     * Returns true if the ConsumerManager is configured to fallback to
     * stateless mode when failing to associate with an OpenID Provider.
     */
    public boolean statelessAllowed()
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
     * See also: {@link #setAllowNoEncHttp(boolean)}
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
     * Flag that determines whether no-encryption association sessions are
     * allowed through plain HTTP.
     * <p>
     * Default: false (require HTTPS for no-encryption association sessions).
     * <p>
     * OpenID specification strongly RECOMMENDEDS AGAINST  the use of
     * "no-encryption" sessions on a public network; this is vulnerable to
     * eavesdropping attacks.
     */
    public void setAllowNoEncHttp(boolean allowNoEncHttp)
    {
        this._allowNoEncHttpSess = allowNoEncHttp;
    }

    /**
     * Returns true if no-encryption association sessions will be allowed to be
     * established over plain HTTP.
     */
    public boolean getAllowNoEncHttp()
    {
        return _allowNoEncHttpSess;
    }

    /**
     * Sets the expiration timeout (in seconds) for keeping track of failed
     * association attempts.
     * <p>
     * If an association cannot be establish with an IdP, subsequesnt
     * authentication request to that IdP will not try to establish an
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
    public long getMaxNonceAge()
    {
        return _nonceVerifier.getMaxAge();
    }

    /**
     * Does discover on an identifier. It delegates the call to its
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
     * Configures a private association for signing consumer nonces.
     * <p>
     * Consumer nonces are needed to prevent replay attacks in compatibility
     * mode, because OpenID 1.x Providers to not attach nonces to
     * authentication responses.
     * <p>
     * One way for the Consumer to know that a consumer nonce in an
     * authentication response was indeed issued by itself (and thus prevent
     * denial of service attacks), is by signing them.
     *
     * @param assoc     The association to be used for signing consumer nonces;
     *                  signing can be deactivated by setting this to null.
     *                  Signing is enabled by default.
     */
    public void setPrivateAssociation(Association assoc)
    {
        _privateAssociation = assoc;
    }

    /**
     * Gets the private association used for signing consumer nonces.
     *
     * @see #setPrivateAssociation(net.openid.association.Association)
     */
    public Association getPrivateAssociation()
    {
        return _privateAssociation;
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
        _httpClient.getParams().setParameter(
                "http.protocol.max-redirects", new Integer(_maxRedirects));
        _httpClient.getParams().setParameter(
                "http.protocol.allow-circular-redirects", Boolean.FALSE);
        _httpClient.getParams().setSoTimeout(_socketTimeout);
        _httpClient.getHttpConnectionManager()
                .getParams().setConnectionTimeout(_connectTimeout);

        int responseCode = -1;
        try
        {
            // build the post message with the parameters from the request
            PostMethod post = new PostMethod(url);
            // can't follow redirects on a POST (w/o user intervention)
            //post.setFollowRedirects(true);
            post.setRequestEntity(new StringRequestEntity(
                    request.wwwFormEncoding(),
                    "application/x-www-form-urlencoded", "UTF-8"));

            // place the http call to the IdP
            responseCode = _httpClient.executeMethod(post);

            response.copyOf(ParameterList.
                    createFromKeyValueForm(post.getResponseBodyAsString()) );
        } catch (IOException e)
        {
            System.out.println("Failed to establish association with: " + url +
                " message: " + request.wwwFormEncoding());
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
     * @see Discovery#discover(net.openid.discovery.Identifier)
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
            assoc = _associations.load(discovered.getIdpEndpoint().toString());

            if ( assoc != null &&
                    ! Association.FAILED_ASSOC_HANDLE.equals(assoc.getHandle()))
                return discovered;
        }

        // no association established, return the first service endpoint
        return discoveries.size() > 0 ?
                (DiscoveryInformation) discoveries.get(0) : null;
    }

    /**
     * Tries to establish an association with the OpenID Provider.
     * <p>
     * The resulting association information will be kept on storage for later
     * use at verification stage.
     *
     * @param discovered    DiscoveryInformation obtained during the discovery
     * @return              The number of association attempts performed.
     */
    private int associate(DiscoveryInformation discovered, int maxAttempts)
    {
        if (_maxAssocAttempts == 0) return 0; // associations disabled

        URL idpUrl = discovered.getIdpEndpoint();
        String idpEndpoint = idpUrl.toString();

        // check if there's an already established association
        Association a = _associations.load(idpEndpoint);
        if (a != null && a.getHandle() != null) return 0;

        String handle = Association.FAILED_ASSOC_HANDLE;

        // build a list of association types, with the preferred one at the end
        LinkedHashMap requests = new LinkedHashMap();

        if (discovered.isVersion2())
        {
            requests.put(AssociationSessionType.NO_ENCRYPTION_SHA1MAC, null);
            requests.put(AssociationSessionType.NO_ENCRYPTION_SHA256MAC, null);
            requests.put(AssociationSessionType.DH_SHA1, null);
            requests.put(AssociationSessionType.DH_SHA256, null);
        } else
        {
            requests.put(AssociationSessionType.NO_ENCRYPTION_COMPAT_SHA1MAC, null);
            requests.put(AssociationSessionType.DH_COMPAT_SHA1, null);
        }

        if (_prefAssocSessEnc.isVersion2() == discovered.isVersion2())
            requests.put(_prefAssocSessEnc, null);

        // build a stack of Association Request objects
        // and keep only the allowed by the configured preferences
        // the most-desirable entry is always at the top of the stack
        Stack reqStack = new Stack();
        Iterator iter = requests.keySet().iterator();
        while(iter.hasNext())
        {
            AssociationSessionType type = (AssociationSessionType) iter.next();

            // create the appropriate Association Request
            AssociationRequest newReq = createAssociationRequest(type, idpUrl);
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

                // was this association / session type attempted already?
                if (alreadyTried.keySet().contains(assocReq.getType()))
                    continue;

                // mark the current request type as already tried
                alreadyTried.put(assocReq.getType(), null);

                ParameterList respParams = new ParameterList();
                int status = call(idpEndpoint, assocReq, respParams);

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


                    if ( assocResp.getType().equals(assocReq.getType()) ||
                            // v1 IdPs may return a success no-encryption resp
                            ( ! discovered.isVersion2() &&
                              assocResp.getType().getHAlgorithm() == null &&
                              createAssociationRequest(
                                      assocResp.getType(), idpUrl) != null ) )
                    {
                        // store the association and do no try alternatives
                        _associations.save(idpEndpoint, assoc);
                        break;
                    }

                } else
                if (status == HttpStatus.SC_BAD_REQUEST) // error response
                {
                    // retrieve fallback sess/assoc/encryption params set by IdP
                    // and queue a new attempt
                    AssociationError assocErr =
                            AssociationError.createAssociationError(respParams);

                    AssociationSessionType idpType =
                            AssociationSessionType.create(
                                    assocErr.getSessionType(),
                                    assocErr.getAssocType());

                    if (alreadyTried.keySet().contains(idpType))
                        continue;

                    // create the appropriate Association Request
                    AssociationRequest newReq =
                            createAssociationRequest(idpType, idpUrl);

                    if (newReq != null)
                        reqStack.push(newReq);
                }
            } catch (OpenIDException e)
            {
                // todo: log association attempt failure
            }
        }

        // store IdPs with which an association could not be established
        // so that association attempts are not performed with each auth request
        if (Association.FAILED_ASSOC_HANDLE.equals(handle)
                && _failedAssocExpire > 0)
            _associations.save(idpEndpoint,
                    Association.getFailedAssociation(_failedAssocExpire));

        return maxAttempts - attemptsLeft;
    }

    /**
     * Constructs an Association Request message of the specified session and
     * association type, taking into account the user preferences (encryption
     * level, default Diffie-Hellman parameters).
     *
     * @param type      The type of the association (session and association)
     * @param idpUrl    The IdP for which the association request is created
     * @return          An AssociationRequest message ready to be sent back
     *                  to the OpenID Provider, or null if an association
     *                  of the requested type cannot be built.
     */
    private AssociationRequest createAssociationRequest(
            AssociationSessionType type, URL idpUrl)
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
                        &&
                        Association.isHmacSupported(type.getAssociationType()))
                    assocReq = AssociationRequest.createAssociationRequest(type, dhSess);
            } else // no-enc session
            {
                if ((_allowNoEncHttpSess ||
                        idpUrl.getProtocol().equals("https"))
                        &&
                        Association.isHmacSupported(type.getAssociationType()))
                    assocReq = AssociationRequest.createAssociationRequest(type);
            }

            return assocReq;
        }
        catch (OpenIDException e)
        {
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

        if (discovered == null)
            throw new ConsumerException("Authentication cannot continue: " +
                    "null discovery information provided.");

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
        associate(discovered, _maxAssocAttempts);

        Association assoc =
                _associations.load(discovered.getIdpEndpoint().toString());
        String handle = assoc != null ?
                assoc.getHandle() : Association.FAILED_ASSOC_HANDLE;

        // get the Claimed ID
        String claimedId;
        if (discovered.hasClaimedIdentifier())
            claimedId = discovered.getClaimedIdentifier().getIdentifier();
        else
            claimedId = AuthRequest.SELECT_ID;

        // set the Delegate ID (aka OP-specific identifier)
        String delegate = claimedId;
        if (discovered.hasDelegateIdentifier())
            delegate = discovered.getDelegateIdentifier().getIdentifier();

        // stateless mode disabled ?
        if ( !_allowStateless && Association.FAILED_ASSOC_HANDLE.equals(handle))
            throw new ConsumerException("Authentication cannot be performed: " +
                    "no association available and stateless mode is disabled");

        AuthRequest authReq = AuthRequest.createAuthRequest(claimedId, delegate,
                ! discovered.isVersion2(), returnToUrl, handle, realm, _realmVerifier);

        authReq.setOPEndpoint(discovered.getIdpEndpoint());

        if (! discovered.isVersion2())
            authReq.setReturnTo(insertConsumerNonce(authReq.getReturnTo()));

        // ignore the immediate flag for OP-directed identifier selection
        if (! AuthRequest.SELECT_ID.equals(claimedId))
            authReq.setImmediate(_immediateAuth);

        if (! authReq.isValid())
            throw new MessageException("Invalid AuthRequest: " +
                    authReq.wwwFormEncoding());

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

        // non-immediate negative response
        if ( "cancel".equals(response.getParameterValue("openid.mode")) )
        {
            result.setAuthResponse(AuthFailure.createAuthFailure(response));
            return result;
        }

        // immediate negative response
        if ( ("id_res".equals(response.getParameterValue("openid.mode"))
                && response.hasParameter("openid.user_setup_url") ) )
        {
            AuthImmediateFailure fail =
                    AuthImmediateFailure.createAuthImmediateFailure(response);
            result.setAuthResponse(fail);
            result.setIdpSetupUrl(fail.getUserSetupUrl());
            return result;
        }

        AuthSuccess authResp = AuthSuccess.createAuthSuccess(response);
        if (!authResp.isValid())
            throw new MessageException("Invalid Authentication Response: " +
                    authResp.wwwFormEncoding());
        result.setAuthResponse(authResp);

        // [1/4] return_to verification
        if (! verifyReturnTo(receivingUrl, authResp))
        {
            result.setStatusMsg("Return_To URL verification failed.");
            return result;
        }

        // [2/4] : nonce verification
        if (! verifyNonce(authResp, discovered))
        {
            result.setStatusMsg("Nonce verificaton failed.");
            return result;
        }

        // [3/4] : discovered info verification
        discovered = verifyDiscovered(authResp, discovered);
        if (discovered == null || ! discovered.hasClaimedIdentifier())
        {
            result.setStatusMsg("Discovered information verification failed.");
            return result;
        }

        // [4/4] : signature verification
        if (verifySignature(authResp, discovered))  // mark verification success
            result.setVerifiedId(discovered.getClaimedIdentifier());
        else
            result.setStatusMsg("Signature verification failed.");

        return result;
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
        URL receiving;
        URL returnTo;
        try
        {
            receiving = new URL(receivingUrl);
            returnTo = new URL(response.getReturnTo());
        }
        catch (MalformedURLException e)
        {
            return false;
        }

        if ( ! receiving.getProtocol().equals(returnTo.getProtocol()) ||
                ! receiving.getAuthority().equals(returnTo.getAuthority()) ||
                ! receiving.getPath().equals(returnTo.getPath()) )
            return false;

        List returnToParams = returnTo.getQuery() != null ?
                Arrays.asList(returnTo.getQuery().split("&")) : null;
        List receivingParams = receiving.getQuery() != null ?
                Arrays.asList(receiving.getQuery().split("&")) : null;

        return (receivingParams == null && returnToParams == null) ||
                (receivingParams != null && receivingParams.containsAll(returnToParams) );
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
            nonce = extractConsumerNonce(authResp.getReturnTo());

        // using the same nonce verifier for both server and consumer nonces
        return (NonceVerifier.OK == _nonceVerifier.seen(
                discovered.getIdpEndpoint().toString(), nonce));
    }

    /**
     * Inserts a consumer-side nonce as a custom parameter in the return_to
     * parameter of the authentication request.
     * <p>
     * Needed for preventing replay attack when running compatibility mode.
     * OpenID 1.1 OpenID Providers do not generate nonces in authentication
     * responses.
     *
     * @param returnTo          The return_to URL to which a custom nonce
     *                          parameter will be added.
     * @return                  The return_to URL containing the nonce.
     */
    public String insertConsumerNonce(String returnTo)
    {
        String nonce = _consumerNonceGenerator.next();

        returnTo += (returnTo.indexOf('?') != -1) ? '&' : '?';

        try
        {
            returnTo += "openid.rpnonce=" + URLEncoder.encode(nonce, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }

        try
        {
            if (_privateAssociation != null)
                returnTo += "&openid.rpsig=" + _privateAssociation.sign(returnTo);
        }
        catch (AssociationException e)
        {
            return null;
        }

        return returnTo;
    }

    /**
     * Extracts the consumer-side nonce from the return_to parameter in
     * authentication response from a OpenID 1.1 Provider.
     *
     * @param returnTo      return_to URL from the authentication response
     * @return              The nonce found in the return_to URL, or null if
     *                      it wasn't found.
     */
    public String extractConsumerNonce(String returnTo)
    {
        if (returnTo == null) return null;

        String nonce = null;
        String signature = null;

        URL returnToUrl;
        try
        {
            returnToUrl = new URL(returnTo);
        }
        catch (MalformedURLException e)
        {
            return null;
        }

        String query = returnToUrl.getQuery();

        String[] params = query.split("&");

        for (int i=0; i < params.length; i++)
        {
            String keyVal[] = params[i].split("=", 2);

            try
            {
                if (keyVal.length == 2 && "openid.rpnonce".equals(keyVal[0]))
                    nonce = URLDecoder.decode(keyVal[1], "UTF-8");

                if (keyVal.length == 2 && "openid.rpsig".equals(keyVal[0]))
                    signature = URLDecoder.decode(keyVal[1], "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                return null;
            }
        }

        // don't check the signature if no private association is configured
        if (_privateAssociation == null)
                return nonce;

        // check the signature
        if (signature == null) return null;

        String signed = returnTo.substring(0, returnTo.indexOf("openid.rpsig="));
        try
        {
            if (_privateAssociation.verifySignature(signed, signature))
                return nonce;
            else
                return null;
        } catch (AssociationException e)
        {
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
     *                      identifier(s) in the request.
     * @return              The discovery information associated with the
     *                      claimed identifier in the response, that can be
     *                      used further in the verification process. May be
     *                      null if the discovery on the claimed identifier
     *                      does not exactly match the data in the assertion.
     */
    private DiscoveryInformation verifyDiscovered(AuthSuccess authResp,
                                        DiscoveryInformation discovered)
            throws DiscoveryException
    {
        if (authResp.getIdentity() == null)
            return null; // assertion is not about an identifier

        // asserted identifier in the AuthResponse
        Identifier assertId = Discovery.parseIdentifier(authResp.getIdentity());

        // claimed identifier in the AuthResponse; can be null in v1 messages
        Identifier respClaimed = authResp.getClaimed() != null ?
                Discovery.parseIdentifier(authResp.getClaimed()) : assertId;

        // the OP endpoint sent in the response; can be null in v1 messages
        String respEndpoint = authResp.getOpEndpoint();

        // was the claimed identifier in the assertion previously discovered?
        if (discovered != null && discovered.hasClaimedIdentifier() &&
                discovered.getClaimedIdentifier().equals(respClaimed) )
        {
            // OP-endpoint, OP-specific ID and protocol version must match
            Identifier opSpecific = discovered.hasDelegateIdentifier() ?
                    discovered.getDelegateIdentifier() :
                    discovered.getClaimedIdentifier();

            if ( assertId.equals(opSpecific) &&
                    (discovered.isVersion2() == authResp.isVersion2()) &&
                    // only check OP-endpoint vor v2 messages
                    (! authResp.isVersion2() ||
                            discovered.getIdpEndpoint().equals(respEndpoint)))
                    return discovered;
        }

        // stateless, bare response, or the user changed the ID at the OP
        DiscoveryInformation firstServiceMatch = null;

        // perform discovery on the claim identifier in the assertion
        List discoveries = _discovery.discover(respClaimed);

        // find the newly discovered service endpoint that matches the assertion
        // - OP endpoint, OP-specific ID and protocol version must match
        // - prefer (first = highest priority) endpoint with an association
        Iterator iter = discoveries.iterator();
        while (iter.hasNext())
        {
            DiscoveryInformation service = (DiscoveryInformation) iter.next();

            if (DiscoveryInformation.OPENID2_OP.equals(service.getVersion()))
                continue;

            Identifier opSpecific = service.hasDelegateIdentifier() ?
                    service.getDelegateIdentifier() :
                    service.getClaimedIdentifier();

            if ( ! assertId.equals(opSpecific) ||
                    authResp.isVersion2() != service.isVersion2() ||
                    // only check the OP endpoint for v2 messages
                    (authResp.isVersion2() &&
                            ! service.getIdpEndpoint().equals(respEndpoint)) )
                continue;

            // take the first endpoint that matches
            if (firstServiceMatch == null) firstServiceMatch = service;

            Association assoc = _associations.load(
                    service.getIdpEndpoint().toString(),
                    authResp.getHandle());

            // don't look further if there is an association for it
            if (assoc != null) return service;
        }

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
    private boolean verifySignature(AuthSuccess authResp,
                                    DiscoveryInformation discovered)
            throws AssociationException, MessageException
    {
        if (discovered == null || authResp == null)
            return false;

        String handle = authResp.getHandle();
        URL idp = discovered.getIdpEndpoint();
        Association assoc = _associations.load(idp.toString(), handle);

        if (assoc != null) // association available, local verification
        {
            String text = authResp.getSignedText();
            String signature = authResp.getSignature();

            if (assoc.verifySignature(text, signature))
                return true;

        } else // no association, verify with the IdP
        {
            VerifyRequest vrfy = VerifyRequest.createVerifyRequest(authResp);

            ParameterList responseParams = new ParameterList();

            int respCode = call(idp.toString(), vrfy, responseParams);
            if (HttpStatus.SC_OK == respCode)
            {
                VerifyResponse vrfyResp =
                        VerifyResponse.createVerifyResponse(responseParams);

                if (vrfyResp.isValid() & vrfyResp.isSignatureVerified())
                {
                    // process the optional invalidate_handle first
                    String invalidateHandle = vrfyResp.getInvalidateHandle();
                    if (invalidateHandle != null)
                        _associations.remove(idp.toString(), invalidateHandle);

                    return true;
                }
            }
        }

        return false;
    }
}
