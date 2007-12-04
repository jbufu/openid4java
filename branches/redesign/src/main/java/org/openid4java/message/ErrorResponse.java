
package org.openid4java.message;

/**
 * OpenID error response.
 */
public interface ErrorResponse extends Message {

    /**
     * A human-readable message indicating the cause of the error.
     * 
     * @return the error message
     */
    public String getError();

    /**
     * Contact address for the administrator of the sever. The contact address may take any form, as it is intended to
     * be displayed to a person.
     * 
     * @return the contact
     */
    public String getContact();

    /**
     * A reference token, such as a support ticket number or a URL to a news blog, etc.
     * 
     * @return the reference
     */
    public String getReference();

}