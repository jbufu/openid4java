
package org.openid4java.message;

/**
 * Response to a verification request.
 */
public interface VerifyResponse extends Message {

    /**
     * Whether the signature of the verification request is valid.
     * 
     * @return if the signature is valid
     */
    public boolean isValid();

    /**
     * The "invalidate_handle" value sent in the verification request, if the OpenID Provider confirms it is invalid.
     * 
     * @return the confirmed invalid handle
     */
    public String getInvalidateHandle();

}