/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.openid4java.association.Association;
import org.openid4java.association.DiffieHellmanSession;
import org.openid4java.association.AssociationException;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.OpenIDException;

import java.util.List;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The OpenID Association Response message.
 * <p>
 * Handles OpenID 2.0 and OpenID 1.x messages.
 *
 * @see AssociationSessionType
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AssociationResponse extends Message
{
    private static Log _log = LogFactory.getLog(AssociationResponse.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    protected final static List requiredFields = Arrays.asList( new String[] {
            "assoc_type",
            "assoc_handle",
            "expires_in"
    });
    protected final static List optionalFields = Arrays.asList( new String[] {
            "ns",                       // not in v1 messages
            "session_type",             // can be missing in v1
            "mac_key",
            "enc_mac_key",
            "dh_server_public"
    });

    /**
     * Constructs an AssociationResponse for a given association request.
     *
     * @param assocReq      The association request that needs to be responded.
     * @param assoc         The association which will be used to sign
     *                      authentication responses.
     */
    protected AssociationResponse(AssociationRequest assocReq, Association assoc)
            throws AssociationException
    {
        if (DEBUG)
            _log.debug("Creating association response, type: " + assocReq.getType()
                       + " association handle: " + assoc.getHandle());

        if (assocReq.isVersion2()) set("ns", OPENID2_NS);

        AssociationSessionType type = assocReq.getType();
        setType(type);

        setAssocHandle(assoc.getHandle());

        Long expiryIn = new Long( ( assoc.getExpiry().getTime() -
                                    System.currentTimeMillis() ) / 1000 );
        setExpire(expiryIn);

        if (type.getHAlgorithm() != null) // DH session, encrypt the MAC key
        {
            DiffieHellmanSession dhSess = DiffieHellmanSession.create(
                    type, assocReq.getDhModulus(), assocReq.getDhGen() );

            setPublicKey(dhSess.getPublicKey());

            setMacKeyEnc(dhSess.encryptMacKey(
                    assoc.getMacKey().getEncoded(),
                    assocReq.getDhPublicKey() ));
        }
        else // no-encryption session, unecrypted MAC key
        {
            setMacKey(new String(
                    Base64.encodeBase64(assoc.getMacKey().getEncoded())));
        }
    }

    /**
     * Constructs an AssociationResponse message from a parameter list.
     * <p>
     * Useful for processing incoming messages.
     */
    protected AssociationResponse(ParameterList params)
    {
        super(params);
    }

    public static AssociationResponse createAssociationResponse(
            AssociationRequest assocReq, Association assoc)
            throws MessageException, AssociationException
    {
        AssociationResponse resp = new AssociationResponse(assocReq, assoc);

        resp.validate();

        if (DEBUG) _log.debug("Created association response:\n"
                              + resp.keyValueFormEncoding());

        return resp;
    }

    public static AssociationResponse createAssociationResponse(ParameterList params)
            throws MessageException
    {
        AssociationResponse resp = new AssociationResponse(params);

        resp.validate();

        if (DEBUG)
            _log.debug("Created association response from message parameters:\n"
                       + resp.keyValueFormEncoding() );
        return resp;
    }

    public List getRequiredFields()
    {
        return requiredFields;
    }

    /**
     * Returns true for OpenID 2.0 messages, false otherwise.
     */
    public boolean isVersion2()
    {
        return hasParameter("ns") && OPENID2_NS.equals(getParameterValue("ns"));
    }

    /**
     * Gets the association type parameter of the message.
     */
    private String getAssociationType()
    {
        return getParameterValue("assoc_type");
    }

    /**
     * Gets the session type parameter of the message.
     */
    private String getSessionType()
    {
        return getParameterValue("session_type");
    }

    /**
     * Sets the association / session type for the association response.
     */
    public void setType(AssociationSessionType type)
    {
        set("session_type", type.getSessionType());
        set("assoc_type", type.getAssociationType());
    }

    /**
     * Gets the association / session type of the association response.
     *
     * @throws AssociationException
     */
    public AssociationSessionType getType() throws AssociationException
    {
        return AssociationSessionType.create(
                getSessionType(), getAssociationType(), ! isVersion2() );
    }

    /**
     * Sets the handle of the association.
     */
    public void setAssocHandle(String handle)
    {
        set("assoc_handle", handle);
    }

    /**
     * Sets the lifetime, in seconds, of the association.
     */
    public void setExpire(Long seconds)
    {
        set("expires_in", seconds.toString());
    }

    /**
     * Sets the unecrtypted MAC key of the association.
     * <p>
     * Should be called only for association responses using no-encryption
     * sessions.
     *
     * @param key               The unencrypted MAC key.
     */
    public void setMacKey(String key)
    {
        set("mac_key", key);
    }

    /**
     * Sets the OP's (server's) public key for the association.
     *
     * @param key           The server's public key for the association.
     */
    public void setPublicKey(String key)
    {
        set("dh_server_public", key);
    }

    /**
     * Sets the encrypted MAC key of the association.
     * <p>
     * Should be called only for association responses using Diffie-Hellman
     * sessions.
     *
     * @param key           The encrypted MAC key.
     */
    public void setMacKeyEnc(String key)
    {
        set("enc_mac_key", key);
    }

    /**
     * Checks if the message is a valid OpenID Association Response..
     *
     * @throws MessageException if message validation failed.
     */
    public void validate() throws MessageException
    {
        // basic checks
        super.validate();

        // association / session type checks
        // (includes most of the compatibility stuff)
        AssociationSessionType type;
        try
        {
            // throws exception for invalid session / association types
            type = getType();

            // make sure compatibility mode is the same for type and message
            if (type.isVersion2() ^ isVersion2())
            {
                throw new MessageException(
                    "Protocol verison mismatch between association " +
                    "session type: " + type +
                    " and AssociationResponse message type.",
                    OpenIDException.ASSOC_ERROR);
            }

        }
        catch (AssociationException e)
        {
            throw new MessageException(
                "Error verifying association response validity.",
                OpenIDException.ASSOC_ERROR, e);
        }

        // additional compatibility checks
        if (! isVersion2() && getAssociationType() == null)
        {
            throw new MessageException(
                "assoc_type cannot be omitted in OpenID1 responses",
                OpenIDException.ASSOC_ERROR);
        }

        String macKey;
        if (type.getHAlgorithm() != null) // DH session
        {
            if ( ! hasParameter("dh_server_public") ||
                    ! hasParameter("enc_mac_key") )
            {
                throw new MessageException(
                    "DH public key or encrypted MAC key missing.",
                    OpenIDException.ASSOC_ERROR);
            }
            else
                macKey = getParameterValue("enc_mac_key");
        } else // no-enc session
        {
            if ( !hasParameter("mac_key") )
            {
                throw new MessageException("Missing MAC key.",
                    OpenIDException.ASSOC_ERROR);
            }
            else
                macKey = getParameterValue("mac_key");
        }

        // mac key size
        int macSize = Base64.decodeBase64(macKey.getBytes()).length * 8;

        if ( macSize != type.getKeySize())
        {
            throw new MessageException("MAC key size: " + macSize +
                " doesn't match the association/session type: " + type,
                OpenIDException.ASSOC_ERROR);
        }
    }

    /**
     * Generates an Association object from an Association Response.
     *
     * @param dhSess        The Diffie-Helman session containing the private key
     *                      used to encrypt / decrypt the MAC key exchange.
     *                      Should be null for no-encryption sessions.
     */
    public Association getAssociation(DiffieHellmanSession dhSess)
            throws AssociationException
    {
        if (DEBUG) _log.debug("Retrieving MAC key from association response...");

        String handle = getParameterValue("assoc_handle");
        int expiresIn = Integer.parseInt(
                getParameterValue("expires_in") );

        // get (and decrypt) the MAC key
        byte[] macKey;

        AssociationSessionType type = getType();

        if ( type.getHAlgorithm() != null )
        {
            macKey = dhSess.decryptMacKey(
                    getParameterValue("enc_mac_key"),
                    getParameterValue("dh_server_public") );
            if (DEBUG) _log.debug("Decrypted MAC key (base64): " +
                                  new String(Base64.encodeBase64(macKey)));
        }
        else
        {
            macKey = Base64.decodeBase64(
                    getParameterValue("mac_key").getBytes() );

            if (DEBUG) _log.debug("Unencrypted MAC key (base64): "
                                  + getParameterValue("mac_key"));
        }

        Association assoc;

        if (Association.TYPE_HMAC_SHA1.equals(type.getAssociationType()))
            assoc = Association.createHmacSha1(handle, macKey, expiresIn);

        else if (Association.TYPE_HMAC_SHA256.equals(type.getAssociationType()))
            assoc = Association.createHmacSha256(handle, macKey, expiresIn);

        else
            throw new AssociationException("Unknown association type: " + type);

        if (DEBUG) _log.debug("Created association for handle: " + handle);

        return assoc;
    }
}
