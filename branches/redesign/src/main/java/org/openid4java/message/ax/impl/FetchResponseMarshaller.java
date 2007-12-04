
package org.openid4java.message.ax.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.ax.AttributeExchange;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.AttributeExchange.Parameter;
import org.openid4java.util.DatatypeHelper;


/**
 * FetchRequestMarshaller.
 */
public class FetchResponseMarshaller implements Marshaller<FetchResponse> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(FetchResponse response) {
        Map<String, String> parameters = new HashMap<String, String>();
        int aliasCount = 0;
        int valueCount;
        String aliasName;

        // update URL
        URL policyURL = response.getUpdateURL();
        if (policyURL != null && !DatatypeHelper.isEmpty(policyURL.toString())) {
            parameters.put(Parameter.update_url.toString(), policyURL.toString());
        }

        // attributes
        Map<String, List<String>> attributes = response.getAttributes();
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