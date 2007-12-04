
package org.openid4java.message.ax;

import java.util.List;
import java.util.Map;

import org.openid4java.message.MessageExtension;


/**
 * Request for an OpenID Provider to store identity information for a user.
 */
public interface StoreRequest extends MessageExtension {

    /**
     * Attribute Exchange mode for a store request.
     */
    public static final String MODE = "store_request";

    /**
     * The attributes to store.
     * 
     * @return the attributes
     */
    public Map<String, List<String>> getAttributes();

}