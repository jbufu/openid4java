
package org.openid4java.message.ax.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openid4java.message.ax.AttributeExchange;
import org.openid4java.message.ax.FetchRequest;


/**
 * FetchRequestImpl.
 */
public class FetchRequestImpl implements FetchRequest {

    /**
     * Optional attributes.
     */
    private List<String> optionalAttributes;

    /**
     * Required attributes.
     */
    private List<String> requiredAttributes;

    /**
     * Map of requested number of values for each attribute.
     */
    private Map<String, Integer> attributeCount;

    /**
     * Update URL.
     */
    private URL updateURL;

    /**
     * Constructor.
     */
    public FetchRequestImpl() {
        optionalAttributes = new ArrayList<String>();
        requiredAttributes = new ArrayList<String>();
        attributeCount = new HashMap<String, Integer>();
    }
    
    /** {@inheritDoc} */
    public List<String> getOptionalAttributes() {
        return optionalAttributes;
    }

    /** {@inheritDoc} */
    public List<String> getRequiredAttributes() {
        return requiredAttributes;
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

    /** {@inheritDoc} */
    public Map<String, Integer> getAttributeCount() {
        return attributeCount;
    }

}