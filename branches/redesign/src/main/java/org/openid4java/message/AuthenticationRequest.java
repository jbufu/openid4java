
package org.openid4java.message;

/**
 * OpenID authentication request.
 */
public interface AuthenticationRequest extends Message {

    /**
     * Message mode for authentication requests in which the relying party does not wish for the OpenID Provider to
     * interact with the user.
     */
    public static final String MODE_IMMEDIATE = "checkid_immediate";

    /**
     * Message mode for authentication requests in which the relying party wishes for the OpenID Provider to interact
     * with the user if necessary.
     */
    public static final String MODE_INTERACTIVE = "checkid_setup";

    /**
     * Get the claimed identifier.
     * 
     * @return the claimed identifier
     */
    public String getClaimedId();

    /**
     * Get the OpenID Provider local identifier.
     * 
     * @return the local identifier
     */
    public String getIdentity();

    /**
     * Get the handle for the established association.
     * 
     * @return the association handle
     */
    public String getAssociationHandle();

    /**
     * Get the URL to which the OpenID Provider should return the user with the response.
     * 
     * @return the return-to URL
     */
    public String getReturnTo();

    /**
     * Get the URL pattern the OpenID Provider should ask the end user to trust.
     * 
     * @return the realm URL pattern
     */
    public String getRealm();

}