/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import org.openid4java.association.DiffieHellmanSession;
import org.openid4java.association.AssociationSessionType;
import org.openid4java.association.AssociationException;
import org.openid4java.OpenIDException;

import java.util.List;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The OpenID Association Request message.
 * <p>
 * Handles OpenID 2.0 and OpenID 1.x messages.
 *
 * @see AssociationSessionType
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AssociationRequest extends Message
{
    private static Log _log = LogFactory.getLog(AssociationRequest.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final String MODE_ASSOC = "associate";

    protected final static List requiredFields = Arrays.asList( new String[] {
            "openid.mode",
            "openid.session_type",
    });

    protected final static List optionalFields = Arrays.asList( new String[] {
            "openid.ns",                    // not in v1 messages
            "openid.assoc_type",            // can be missing in v1
            "openid.dh_modulus",
            "openid.dh_gen",
            "openid.dh_consumer_public"
    });

    /**
     * The Diffie-Hellman session containing the cryptografic data needed for
     * encrypting the MAC key exchange.
     * <p>
     * Null for no-encryption sessions.
     */
    private DiffieHellmanSession _dhSess;


    /**
     * Creates an Association Request message with the
     * specified association type and "no-encryption" session.
     * <p>
     * The supplied type must be one of the "no-encryption" types, otherwise
     * a DiffieHellman session is required.
     *
     * @see #AssociationRequest(AssociationSessionType, DiffieHellmanSession)
     */
    protected AssociationRequest(AssociationSessionType type)
    {
        this(type, null);
    }

    /**
     * Constructs an AssociationRequest message with the
     * specified association type and Diffie-Hellman session.
     *
     * @param dhSess    Diffie-Hellman session to be used for this association;
     *                  if null, a "no-encryption" session is created.
     */
    protected AssociationRequest(AssociationSessionType type,
                                 DiffieHellmanSession dhSess)
    {
        if (DEBUG)
            _log.debug("Creating association request, type: " + type +
                       "DH session: " + dhSess);

        if (type.isVersion2())
            set("openid.ns", OPENID2_NS);

        set("openid.mode", MODE_ASSOC);
        set("openid.session_type", type.getSessionType());
        set("openid.assoc_type", type.getAssociationType());

        _dhSess = dhSess;

        if (dhSess != null )
        {
            set("openid.dh_consumer_public", _dhSess.getPublicKey());

            // send both diffie-hellman generator and modulus if either are not the default values
            // (this meets v1.1 spec and is compatible with v2.0 spec)

            if (!DiffieHellmanSession.DEFAULT_GENERATOR_BASE64.equals(_dhSess.getGenerator())
                    || !DiffieHellmanSession.DEFAULT_MODULUS_BASE64.equals(_dhSess.getModulus()))
            {
                set("openid.dh_gen", _dhSess.getGenerator());
                set("openid.dh_modulus", _dhSess.getModulus());
            }
        }
    }

    /**
     * Constructs an AssociationRequest message from a parameter list.
     * <p>
     * Useful for processing incoming messages.
     */
    protected AssociationRequest(ParameterList params)
    {
        super(params);
    }

    public static AssociationRequest createAssociationRequest(
            AssociationSessionType type) throws MessageException
    {
        return createAssociationRequest(type, null);
    }

    public static AssociationRequest createAssociationRequest(
            AssociationSessionType type, DiffieHellmanSession dhSess)
            throws MessageException
    {
        AssociationRequest req = new AssociationRequest(type, dhSess);

        // make sure the association / session type matches the dhSess
        if ( type == null ||
                (dhSess == null && type.getHAlgorithm() != null) ||
                (dhSess != null && ! dhSess.getType().equals(type) ) )
            throw new MessageException(
                    "Invalid association / session combination specified: " +
                            type + "DH session: " + dhSess);

        req.validate();

        if (DEBUG) _log.debug("Created association request:\n"
                              + req.keyValueFormEncoding());

        return req;
    }
    public static AssociationRequest createAssociationRequest(
            ParameterList params) throws MessageException
    {
        AssociationRequest req = new AssociationRequest(params);

        req.validate();

        if (DEBUG)
            _log.debug("Created association request from message parameters:\n"
                       + req.keyValueFormEncoding());

        return req;
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
        return hasParameter("openid.ns") &&
                OPENID2_NS.equals(getParameterValue("openid.ns"));
    }

    /**
     * Gets the association type parameter of the message.
     */
    private String getAssociationType()
    {
        return getParameterValue("openid.assoc_type");
    }

    /**
     * Gets the session type parameter of the message.
     */
    private String getSessionType()
    {
        return getParameterValue("openid.session_type");
    }

    /**
     * Gets the association / session type of the association request.
     *
     * @throws AssociationException
     */
    public AssociationSessionType getType() throws AssociationException
    {
        return AssociationSessionType.create(
                getSessionType(), getAssociationType(), ! isVersion2() );
    }

    /**
     * Gets the Diffie-Hellman session
     * Null for no-encryption association requests.
     */
    public DiffieHellmanSession getDHSess()
    {
        return _dhSess;
    }

    /**
     * Gets the Diffie-Hellman modulus parameter of the message, or null for
     * messages with no-encryption sessions.
     */
    public String getDhModulus()
    {
        String modulus = getParameterValue("openid.dh_modulus");

        return modulus != null ?
                    modulus : hasParameter("openid.dh_consumer_public") ?
                    DiffieHellmanSession.DEFAULT_MODULUS_BASE64 : null;
    }

    /**
     * Gets the Diffie-Hellman generator parameter of the message, or null for
     * messages with no-encryption sessions.
     */
    public String getDhGen()
    {
        String gen = getParameterValue("openid.dh_gen");

        return gen != null ?
                gen : hasParameter("openid.dh_consumer_public") ?
                DiffieHellmanSession.DEFAULT_GENERATOR_BASE64 : null;
    }

    /**
     * Gets the Relying Party's (consumer) Diffie-Hellman public key, or null
     * for messages with no-encryption sessions.
     */
    public String getDhPublicKey()
    {
        return getParameterValue("openid.dh_consumer_public");
    }

    /**
     * Checks if the message is a valid OpenID Association Request.
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
            if (type.isVersion2() != isVersion2())
            {
                throw new MessageException("Protocol verison mismatch " +
                    "between association session type: " + type +
                    " and AssociationRequest message type.",
                    OpenIDException.ASSOC_ERROR);
            }

        }
        catch (AssociationException e)
        {
            throw new MessageException(
                "Error verifying association request validity.",
                OpenIDException.ASSOC_ERROR, e);
        }

        // additional compatibility checks
        if (! isVersion2() && getSessionType() == null)
        {
            throw new MessageException(
                "sess_type cannot be omitted in OpenID1 association requests",
                OpenIDException.ASSOC_ERROR);
        }

        // DH seesion parameters
        if ( type.getHAlgorithm() != null && getDhPublicKey() == null)
        {
            throw new MessageException("DH consumer public key not specified.",
                OpenIDException.ASSOC_ERROR);
        }

        // no-enc session
        if (type.getHAlgorithm() == null && (getDhGen() != null ||
                getDhModulus() != null || getDhPublicKey() != null) )
        {
            throw new MessageException(
                "No-encryption session, but DH parameters specified.",
                OpenIDException.ASSOC_ERROR);
        }
    }
}
