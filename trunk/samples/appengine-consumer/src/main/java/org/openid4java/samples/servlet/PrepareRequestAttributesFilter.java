package org.openid4java.samples.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openid4java.consumer.ConsumerManager;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

@Singleton
public class PrepareRequestAttributesFilter implements Filter {

  private final ConsumerManager consumerManager;

  @Inject
  public PrepareRequestAttributesFilter(ConsumerManager consumerManager) {
    this.consumerManager = consumerManager;
  }

  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {
    req.setAttribute("consumermanager", consumerManager);
    chain.doFilter(req, resp);
  }

  public void init(FilterConfig config) {
  }

  public void destroy() {
  }
}
