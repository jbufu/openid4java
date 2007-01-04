/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.association.Association;
import net.openid.association.AssociationException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface ServerAssociationStore
{
    public Association generate(String type, int expiryIn) throws AssociationException;

    public Association load(String handle);

    public void remove(String handle);
}
