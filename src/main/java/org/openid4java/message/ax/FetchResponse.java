
package org.openid4java.message.ax;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.openid4java.message.MessageExtension;


/**
 * Response to an Attribute Exchange fetch request.
 */
public interface FetchResponse extends MessageExtension {

    /**
     * Message mode for attribute exchange fetch responses.
     */
    public static final String MODE = "fetch_response";

    /**
     * AX attributes.
     * 
     * @return the attributes
     */
    public Map<String, List<String>> getAttributes();

    /**
     * The update URL from the AX fetch request, if the OpenID Provider intends to support attribute update.
     * 
     * @return the update URL
     */
    public URL getUpdateURL();

}