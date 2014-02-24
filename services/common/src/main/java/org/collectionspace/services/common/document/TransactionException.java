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

/**
 * TransactionException
 * 
 */
public class TransactionException extends DocumentException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Custom HTTP status code, per the extensibility offered via RFC-2616
    // e.g. http://tools.ietf.org/html/rfc2616#section-6.1.1
    final public static int HTTP_CODE = 590;
    
    final static String TRANSACTION_FAILED_MSG = 
        "A transaction failed, whether due to exceeding a timeout value or some other cause. Please contact your system administrator.";

    /**
     * Creates a new instance of <code>TransactionException</code> without detail message.
     */
    public TransactionException() {
        super(TRANSACTION_FAILED_MSG);
        setErrorCode(HTTP_CODE);
    }

    /**
     * Constructs an instance of <code>TransactionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TransactionException(String msg) {
        super(msg);
        setErrorCode(HTTP_CODE);
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
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
        setErrorCode(HTTP_CODE);
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
    public TransactionException(Throwable cause) {
        super(TRANSACTION_FAILED_MSG, cause);
        setErrorCode(HTTP_CODE);
    }
}
