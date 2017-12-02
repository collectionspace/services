package org.collectionspace.services.client;

import org.collectionspace.services.authorization.perms.ActionType;
import org.collectionspace.services.authorization.perms.PermissionAction;

public class PermissionActionFactory {
	public static PermissionAction create(ActionType actionType) {
		PermissionAction result = new PermissionAction();
        result.setName(actionType);
        return result; 
	}
}
