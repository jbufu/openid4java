
package org.openid4java.message.ax.impl;

import org.openid4java.message.ax.StoreResponse;

/**
 * StoreResponseImpl.
 */
public class StoreResponseImpl implements StoreResponse {

    /**
     * Whether this response indicates success.
     */
    private boolean success;

    /**
     * Error message.
     */
    private String error;

    /** {@inheritDoc} */
    public String getError() {
        return error;
    }

    /**
     * Set error message.
     * 
     * @param newError new error message
     */
    public void setError(String newError) {
        error = newError;
    }

    /**
     * Get whether this response indicates success.
     * 
     * @return if success
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Set whether this response indicates success.
     * 
     * @param newSuccess if success
     */
    public void setSuccess(boolean newSuccess) {
        success = newSuccess;
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        if (success) {
            return StoreResponse.MODE_SUCCESS;
        } else {
            return StoreResponse.MODE_FAILURE;
        }
    }

}