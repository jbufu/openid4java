
package org.openid4java.message.impl;

import org.openid4java.message.ErrorResponse;

/**
 * ErrorResponseImpl.
 */
public class ErrorResponseImpl extends AbstractMessage implements ErrorResponse {

    /**
     * Contact.
     */
    private String contact;

    /**
     * Error message.
     */
    private String error;

    /**
     * Error reference.
     */
    private String reference;

    /** {@inheritDoc} */
    public String getContact() {
        return contact;
    }

    /** {@inheritDoc} */
    public String getError() {
        return error;
    }

    /** {@inheritDoc} */
    public String getReference() {
        return reference;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return null;
    }

    /**
     * Set the contact.
     * 
     * @param newContact the contact to set
     */
    public void setContact(String newContact) {
        contact = newContact;
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
     * Set the error reference.
     * 
     * @param newReference the reference to set
     */
    public void setReference(String newReference) {
        reference = newReference;
    }

}