/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2018 Brien L. Wheeler (brienwheeler@yahoo.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.brienwheeler.apps.tomcat;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.filters.FilterBase;
import org.apache.catalina.filters.HttpHeaderSecurityFilter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class AdditionalHeadersFilter extends FilterBase {
  private static final Log log = LogFactory.getLog(HttpHeaderSecurityFilter.class);

  private String additionalHeaders;
  private transient ArrayList<Header> headers = new ArrayList<Header>();

  public String getAdditionalHeaders() {
    return additionalHeaders;
  }

  public void setAdditionalHeaders(String additionalHeaders) {
    this.additionalHeaders = additionalHeaders;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);

    String headerItems[] = additionalHeaders.split("\\|");
    for (String headerItem : headerItems) {
      headerItem = headerItem.trim();
      int eq = headerItem.indexOf('=');
      if (eq < 0) {
        log.warn("skipping headerItem without equals sign: " + headerItem);
      }
      else {
        headers.add(new Header(headerItem.substring(0, eq).trim(), headerItem.substring(eq + 1).trim()));
      }
    }
  }

  @Override
  protected org.apache.juli.logging.Log getLogger() {
    return log;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if(response instanceof HttpServletResponse) {
      HttpServletResponse httpResponse = (HttpServletResponse)response;
      if(response.isCommitted()) {
        throw new ServletException(sm.getString("AdditionalHeadersFilter.committed"));
      }

      for (Header header : headers) {
        httpResponse.setHeader(header.getName(), header.getValue());
      }
    }

    chain.doFilter(request, response);
  }

  private static class Header {
    private final String name;
    private final String value;

    public Header(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
  }
}
