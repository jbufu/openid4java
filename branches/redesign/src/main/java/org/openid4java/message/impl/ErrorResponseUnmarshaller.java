
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.ErrorResponse;
import org.openid4java.message.Unmarshaller;


/**
 * ErrorResponseUnmarshaller.
 */
public class ErrorResponseUnmarshaller implements Unmarshaller<ErrorResponse> {

    /** {@inheritDoc} */
    public ErrorResponse unmarshall(Map<String, String> parameters) {
        ErrorResponseImpl response = new ErrorResponseImpl();

        return response;
    }

}