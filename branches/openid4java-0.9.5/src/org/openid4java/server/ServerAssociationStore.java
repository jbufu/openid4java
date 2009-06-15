/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.server;

import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface ServerAssociationStore
{
    public Association generate(String type, int expiryIn) throws AssociationException;

    public Association load(String handle);

    public void remove(String handle);
}
