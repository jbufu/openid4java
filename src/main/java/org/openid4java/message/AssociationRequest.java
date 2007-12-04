
package org.openid4java.message;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;

/**
 * Request for the initiation of an association session.
 */
public interface AssociationRequest extends Message {

    /**
     * Message mode for association requests.
     */
    public static final String MODE = "associate";

    /**
     * Get the preferred association type algorithm.
     * 
     * @return the association type
     */
    public AssociationType getAssociationType();

    /**
     * Get the preferred association session encryption method.
     * 
     * @return the session type
     */
    public SessionType getSessionType();

    /**
     * The Diffie-Hellman modulus.
     * 
     * @return DH modulus
     */
    public String getDHModulus();

    /**
     * The Diffie-Hellman generator.
     * 
     * @return the DH generator
     */
    public String getDHGen();

    /**
     * The Relying Party's Diffie-Hellman public key.
     * 
     * @return the DH key
     */
    public String getDHConsumerPublic();

}