
package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.AssociationResponse;
import org.openid4java.message.Unmarshaller;


/**
 * AssociationResponseUnmarshaller.
 */
public class AssociationResponseUnmarshaller implements Unmarshaller<AssociationResponse> {

    /** {@inheritDoc} */
    public AssociationResponse unmarshall(Map<String, String> parameters) {
        AssociationResponseImpl response = new AssociationResponseImpl();

        return response;
    }

}