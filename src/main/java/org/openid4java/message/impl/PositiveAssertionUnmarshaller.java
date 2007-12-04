
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.PositiveAssertion;
import org.openid4java.message.Unmarshaller;


/**
 * PositiveAssertionUnmarshaller.
 */
public class PositiveAssertionUnmarshaller implements Unmarshaller<PositiveAssertion> {

    /** {@inheritDoc} */
    public PositiveAssertion unmarshall(Map<String, String> parameters) {
        PositiveAssertionImpl response = new PositiveAssertionImpl();
        
        return response;
    }

}