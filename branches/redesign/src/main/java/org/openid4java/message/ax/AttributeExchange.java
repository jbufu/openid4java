
package org.openid4java.message.ax;

/**
 * Attribute Exchange.
 */
public final class AttributeExchange {

    /** Namespace for Attribute Exchange version 1.0. */
    public static final String AX_10_NS = "http://openid.net/srv/ax/1.0";

    /** Attribute Exchange parameters. */
    public static enum Parameter {

        /** Relying party's update URL. */
        update_url,

        /** Required SimpleRegistration fields. */
        required,

        /** Optional SimpleRegistration fields. */
        if_available,

        /** Error message. */
        error,

        /** Attribute type. */
        type,

        /** Number of attribute values. */
        count,

        /** Attribute value. */
        value,
    }

    /** Prefix for AX attribute aliases. */
    public static final String ALIAS_PREFIX = "attr";

}