/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.openid4java.util.NameValuePair;

import org.openid4java.message.ax.Attribute;
import org.openid4java.message.ParameterList;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public interface AttributeProvider
{
    // Called post instantiation to set configuration parameters and
    // initialize provider
    public void initialize(NameValuePair[] parameters)
        throws ConfigException;

    public Attribute[] getAttributes(String idpIdentity, ParameterList requestParams)
        throws AttributeProviderException, ConfigException;
}
