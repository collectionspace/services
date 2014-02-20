package org.collectionspace.services.common;

import java.net.SocketException;
import java.util.Random;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.document.DocumentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSWebApplicationException extends WebApplicationException {
    static final Logger logger = LoggerFactory.getLogger(CSWebApplicationException.class);
    public final static String NETWORK_ERROR_TYPE = "text/neterr";
    public final static String TEXT_MIME_TYPE = "text/plain";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Response response;

	//
	// Override of constructors
	//
	public CSWebApplicationException(Response response) {
		super(response); // this set's our parent's private response member
		this.response = response; // we need to set our copy since we override the getResponse() method.
		logger.warn("CSWebApplicationException instance created without an underlying 'cause' exception.");
	}

	public CSWebApplicationException(Throwable cause) {
		super(cause);
		this.response = getFinalResponse(cause, null);
	}

	public CSWebApplicationException(Throwable cause, Response response) {
		super(cause);
		this.response = getFinalResponse(cause, response);
	}

	//
	// Overrides and custom methods
	//
	
	@Override
	public Response getResponse() {
		return response;
	}

	private Response getFinalResponse(Throwable cause, Response response) {
		Response finalResponse = response;

		if (exceptionChainContainsNetworkError(cause) == true) {
			if (response != null) {
				finalResponse = Response.fromResponse(response).entity(cause.getMessage()).type(NETWORK_ERROR_TYPE).build();
			} else {
				finalResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						cause.getMessage()).type(NETWORK_ERROR_TYPE).build();

			}
			if (logger.isTraceEnabled() == true) {
				logger.trace(cause.getMessage(), cause);
			}
		}
		
		if (finalResponse == null) {
			finalResponse = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
					cause.getMessage()).type(TEXT_MIME_TYPE).build();
		}

		return finalResponse;
	}

	/*
	 * This method crawls the exception chain looking for network related exceptions and
	 * returns 'true' if it finds one.
	 */
	public static boolean exceptionChainContainsNetworkError(Throwable exceptionChain) {
		boolean result = false;
		
		Throwable cause = exceptionChain;
		while (cause != null) {
			if (cause instanceof DocumentException) {
				if (((DocumentException)cause).exceptionChainContainsNetworkError() == true) {  // org.collectionspace.services.common.document.DocumentException (and subclasses) are a special case
					result = true;
					break;
				}
			} else if (isExceptionNetworkRelated(cause) == true) {
				result = true;
				break;
			}
			
			cause = cause.getCause();
		}

		return result;
	}
	
	//
	// We're using this method to try and figure out if the the exception is network related.  Ideally,
	// the exception's class name contains "java.net".  Unfortunately, Nuxeo consumes some of these exceptions and
	// returns a more general "java.sql" exception so we need to check for that as well.
	//
	public static boolean isExceptionNetworkRelated(String exceptionClassName) {
		boolean result = false;
		
		if (exceptionClassName != null) {
			if (exceptionClassName.contains("java.net")) {
				result = true;
			} else if (exceptionClassName.contains("java.sql")) {
				result = true;
			}
		}
		
		return result;
	}

	/*
	 * Return 'true' if the exception is in the "java.net" package.
	 */
	public static boolean isExceptionNetworkRelated(Throwable exception) {
		boolean result = false;
		
		if (exception != null) {
			String className = exception.getClass().getCanonicalName();
			result = isExceptionNetworkRelated(className);
		}
		
		return result;
	}
	
	/*
	 * This Method is intended only for testing the robustness of our network exception handling
	 * and retry code.  You can place a call to this method is various locations throughout the code
	 * and it will randomly throw an exception.
	 */
	public static void throwAnException(int chance) throws SocketException {
		Random generator = new Random(System.currentTimeMillis());
		int roll = generator.nextInt(chance) + 1;
		if (roll != 1) {
			if (logger.isDebugEnabled()) {
				logger.debug("Throwing a SocketException at random to test our network robustness.");
			}
			throw new SocketException();
		}
	}

}
