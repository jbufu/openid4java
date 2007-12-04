
package org.openid4java.message.encoding;

import java.util.Map;

/**
 * Encodes and decodes a map of OpenID message parameters into a transport specific format.
 */
public interface MessageCodec {

    /**
     * Decode the message.
     * 
     * @param encoded encoded string
     * @return map of parameters
     */
    public Map<String, String> decode(String encoded);

    /**
     * Encode the message.
     * 
     * @param parameters parameters to encode
     * @return encoded format
     */
    public String encode(Map<String, String> parameters);

}