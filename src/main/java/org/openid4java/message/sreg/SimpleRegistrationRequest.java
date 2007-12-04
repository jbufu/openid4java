
package org.openid4java.message.sreg;

import java.net.URL;
import java.util.EnumSet;

import org.openid4java.message.MessageExtension;
import org.openid4java.message.sreg.SimpleRegistration.Field;


/**
 * Simple Registration extension for an OpenID authentication request.
 */
public interface SimpleRegistrationRequest extends MessageExtension {
    
    /**
     * Fields which, if absent from the response, will prevent the relying party from completing the registration
     * without end user interaction. Fields do not include the "openid.sreg." prefix.
     * 
     * @return the required fields
     */
    public EnumSet<Field> getRequiredFields();

    /**
     * Fields that will be used by the relying party, but whose absence wil not prevent the registration from
     * completing. Fields do not include the "openid.sreg." prefix.
     * 
     * @return the optional fields
     */
    public EnumSet<Field> getOptionalFields();

    /**
     * A URL which the Relying Party provides to give the end user a place to read about how the profile data will be
     * used.
     * 
     * @return the policy URL
     */
    public URL getPolicyURL();
}