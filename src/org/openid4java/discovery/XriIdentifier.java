/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.discovery;

import org.openxri.XRI;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class XriIdentifier implements Identifier
{
    private XRI _xriIdentifier;

    public XriIdentifier(String identifier) throws DiscoveryException
    {
        // should be in the canonical form
        _xriIdentifier = new XRI(identifier);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        final XriIdentifier that = (XriIdentifier) o;

        // workaround, XRI should properly implement equals
        String thisNormalForm = this.toIRINormalForm();
        String thatNormalForm = that.toIRINormalForm();

        return thisNormalForm.equals(thatNormalForm);
    }

    public int hashCode()
    {
        return _xriIdentifier.hashCode();
    }

    public String getIdentifier()
    {
        return _xriIdentifier.toString();
    }

    public String toString()
    {
        return _xriIdentifier.toString();
    }

    public String toIRINormalForm()
    {
        return _xriIdentifier.toIRINormalForm();
    }

    public String toURINormalForm()
    {
        return _xriIdentifier.toURINormalForm();
    }
}
