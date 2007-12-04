package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.AssociationRequest;
import org.openid4java.message.Unmarshaller;


/**
 * AssociationRequestUnmarshaller.
 */
public class AssociationRequestUnmarshaller implements Unmarshaller<AssociationRequest> {

    /** {@inheritDoc} */
    public AssociationRequest unmarshall(Map<String, String> parameters) {
        AssociationRequestImpl request = new AssociationRequestImpl();
        
        return request;
    }

}