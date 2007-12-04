
package org.openid4java.message.ax.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openid4java.message.ax.StoreRequest;


/**
 * StoreRequestImpl.
 */
public class StoreRequestImpl implements StoreRequest {

    /**
     * Attributes.
     */
    private Map<String, List<String>> attributes;

    /**
     * Constructor.
     */
    public StoreRequestImpl() {
        attributes = new HashMap<String, List<String>>();
    }

    /** {@inheritDoc} */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        return StoreRequest.MODE;
    }

}