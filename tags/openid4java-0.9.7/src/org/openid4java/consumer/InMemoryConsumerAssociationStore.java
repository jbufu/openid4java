/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.openid4java.association.Association;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryConsumerAssociationStore implements ConsumerAssociationStore
{
    private static Log _log = LogFactory.getLog(InMemoryConsumerAssociationStore.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private Map _opMap = new HashMap();

    public synchronized void save(String opUrl, Association association)
    {
        removeExpired();

        Map handleMap = (Map) _opMap.get(opUrl);

        if (handleMap == null)
        {
            handleMap = new HashMap();


            _opMap.put(opUrl, handleMap);
        }

        String handle = association.getHandle();

        if(DEBUG)
            _log.debug("Adding association to the in-memory store: " + handle +
                       " with OP: " + opUrl);

        handleMap.put(association.getHandle(), association);
    }

    public synchronized Association load(String opUrl, String handle)
    {
        removeExpired();

        if (_opMap.containsKey(opUrl))
        {
            Map handleMap = (Map) _opMap.get(opUrl);

            if (handleMap.containsKey(handle))
            {
                return (Association) handleMap.get(handle);
            }
        }

        return null;
    }


    public synchronized Association load(String opUrl)
    {
        removeExpired();

        Association latest = null;

        if (_opMap.containsKey(opUrl))
        {
            Map handleMap = (Map) _opMap.get(opUrl);

            Iterator handles = handleMap.keySet().iterator();
            while (handles.hasNext())
            {
                String handle = (String) handles.next();

                Association association = (Association) handleMap.get(handle);

                if (latest == null ||
                        latest.getExpiry().before(association.getExpiry()))
                    latest = association;
            }
        }

        return latest;
    }

    public synchronized void remove(String opUrl, String handle)
    {
        removeExpired();

        if (_opMap.containsKey(opUrl))
        {
            Map handleMap = (Map) _opMap.get(opUrl);

            _log.info("Removing association: " + handle + " widh OP: " + opUrl);

            handleMap.remove(handle);

            if (handleMap.size() == 0)
                _opMap.remove(opUrl);
        }
    }


    private synchronized void removeExpired()
    {
        Set opToRemove = new HashSet();
        Iterator opUrls = _opMap.keySet().iterator();
        while (opUrls.hasNext())
        {
            String opUrl = (String) opUrls.next();

            Map handleMap = (Map) _opMap.get(opUrl);

            Set handleToRemove = new HashSet();
            Iterator handles = handleMap.keySet().iterator();
            while (handles.hasNext())
            {
                String handle = (String) handles.next();

                Association association = (Association) handleMap.get(handle);

                if (association.hasExpired())
                {
                    handleToRemove.add(handle);
                }
            }

            handles = handleToRemove.iterator();
            while (handles.hasNext())
            {
                String handle = (String) handles.next();

                _log.info("Removing expired association: " + handle +
                          " with OP: " + opUrl);

                handleMap.remove(handle);
            }

            if (handleMap.size() == 0)
                opToRemove.add(opUrl);
        }

        opUrls = opToRemove.iterator();
        while (opUrls.hasNext())
        {
            String opUrl = (String) opUrls.next();

            _opMap.remove(opUrl);
        }
    }

    protected synchronized int size()
    {
        int total = 0;

        Iterator opUrls = _opMap.keySet().iterator();
        while (opUrls.hasNext())
        {
            String opUrl = (String) opUrls.next();

            Map handleMap = (Map) _opMap.get(opUrl);

            total += handleMap.size();
        }

        return total;
    }
}
