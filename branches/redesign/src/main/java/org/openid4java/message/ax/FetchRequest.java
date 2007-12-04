
package org.openid4java.message.ax;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.openid4java.message.MessageExtension;


/**
 * Attribute Exchange request to receive attributes for a user.
 */
public interface FetchRequest extends MessageExtension {

    /**
     * Attribute Exchange mode for a fetch request.
     */
    public static final String MODE = "fetch_request";

    /**
     * Types for Attributes which are required by the relying party to offer certain functionality.
     * 
     * @return the required attributes
     */
    public List<String> getRequiredAttributes();

    /**
     * Types of optional attributes.
     * 
     * @return the optional attributes
     */
    public List<String> getOptionalAttributes();

    /**
     * Get the number of requested values for each attributes.
     * 
     * @return map of attribute counts
     */
    public Map<String, Integer> getAttributeCount();

    /**
     * URL to which the OpenID Provider may re-post the fetch response message at some later time.
     * 
     * @return the update URL
     */
    public URL getUpdateURL();

}