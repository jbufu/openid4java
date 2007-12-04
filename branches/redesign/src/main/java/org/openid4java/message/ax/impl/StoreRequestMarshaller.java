
package org.openid4java.message.ax.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.ax.AttributeExchange;
import org.openid4java.message.ax.StoreRequest;
import org.openid4java.message.ax.AttributeExchange.Parameter;


/**
 * StoreRequestMarshaller.
 */
public class StoreRequestMarshaller implements Marshaller<StoreRequest> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(StoreRequest request) {
        Map<String, String> parameters = new HashMap<String, String>();
        int aliasCount = 0;
        int valueCount;
        String aliasName;

        // attributes
        Map<String, List<String>> attributes = request.getAttributes();
        for (String name : attributes.keySet()) {
            List<String> values = attributes.get(name);
            if (values.size() <= 0) {
                continue;
            }

            aliasName = AttributeExchange.ALIAS_PREFIX + (++aliasCount);
            valueCount = 0;

            // add type parameter
            parameters.put(Parameter.type.toString() + "." + aliasName, name);

            if (values.size() == 1) {
                parameters.put(Parameter.value.toString() + "." + aliasName, values.get(0));
            } else {
                parameters.put(Parameter.count.toString() + "." + aliasName, values.size() + "");
                for (String value : values) {
                    parameters.put(Parameter.value.toString() + "." + aliasName + "." + (++valueCount), value);
                }
            }

        }

        return parameters;
    }

}