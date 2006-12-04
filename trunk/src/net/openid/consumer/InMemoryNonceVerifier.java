/*
 * Copyright 2006 Sxip Identity Corporation
 */

package net.openid.consumer;

import net.openid.util.InternetDateFormat;

import java.util.*;
import java.text.ParseException;

/**
 * @author Marius Scurtescu, Johnny Bufu
 */
public class InMemoryNonceVerifier implements NonceVerifier
{
    private static InternetDateFormat _dateFormat = new InternetDateFormat();
    private Map _idpMap = new HashMap();
    private long _maxAge;

    /**
     * @param maxAge maximum token age in seconds
     */
    public InMemoryNonceVerifier(int maxAge)
    {
        _maxAge = maxAge * 1000;
    }

    public long getMaxAge()
    {
        return _maxAge / 1000;
    }

    public void setMaxAge(long age)
    {
        _maxAge = age * 1000;
    }

    public synchronized int seen(String idpUrl, String nonce)
    {
        Date now = new Date();

        try
        {
            removeAged(now);

            Date nonceDate = _dateFormat.parse(nonce);

            if (isTooOld(now, nonceDate))
                return TOO_OLD;

            Set seenSet = (Set) _idpMap.get(idpUrl);

            if (seenSet == null)
            {
                seenSet = new HashSet();

                _idpMap.put(idpUrl, seenSet);
            }

            if (seenSet.contains(nonce))
                return SEEN;

            seenSet.add(nonce);

            return OK;
        }
        catch (ParseException e)
        {
            return INVALID_TIMESTAMP;
        }
    }

    private synchronized void removeAged(Date now) throws ParseException
    {
        Set idpToRemove = new HashSet();
        Iterator idpUrls = _idpMap.keySet().iterator();
        while (idpUrls.hasNext())
        {
            String idpUrl = (String) idpUrls.next();

            Set seenSet = (Set) _idpMap.get(idpUrl);
            Set nonceToRemove = new HashSet();

            Iterator nonces = seenSet.iterator();
            while (nonces.hasNext())
            {
                String nonce = (String) nonces.next();

                Date nonceDate = _dateFormat.parse(nonce);

                if (isTooOld(now, nonceDate))
                {
                    nonceToRemove.add(nonce);
                }
            }

            nonces = nonceToRemove.iterator();
            while (nonces.hasNext())
            {
                String nonce = (String) nonces.next();

                seenSet.remove(nonce);
            }

            if (seenSet.size() == 0)
                idpToRemove.add(idpUrl);
        }

        idpUrls = idpToRemove.iterator();
        while (idpUrls.hasNext())
        {
            String idpUrl = (String) idpUrls.next();
            
            _idpMap.remove(idpUrl);
        }
    }

    private boolean isTooOld(Date now, Date nonce)
    {
        long age = now.getTime() - nonce.getTime();

        return age > _maxAge;
    }
}
