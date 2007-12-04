
package org.openid4java.message.impl;

import java.util.Collection;

import org.openid4java.message.Message;
import org.openid4java.message.MessageExtension;


/**
 * AbstractMessage.
 */
public abstract class AbstractMessage implements Message {

    /**
     * Message extensions.
     */
    private Collection<MessageExtension> extensions;

    /** {@inheritDoc} */
    public String getNamespace() {
        return Message.OPENID2_NS;
    }

    /** {@inheritDoc} */
    public Collection<MessageExtension> getExtensions() {
        return extensions;
    }
}
