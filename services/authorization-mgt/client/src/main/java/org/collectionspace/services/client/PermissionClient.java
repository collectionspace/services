/**	
 * PermissionClient.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.description.ServiceDescription;

/**
 * A PermissionClient.

 * @version $Revision:$
 */
public class PermissionClient extends AbstractServiceClientImpl<PermissionsList, Permission, Permission, PermissionProxy> {
	public static final String SERVICE_NAME = "authorization/permissions";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;	
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_PATH_PROXY = SERVICE_PATH + "/";
	
	public enum ActionCompare {
	    ACTION_GROUP_EMPTY, ACTION_LIST_EMPTY, ACTIONS_MISSING, MATCHES, MISMATCHES
	}
    
	public PermissionClient() throws Exception {
		super();
	}

	public PermissionClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	/* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
	public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public Class<PermissionProxy> getProxyClass() {
		return PermissionProxy.class;
	}
	
	/*
	 * CRUD+L Methods
	 */
	
    /**
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#readList()
     */
	@Override
    public Response readList() {
        return getProxy().readList();

    }

    public Response readSearchList(String resourceName) {
        return getProxy().readSearchList(resourceName);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#getAccount(java.lang.String)
     */
    @Override
    public Response read(String csid) {
        return getProxy().read(csid);
    }

    /**
     * @param permission
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#create(org.collectionspace.services.permission.Permission)
     */
    @Override
    public Response create(Permission permission) {
        return getProxy().create(permission);
    }

    /**
     * @param csid
     * @param permission
     * @return
     * @see org.collectionspace.hello.client.PermissionProxy#updateAccount(java.lang.Long, org.collectionspace.services.permission.Permission)
     */
    @Override
    public Response update(String csid, Permission permission) {
        return getProxy().update(csid, permission);
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
	
    public static String getActionGroup(List<PermissionAction> actionList) {
		String result = null;
		HashMap<ActionType, String> actionMap = getEmptyActionMap();
		
		for (PermissionAction permAction : actionList) {
			switch (permAction.getName()) {
				case CREATE:
					actionMap.put(ActionType.CREATE, "C");
					break;
				case READ:
					actionMap.put(ActionType.READ, "R");
					break;
				case UPDATE:
					actionMap.put(ActionType.UPDATE, "U");
					break;
				case DELETE:
					actionMap.put(ActionType.DELETE, "D");
					break;
				case SEARCH:
					actionMap.put(ActionType.SEARCH, "L");
					break;
				default:
					String msg = String.format("Unknown permission action '%s'.", permAction.getName().value());
					logger.error(null);
					return result;
			}
		}
		
		result = String.format("%s%s%s%s%s",
				actionMap.get(ActionType.CREATE),
				actionMap.get(ActionType.READ),
				actionMap.get(ActionType.UPDATE),
				actionMap.get(ActionType.DELETE),
				actionMap.get(ActionType.SEARCH));
		
		return result;
	}

	private static HashMap<ActionType, String> getEmptyActionMap() {
		HashMap<ActionType, String> emptyActionMap = new HashMap<ActionType, String>();
		
		emptyActionMap.put(ActionType.CREATE, "");
		emptyActionMap.put(ActionType.READ, "");
		emptyActionMap.put(ActionType.UPDATE, "");
		emptyActionMap.put(ActionType.DELETE, "");
		emptyActionMap.put(ActionType.SEARCH, "");

		return emptyActionMap;
	}
	
	public static List<PermissionAction> getActionList(String actionGroup) {
		if (actionGroup == null || actionGroup.trim().isEmpty()) {
			return null;
		}
		
		List<PermissionAction> result = new ArrayList<PermissionAction>();
		for (char c : actionGroup.toUpperCase().toCharArray()) {
			switch (c) {
				case 'C':
					result.add(PermissionActionFactory.create(ActionType.CREATE));
					break;
				case 'R':
					result.add(PermissionActionFactory.create(ActionType.READ));
					break;
				case 'U':
					result.add(PermissionActionFactory.create(ActionType.UPDATE));
					break;
				case 'D':
					result.add(PermissionActionFactory.create(ActionType.DELETE));
					break;
				case 'L':
					result.add(PermissionActionFactory.create(ActionType.SEARCH));
					break;
			}
		}
		
		return result;
	}
	
	/*
	 * Validate that the permission's action group and action list are non-null, non-empty, and equivalent.
	 * Returns:
	 * 		-1 - Permission action group is empty or null
	 */
	public static ActionCompare validatePermActions(Permission permission) {
		String actionGroup = permission.getActionGroup();
		List<PermissionAction> actionList = permission.getAction();

		if ((actionGroup == null || actionGroup.trim().isEmpty() == true) && (actionList == null || actionList.size() < 1)) {
			return ActionCompare.ACTIONS_MISSING;
		}
		
		if (actionGroup == null || actionGroup.trim().isEmpty() == true) {
			return ActionCompare.ACTION_GROUP_EMPTY;
		}
		
		if (actionList == null || actionList.size() < 1) {
			return ActionCompare.ACTION_LIST_EMPTY;
		}
				
		String actionGroupFromActionList = getActionGroup(permission.getAction());
		if (actionGroupFromActionList == null || !actionGroupFromActionList.equalsIgnoreCase(actionGroup)) {
			return ActionCompare.MISMATCHES;
		}
		
		return ActionCompare.MATCHES;
	}
}
