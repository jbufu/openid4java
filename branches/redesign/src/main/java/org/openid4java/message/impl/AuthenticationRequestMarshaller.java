
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.AuthenticationRequest;
import org.openid4java.message.Marshaller;


/**
 * AuthenticationRequestMarshaller.
 */
public class AuthenticationRequestMarshaller implements Marshaller<AuthenticationRequest> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(AuthenticationRequest object) {
        Map<String, String> parameters = new HashMap<String, String>();

        return parameters;
    }

}