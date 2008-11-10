/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.association;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Modells the session and association types allowed in OpenID associations.
 * <p>
 * Association requests and responses must have one of the
 * AssociationSessionType's defined here.
 * <p>
 * Compatibility mode flag defines backwards-compatibile value sets allowed
 * in OpenID 1.x, but not in OpenID 2
 *
 * @see Association DiffieHellmanSession
 * @author Marius Scurtescu, Johnny Bufu
 */
public class AssociationSessionType implements Comparable
{
    private static Log _log = LogFactory.getLog(AssociationSessionType.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    public static final AssociationSessionType NO_ENCRYPTION_SHA1MAC
            = new AssociationSessionType("no-encryption", null,
                        Association.TYPE_HMAC_SHA1, false, 0);

    public static final AssociationSessionType NO_ENCRYPTION_COMPAT_SHA1MAC
            = new AssociationSessionType("", null,
                        Association.TYPE_HMAC_SHA1, true, 1);

    public static final AssociationSessionType NO_ENCRYPTION_SHA256MAC
            = new AssociationSessionType("no-encryption", null,
                        Association.TYPE_HMAC_SHA256, false, 2);

    public static final AssociationSessionType DH_SHA1
            = new AssociationSessionType("DH-SHA1",
                        DiffieHellmanSession.H_ALGORITHM_SHA1,
                        Association.TYPE_HMAC_SHA1, false, 3);

    public static final AssociationSessionType DH_COMPAT_SHA1
            = new AssociationSessionType("DH-SHA1",
                        DiffieHellmanSession.H_ALGORITHM_SHA1,
                        Association.TYPE_HMAC_SHA1, true, 4);

    public static final AssociationSessionType DH_SHA256
            = new AssociationSessionType("DH-SHA256",
                        DiffieHellmanSession.H_ALGORITHM_SHA256,
                        Association.TYPE_HMAC_SHA256, false, 5);

    /**
     * Session type; possible values are 'no-encryption', DH-*;
     * can be blank or null in compatibility mode.
     */
    private String _sessType;

    /**
     * The H algorithm used for Diffie-Hellman sessions.
     * Null for no-encryption sessions.
     */
    private String _hAlgorithm;

    /**
     * Association type; possible values are HMAC-SHA1 and HMAC-SHA256.
     */
    private String _assocType;

    /**
     * Compatibility mode flag defines backwards-compatibile value sets allowed
     * in OpenID 1.x, but not in OpenID 2
     */
    private boolean _compat;

    /**
     * Field used for ordering and comparing the encryption 'level' of
     * AssociationSessionType's.
     *
     * @see #isBetter(AssociationSessionType)
     */
    private int _order;


    /**
     * Creates a AssociationSessionType with all the specified parameters.
     *
     * @param sessType      Session type
     * @param hAlgorithm    H algorithm for Diffie-Hellman sessions
     * @param assocType     Association type
     * @param compat        True for compatibility-mode types, false otherwise
     * @param order         internal order, used for sorting encryption level
     */
    private AssociationSessionType(String sessType, String hAlgorithm,
                                   String assocType, boolean compat, int order)
    {
        _sessType = sessType;
        _hAlgorithm = hAlgorithm;
        _assocType = assocType;
        _compat = compat;
        _order = order;
    }

    /**
     * Creates a OpenID 2 AssociationSessionType with the specified session type
     * and HMAC-SHA1 association type.
     *
     * @param sessType      The session type.
     */
    public static AssociationSessionType create(String sessType)
            throws AssociationException
    {
        return create(sessType, Association.TYPE_HMAC_SHA1);
    }

    /**
     * Creates a OpenID 2 AssociationSessionType with the specified session and
     * association types.
     *
     * @param sessType      The session type.
     * @param assocType     The association type.
     */
    public static AssociationSessionType create(String sessType,
                                                String assocType)
            throws AssociationException
    {
        return create(sessType, assocType, false);
    }


    /**
     * Creates a AssociationSessionType with the specified session and
     * association types.
     * <p>
     * Compatibility flag defines backwards-compatibile value sets allowed
     * in OpenID 1.x, but not in OpenID 2
     *
     * @param sessType                  The session type.
     * @param assocType                 The association type.
     * @param compatibility             True for OpenID 1.x association /
     *                                  session types.
     * @throws AssociationException     For unsupported parameter sets.
     */
    public static AssociationSessionType create(String sessType,
                                                String assocType,
                                                boolean compatibility)
            throws AssociationException
    {
        AssociationSessionType result;

        if(! compatibility && "no-encryption".equals(sessType) &&
                Association.TYPE_HMAC_SHA1.equals(assocType))
                result = NO_ENCRYPTION_SHA1MAC;

        else if (! compatibility && "no-encryption".equals(sessType) &&
                Association.TYPE_HMAC_SHA256.equals(assocType))
                result =  NO_ENCRYPTION_SHA256MAC;

        else if ( compatibility &&
                ("".equals(sessType) || sessType == null) &&
                (Association.TYPE_HMAC_SHA1.equals(assocType) || assocType == null))
        {
            // sess_type:  DH-SHA1, blank, may be omitted in v1 response
            // assoc_type: HMAC_SHA1, may be omitted in v1 requests

            result = NO_ENCRYPTION_COMPAT_SHA1MAC;
        }

        else if (! compatibility && "DH-SHA1".equals(sessType) &&
                Association.TYPE_HMAC_SHA1.equals(assocType))
            result = DH_SHA1;

        else if (compatibility &&
                ("DH-SHA1".equals(sessType) || sessType == null))
            result = DH_COMPAT_SHA1;

        else if (! compatibility && "DH-SHA256".equals(sessType) &&
                Association.TYPE_HMAC_SHA256.equals(assocType) )
            result = DH_SHA256;
        else
            throw new AssociationException(
                    "Unsupported session / association type: "
                    + sessType + " : " + assocType +
                    ", compatibility: " + compatibility);

        if (DEBUG) _log.debug("Session:Association Type: " + result);

        return result;
    }

    /**
     * Gets the session type.
     */
    public String getSessionType()
    {
        return _sessType;
    }

    /**
     * Gets the H algorithm of the Diffie-Hellman session, or null for
     * no-encryption session types.
     */
    public String getHAlgorithm()
    {
        return _hAlgorithm;
    }

    /**
     * Gets the association type.
     */
    public String getAssociationType()
    {
        return _assocType;
    }

    /**
     * Gets the MAC key size, in bits, of this association type.
     */
    public int getKeySize()
    {
        if (Association.TYPE_HMAC_SHA1.equals(_assocType))
            return Association.HMAC_SHA1_KEYSIZE;
        else if (Association.TYPE_HMAC_SHA256.equals(_assocType))
            return Association.HMAC_SHA256_KEYSIZE;
        else
            return 0;
    }

    /**
     * Compares to another AssociationSessionType; used for sorting.
     */
    public int compareTo(Object object)
    {
        AssociationSessionType that = (AssociationSessionType) object;

        if (this._order == that._order)
            return 0;
        else
            return this._order > that._order ? 1 : -1;
    }

    /**
     * Returns true if the specified argument's encryption level is considered
     * better than the one of the current instance.
     */
    public boolean isBetter(AssociationSessionType other)
    {
        return this.compareTo(other) > 0;
    }

    /**
     * Returns true for OpenID 2 AssociationSessionType's, or false for
     * OpenID 1.x types.
     */
    public boolean isVersion2()
    {
        return ! _compat;
    }

    public String toString()
    {
        return _sessType + ":" + _assocType + ":" +
                (_compat ? "OpenID1" : "OpenID2");
    }
}
