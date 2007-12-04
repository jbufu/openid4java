
package org.openid4java.message.ax.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openid4java.message.ax.AttributeExchange;
import org.openid4java.message.ax.FetchResponse;


/**
 * FetchResponseImpl.
 */
public class FetchResponseImpl implements FetchResponse {

    /**
     * Attributes.
     */
    private Map<String, List<String>> attributes;

    /**
     * Update URL.
     */
    private URL updateURL;

    /**
     * Constructor.
     */
    public FetchResponseImpl() {
        attributes = new HashMap<String, List<String>>();
    }

    /** {@inheritDoc} */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** {@inheritDoc} */
    public URL getUpdateURL() {
        return updateURL;
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        return AttributeExchange.AX_10_NS;
    }

    /**
     * Set update URL.
     * 
     * @param newUpdateURL the updateURL to set
     */
    public void setUpdateURL(URL newUpdateURL) {
        updateURL = newUpdateURL;
    }

}