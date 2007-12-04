
package org.openid4java.message.impl;

import org.openid4java.message.NegativeAssertion;

/**
 * NegativeAssertionImpl.
 */
public class NegativeAssertionImpl extends AbstractMessage implements NegativeAssertion {

    /**
     * OpenID mode.
     */
    private String mode;

    /** {@inheritDoc} */
    public String getMode() {
        return mode;
    }

    /**
     * Set mode.
     * 
     * @param newMode new mode to set.
     */
    public void setMode(String newMode) {
        mode = newMode;
    }

}