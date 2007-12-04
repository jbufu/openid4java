
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.NegativeAssertion;


/**
 * NegativeAssertionMarshaller.
 */
public class NegativeAssertionMarshaller implements Marshaller<NegativeAssertion> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(NegativeAssertion object) {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }

}