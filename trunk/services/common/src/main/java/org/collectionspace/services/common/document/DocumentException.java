/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */

package org.collectionspace.services.common.document;

import java.sql.BatchUpdateException;

import javax.persistence.RollbackException;

import org.collectionspace.services.common.ServiceException;

/**
 * DocumentException
 * document handling exception
 */
public class DocumentException extends ServiceException {

    static public DocumentException createDocumentException(Throwable ex) {
    	DocumentException result = new DocumentException(ex);
    	
    	if (RollbackException.class.isInstance(ex) == true) {
    		Throwable jpaProviderCause = ex.getCause();
    		if (jpaProviderCause != null) {
    			Throwable cause = jpaProviderCause.getCause();
	    		if (cause != null && BatchUpdateException.class.isInstance(cause) == true) {
	    			BatchUpdateException bue = (BatchUpdateException)cause;
	    			String sqlState = bue.getSQLState();
	    			result = new DocumentException(bue.getSQLState() +
	    					" : " +
	    					bue.getMessage());
	    		}
    		}
    	}
    	
    	return result;
    }

    /**
     * Creates a new instance of <code>DocumentException</code> without detail message.
     */
    public DocumentException() {
    }

    /**
     * Constructs an instance of <code>DocumentException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DocumentException(String msg) {
        super(msg);
    }

    /**
     * DocumentException with application specific code
     * @param errorCode
     */
    public DocumentException(int errorCode) {
        super(errorCode);
    }
    /**
     * DocumentException with application specific code and reason
     * @param errorCode
     * @param errorReason
     */
    public DocumentException(int errorCode, String errorReason) {
        super(errorCode, errorReason);
    }
    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public DocumentException(Throwable cause) {
        super(cause);
    }

}
