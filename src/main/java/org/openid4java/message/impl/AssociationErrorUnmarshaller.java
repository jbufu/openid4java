package org.openid4java.message.impl;

import java.util.Map;

import org.openid4java.message.AssociationError;
import org.openid4java.message.Unmarshaller;


/**
 * AssociationErrorUnmarshaller.
 */
public class AssociationErrorUnmarshaller implements Unmarshaller<AssociationError> {

    /** {@inheritDoc} */
    public AssociationError unmarshall(Map<String, String> parameters) {
        AssociationErrorImpl response = new AssociationErrorImpl();
        
        return response;
    }

}