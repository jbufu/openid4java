
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.VerifyResponse;


/**
 * VerifyResponseMarshaller.
 */
public class VerifyResponseMarshaller implements Marshaller<VerifyResponse> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(VerifyResponse object) {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }

}