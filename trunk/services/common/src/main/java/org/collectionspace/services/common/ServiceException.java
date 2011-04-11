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

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common;

/**
 * ServiceException is a base exception for any exception thrown by the service layer
 * The error code is the code that is sent to the service consumer with relevant
 * error reason. Note that detail message could be server side service-centric message.
 * This message should not be sent to the service consumer who does not need to know
 * about the internals of the service. The service consumer should see error code
 * and error reason only. CollectionSpace service documentation should be checked
 * to get help on troubleshooting the error.
 * @author 
 */
public class ServiceException extends Exception {

    private int errorCode;

    /**
     * Creates a new instance of <code>ServiceException</code> without detail message, error code
     * or error reason.
     */
    public ServiceException() {
    }

    /**
     * Constructs an instance of <code>ServiceException</code> with the specified detail message
     * without error code or error reason.
     * @param msg the detail message.
     */
    public ServiceException(String msg) {
        super(msg);
    }

        /**
     * Constructs an instance of <code>ServiceException</code> with the specified error code.
     * @param errorCode error code
     */
    public ServiceException(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Constructs an instance of <code>ServiceException</code> with the specified error code and error reason.
     * @param errorCode error code
     * @param errorReason reason for error
     */
    public ServiceException(int errorCode, String errorReason) {
        super(errorReason);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new an instance of <code>ServiceException</code> with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  msg the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new an instance of <code>ServiceException</code> with the specified cause and a detail
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
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @return the errorReason
     */
    public String getErrorReason() {
        return getMessage();
    }
}
