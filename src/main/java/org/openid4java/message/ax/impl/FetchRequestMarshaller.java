
package org.openid4java.message.ax.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.ax.AttributeExchange;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.AttributeExchange.Parameter;
import org.openid4java.util.DatatypeHelper;
import org.openid4java.util.StringUtils;


/**
 * FetchRequestMarshaller.
 */
public class FetchRequestMarshaller implements Marshaller<FetchRequest> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(FetchRequest request) {
        Map<String, String> parameters = new HashMap<String, String>();
        int aliasCount = 0;
        Integer valueCount;
        String aliasName;

        // update URL
        URL policyURL = request.getUpdateURL();
        if (policyURL != null && !DatatypeHelper.isEmpty(policyURL.toString())) {
            parameters.put(Parameter.update_url.toString(), policyURL.toString());
        }

        // required attributes
        List<String> requiredAliases = new ArrayList<String>();
        for (String type : request.getRequiredAttributes()) {
            aliasName = AttributeExchange.ALIAS_PREFIX + (++aliasCount);
            parameters.put(Parameter.type.toString() + "." + aliasName, type);
            requiredAliases.add(aliasName);

            valueCount = request.getAttributeCount().get(type);

            if (valueCount != null && valueCount > 1) {
                parameters.put(Parameter.count.toString() + "." + aliasName, valueCount + "");
            }
        }

        // optional attributes
        List<String> optionalAliases = new ArrayList<String>();
        for (String type : request.getOptionalAttributes()) {
            aliasName = AttributeExchange.ALIAS_PREFIX + (++aliasCount);
            parameters.put(Parameter.type.toString() + "." + aliasName, type);
            optionalAliases.add(aliasName);

            valueCount = request.getAttributeCount().get(type);

            if (valueCount != null && valueCount > 1) {
                parameters.put(Parameter.count.toString() + "." + aliasName, valueCount + "");
            }
        }

        parameters.put(Parameter.required.toString(), StringUtils.join(requiredAliases, ","));
        parameters.put(Parameter.if_available.toString(), StringUtils.join(optionalAliases, ","));

        return parameters;
    }

}