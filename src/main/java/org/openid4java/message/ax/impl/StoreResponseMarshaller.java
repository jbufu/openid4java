
package org.openid4java.message.ax.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.ax.StoreResponse;
import org.openid4java.message.ax.AttributeExchange.Parameter;
import org.openid4java.util.DatatypeHelper;


/**
 * StoreResponseMarshaller.
 */
public class StoreResponseMarshaller implements Marshaller<StoreResponse> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(StoreResponse response) {
        Map<String, String> parameters = new HashMap<String, String>();

        if (!DatatypeHelper.isEmpty(response.getError())) {
            parameters.put(Parameter.error.toString(), response.getError());
        }

        return parameters;
    }

}