/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryServerAssociationStoreTest extends ServerAssociationStoreTest
{
    public InMemoryServerAssociationStoreTest(String name)
    {
        super(name);
    }

    public ServerAssociationStore createStore()
    {
        return new InMemoryServerAssociationStore();
    }

    public static Test suite()
    {
        return new TestSuite(InMemoryServerAssociationStoreTest.class);
    }
}
