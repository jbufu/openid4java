
package org.openid4java.message;

import java.util.List;

/**
 * Request to verify a positive authentication assertion. All of the data from the assertion to be verified is included
 * in the verification request.
 */
public interface VerifyRequest extends Message {

    /**
     * Message mode for verification requests.
     */
    public static final String MODE = "check_authentication";

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
