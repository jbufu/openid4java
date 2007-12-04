
package org.openid4java.message.sreg.impl;

import java.net.URL;
import java.util.EnumSet;

import org.openid4java.message.sreg.SimpleRegistration;
import org.openid4java.message.sreg.SimpleRegistrationRequest;
import org.openid4java.message.sreg.SimpleRegistration.Field;


/**
 * SimpleRegistrationRequestImpl.
 */
public class SimpleRegistrationRequestImpl implements SimpleRegistrationRequest {

    /**
     * Optional Fields.
     */
    private EnumSet<Field> optionalFields;

    /**
     * Required Fields.
     */
    private EnumSet<Field> requiredFields;

    /**
     * Policy URL.
     */
    private URL policyURL;

    /**
     * Default constructor.
     */
    public SimpleRegistrationRequestImpl() {
        optionalFields = EnumSet.noneOf(Field.class);
        requiredFields = EnumSet.noneOf(Field.class);
    }

    /** {@inheritDoc} */
    public EnumSet<Field> getOptionalFields() {
        return optionalFields;
    }

    /** {@inheritDoc} */
    public URL getPolicyURL() {
        return policyURL;
    }

    /** {@inheritDoc} */
    public EnumSet<Field> getRequiredFields() {
        return requiredFields;
    }

    /** {@inheritDoc} */
    public String getNamespace() {
        // TODO handle SREG_10_NS
        return SimpleRegistration.SREG_11_NS;
    }

    /**
     * Set the policy URL.
     * 
     * @param newPolicyURL the policyURL to set
     */
    public void setPolicyURL(URL newPolicyURL) {
        policyURL = newPolicyURL;
    }

}