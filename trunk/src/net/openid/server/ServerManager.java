/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.message.*;
import net.openid.association.AssociationSessionType;
import net.openid.association.AssociationException;
import net.openid.association.DiffieHellmanSession;
import net.openid.association.Association;
import net.openid.OpenIDException;

/**
 * Manages OpenID communications with an OpenID Relying Party (Consumer).
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class ServerManager
{
    /**
     * Keeps track of the associations established with consumer sites.
     */
    private static ServerAssociationStore _sharedAssociations;

    /**
     * Keeps track of private (internal) associations created for signing
     * authentication responses for stateless consumer sites.
     */
    private static ServerAssociationStore _privateAssociations;

    /**
     * Nonce generator implementation.
     */
    private static NonceGenerator _nonceGenerator
            = new IncrementalNonceGenerator();

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
     * The URL at the OpenID Provider where the user should be directed when
     * a immediate authentication request fails.
     */
    private String _userSetupUrl = null;

    /**
     * List of coma-separated fields to be signed in authentication responses.
     */
    private String _signList;

    /**
     * Used to perform verify realms against return_to URLs.
     */
    private RealmVerifier _realmVerifier;


    /**
     * Gets the store implementation used for keeping track of the generated
     * associations established with consumer sites.
     *
     * @see ServerAssociationStore
     */
    public static ServerAssociationStore getSharedAssociations()
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
    public static void setSharedAssociations(
            ServerAssociationStore sharedAssociations)
    {
        ServerManager._sharedAssociations = sharedAssociations;
    }

    /**
     * Gets the store implementation used for keeping track of the generated
     * private associations (used for signing responses to stateless consumer
     * sites).
     *
     * @see ServerAssociationStore
     */
    public static ServerAssociationStore getPrivateAssociations()
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
    public static void setPrivateAssociations(
            ServerAssociationStore privateAssociations)
    {
        ServerManager._privateAssociations = privateAssociations;
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
    public static NonceGenerator getNonceGenerator()
    {
        return _nonceGenerator;
    }

    /**
     * Sets the NonceGenerator implementation that will be used to generate
     * nonce tokens to uniquely identify authentication responses.
     *
     * @see NonceGenerator
     */
    public static void setNonceGenerator(NonceGenerator nonceGenerator)
    {
        ServerManager._nonceGenerator = nonceGenerator;
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
     * Coma-separated list, in the desired order.
     */
    public void setSignList(String _signList)
    {
        this._signList = _signList;
    }

    /**
     * Gets the list of parameters that the OpenID Provider will sign when
     * generating authentication responses.
     * <p>
     * Coma-separated list, in the desired order.
     */
    public String getSignList()
    {
        return _signList;
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

                return AssociationResponse.createAssociationResponse(assocReq, assoc);
            }
        }
        catch (OpenIDException e)
        {
            // association failed, respond accordingly
            if (isVersion2)
            {
                return AssociationError.createAssociationError(
                        e.getMessage(), _prefAssocSessEnc);
            }
            else
            {
                try
                {
                // generate dummy association & no-encryption response
                // for compatibility mode
                Association dummyAssoc = _sharedAssociations.generate(
                        Association.TYPE_HMAC_SHA1, 0);

                AssociationRequest dummyRequest =
                        AssociationRequest.createAssociationRequest(
                        AssociationSessionType.NO_ENCRYPTION_COMPAT_SHA1MAC);

                return AssociationResponse.createAssociationResponse(dummyRequest, dummyAssoc);
                }
                catch (OpenIDException ee)
                {
                    // todo: log error: canot send any association response
                    return null;
                }

            }

        }
    }

    /**
     * Processes a Authentication Request received from a consumer site.
     *
     * todo: how is the opEndpoint obtained?
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
     *
     * @return                  <ul><li> AuthSuccess, if authenticatedAndApproved
     *                          <li> AuthFailure (negative response) if either
     *                          of authenticatedAndApproved is false;
     *                          <li> A IndirectError or DirectError message
     *                          if the authentication could not be performed, or
     *                          <li> Null if there was no return_to parameter
     *                          specified in the AuthRequest.</ul>
     */
    public Message authResponse(String opEndpoint,
                                ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved)
    {
        boolean isVersion2 = true;

        try
        {
            // build request message from response params (+ integrity check)
            AuthRequest authReq = AuthRequest.createAuthRequest(
                    requestParams, _realmVerifier);
            isVersion2 = authReq.isVersion2();

            String id;
            String claimed;

            if (AuthRequest.SELECT_ID.equals(authReq.getIdentity()))
            {
                id = userSelId;
                claimed = userSelClaimed;
            } else
            {
                id = userSelId != null ? userSelId : authReq.getIdentity();
                claimed = userSelClaimed != null ? userSelClaimed :
                        authReq.getClaimed();
            }

            if (id == null)
                throw new ServerException(
                        "No identifier provided by the authntication request" +
                                "or by the OpenID Provider");

            if (authenticatedAndApproved) // positive response
            {
                Association assoc = null;
                String handle = authReq.getHandle();
                String invalidateHandle = null;

                if (handle != null)
                {
                    assoc = _sharedAssociations.load(handle);
                    if (assoc == null) invalidateHandle = handle;
                }

                if (assoc == null)
                    assoc = _privateAssociations.generate(
                            _prefAssocSessEnc.getAssociationType(),
                            _expireIn);

                if (authReq.getReturnTo() != null)
                    return AuthSuccess.createAuthSuccess(
                            opEndpoint, claimed, id, !authReq.isVersion2(),
                            authReq.getReturnTo(), _nonceGenerator.next(),
                            invalidateHandle, assoc, _signList);
                else
                    return null;

            } else // negative response
            {
                if (authReq.isImmediate())
                    return AuthImmediateFailure.createAuthImmediateFailure(
                            _userSetupUrl, !authReq.isVersion2());
                else
                    return new AuthFailure(! authReq.isVersion2());
            }
        }
        catch (OpenIDException e)
        {
            if (requestParams.hasParameter("openid.return_to"))
                return IndirectError.createIndirectError(e.getMessage(),
                        requestParams.getParameterValue("openid.return_to"),
                        ! isVersion2 );
            else
                return DirectError.createDirectError( e.getMessage(), isVersion2 );
        }
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

            // confirm shared association handle invalidation
            if (_sharedAssociations.load(vrfyReq.getInvalidateHandle()) == null)
                vrfyResp.setInvalidateHandle(vrfyReq.getInvalidateHandle());

            return vrfyResp;
        }
        catch (OpenIDException e)
        {
            return DirectError.createDirectError(e.getMessage(), ! isVersion2);
        }
    }
}
