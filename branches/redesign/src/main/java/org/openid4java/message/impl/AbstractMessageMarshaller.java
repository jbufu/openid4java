
package org.openid4java.message.impl;

import java.util.HashMap;
import java.util.Map;

import org.openid4java.message.Marshaller;
import org.openid4java.message.Message;


/**
 * AbstractMessageMarshaller.
 * 
 * @param <MessageType> type of message being marshalled
 */
public abstract class AbstractMessageMarshaller<MessageType extends Message> implements Marshaller<MessageType> {

    /** {@inheritDoc} */
    public Map<String, String> marshall(MessageType message) {
        Map<String, String> parameters = new HashMap<String, String>();
        // get
        return parameters;
    }

}