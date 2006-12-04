/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.association.Association;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryConsumerAssociationStore implements ConsumerAssociationStore
{
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

            handleMap.remove(handle);

            if (handleMap.size() == 0)
                _idpMap.remove(idpUrl);
        }
    }


    private synchronized void removeExpired()
    {
        Iterator idpUrls = _idpMap.keySet().iterator();
        while (idpUrls.hasNext())
        {
            String idpUrl = (String) idpUrls.next();

            Map handleMap = (Map) _idpMap.get(idpUrl);

            Iterator handles = handleMap.keySet().iterator();
            while (handles.hasNext())
            {
                String handle = (String) handles.next();

                Association association = (Association) handleMap.get(handle);

                if (association.hasExpired())
                {
                    handleMap.remove(handle);

                    if (handleMap.size() == 0)
                        _idpMap.remove(idpUrl);
                }
            }
        }
    }
}
