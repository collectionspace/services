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
package org.collectionspace.services.common.security;

import org.collectionspace.services.common.ServiceException;

/**
 * ServiceForbidenException is thrown when access to service is not allowed for
 * one or more of the following reasons:
 * - access not allowed
 * - no application key found
 * @author 
 */
public class ServiceForbiddenException extends ServiceException {

    final public static int HTTP_CODE = 401;

    /**
     * Creates a new instance of <code>UnauthorizedException</code> without detail message.
     */
    public ServiceForbiddenException() {
        super(HTTP_CODE);
    }

    /**
     * Constructs an instance of <code>UnauthorizedException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ServiceForbiddenException(String msg) {
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
    public ServiceForbiddenException(String message, Throwable cause) {
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
    public ServiceForbiddenException(Throwable cause) {
        super(cause);
        setErrorCode(HTTP_CODE);
    }
}
