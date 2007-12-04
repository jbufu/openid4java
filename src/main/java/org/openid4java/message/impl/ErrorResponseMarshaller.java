
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.ErrorResponse;
import org.openid4java.message.Marshaller;


/**
 * ErrorResponseMarshaller.
 */
public class ErrorResponseMarshaller implements Marshaller<ErrorResponse> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(ErrorResponse object) {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }
}