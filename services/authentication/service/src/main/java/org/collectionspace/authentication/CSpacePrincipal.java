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
package org.collectionspace.authentication;

import java.security.Principal;

/**
 * CSpacePrincipal provides additional tenant-specific context to application
 * @author 
 */
final public class CSpacePrincipal
        implements Principal, java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1357683611240908638L;
	private String name;
    private String tenantId;

    public CSpacePrincipal(String name) {
        this.name = name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object obj) {
        Principal p = (Principal) obj;
        return name.equals(p.getName());
    }

    public String toString() {
        return name;
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of the tenant this principal is associated with
     * @return
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Set the id for the tenant to which princiapal is associated with
     * The access to this method must be package private
     * @param tenantId
     */
    void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
