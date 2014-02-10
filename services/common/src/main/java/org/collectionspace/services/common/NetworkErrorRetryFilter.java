/**
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
package org.collectionspace.services.common;

import java.io.IOException;
import java.net.ConnectException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServletTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSpaceFilter.java
 *
 * A filter that performs specified actions at the time
 * each new request is received by the servlet container.
 *
 * This filter is currently used for recording performance
 * metrics for requests to the CollectionSpace services.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 */
public class NetworkErrorRetryFilter implements Filter {
    final Logger logger = LoggerFactory.getLogger(NetworkErrorRetryFilter.class);

    /** The filter config. */
    FilterConfig filterConfig = null;
    private final String CLASS_NAME = this.getClass().getSimpleName();
    
	private long maxRetrySeconds = MAX_RETRY_SECONDS;
	private static final int MAX_RETRY_SECONDS = 5;
    private static final String MAX_RETRY_SECONDS_STR = "maxRetrySeconds";

	private long delayBetweenAttemptsMillis = DELAY_BETWEEN_ATTEMPTS_MILLISECONDS;
    private static final String DELAY_BETWEEN_ATTEMPTS_MILLISECONDS_STR = "delayBetweenAttemptsMillis";
	private static final long DELAY_BETWEEN_ATTEMPTS_MILLISECONDS = 200;
	
	protected void setMaxRetrySeconds(FilterConfig filterConfig) {
		String paramValue = filterConfig.getInitParameter(MAX_RETRY_SECONDS_STR);
		if (paramValue != null) {
			try {
				maxRetrySeconds = Long.parseLong(paramValue);
			} catch (NumberFormatException e) {
				logger.warn(String.format("The init parameter '%s' with value '%s' of the servlet filter '%s' could not be parsed to a long value.  The default value of '%d' will be used instead.",
						MAX_RETRY_SECONDS_STR, paramValue, CLASS_NAME, maxRetrySeconds));
			}
		}
	}
	
	protected long getMaxRetrySeconds() {
		return this.maxRetrySeconds;
	}
	
	protected void setDelayBetweenAttemptsMillis(FilterConfig filterConfig) {
		String paramValue = filterConfig.getInitParameter(DELAY_BETWEEN_ATTEMPTS_MILLISECONDS_STR);
		if (paramValue != null) {
			try {
				delayBetweenAttemptsMillis = Long.parseLong(paramValue);
			} catch (NumberFormatException e) {
				logger.warn(String.format("The init parameter '%s' with value '%s' of the servlet filter '%s' could not be parsed to a long value.  The default value of '%d' will be used instead.",
						MAX_RETRY_SECONDS_STR, paramValue, CLASS_NAME, delayBetweenAttemptsMillis));
			}
		}
	}
	
	protected long getDelayBetweenAttemptsMillis() {
		return this.delayBetweenAttemptsMillis;
	}
	
    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig theFilterConfig) throws ServletException {
        filterConfig = theFilterConfig;
        
        if (filterConfig != null) {
            setMaxRetrySeconds(theFilterConfig);
            setDelayBetweenAttemptsMillis(theFilterConfig);
        }
    }

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
			try {
				doWrappedFilter(request, response, chain);
			} catch (Throwable e) {
				if (logger.isDebugEnabled() == true) {
					logger.debug(CLASS_NAME, e);
				}
				throw new ServletException(e);
			}
        }
    }
    
    //
    // This method will attempt to repeat the chain.doFilter() method if it fails because of a
    // network related reason.  It looks for failures in two ways:
    //
    // 		1. It looks at the response content type for a special string that we set in the CSWebApplicationException class.
    //		2. If we catch an exception, we look at the exception chain for network related exceptions.
    //
	public void doWrappedFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws Throwable {
		boolean failed = true;
		Throwable lastException = null;
		int requestAttempts = 0;

		long quittingTime = System.currentTimeMillis() + getMaxRetrySeconds() * 1000; // This is how long we attempt retries
		do {
			if (requestAttempts > 0) {
				response.reset();  // This will reset the response instance from the last failed attempt -i.e., we're retrying the request so we don't care about the last response
				Thread.sleep(getDelayBetweenAttemptsMillis()); // Wait a little time between reattempts.
			}
						
			try {
				// proceed to the original request by calling doFilter()
				chain.doFilter(request, response);
				// check the response for network related errors
				if (hasNetworkRelatedError(response) == false) {
					failed = false;
					break; // the request was successfully executed, so we can break out of this retry loop
				} else {
					throw new ConnectException(); // The 'response' argument indicated a network related failure, so let's throw a generic connection exception
				}
			} catch (Exception e) {
				lastException = e;
				if (CSWebApplicationException.exceptionChainContainsNetworkError(lastException) == false) {
					// Break if the exception chain does not contain a
					// SocketException because we don't want to retry if it's not a network related failure
					break;
				}
				requestAttempts++; // keep track of how many times we've tried the request
			}
		} while (System.currentTimeMillis() < quittingTime);  // keep trying until we run out of time
		
		//
		// Add a warning to the logs if we encountered *any* failures on our re-attempts.  Only add the warning
		// if we were eventually successful.
		//
		if (requestAttempts > 0 && failed == false) {
			logger.warn(String.format("Request to '%s' URL failed with exception '%s' at attempt number '%d' before finally succeeding on the next attempt.",
					ServletTools.getURL((HttpServletRequest) request),
					lastException.getClass().getName(),
					requestAttempts));
		}

		if (failed == true) {
			// If we get here, it means all of our attempts to get a successful call to chain.doFilter() have failed.
			throw lastException;
		}
	}
	
	private boolean hasNetworkRelatedError(ServletResponse response) {
		boolean result = false;
		
		String contentType = response.getContentType();
		if (contentType != null && contentType.equalsIgnoreCase(CSWebApplicationException.NETWORK_ERROR_TYPE) == true) {
			result = true;
		}
		
		if (contentType == null) {
			logger.debug("Response had no content type specified.  Probably just a successful POST which will have content type.");
		}
		
		return result;
	}
}
