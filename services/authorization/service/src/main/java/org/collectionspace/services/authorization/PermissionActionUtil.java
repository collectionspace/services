package org.collectionspace.services.authorization;

import org.collectionspace.services.authorization.PermissionAction;
import org.collectionspace.services.authorization.ActionType;
import org.collectionspace.services.authorization.Permission;

public class PermissionActionUtil {

	static public PermissionAction create(Permission perm,
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
}
