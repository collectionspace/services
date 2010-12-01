/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.profile;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.servlet.ServletContext;

import org.collectionspace.services.common.ServletTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Install like this:
    C:\src\trunk\services\JaxRsServiceProvider\src\main\webapp\WEB-INF\web.xml
 <filter>
     <filter-name>PayloadFilter</filter-name>
     <filter-class>org.collectionspace.services.common.profile.PayloadFilter</filter-class>
 </filter>
 <filter-mapping>
     <filter-name>PayloadFilter</filter-name>
     <url-pattern>/*</url-pattern>
 </filter-mapping>
 */
public class PayloadFilter implements Filter {
    FilterConfig filterConfig = null;

    public String CRLF = "\r\n";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request != null) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            //String uri = httpRequest.getRequestURI();
            //String method = httpRequest.getMethod();
            RequestWrapper requestWrapper = new RequestWrapper(httpRequest);
            //ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);

            java.io.PrintWriter out = response.getWriter();
            ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);



            // pass the wrappers on to the next entry
            chain.doFilter(requestWrapper, responseWrapper);



            //StringReader sr = new StringReader(new String(responseWrapper.getData()));
            String rsp = responseWrapper.toString();
            response.setContentLength(rsp.length());
            out.write(rsp);

            StringBuffer rqd = new StringBuffer();
            StringBuffer rsd = new StringBuffer();

            rqd.append(httpRequest.getMethod()+' '+ServletTools.getURL(httpRequest)+' '+httpRequest.getProtocol()+CRLF);
            rqd.append(requestWrapper.getHeaderBlock());
            rqd.append(CRLF);
            rqd.append(requestWrapper.getRequestAsString());
            System.out.println("request: =========="+CRLF+rqd.toString());

            rsd.append(responseWrapper.getResponseLine()+CRLF);
            rsd.append(responseWrapper.getHeaderBlock());
            rsd.append("Content-Length: "+rsp.length());
            rsd.append(CRLF);
            rsd.append(rsp);
            //responseWrapper.getContentType() + responseWrapper.getLocale() + responseWrapper.getResponse().)
            System.out.println("response: =========="+CRLF+rsd.toString());
        }
    }

    @Override
    public void init(FilterConfig theFilterConfig) throws ServletException {
        filterConfig = theFilterConfig;
        if (filterConfig != null) {
            // We can initialize using the init-params here which we defined in
            // web.xml)
        }
    }
    @Override

    public void destroy() {
        // Empty method.
    }



}
