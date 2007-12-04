
package org.openid4java.message;

import java.util.List;

/**
 * Response to an authentication request indicating that the OpenID Provider was successfully able to authenticate the
 * end user.
 */
public interface PositiveAssertion extends Message {

    /**
     * Message mode indicating a positive assertion.
     */
    public static final String MODE = "id_res";

    /**
     * The OpenID Provider endpoint URL.
     * 
     * @return the endpoint URL
     */
    public String getEndpoint();

    /**
     * The claimed identifier.
     * 
     * @return the claimed identifier
     */
    public String getClaimedId();

    /**
     * The OpenID Provider local identifier.
     * 
     * @return the local identifier
     */
    public String getIdentity();

    /**
     * The return-to URL from the authentication request.
     * 
     * @return return-to URL
     */
    public String getReturnTo();

    /**
     * Unique nonce for this response.
     * 
     * @return the nonce
     */
    public String getResponseNonce();

    /**
     * The association handle from the authentication request, if it was invalid.
     * 
     * @return the invalid handle
     */
    public String getInvalidateHandle();

    /**
     * The association handle that was used to sign this assertion.
     * 
     * @return the association handle
     */
    public String getAssociationHandle();

    /**
     * The message fields that were used to generate the message signature. Fields do not include the "openid." prefix.
     * 
     * @return the signed fields
     */
    public List<String> getSignedFields();

    /**
     * The Base64 encoded signature.
     * 
     * @return the signature
     */
    public String getSignature();

}