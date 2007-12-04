
package org.openid4java.message.impl;

import org.openid4java.message.AuthenticationRequest;

/**
 * AuthenticationRequestImpl.
 */
public class AuthenticationRequestImpl extends AbstractMessage implements AuthenticationRequest {

    /**
     * Association handle.
     */
    private String associationHandle;

    /**
     * Claimed Id.
     */
    private String claimedId;

    /**
     * Local identity.
     */
    private String identity;

    /**
     * Realm.
     */
    private String realm;

    /**
     * Return to address.
     */
    private String returnTo;

    /**
     * OpenID mode.
     */
    private String mode;

    /** {@inheritDoc} */
    public String getAssociationHandle() {
        return associationHandle;
    }

    /** {@inheritDoc} */
    public String getClaimedId() {
        return claimedId;
    }

    /** {@inheritDoc} */
    public String getIdentity() {
        return identity;
    }

    /** {@inheritDoc} */
    public String getRealm() {
        return realm;
    }

    /** {@inheritDoc} */
    public String getReturnTo() {
        return returnTo;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return mode;
    }

    /**
     * Set the association handle.
     * 
     * @param newAssociationHandle the associationHandle to set
     */
    public void setAssociationHandle(String newAssociationHandle) {
        associationHandle = newAssociationHandle;
    }

    /**
     * Set the claimed Id.
     * 
     * @param newClaimedId the claimedId to set
     */
    public void setClaimedId(String newClaimedId) {
        claimedId = newClaimedId;
    }

    /**
     * Set the local identity.
     * 
     * @param newIdentity the identity to set
     */
    public void setIdentity(String newIdentity) {
        identity = newIdentity;
    }

    /**
     * Set the realm.
     * 
     * @param newRealm the realm to set
     */
    public void setRealm(String newRealm) {
        realm = newRealm;
    }

    /**
     * Set the return to address.
     * 
     * @param newReturnTo the returnTo to set
     */
    public void setReturnTo(String newReturnTo) {
        returnTo = newReturnTo;
    }

    /**
     * Set the mode.
     * 
     * @param newMode the mode to set
     */
    public void setMode(String newMode) {
        mode = newMode;
    }

}