/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.openid4java.util.NameValuePair;

import org.openid4java.message.ax.Attribute;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public interface AttributeProvider
{
    // Called post instantiation to set configuration parameters and
    // initialize provider
    public void initialize(NameValuePair[] parameters)
        throws ConfigException;

    public Attribute[] getAttributes(String idpIdentity)
        throws AttributeProviderException, ConfigException;
}
