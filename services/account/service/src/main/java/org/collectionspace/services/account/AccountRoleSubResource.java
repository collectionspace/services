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
package org.collectionspace.services.account;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.PersistenceException;

import org.collectionspace.services.account.storage.AccountRoleDocumentHandler;
//import org.collectionspace.services.authorization.AccountRolesList;
//import org.collectionspace.services.authorization.AccountRolesList.AccountRoleListItem;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;

import org.collectionspace.services.common.authorization_mgt.AuthorizationCommon;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaRelationshipStorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.collectionspace.services.common.context.ServiceContextProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountRoleSubResource is used to manage account-role relationship
 * @author
 */
public class AccountRoleSubResource
//        extends AbstractCollectionSpaceResourceImpl<AccountRole, AccountRolesList> {
    	extends AbstractCollectionSpaceResourceImpl<AccountRole, AccountRole> {
	
    final public static String ACCOUNT_ACCOUNTROLE_SERVICE = "accounts/accountroles";
    final public static String ROLE_ACCOUNTROLE_SERVICE = "authorization/roles/accountroles";
    //this service is never exposed as standalone RESTful service...just use unique
    //service name to identify binding
    /** The service name. */
    private String serviceName = ACCOUNT_ACCOUNTROLE_SERVICE;
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(AccountRoleSubResource.class);
    /** The storage client. */
    final StorageClient storageClient = new JpaRelationshipStorageClient<AccountRole>();

    /**
     *
     * @param serviceName qualified service path
     */
    public AccountRoleSubResource(String serviceName) {
        this.serviceName = serviceName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getVersionString()
     */
    @Override
    protected String getVersionString() {
        /** The last change revision. */
        final String lastChangeRevision = "$LastChangedRevision: 1165 $";
        return lastChangeRevision;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
     */
    @Override
    public Class<AccountRole> getCommonPartClass() {
        return AccountRole.class;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.CollectionSpaceResource#getServiceContextFactory()
     */
    @Override
    public ServiceContextFactory<AccountRole, AccountRole> getServiceContextFactory() {
//    public ServiceContextFactory<AccountRole, AccountRolesList> getServiceContextFactory() {
        return RemoteServiceContextFactory.get();
    }

    /**
     * Creates the service context.
     * 
     * @param input the input
     * @param subject the subject
     * 
     * @return the service context< account role, account role>
     * 
     * @throws Exception the exception
     */
    private ServiceContext<AccountRole, AccountRole> createServiceContext(AccountRole input,
            SubjectType subject) throws Exception {
        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext(input);
        ctx.setDocumentType(AccountRole.class.getPackage().getName()); //persistence unit
        ctx.setProperty(ServiceContextProperties.ENTITY_NAME, AccountRoleRel.class.getName());
        ctx.setProperty(ServiceContextProperties.ENTITY_CLASS, AccountRoleRel.class);
        //subject name is necessary to indicate if role or account is a subject
        ctx.setProperty(ServiceContextProperties.SUBJECT, subject);
        
        //set context for the relationship query
        if (subject == SubjectType.ROLE) {
            ctx.setProperty(ServiceContextProperties.OBJECT_CLASS, AccountsCommon.class);
            ctx.setProperty(ServiceContextProperties.OBJECT_ID, "account_id");
        } else if (subject == SubjectType.ACCOUNT) {
            ctx.setProperty(ServiceContextProperties.OBJECT_CLASS, Role.class);
            ctx.setProperty(ServiceContextProperties.OBJECT_ID, "role_id");
        }
        
        return ctx;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getStorageClient(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public StorageClient getStorageClient(ServiceContext<AccountRole, AccountRole> ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    /**
     * createAccountRole creates one or more account-role relationships
     * between object (account/role) and subject (role/account)
     * @param input
     * @param subject
     * @return
     * @throws Exception
     */
    public String createAccountRole(AccountRole input, SubjectType subject)
            throws Exception {
    	
    	//
    	// We need to associate every new account with the Spring Security Admin role so we can make
    	// changes to the Spring Security ACL tables.  The Spring Security Admin role has NO CollectionSpace
    	// specific permissions.  It is an internal/private role that service consumers and end-users NEVER see.
    	//
    	
    	//Preserve the original incoming list of roles
    	List<RoleValue> inputRoleValues = input.getRole();

    	//Change the role list to be just the Spring role
    	List<RoleValue> springRoles = new ArrayList<RoleValue>();
    	input.setRole(springRoles);
    	RoleValue springAdminRole = new RoleValue();
    	springRoles.add(springAdminRole);
    	springAdminRole.setRoleId(AuthorizationCommon.ROLE_SPRING_ADMIN_ID);
    	springAdminRole.setRoleName(AuthorizationCommon.ROLE_SPRING_ADMIN_NAME);

    	// The Spring role relationship may already exist, if it does then we'll get a PersistenceException that
    	// we'll just ignore.
    	try {
	        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext(input, subject);
	        DocumentHandler handler = createDocumentHandler(ctx);        
	        getStorageClient(ctx).create(ctx, handler);
    	} catch (PersistenceException e) {
    		//If we get this exception, it means that the role relationship already exists, so
    		//we can just ignore this exception.
    		if (logger.isTraceEnabled() == true) {
    			logger.trace(AuthorizationCommon.ROLE_SPRING_ADMIN_NAME +
    					" relationship already exists for account: " +
    					input.getAccount().get(0).getAccountId(), e);
    		}
    	}
    	
    	//
    	// Now we'll add the account relationships for the original incoming roles.
    	//
    	input.setRole(inputRoleValues);
        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext(input, subject);
        DocumentHandler handler = createDocumentHandler(ctx);        
        String bogusCsid = getStorageClient(ctx).create(ctx, handler);
        
        return bogusCsid;
    }

    /**
     * getAccountRole retrieves account-role relationships using given
     * csid of object (account/role) and subject (role/account)
     * @param csid
     * @param subject
     * @return
     * @throws Exception
     */
    public AccountRole getAccountRole(
            String csid, SubjectType subject) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("getAccountRole with csid=" + csid);
        }
        AccountRole result = null;
        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext((AccountRole) null, subject);
        DocumentHandler handler = createDocumentHandler(ctx);
        getStorageClient(ctx).get(ctx, csid, handler);
        result = (AccountRole) ctx.getOutput();

        return result;
    }

    /**
     * Gets the account role.
     *
     * @param csid the csid
     * @param subject the subject
     * @param accountRoleCsid the account role csid
     * @return the account role
     * @throws Exception the exception
     */
    public AccountRoleRel getAccountRoleRel(String csid,
    		SubjectType subject,
    		String accountRoleCsid) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("getAccountRole with csid=" + csid);
        }
//        AccountRolesList result = new AccountRolesList();
        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext((AccountRole) null, subject);
        AccountRoleDocumentHandler handler = (AccountRoleDocumentHandler)createDocumentHandler(ctx);
        handler.setAccountRoleCsid(accountRoleCsid);
        //getStorageClient(ctx).get(ctx, csid, handler);
        AccountRoleRel accountRoleRel = (AccountRoleRel)JpaStorageUtils.getEntity(new Long(accountRoleCsid).longValue(), AccountRoleRel.class);
//        List<AccountRoleListItem> accountRoleList = result.getAccountRoleListItems();
//        AccountRoleListItem listItem = new AccountRoleListItem();
//        // fill the item
//        listItem.setCsid(accountRoleRel.getHjid().toString());
//        listItem.setRoleId(accountRoleRel.getRoleId());
//        listItem.setRoleName(accountRoleRel.getRoleName());
        // add item to result list
//        result = (AccountRolesList) ctx.getOutput();
        
        return accountRoleRel;
    }

    /**
     * X_delete account role.
     *
     * @param csid the csid
     * @param subject the subject
     * @throws Exception the exception
     */
    public void x_deleteAccountRole(String csid,
            SubjectType subject) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccountRole with csid=" + csid);
        }
        AccountRole toDelete = getAccountRole(csid, subject);
        deleteAccountRole(csid, subject, toDelete);
    }
    
    /**
     * deleteAccountRole deletes all account-role relationships using given
     * csid of object (account/role) and subject (role/account)
     * @param csid of the object
     * @param subject
     * @return
     * @throws Exception
     */
    public void deleteAccountRole(String csid,
            SubjectType subject) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("deleteAccountRole with csid=" + csid);
        }
        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext((AccountRole) null, subject);
        getStorageClient(ctx).delete(ctx, csid);
    }

    /**
     * deleteAccountRole deletes given account-role relationships using given
     * csid of object (account/role) and subject (role/account)
     * @param csid of the object
     * @param subject
     * @param input with account role relationships to delete
     * @return
     * @throws Exception
     */
    public void deleteAccountRole(String csid, SubjectType subject, AccountRole input)
            throws Exception {

        ServiceContext<AccountRole, AccountRole> ctx = createServiceContext(input, subject);
        DocumentHandler handler = createDocumentHandler(ctx);
        getStorageClient(ctx).delete(ctx, csid, handler);
    }
}
