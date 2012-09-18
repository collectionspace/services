package org.collectionspace.services.common.authorization_mgt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.PermissionException;
import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.EffectType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;

import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;

import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.lifecycle.TransitionDefList;

//import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.acls.model.AlreadyExistsException;


public class AuthorizationCommon {
	
	final public static String REFRESH_AUTZ_PROP = "refreshAuthZOnStartup";
    //
    // ActionGroup labels/constants
    //
	
	// for READ-WRITE
    final public static String ACTIONGROUP_CRUDL_NAME = "CRUDL";
    final public static ActionType[] ACTIONSET_CRUDL = {ActionType.CREATE, ActionType.READ, ActionType.UPDATE, ActionType.DELETE, ActionType.SEARCH};
    // for READ-ONLY
    final public static String ACTIONGROUP_RL_NAME = "RL";
    final public static ActionType[] ACTIONSET_RL = {ActionType.READ, ActionType.SEARCH};
    
	/*
	 * Inner class to deal with predefined ADMIN and READER action groupds
	 */
	public class ActionGroup {
		String name;
		ActionType[] actions;
	}
	
	static ActionGroup ACTIONGROUP_CRUDL;
	static ActionGroup ACTIONGROUP_RL;
	
	// A static block to initialize the predefined action groups
	static {
		AuthorizationCommon ac = new AuthorizationCommon();
		// For admin
		ACTIONGROUP_CRUDL = ac.new ActionGroup();
		ACTIONGROUP_CRUDL.name = ACTIONGROUP_CRUDL_NAME;
		ACTIONGROUP_CRUDL.actions = ACTIONSET_CRUDL;
		// For reader
		ACTIONGROUP_RL = ac.new ActionGroup();
		ACTIONGROUP_RL.name = ACTIONGROUP_RL_NAME;
		ACTIONGROUP_RL.actions = ACTIONSET_RL;

	}
	
    final static Logger logger = LoggerFactory.getLogger(AuthorizationCommon.class);

    //
    // The "super" role has a predefined ID of "0" and a tenant ID of "0";
    //
    final public static String ROLE_ADMINISTRATOR = "ADMINISTRATOR";
    final public static String ROLE_ADMINISTRATOR_ID = "0";
    final public static String ADMINISTRATOR_TENANT_ID = "0";

    final public static String ROLE_TENANT_ADMINISTRATOR = "TENANT_ADMINISTRATOR";
    final public static String ROLE_TENANT_READER = "TENANT_READER";
	
    public static final String TENANT_ADMIN_ACCT_PREFIX = "admin@"; 
    public static final String TENANT_READER_ACCT_PREFIX = "reader@"; 
    public static final String ROLE_PREFIX = "ROLE_"; 
    public static final String SPRING_ADMIN_ROLE = "ROLE_SPRING_ADMIN"; 
    public static final String TENANT_ADMIN_ROLE_SUFFIX = "_TENANT_ADMINISTRATOR"; 
    public static final String TENANT_READER_ROLE_SUFFIX = "_TENANT_READER"; 
    public static final String DEFAULT_ADMIN_PASSWORD = "Administrator";
    public static final String DEFAULT_READER_PASSWORD = "reader";

    public static String ROLE_SPRING_ADMIN_ID = "-1";
    public static String ROLE_SPRING_ADMIN_NAME = "ROLE_SPRING_ADMIN";

    public static Role getRole(String tenantId, String displayName) {
    	Role role = null;
    	
    	String roleName = AuthorizationCommon.getQualifiedRoleName(tenantId, displayName);
    	role = AuthorizationStore.getRoleByName(roleName, tenantId);
        
        return role;
    }
    
    public static Role getRole(EntityManager em, String tenantId, String displayName) {
    	Role role = null;
    	
    	String roleName = AuthorizationCommon.getQualifiedRoleName(tenantId, displayName);
    	role = AuthorizationStore.getRoleByName(em, roleName, tenantId);
        
        return role;
    }
    
    
    public static Role createRole(String tenantId, String name, String description) {
    	return createRole(tenantId, name, description, false /* mutable by default */);
    }
    
    public static Role createRole(String tenantId, String name, String description, boolean immutable) {
    	Role role = new Role();
    	
        role.setCreatedAtItem(new Date());
        role.setDisplayName(name);
    	String roleName = AuthorizationCommon.getQualifiedRoleName(tenantId, name);    	
        role.setRoleName(roleName);
        String id = UUID.randomUUID().toString(); //FIXME: The qualified role name should be unique enough to use as an ID/key
        role.setCsid(id);
		role.setDescription(description);
        role.setTenantId(tenantId);
        if (immutable == true) {
	        role.setMetadataProtection(RoleClient.IMMUTABLE);
	        role.setPermsProtection(RoleClient.IMMUTABLE);
        }
    	
    	return role;
    }
    
    /**
     * Add permission to the Spring Security tables
     * with assumption that resource is of type URI
     * @param permission configuration
     */
    public static void addPermissionsForUri(Permission perm,
            PermissionRole permRole) throws PermissionException {
    	//
    	// First check the integrity of the incoming arguments.
    	//
        if (!perm.getCsid().equals(permRole.getPermission().get(0).getPermissionId())) {
            throw new IllegalArgumentException("permission ids do not"
                    + " match for role=" + permRole.getRole().get(0).getRoleName()
                    + " with permissionId=" + permRole.getPermission().get(0).getPermissionId()
                    + " for permission with csid=" + perm.getCsid());
        }
        
        List<String> principals = new ArrayList<String>();        
        for (RoleValue roleValue : permRole.getRole()) {
            principals.add(roleValue.getRoleName());
        }
        List<PermissionAction> permActions = perm.getAction();
        for (PermissionAction permAction : permActions) {
        	try {
	            CSpaceAction action = URIResourceImpl.getAction(permAction.getName()); 
	            URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(),
	                    perm.getResourceName(), action);
	            boolean grant = perm.getEffect().equals(EffectType.PERMIT) ? true : false;
	            AuthZ.get().addPermissions(uriRes, principals.toArray(new String[0]), grant);//CSPACE-4967
        	} catch (PermissionException e) {
        		//
        		// Only throw the exception if it is *not* an already-exists exception
        		//
        		if (e.getCause() instanceof AlreadyExistsException == false) {
        			throw e;
        		}
        	}
        }
    }
    
    private static Connection getConnection() throws NamingException, SQLException {
        return JDBCTools.getConnection(JDBCTools.CSPACE_REPOSITORY_NAME);
    }
    
    /*
     * Spring security seems to require that all of our role names start
     * with the ROLE_PREFIX string.
     */
    public static String getQualifiedRoleName(String tenantId, String name) {
    	String result = name;
    	
    	String qualifiedName = ROLE_PREFIX + tenantId.toUpperCase() + "_" + name.toUpperCase();    	
    	if (name.equals(qualifiedName) == false) {
    		result = qualifiedName;
    	}
    	
    	return result;
    }
        
    private static ActionGroup getActionGroup(String actionGroupStr) {
    	ActionGroup result = null;
    	
    	if (actionGroupStr.equalsIgnoreCase(ACTIONGROUP_CRUDL_NAME)) {
    		result = ACTIONGROUP_CRUDL;
    	} else if (actionGroupStr.equalsIgnoreCase(ACTIONGROUP_RL_NAME)) {
    		result = ACTIONGROUP_RL;
    	}
    	
    	return result;
    }
    
    public static Permission createPermission(String tenantId,
    		String resourceName,
    		String description,
    		String actionGroupStr) {
    	Permission result = null;
    	
    	ActionGroup actionGroup = getActionGroup(actionGroupStr);
    	result = createPermission(tenantId, resourceName, description, actionGroup);
    	
    	return result;
    }
    
    private static Permission createPermission(String tenantId,
    		String resourceName,
    		String description,
    		ActionGroup actionGroup) {
        String id = tenantId
        		+ "-" + resourceName.replace('/', '_') // Remove the slashes so the ID can be used in a URI/URL
        		+ "-" + actionGroup.name;
        Permission perm = new Permission();
        perm.setCsid(id);
        perm.setDescription(description);
        perm.setCreatedAtItem(new Date());
        perm.setResourceName(resourceName.toLowerCase().trim());
        perm.setEffect(EffectType.PERMIT);
        perm.setTenantId(tenantId);
        
        perm.setActionGroup(actionGroup.name);
        ArrayList<PermissionAction> pas = new ArrayList<PermissionAction>();
        perm.setAction(pas);
        for (ActionType actionType : actionGroup.actions) {
        	PermissionAction permAction = createPermissionAction(perm, actionType);
        	pas.add(permAction);
        }
        
        return perm;
    }
    
    private static Permission createWorkflowPermission(TenantBindingType tenantBinding,
    		ServiceBindingType serviceBinding,
    		TransitionDef transitionDef,
    		ActionGroup actionGroup)
    {
    	Permission result = null;
    	String workFlowServiceSuffix;
    	String transitionName;
    	if (transitionDef != null) {
    		transitionName = transitionDef.getName();
    		workFlowServiceSuffix = WorkflowClient.SERVICE_AUTHZ_SUFFIX;
    	} else {
    		transitionName = ""; //since the transitionDef was null, we're assuming that this is the base workflow permission to be created    		
    		workFlowServiceSuffix = WorkflowClient.SERVICE_PATH;
    	}
    	
    	String tenantId = tenantBinding.getId();
    	String resourceName = "/"
    			+ serviceBinding.getName().toLowerCase().trim()
    			+ workFlowServiceSuffix
    			+ transitionName;
    	String description = "A generated workflow permission for actiongroup " + actionGroup.name;
    	result = createPermission(tenantId, resourceName, description, actionGroup);
    	
    	if (logger.isDebugEnabled() == true) {
    		logger.debug("Generated a workflow permission: "
    				+ result.getResourceName()
    				+ ":" + transitionName
    				+ ":" + "tenant id=" + result.getTenantId()
    				+ ":" + actionGroup.name);
    	}
    	
    	return result;
    }
    
    private static PermissionRole createPermissionRole(EntityManager em,
    		Permission permission,
    		Role role,
    		boolean enforceTenancy) throws Exception
    {
    	PermissionRole permRole = new PermissionRole();
    	// Check to see if the tenant ID of the permission and the tenant ID of the role match
    	boolean tenantIdsMatch = role.getTenantId().equalsIgnoreCase(permission.getTenantId());
    	if (tenantIdsMatch == false && enforceTenancy == false) {
    		tenantIdsMatch = true; // If we don't need to enforce tenancy then we'll just consider them matched.
    	}
    			
		if (tenantIdsMatch == true) {
	    	permRole.setSubject(SubjectType.ROLE);
	    	//
	    	// Set of the permission value list of the permrole
	    	//
	        List<PermissionValue> permValues = new ArrayList<PermissionValue>();
	        PermissionValue permValue = new PermissionValue();
	        permValue.setPermissionId(permission.getCsid());
	        permValue.setResourceName(permission.getResourceName().toLowerCase());
	        permValue.setActionGroup(permission.getActionGroup());
	        permValues.add(permValue);
	        permRole.setPermission(permValues);
	        //
	        // Set of the role value list of the permrole
	        //
	        List<RoleValue> roleValues = new ArrayList<RoleValue>();
	        RoleValue rv = new RoleValue();
            // This needs to use the qualified name, not the display name
            rv.setRoleName(role.getRoleName());
            rv.setRoleId(role.getCsid());
            roleValues.add(rv);
            permRole.setRole(roleValues);
		} else {
    		String errMsg = "The tenant ID of the role: " + role.getTenantId()
    				+ " did not match the tenant ID of the permission: " + permission.getTenantId();
    		throw new Exception(errMsg);
		}
    	
    	return permRole;
    }
    
    
    /*
     * FIXME: REM - This method is way too big -over 300 lines!  We need to break it up into
     * smaller, discrete, sub-methods.
     */
    public static void createDefaultAccounts(TenantBindingConfigReaderImpl tenantBindingConfigReader) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("ServiceMain.createDefaultAccounts starting...");
    	}
    	
        Hashtable<String, TenantBindingType> tenantBindings =
        	tenantBindingConfigReader.getTenantBindings();
        Hashtable<String, String> tenantInfo = new Hashtable<String, String>();
        for (TenantBindingType tenantBinding : tenantBindings.values()) {
        	String tId = tenantBinding.getId();
        	String tName = tenantBinding.getName();
        	tenantInfo.put(tId, tName);
        	if (logger.isDebugEnabled()) {
        		logger.debug("createDefaultAccounts found configured tenant id: "+tId+" name: "+tName);
        	}
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
    	Statement stmt = null;
        // TODO - need to put in tests for existence first.
        // We could just look for the accounts per tenant up front, and assume that
        // the rest is there if the accounts are.
        // Could add a sql script to remove these if need be - Spring only does roles, 
        // and we're not touching that, so we could safely toss the 
        // accounts, users, account-tenants, account-roles, and start over.
        try {
        	conn = getConnection();
        	// First find or create the tenants
        	String queryTenantSQL = 
        		"SELECT id,name FROM tenants";
        	stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(queryTenantSQL);
	        ArrayList<String> existingTenants = new ArrayList<String>();
			while (rs.next()) {
				String tId = rs.getString("id");
				String tName = rs.getString("name");
				if(tenantInfo.containsKey(tId)) {
					existingTenants.add(tId);
					if(!tenantInfo.get(tId).equalsIgnoreCase(tName)) {
						logger.warn("Configured name for tenant: "
								+tId+" in repository: "+tName
								+" does not match config'd name: "+ tenantInfo.get(tId));
					}
				}
			}
			rs.close();

        	String insertTenantSQL = 
        		"INSERT INTO tenants (id,name,created_at) VALUES (?,?, now())";
        	pstmt = conn.prepareStatement(insertTenantSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		if(existingTenants.contains(tId)) {
                	if (logger.isDebugEnabled()) {
                		logger.debug("createDefaultAccounts: tenant exists (skipping): "
                				+tenantInfo.get(tId));
                	}
        			continue;
        		}
        		pstmt.setString(1, tId);					// set id param
        		pstmt.setString(2, tenantInfo.get(tId));	// set name param
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts adding entry for tenant: "+tId);
            	}
        		pstmt.executeUpdate();
        	}
        	pstmt.close();
        	// Second find or create the users
        	String queryUserSQL = 
        		"SELECT username FROM users WHERE username LIKE '"
        			+TENANT_ADMIN_ACCT_PREFIX+"%' OR username LIKE '"
        			+TENANT_READER_ACCT_PREFIX+"%'";
			rs = stmt.executeQuery(queryUserSQL);
	        ArrayList<String> usersInRepo = new ArrayList<String>();
			while (rs.next()) {
				String uName = rs.getString("username");
				usersInRepo.add(uName);
			}
			rs.close();
        	String insertUserSQL = 
        		"INSERT INTO users (username,passwd, created_at)"
        		+" VALUES (?,?, now())";
        	pstmt = conn.prepareStatement(insertUserSQL); // create a statement
        	for(String tName : tenantInfo.values()) {
        		String adminAcctName = getDefaultAdminUserID(tName);
        		if(!usersInRepo.contains(adminAcctName)) {
	        		String secEncPasswd = SecurityUtils.createPasswordHash(
	        				adminAcctName, DEFAULT_ADMIN_PASSWORD);
	        		pstmt.setString(1, adminAcctName);	// set username param
	        		pstmt.setString(2, secEncPasswd);	// set passwd param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts adding user: "
	            				+adminAcctName+" for tenant: "+tName);
	            	}
	        		pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+adminAcctName
            						+" already exists - skipping.");
            	}


        		String readerAcctName =  getDefaultReaderUserID(tName);
        		if(!usersInRepo.contains(readerAcctName)) {
	        		String secEncPasswd = SecurityUtils.createPasswordHash(
	        				readerAcctName, DEFAULT_READER_PASSWORD);
	        		pstmt.setString(1, readerAcctName);	// set username param
	        		pstmt.setString(2, secEncPasswd);	// set passwd param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts adding user: "
	            				+readerAcctName+" for tenant: "+tName);
	            	}
	        		pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+readerAcctName
            						+" already exists - skipping.");
        		}
        	}
        	pstmt.close();
        	// Third, create the accounts. Assume that if the users were already there,
        	// then the accounts were as well
            String insertAccountSQL = 
            	"INSERT INTO accounts_common "
            	+ "(csid, email, userid, status, screen_name, metadata_protection, roles_protection, created_at) "
            	+ "VALUES (?,?,?,'ACTIVE',?, 'immutable', 'immutable', now())";
            Hashtable<String, String> tenantAdminAcctCSIDs = new Hashtable<String, String>();
            Hashtable<String, String> tenantReaderAcctCSIDs = new Hashtable<String, String>();
        	pstmt = conn.prepareStatement(insertAccountSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		String tName = tenantInfo.get(tId);
            	String adminCSID = UUID.randomUUID().toString();
            	tenantAdminAcctCSIDs.put(tId, adminCSID);
        		String adminAcctName =  getDefaultAdminUserID(tName);
        		if(!usersInRepo.contains(adminAcctName)) {
	        		pstmt.setString(1, adminCSID);			// set csid param
	        		pstmt.setString(2, adminAcctName);	// set email param (bogus)
	        		pstmt.setString(3, adminAcctName);	// set userid param
	        		pstmt.setString(4, "Administrator");// set screen name param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts adding account: "
	            				+adminAcctName+" for tenant: "+tName);
	            	}
	        		pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+adminAcctName
            						+" already exists - skipping account generation.");
        		}

        		String readerCSID = UUID.randomUUID().toString();	
            	tenantReaderAcctCSIDs.put(tId, readerCSID);
        		String readerAcctName =  getDefaultReaderUserID(tName);
        		if(!usersInRepo.contains(readerAcctName)) {
        			pstmt.setString(1, readerCSID);		// set csid param
        			pstmt.setString(2, readerAcctName);	// set email param (bogus)
        			pstmt.setString(3, readerAcctName);	// set userid param
        			pstmt.setString(4, "Reader");		// set screen name param
        			if (logger.isDebugEnabled()) {
        				logger.debug("createDefaultAccounts adding account: "
        						+readerAcctName+" for tenant: "+tName);
        			}
        			pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts: user: "+readerAcctName
            						+" already exists - skipping account creation.");
        		}
        	}
        	pstmt.close();
        	// Fourth, bind accounts to tenants. Assume that if the users were already there,
        	// then the accounts were bound to tenants correctly
        	String insertAccountTenantSQL;
        	DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType();
        	if (databaseProductType == DatabaseProductType.MYSQL) {
        		insertAccountTenantSQL =
        			"INSERT INTO accounts_tenants (TENANTS_ACCOUNTSCOMMON_CSID,tenant_id) "
        			+ " VALUES(?, ?)";
        	} else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
        		insertAccountTenantSQL =
        			"INSERT INTO accounts_tenants (HJID, TENANTS_ACCOUNTSCOMMON_CSID,tenant_id) "
        			+ " VALUES(nextval('hibernate_sequence'), ?, ?)";
        	} else {
        		throw new Exception("Unrecognized database system.");
        	}
        	pstmt = conn.prepareStatement(insertAccountTenantSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		String tName = tenantInfo.get(tId);
        		if(!usersInRepo.contains(getDefaultAdminUserID(tName))) {
	        		String adminAcct = tenantAdminAcctCSIDs.get(tId);
	        		pstmt.setString(1, adminAcct);		// set acct CSID param
	        		pstmt.setString(2, tId);			// set tenant_id param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account id: "
	            				+adminAcct+" to tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
        		}
        		if(!usersInRepo.contains(getDefaultReaderUserID(tName))) {
	        		String readerAcct = tenantReaderAcctCSIDs.get(tId);
	        		pstmt.setString(1, readerAcct);		// set acct CSID param
	        		pstmt.setString(2, tId);			// set tenant_id param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account id: "
	            				+readerAcct+" to tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
        		}
        	}
        	pstmt.close();
        	// Fifth, fetch and save the default roles
			String springAdminRoleCSID = null;
        	String querySpringRole = 
        		"SELECT csid from roles WHERE rolename='"+SPRING_ADMIN_ROLE+"'";
			rs = stmt.executeQuery(querySpringRole);
    		if(rs.next()) {
    			springAdminRoleCSID = rs.getString(1);
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts found Spring Admin role: "
            				+springAdminRoleCSID);
            	}
    		} else {
                String insertSpringAdminRoleSQL =
                	"INSERT INTO roles (csid, rolename, displayName, rolegroup, created_at, tenant_id) "
                	+ "VALUES ('-1', 'ROLE_SPRING_ADMIN', 'SPRING_ADMIN', 'Spring Security Administrator', now(), '0')";
    			stmt.executeUpdate(insertSpringAdminRoleSQL);
    			springAdminRoleCSID = "-1";
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts CREATED Spring Admin role: "
            				+springAdminRoleCSID);
            	}
    		}
        	rs.close();
        	String getRoleCSIDSql =
        		"SELECT csid from roles WHERE tenant_id=? and rolename=?";
        	pstmt = conn.prepareStatement(getRoleCSIDSql); // create a statement
        	rs = null;
            Hashtable<String, String> tenantAdminRoleCSIDs = new Hashtable<String, String>();
            Hashtable<String, String> tenantReaderRoleCSIDs = new Hashtable<String, String>();
        	for(String tId : tenantInfo.keySet()) {
        		pstmt.setString(1, tId);						// set tenant_id param
        		pstmt.setString(2, getDefaultAdminRole(tId));	// set rolename param
        		rs = pstmt.executeQuery();
        		// extract data from the ResultSet
        		if(!rs.next()) {
        			throw new RuntimeException("Cannot find role: "+getDefaultAdminRole(tId)
        					+" for tenant id: "+tId+" in roles!");
        		}
    			String tenantAdminRoleCSID = rs.getString(1);
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts found role: "
            				+getDefaultAdminRole(tId)+"("+tenantAdminRoleCSID
            				+") for tenant id: "+tId);
            	}
    			tenantAdminRoleCSIDs.put(tId, tenantAdminRoleCSID);
        		pstmt.setString(1, tId);						// set tenant_id param
        		pstmt.setString(2, getDefaultReaderRole(tId));	// set rolename param
        		rs.close();
        		rs = pstmt.executeQuery();
        		// extract data from the ResultSet
        		if(!rs.next()) {
        			throw new RuntimeException("Cannot find role: "+getDefaultReaderRole(tId)
        					+" for tenant id: "+tId+" in roles!");
        		}
    			String tenantReaderRoleCSID = rs.getString(1);
            	if (logger.isDebugEnabled()) {
            		logger.debug("createDefaultAccounts found role: "
            				+getDefaultReaderRole(tId)+"("+tenantReaderRoleCSID
            				+") for tenant id: "+tId);
            	}
    			tenantReaderRoleCSIDs.put(tId, tenantReaderRoleCSID);
        		rs.close();
        	}
        	pstmt.close();
        	// Sixth, bind the accounts to roles. If the users already existed,
        	// we'll assume they were set up correctly.
					String insertAccountRoleSQL;
					if (databaseProductType == DatabaseProductType.MYSQL) {
						insertAccountRoleSQL =
						"INSERT INTO accounts_roles(account_id, user_id, role_id, role_name, created_at)"
							+" VALUES(?, ?, ?, ?, now())";
					} else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
						insertAccountRoleSQL =
						"INSERT INTO accounts_roles(HJID, account_id, user_id, role_id, role_name, created_at)"
							+" VALUES(nextval('hibernate_sequence'), ?, ?, ?, ?, now())";
					} else {
							throw new Exception("Unrecognized database system.");
					}
        	if (logger.isDebugEnabled()) {
        		logger.debug("createDefaultAccounts binding accounts to roles with SQL:\n"
        				+insertAccountRoleSQL);
        	}
        	pstmt = conn.prepareStatement(insertAccountRoleSQL); // create a statement
        	for(String tId : tenantInfo.keySet()) {
        		String adminUserId =  getDefaultAdminUserID(tenantInfo.get(tId));
        		if(!usersInRepo.contains(adminUserId)) {
            		String adminAcct = tenantAdminAcctCSIDs.get(tId);
	        		String adminRoleId = tenantAdminRoleCSIDs.get(tId);
	        		pstmt.setString(1, adminAcct);		// set acct CSID param
	        		pstmt.setString(2, adminUserId);	// set user_id param
	        		pstmt.setString(3, adminRoleId);	// set role_id param
	        		pstmt.setString(4, getDefaultAdminRole(tId));	// set rolename param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account: "
	            				+adminUserId+" to Admin role("+adminRoleId
	            				+") for tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
	        		// Now add the Spring Admin Role to the admin accounts
	        		pstmt.setString(3, springAdminRoleCSID);	// set role_id param
	        		pstmt.setString(4, SPRING_ADMIN_ROLE);		// set rolename param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account: "
	            				+adminUserId+" to Spring Admin role: "+springAdminRoleCSID);
	            	}
	        		pstmt.executeUpdate();
        		}
        		String readerUserId = getDefaultReaderUserID(tenantInfo.get(tId));
        		if(!usersInRepo.contains(readerUserId)) {
	        		String readerAcct = tenantReaderAcctCSIDs.get(tId);
	        		String readerRoleId = tenantReaderRoleCSIDs.get(tId);
	        		pstmt.setString(1, readerAcct);		// set acct CSID param
	        		pstmt.setString(2, readerUserId);	// set user_id param
	        		pstmt.setString(3, readerRoleId);	// set role_id param
	        		pstmt.setString(4, getDefaultReaderRole(tId));	// set rolename param
	            	if (logger.isDebugEnabled()) {
	            		logger.debug("createDefaultAccounts binding account: "
	            				+readerUserId+" to Reader role("+readerRoleId
	            				+") for tenant id: "+tId);
	            	}
	        		pstmt.executeUpdate();
        		}
        	}
        	pstmt.close();
			stmt.close();
        } catch (RuntimeException rte) {
        	if (logger.isDebugEnabled()) {
        		logger.debug("Exception in createDefaultAccounts: "+
						rte.getLocalizedMessage());
        		logger.debug(rte.getStackTrace().toString());
        	}
            throw rte;
        } catch (SQLException sqle) {
            // SQLExceptions can be chained. We have at least one exception, so
            // set up a loop to make sure we let the user know about all of them
            // if there happens to be more than one.
        	if (logger.isDebugEnabled()) {
        		SQLException tempException = sqle;
        		while (null != tempException) {
        			logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
        			tempException = tempException.getNextException();
        		}
        		logger.debug(sqle.getStackTrace().toString());
        	}
            throw new RuntimeException("SQL problem in createDefaultAccounts: ", sqle);
        } catch (Exception e) {
        	if (logger.isDebugEnabled()) {
        		logger.debug("Exception in createDefaultAccounts: "+
						e.getLocalizedMessage());
        	}
        } finally {
        	try {
            	if(conn!=null)
                    conn.close();
            	if(pstmt!=null)
                    pstmt.close();
            	if(stmt!=null)
                    stmt.close();
            } catch (SQLException sqle) {
            	if (logger.isDebugEnabled()) {
        			logger.debug("SQL Exception closing statement/connection: "
        					+ sqle.getLocalizedMessage());
            	}
        	}
        }    	
    }
    
    private static String getDefaultAdminRole(String tenantId) {
    	return ROLE_PREFIX+tenantId+TENANT_ADMIN_ROLE_SUFFIX;
    }
    
    private static String getDefaultReaderRole(String tenantId) {
    	return ROLE_PREFIX+tenantId+TENANT_READER_ROLE_SUFFIX;
    }
    
    private static String getDefaultAdminUserID(String tenantName) {
    	return TENANT_ADMIN_ACCT_PREFIX+tenantName;
    }
    
    private static String getDefaultReaderUserID(String tenantName) {
    	return TENANT_READER_ACCT_PREFIX+tenantName;
    }
    
	static public PermissionAction createPermissionAction(Permission perm,
			ActionType actionType) {
        PermissionAction pa = new PermissionAction();

	    CSpaceAction action = URIResourceImpl.getAction(actionType);
	    URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(),
	            perm.getResourceName(), action);
	    pa.setName(actionType);
	    pa.setObjectIdentity(uriRes.getHashedId().toString());
	    pa.setObjectIdentityResource(uriRes.getId());
	    
	    return pa;
	}

	static public PermissionAction update(Permission perm, PermissionAction permAction) {
        PermissionAction pa = new PermissionAction();

	    CSpaceAction action = URIResourceImpl.getAction(permAction.getName());
	    URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(),
	            perm.getResourceName(), action);
	    pa.setObjectIdentity(uriRes.getHashedId().toString());
	    pa.setObjectIdentityResource(uriRes.getId());
	    
	    return pa;
	}
	
	private static TransitionDefList getTransitionDefList(TenantBindingType tenantBinding, ServiceBindingType serviceBinding) {
		TransitionDefList result = null;
		try {
			String serviceObjectName = serviceBinding.getObject().getName();
	    	DocumentHandler docHandler = ServiceConfigUtils.createDocumentHandlerInstance(
	    			tenantBinding, serviceBinding);
	    	Lifecycle lifecycle = docHandler.getLifecycle(serviceObjectName);
	    	if (lifecycle != null) {
	    		result = lifecycle.getTransitionDefList();
	    	}
		} catch (Exception e) {
			// Ignore this exception and return an empty non-null TransitionDefList
		}
		
		if (result == null) {
			logger.warn("Could not retrieve a lifecycle transition definition list from: "
					+ serviceBinding.getName()
					+ " with tenant ID = "
					+ tenantBinding.getId());			
			// return an empty list			
			result = new TransitionDefList();
		} else {
			logger.debug("Successfully etrieved a lifecycle transition definition list from: "
					+ serviceBinding.getName()
					+ " with tenant ID = "
					+ tenantBinding.getId());
		}
		
		return result;
	}
	
    public static void createDefaultWorkflowPermissions(TenantBindingConfigReaderImpl tenantBindingConfigReader) throws Exception //FIXME: REM - 4/11/2012 - Rename to createWorkflowPermissions
    {
    	AuthZ.get().login(); //login to Spring Security manager
    	
        EntityManagerFactory emf = JpaStorageUtils.getEntityManagerFactory(JpaStorageUtils.CS_PERSISTENCE_UNIT);
        EntityManager em = null;

        try {
            em = emf.createEntityManager();

            Role superRole = AuthorizationCommon.getRole(em, ADMINISTRATOR_TENANT_ID, ROLE_ADMINISTRATOR);
	        Hashtable<String, TenantBindingType> tenantBindings =
	            	tenantBindingConfigReader.getTenantBindings();
	        for (String tenantId : tenantBindings.keySet()) {
		        TenantBindingType tenantBinding = tenantBindings.get(tenantId);
	    		Role adminRole = AuthorizationCommon.getRole(em, tenantBinding.getId(), ROLE_TENANT_ADMINISTRATOR);
	    		Role readonlyRole = AuthorizationCommon.getRole(em, tenantBinding.getId(), ROLE_TENANT_READER);
		        for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
		        	String prop = ServiceBindingUtils.getPropertyValue(serviceBinding, REFRESH_AUTZ_PROP);
		        	if (prop == null ? true : Boolean.parseBoolean(prop)) {
			        		try {
			        		em.getTransaction().begin();
				        	TransitionDefList transitionDefList = getTransitionDefList(tenantBinding, serviceBinding);
				        	for (TransitionDef transitionDef : transitionDefList.getTransitionDef()) {
				        		//
				        		// Create the permission for the admin role
				        		Permission adminPerm = createWorkflowPermission(tenantBinding, serviceBinding, transitionDef, ACTIONGROUP_CRUDL);
				        		persist(em, adminPerm, adminRole, true);
				        		//
				        		// Create the permission for the read-only role
				        		Permission readonlyPerm = createWorkflowPermission(tenantBinding, serviceBinding, transitionDef, ACTIONGROUP_RL);
				        		
				        		Profiler profiler = new Profiler(AuthorizationCommon.class, 1);
				        		profiler.start("createDefaultPermissions started:" + readonlyPerm.getCsid());
				        		persist(em, readonlyPerm, readonlyRole, true); // Persist/store the permission and permrole records and related Spring Security info
				        		profiler.stop();
				        		logger.debug("Finished full perm generation for "
				        				+ ":" + tenantBinding.getId()
				        				+ ":" + serviceBinding.getName()
				        				+ ":" + transitionDef.getName()
				        				+ ":" + ACTIONGROUP_RL
				        				+ ":" + profiler.getCumulativeTime());			        		
				        		/*
				        		//
				        		// Create the permission for the super-admin role.  Note we use the same "adminPerm" instance we used for the "adminPermRole" instance
				        		//
				        		persist(em, adminPerm, superRole, false);
				        		*/
				        	}
				        	em.getTransaction().commit();
			        	} catch (IllegalStateException e) {
			        		logger.debug(e.getLocalizedMessage(), e); //We end up here if there is no document handler for the service -this is ok for some of the services.
			        	}
		        	} else {
		        		logger.warn("AuthZ refresh service binding property is set to FALSE so default permissions will NOT be refreshed for: "
		        				+ serviceBinding.getName());
		        	}
		        }
	        }
            em.close();
    	} catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception and rolling back permission creation: ", e);
            }
            throw e;
        } finally {
            if (em != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }
    
    private static PermissionRoleRel findPermRoleRel(EntityManager em, String permissionId, String RoleId) {
    	PermissionRoleRel result = null;
    	
    	try {
	        String whereClause = "where permissionId = :id and roleId = :roleId";
	        HashMap<String, Object> params = new HashMap<String, Object>();
	        params.put("id", permissionId);
	        params.put("roleId", RoleId);        
	
	        result = (PermissionRoleRel) JpaStorageUtils.getEntity(em,
	        		PermissionRoleRel.class.getCanonicalName(), whereClause, params);
    	} catch (Exception e) {
    		//Do nothing. Will return null;
    	}
    	    	
    	return result;
    }
    
    /*
     * Persists the Permission, PermissionRoleRel, and Spring Security table entries all in one transaction
     */
    private static void persist(EntityManager em, Permission permission, Role role, boolean enforceTenancy) throws Exception {
		AuthorizationStore authzStore = new AuthorizationStore();
		// First persist the Permission record
		authzStore.store(em, permission);
		
		// If the PermRoleRel doesn't already exists then relate the permission and the role in a new PermissionRole (the service payload)
		// Create a PermissionRoleRel (the database relation table for the permission and role)
		PermissionRoleRel permRoleRel = findPermRoleRel(em, permission.getCsid(), role.getCsid());
		if (permRoleRel == null) {
			PermissionRole permRole = createPermissionRole(em, permission, role, enforceTenancy);
	        List<PermissionRoleRel> permRoleRels = new ArrayList<PermissionRoleRel>();
	        PermissionRoleUtil.buildPermissionRoleRel(em, permRole, SubjectType.ROLE, permRoleRels, false /*not for delete*/);
	        for (PermissionRoleRel prr : permRoleRels) {
	            authzStore.store(em, prr);
	        }
			Profiler profiler = new Profiler(AuthorizationCommon.class, 2);
			profiler.start();
			// Add a corresponding entry in the Spring Security Tables
			addPermissionsForUri(permission, permRole);
			profiler.stop();
			logger.debug("Finished full perm generation for "
					+ ":" + permission.getTenantId()
					+ ":" + permission.getResourceName()
					+ ":" + ACTIONGROUP_RL
					+ ":" + profiler.getCumulativeTime());
		}
        
    }

}
