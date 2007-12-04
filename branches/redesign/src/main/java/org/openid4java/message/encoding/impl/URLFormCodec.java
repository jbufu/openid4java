
package org.openid4java.message.encoding.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.encoding.MessageCodec;


/**
 * Message encoder implementation which produces x-www-urlencoded strings.
 */
public class URLFormCodec implements MessageCodec {

    /** {@inheritDoc} */
    public Map<String, String> decode(String encoded) {
        Map<String, String> parameters = new HashMap<String, String>();
        String key;
        String value;

        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                try {
                    key = URLDecoder.decode(parts[0], "UTF-8");
                    value = URLDecoder.decode(parts[1], "UTF-8");
                    parameters.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // do nothing
                }
            }
        }

        return parameters;
    }

    /** {@inheritDoc} */
    public String encode(Map<String, String> parameters) {
        StringBuffer buffer = new StringBuffer();

        for (String key : parameters.keySet()) {
            try {
                buffer.append(URLEncoder.encode(key, "UTF-8"));
                buffer.append("=");
                buffer.append(URLEncoder.encode(parameters.get(key), "UTF-8"));
                buffer.append("&");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        }

        return buffer.toString();
    }

}