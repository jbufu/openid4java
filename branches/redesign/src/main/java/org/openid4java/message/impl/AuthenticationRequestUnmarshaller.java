
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.AuthenticationRequest;
import org.openid4java.message.Unmarshaller;


/**
 * AuthenticationRequestUnmarshaller.
 */
public class AuthenticationRequestUnmarshaller implements Unmarshaller<AuthenticationRequest> {

    /** {@inheritDoc} */
    public AuthenticationRequest unmarshall(Map<String, String> parameters) {
        AuthenticationRequestImpl request = new AuthenticationRequestImpl();

        return request;
    }

}