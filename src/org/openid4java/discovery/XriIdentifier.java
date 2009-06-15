/*
 * Copyright 2006-2007 Sxip Identity Corporation
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
        String thisNormalForm = this._xriIdentifier.toIRINormalForm();
        String thatNormalForm = that._xriIdentifier.toIRINormalForm();

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

    public XRI getXriIdentifier()
    {
        return _xriIdentifier;
    }
}
