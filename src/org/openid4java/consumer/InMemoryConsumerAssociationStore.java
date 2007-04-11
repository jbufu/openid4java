/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.consumer;

import org.openid4java.association.Association;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryConsumerAssociationStore implements ConsumerAssociationStore
{
    private static Logger _log = Logger.getLogger(InMemoryConsumerAssociationStore.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private Map _idpMap = new HashMap();

    public synchronized void save(String idpUrl, Association association)
    {
        removeExpired();

        Map handleMap = (Map) _idpMap.get(idpUrl);

        if (handleMap == null)
        {
            handleMap = new HashMap();


            _idpMap.put(idpUrl, handleMap);
        }

        String handle = association.getHandle();

        if(DEBUG)
            _log.debug("Adding association to the in-memory store: " + handle +
                       " with OP: " + idpUrl);

        handleMap.put(association.getHandle(), association);
    }

    public synchronized Association load(String idpUrl, String handle)
    {
        removeExpired();

        if (_idpMap.containsKey(idpUrl))
        {
            Map handleMap = (Map) _idpMap.get(idpUrl);

            if (handleMap.containsKey(handle))
            {
                return (Association) handleMap.get(handle);
            }
        }

        return null;
    }


    public synchronized Association load(String idpUrl)
    {
        removeExpired();

        Association latest = null;

        if (_idpMap.containsKey(idpUrl))
        {
            Map handleMap = (Map) _idpMap.get(idpUrl);

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

    public synchronized void remove(String idpUrl, String handle)
    {
        removeExpired();

        if (_idpMap.containsKey(idpUrl))
        {
            Map handleMap = (Map) _idpMap.get(idpUrl);

            _log.info("Removing association: " + handle + " widh OP: " + idpUrl);

            handleMap.remove(handle);

            if (handleMap.size() == 0)
                _idpMap.remove(idpUrl);
        }
    }


    private synchronized void removeExpired()
    {
        Set idpToRemove = new HashSet();
        Iterator idpUrls = _idpMap.keySet().iterator();
        while (idpUrls.hasNext())
        {
            String idpUrl = (String) idpUrls.next();

            Map handleMap = (Map) _idpMap.get(idpUrl);

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
                          " with OP: " + idpUrl);

                handleMap.remove(handle);
            }

            if (handleMap.size() == 0)
                idpToRemove.add(idpUrl);
        }

        idpUrls = idpToRemove.iterator();
        while (idpUrls.hasNext())
        {
            String idpUrl = (String) idpUrls.next();

            _idpMap.remove(idpUrl);
        }
    }

    protected synchronized int size()
    {
        int total = 0;

        Iterator idpUrls = _idpMap.keySet().iterator();
        while (idpUrls.hasNext())
        {
            String idpUrl = (String) idpUrls.next();

            Map handleMap = (Map) _idpMap.get(idpUrl);

            total += handleMap.size();
        }

        return total;
    }
}
