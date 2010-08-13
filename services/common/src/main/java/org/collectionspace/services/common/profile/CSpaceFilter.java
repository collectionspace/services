/**	
 * CSpaceFilter.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.profile;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.ServletContext;

/**
 * The Class CSpaceFilter.
 */
public class CSpaceFilter implements Filter {
	
	/** The filter config. */
	FilterConfig filterConfig = null;

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// Empty method.
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {		
		if (request != null) {
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			StringBuffer uri = new StringBuffer(httpRequest.getRequestURI());
			uri.append(':');
			uri.append(httpRequest.getMethod());
			Profiler profiler = new Profiler(uri.toString(),
					0);
			profiler.start();
			chain.doFilter(request, response);
//			profiler.log(httpRequest.getRequestURI());
			profiler.stop();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig theFilterConfig) throws ServletException {
		filterConfig = theFilterConfig;
		if (filterConfig != null) {
			// We can initialize using the init-params here which we defined in
			// web.xml)
		}
	}

}
