/**
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.openid4java.util;

import com.google.inject.ImplementedBy;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for fetching HTTP requests. The default implementation caches
 * responses, but can be replaced by custom implementations.
 */
@ImplementedBy(HttpCache.class)
public interface HttpFetcher {

  /**
   * Returns the default {@link HttpRequestOptions}. Note that this does
   * not return a clone, so manipulating the object returned here will
   * manipulate the {@link HttpRequestOptions} used by the {@link HttpFetcher}.
   */
  public HttpRequestOptions getDefaultRequestOptions();

  /**
   * Gets a clone of the default HttpRequestOptions.
   */
  public HttpRequestOptions getRequestOptions();

  public void setDefaultRequestOptions(HttpRequestOptions defaultOptions);

  /**
   * GETs a HTTP URL. A cached copy will be returned if one exists.
   *
   * @param url       The HTTP URL to GET.
   * @return          A HttpResponse object containing the fetched data.
   *
   * @see HttpResponse
   */
  public HttpResponse get(String url) throws IOException;

  /**
   * GETs a HTTP URL. A cached copy will be returned if one exists and the
   * supplied options match it.
   *
   * @param url       The HTTP URL to GET.
   * @return          A HttpResponse object containing the fetched data.
   *
   * @see HttpRequestOptions, HttpResponse
   */
  public HttpResponse get(String url, HttpRequestOptions requestOptions) throws IOException;

  public HttpResponse head(String url) throws IOException;

  public HttpResponse head(String url, HttpRequestOptions requestOptions) throws IOException;

  public HttpResponse post(String url, Map<String, String> parameters) throws IOException;

  public HttpResponse post(String url, Map<String, String> parameters,
      HttpRequestOptions requestOptions) throws IOException;

}
