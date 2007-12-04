
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.NegativeAssertion;
import org.openid4java.message.Unmarshaller;


/**
 * NegativeAssertionUnmarshaller.
 */
public class NegativeAssertionUnmarshaller implements Unmarshaller<NegativeAssertion> {

    /** {@inheritDoc} */
    public NegativeAssertion unmarshall(Map<String, String> parameters) {
        NegativeAssertionImpl response = new NegativeAssertionImpl();

        return response;
    }

}