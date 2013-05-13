/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.server;

import com.google.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.association.DiffieHellmanSession;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.message.*;
import org.openid4java.util.HttpFetcherFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Manages OpenID communications with an OpenID Relying Party (Consumer).
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ServerManager
{
    private static Log _log = LogFactory.getLog(ServerManager.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Keeps track of the associations established with consumer sites.
     *
     * MUST be a different store than ServerAssociationStore#_privateAssociations,
     * otherwise openid responses can be forged and user accounts hijacked.
     */
    private ServerAssociationStore _sharedAssociations = new InMemoryServerAssociationStore();

    /**
     * Keeps track of private (internal) associations created for signing
     * authentication responses for stateless consumer sites.
     *
     * MUST be a different store than ServerAssociationStore#_sharedAssociations,
     * otherwise openid responses can be forged and user accounts hijacked.
     */
    private ServerAssociationStore _privateAssociations = new InMemoryServerAssociationStore();

    /**
     * Flag for checking that shared associations are not accepted as or mixed with
     * the private ones.
     *
     * Default true: check is performed at the expense of one extra association store query
     * to ensure that ServerManager#_sharedAssociations and ServerManager#_privateAssociations
     * are different store/instances.
     */
    private boolean _checkPrivateSharedAssociations = true;

    /**
     * Nonce generator implementation.
     */
    private NonceGenerator _nonceGenerator = new IncrementalNonceGenerator();

    // --- association preferences ---

    /**
     * The lowest encryption level session accepted for association sessions
     */
    private AssociationSessionType _minAssocSessEnc
            = AssociationSessionType.NO_ENCRYPTION_SHA1MAC;

    /**
     * The preferred association session type; will be attempted first.
     */
    private AssociationSessionType _prefAssocSessEnc
            = AssociationSessionType.DH_SHA256;

    /**
     * Expiration time (in seconds) for associations.
     */
    private int _expireIn = 1800;

    // --- authentication preferences ---

    /**
     * In OpenID 1.x compatibility mode, the URL at the OpenID Provider where
     * the user should be directed when a immediate authentication request
     * fails.
     * <p>
     * If not configured, the OP endpoint URL is used.
     */
    private String _userSetupUrl = null;

    /**
     * List of coma-separated fields to be signed in authentication responses.
     */
    private String _signFields;

    /**
     * Array of extension namespace URIs that the consumer manager will sign,
     * if present in auth responses.
     */
    private String[] _signExtensions;

    /**
     * Used to perform verify realms against return_to URLs.
     */
    private RealmVerifier _realmVerifier;

    /**
     * The OpenID Provider's endpoint URL, where it accepts OpenID
     * authentication requests.
     * <p>
     * This is a global setting for the ServerManager; can also be set on a
     * per message basis.
     *
     * @see #authResponse(org.openid4java.message.ParameterList, String, String, boolean, String)
     */
    private String _opEndpointUrl;


    /**
     * Gets the store implementation used for keeping track of the generated
     * associations established with consumer sites.
     *
     * @see ServerAssociationStore
     */
    public ServerAssociationStore getSharedAssociations()
    {
        return _sharedAssociations;
    }

    /**
     * Sets the store implementation that will be used for keeping track of
     * the generated associations established with consumer sites.
     *
     * @param sharedAssociations       ServerAssociationStore implementation
     * @see ServerAssociationStore
     */
    public void setSharedAssociations(ServerAssociationStore sharedAssociations)
    {
        _sharedAssociations = sharedAssociations;
    }

    /**
     * Gets the store implementation used for keeping track of the generated
     * private associations (used for signing responses to stateless consumer
     * sites).
     *
     * @see ServerAssociationStore
     */
    public ServerAssociationStore getPrivateAssociations()
    {
        return _privateAssociations;
    }

    /**
     * Sets the store implementation that will be used for keeping track of
     * the generated private associations (used for signing responses to
     * stateless consumer sites).
     *
     * @param privateAssociations       ServerAssociationStore implementation
     * @see ServerAssociationStore
     */
    public void setPrivateAssociations(ServerAssociationStore privateAssociations)
    {
        _privateAssociations = privateAssociations;
    }

    /**
     * Gets the _checkPrivateSharedAssociations flag.
     *
     * @see ServerManager#_checkPrivateSharedAssociations
     */
    public boolean isCheckPrivateSharedAssociations() {
        return _checkPrivateSharedAssociations;
    }

    /**
     * Sets the _checkPrivateSharedAssociations flag.
     *
     * @see ServerManager#_checkPrivateSharedAssociations
     */
    public void setCheckPrivateSharedAssociations(boolean _checkPrivateSharedAssociations) {
        this._checkPrivateSharedAssociations = _checkPrivateSharedAssociations;
    }

    /**
     * Gets the minimum level of encryption configured for association sessions.
     * <p>
     * Default: no-encryption session, SHA1 MAC association
     */
    public AssociationSessionType getMinAssocSessEnc()
    {
        return _minAssocSessEnc;
    }

    /**
     * Gets the NonceGenerator used for generating nonce tokens to uniquely
     * identify authentication responses.
     *
     * @see NonceGenerator
     */
    public NonceGenerator getNonceGenerator()
    {
        return _nonceGenerator;
    }

    /**
     * Sets the NonceGenerator implementation that will be used to generate
     * nonce tokens to uniquely identify authentication responses.
     *
     * @see NonceGenerator
     */
    public void setNonceGenerator(NonceGenerator nonceGenerator)
    {
        _nonceGenerator = nonceGenerator;
    }

    /**
     * Configures the minimum level of encryption accepted for association
     * sessions.
     * <p>
     * Default: no-encryption session, SHA1 MAC association
     */
    public void setMinAssocSessEnc(AssociationSessionType minAssocSessEnc)
    {
        this._minAssocSessEnc = minAssocSessEnc;
    }

    /**
     * Gets the preferred association / session type.
     */
    public AssociationSessionType getPrefAssocSessEnc()
    {
        return _prefAssocSessEnc;
    }

    /**
     * Sets the preferred association / session type.
     *
     * @see AssociationSessionType
     */
    public void setPrefAssocSessEnc(AssociationSessionType type)
            throws ServerException
    {
        if (! Association.isHmacSupported(type.getAssociationType()) ||
            ! DiffieHellmanSession.isDhSupported(type) )
            throw new ServerException("Unsupported association / session type: "
            + type.getSessionType() + " : " + type.getAssociationType());

        if (_minAssocSessEnc.isBetter(type) )
            throw new ServerException(
                    "Minimum encryption settings cannot be better than the preferred");

        this._prefAssocSessEnc = type;
    }

    /**
     * Gets the expiration time (in seconds) for the generated associations
     */
    public int getExpireIn()
    {
        return _expireIn;
    }

    /**
     * Sets the expiration time (in seconds) for the generated associations
     */
    public void setExpireIn(int _expireIn)
    {
        this._expireIn = _expireIn;
    }

    /**
     * Gets the URL at the OpenID Provider where the user should be directed
     * when a immediate authentication request fails.
     */
    public String getUserSetupUrl()
    {
        return _userSetupUrl;
    }

    /**
     * Sets the URL at the OpenID Provider where the user should be directed
     * when a immediate authentication request fails.
     */
    public void setUserSetupUrl(String userSetupUrl)
    {
        this._userSetupUrl = userSetupUrl;
    }

    /**
     * Sets the list of parameters that the OpenID Provider will sign when
     * generating authentication responses.
     * <p>
     * The fields in the list must be coma-separated and must not include the
     * 'openid.' prefix. Fields that are required to be signed are automatically
     * added by the underlying logic, so that a valid message is generated,
     * regardles if they are included in the user-supplied list or not.
     */
    public void setSignFields(String signFields)
    {
        this._signFields = signFields;
    }

    /**
     * Gets the list of parameters that the OpenID Provider will sign when
     * generating authentication responses.
     * <p>
     * Coma-separated list.
     */
    public String getSignFields()
    {
        return _signFields;
    }

    public void setSignExtensions(String[] extensins)
    {
        _signExtensions = extensins;
    }

    public String[] getSignExtensions()
    {
        return _signExtensions;
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
     * Gets the flag that instructs the realm verifier to enforce validation
     * of the return URL agains the endpoints discovered from the RP's realm.
     */
    public boolean getEnforceRpId()
    {
        return _realmVerifier.getEnforceRpId();
    }

    /**
     * Sets the flag that instructs the realm verifier to enforce validation
     * of the return URL agains the endpoints discovered from the RP's realm.
     */
    public void setEnforceRpId(boolean enforceRpId)
    {
        _realmVerifier.setEnforceRpId(enforceRpId);
    }

    /**
     * Gets OpenID Provider's endpoint URL, where it accepts OpenID
     * authentication requests.
     * <p>
     * This is a global setting for the ServerManager; can also be set on a
     * per message basis.
     *
     * @see #authResponse(org.openid4java.message.ParameterList, String, String, boolean, String)
     */
    public String getOPEndpointUrl()
    {
        return _opEndpointUrl;
    }

    /**
     * Sets the OpenID Provider's endpoint URL, where it accepts OpenID
     * authentication requests.
     * <p>
     * This is a global setting for the ServerManager; can also be set on a
     * per message basis.
     *
     * @see #authResponse(org.openid4java.message.ParameterList, String, String, boolean, String)
     */
    public void setOPEndpointUrl(String opEndpointUrl)
    {
        this._opEndpointUrl = opEndpointUrl;
    }

    /**
     * Constructs a ServerManager with default settings.
     */
    public ServerManager() {
      this(new RealmVerifierFactory(new YadisResolver(new HttpFetcherFactory())));
    }

    @Inject
    public ServerManager(RealmVerifierFactory factory)
    {
        // initialize a default realm verifier
        _realmVerifier = factory.getRealmVerifierForServer();
        _realmVerifier.setEnforceRpId(false);
    }


    /**
     * Processes a Association Request and returns a Association Response
     * message, according to the request parameters and the preferences
     * configured for the OpenID Provider
     *
     * @return AssociationResponse      upon successfull association,
     *                                  or AssociationError if no association
     *                                  was established
     *
     */
    public Message associationResponse(ParameterList requestParams)
    {
        boolean isVersion2 = requestParams.hasParameter("openid.ns");

        _log.info("Processing association request...");

        try
        {
            // build request message from response params (+ integrity check)
            AssociationRequest assocReq =
                    AssociationRequest.createAssociationRequest(requestParams);

            isVersion2 = assocReq.isVersion2();

            AssociationSessionType type = assocReq.getType();

            // is supported / allowed ?
            if (! Association.isHmacSupported(type.getAssociationType()) ||
                    ! DiffieHellmanSession.isDhSupported(type) ||
                    _minAssocSessEnc.isBetter(type))
            {
                throw new AssociationException("Unable create association for: "
                        + type.getSessionType() + " / "
                        + type.getAssociationType() );
            }
            else // all ok, go ahead
            {
                Association assoc = _sharedAssociations.generate(
                        type.getAssociationType(), _expireIn);

                _log.info("Returning shared association; handle: " + assoc.getHandle());

                return AssociationResponse.createAssociationResponse(assocReq, assoc);
            }
        }
        catch (OpenIDException e)
        {
            // association failed, respond accordingly
            if (isVersion2)
            {
                _log.warn("Cannot establish association, " +
                           "responding with an OpenID2 association error.", e);

                return AssociationError.createAssociationError(
                        e.getMessage(), _prefAssocSessEnc);
            }
            else
            {
                _log.warn("Error processing an OpenID1 association request: " +
                          e.getMessage() +
                          " Responding with a dummy association.", e);
                try
                {
                    // generate dummy association & no-encryption response
                    // for compatibility mode
                    Association dummyAssoc = _sharedAssociations.generate(
                            Association.TYPE_HMAC_SHA1, 0);

                    AssociationRequest dummyRequest =
                            AssociationRequest.createAssociationRequest(
                            AssociationSessionType.NO_ENCRYPTION_COMPAT_SHA1MAC);


                    return AssociationResponse.createAssociationResponse(
                            dummyRequest, dummyAssoc);
                }
                catch (OpenIDException ee)
                {
                    _log.error("Error creating negative OpenID1 association response.", e);
                    return null;
                }

            }

        }
    }

    /**
     * Processes a Authentication Request received from a consumer site.
     * <p>
     * Uses ServerManager's global OpenID Provider endpoint URL.
     *
     * @return      An signed positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.ParameterList, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved)
    {
        return authResponse(requestParams, userSelId, userSelClaimed,
                authenticatedAndApproved, _opEndpointUrl, true);

    }

    /**
     * Processes a Authentication Request received from a consumer site.
     * <p>
     * Uses ServerManager's global OpenID Provider endpoint URL.
     *
     * @return      A signed positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.AuthRequest, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(AuthRequest authReq,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved)
    {
        return authResponse(authReq, userSelId, userSelClaimed,
                authenticatedAndApproved, _opEndpointUrl, true);

    }

    /**
     * Processes a Authentication Request received from a consumer site.
     * <p>
     * Uses ServerManager's global OpenID Provider endpoint URL.
     *
     * @return      A positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.ParameterList, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                boolean signNow)
    {
        return authResponse(requestParams, userSelId, userSelClaimed,
                authenticatedAndApproved, _opEndpointUrl, signNow);

    }

    /**
     * Processes a Authentication Request received from a consumer site.
     * <p>
     * Uses ServerManager's global OpenID Provider endpoint URL.
     *
     * @return      A positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.AuthRequest, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(AuthRequest authReq,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                boolean signNow)
    {
        return authResponse(authReq, userSelId, userSelClaimed,
                authenticatedAndApproved, _opEndpointUrl, signNow);

    }

    /**
     * Processes a Authentication Request received from a consumer site.
     * <p>
     *
     * @return      A signed positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.ParameterList, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                String opEndpoint)
    {
        return authResponse(requestParams, userSelId, userSelClaimed,
                authenticatedAndApproved, opEndpoint, true);
    }

    /**
     * Processes a Authentication Request received from a consumer site.
     * <p>
     *
     * @return      A signed positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.AuthRequest, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(AuthRequest auhtReq,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                String opEndpoint)
    {
        return authResponse(auhtReq, userSelId, userSelClaimed,
                authenticatedAndApproved, opEndpoint, true);
    }

    /**
     * Processes a Authentication Request received from a consumer site,
     * after parsing the request parameters into a valid AuthRequest.
     * <p>
     *
     * @return      A signed positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(org.openid4java.message.AuthRequest, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                String opEndpoint,
                                boolean signNow)
    {
        _log.info("Parsing authentication request...");

        AuthRequest authReq;

        boolean isVersion2 = Message.OPENID2_NS.equals(
                                requestParams.getParameterValue("openid.ns"));

        try
        {
            // build request message from response params (+ integrity check)
            authReq = AuthRequest.createAuthRequest(
                requestParams, _realmVerifier);

            return authResponse(authReq, userSelId, userSelClaimed,
                                authenticatedAndApproved, opEndpoint, signNow);
        }
        catch (MessageException e)
        {
            if (requestParams.hasParameter("openid.return_to"))
            {
                _log.error("Invalid authentication request; " +
                           "responding with an indirect error message.", e);

                return IndirectError.createIndirectError(e,
                        requestParams.getParameterValue("openid.return_to"),
                        ! isVersion2 );
            }
            else
            {
                _log.error("Invalid authentication request; " +
                           "responding with a direct error message.", e);

                return DirectError.createDirectError( e, ! isVersion2 );
            }
        }
    }

    /**
     * Processes a Authentication Request received from a consumer site.
     *
     * @param opEndpoint        The endpoint URL where the OP accepts OpenID
     *                          authentication requests.
     * @param authReq           A valid authentication request.
     * @param userSelId         OP-specific Identifier selected by the user at
     *                          the OpenID Provider; if present it will override
     *                          the one received in the authentication request.
     * @param userSelClaimed    Claimed Identifier selected by the user at
     *                          the OpenID Provider; if present it will override
     *                          the one received in the authentication request.
     * @param authenticatedAndApproved  Flag indicating that the OP has
     *                                  authenticated the user and the user
     *                                  has approved the authentication
     *                                  transaction
     * @param signNow           If true, the returned AuthSuccess will be signed.
     *                          If false, the signature will not be computed and
     *                          set - this will have to be performed later,
     *                          using #sign(org.openid4java.message.Message).
     *
     * @return                  <ul><li> AuthSuccess, if authenticatedAndApproved
     *                          <li> AuthFailure (negative response) if either
     *                          of authenticatedAndApproved is false;
     *                          <li> A IndirectError or DirectError message
     *                          if the authentication could not be performed, or
     *                          <li> Null if there was no return_to parameter
     *                          specified in the AuthRequest.</ul>
     */
    public Message authResponse(AuthRequest authReq,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                String opEndpoint,
                                boolean signNow)
    {
        _log.info("Processing authentication request...");

        boolean isVersion2 = authReq.isVersion2();

        if (authReq.getReturnTo() == null)
        {
            _log.error("No return_to in the received (valid) auth request; "
                    + "returning null auth response.");
            return null;
        }

        try
        {
            if (authenticatedAndApproved) // positive response
            {
                try
                {
                    new URL(opEndpoint);
                }
                catch (MalformedURLException e)
                {
                    String errMsg = "Invalid OP-endpoint configured; " +
                            "cannot issue authentication responses." + opEndpoint;

                    _log.error(errMsg, e);

                    return DirectError.createDirectError(
                            new ServerException(errMsg, e), isVersion2);
                }

                String id;
                String claimed;

                if (AuthRequest.SELECT_ID.equals(authReq.getIdentity()))
                {
                    id = userSelId;
                    claimed = userSelClaimed;
                }
                else
                {
                    id = userSelId != null ? userSelId : authReq.getIdentity();
                    claimed = userSelClaimed != null ? userSelClaimed :
                            authReq.getClaimed();
                }

                if (id == null)
                    throw new ServerException(
                            "No identifier provided by the authentication request " +
                                    "or by the OpenID Provider");

                if (DEBUG) _log.debug("Using ClaimedID: " + claimed +
                        " OP-specific ID: " + id);

                Association assoc = null;
                String handle = authReq.getHandle();
                String invalidateHandle = null;

                if (handle != null)
                {
                    assoc = _sharedAssociations.load(handle);
                    if (assoc == null)
                    {
                        _log.info("Invalidating handle: " + handle);
                        invalidateHandle = handle;
                    }
                    else
                        _log.info("Loaded shared association; handle: " + handle);
                }

                if (assoc == null)
                {
                    assoc = _privateAssociations.generate(
                            _prefAssocSessEnc.getAssociationType(),
                            _expireIn);

                    _log.info("Generated private association; handle: "
                              + assoc.getHandle());
                }

                AuthSuccess response = AuthSuccess.createAuthSuccess(
                            opEndpoint, claimed, id, !isVersion2,
                            authReq.getReturnTo(),
                            isVersion2 ? _nonceGenerator.next() : null,
                            invalidateHandle, assoc, false);

                if (_signFields != null)
                    response.setSignFields(_signFields);

                if (_signExtensions != null)
                    response.setSignExtensions(_signExtensions);

                if (signNow)
                    response.setSignature(assoc.sign(response.getSignedText()));

                _log.info("Returning positive assertion for " +
                          response.getReturnTo());

                return response;
            }
            else // negative response
            {
                if (authReq.isImmediate())
                {
                    _log.error("Responding with immediate authentication " +
                               "failure to " + authReq.getReturnTo());

                    authReq.setImmediate(false);

                    String userSetupUrl = _userSetupUrl == null ? opEndpoint : _userSetupUrl;
                    userSetupUrl += (userSetupUrl.contains("?") ? "&" : "?") + authReq.wwwFormEncoding();

                    return AuthImmediateFailure.createAuthImmediateFailure(
                        userSetupUrl, authReq.getReturnTo(), ! isVersion2);
                }
                else
                {
                    _log.error("Responding with authentication failure to " +
                               authReq.getReturnTo());

                    return new AuthFailure(! isVersion2, authReq.getReturnTo());
                }
            }
        }
        catch (OpenIDException e)
        {
            if (authReq.hasParameter("openid.return_to"))
            {
                _log.error("Error processing authentication request; " +
                           "responding with an indirect error message.", e);

                return IndirectError.createIndirectError(e,
                        authReq.getReturnTo(),
                        ! isVersion2 );
            }
            else
            {
                _log.error("Error processing authentication request; " +
                           "responding with a direct error message.", e);

                return DirectError.createDirectError( e, ! isVersion2 );
            }
        }
    }

    /**
     * Signs an AuthSuccess message, using the association identified by the
     * handle specified within the message.
     *
     * @param   authSuccess     The Authentication Success message to be signed.
     *
     * @throws  ServerException If the Association corresponding to the handle
     *                          in the @authSuccess cannot be retrieved from
     *                          the store.
     * @throws  AssociationException    If the signature cannot be computed.
     *
     */
    public void sign(AuthSuccess authSuccess)
        throws ServerException, AssociationException
    {
        String handle = authSuccess.getHandle();

        // try shared associations first, then private
        Association assoc = _sharedAssociations.load(handle);

        if (assoc == null)
            assoc = _privateAssociations.load(handle);

        if (assoc == null) throw new ServerException(
                "No association found for handle: " + handle);

        authSuccess.setSignature(assoc.sign(authSuccess.getSignedText()));
    }

    /**
     * Responds to a verification request from the consumer.
     *
     * @param requestParams     ParameterList containing the parameters received
     *                          in a verification request from a consumer site.
     * @return                  VerificationResponse to be sent back to the
     *                          consumer site.
     */
    public Message verify(ParameterList requestParams)
    {
        _log.info("Processing verification request...");

        boolean isVersion2 = true;

        try
        {
            // build request message from response params (+ ntegrity check)
            VerifyRequest vrfyReq = VerifyRequest.createVerifyRequest(requestParams);
            isVersion2 = vrfyReq.isVersion2();
            String handle = vrfyReq.getHandle();

            boolean verified = false;

            Association assoc = _privateAssociations.load(handle);

            if (_checkPrivateSharedAssociations && _sharedAssociations.load(handle) != null)
            {
                _log.warn("association for handle: " + handle + " expected to be private " +
                "but was found in shared association store, denying direct verification request; " +
                "please configure different association store/instances for private vs shared associations");
            }
            else if (assoc != null)
            {
                // verify the signature
                _log.info("Loaded private association; handle: " + handle);

                verified = assoc.verifySignature(
                        vrfyReq.getSignedText(),
                        vrfyReq.getSignature());

                // remove the association so that the request
                // cannot be verified more than once
                _privateAssociations.remove(handle);
            }

            VerifyResponse vrfyResp =
                    VerifyResponse.createVerifyResponse(! vrfyReq.isVersion2());

            vrfyResp.setSignatureVerified(verified);

            if (verified)
            {
                String invalidateHandle = vrfyReq.getInvalidateHandle();
                if (invalidateHandle != null &&
                        _sharedAssociations.load(invalidateHandle) == null) {
                    _log.info("Confirming shared association invalidate handle: "
                            + invalidateHandle);

                    vrfyResp.setInvalidateHandle(invalidateHandle);
                }
            }
            else
                _log.error("Signature verification failed, handle: " + handle);


            _log.info("Responding with " + (verified? "positive" : "negative")
                      + " verification response");

            return vrfyResp;
        }
        catch (OpenIDException e)
        {
            _log.error("Error processing verification request; " +
                       "responding with verification error.", e);

            return DirectError.createDirectError(e, ! isVersion2);
        }
    }
}
