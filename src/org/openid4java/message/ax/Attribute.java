/*
 * Copyright 2008 University of Chicago/Argonne National Laboratory
 */

package org.openid4java.message.ax;

import java.util.List;

/**
 * @author Neill Miller, Rachana Ananthakrishnan
 */
public class Attribute
{
    private String alias;
    private String type;

    private List values;

    private boolean required;

    private int count;

    public Attribute()
    {
        this.alias = null;
        this.type = null;
        this.values = null;
        this.required = false;
        this.count = 1;
    }

    public Attribute(String alias, String type, boolean required)
    {
        this.alias = alias;
        this.type = type;
        this.values = null;
        this.required = required;
        this.count = 1;
    }

    public Attribute(String alias, String type, List values)
    {
        this.alias = alias;
        this.type = type;
        this.values = values;
        this.required = false;
        this.count = 1;
    }

    public Attribute(String alias, String type,
                     boolean required, int count)
    {
        this.alias = alias;
        this.type = type;
        this.values = null;
        this.required = required;
        this.count = count;
    }

    public Attribute(String alias, String type, List values,
                     boolean required, int count)
    {
        this.alias = alias;
        this.type = type;
        this.values = values;
        this.required = required;
        this.count = count;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void setValues(List values)
    {
        this.values = values;
    }

    public String getAlias()
    {
        return this.alias;
    }

    public String getType()
    {
        return this.type;
    }

    public List getValues()
    {
        return this.values;
    }

    public boolean getRequired()
    {
        return this.required;
    }

    public int getCount()
    {
        return this.count;
    }
}