/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.util;

public class NameValuePair
{
    private String name;
    private String value;

    public NameValuePair()
    {
        this.name = null;
        this.value = null;
    }

    public NameValuePair(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getName()
    {
        return this.name;
    }

    public String getValue()
    {
        return this.value;
    }
}
