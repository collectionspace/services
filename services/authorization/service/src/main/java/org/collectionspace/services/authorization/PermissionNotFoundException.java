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
package org.collectionspace.services.authorization;

/**
 *
 * @author sanjaydalal
 */
public class PermissionNotFoundException extends PermissionException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new instance of <code>PermissionNotFoundException</code> without detail message.
     */
    public PermissionNotFoundException() {
    }

    /**
     * Constructs an instance of <code>PermissionNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PermissionNotFoundException(String msg) {
        super(msg);
    }

    public PermissionNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public PermissionNotFoundException(Throwable cause) {
        super(cause);
    }
}
