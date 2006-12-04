/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.server;

import net.openid.association.Association;
import net.openid.association.AssociationException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryServerAssociationStore implements ServerAssociationStore
{
    private String _timestamp;
    private int _counter;
    private Map _handleMap;

    public InMemoryServerAssociationStore()
    {
        _timestamp = Long.toString(new Date().getTime());
        _counter   = 0;
        _handleMap = new HashMap();
    }

    public synchronized Association generate(String type, int expiryIn) throws AssociationException
    {
        removeExpired();

        String handle = _timestamp + "-" + _counter++;

        Association association = Association.generate(type, handle, expiryIn);

        _handleMap.put(handle, association);

        return association;
    }

    public synchronized Association load(String handle)
    {
        removeExpired();

        return (Association) _handleMap.get(handle);
    }

    public synchronized void remove(String handle)
    {
        _handleMap.remove(handle);

        removeExpired();
    }

    private synchronized void removeExpired()
    {
        Iterator handles = _handleMap.keySet().iterator();
        while (handles.hasNext())
        {
            String handle = (String) handles.next();

            Association association = (Association) _handleMap.get(handle);

            if (association.hasExpired())
                _handleMap.remove(handle);
        }
    }
}
