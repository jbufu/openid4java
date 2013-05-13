/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class XriIdentifier implements Identifier
{
    private String identifier;
    private String iriNormalForm;
    private String uriNormalForm;

    public XriIdentifier(String identifier, String iriNormalForm, String uriNormalForm) throws DiscoveryException
    {
        this.identifier = identifier;
        this.iriNormalForm = iriNormalForm;
        this.uriNormalForm = uriNormalForm;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XriIdentifier that = (XriIdentifier) o;

        if (iriNormalForm != null ? !iriNormalForm.equals(that.iriNormalForm) : that.iriNormalForm != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        return (iriNormalForm != null ? iriNormalForm.hashCode() : 0);
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public String toString()
    {
        return identifier;
    }

    public String toIRINormalForm()
    {
        return iriNormalForm;
    }

    public String toURINormalForm()
    {
        return uriNormalForm;
    }
}
