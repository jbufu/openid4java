
package org.openid4java.message.ax.impl;

import java.util.Map;

import org.openid4java.message.Unmarshaller;
import org.openid4java.message.ax.StoreResponse;
import org.openid4java.message.ax.AttributeExchange.Parameter;


/**
 * StoreResponseUnmarshaller.
 */
public class StoreResponseUnmarshaller implements Unmarshaller<StoreResponse> {

    /** {@inheritDoc} */
    public StoreResponse unmarshall(Map<String, String> parameters) {
        StoreResponseImpl response = new StoreResponseImpl();

        response.setError(parameters.get(Parameter.error.toString()));

        return response;
    }

}