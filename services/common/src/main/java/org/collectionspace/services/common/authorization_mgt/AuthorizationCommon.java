package org.collectionspace.services.common.authorization_mgt;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.naming.NamingException;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.account.AccountListItem;

import org.collectionspace.services.authentication.Token;
import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.CSpaceResource;
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
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.client.Profiler;
import org.collectionspace.services.client.RoleClient;
import org.collectionspace.services.client.workflow.WorkflowClient;

import org.collectionspace.services.common.config.ServiceConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.EmailConfig;
import org.collectionspace.services.config.tenant.PasswordResetConfig;
import org.collectionspace.services.config.tenant.TenantBindingType;

import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.lifecycle.TransitionDefList;

//import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AuthorizationCommon {
	
	final public static String REFRESH_AUTHZ_PROP = "refreshAuthZOnStartup";
	
	//
	// For token generation and password reset
	//
	final private static String DEFAULT_PASSWORD_RESET_EMAIL_MESSAGE = "Hello {{greeting}},\n\r\n\rYou've started the process to reset your CollectionSpace account password. To finish resetting your password, go to the Reset Password page {{link}} on CollectionSpace.\n\r\n\rIf clicking the link doesn't work, copy and paste the following link into your browser address bar and click Go.\n\r\n\r{{link}}\n\r Thanks,\n\r\n\r CollectionSpace Administrator\n\r\n\rPlease do not reply to this email. This mailbox is not monitored and you will not receive a response. For assistance, contact your CollectionSpace Administrator directly.";
	private static final String DEFAULT_PASSWORD_RESET_EMAIL_SUBJECT = "Password reset for CollectionSpace account";
	
	//
	// Keep track of the MD5 hash value for the tenant bindings
	//
	private static final Map<String, String> tenantConfigMD5HashTable = new HashMap<String, String>();

    //
    // ActionGroup labels/constants
    //
	
	// for READ-WRITE-DELETE
    final public static String ACTIONGROUP_CRUDL_NAME = "CRUDL";
    final public static ActionType[] ACTIONSET_CRUDL = {ActionType.CREATE, ActionType.READ, ActionType.UPDATE, ActionType.DELETE, ActionType.SEARCH};
	// for READ-WRITE
    final public static String ACTIONGROUP_CRUL_NAME = "CRUL";
    final public static ActionType[] ACTIONSET_CRUL = {ActionType.CREATE, ActionType.READ, ActionType.UPDATE, ActionType.SEARCH};
    // for READ-ONLY
    final public static String ACTIONGROUP_RL_NAME = "RL";
    final public static ActionType[] ACTIONSET_RL = {ActionType.READ, ActionType.SEARCH};
	
	static ActionGroup ACTIONGROUP_CRUDL;
	static ActionGroup ACTIONGROUP_CRUL;
	static ActionGroup ACTIONGROUP_RL;
	
	// A static block to initialize the predefined action groups
	static {
		// For admin
		ACTIONGROUP_CRUDL = new ActionGroup();
		ACTIONGROUP_CRUDL.name = ACTIONGROUP_CRUDL_NAME;
		ACTIONGROUP_CRUDL.actions = ACTIONSET_CRUDL;
		// For reader
		ACTIONGROUP_RL = new ActionGroup();
		ACTIONGROUP_RL.name = ACTIONGROUP_RL_NAME;
		ACTIONGROUP_RL.actions = ACTIONSET_RL;
		// For read-write
		ACTIONGROUP_CRUL = new ActionGroup();
		ACTIONGROUP_CRUL.name = ACTIONGROUP_CRUL_NAME;
		ACTIONGROUP_CRUL.actions = ACTIONSET_CRUL;
	}
	
    final static Logger logger = LoggerFactory.getLogger(AuthorizationCommon.class);

    final public static String ROLE_TENANT_ADMINISTRATOR = "TENANT_ADMINISTRATOR";
    final public static String ROLE_TENANT_READER = "TENANT_READER";
	
    public static final String TENANT_MANAGER_USER = "tenantManager"; 
    public static final String TENANT_MANAGER_SCREEN_NAME = TENANT_MANAGER_USER; 
    public static final String DEFAULT_TENANT_MANAGER_PASSWORD = "manage"; 
    public static final String DEFAULT_TENANT_MANAGER_EMAIL = "tenantManager@collectionspace.org"; 
    
    public static final String TENANT_ADMIN_ACCT_PREFIX = "admin@"; 
    public static final String TENANT_READER_ACCT_PREFIX = "reader@"; 
    public static final String ROLE_PREFIX = "ROLE_"; 
    public static final String TENANT_ADMIN_ROLE_SUFFIX = "_TENANT_ADMINISTRATOR"; 
    public static final String TENANT_READER_ROLE_SUFFIX = "_TENANT_READER"; 
    public static final String DEFAULT_ADMIN_PASSWORD = "Administrator";
    public static final String DEFAULT_READER_PASSWORD = "reader";
    
    // SQL for init tasks
	final private static String INSERT_ACCOUNT_ROLE_SQL_MYSQL = 
			"INSERT INTO accounts_roles(account_id, user_id, role_id, role_name, created_at)"
					+" VALUES(?, ?, ?, ?, now())";
	final private static String INSERT_ACCOUNT_ROLE_SQL_POSTGRES =
			"INSERT INTO accounts_roles(HJID, account_id, user_id, role_id, role_name, created_at)"
					+" VALUES(nextval('hibernate_sequence'), ?, ?, ?, ?, now())";
	final private static String QUERY_USERS_SQL = 
    		"SELECT username FROM users WHERE username LIKE '"
    			+TENANT_ADMIN_ACCT_PREFIX+"%' OR username LIKE '"+TENANT_READER_ACCT_PREFIX+"%'";
	final private static String INSERT_USER_SQL =
			"INSERT INTO users (username,passwd,salt, created_at) VALUES (?,?,?, now())";
	final private static String INSERT_ACCOUNT_SQL = 
			"INSERT INTO accounts_common "
					+ "(csid, email, userid, status, screen_name, metadata_protection, roles_protection, created_at) "
					+ "VALUES (?,?,?,'ACTIVE',?, 'immutable', 'immutable', now())";
	
	// TENANT MANAGER specific SQL
	final private static String QUERY_TENANT_MGR_USER_SQL = 
    		"SELECT username FROM users WHERE username = '"+TENANT_MANAGER_USER+"'";
	final private static String GET_TENANT_MGR_ROLE_SQL =
			"SELECT csid from roles WHERE tenant_id='" + AuthN.ALL_TENANTS_MANAGER_TENANT_ID + "' and rolename=?";

	public static final String IGNORE_TENANT_ID = null; // A null constant to indicate an empty/unused value for the tenant ID


	public static String getTenantConfigMD5Hash(String tenantId) {
		return tenantConfigMD5HashTable.get(tenantId);
	}
	
	public static String setTenantConfigMD5Hash(String tenantId, String md5hash) {
		return tenantConfigMD5HashTable.put(tenantId, md5hash);
	}	
	    
    public static Role getRole(JPATransactionContext jpaTransactionContext, String tenantId, String displayName) {
    	Role role = null;
    	
    	String roleName = AuthorizationCommon.getQualifiedRoleName(tenantId, displayName);
    	role = AuthorizationStore.getRoleByName(jpaTransactionContext, roleName, tenantId);
        
        return role;
    }
    
    /**
     * Create a new role instance to be persisted later.
     * 
     * @param tenantId
     * @param name
     * @param description
     * @param immutable
     * @return
     */
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
    public static void addPermissionsForUri(JPATransactionContext jpaTransactionContext,
    		Permission perm,
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
        
        boolean grant = perm.getEffect().equals(EffectType.PERMIT) ? true : false;
        List<PermissionAction> permActions = perm.getAction();
        ArrayList<CSpaceResource> resources = new ArrayList<CSpaceResource>();
        for (PermissionAction permAction : permActions) {
            CSpaceAction action = URIResourceImpl.getAction(permAction.getName()); 
            URIResourceImpl uriRes = new URIResourceImpl(perm.getTenantId(), perm.getResourceName(), action);
            resources.add(uriRes);
        }
        AuthZ.get().addPermissions(resources.toArray(new CSpaceResource[0]), principals.toArray(new String[0]), grant); // CSPACE-4967
        jpaTransactionContext.setAclTablesUpdateFlag(true); // Tell the containing JPA transaction that we've committed changes to the Spring Tables
    }
    
    private static Connection getConnection(String databaseName) throws NamingException, SQLException {
        return JDBCTools.getConnection(JDBCTools.CSPACE_DATASOURCE_NAME,
        		databaseName);
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
    	} else if (actionGroupStr.equalsIgnoreCase(ACTIONGROUP_CRUL_NAME)) {
    		result = ACTIONGROUP_CRUL;
    	}
    	
    	return result;
    }
    
    public static Permission createPermission(String tenantId,
    		String resourceName,
    		String description,
    		String actionGroupStr,
    		boolean immutable) {
    	Permission result = null;
    	
    	ActionGroup actionGroup = getActionGroup(actionGroupStr);
    	result = createPermission(tenantId, resourceName, description, actionGroup, immutable);
    	
    	return result;
    }
    
    private static Permission createPermission(String tenantId,
    		String resourceName,
    		String description,
    		ActionGroup actionGroup,
    		boolean immutable) {
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
        
        if (immutable) {
        	perm.setMetadataProtection(PermissionClient.IMMUTABLE);
        	perm.setActionsProtection(PermissionClient.IMMUTABLE);
        }
        
        return perm;
    }
    
    private static Permission createWorkflowPermission(TenantBindingType tenantBinding,
    		ServiceBindingType serviceBinding,
    		String transitionVerb,
    		ActionGroup actionGroup,
    		boolean immutable)
    {
    	Permission result = null;
    	String workFlowServiceSuffix;
    	String transitionName;
    	if (transitionVerb != null) {
    		transitionName = transitionVerb;
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
    	result = createPermission(tenantId, resourceName, description, actionGroup, immutable);
    	
    	if (logger.isDebugEnabled() == true) {
    		logger.debug("Generated a workflow permission: "
    				+ result.getResourceName()
    				+ ":" + transitionName
    				+ ":" + "tenant id=" + result.getTenantId()
    				+ ":" + actionGroup.name);
    	}
    	
    	return result;
    }
    
    private static PermissionRole createPermissionRole(
    		Permission permission,
    		Role role,
    		boolean enforceTenancy) throws DocumentException
    {
    	PermissionRole permRole = new PermissionRole();
    	
    	//
    	// Check to see if the tenant ID of the permission and the tenant ID of the role match
    	//
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
    		throw new DocumentException(errMsg);
		}
    	
    	return permRole;
    }
    
	private static Hashtable<String, String> getTenantNamesFromConfig(TenantBindingConfigReaderImpl tenantBindingConfigReader) {

    	// Note that this only handles tenants not marked as "createDisabled"
    	Hashtable<String, TenantBindingType> tenantBindings =
    			tenantBindingConfigReader.getTenantBindings();
    	Hashtable<String, String> tenantInfo = new Hashtable<String, String>();
    	for (TenantBindingType tenantBinding : tenantBindings.values()) {
    		String tId = tenantBinding.getId();
    		String tName = tenantBinding.getName();
    		tenantInfo.put(tId, tName);
    		if (logger.isDebugEnabled()) {
    			logger.debug("getTenantNamesFromConfig found configured tenant id: "+tId+" name: "+tName);
    		}
    	}
    	return tenantInfo;
    }
    
    private static ArrayList<String> compileExistingTenants(Connection conn, Hashtable<String, String> tenantInfo)
    	throws SQLException, Exception {
    	Statement stmt = null;
    	ArrayList<String> existingTenants = new ArrayList<String>();
    	// First find or create the tenants
    	final String queryTenantSQL = "SELECT id,name FROM tenants";
    	try {
    		stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(queryTenantSQL);
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
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(stmt!=null)
    			stmt.close();
    	}

    	return existingTenants;
    }
        
    private static ArrayList<String> findOrCreateDefaultUsers(Connection conn, Hashtable<String, String> tenantInfo) 
        	throws SQLException, Exception {
    	// Second find or create the users
    	Statement stmt = null;
        PreparedStatement pstmt = null;
    	ArrayList<String> usersInRepo = new ArrayList<String>();
        try {
    		stmt = conn.createStatement();
        	ResultSet rs = stmt.executeQuery(QUERY_USERS_SQL);
        	while (rs.next()) {
        		String uName = rs.getString("username");
        		usersInRepo.add(uName);
        	}
        	rs.close();
        	pstmt = conn.prepareStatement(INSERT_USER_SQL); // create a statement
        	for(String tName : tenantInfo.values()) {
        		String adminAcctName = getDefaultAdminUserID(tName);
        		if(!usersInRepo.contains(adminAcctName)) {
        			String salt = UUID.randomUUID().toString();
        			String secEncPasswd = SecurityUtils.createPasswordHash(
        					adminAcctName, DEFAULT_ADMIN_PASSWORD, salt);
        			pstmt.setString(1, adminAcctName);	// set username param
        			pstmt.setString(2, secEncPasswd);	// set passwd param
        			pstmt.setString(3, salt);
        			if (logger.isDebugEnabled()) {
        				logger.debug("createDefaultUsersAndAccounts adding user: "
        						+adminAcctName+" for tenant: "+tName);
        			}
        			pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
        			logger.debug("createDefaultUsersAndAccounts: user: "+adminAcctName
        					+" already exists - skipping.");
        		}


        		String readerAcctName =  getDefaultReaderUserID(tName);
        		if(!usersInRepo.contains(readerAcctName)) {
        			String salt = UUID.randomUUID().toString();
        			String secEncPasswd = SecurityUtils.createPasswordHash(
        					readerAcctName, DEFAULT_READER_PASSWORD, salt);
        			pstmt.setString(1, readerAcctName);	// set username param
        			pstmt.setString(2, secEncPasswd);	// set passwd param
        			pstmt.setString(3, salt);
        			if (logger.isDebugEnabled()) {
        				logger.debug("createDefaultUsersAndAccounts adding user: "
        						+readerAcctName+" for tenant: "+tName);
        			}
        			pstmt.executeUpdate();
        		} else if (logger.isDebugEnabled()) {
        			logger.debug("createDefaultUsersAndAccounts: user: "+readerAcctName
        					+" already exists - skipping.");
        		}
        	}
        	pstmt.close();
        } catch(Exception e) {
        	throw e;
        } finally {
        	if(stmt!=null)
        		stmt.close();
        	if(pstmt!=null)
        		pstmt.close();
        }
        return usersInRepo;
    }
    
    private static void findOrCreateDefaultAccounts(Connection conn, Hashtable<String, String> tenantInfo,
    		ArrayList<String> usersInRepo,
    		Hashtable<String, String> tenantAdminAcctCSIDs, Hashtable<String, String> tenantReaderAcctCSIDs) 
    		    	throws SQLException, Exception {
    	// Third, create the accounts. Assume that if the users were already there,
    	// then the accounts were as well
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(INSERT_ACCOUNT_SQL); // create a statement
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
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(pstmt!=null)
    			pstmt.close();
    	}
    }
    
    private static boolean findOrCreateTenantManagerUserAndAccount(Connection conn) 
    		    	throws SQLException, Exception {
    	// Find or create the special tenant manager account.
    	// Later can make the user name for tenant manager be configurable, settable.
    	Statement stmt = null;
        PreparedStatement pstmt = null;
        boolean created = false;
        try {
        	boolean foundTMgrUser = false;
    		stmt = conn.createStatement();
        	ResultSet rs = stmt.executeQuery(QUERY_TENANT_MGR_USER_SQL);
        	// Should only find one - only consider it
        	if(rs.next()) {
        		String uName = rs.getString("username");
        		foundTMgrUser = uName.equals(TENANT_MANAGER_USER);
        	}
        	rs.close();
        	if(!foundTMgrUser) {
        		String salt = UUID.randomUUID().toString();
        		pstmt = conn.prepareStatement(INSERT_USER_SQL); // create a statement
    			String secEncPasswd = SecurityUtils.createPasswordHash(
    					TENANT_MANAGER_USER, DEFAULT_TENANT_MANAGER_PASSWORD, salt);
    			pstmt.setString(1, TENANT_MANAGER_USER);	// set username param
    			pstmt.setString(2, secEncPasswd);	// set passwd param
    			pstmt.setString(3, salt);
    			if (logger.isDebugEnabled()) {
    				logger.debug("findOrCreateTenantManagerUserAndAccount adding tenant manager user: "
    						+TENANT_MANAGER_USER);
    			}
    			pstmt.executeUpdate();
            	pstmt.close();
            	// Now create the account to match
        		pstmt = conn.prepareStatement(INSERT_ACCOUNT_SQL); // create a statement
				pstmt.setString(1, AuthN.TENANT_MANAGER_ACCT_ID);	 	 // set csid param
				pstmt.setString(2, DEFAULT_TENANT_MANAGER_EMAIL);	// set email param (bogus)
				pstmt.setString(3, TENANT_MANAGER_USER);	// set userid param
				pstmt.setString(4, TENANT_MANAGER_SCREEN_NAME);// set screen name param
				if (logger.isDebugEnabled()) {
					logger.debug("findOrCreateTenantManagerUserAndAccount adding tenant manager account: "
							+TENANT_MANAGER_USER);
				}
				pstmt.executeUpdate();
	    		pstmt.close();
	    		created = true;
        	} else if (logger.isDebugEnabled()) {
        		logger.debug("findOrCreateTenantManagerUserAndAccount: tenant manager: "+TENANT_MANAGER_USER
        				+" already exists.");
        	}
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(stmt!=null)
    			stmt.close();
    		if(pstmt!=null)
    			pstmt.close();
    	}
        return created;
    }
    
    private static void bindDefaultAccountsToTenants(Connection conn, DatabaseProductType databaseProductType,
    		Hashtable<String, String> tenantInfo, ArrayList<String> usersInRepo,
    		Hashtable<String, String> tenantAdminAcctCSIDs, Hashtable<String, String> tenantReaderAcctCSIDs) 
    		    	throws SQLException, Exception {
    	// Fourth, bind accounts to tenants. Assume that if the users were already there,
    	// then the accounts were bound to tenants correctly
    	PreparedStatement pstmt = null;
    	try {
    		String insertAccountTenantSQL;
    		if (databaseProductType == DatabaseProductType.MYSQL) {
    			insertAccountTenantSQL =
    					"INSERT INTO accounts_tenants (TENANTS_ACCOUNTS_COMMON_CSID,tenant_id) "
    							+ " VALUES(?, ?)";
    		} else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
    			insertAccountTenantSQL =
    					"INSERT INTO accounts_tenants (HJID, TENANTS_ACCOUNTS_COMMON_CSID,tenant_id) "
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
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(pstmt!=null)
    			pstmt.close();
    	}
    }
    
    /**
     * Creates the default Admin and Reader roles for all the configured tenants.
     * 
     * Returns the CSID of the Spring Admin role.
     * 
     * @param conn
     * @param tenantInfo
     * @param tenantAdminRoleCSIDs
     * @param tenantReaderRoleCSIDs
     * @return
     * @throws SQLException
     * @throws Exception
     */
    private static String findOrCreateDefaultRoles(Connection conn, Hashtable<String, String> tenantInfo,
    		Hashtable<String, String> tenantAdminRoleCSIDs, Hashtable<String, String> tenantReaderRoleCSIDs) 
    		    	throws SQLException, Exception {

		String springAdminRoleCSID = null;
    	Statement stmt = null;
    	PreparedStatement pstmt = null;
    	try {
    		//
    		// Look for the Spring Security admin role.  If not found, create it.
    		//
    		final String querySpringRole = String.format("SELECT csid from roles WHERE rolename='%s'", AuthN.ROLE_SPRING_ADMIN_NAME);
    		stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(querySpringRole);
    		if (rs.next()) {
    			springAdminRoleCSID = rs.getString(1);
    			if (logger.isDebugEnabled()) {
    				logger.debug("createDefaultAccounts found Spring Admin role: " + springAdminRoleCSID);
    			}
    		} else {
    			final String insertSpringAdminRoleSQL = String.format(
    					"INSERT INTO roles (csid, rolename, displayName, rolegroup, created_at, tenant_id) VALUES ('%s', '%s', '%s', '%s', now(), '%s')",
    					AuthN.ROLE_SPRING_ADMIN_ID, AuthN.ROLE_SPRING_ADMIN_NAME, AuthN.SPRING_ADMIN_USER, AuthN.ROLE_SPRING_GROUP_NAME, AuthN.ADMIN_TENANT_ID);
    			stmt.executeUpdate(insertSpringAdminRoleSQL);
    			springAdminRoleCSID = AuthN.ROLE_SPRING_ADMIN_ID;
    		}
    		rs.close();
    		rs = null;
    		
    		//
    		// Look for and save each tenants default Admin and Reader roles
    		//
    		final String getRoleCSIDSql = "SELECT csid from roles WHERE tenant_id=? and rolename=?";
    		pstmt = conn.prepareStatement(getRoleCSIDSql); // create a statement
    		for (String tenantId : tenantInfo.keySet()) {
    			//
    			// Look for the default Admin role
    			//
    			pstmt.setString(1, tenantId);
    			pstmt.setString(2, getDefaultAdminRole(tenantId));
    			rs = pstmt.executeQuery();
    			// extract data from the ResultSet
    			if (!rs.next()) {
    				throw new RuntimeException("Cannot find role: " + getDefaultAdminRole(tenantId)
    					+ " for tenant id: " + tenantId + " in roles!");
    			}
    			String tenantAdminRoleCSID = rs.getString(1); // First column (#1) is the CSID
    			tenantAdminRoleCSIDs.put(tenantId, tenantAdminRoleCSID);
    			rs.close();
    			rs = null;
    			//
    			// Look for the default Reader role
    			//
    			pstmt.setString(1, tenantId);						// set tenant_id param
    			pstmt.setString(2, getDefaultReaderRole(tenantId));	// set rolename param
    			rs = pstmt.executeQuery();
    			// extract data from the ResultSet
    			if (!rs.next()) {
    				throw new RuntimeException("Cannot find role: " + getDefaultReaderRole(tenantId)
    						+ " for tenant id: " + tenantId + " in roles!");
    			}
    			String tenantReaderRoleCSID = rs.getString(1);
    			tenantReaderRoleCSIDs.put(tenantId, tenantReaderRoleCSID);
    			rs.close();
    		}
    		pstmt.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
        	if (stmt != null) stmt.close();
    		if (pstmt != null) pstmt.close();
    	}
    	
    	return springAdminRoleCSID;
    }

    private static String findTenantManagerRole(Connection conn ) 
    		    	throws SQLException, RuntimeException, Exception {
		String tenantMgrRoleCSID = null;
    	PreparedStatement pstmt = null;
    	try {
    		String rolename = getQualifiedRoleName(AuthN.ALL_TENANTS_MANAGER_TENANT_ID, 
    				AuthN.ROLE_ALL_TENANTS_MANAGER);    		
    		pstmt = conn.prepareStatement(GET_TENANT_MGR_ROLE_SQL); // create a statement
    		ResultSet rs = null;
    		pstmt.setString(1, rolename);	// set rolename param
    		rs = pstmt.executeQuery();
    		if(rs.next()) {
    			tenantMgrRoleCSID = rs.getString(1);
    			if (logger.isDebugEnabled()) {
    				logger.debug("findTenantManagerRole found Tenant Mgr role: "
    						+tenantMgrRoleCSID);
    			}
    		}
    		rs.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(pstmt!=null)
    			pstmt.close();
    	}
    	if(tenantMgrRoleCSID==null)
    		throw new RuntimeException("findTenantManagerRole: Cound not find tenant Manager Role!");
    	return tenantMgrRoleCSID;
    }

    private static void bindAccountsToRoles(Connection conn,  DatabaseProductType databaseProductType,
    		Hashtable<String, String> tenantInfo, ArrayList<String> usersInRepo,
    		String springAdminRoleCSID,
    		Hashtable<String, String> tenantAdminRoleCSIDs, Hashtable<String, String> tenantReaderRoleCSIDs,
    		Hashtable<String, String> tenantAdminAcctCSIDs, Hashtable<String, String> tenantReaderAcctCSIDs) 
    		    	throws SQLException, Exception {
    	// Sixth, bind the accounts to roles. If the users already existed,
    	// we'll assume they were set up correctly.
    	PreparedStatement pstmt = null;
    	try {
    		String insertAccountRoleSQL;
    		if (databaseProductType == DatabaseProductType.POSTGRESQL) {
    			insertAccountRoleSQL = INSERT_ACCOUNT_ROLE_SQL_POSTGRES;
    		} else {
    			throw new Exception("Unrecognized database system.");
    		}
    		
    		pstmt = conn.prepareStatement(insertAccountRoleSQL); // create a statement
    		for (String tId : tenantInfo.keySet()) {
    			String adminUserId = getDefaultAdminUserID(tenantInfo.get(tId));
    			if (!usersInRepo.contains(adminUserId)) {
    				String adminAcct = tenantAdminAcctCSIDs.get(tId);
    				String adminRoleId = tenantAdminRoleCSIDs.get(tId);
    				pstmt.setString(1, adminAcct);		// set acct CSID param
    				pstmt.setString(2, adminUserId);	// set user_id param
    				pstmt.setString(3, adminRoleId);	// set role_id param
    				pstmt.setString(4, getDefaultAdminRole(tId));	// set rolename param
    				pstmt.executeUpdate();
    				//
    				// Now add the Spring Admin Role to the admin accounts
    				//
    				pstmt.setString(3, springAdminRoleCSID);	// set role_id param
    				pstmt.setString(4, AuthN.ROLE_SPRING_ADMIN_NAME);		// set rolename param
    				pstmt.executeUpdate();
    			}
    			String readerUserId = getDefaultReaderUserID(tenantInfo.get(tId));
    			if (!usersInRepo.contains(readerUserId)) {
    				String readerAcct = tenantReaderAcctCSIDs.get(tId);
    				String readerRoleId = tenantReaderRoleCSIDs.get(tId);
    				pstmt.setString(1, readerAcct);		// set acct CSID param
    				pstmt.setString(2, readerUserId);	// set user_id param
    				pstmt.setString(3, readerRoleId);	// set role_id param
    				pstmt.setString(4, getDefaultReaderRole(tId));	// set rolename param
    				pstmt.executeUpdate();
    			}
    		}
    		pstmt.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if (pstmt!=null) {
    			pstmt.close();
    		}
    	}
    }
    
    private static void bindTenantManagerAccountRole(Connection conn,  DatabaseProductType databaseProductType,
    		String tenantManagerUserID, String tenantManagerAccountID, String tenantManagerRoleID, String tenantManagerRoleName ) 
    		    	throws SQLException, Exception {
    	PreparedStatement pstmt = null;
    	try {
    		String insertAccountRoleSQL;
    		if (databaseProductType == DatabaseProductType.MYSQL) {
    			insertAccountRoleSQL = INSERT_ACCOUNT_ROLE_SQL_MYSQL;
    		} else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
    			insertAccountRoleSQL = INSERT_ACCOUNT_ROLE_SQL_POSTGRES;
    		} else {
    			throw new Exception("Unrecognized database system.");
    		}
    		if (logger.isDebugEnabled()) {
    			logger.debug("bindTenantManagerAccountRole binding account to role with SQL:\n"
    					+insertAccountRoleSQL);
    		}
    		pstmt = conn.prepareStatement(insertAccountRoleSQL); // create a statement
    		pstmt.setString(1, tenantManagerAccountID);		// set acct CSID param
    		pstmt.setString(2, tenantManagerUserID);	// set user_id param
    		pstmt.setString(3, tenantManagerRoleID);	// set role_id param
    		pstmt.setString(4, tenantManagerRoleName);	// set rolename param
    		pstmt.executeUpdate();
    		
    		/* At this point, tenant manager should not need the Spring Admin Role
	    		pstmt.setString(3, springAdminRoleCSID);	// set role_id param
	    		pstmt.setString(4, SPRING_ADMIN_ROLE);		// set rolename param
	    		if (logger.isDebugEnabled()) {
	    			logger.debug("createDefaultAccounts binding account: "
	    					+adminUserId+" to Spring Admin role: "+springAdminRoleCSID);
	    		}
	    		pstmt.executeUpdate();
    		*/
    		
    		pstmt.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(pstmt!=null)
    			pstmt.close();
    	}
    }
    
    /*
     * Using the tenant bindings, ensure there are corresponding Tenant records (db columns).
     */
    //FIXME: This code should be using JPA objects and JPATransactionContext, not raw SQL.
    public static void createTenants(
    		TenantBindingConfigReaderImpl tenantBindingConfigReader,
    		DatabaseProductType databaseProductType,
			String cspaceDatabaseName) throws Exception {
		logger.debug("ServiceMain.createTenants starting...");
		Hashtable<String, String> tenantInfo = getTenantNamesFromConfig(tenantBindingConfigReader);
		Connection conn = null;
		try {
			conn = getConnection(cspaceDatabaseName);
			ArrayList<String> existingTenants = compileExistingTenants(conn, tenantInfo);

			// Note that this only creates tenants not marked as "createDisabled"
			createMissingTenants(conn, tenantInfo, existingTenants);
		} catch (Exception e) {
			logger.debug("Exception in createTenants: " + e.getLocalizedMessage());
			throw e;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				if (logger.isDebugEnabled()) {
					logger.debug("SQL Exception closing statement/connection: " + sqle.getLocalizedMessage());
				}
			}
		}
	}
    
    /**
     * 
     * @param tenantBindingConfigReader
     * @param databaseProductType
     * @param cspaceDatabaseName
     * @throws Exception
     */
    //FIXME: This code should be using the JPA objects and JPATransactionContext, not raw SQL.
    public static void createDefaultAccounts(
    		TenantBindingConfigReaderImpl tenantBindingConfigReader,
    		DatabaseProductType databaseProductType,
    		String cspaceDatabaseName) throws Exception {

    	logger.debug("ServiceMain.createDefaultAccounts starting...");
    	
        Hashtable<String, String> tenantInfo = getTenantNamesFromConfig(tenantBindingConfigReader);
        Connection conn = null;
        // TODO - need to put in tests for existence first.
        // We could just look for the accounts per tenant up front, and assume that
        // the rest is there if the accounts are.
        // Could add a sql script to remove these if need be - Spring only does roles, 
        // and we're not touching that, so we could safely toss the 
        // accounts, users, account-tenants, account-roles, and start over.
        try {
        	conn = getConnection(cspaceDatabaseName);
	        
	        ArrayList<String> usersInRepo = findOrCreateDefaultUsers(conn, tenantInfo);
	        
    		Hashtable<String, String> tenantAdminAcctCSIDs = new Hashtable<String, String>();
    		Hashtable<String, String> tenantReaderAcctCSIDs = new Hashtable<String, String>();
	        findOrCreateDefaultAccounts(conn, tenantInfo, usersInRepo,
	        		tenantAdminAcctCSIDs, tenantReaderAcctCSIDs);

	        bindDefaultAccountsToTenants(conn, databaseProductType, tenantInfo, usersInRepo,
	        		tenantAdminAcctCSIDs, tenantReaderAcctCSIDs);
	        
    		Hashtable<String, String> tenantAdminRoleCSIDs = new Hashtable<String, String>();
    		Hashtable<String, String> tenantReaderRoleCSIDs = new Hashtable<String, String>();
    		String springAdminRoleCSID = findOrCreateDefaultRoles(conn, tenantInfo,
    				tenantAdminRoleCSIDs, tenantReaderRoleCSIDs);
    		
    		bindAccountsToRoles(conn,  databaseProductType,
    				tenantInfo, usersInRepo, springAdminRoleCSID,
    				tenantAdminRoleCSIDs, tenantReaderRoleCSIDs,
    				tenantAdminAcctCSIDs, tenantReaderAcctCSIDs);
    		
    		boolean createdTenantMgrAccount = findOrCreateTenantManagerUserAndAccount(conn);
    		if (createdTenantMgrAccount) {
    			// If we created the account, we need to create the bindings. Otherwise, assume they
    			// are all set (from previous initialization).
	    		String tenantManagerRoleCSID = findTenantManagerRole(conn);
	    		bindTenantManagerAccountRole(conn, databaseProductType, 
	    				TENANT_MANAGER_USER, AuthN.TENANT_MANAGER_ACCT_ID, 
	    				tenantManagerRoleCSID, AuthN.ROLE_ALL_TENANTS_MANAGER);
    		}
        } catch (Exception e) {
			logger.debug("Exception in createDefaultAccounts: " + e.getLocalizedMessage());
        	throw e;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqle) {
				if (logger.isDebugEnabled()) {
					logger.debug("SQL Exception closing statement/connection: " + sqle.getLocalizedMessage());
				}
			}
		}    	
    }
    
    private static String getDefaultAdminRole(String tenantId) {
    	return ROLE_PREFIX + tenantId + TENANT_ADMIN_ROLE_SUFFIX;
    }
    
    private static String getDefaultReaderRole(String tenantId) {
    	return ROLE_PREFIX+tenantId+TENANT_READER_ROLE_SUFFIX;
    }
    
    private static String getDefaultAdminUserID(String tenantName) {
    	return TENANT_ADMIN_ACCT_PREFIX + tenantName;
    }
    
    private static String getDefaultReaderUserID(String tenantName) {
    	return TENANT_READER_ACCT_PREFIX + tenantName;
    }
    
	static private PermissionAction createPermissionAction(Permission perm,
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
	
	private static HashSet<String> getTransitionVerbList(TenantBindingType tenantBinding, ServiceBindingType serviceBinding) {
		HashSet<String> result = new HashSet<String>();
		
		TransitionDefList transitionDefList = getTransitionDefList(tenantBinding, serviceBinding);
    	for (TransitionDef transitionDef : transitionDefList.getTransitionDef()) {
    		String transitionVerb = transitionDef.getName();
    		String[] tokens = transitionVerb.split("_");  // Split the verb into words.  The workflow verbs are compound words combined with the '_' character.
    		result.add(tokens[0]); // We only care about the first word.
    	}

    	return result;
	}
	
	private static TransitionDefList getTransitionDefList(TenantBindingType tenantBinding, ServiceBindingType serviceBinding) {
		TransitionDefList result = null;
		try {
			String serviceObjectName = serviceBinding.getObject().getName();
			
	    	@SuppressWarnings("rawtypes")
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
			if (serviceBinding.getType().equalsIgnoreCase(ServiceBindingUtils.SERVICE_TYPE_SECURITY) == false) {
				logger.debug("Could not retrieve a lifecycle transition definition list from: "
						+ serviceBinding.getName()
						+ " with tenant ID = "
						+ tenantBinding.getId());
			}
			// return an empty list			
			result = new TransitionDefList();
		} else {
			logger.debug("Successfully retrieved a lifecycle transition definition list from: "
					+ serviceBinding.getName()
					+ " with tenant ID = "
					+ tenantBinding.getId());
		}
		
		return result;
	}
	
	/**
	 * Creates the immutable workflow permission sets for the default admin and reader roles.
	 * 
	 * @param tenantBindingConfigReader
	 * @param databaseProductType
	 * @param cspaceDatabaseName
	 * @throws Exception
	 */
    public static void createDefaultWorkflowPermissions(
    		JPATransactionContext jpaTransactionContext,
    		TenantBindingConfigReaderImpl tenantBindingConfigReader,
    		DatabaseProductType databaseProductType, 
    		String cspaceDatabaseName) throws Exception
    {
    	java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();

    	AuthZ.get().login(); //login to Spring Security manager
    	
        try {
	        Hashtable<String, TenantBindingType> tenantBindings = tenantBindingConfigReader.getTenantBindings();
	        for (String tenantId : tenantBindings.keySet()) {
	        	logger.info(String.format("Creating/verifying workflow permissions for tenant ID=%s.", tenantId));
		        TenantBindingType tenantBinding = tenantBindings.get(tenantId);
		        if (tenantBinding.isConfigChangedSinceLastStart() == false) {
		        	continue; // skip the rest of the loop and go to the next tenant
		        }
		        
	    		Role adminRole = AuthorizationCommon.getRole(jpaTransactionContext, tenantBinding.getId(), ROLE_TENANT_ADMINISTRATOR);
	    		Role readonlyRole = AuthorizationCommon.getRole(jpaTransactionContext, tenantBinding.getId(), ROLE_TENANT_READER);
	    		
	    		if (adminRole == null || readonlyRole == null) {
	    			String msg = String.format("One or more of the required default CollectionSpace administrator roles is missing or was never created.  If you're setting up a new instance of CollectionSpace, shutdown the Tomcat server and run the 'ant import' command from the root/top level CollectionSpace 'Services' source directory.  Then try restarting Tomcat.");
	    			logger.info(msg);
	    			throw new RuntimeException("One or more of the required default CollectionSpace administrator roles is missing or was never created.");
	    		}
	    		
		        for (ServiceBindingType serviceBinding : tenantBinding.getServiceBindings()) {
		        	String prop = ServiceBindingUtils.getPropertyValue(serviceBinding, REFRESH_AUTHZ_PROP);
		        	if (prop == null ? true : Boolean.parseBoolean(prop)) {
			        	try {
			        		jpaTransactionContext.beginTransaction();
				        	HashSet<String> transitionVerbList = getTransitionVerbList(tenantBinding, serviceBinding);
				        	for (String transitionVerb : transitionVerbList) {
				        		//
				        		// Create the permission for the admin role
				        		Permission adminPerm = createWorkflowPermission(tenantBinding, serviceBinding, transitionVerb, ACTIONGROUP_CRUDL, true);
				        		persist(jpaTransactionContext, adminPerm, adminRole, true, ACTIONGROUP_CRUDL);
				        		//
				        		// Create the permission for the read-only role
				        		Permission readonlyPerm = createWorkflowPermission(tenantBinding, serviceBinding, transitionVerb, ACTIONGROUP_RL, true);				        		
				        		persist(jpaTransactionContext, readonlyPerm, readonlyRole, true, ACTIONGROUP_RL); // Persist/store the permission and permrole records and related Spring Security info
				        	}
				        	jpaTransactionContext.commitTransaction();
			        	} catch (IllegalStateException e) {
			        		logger.fine(e.getLocalizedMessage()); //We end up here if there is no document handler for the service -this is ok for some of the services.
			        	} catch (Exception x) {
			        		jpaTransactionContext.markForRollback();
			        	}
		        	} else {
		        		logger.warning("AuthZ refresh service binding property is set to FALSE so default permissions will NOT be refreshed for: "
		        				+ serviceBinding.getName());
		        	}
		        }
	        }
    	} catch (Exception e) {
    		jpaTransactionContext.markForRollback();
            logger.fine("Caught exception and rolling back permission creation: " + e.getMessage());
            throw e;
        }
    }
    
	private static void createMissingTenants(Connection conn, Hashtable<String, String> tenantInfo,
    		ArrayList<String> existingTenants) throws SQLException, Exception {
		// Need to define and look for a createDisabled attribute in tenant config
    	final String insertTenantSQL = 
    		"INSERT INTO tenants (id,name,authorities_initialized,disabled,created_at) VALUES (?,?,FALSE,FALSE,now())";
        PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(insertTenantSQL); // create a statement
    		for(String tId : tenantInfo.keySet()) {
    			if(existingTenants.contains(tId)) {
    				if (logger.isDebugEnabled()) {
    					logger.debug("createMissingTenants: tenant exists (skipping): "
    							+tenantInfo.get(tId));
    				}
    				continue;
    			}
    			pstmt.setString(1, tId);					// set id param
    			pstmt.setString(2, tenantInfo.get(tId));	// set name param
    			if (logger.isDebugEnabled()) {
    				logger.debug("createMissingTenants adding entry for tenant: "+tId);
    			}
    			pstmt.executeUpdate();
    		}
    		pstmt.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if(pstmt!=null)
    			pstmt.close();
    	}
    }
    
    public static String getPersistedMD5Hash(String tenantId, String cspaceDatabaseName) throws Exception {
    	String result = null;
    	
    	// First find or create the tenants
    	final String queryTenantSQL = String.format("SELECT id, name, config_md5hash FROM tenants WHERE id = '%s'", tenantId);
    	
    	Statement stmt = null;
    	Connection conn;
    	int rowCount = 0;
    	try {
			conn = getConnection(cspaceDatabaseName);
    		stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(queryTenantSQL);
    		while (rs.next()) {
    			if (rowCount > 0) {
    				String errMsg = String.format("Unable to configure tenant ID='%s'.  There appears to be more than one tenant with that ID in the AuthN/AuthZ database named '%s'.",
    						tenantId, cspaceDatabaseName);
    				throw new Exception(errMsg);
    			}
    			String tId = rs.getString("id");		// for debugging only
    			String tName = rs.getString("name");	// for debugging only
    			result = rs.getString("config_md5hash");
    			rowCount++;
    		}
    		rs.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if (stmt != null) stmt.close();
    	}
    	
    	return result;
    }

	private static PermissionRoleRel findPermRoleRel(
			JPATransactionContext jpaTransactionContext,
			String permissionId,
			String RoleId) {
    	PermissionRoleRel result = null;
    	
    	try {
	        String whereClause = "where permissionId = :id and roleId = :roleId";
	        HashMap<String, Object> params = new HashMap<String, Object>();
	        params.put("id", permissionId);
	        params.put("roleId", RoleId);        
	
	        result = (PermissionRoleRel) JpaStorageUtils.getEntity(jpaTransactionContext,
	        		PermissionRoleRel.class.getCanonicalName(), whereClause, params);
    	} catch (Exception e) {
    		//Do nothing. Will return null;
    	}
    	    	
    	return result;
    }
    
    /*
     * Persists the Permission, PermissionRoleRel, and Spring Security table entries all in one transaction
     */
    private static void persist(JPATransactionContext jpaTransactionContext, Permission permission, Role role, boolean enforceTenancy, ActionGroup actionGroup) throws Exception {
		AuthorizationStore authzStore = new AuthorizationStore();
		// First persist the Permission record
		authzStore.store(jpaTransactionContext, permission);
		
		// If the PermRoleRel doesn't already exists then relate the permission and the role in a new PermissionRole (the service payload)
		// Create a PermissionRoleRel (the database relation table for the permission and role)
		PermissionRoleRel permRoleRel = findPermRoleRel(jpaTransactionContext, permission.getCsid(), role.getCsid());
		if (permRoleRel == null) {
			PermissionRole permRole = createPermissionRole(permission, role, enforceTenancy);
	        List<PermissionRoleRel> permRoleRels = new ArrayList<PermissionRoleRel>();
	        PermissionRoleUtil.buildPermissionRoleRel(jpaTransactionContext, permRole, SubjectType.ROLE, permRoleRels,
	        		false /*not for delete*/, role.getTenantId());
	        for (PermissionRoleRel prr : permRoleRels) {
	            authzStore.store(jpaTransactionContext, prr);
	        }
			Profiler profiler = new Profiler(AuthorizationCommon.class, 2);
			profiler.start();
			// Add a corresponding entry in the Spring Security Tables
			addPermissionsForUri(jpaTransactionContext, permission, permRole);
			profiler.stop();
			logger.debug("Finished full perm generation for "
					+ ":" + permission.getTenantId()
					+ ":" + permission.getResourceName()
					+ ":" + actionGroup.getName()
					+ ":" + profiler.getCumulativeTime());
		}
        
    }
	
	public static boolean hasTokenExpired(EmailConfig emailConfig, Token token) throws NoSuchAlgorithmException {
		boolean result = false;
		
		int maxConfigSeconds = emailConfig.getPasswordResetConfig().getTokenExpirationSeconds().intValue();
		int maxTokenSeconds = token.getExpireSeconds().intValue();
		
		long createdTime = token.getCreatedAtItem().getTime();		
		long configExpirationTime = createdTime + maxConfigSeconds * 1000;		// the current tenant config for how long a token stays valid
		long tokenDefinedExirationTime = createdTime + maxTokenSeconds * 1000;	// the tenant config for how long a token stays valid when the token was created.
		
		if (configExpirationTime != tokenDefinedExirationTime) {
			String msg = String.format("The configured expiration time for the token = '%s' changed from when the token was created.",
					token.getId());
			logger.warn(msg);
		}
		//
		// Note: the current tenant bindings config for expiration takes precedence over the config used to create the token.
		//
		if (System.currentTimeMillis() >= configExpirationTime) {
			result = true;
		}
		
		return result;
	}
		
	/*
	 * Validate that the password reset configuration is correct.
	 */
	private static String validatePasswordResetConfig(PasswordResetConfig passwordResetConfig) {
		String result = null;
		
		if (passwordResetConfig != null) {
			result = passwordResetConfig.getMessage();
			if (result == null || result.length() == 0) {
				result = DEFAULT_PASSWORD_RESET_EMAIL_MESSAGE;
				logger.warn("Could not find a password reset message in the tenant's configuration.  Using the default one");
			}
			
			if (result.contains("{{link}}") == false) {
				logger.warn("The tenant's password reset message does not contain a required '{{link}}' marker.");
				result = null;
			}
			
			if (passwordResetConfig.getLoginpage() == null || passwordResetConfig.getLoginpage().trim().isEmpty()) {
				logger.warn("The tenant's password reset configuration is missing a 'loginpage' value.  It should be set to something like '/collectionspace/ui/core/html/index.html'.");
				result = null;
			}
			
		    String subject = passwordResetConfig.getSubject();
		    if (subject == null || subject.trim().isEmpty()) {
		    	passwordResetConfig.setSubject(DEFAULT_PASSWORD_RESET_EMAIL_SUBJECT);
		    }

		}
		
		return result;
	}
	
	/*
	 * Generate a password reset message. Embeds an authorization token to reset a user's password.
	 */
	public static String generatePasswordResetEmailMessage(EmailConfig emailConfig, AccountListItem accountListItem, Token token) throws Exception {
		String result = null;
		
		result = validatePasswordResetConfig(emailConfig.getPasswordResetConfig());
		if (result == null) {
			String errMsg = String.format("The password reset configuration for the tenant ID='%s' is missing or malformed.  Could not initiate a password reset for user ID='%s. See the log files for more details.",
					token.getTenantId(), accountListItem.getEmail());
			throw new Exception(errMsg);
		}
		
		String link = emailConfig.getBaseurl() + emailConfig.getPasswordResetConfig().getLoginpage() + "?token=" + token.getId();
		result = result.replaceAll("\\{\\{link\\}\\}", link);
		
		if (result.contains("{{greeting}}")) {
			String greeting = accountListItem.getScreenName();
			result = result.replaceAll("\\{\\{greeting\\}\\}", greeting);
			result = result.replaceAll("\\\\n", "\\\n");
			result = result.replaceAll("\\\\r", "\\\r");
		}			
		
		return result;
	}

	public static void persistTenantBindingsMD5Hash(TenantBindingConfigReaderImpl tenantBindingConfigReader,
			DatabaseProductType databaseProductType, String cspaceDatabaseName) throws Exception {
		// Need to define and look for a createDisabled attribute in tenant config
		String updateTableSQL = "UPDATE tenants SET config_md5hash = ? WHERE id = ?";

    	Connection conn;
        PreparedStatement pstmt = null;
    	try {
			conn = getConnection(cspaceDatabaseName);
    		pstmt = conn.prepareStatement(updateTableSQL); // create a statement
    		for (String tId : AuthorizationCommon.tenantConfigMD5HashTable.keySet()) {
    			pstmt.setString(1, AuthorizationCommon.getTenantConfigMD5Hash(tId));
    			pstmt.setString(2, tId);
    			if (logger.isDebugEnabled()) {
    				logger.debug("createMissingTenants adding entry for tenant: " + tId);
    			}
    			pstmt.executeUpdate();
    		}
    		pstmt.close();
    	} catch(Exception e) {
    		throw e;
    	} finally {
    		if (pstmt!=null) pstmt.close();
    	}
    }
}
