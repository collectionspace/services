/**	
 * RoleClient.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (C) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RolesList;
import org.collectionspace.services.description.ServiceDescription;

/**
 * A RoleClient.

 * @version $Revision:$
 */
public class RoleClient extends AbstractServiceClientImpl<RolesList, Role, Role, RoleProxy> {
	public static final String SERVICE_NAME = "authorization/roles";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";	
	public final static String IMMUTABLE = "immutable";
	private final static String BACKEND_ROLE_PREFIX = "ROLE_";

    public RoleClient() throws Exception {
		super();
	}

    public RoleClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}
    
    /**
     * Creates a backend (Spring Security as of v4.5) role name.
     * @param roleDisplayName
     * @param tenantId
     * @return
     */
    static public String getBackendRoleName(String roleDisplayName, String tenantId) {
        String roleName = roleDisplayName.toUpperCase();
        String rolePrefix = BACKEND_ROLE_PREFIX + tenantId + "_";
        if (!roleName.startsWith(rolePrefix)) {
            roleName = rolePrefix + roleName;
        }
        return roleName;
    }
    
    /*
     * Only call this method with a valid backend role name (not the display name).
     */
    static public String inferDisplayName(String backendRoleName, String tenantId) {
        String rolePrefix = BACKEND_ROLE_PREFIX + tenantId + "_";
        String inferredRoleName = backendRoleName.replace(rolePrefix, "");
        
        if (logger.isWarnEnabled()) {
        	String msg = String.format("Role display name '%s' is being inferred from backend role name '%s'.", 
        			inferredRoleName, backendRoleName);
        	logger.warn(msg);
        }
        
        return inferredRoleName;
    }
    
	@Override
    public String getServiceName() { 
    	throw new UnsupportedOperationException(); //FIXME: REM - http://issues.collectionspace.org/browse/CSPACE-3498 }
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
	public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

    @Override
	public Response readList() {
        return getProxy().readList();

    }

    public Response readSearchList(String roleName) {
        return getProxy().readSearchList(roleName);

    }

    @Override
	public Response read(String csid) {
        return getProxy().read(csid);
    }
    
    public Response readRoleAccounts(String csid) {
    	return getProxy().readRoleAccounts(csid);
    }

    /**
     * Creates the.
     *
     * @param role the role
     * @return the client response
     */
    @Override
	public Response create(Role role) {
        return getProxy().create(role);
    }

    /**
     * @param csid
     * @param role
     * @return
     */
    @Override
	public Response update(String csid, Role role) {
        return getProxy().update(csid, role);
    }

	@Override
	public Class<RoleProxy> getProxyClass() {
		return RoleProxy.class;
	}

	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}
}
