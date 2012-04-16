package org.collectionspace.services.common.authorization_mgt;

import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.RoleValue;

public class AuthorizationRoleRel {

    /**
     * Builds the account value.
     *
     * @param arr the arr
     * @return the account value
     */
    static public AccountValue buildAccountValue(AccountRoleRel arr) {
        AccountValue av = new AccountValue();
        av.setAccountId(arr.getAccountId());
        av.setUserId(arr.getUserId());
        av.setScreenName(arr.getScreenName());
        return av;
    }
	
    /**
     * Builds the role value.
     *
     * @param arr the arr
     * @return the role account value
     */
    static public RoleValue buildRoleValue(AccountRoleRel arr) {
    	RoleValue rv = null;
    	if (arr.getRoleId().equals(AuthorizationCommon.ROLE_SPRING_ADMIN_ID) == false) {
	    	rv = new RoleValue();
	        rv.setRoleId(arr.getRoleId());
	        rv.setRoleName(arr.getRoleName());
	    	rv.setRoleRelationshipId(arr.getHjid().toString());
    	}
        return rv;
    }
    
    /**
     * Builds the permission value.
     *
     * @param prr the prr
     * @return the permission value
     */
    static public PermissionValue buildPermissionValue(PermissionRoleRel prr) {
        PermissionValue pv = new PermissionValue();
        pv.setPermissionId(prr.getPermissionId());
        pv.setResourceName(prr.getPermissionResource());
        pv.setActionGroup(prr.getActionGroup());
        pv.setPermRelationshipId(prr.getHjid().toString());
        return pv;
    }
    
    /**
     * Builds the role value.
     *
     * @param prr the prr
     * @return the role value
     */
    static public RoleValue buildRoleValue(PermissionRoleRel prr) {
        RoleValue rv = new RoleValue();
        rv.setRoleId(prr.getRoleId());
        rv.setRoleName(prr.getRoleName());
        rv.setRoleRelationshipId(prr.getHjid().toString());
        return rv;
    }
    
}
