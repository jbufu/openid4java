/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

import org.openid4java.util.NameValuePair;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class IdPValidatorConfig
{
    private String className;
    private String configFile;
    private NameValuePair[] parameters;

    public IdPValidatorConfig()
    {
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public void setConfigFile(String configFile)
    {
        this.configFile = configFile;
    }

    public void setParameters(NameValuePair[] parameters)
    {
        this.parameters = parameters;
    }

    public String getClassName()
    {
        return this.className;
    }

    public String getConfigFile()
    {
        return this.configFile;
    }

    public NameValuePair[] getParameters()
    {
        return this.parameters;
    }
}
