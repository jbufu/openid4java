
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.message.AssociationRequest;
import org.openid4java.message.Marshaller;
import org.openid4java.message.Message.Parameter;


/**
 * AssociationRequestMarshaller.
 */
public class AssociationRequestMarshaller implements Marshaller<AssociationRequest> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(AssociationRequest request) {
        Map<String, String> parameters = new HashMap<String, String>();

        AssociationType associationType = request.getAssociationType();
        if (associationType != null) {
            parameters.put(Parameter.assoc_type.toString(), associationType.toString());

            if (associationType.equals(AssociationType.HMAC_SHA1)
                    || associationType.equals(AssociationType.HMAC_SHA256)) {

                parameters.put(Parameter.dh_modulus.toString(), request.getDHModulus());
                parameters.put(Parameter.dh_gen.toString(), request.getDHGen());
                parameters.put(Parameter.dh_consumer_public.toString(), request.getDHConsumerPublic());

            }
        }

        parameters.put(Parameter.session_type.toString(), request.getSessionType().toString());

        return parameters;
    }
}