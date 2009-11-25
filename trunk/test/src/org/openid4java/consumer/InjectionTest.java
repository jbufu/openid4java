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
package org.openid4java.consumer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

import org.apache.http.Header;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.util.AbstractHttpFetcher;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpFetcher;
import org.openid4java.util.HttpRequestOptions;
import org.openid4java.util.HttpResponse;

import java.util.Map;

public class InjectionTest extends TestCase {

  public void testNonGuice() throws Exception {
    ConsumerManager m = new ConsumerManager();
    HttpFetcher fetcher = m.getHttpFetcher();

    assertTrue(fetcher instanceof HttpCache);
    assertEquals(0, fetcher.getDefaultRequestOptions().getMaxRedirects());

    YadisResolver yadis = m.getDiscovery().getYadisResolver();
    fetcher = yadis.getHttpFetcher();

    assertTrue(fetcher instanceof HttpCache);
    assertEquals(10, fetcher.getDefaultRequestOptions().getMaxRedirects());
  }

  public void testGuiceNoModule() throws Exception {
    Injector injector = Guice.createInjector();
    ConsumerManager m = injector.getInstance(ConsumerManager.class);
    HttpFetcher fetcher = m.getHttpFetcher();

    assertTrue(fetcher instanceof HttpCache);
    assertEquals(0, fetcher.getDefaultRequestOptions().getMaxRedirects());

    YadisResolver yadis = m.getDiscovery().getYadisResolver();
    fetcher = yadis.getHttpFetcher();

    assertTrue(fetcher instanceof HttpCache);
    assertEquals(10, fetcher.getDefaultRequestOptions().getMaxRedirects());
  }

  public void testGuiceInjectedFetcher() throws Exception {
    Injector injector = Guice.createInjector(new TestModule());
    ConsumerManager m = injector.getInstance(ConsumerManager.class);
    HttpFetcher fetcher = m.getHttpFetcher();

    assertTrue(fetcher instanceof TestFetcher);
    assertEquals(0, fetcher.getDefaultRequestOptions().getMaxRedirects());

    YadisResolver yadis = m.getDiscovery().getYadisResolver();
    fetcher = yadis.getHttpFetcher();

    assertTrue(fetcher instanceof TestFetcher);
    assertEquals(10, fetcher.getDefaultRequestOptions().getMaxRedirects());
  }

  private static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(HttpFetcher.class).to(TestFetcher.class);
    }
  }

  private static class TestFetcher extends AbstractHttpFetcher {

    @Override
    public HttpResponse get(String url, HttpRequestOptions requestOptions) {
      return new TestHttpResponse(url, "test");
    }

    @Override
    public HttpResponse head(String url, HttpRequestOptions requestOptions) {
      return new TestHttpResponse(url, "test");
    }

    @Override
    public HttpResponse post(String url, Map<String, String> content, HttpRequestOptions requestOptions) {
      return new TestHttpResponse(url, "test");
    }
  }

  private static class TestHttpResponse implements HttpResponse {

    private final String url;
    private final String body;

    public TestHttpResponse(String url, String body) {
      this.url = url;
      this.body = body;
    }

    public String getBody() {
      return body;
    }

    public String getFinalUri() {
      return url;
    }

    public Header getResponseHeader(String headerName) {
      return null;
    }

    public Header[] getResponseHeaders(String headerName) {
      return new Header[0];
    }

    public int getStatusCode() {
      return 200;
    }

    public boolean isBodySizeExceeded() {
      return false;
    }
  }
}
