
package org.openid4java.message;

import java.util.Map;

/**
 * Unmarshallers are used to unmarshall a collection of {@link Parameter}s into an OpenID {@link Message} or
 * {@link MessageExtension}.
 * 
 * @param <ObjectType> type of object this unmarshaller handles
 */
public interface Unmarshaller<ObjectType> {

    /**
     * Unmarshall the parameters.
     * 
     * @param parameters parameters
     * @return the OpenID message
     */
    public ObjectType unmarshall(Map<String, String> parameters);
}