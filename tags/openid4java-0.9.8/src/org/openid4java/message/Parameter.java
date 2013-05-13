/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.message;

import java.io.Serializable;

/**
 * A key / value pair which is part of an OpenID message.
 *
 * @author Marius Scurtescu, Johnny Bufu
 * @see ParameterList
 */
public class Parameter implements Comparable, Serializable
{
    private String _key;
    private String _value;

    public Parameter(String key, String value)
    {
        _key   = key;
        _value = value;
    }

    public boolean isValid()
    {
        return !((_key != null && _key.indexOf(':') > -1) ||
                (_key != null && _key.indexOf('\n') > -1) ||
                (_value != null && _value.indexOf('\n') > -1));

        //throw new IllegalArgumentException(
        //        "Invalid characters (colon or newline) found in the " +
        //        "key and/or value: \nkey=" + _key + "\nvalue=" + _value );
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        final Parameter that = (Parameter) obj;

        if (this._key == null ? that._key != null : !this._key.equals(that._key))
            return false;

        return (this._value == null ? that._value == null : this._value.equals(that._value));
    }

    public int hashCode()
    {
        int hash;

        hash = (_key != null ? _key.hashCode() : 0);
        hash = 29 * hash + (_value != null ? _value.hashCode() : 0);

        return hash;
    }

    public String getKey()
    {
        return _key;
    }

    public String getValue()
    {
        return _value;
    }

    public int compareTo(Object obj)
    {
        Parameter that = (Parameter) obj;

        int keyComp = this._key.compareTo(that._key);

        if (keyComp == 0) {
            return this._value.compareTo(that._value);
        } else {
            return keyComp;
        }
    }

    public String toString()
    {
        return _key + ":" + _value;
    }
}
