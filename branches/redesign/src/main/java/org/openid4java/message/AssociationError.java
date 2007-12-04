
package org.openid4java.message;

import org.openid4java.association.Association.AssociationType;
import org.openid4java.association.Association.SessionType;

/**
 * Response to an {@link AssociationRequest} indicating that the OpenID Provider does not support the requested session
 * type or association type.
 */
public interface AssociationError extends Message {

    /**
     * Unsuccessful response error code.
     */
    public static final String ERROR_CODE = "unsupported-type";

    /**
     * A human-readable message indicating why the association request failed.
     * 
     * @return the error message
     */
    public String getError();

    /**
     * Error code. Value must be "unsupported-type".
     * 
     * @return the error code
     */
    public String getErrorCode();

    /**
     * A valid association session type that the OpenID Provider supports.
     * 
     * @return the session type
     */
    public SessionType getSessionType();

    /**
     * A valid association type that the OpenID Provider supports.
     * 
     * @return the association type
     */
    public AssociationType getAssociationType();
}