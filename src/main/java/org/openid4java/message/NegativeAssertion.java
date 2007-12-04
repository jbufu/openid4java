
package org.openid4java.message;

/**
 * Response to an authentication request indication the OpenID Provider was unable to authenticate the end user.
 */
public interface NegativeAssertion extends Message {

    /**
     * Message mode for a negative assertion to an immediate authentcation request.
     */
    public static final String MODE_IMMEDIATE = "setup_needed";

    /**
     * Message mode for a negative assertion to a non-immediate authentication request.
     */
    public static final String MODE_INTERACTIVE = "cancel";
}