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
package org.collectionspace.services.authorization.importer;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.EffectType;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.PermissionActionUtil;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.RolesList;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.security.SecurityUtils;

/**
 * AuthorizationGen generates authorizations (permissions and roles)
 * for tenant services
 * @author 
 */
public class AuthorizationGen {

	final public static String ROLE_PREFIX = "ROLE_";
    final public static String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    final public static String ROLE_TENANT_ADMINISTRATOR = "TENANT_ADMINISTRATOR";
    final public static String ROLE_TENANT_READER = "TENANT_READER";
    final public static String ROLE_ADMINISTRATOR_ID = "0";
    final public static String ADMINISTRATOR_TENANT_ID = "0";
    //
    // ActionGroup labels/constants
    //
    final public static String ACTIONGROUP_CRUDL = "CRUDL";
    final public static String ACTIONGROUP_RL = "RL";
    //
    // Should the base resource act as a proxy for its sub-resources for AuthZ purposes
    //
    final public static boolean AUTHZ_IS_ENTITY_PROXY = false;
    
    final Logger logger = LoggerFactory.getLogger(AuthorizationGen.class);
    private List<Permission> adminPermList = new ArrayList<Permission>();
    private List<PermissionRole> adminPermRoleList = new ArrayList<PermissionRole>();
    private List<Permission> readerPermList = new ArrayList<Permission>();
    private List<PermissionRole> readerPermRoleList = new ArrayList<PermissionRole>();
    private List<Role> adminRoles = new ArrayList<Role>();
    private List<Role> readerRoles = new ArrayList<Role>();
    private Role cspaceAdminRole;
    private Hashtable<String, TenantBindingType> tenantBindings =
            new Hashtable<String, TenantBindingType>();
	//
    // Store the list of default roles, perms, and roleperms
    //
    private List<PermissionRole> allPermRoleList = null;
	private List<Permission> allPermList;
	private List<Role> allRoleList;

    public void initialize(String tenantRootDirPath) throws Exception {
        TenantBindingConfigReaderImpl tenantBindingConfigReader =
                new TenantBindingConfigReaderImpl(tenantRootDirPath);
        tenantBindingConfigReader.read();
        tenantBindings = tenantBindingConfigReader.getTenantBindings();
        cspaceAdminRole = buildCSpaceAdminRole();

        if (logger.isDebugEnabled()) {
            logger.debug("initialized with tenant bindings from " + tenantRootDirPath);
        }
    }

    /**
     * createDefaultPermissions creates default admin and reader permissions
     * for each tenant found in the given tenant binding file
     * @see initialize
     * @return
     */
    public void createDefaultPermissions() {
        for (String tenantId : tenantBindings.keySet()) {
            List<Permission> adminPerms = createDefaultAdminPermissions(tenantId, AUTHZ_IS_ENTITY_PROXY);
            adminPermList.addAll(adminPerms);

            List<Permission> readerPerms = createDefaultReaderPermissions(tenantId, AUTHZ_IS_ENTITY_PROXY);
            readerPermList.addAll(readerPerms);
        }
    }

    /**
     * createDefaultAdminPermissions creates default admin permissions for all services
     * used by the given tenant
     * @param tenantId
     * @return
     */
    public List<Permission> createDefaultAdminPermissions(String tenantId, boolean isEntityProxy) {
        ArrayList<Permission> apcList = new ArrayList<Permission>();
        TenantBindingType tbinding = tenantBindings.get(tenantId);
        for (ServiceBindingType sbinding : tbinding.getServiceBindings()) {

            //add permissions for the main path
        	String resourceName = sbinding.getName().toLowerCase().trim();
        	if (isEntityProxy == true) {
        		resourceName = SecurityUtils.getResourceEntity(resourceName);
        	}
            Permission perm = buildAdminPermission(tbinding.getId(),
                    resourceName);
            apcList.add(perm);

            //add permissions for alternate paths
            if (isEntityProxy == false) {
	            List<String> uriPaths = sbinding.getUriPath();
	            for (String uriPath : uriPaths) {
	                perm = buildAdminPermission(tbinding.getId(),
	                        uriPath.toLowerCase());
	                apcList.add(perm);
	            }
            }
        }
        
        return apcList;
    }

    private Permission buildAdminPermission(String tenantId, String resourceName) {
        String id = UUID.randomUUID().toString();
        Permission perm = new Permission();
        perm.setCsid(id);
        perm.setDescription("generated admin permission");
        perm.setCreatedAtItem(new Date());
        perm.setResourceName(resourceName.toLowerCase().trim());
        perm.setEffect(EffectType.PERMIT);
        perm.setTenantId(tenantId);
        
        perm.setActionGroup(ACTIONGROUP_CRUDL);
        ArrayList<PermissionAction> pas = new ArrayList<PermissionAction>();
        perm.setAction(pas);

        PermissionAction permAction = PermissionActionUtil.create(perm, ActionType.CREATE);
        pas.add(permAction);
        
        permAction = PermissionActionUtil.create(perm, ActionType.READ);
        pas.add(permAction);
        
        permAction = PermissionActionUtil.create(perm, ActionType.UPDATE);
        pas.add(permAction);
        
        permAction = PermissionActionUtil.create(perm, ActionType.DELETE);
        pas.add(permAction);
        
        permAction = PermissionActionUtil.create(perm, ActionType.SEARCH);
        pas.add(permAction);
        
        return perm;
    }

    /**
     * createDefaultReaderPermissions creates read only permissions for all services
     * used by the given tenant
     * @param tenantId
     * @return
     */
    public List<Permission> createDefaultReaderPermissions(String tenantId, boolean isEntityProxy) {
        ArrayList<Permission> apcList = new ArrayList<Permission>();
        TenantBindingType tbinding = tenantBindings.get(tenantId);
        for (ServiceBindingType sbinding : tbinding.getServiceBindings()) {
            //add permissions for the main path
        	String resourceName = sbinding.getName().toLowerCase().trim();
        	if (isEntityProxy == true) {
        		resourceName = SecurityUtils.getResourceEntity(resourceName);
        	}        	
            Permission perm = buildReaderPermission(tbinding.getId(),
                    resourceName);
            apcList.add(perm);

            //add permissions for alternate paths
            if (isEntityProxy == false) {
	            List<String> uriPaths = sbinding.getUriPath();
	            for (String uriPath : uriPaths) {
	                perm = buildReaderPermission(tbinding.getId(),
	                        uriPath.toLowerCase());
	                apcList.add(perm);
	            }
            }
        }
        return apcList;

    }

    private Permission buildReaderPermission(String tenantId, String resourceName) {
        String id = UUID.randomUUID().toString();
        Permission perm = new Permission();
        perm.setCsid(id);
        perm.setCreatedAtItem(new Date());
        perm.setDescription("generated readonly permission");
        perm.setResourceName(resourceName.toLowerCase().trim());
        perm.setEffect(EffectType.PERMIT);
        perm.setTenantId(tenantId);
        
        perm.setActionGroup(ACTIONGROUP_RL);
        ArrayList<PermissionAction> pas = new ArrayList<PermissionAction>();
        perm.setAction(pas);

        PermissionAction permAction = PermissionActionUtil.create(perm, ActionType.READ);
        pas.add(permAction);

        permAction = PermissionActionUtil.create(perm, ActionType.SEARCH);
        pas.add(permAction);

        return perm;
    }

    public List<Permission> getDefaultPermissions() {
    	if (allPermList == null) {
	        allPermList = new ArrayList<Permission>();
	        allPermList.addAll(adminPermList);
	        allPermList.addAll(readerPermList);
    	}
        return allPermList;
    }

    public List<Permission> getDefaultAdminPermissions() {
        return adminPermList;
    }

    public List<Permission> getDefaultReaderPermissions() {
        return readerPermList;
    }

    /**
     * createDefaultRoles creates default admin and reader roles
     * for each tenant found in the given tenant binding file
     */
    public void createDefaultRoles() {
        for (String tenantId : tenantBindings.keySet()) {

            Role arole = buildTenantAdminRole(tenantId);
            adminRoles.add(arole);

            Role rrole = buildTenantReaderRole(tenantId);
            readerRoles.add(rrole);
        }
    }

    private Role buildTenantAdminRole(String tenantId) {
        return buildTenantRole(tenantId, ROLE_TENANT_ADMINISTRATOR, "admin");
    }

    private Role buildTenantReaderRole(String tenantId) {
        return buildTenantRole(tenantId, ROLE_TENANT_READER, "read only");
    }

    private Role buildTenantRole(String tenantId, String name, String type) {
    	Role role = null;
    	
    	String roleName = ROLE_PREFIX + tenantId + "_" + name;
    	role = AuthorizationStore.getRoleByName(roleName, tenantId);
    	if (role == null) {
    		// the role doesn't exist already, so we need to create it
	        role = new Role();
	        role.setCreatedAtItem(new Date());
	        role.setDisplayName(name);
	        role.setRoleName(roleName);
	        String id = UUID.randomUUID().toString();
	        role.setCsid(id);
			role.setDescription("generated tenant " + type + " role");
	        role.setTenantId(tenantId);
	        role.setMetadataProtection(RoleClient.IMMUTABLE);
	        role.setPermsProtection(RoleClient.IMMUTABLE);
    	}
        
        return role;
    }

    public List<Role> getDefaultRoles() {
    	if (allRoleList == null) {
	        allRoleList = new ArrayList<Role>();
	        allRoleList.addAll(adminRoles);
	        allRoleList.addAll(readerRoles);
    	}
        return allRoleList;
    }

    public void associateDefaultPermissionsRoles() {
        for (Permission p : adminPermList) {
            PermissionRole permAdmRole = associatePermissionRoles(p, adminRoles, true);
            adminPermRoleList.add(permAdmRole);
        }

        for (Permission p : readerPermList) {
            PermissionRole permRdrRole = associatePermissionRoles(p, readerRoles, true);
            readerPermRoleList.add(permRdrRole);
        }
        
        //CSpace Administrator has all access
        List<Role> roles = new ArrayList<Role>();
        roles.add(cspaceAdminRole);
        for (Permission p : adminPermList) {
            PermissionRole permCAdmRole = associatePermissionRoles(p, roles, false);
            adminPermRoleList.add(permCAdmRole);
        }        
    }

    public List<PermissionRole> associatePermissionsRoles(List<Permission> perms, List<Role> roles, boolean enforceTenancy) {
    	List<PermissionRole> result = null;
    	
        List<PermissionRole> permRoles = new ArrayList<PermissionRole>();
        for (Permission perm : perms) {
            PermissionRole permRole = associatePermissionRoles(perm, roles, enforceTenancy);
            if (permRole != null) {
            	permRoles.add(permRole);
            }
        }
        
        if (permRoles.isEmpty() == false) {
        	result = permRoles;
        }
        
        return result;
    }

    private PermissionRole associatePermissionRoles(Permission perm,
            List<Role> roles, boolean enforceTenancy) {
    	PermissionRole result = null;
    	
        PermissionRole pr = new PermissionRole();
        pr.setSubject(SubjectType.ROLE);
        List<PermissionValue> permValues = new ArrayList<PermissionValue>();
        pr.setPermission(permValues);
        PermissionValue permValue = new PermissionValue();
        permValue.setPermissionId(perm.getCsid());
        permValue.setResourceName(perm.getResourceName().toLowerCase());
        permValue.setActionGroup(perm.getActionGroup());
        permValues.add(permValue);

        List<RoleValue> roleValues = new ArrayList<RoleValue>();
        for (Role role : roles) {
        	boolean tenantIdsMatched = true;
        	if (enforceTenancy == true) {
        		tenantIdsMatched = role.getTenantId().equals(perm.getTenantId());
        	}
        	if (tenantIdsMatched == true) {
	            RoleValue rv = new RoleValue();
	            // This needs to use the qualified name, not the display name
	            rv.setRoleName(role.getRoleName().toUpperCase());
	            rv.setRoleId(role.getCsid());
	            roleValues.add(rv);
        	} else {
        		if (logger.isTraceEnabled() == true) {
        			logger.trace("Role and Permission tenant ID did not match."); //FIXME: REM - Remove this debug statement.
        		}
        	}
        }
        //
        // If 'roleValues' is not empty, then associate it with the incoming 'perm' values
        // otherwise, return null;
        //
        if (roleValues.isEmpty() == false) {
        	pr.setRole(roleValues);
        	result = pr;
        }

        return result;
    }

    public List<PermissionRole> getDefaultPermissionRoles() {
    	if (allPermRoleList  == null) {
	        allPermRoleList = new ArrayList<PermissionRole>();
	        allPermRoleList.addAll(adminPermRoleList);
	        allPermRoleList.addAll(readerPermRoleList);
    	}
        return allPermRoleList;
    }

    public List<PermissionRole> getDefaultAdminPermissionRoles() {
        return adminPermRoleList;
    }

    public List<PermissionRole> getDefaultReaderPermissionRoles() {
        return readerPermRoleList;
    }

    private Role buildCSpaceAdminRole() {
        Role role = new Role();
        role.setDisplayName(ROLE_ADMINISTRATOR);
        role.setRoleName(ROLE_PREFIX + role.getDisplayName());
        role.setCsid(ROLE_ADMINISTRATOR_ID);
        role.setTenantId(ADMINISTRATOR_TENANT_ID);
        return role;
    }

    public void exportDefaultRoles(String fileName) {
        RolesList rList = new RolesList();
        rList.setRole(this.getDefaultRoles());
        //
        // Since it is missing the @XMLRootElement annotation, create a JAXBElement wrapper for the RoleList instance
        // so we can have it marshalled it correctly.
        //
        org.collectionspace.services.authorization.ObjectFactory objectFactory = new org.collectionspace.services.authorization.ObjectFactory();
        toFile(objectFactory.createRolesList(rList), RolesList.class,
                fileName);
        if (logger.isDebugEnabled()) {
            logger.debug("exported roles to " + fileName);
        }
    }

    public void exportDefaultPermissions(String fileName) {
        PermissionsList pcList = new PermissionsList();
        pcList.setPermission(this.getDefaultPermissions());
        org.collectionspace.services.authorization.ObjectFactory objectFactory =
        	new org.collectionspace.services.authorization.ObjectFactory();
        toFile(pcList, PermissionsList.class,
//        toFile(objectFactory.createPermissionsList(pcList), PermissionsList.class,
                fileName);
        if (logger.isDebugEnabled()) {
            logger.debug("exported permissions to " + fileName);
        }
    }

    public void exportDefaultPermissionRoles(String fileName) {
        PermissionsRolesList psrsl = new PermissionsRolesList();
        psrsl.setPermissionRole(this.getDefaultAdminPermissionRoles());
        toFile(psrsl, PermissionsRolesList.class,
                fileName);
        if (logger.isDebugEnabled()) {
            logger.debug("exported permissions-roles to " + fileName);
        }
    }

    private void toFile(Object o, Class jaxbClass, String fileName) {
        File f = new File(fileName);
        try {
            JAXBContext jc = JAXBContext.newInstance(jaxbClass);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
