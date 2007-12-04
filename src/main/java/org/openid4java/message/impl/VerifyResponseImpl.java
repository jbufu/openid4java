
package org.openid4java.message.impl;

import org.openid4java.message.VerifyResponse;

/**
 * VerifyResponseImpl.
 */
public class VerifyResponseImpl extends AbstractMessage implements VerifyResponse {

    /**
     * Confirmed handle to invalidate.
     */
    private String invalidateHandle;

    /**
     * Whether the signature is valid.
     */
    private boolean valid;

    /** {@inheritDoc} */
    public String getInvalidateHandle() {
        return invalidateHandle;
    }

    /** {@inheritDoc} */
    public boolean isValid() {
        return valid;
    }

    /** {@inheritDoc} */
    public String getMode() {
        return null;
    }

    /**
     * Set invalidate Handle.
     * 
     * @param newInvalidateHandle the invalidateHandle to set
     */
    public void setInvalidateHandle(String newInvalidateHandle) {
        invalidateHandle = newInvalidateHandle;
    }

    /**
     * Set whether the signature is valid.
     * 
     * @param newValid the valid to set
     */
    public void setValid(boolean newValid) {
        valid = newValid;
    }

}