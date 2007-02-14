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
    public void setSignList(String _signList)
    {
        this._signList = _signList;
    }

    /**
     * Gets the list of parameters that the OpenID Provider will sign when
     * generating authentication responses.
     * <p>
     * Coma-separated list.
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
     * <p>
     * Uses ServerManager's global OpenID Provider endpoint URL.
     *
     * @see #authResponse(net.openid.message.ParameterList, String, String, boolean, String)
     */
    public Message authResponse(ParameterList requestParams,
                                String userSelId,
                                String userSelClaimed,
                                boolean authenticatedAndApproved)
    {
        return authResponse(requestParams, userSelId, userSelClaimed,
                authenticatedAndApproved, _opEndpointUrl);

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
                                String opEndpoint)
    {
        boolean isVersion2 = true;

        try
        {
            new URL(opEndpoint);
        }
        catch (MalformedURLException e)
        {
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
                            opEndpoint, claimed, id, !isVersion2,
                            authReq.getReturnTo(),
                            isVersion2 ? _nonceGenerator.next() : null,
                            invalidateHandle, assoc, _signList);
                else
                    return null;

            } else // negative response
            {
                if (authReq.isImmediate())
                    return AuthImmediateFailure.createAuthImmediateFailure(
                            _userSetupUrl, authReq.getReturnTo(), ! isVersion2);
                else
                    return new AuthFailure(! isVersion2, authReq.getReturnTo());
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