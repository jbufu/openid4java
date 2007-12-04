
package org.openid4java.message.impl;

import java.util.ArrayList;
import java.util.List;

import org.openid4java.message.PositiveAssertion;


/**
 * PositiveAssertionImpl.
 */
public class PositiveAssertionImpl extends AbstractMessage implements PositiveAssertion {

    /**
     * Association handle.
     */
    private String associationHandle;

    /**
     * Claimed Id.
     */
    private String claimedId;

    /**
     * Endpoint.
     */
    private String endpoint;

    /**
     * Local identity.
     */
    private String identity;

    /**
     * Invalidate handle.
     */
    private String invalidateHandle;

    /**
     * Response nonce.
     */
    private String responseNonce;

    /**
     * Signature.
     */
    private String signature;

    /**
     * Signed Fields.
     */
    private List<String> signedFields;

    /**
     * Return to address.
     */
    private String returnTo;

    /**
     * Constructor.
     */
    public PositiveAssertionImpl() {
        signedFields = new ArrayList<String>();
    }

    /** {@inheritDoc} */
    public String getAssociationHandle() {
        return associationHandle;
    }

    /** {@inheritDoc} */
    public String getClaimedId() {
        return claimedId;
    }

    /** {@inheritDoc} */
    public String getEndpoint() {
        return endpoint;
    }

    /** {@inheritDoc} */
    public String getIdentity() {
        return identity;
    }

    /** {@inheritDoc} */
    public String getInvalidateHandle() {
        return invalidateHandle;
    }

    /** {@inheritDoc} */
    public String getResponseNonce() {
        return responseNonce;
    }

    /** {@inheritDoc} */
    public String getReturnTo() {
        return returnTo;
    }

    /** {@inheritDoc} */
    public String getSignature() {
        return signature;
    }

    /** {@inheritDoc} */
    public List<String> getSignedFields() {
        return signedFields;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return PositiveAssertion.MODE;
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
     * Set the return to address.
     * 
     * @param newReturnTo the returnTo to set
     */
    public void setReturnTo(String newReturnTo) {
        returnTo = newReturnTo;
    }

    /**
     * Set the endpoint.
     * 
     * @param newEndpoint the endpoint to set
     */
    public void setEndpoint(String newEndpoint) {
        endpoint = newEndpoint;
    }

    /**
     * Set the handle to invalidate.
     * 
     * @param newInvalidateHandle the invalidateHandle to set
     */
    public void setInvalidateHandle(String newInvalidateHandle) {
        invalidateHandle = newInvalidateHandle;
    }

    /**
     * Set the response nonce.
     * 
     * @param newResponseNonce the responseNonce to set
     */
    public void setResponseNonce(String newResponseNonce) {
        responseNonce = newResponseNonce;
    }

    /**
     * Set the signature.
     * 
     * @param newSignature the signature to set
     */
    public void setSignature(String newSignature) {
        signature = newSignature;
    }

}