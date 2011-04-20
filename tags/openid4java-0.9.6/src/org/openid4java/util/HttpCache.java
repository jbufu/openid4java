/*
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.openid4java.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Date;

import javax.net.ssl.SSLContext;

/**
 * Wrapper cache around HttpClient providing caching for HTTP requests.
 * Intended to be used to optimize the number of HTTP requests performed
 * during OpenID discovery.
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class HttpCache extends AbstractHttpFetcher
{
    private static Log _log = LogFactory.getLog(HttpCache.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    /**
     * HttpClient used to place the HTTP requests.
     */
    private HttpClient _client;

    /**
     * Cache for GET requests. Map of URL -> HttpResponse.
     */
    private Map _getCache = new HashMap();

    // todo: cache management

    /**
     * Cache for HEAD requests. Map of URL -> HttpResponse.
     */
    private Map _headCache = new HashMap();

    public HttpCache()
    {
    	this(null);
    }
    
    public HttpCache(SSLContext sslContext)
    {
    	this(sslContext, null);
    }
    
    /**
     * Constructs a new HttpCache object, that will be initialized with the
     * default set of HttpRequestOptions.
     *
     * @see HttpRequestOptions
     */
    public HttpCache(SSLContext sslContext, X509HostnameVerifier hostnameVerifier)
    {
        super();
        _client = HttpClientFactory.getInstance(
                getDefaultRequestOptions().getMaxRedirects(),
                getDefaultRequestOptions().getAllowCircularRedirects(),
                getDefaultRequestOptions().getSocketTimeout(),
                getDefaultRequestOptions().getConnTimeout(),
                null, sslContext, hostnameVerifier);
    }

    /**
     * Removes a cached GET response.
     *
     * @param url   The URL for which to remove the cached response.
     */
    private void removeGet(String url)
    {
        if (_getCache.keySet().contains(url))
        {
            _log.info("Removing cached GET response for " + url);
            _getCache.remove(url);
        }
        else
            _log.info("NOT removing cached GET for " + url + " NOT FOUND.");
    }

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#get(java.lang.String, org.openid4java.util.HttpRequestOptions)
     */
    public HttpResponse get(String url, HttpRequestOptions requestOptions)
        throws IOException
    {
        DefaultHttpResponse resp = (DefaultHttpResponse) _getCache.get(url);

        if (resp != null)
        {
            if (match(resp, requestOptions))
            {
                _log.info("Returning cached GET response for " + url);
                return resp;
            } else
            {
                _log.info("Removing cached GET for " + url);
                removeGet(url);
            }
        }

        HttpGet get = new HttpGet(url);

        org.apache.http.HttpResponse httpResponse = null;
        HttpEntity responseEntity = null;

        try
        {
            get.getParams().setParameter(AllClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
            HttpUtils.setRequestOptions(get, requestOptions);

            httpResponse = _client.execute(get);
            responseEntity = httpResponse.getEntity();
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusLine = httpResponse.getStatusLine().getReasonPhrase();

            ResponseBody body = getResponseBody(responseEntity,
                requestOptions.getMaxBodySize());

            resp = new DefaultHttpResponse(statusCode, statusLine,
                    requestOptions.getMaxRedirects(), get.getURI().toString(),
                    httpResponse.getAllHeaders(), body.getBody());
            resp.setBodySizeExceeded(body.isBodyTruncated());

            // save result in cache
            _getCache.put(url, resp);
        }
        finally
        {
            HttpUtils.dispose(responseEntity);
        }

        return resp;
    }

    private List<NameValuePair> toList(Map<String, String> parameters) {
        List<NameValuePair> list = new ArrayList<NameValuePair>(parameters.size());
        for (Entry<String, String> entry : parameters.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    @Override
    public HttpResponse post(String url, Map<String, String> parameters,
        HttpRequestOptions requestOptions) throws IOException {

      // we don't actually cache posts, since they are used for
      // association requests and signature verification

      // build the post message with the parameters from the request
      HttpPost post = new HttpPost(url);

      DefaultHttpResponse resp;
      org.apache.http.HttpResponse httpResponse = null;
      try
      {
          // can't follow redirects on a POST (w/o user intervention)
          post.getParams().setBooleanParameter(AllClientPNames.HANDLE_REDIRECTS, false);
          HttpUtils.setRequestOptions(post, requestOptions);

          post.setEntity(new UrlEncodedFormEntity(toList(parameters), "UTF-8"));

          // place the http call to the OP
          if (DEBUG) _log.debug("Performing HTTP POST on " + url);
          httpResponse = _client.execute(post);
          int statusCode = httpResponse.getStatusLine().getStatusCode();
          String statusLine = httpResponse.getStatusLine().getReasonPhrase();

          ResponseBody body = getResponseBody(httpResponse.getEntity(),
              requestOptions.getMaxBodySize());

          resp = new DefaultHttpResponse(statusCode, statusLine,
                  requestOptions.getMaxRedirects(), post.getURI().toString(),
                  httpResponse.getAllHeaders(), body.getBody());
          resp.setBodySizeExceeded(body.isBodyTruncated());
      }
      finally
      {
          HttpUtils.dispose(httpResponse);
      }
      return resp;
    }

    /**
     * Returns content of an HTTP response entitity, but no more than maxBytes.
     * @throws IOException
     */
    private ResponseBody getResponseBody(HttpEntity response, int maxBodySize) throws IOException {
      InputStream httpBodyInput = response.getContent();
      if (httpBodyInput == null) {
        return new ResponseBody(null, false);
      }

      // trim down maxBodySize if we know the content is smaller than
      // maxBodySize
      if ((response.getContentLength() > 0)
          && (response.getContentLength() < maxBodySize)) {
        maxBodySize = (int) response.getContentLength();
      }

      byte data[] = new byte[maxBodySize];

      int totalRead = 0;
      int currentRead;
      while (totalRead < maxBodySize)
      {
          currentRead = httpBodyInput.read(
                  data, totalRead, maxBodySize - totalRead);

          if (currentRead == -1) break;

          totalRead += currentRead;
      }

      boolean bodySizeExceeded = (httpBodyInput.read() > 0);

      httpBodyInput.close();

      if (DEBUG) _log.debug("Read " + totalRead + " bytes.");

      return new ResponseBody(new String(data, 0, totalRead), bodySizeExceeded);
    }

    private boolean match(DefaultHttpResponse resp, HttpRequestOptions requestOptions)
    {
        // use cache?
        if ( resp != null && ! requestOptions.isUseCache())
        {
            _log.info("Explicit fresh GET requested; removing cached copy");
            return false;
        }

        //is cache fresh?
        if ( resp != null && (requestOptions.getCacheTTLSeconds() >= 0))
        {
            long cacheTTL = requestOptions.getCacheTTLSeconds() * 1000;
            Date now = new Date();
            long currentTime = now.getTime();
            long cacheExpTime = resp.getTimestamp() + cacheTTL;
            if (cacheExpTime < currentTime)
            {

                String cacheExpTimeStr = (new Date(cacheExpTime)).toString();
                _log.info("Cache Expired at " + cacheExpTimeStr + "; removing cached copy");
                return false;
        		
    	    }
        }

        // content type rules
        String requiredContentType = requestOptions.getContentType();
        if (resp != null && requiredContentType != null)
        {
            Header responseContentType = resp.getResponseHeader("content-type");
            if ( responseContentType != null &&
                 responseContentType.getValue() != null &&
                 !responseContentType.getValue().split(";")[0]
                     .equalsIgnoreCase(requiredContentType) )
            {
                _log.info("Cached GET response does not match " +
                    "the required content type, removing.");
                return false;
            }
        }

        if (resp != null &&
            resp.getMaxRedirectsFollowed() > requestOptions.getMaxRedirects())
        {
            _log.info("Cached GET response used " +
                      resp.getMaxRedirectsFollowed() +
                      " max redirects; current requirement is: " +
                      requestOptions.getMaxRedirects());
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.openid4java.util.HttpFetcher#head(java.lang.String, org.openid4java.util.HttpRequestOptions)
     */
    public HttpResponse head(String url, HttpRequestOptions requestOptions)
            throws IOException
    {
        DefaultHttpResponse resp = (DefaultHttpResponse) _headCache.get(url);

        if (resp != null)
        {
            if (match(resp, requestOptions))
            {
                _log.info("Returning cached HEAD response for " + url);
                return resp;
            } else
            {
                _log.info("Removing cached HEAD for " + url);
                removeGet(url);
            }
        }

        HttpHead head = new HttpHead(url);

        org.apache.http.HttpResponse httpResponse = null;
        HttpEntity responseEntity = null;

        try
        {
            head.getParams().setParameter(AllClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
            HttpUtils.setRequestOptions(head, requestOptions);

            httpResponse = _client.execute(head);
            responseEntity = httpResponse.getEntity();

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusLine = httpResponse.getStatusLine().getReasonPhrase();

            resp = new DefaultHttpResponse(statusCode, statusLine,
                    requestOptions.getMaxRedirects(), head.getURI().toString(),
                    httpResponse.getAllHeaders(), null);

            // save result in cache
            _headCache.put(url, resp);
        }
        finally
        {
            HttpUtils.dispose(responseEntity);
        }

        return resp;
    }

    private static class DefaultHttpResponse implements HttpResponse
    {
        /**
         * The status code of the HTTP response.
         */
        private int _statusCode;

        /**
         * The status line of the HTTP response.
         */
        private String _statusLine;

        /**
         * The maximum HTTP redirects limit that was configured
         * when this HTTP response was obtained.
         */
        private int _maxRedirectsFollowed;

        /**
         * The final URI from where the document was obtained,
         * after following redirects.
         */
        private String _finalUri;

        /**
         * Map of header names  List of Header objects of the HTTP response.
         */
        private Map _responseHeaders;

        /**
         * The HTTP response body.
         */
        private String _body;

        /**
         * Flag to indicate if the HTTP response size exceeded the maximum
         * allowed by the (default) HttpRequestOptions.
         */
        private boolean _bodySizeExceeded = false;

        /**
         * timestamp of creation 
         * 
         *(number of milliseconds since January 1, 1970, 00:00:00 GMT)
         */
        private long _timestamp;


        /**
         * Constructs a new HttpResponse with the provided parameters.
         */
        public DefaultHttpResponse(int statusCode, String statusLine,
                            int redirectsFollowed, String finalUri,
                            Header[] responseHeaders, String body)
        {
            _statusCode = statusCode;
            _statusLine = statusLine;

            _maxRedirectsFollowed = redirectsFollowed;
            _finalUri = finalUri;

            _responseHeaders = new HashMap();
            if (responseHeaders != null)
            {
                String headerName;
                Header header;
                for (int i=0; i < responseHeaders.length; i++)
                {
                    // HTTP header names are case-insensitive
                    headerName = responseHeaders[i].getName().toLowerCase();
                    header = responseHeaders[i];

                    List headerList = (List) _responseHeaders.get(headerName);
                    if (headerList != null)
                        headerList.add(responseHeaders[i]);
                    else
                        _responseHeaders.put(headerName,
                            new ArrayList(Arrays.asList(new Header[] {header})));
                }
            }

            _body = body;
            Date now = new Date();
            _timestamp = now.getTime();
        }

        /**
         * Gets the status code of the HttpResponse.
         */
        public int getStatusCode()
        {
            return _statusCode;
        }

        /**
         * Gets the status line of the HttpResponse.
         */
        public String getStatusLine()
        {
            return _statusLine;
        }

        /**
         * Gets the maximum HTTP redirects limit that was configured
         * when this HTTP response was obtained.
         */
        public int getMaxRedirectsFollowed()
        {
            return _maxRedirectsFollowed;
        }

        /**
         * Gets the final URI from where the document was obtained,
         * after following redirects.
         */
        public String getFinalUri()
        {
            return _finalUri;
        }

        /**
         * Gets the first header matching the provided headerName parameter,
         * or null if no header with that name exists.
         */
        public Header getResponseHeader(String headerName)
        {
            List headerList = (List) _responseHeaders.get(headerName.toLowerCase());

            if (headerList != null && headerList.size() > 0)
                return (Header) headerList.get(0);
            else
                return null;
        }

        /**
         * Gets an array of Header objects for the provided headerName parameter.
         */
        public Header[] getResponseHeaders(String headerName)
        {
            List headerList = (List) _responseHeaders.get(headerName.toLowerCase());

            if (headerList != null)
                return (Header[]) headerList.toArray(new Header[headerList.size()]);
            else
                return new Header[]{}; // empty array, same as HttpClient's method
        }

        /**
         * Gets the HttpResponse body.
         */
        public String getBody()
        {
            return _body;
        }

        /**
         * Returns true if the HTTP response size exceeded the maximum
         * allowed by the (default) HttpRequestOptions.
         * @return
         */
        public boolean isBodySizeExceeded()
        {
            return _bodySizeExceeded;
        }


        /**
         * Sets the flag to indicate whether the HTTP response size exceeded
         * the maximum allowed by the (default) HttpRequestOptions.
         */
        public void setBodySizeExceeded(boolean bodySizeExceeded)
        {
            this._bodySizeExceeded = bodySizeExceeded;
        }

        public long getTimestamp()
        {

            return _timestamp;
        }

    }

    private static class ResponseBody {
        private final String body;
        private final boolean bodyIsTruncated;

        public ResponseBody(String body, boolean truncated)
        {
            this.body = body;
            this.bodyIsTruncated = truncated;
        }

        public String getBody()
        {
            return body;
        }

        public boolean isBodyTruncated()
        {
            return bodyIsTruncated;
        }
    }
}
