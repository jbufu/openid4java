
package org.openid4java.message.sreg;

import java.util.EnumMap;

import org.openid4java.message.MessageExtension;
import org.openid4java.message.sreg.SimpleRegistration.Field;


/**
 * Simple Registration extension for an OpenID authentication response.
 */
public interface SimpleRegistrationResponse extends MessageExtension {

    /**
     * Get field map, which maps field names to values.
     * 
     * @return the field map
     */
    public EnumMap<Field, String> getFields();
}