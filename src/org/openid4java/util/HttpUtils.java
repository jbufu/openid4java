/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.AllClientPNames;

public final class HttpUtils
{
    private HttpUtils()
    {
        // empty
    }

    public static void dispose(final org.apache.http.HttpResponse response)
    {
        if (response != null)
        {
            HttpEntity e = response.getEntity();
            if (e != null)
            {
                dispose(e);
            }
        }
    }

    public static void dispose(final HttpEntity entity)
    {
        if (entity != null)
        {
            try
            {
                entity.consumeContent();
            }
            catch (Exception ignored)
            {
                // ignored
            }
        }
    }

    public static void setRequestOptions(HttpRequestBase request, HttpRequestOptions requestOptions)
    {
        request.getParams().setParameter(
                AllClientPNames.MAX_REDIRECTS,
                new Integer(requestOptions.getMaxRedirects()));

        request.getParams().setParameter(AllClientPNames.SO_TIMEOUT,
                                            new Integer(requestOptions.getSocketTimeout()));
        request.getParams().setParameter(AllClientPNames.CONNECTION_TIMEOUT,
                                            new Integer(requestOptions.getConnTimeout()));
    }
}
