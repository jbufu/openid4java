/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.message.*;
import net.openid.association.AssociationSessionType;
import net.openid.association.AssociationException;
import net.openid.association.DiffieHellmanSession;
import net.openid.association.Association;
import net.openid.OpenIDException;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

/**
 * Manages OpenID communications with an OpenID Relying Party (Consumer).
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ServerManager
{
    private static Logger _log = Logger.getLogger(ServerManager.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * Keeps track of the associations established with consumer sites.
     */
    private ServerAssociationStore _sharedAssociations = new InMemoryServerAssociationStore();

    /**
     * Keeps track of private (internal) associations created for signing
     * authentication responses for stateless consumer sites.
     */
    private ServerAssociationStore _privateAssociations = new InMemoryServerAssociationStore();

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
     * MUST be configured in order for the OpenID provider to be able to
     * respond correctly with AuthImmediateFailure messages in compatibility
     * mode.
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
     * @see #authResponse(net.openid.message.ParameterList, String, String, boolean, String)
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
     * Gets OpenID Provider's endpoint URL, where it accepts OpenID
     * authentication requests.
     * <p>
     * This is a global setting for the ServerManager; can also be set on a
     * per message basis.
     *
     * @see #authResponse(net.openid.message.ParameterList, String, String, boolean, String)
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
     * @see #authResponse(net.openid.message.ParameterList, String, String, boolean, String)
     */
    public void setOPEndpointUrl(String opEndpointUrl)
    {
        this._opEndpointUrl = opEndpointUrl;
    }

    /**
     * Constructs a ServerManager with default settings.
     */
    public ServerManager()
    {
        // initialize a default realm verifier
        _realmVerifier = new RealmVerifier();
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
        boolean isVersion2 = true;

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
                _log.warn("Error processing an OpenID1 association request; " +
                          "responding with a dummy association", e);
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
     * @see #authResponse(net.openid.message.ParameterList, String, String,
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
     * @return      A positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(net.openid.message.ParameterList, String, String,
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
     *
     * @return      An signed positive Authentication Response if successfull,
     *              or an IndirectError / DirectError message.
     * @see #authResponse(net.openid.message.ParameterList, String, String,
     *                    boolean, String, boolean)
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                String opEndpoint)
    {
        return authResponse(requestParams, userSelId, userSelClaimed,
                authenticatedAndApproved, _opEndpointUrl, true);
    }

    /**
     * Processes a Authentication Request received from a consumer site.
     *
     * @param opEndpoint        The endpoint URL where the OP accepts OpenID
     *                          authentication requests.
     * @param requestParams     The parameters contained
     *                          in the authentication request message received
     *                          from a consumer site.
     * @param userSelId         OP-specific Identifier selected by the user at
     *                          the OpenID Provider; if present it will override
     *                          the one received in the authentication request.
     * @param userSelClaimed    Claimed Identifier selected by the user at
     *                          the OpenID Provider; if present it will override
     *                          the one received in the authentication request.
     * @param authenticatedAndApproved  Flag indicating that the IdP has
     *                                  authenticated the user and the user
     *                                  has approved the authentication
     *                                  transaction
     * @param signNow           If true, the returned AuthSuccess will be signed.
     *                          If false, the signature will not be computed and
     *                          set - this will have to be performed later,
     *                          using #sign(net.openid.message.Message).
     *
     * @return                  <ul><li> AuthSuccess, if authenticatedAndApproved
     *                          <li> AuthFailure (negative response) if either
     *                          of authenticatedAndApproved is false;
     *                          <li> A IndirectError or DirectError message
     *                          if the authentication could not be performed, or
     *                          <li> Null if there was no return_to parameter
     *                          specified in the AuthRequest.</ul>
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved,
                                String opEndpoint,
                                boolean signNow)
    {
        _log.info("Processing authentication request...");

        boolean isVersion2 = true;

        try
        {
            new URL(opEndpoint);
        }
        catch (MalformedURLException e)
        {
            _log.error("Invalid OP-endpoint configured; " +
                  "cannot issue OpenID authentication responses." + opEndpoint);

            return DirectError.createDirectError(
                    "Invalid OpenID Provider endpoint URL; " +
                    "cannot issue authentication response", isVersion2);
        }

        try
        {
            // build request message from response params (+ integrity check)
            AuthRequest authReq = AuthRequest.createAuthRequest(
                    requestParams, _realmVerifier);
            isVersion2 = authReq.isVersion2();

            if (authReq.getReturnTo() == null)
            {
                _log.error("Received valid auth request, but no return_to " +
                           "specified; authResponse() should not be called.");
                return null;
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
                        "No identifier provided by the authntication request" +
                        "or by the OpenID Provider");

            if (DEBUG) _log.debug("Using ClaimedID: " + claimed +
                                  " OP-specific ID: " + id);

            if (authenticatedAndApproved) // positive response
            {
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
                        _log.info("Loaded shared association; hadle: " + handle);
                }

                if (assoc == null)
                {
                    assoc = _privateAssociations.generate(
                            _prefAssocSessEnc.getAssociationType(),
                            _expireIn);

                    _log.info("Generated private association; handle: " + handle);
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

                    return AuthImmediateFailure.createAuthImmediateFailure(
                            _userSetupUrl, authReq.getReturnTo(), ! isVersion2);
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
            if (requestParams.hasParameter("openid.return_to"))
            {
                _log.error("Error processing an authentication request; " +
                           "responding with an indirect error message.", e);

                return IndirectError.createIndirectError(e.getMessage(),
                        requestParams.getParameterValue("openid.return_to"),
                        ! isVersion2 );
            }
            else
            {
                _log.error("Error processing an authentication request; " +
                           "responding with an direct error message.", e);

                return DirectError.createDirectError( e.getMessage(), isVersion2 );
            }
        }
    }

    /**
     * Signs an AuthSuccess message, using the association identified by the
     * handle specified within the message.
     */
    public void sign(Message msg) throws ServerException, AssociationException
    {
        if (! (msg instanceof AuthSuccess) ) throw new ServerException(
                    "Cannot sign message of type: " + msg.getClass());

        AuthSuccess authResp = (AuthSuccess) msg;

        String handle = authResp.getHandle();

        // try shared associations first, then private
        Association assoc = _sharedAssociations.load(handle);

        if (assoc == null)
        assoc = _privateAssociations.load(handle);

        if (assoc == null) throw new ServerException(
                "No association found for handle: " + handle);

        authResp.setSignature(assoc.sign(authResp.getSignedText()));
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
            if (assoc != null) // verify the signature
            {
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

            String invalidateHandle = vrfyReq.getInvalidateHandle();
            if (_sharedAssociations.load(invalidateHandle) == null)
            {
                _log.info("Confirming shared association invalidate handle: "
                          + invalidateHandle);

                vrfyResp.setInvalidateHandle(invalidateHandle);
            }

            _log.info("Responding with " + (verified? "positive" : "negative")
                      + " verification response");

            return vrfyResp;
        }
        catch (OpenIDException e)
        {
            _log.error("Error processing verification request; " +
                       "responding with verificatioin error.", e);

            return DirectError.createDirectError(e.getMessage(), ! isVersion2);
        }
    }
}
