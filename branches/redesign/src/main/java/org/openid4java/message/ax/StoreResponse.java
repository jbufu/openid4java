
package org.openid4java.message.ax;

import org.openid4java.message.MessageExtension;

/**
 * Response to an Attribute Exchange store request.
 */
public interface StoreResponse extends MessageExtension {

    /**
     * Attribute Exchange mode representing a successful response to a store request.
     */
    public static final String MODE_SUCCESS = "store_response_success";

    /**
     * Attribute Exchange mode representing a successful response to a store request.
     */
    public static final String MODE_FAILURE = "store_response_failure";

    /**
     * A human-readable message indicating why the store request failed.
     * 
     * @return the error message
     */
    public String getError();

}