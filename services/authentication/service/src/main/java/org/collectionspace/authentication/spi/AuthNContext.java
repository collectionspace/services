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
.
 */
package org.collectionspace.authentication.spi;

import javax.security.auth.Subject;
import org.collectionspace.authentication.CSpaceTenant;

/**
 * Utilities to be used by Services runtime to interface with authentication service
 * @author 
 */
public abstract class AuthNContext {

    /**
     * getUserId returns authenticated user id
     * @return
     */
    public abstract String getUserId();

    /**
     * getTenantIds get tenant ids from the tenant context associated with the
     * security context
     * @return
     */
    public abstract String[] getTenantIds();

    /**
     * getCurrentTenantId get id of the tenant associated with the authenticated user
     * @return
     */
    public abstract String getCurrentTenantId();

    /**
     * getCurrentTenantName get name of the tenant associated with the authenticated user
     * @return
     */
    public abstract String getCurrentTenantName();

    /**
     * getTenants get tenant context associated with the security context
     * @see CSpaceTenant
     * @return
     */
    public abstract CSpaceTenant[] getTenants();

    /**
     * getSubject retrieves security context as Subject
     * @see javax.security.auth.Subject
     */
    public abstract Subject getSubject();
}
