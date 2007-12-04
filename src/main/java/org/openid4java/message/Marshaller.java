
package org.openid4java.message;

import java.util.Map;

/**
 * Marshallers are used to marshall OpenID {@link Message} and {@link MessageExtensions}s into a map of
 * parameters. Marshallers <b>MUST</b> be thread-safe.
 * 
 * @param <ObjectType> type of object this marshaller handles
 */
public interface Marshaller<ObjectType> {

    /**
     * Marshall the object.
     * 
     * @param object object to marshall
     * @return the parameters
     */
    public Map<String, String> marshall(ObjectType object);
}