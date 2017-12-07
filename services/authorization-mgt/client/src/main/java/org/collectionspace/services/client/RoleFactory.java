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
 *//**
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.client;


import java.util.List;

import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class RoleFactory {

    static private final Logger logger = LoggerFactory.getLogger(RoleFactory.class);
    
	public static final List<PermissionValue> EMPTY_PERMVALUE_LIST = null;

    /**
     * create role instance
     * @param roleName
     * @param description
     * @param useRoleName
     * @return
     */
    public static Role createRoleInstance(String roleName,
    		String displayName,
            String description,
            boolean useRoleName,
            List<PermissionValue> permValueList) {

        Role role = new Role();
        if (useRoleName == true) {
            role.setRoleName(roleName);
        }
        role.setDisplayName(displayName);
        role.setDescription(description);
        role.setPermission(permValueList);
        
        return role;

    }
    
    public static RoleValue createRoleValueInstance(Role role) {
    	RoleValue result = new RoleValue();
    	
    	result.setDisplayName(role.getDisplayName());
    	result.setRoleId(role.getCsid());
    	result.setRoleName(role.getRoleName());
    	result.setTenantId(role.getTenantId());
    	
    	return result;
    }
}
