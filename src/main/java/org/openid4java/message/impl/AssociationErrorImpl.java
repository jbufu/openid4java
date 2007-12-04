
package org.openid4java.message.impl;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;
import org.openid4java.message.AssociationError;


/**
 * AssociationErrorImpl.
 */
public class AssociationErrorImpl extends AbstractMessage implements AssociationError {

    /**
     * Association type.
     */
    private AssociationType associationType;

    /**
     * Error message.
     */
    private String error;

    /**
     * Error code.
     */
    private String errorCode;

    /**
     * Association session type.
     */
    private SessionType sessionType;

    /** {@inheritDoc} */
    public AssociationType getAssociationType() {
        return associationType;
    }

    /** {@inheritDoc} */
    public String getError() {
        return error;
    }

    /** {@inheritDoc} */
    public String getErrorCode() {
        return errorCode;
    }

    /** {@inheritDoc} */
    public SessionType getSessionType() {
        return sessionType;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return AssociationError.ERROR_CODE;
    }

    /**
     * Set the association type.
     * 
     * @param newAssociationType the associationType to set
     */
    public void setAssociationType(AssociationType newAssociationType) {
        associationType = newAssociationType;
    }

    /**
     * Set the error message.
     * 
     * @param newError the error to set
     */
    public void setError(String newError) {
        error = newError;
    }

    /**
     * Set the error code.
     * 
     * @param newErrorCode the errorCode to set
     */
    public void setErrorCode(String newErrorCode) {
        errorCode = newErrorCode;
    }

    /**
     * Set the association session type.
     * 
     * @param newSessionType the sessionType to set
     */
    public void setSessionType(SessionType newSessionType) {
        sessionType = newSessionType;
    }

}