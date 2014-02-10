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
			if (isExceptionNetworkRelated(cause) == true) {
				result = true;
				break;
			}
			
			Throwable nextCause = cause.getCause();
			if (nextCause == null) {
				// Since we reached the end of the exception chain, we can see if the last code
				// executed was network related.
				StackTraceElement[] finalStackTrace = cause.getStackTrace();
			}
		}

		return result;
	}
	
	private static boolean isStackTraceNetworkRelated(StackTraceElement[] stackTraceElementList) {
		boolean result = false;
		
		for (StackTraceElement stackTraceElement : stackTraceElementList) {
			if (stackTraceElement.getClassName().contains("") == true) {
				
			}
		}
		
		return result;
	}
	
	/*
	 * Return 'true' if the exception is in the "java.net" package.
	 */
	private static boolean isExceptionNetworkRelated(Throwable cause) {
		boolean result = false;

		if (cause != null) {
			String className = cause.getClass().getCanonicalName();
			if (cause instanceof DocumentException) {
				className = ((DocumentException) cause).getCausesClassName();  // Since Nuxeo wraps the real exception, we needed to create this special getCausesClassName() method -see NuxeoDocumentException for details
			}
			if (className != null && className.contains("java.net") == true) {
				result = true;
			}
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
