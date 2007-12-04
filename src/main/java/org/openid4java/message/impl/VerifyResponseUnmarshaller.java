
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.Unmarshaller;
import org.openid4java.message.VerifyResponse;


/**
 * VerifyResponseUnmarshaller.
 */
public class VerifyResponseUnmarshaller implements Unmarshaller<VerifyResponse> {

    /** {@inheritDoc} */
    public VerifyResponse unmarshall(Map<String, String> parameters) {
        VerifyResponseImpl response = new VerifyResponseImpl();

        return response;
    }

}