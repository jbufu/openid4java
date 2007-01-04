/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.association.Association;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface ConsumerAssociationStore
{
    public void save(String idpUrl, Association association);

    public Association load(String idpUrl, String handle);

    public Association load(String idpUrl);

    public void remove(String idpUrl, String handle);
}
