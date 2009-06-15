package org.openid4java.discovery.xrds;

import java.util.Set;
import java.util.Arrays;

/**
 * Encapsulates the (OpenID-related) information extracted in
 * service elements discovered through Yadis.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 * Only the URI priority and Service priority fields are used for comparison.
 *
 * @author jbufu
 */
public class XrdsServiceEndpoint implements Comparable {

    private int servicePriority;
    private int uriPriority;
    private Set types;
    private String uri;
    private String localId;
    private String delegate;
    public static final int LOWEST_PRIORITY = -1;
    private String canonicalId;

    public XrdsServiceEndpoint(String uri, Set types,
                        int servicePriority, int uriPriority, String localId, String delegate, String canonicalId)
    {
        this.servicePriority = servicePriority;
        this.uriPriority = uriPriority;
        this.types = types;
        this.uri = uri;
        this.localId = localId;
        this.delegate = delegate;
        this.canonicalId = canonicalId;
    }

    public int getServicePriority() {
        return servicePriority;
    }

    public void setServicePriority(int servicePriority) {
        this.servicePriority = servicePriority;
    }

    public int getUriPriority() {
        return uriPriority;
    }

    public void setUriPriority(int uriPriority) {
        this.uriPriority = uriPriority;
    }

    public Set getTypes() {
        return types;
    }

    public void setTypes(Set types) {
        this.types = types;
    }

    public boolean matchesType(String type)
    {
        return types != null && types.contains(type);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getDelegate() {
        return delegate;
    }

    public void setDelegate(String delegate) {
        this.delegate = delegate;
    }

    public String getCanonicalId() {
        return canonicalId;
    }

    public void setCanonicalId(String canonicalId) {
        this.canonicalId = canonicalId;
    }

    public int compareTo(Object o) {
        XrdsServiceEndpoint other = (XrdsServiceEndpoint) o;

        if (servicePriority == LOWEST_PRIORITY && other.servicePriority != LOWEST_PRIORITY)
            return 1;
        if (other.servicePriority == LOWEST_PRIORITY && servicePriority != LOWEST_PRIORITY)
            return -1;
        if (servicePriority < other.servicePriority) return -1;
        if (servicePriority > other.servicePriority) return 1;

        if (uriPriority == LOWEST_PRIORITY && other.uriPriority != LOWEST_PRIORITY)
            return 1;
        if (other.uriPriority == LOWEST_PRIORITY && uriPriority != LOWEST_PRIORITY)
            return -1;
        if (uriPriority < other.uriPriority) return -1;
        if (uriPriority > other.uriPriority) return 1;

        // XRI spec says the consumer should pick at random here
        return 0;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Service priority: ").append(servicePriority);
        sb.append("\nType: ").append(types.toString());
        sb.append("\nURI: ").append(uri);
        sb.append("\nURI Priority: ").append(uriPriority);
        sb.append("\nLocalID: ").append(localId);
        return sb.toString();
    }
}
