
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.VerifyRequest;


/**
 * VerifyRequestMarshaller.
 */
public class VerifyRequestMarshaller implements Marshaller<VerifyRequest> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(VerifyRequest object) {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }

}