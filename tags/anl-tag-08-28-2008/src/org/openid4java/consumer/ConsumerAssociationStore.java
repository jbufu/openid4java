/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.openid4java.association.Association;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public interface ConsumerAssociationStore
{
    public void save(String opUrl, Association association);

    public Association load(String opUrl, String handle);

    public Association load(String opUrl);

    public void remove(String opUrl, String handle);
}
