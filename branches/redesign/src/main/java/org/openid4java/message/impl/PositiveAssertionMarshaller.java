
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.PositiveAssertion;


/**
 * PositiveAssertionMarshaller.
 */
public class PositiveAssertionMarshaller implements Marshaller<PositiveAssertion> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(PositiveAssertion object) {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }

}