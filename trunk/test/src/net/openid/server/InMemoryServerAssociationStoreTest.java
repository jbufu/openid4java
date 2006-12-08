/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.openid.association.AssociationException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryServerAssociationStoreTest extends AbstractServerAssociationStoreTest
{
    public InMemoryServerAssociationStoreTest(String name)
    {
        super(name);
    }

    public ServerAssociationStore createStore()
    {
        return new InMemoryServerAssociationStore();
    }

    public void testCleanup() throws AssociationException, InterruptedException
    {
        super.testCleanup();

        InMemoryServerAssociationStore inMemoryAssociationStore = (InMemoryServerAssociationStore) _associationStore;

        assertEquals(1, inMemoryAssociationStore.size());
    }

    public static Test suite()
    {
        return new TestSuite(InMemoryServerAssociationStoreTest.class);
    }
}
