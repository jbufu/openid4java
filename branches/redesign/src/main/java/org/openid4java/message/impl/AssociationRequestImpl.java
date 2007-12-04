
package org.openid4java.message.impl;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;
import org.openid4java.message.AssociationRequest;


/**
 * AssociationRequestImpl.
 */
public class AssociationRequestImpl extends AbstractMessage implements AssociationRequest {

    /**
     * Association type.
     */
    private AssociationType associationType;

    /**
     * Association session type.
     */
    private SessionType sessionType;

    /**
     * Relying Party's Diffie-Hellman public key.
     */
    private String dhConsumerPublic;

    /**
     * Diffie-Hellman generator.
     */
    private String dhGen;

    /**
     * Diffie-Hellman modulus.
     */
    private String dhModulus;

    /** {@inheritDoc} */
    public AssociationType getAssociationType() {
        return associationType;
    }

    /** {@inheritDoc} */
    public String getDHConsumerPublic() {
        return dhConsumerPublic;
    }

    /** {@inheritDoc} */
    public String getDHGen() {
        return dhGen;
    }

    /** {@inheritDoc} */
    public String getDHModulus() {
        return dhModulus;
    }

    /** {@inheritDoc} */
    public SessionType getSessionType() {
        return sessionType;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return AssociationRequest.MODE;
    }

    /**
     * Set association type.
     * 
     * @param newAssociationType the associationType to set
     */
    public void setAssociationType(AssociationType newAssociationType) {
        this.associationType = newAssociationType;
    }

    /**
     * Set association session type.
     * 
     * @param newSessionType the sessionType to set
     */
    public void setSessionType(SessionType newSessionType) {
        sessionType = newSessionType;
    }

    /**
     * Set relying party's Diffie-Hellman public key.
     * 
     * @param newDhConsumerPublic the dhConsumerPublic to set
     */
    public void setDhConsumerPublic(String newDhConsumerPublic) {
        dhConsumerPublic = newDhConsumerPublic;
    }

    /**
     * Set the Diffie-Hellman generator.
     * 
     * @param newDhGen the dhGen to set
     */
    public void setDhGen(String newDhGen) {
        dhGen = newDhGen;
    }

    /**
     * Set the Diffie-Hellman modulus.
     * 
     * @param newDhModulus the dhModulus to set
     */
    public void setDhModulus(String newDhModulus) {
        dhModulus = newDhModulus;
    }

}