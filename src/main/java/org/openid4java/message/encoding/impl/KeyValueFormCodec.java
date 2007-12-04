
package org.openid4java.message.encoding.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.encoding.MessageCodec;


/**
 * Message encoder implementation which produces Key-Value Form encoded strings.
 */
public class KeyValueFormCodec implements MessageCodec {

    /** {@inheritDoc} */
    public Map<String, String> decode(String encoded) {
        Map<String, String> parameters = new HashMap<String, String>();

        for (String line : encoded.split("\n")) {
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                parameters.put(parts[0], parts[1]);
            }
        }

        return parameters;
    }

    /** {@inheritDoc} */
    public String encode(Map<String, String> parameters) {
        StringBuffer buffer = new StringBuffer();

        for (String key : parameters.keySet()) {
            buffer.append(key);
            buffer.append(":");
            buffer.append(parameters.get(key));
            buffer.append("\n");
        }

        return buffer.toString();
    }

}