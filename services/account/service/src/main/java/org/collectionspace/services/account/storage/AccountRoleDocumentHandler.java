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
 *  See the License for the specific language governing accountRoles and
 *  limitations under the License.
 */
package org.collectionspace.services.account.storage;

import java.util.ArrayList;
import java.util.List;

//import org.collectionspace.services.authorization.AccountRolesList;
//import org.collectionspace.services.authorization.AccountRolesList.AccountRoleListItem;

import org.collectionspace.services.common.authorization_mgt.AuthorizationRoleRel;
import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;

import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.context.ServiceContextProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Document handler for AccountRole association.
 *
 * @author
 */
public class AccountRoleDocumentHandler
        extends JpaDocumentHandler<AccountRole, AccountRole, List<AccountRoleRel>, List<AccountRoleRel>> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(AccountRoleDocumentHandler.class);
    
    /** The account role. */
    private AccountRole accountRole;
//    private AccountRolesList accountRolesList;
    /** The account role csid. */
    private String accountRoleCsid = null;

    /**
     * Gets the account role csid.
     *
     * @return the account role csid
     */
    public String getAccountRoleCsid() {
    	return this.accountRoleCsid;
    }
    
    /**
     * Sets the account role csid.
     *
     * @param theAccountRoleCsid the new account role csid
     */
    public void setAccountRoleCsid(String theAccountRoleCsid) {
    	this.accountRoleCsid = theAccountRoleCsid;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeUpdate(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleGet(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleGet(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
    	AccountRole output = extractCommonPart(wrapDoc);
        setCommonPart(output);
        getServiceContext().setOutput(output);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleGetAll(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleGetAll(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#handleDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleDelete(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc, true);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public AccountRole extractCommonPartList(
            DocumentWrapper<List<AccountRoleRel>> wrapDoc)
            throws Exception {
    	
    	throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    	
//    	AccountRolesList result = new AccountRolesList();
//        List<AccountRoleRel> arrl = wrapDoc.getWrappedObject();
//        AccountRole ar = new AccountRole();
//        SubjectType subject = getSubject(getServiceContext());
//        if (arrl.size() == 0) {
//            return result;
//        }
//        
////        result.setSubject(subject);
//        AccountRoleRel ar0 = arrl.get(0);
//        AccountValue av = buildAccountValue(ar0);
////        result.setAccount(av);
//        
//    	List<AccountRoleListItem> accountRoleListItems = result.getAccountRoleListItems();
//    	if (accountRoleListItems == null) {
//    		accountRoleListItems = new ArrayList<AccountRoleListItem>();
//    	}
//    	for (AccountRoleRel e : arrl) {
//    		AccountRoleListItem accountRoleListItem = new AccountRoleListItem();
//    		// fill the item
//    		accountRoleListItem.setRoleName(e.getRoleName());
//    		accountRoleListItem.setRoleId(e.getRoleId());
//    		accountRoleListItem.setCsid(e.getHjid().toString());
//    		// add item to result list
//    		accountRoleListItems.add(accountRoleListItem);
//    	}
//    	
//        //
//    	// Old Sanjay code
//    	//
//    	
//        ar0 = arrl.get(0);
//        if (SubjectType.ROLE.equals(subject)) {
//
//            List<AccountValue> avs = new ArrayList<AccountValue>();
//            ar.setAccounts(avs);
//            av = buildAccountValue(ar0);
//            avs.add(av);
//
//            //add roles
//            List<RoleValue> rvs = new ArrayList<RoleValue>();
//            ar.setRoles(rvs);
//            for (AccountRoleRel arr : arrl) {
//                RoleValue rv = buildRoleValue(arr);
//                rvs.add(rv);
//            }
//        } else if (SubjectType.ACCOUNT.equals(subject)) {
//
//            List<RoleValue> rvs = new ArrayList<RoleValue>();
//            ar.setRoles(rvs);
//            RoleValue rv = buildRoleValue(ar0);
//            rvs.add(rv);
//
//            //add accounts
//            List<AccountValue> avs = new ArrayList<AccountValue>();
//            ar.setAccounts(avs);
//            for (AccountRoleRel arr : arrl) {
//                av = buildAccountValue(arr);
//                avs.add(av);
//            }
//        }
//        return result;
    }

    public void fillCommonPart(AccountRole ar,
    		DocumentWrapper<List<AccountRoleRel>> wrapDoc,
    		boolean handleDelete)
            	throws Exception {
        List<AccountRoleRel> arrl = wrapDoc.getWrappedObject();
        SubjectType subject = ar.getSubject();
        if (subject == null) {
            //it is not required to give subject as URI determines the subject
            subject = getSubject(getServiceContext());
        } else {
            //subject mismatch should have been checked during validation
        }
        if (subject.equals(SubjectType.ROLE)) {
            //FIXME: potential index out of bounds exception...negative test needed
            AccountValue av = ar.getAccount().get(0);

            for (RoleValue rv : ar.getRole()) {
                AccountRoleRel arr = buildAccountRoleRel(av, rv, handleDelete);
                arrl.add(arr);
            }
        } else if (SubjectType.ACCOUNT.equals(subject)) {
            //FIXME: potential index out of bounds exception...negative test needed
        	RoleValue rv = ar.getRole().get(0);
            for (AccountValue av : ar.getAccount()) {
                AccountRoleRel arr = buildAccountRoleRel(av, rv, handleDelete);
                arrl.add(arr);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(AccountRole ar,
    		DocumentWrapper<List<AccountRoleRel>> wrapDoc)
    			throws Exception {
    	fillCommonPart(ar, wrapDoc, false);
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public AccountRole extractCommonPart(
            DocumentWrapper<List<AccountRoleRel>> wrapDoc)
            throws Exception {
        List<AccountRoleRel> arrl = wrapDoc.getWrappedObject();
        AccountRole ar = new AccountRole();
        SubjectType subject = getSubject(getServiceContext());
        if (arrl.size() == 0) {
            return ar;
        }
        AccountRoleRel ar0 = arrl.get(0);
        if (SubjectType.ROLE.equals(subject)) {

            List<AccountValue> avs = new ArrayList<AccountValue>();
            ar.setAccount(avs);
            AccountValue av = AuthorizationRoleRel.buildAccountValue(ar0);
            if (av != null) {
            	avs.add(av);
            }

            //add roles
            List<RoleValue> rvs = new ArrayList<RoleValue>();
            ar.setRole(rvs);
            for (AccountRoleRel arr : arrl) {
            	RoleValue rv = AuthorizationRoleRel.buildRoleValue(arr);
            	if (rv != null) {
            		rvs.add(rv);
            	}
            }
        } else if (SubjectType.ACCOUNT.equals(subject)) {

            List<RoleValue> rvs = new ArrayList<RoleValue>();
            ar.setRole(rvs);
            RoleValue rv = AuthorizationRoleRel.buildRoleValue(ar0);
            rvs.add(rv);

            //add accounts
            List<AccountValue> avs = new ArrayList<AccountValue>();
            ar.setAccount(avs);
            for (AccountRoleRel arr : arrl) {
                AccountValue av = AuthorizationRoleRel.buildAccountValue(arr);
                avs.add(av);
            }
        }
        return ar;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getCommonPart()
     */
    @Override
    public AccountRole getCommonPart() {
        return accountRole;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#setCommonPart(java.lang.Object)
     */
    @Override
    public void setCommonPart(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getCommonPartList()
     */
    @Override
    public AccountRole getCommonPartList() {
        return accountRole;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(AccountRole theAccountRole) {
//        this.accountRolesList = accountRolesList;
    	this.accountRole = theAccountRole;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractDocumentHandlerImpl#createDocumentFilter()
     */
    @Override
    public DocumentFilter createDocumentFilter() {
        return new DocumentFilter(this.getServiceContext());
    }

    /**
     * Builds the account role rel.
     *
     * @param av the av
     * @param rv the rv
     * @return the account role rel
     */
    private AccountRoleRel buildAccountRoleRel(AccountValue av, RoleValue rv, boolean handleDelete) {
        AccountRoleRel arr = new AccountRoleRel();
        arr.setAccountId(av.getAccountId());
        arr.setUserId(av.getUserId());
        arr.setScreenName(av.getScreenName());
        arr.setRoleId(rv.getRoleId());
        arr.setRoleName(rv.getRoleName());
        
        String relationshipId = rv.getRoleRelationshipId();
        if (relationshipId != null && handleDelete == true) {
        	arr.setHjid(Long.parseLong(relationshipId));  // set this so we can convince JPA to del the relation
        }        
        return arr;
    }

    /**
     * Gets the subject.
     *
     * @param ctx the ctx
     * @return the subject
     */
    static SubjectType getSubject(ServiceContext ctx) {
        Object o = ctx.getProperty(ServiceContextProperties.SUBJECT);
        if (o == null) {
            throw new IllegalArgumentException(ServiceContextProperties.SUBJECT
                    + " property is missing in context "
                    + ctx.toString());
        }
        return (SubjectType) o;
    }
}
