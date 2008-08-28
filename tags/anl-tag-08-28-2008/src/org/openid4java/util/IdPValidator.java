/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import java.net.URL;
import org.openid4java.util.NameValuePair;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public interface IdPValidator
{
    // Called post instantiation to set configuration
    // parameters and initialize validator
    public void initialize(NameValuePair[] parameters)
        throws ConfigException;

    // Validates the IdP
    public void validate(URL idpEndpoint, String idpIdentity)
        throws IdPInvalidException, ConfigException;
}
