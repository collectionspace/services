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
import java.util.Hashtable;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.client.TenantClient;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.authentication.AuthN;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.authorization.PermissionsRolesList;

import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.RolesList;
import org.collectionspace.services.authorization.SubjectType;

import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;

import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;

/**
 * AuthorizationGen generates authorizations (permissions and roles)
 * for tenant services
 * @author 
 */
public class AuthorizationGen {
    final Logger logger = LoggerFactory.getLogger(AuthorizationGen.class);
    //
    // Should the base resource act as a proxy for its sub-resources for AuthZ purposes
    //
    final public static boolean AUTHZ_IS_ENTITY_PROXY = false;
    
    final public static String TENANT_MGMNT_ID = "0";
    
	private static final boolean USE_APP_GENERATED_BINDINGS = true;
    
    private List<Permission> readWritePermList = new ArrayList<Permission>();
    private List<Permission> tenantMgmntPermList = new ArrayList<Permission>();
    private List<PermissionRole> tenantMgmntPermRoleList = new ArrayList<PermissionRole>();
    
    private List<Permission> adminPermList = new ArrayList<Permission>();
    private List<PermissionRole> adminPermRoleList = new ArrayList<PermissionRole>();
    
    private List<Permission> readerPermList = new ArrayList<Permission>();
    private List<PermissionRole> readerPermRoleList = new ArrayList<PermissionRole>();
    
    private List<Role> adminRoles = new ArrayList<Role>();
    private List<Role> readerRoles = new ArrayList<Role>();
    
    private Role cspaceTenantMgmntRole;
    private Hashtable<String, TenantBindingType> tenantBindings = new Hashtable<String, TenantBindingType>();
	//
    // Store the list of default roles, perms, and roleperms
    //
    private List<PermissionRole> allPermRoleList = null;
	private List<Permission> allPermList;
	private List<Role> allRoleList;

    public void initialize(String tenantRootDirPath) throws Exception {
    	ServicesConfigReaderImpl servicesConfigReader = new ServicesConfigReaderImpl(tenantRootDirPath);
        servicesConfigReader.read(USE_APP_GENERATED_BINDINGS);        
        Boolean useAppGeneratedBindings = servicesConfigReader.getConfiguration().isUseAppGeneratedTenantBindings();

        TenantBindingConfigReaderImpl tenantBindingConfigReader =
                new TenantBindingConfigReaderImpl(tenantRootDirPath);
        tenantBindingConfigReader.read(useAppGeneratedBindings);

        //
        // Don't include disabled tenants
        //
        tenantBindings = tenantBindingConfigReader.getTenantBindings(	TenantBindingConfigReaderImpl.EXCLUDE_CREATE_DISABLED_TENANTS);
        cspaceTenantMgmntRole = buildTenantMgmntRole();

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
    public void createDefaultPermissions(JPATransactionContext jpaTransactionContext) {
        for (String tenantId : tenantBindings.keySet()) {
            List<Permission> adminPerms = createDefaultAdminPermissions(tenantId, AUTHZ_IS_ENTITY_PROXY); // CRUDL perms
            adminPermList.addAll(adminPerms);

            List<Permission> readerPerms = createDefaultReaderPermissions(tenantId, AUTHZ_IS_ENTITY_PROXY); // RL perms
            readerPermList.addAll(readerPerms);
            
            List<Permission> readWritePerms = createDefaultReadWritePermissions(tenantId, AUTHZ_IS_ENTITY_PROXY); // CRUL perms
            readWritePermList.addAll(readWritePerms);
        }
        
        List<Permission> tenantMgmntPerms = createDefaultTenantMgmntPermissions();
        tenantMgmntPermList.addAll(tenantMgmntPerms);
    }

    /**
     * createDefaultAdminPermissions creates default admin permissions for all services
     * used by the given tenant
     * @param tenantId
     * @return
     */
    public List<Permission> createDefaultAdminPermissions(String tenantId, boolean isEntityProxy) {
        ArrayList<Permission> result = new ArrayList<Permission>();
        TenantBindingType tbinding = tenantBindings.get(tenantId);
        for (ServiceBindingType sbinding : tbinding.getServiceBindings()) {

            //add permissions for the main path
        	String resourceName = sbinding.getName().toLowerCase().trim();
        	if (isEntityProxy == true) {
        		resourceName = SecurityUtils.getResourceEntity(resourceName);
        	}
            Permission perm = buildAdminPermission(tbinding.getId(), resourceName);
            result.add(perm);

            //add permissions for alternate paths
            if (isEntityProxy == false) {
	            List<String> uriPaths = sbinding.getUriPath();
	            for (String uriPath : uriPaths) {
	                perm = buildAdminPermission(tbinding.getId(), uriPath.toLowerCase());
	                result.add(perm);
	            }
            }
        }
        
        return result;
    }

    /**
     * createDefaultTenantMgmntPermissions creates default permissions for known 
     * Tenant Mgmnt services. 
     * @return
     */
    public List<Permission> createDefaultTenantMgmntPermissions() {
        ArrayList<Permission> apcList = new ArrayList<Permission>();
        // Later can think about ways to configure this if we want to
        Permission perm = createTenantMgmntPermission(TenantClient.SERVICE_NAME);
        apcList.add(perm);
        
        return apcList;
    }

    /**
     * createTenantMgmntPermission creates special admin permissions for tenant management
     * @return
     */
    private Permission  createTenantMgmntPermission(String resourceName) {
    	Permission perm = buildAdminPermission(TENANT_MGMNT_ID, resourceName);
    	return perm;
    }

    /**
     * createDefaultReadWritePermissions creates read-write (CRUL) permissions for all services
     * used by the given tenant
     * @param tenantId
     * @return
     */
    public List<Permission> createDefaultReadWritePermissions(String tenantId, boolean isEntityProxy) {
        ArrayList<Permission> apcList = new ArrayList<Permission>();

        TenantBindingType tbinding = tenantBindings.get(tenantId);
        for (ServiceBindingType sbinding : tbinding.getServiceBindings()) {
            //add permissions for the main path
        	String resourceName = sbinding.getName().toLowerCase().trim();
        	if (isEntityProxy == true) {
        		resourceName = SecurityUtils.getResourceEntity(resourceName);
        	}
            Permission perm = buildReadWritePermission(tbinding.getId(), resourceName);
            apcList.add(perm);

            //add permissions for alternate paths
            if (isEntityProxy == false) {
	            List<String> uriPaths = sbinding.getUriPath();
	            for (String uriPath : uriPaths) {
	                perm = buildReadWritePermission(tbinding.getId(), uriPath.toLowerCase());
	                apcList.add(perm);
	            }
            }
        }

        return apcList;
    }
    
    /**
     * createDefaultReaderPermissions creates read only permissions for all services
     * used by the given tenant
     * 
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
            Permission perm = buildReaderPermission(tbinding.getId(), resourceName);
            apcList.add(perm);

            //add permissions for alternate paths
            if (isEntityProxy == false) {
	            List<String> uriPaths = sbinding.getUriPath();
	            for (String uriPath : uriPaths) {
	                perm = buildReaderPermission(tbinding.getId(), uriPath.toLowerCase());
	                apcList.add(perm);
	            }
            }
        }
        
        return apcList;
    }

    private Permission buildAdminPermission(String tenantId, String resourceName) {
    	String description = AuthN.GENERATED_STR + "admin permission.";
    	return AuthorizationCommon.createPermission(tenantId, resourceName, description, AuthorizationCommon.ACTIONGROUP_CRUDL_NAME, true);
    }
    
    private Permission buildReaderPermission(String tenantId, String resourceName) {
    	String description = AuthN.GENERATED_STR + "read-only (RL) permission.";
    	return AuthorizationCommon.createPermission(tenantId, resourceName, description, AuthorizationCommon.ACTIONGROUP_RL_NAME, true);    	
    }
    
    private Permission buildReadWritePermission(String tenantId, String resourceName) {
    	String description = AuthN.GENERATED_STR + "read-write (CRUL) permission.";
    	return AuthorizationCommon.createPermission(tenantId, resourceName, description, AuthorizationCommon.ACTIONGROUP_CRUL_NAME, true);    	
    }

    public List<Permission> getDefaultPermissions() {
    	if (allPermList == null) {
	        allPermList = new ArrayList<Permission>();
	        allPermList.addAll(adminPermList);
	        allPermList.addAll(readerPermList);
	        allPermList.addAll(readWritePermList);	        
	        allPermList.addAll(tenantMgmntPermList);
    	}
        return allPermList;
    }

    public List<Permission> getDefaultAdminPermissions() {
        return adminPermList;
    }

    public List<Permission> getDefaultReaderPermissions() {
        return readerPermList;
    }

    public List<Permission> getDefaultTenantMgmntPermissions() {
        return tenantMgmntPermList;
    }

    /**
     * createDefaultRoles creates default admin and reader roles
     * for each tenant found in the given tenant binding file
     */
    public void createDefaultRoles(JPATransactionContext jpaTransactionContext) {
        for (String tenantId : tenantBindings.keySet()) {

            Role arole = buildTenantAdminRole(jpaTransactionContext, tenantId);
            adminRoles.add(arole);

            Role rrole = buildTenantReaderRole(jpaTransactionContext, tenantId);
            readerRoles.add(rrole);
        }
    }

    private Role buildTenantAdminRole(JPATransactionContext jpaTransactionContext, String tenantId) {
    	String type = "admin";
        Role result = AuthorizationCommon.getRole(jpaTransactionContext, tenantId, AuthorizationCommon.ROLE_TENANT_ADMINISTRATOR);
        
        if (result == null) {
    		// the role doesn't exist already, so we need to create it
    		String description = "Generated tenant " + type + " role.";
	        result = AuthorizationCommon.createRole(tenantId, AuthorizationCommon.ROLE_TENANT_ADMINISTRATOR, description, true /*immutable*/);
        }
        
        return result;
    }

    private Role buildTenantReaderRole(JPATransactionContext jpaTransactionContext, String tenantId) {
    	String type = "read only";
        Role result = AuthorizationCommon.getRole(jpaTransactionContext, tenantId, AuthorizationCommon.ROLE_TENANT_READER);
        
        if (result == null) {
    		// the role doesn't exist already, so we need to create it
    		String description = "Generated tenant " + type + " role.";
	        result = AuthorizationCommon.createRole(tenantId, AuthorizationCommon.ROLE_TENANT_READER, description, true /*immutable*/);
        }
        
        return result;
    }
    

    public List<Role> getDefaultRoles() {
    	if (allRoleList == null) {
	        allRoleList = new ArrayList<Role>();
	        allRoleList.addAll(adminRoles);
	        allRoleList.addAll(readerRoles);
	        // Finally, add the tenant manager role to the list
	        allRoleList.add(cspaceTenantMgmntRole);
    	}
        return allRoleList;
    }

    public void associateDefaultPermissionsRoles() {
        for (Permission p : adminPermList) {
            PermissionRole permAdmRole = associatePermissionToRoles(p, adminRoles, true);
            adminPermRoleList.add(permAdmRole);
        }

        for (Permission p : readerPermList) {
            PermissionRole permRdrRole = associatePermissionToRoles(p, readerRoles, true);
            readerPermRoleList.add(permRdrRole);
        }
        
        //CSpace Tenant Manager has all access
        // PLS - this looks wrong. This should be a tenantMgmnt role, and only have access to 
        // tenantMgmnt perms. Will leave this for now...
        List<Role> roles = new ArrayList<Role>();
        roles.add(cspaceTenantMgmntRole);
        /* for (Permission p : adminPermList) {
            PermissionRole permCAdmRole = associatePermissionRoles(p, roles, false);
            adminPermRoleList.add(permCAdmRole);
        }  */
        
        // Now associate the tenant management perms to the role
        for (Permission p : tenantMgmntPermList) {
        	// Note we enforce tenant, as should all be tenant 0 (the special one)
            PermissionRole permTMRole = associatePermissionToRoles(p, roles, true);
            tenantMgmntPermRoleList.add(permTMRole);
        }        
    }

    @Deprecated
    public List<PermissionRole> associatePermissionsRoles(List<Permission> perms, List<Role> roles, boolean enforceTenancy) {
    	List<PermissionRole> result = null;
    	
        List<PermissionRole> permRoles = new ArrayList<PermissionRole>();
        for (Permission perm : perms) {
            PermissionRole permRole = associatePermissionToRoles(perm, roles, enforceTenancy);
            if (permRole != null) {
            	permRoles.add(permRole);
            }
        }
        
        if (permRoles.isEmpty() == false) {
        	result = permRoles;
        }
        
        return result;
    }

    private PermissionRole associatePermissionToRoles(Permission perm,
            List<Role> roles, boolean enforceTenancy) {
    	PermissionRole result = null;
    	
        PermissionRole pr = new PermissionRole();
        pr.setSubject(SubjectType.ROLE);
        List<PermissionValue> permValueList = new ArrayList<PermissionValue>();
        pr.setPermission(permValueList);
        
        PermissionValue permValue = new PermissionValue();
        permValue.setPermissionId(perm.getCsid());
        permValue.setResourceName(perm.getResourceName().toLowerCase());
        permValue.setActionGroup(perm.getActionGroup());
        permValue.setTenantId(perm.getTenantId());
        permValueList.add(permValue);

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
	            rv.setTenantId(role.getTenantId());
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
	        allPermRoleList.addAll(tenantMgmntPermRoleList);
    	}
        return allPermRoleList;
    }

    public List<PermissionRole> getDefaultAdminPermissionRoles() {
        return adminPermRoleList;
    }

    public List<PermissionRole> getDefaultReaderPermissionRoles() {
        return readerPermRoleList;
    }

    private Role buildTenantMgmntRole() {
        Role role = new Role();
        
        role.setDescription("A generated super role that has permissions to manage tenants.");
        role.setDisplayName(AuthN.ROLE_ALL_TENANTS_MANAGER);
        role.setRoleName(AuthorizationCommon.getQualifiedRoleName(
        		AuthN.ALL_TENANTS_MANAGER_TENANT_ID, role.getDisplayName()));
        role.setCsid(AuthN.ROLE_ALL_TENANTS_MANAGER_ID);
        role.setTenantId(AuthN.ALL_TENANTS_MANAGER_TENANT_ID);
        
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
