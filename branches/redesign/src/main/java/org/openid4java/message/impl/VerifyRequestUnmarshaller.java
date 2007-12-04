
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.Unmarshaller;
import org.openid4java.message.VerifyRequest;


/**
 * VerifyRequestUnmarshaller.
 */
public class VerifyRequestUnmarshaller implements Unmarshaller<VerifyRequest> {

    /** {@inheritDoc} */
    public VerifyRequest unmarshall(Map<String, String> parameters) {
        VerifyRequestImpl request = new VerifyRequestImpl();

        return request;
    }

}