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
 * CSpaceResourceImpl abstract resource implementation
 * @author 
 */
public abstract class CSpaceResourceImpl implements CSpaceResource {

    final protected static String SEPARATOR_HASH = "#";
    final protected static String SEPARATOR_COLON = ":";
    private String id;
    private TYPE type;
    private CSpaceAction action;
    private String tenantId;

    private CSpaceResourceImpl() {
    	// Do nothing.
    }

    /**
     * constructor that uses logged in user's tenant context to associate resource with
     * @param id
     * @param action
     * @param type
     */
    /*
    public CSpaceResourceImpl(String id, CSpaceAction action, TYPE type) {
        setup(id, action, type);
        tenantId = AuthN.get().getCurrentTenantId();
    }
    */

    /**
     * constructor that uses given tenant id to associate the resource with
     * @param tenantId
     * @param id
     * @param action
     * @param type
     */
    public CSpaceResourceImpl(String tenantId, String id, CSpaceAction action, TYPE type) {
        setup(id, action, type);
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId cannot be null");
        }
        this.tenantId = tenantId;
    }

    private void setup(String id, CSpaceAction action, TYPE type) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        this.id = id.toLowerCase();
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.type = type;
        if (action == null) {
            throw new IllegalArgumentException("action cannot be null");
        }
        this.action = action;
    }

    @Override
    public String getId() {
        //tenant-qualified id
        return tenantId + SEPARATOR_COLON + id;
    }
    
    @Override
    public Long getHashedId() {
    	return Long.valueOf(getId().hashCode());
    }

    @Override
    public TYPE getType() {
        return type;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    /**
     * getAction a convenience method to get action invoked on the resource
     */
    @Override
    public CSpaceAction getAction() {
        return action;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CSpaceResourceImpl [");
        builder.append("id=");
        builder.append(id);
        builder.append(", type=");
        builder.append(type);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", action=");
        builder.append(action.toString());
        builder.append("]");
        return builder.toString();
    }
}
