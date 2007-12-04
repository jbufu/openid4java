
package org.openid4java.message;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;

/**
 * Successful response to an association request.
 */
public interface AssociationResponse extends Message {

    /**
     * The association handle is used as a key to refer to this association in subsequent messages.
     * 
     * @return the association handle.
     */
    public String getAssociationHandle();

    /**
     * The session encryption method for this association.
     * 
     * @return the session type
     */
    public SessionType getSessionType();

    /**
     * The associate type algorithm for this association.
     * 
     * @return the association type
     */
    public AssociationType getAssociationType();

    /**
     * The lifetime, in seconds, of this association.
     * 
     * @return the lifetime
     */
    public int getLifetime();

    /**
     * The MAC key (shared secret) for this association. Depending on the session type, this may be the plain-text MAC
     * key or encrypted with the secret Diffie-Hellman value.
     * 
     * @return the MAC key
     */
    public String getMACKey();

    /**
     * The OpenID Provider's Diffie-Hellman public key.
     * 
     * @return the DH public key
     */
    public String getDHPublicKey();

}