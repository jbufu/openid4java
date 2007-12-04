
package org.openid4java.message.sreg.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.sreg.SimpleRegistrationRequest;
import org.openid4java.message.sreg.SimpleRegistration.Parameter;
import org.openid4java.util.DatatypeHelper;
import org.openid4java.util.StringUtils;


/**
 * Marshaller for a simple registration request.
 */
public class SimpleRegistrationRequestMarshaller implements Marshaller<SimpleRegistrationRequest> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(SimpleRegistrationRequest request) {
        Map<String, String> parameters = new HashMap<String, String>();
        String fields;

        // policy URL
        URL policyURL = request.getPolicyURL();
        if (policyURL != null && !DatatypeHelper.isEmpty(policyURL.toString())) {
            parameters.put(Parameter.policy_url.toString(), policyURL.toString());
        }

        // required parameters
        fields = StringUtils.join(request.getRequiredFields(), ",");
        if (!DatatypeHelper.isEmpty(fields)) {
            parameters.put(Parameter.required.toString(), fields);
        }

        // optional parameters
        fields = StringUtils.join(request.getOptionalFields(), ",");
        if (!DatatypeHelper.isEmpty(fields)) {
            parameters.put(Parameter.optional.toString(), fields);
        }

        return parameters;
    }

}