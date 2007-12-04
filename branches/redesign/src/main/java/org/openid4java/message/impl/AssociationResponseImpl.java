
package org.openid4java.message.impl;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;
import org.openid4java.message.AssociationRequest;
import org.openid4java.message.AssociationResponse;


/**
 * AssociationRequestImpl.
 */
public class AssociationResponseImpl extends AbstractMessage implements AssociationResponse {

    /**
     * Association type.
     */
    private AssociationType associationType;

    /**
     * Association session type.
     */
    private SessionType sessionType;

    /**
     * OpenID Provider's Diffie-Hellman public key.
     */
    private String dhPublicKey;

    /**
     * Lifetime in seconds.
     */
    private int lifetime;

    /**
     * MAC key.
     */
    private String macKey;

    /** {@inheritDoc} */
    public AssociationType getAssociationType() {
        return associationType;
    }

    /** {@inheritDoc} */
    public SessionType getSessionType() {
        return sessionType;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return AssociationRequest.MODE;
    }

    /** {@inheritDoc} */
    public String getAssociationHandle() {
        return null;
    }

    /** {@inheritDoc} */
    public String getDHPublicKey() {
        return dhPublicKey;
    }

    /** {@inheritDoc} */
    public int getLifetime() {
        return lifetime;
    }

    /** {@inheritDoc} */
    public String getMACKey() {
        return macKey;
    }

    /**
     * Set association type.
     * 
     * @param newAssociationType the associationType to set
     */
    public void setAssociationType(AssociationType newAssociationType) {
        associationType = newAssociationType;
    }

    /**
     * Set association session type.
     * 
     * @param newSessionType the sessionType to set
     */
    public void setSessionType(SessionType newSessionType) {
        this.sessionType = newSessionType;
    }

    /**
     * Set OpenID Provider's Diffie-Hellman public key.
     * 
     * @param newDhPublicKey the dhPublicKey to set
     */
    public void setDhPublicKey(String newDhPublicKey) {
        dhPublicKey = newDhPublicKey;
    }

    /**
     * Set lifetime of response in seconds.
     * 
     * @param newLifetime the lifetime to set
     */
    public void setLifetime(int newLifetime) {
        lifetime = newLifetime;
    }

    /**
     * Set the MAC key.
     * 
     * @param newMACKey the macKey to set
     */
    public void setMACKey(String newMACKey) {
        macKey = newMACKey;
    }

}