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
package org.collectionspace.services.account.storage;

import java.util.Date;
import java.util.HashMap;

import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.storage.csidp.UserStorageClient;
import org.collectionspace.services.authentication.User;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountStorageClient deals with both Account and CSIdP's
 * state in persistent storage. The rationale behind creating this class is that
 * this class manages pesistence for both account and CSIP's user. Transactions
 * are used where possible to permorme the persistence operations atomically.
 * @author 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AccountStorageClient extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(AccountStorageClient.class);
    private UserStorageClient userStorageClient = new UserStorageClient();

    public AccountStorageClient() {
    }

	@Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {
    	String result = null;
    	
    	AccountsCommon account = null;
    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();    	
        try {
            account = (AccountsCommon) handler.getCommonPart();
            handler.prepare(Action.CREATE);
            DocumentWrapper<AccountsCommon> wrapDoc = new DocumentWrapperImpl<AccountsCommon>(account);
            handler.handle(Action.CREATE, wrapDoc);
            jpaConnectionContext.beginTransaction();
            //
            // If userid and password are given, add to default ID provider -i.e., add it to the Spring Security account list
            //
            if (account.getUserId() != null && isForCSpaceIdentityProvider(account.getPassword())) {
                User user = userStorageClient.create(account.getUserId(), account.getPassword());
	            jpaConnectionContext.persist(user);
            }
            //
            // Now add the account to the CSpace list of accounts
            //
            account.setCreatedAtItem(new Date());
            jpaConnectionContext.persist(account);        	
            //
            // Finish creating related resources -e.g., account-role relationships
            //
            handler.complete(Action.CREATE, wrapDoc);
            jpaConnectionContext.commitTransaction();

            result = (String)JaxbUtils.getValue(account, "getCsid");
        } catch (BadRequestException bre) {
        	jpaConnectionContext.markForRollback();
            throw bre;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            boolean uniqueConstraint = false;
            if (userStorageClient.get(ctx, account.getUserId()) != null) {
                //might be unique constraint violation
                uniqueConstraint = true;
            }
            
        	jpaConnectionContext.markForRollback();

            if (uniqueConstraint) {
                String msg = "UserId exists. Non unique userId=" + account.getUserId();
                logger.error(msg);
                throw new BadRequestException(msg);
            }
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
        
        return result;
    }

    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }

        JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();
        try {
            handler.prepare(Action.GET);
            Object o = null;
            String whereClause = " JOIN a.tenants as at where csid = :csid and at.tenantId = :tenantId";
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("csid", id);
            params.put("tenantId", ctx.getTenantId());

            o = JpaStorageUtils.getEntity(jpaTransactionContext, 
                    "org.collectionspace.services.account.AccountsCommon", whereClause, params);
            if (null == o) {
                String msg = "could not find entity with id=" + id;
                throw new DocumentNotFoundException(msg);
            }
            DocumentWrapper<Object> wrapDoc = new DocumentWrapperImpl<Object>(o);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
        } catch (DocumentException de) {
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
        	ctx.closeConnection();
        }
        
    }

    @Override
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {

    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();        
        try {
            jpaConnectionContext.beginTransaction();

            handler.prepare(Action.UPDATE);
            AccountsCommon accountReceived = (AccountsCommon) handler.getCommonPart();
            AccountsCommon accountFound = getAccount(jpaConnectionContext, id);
            checkAllowedUpdates(accountReceived, accountFound);
            //if userid and password are given, add to default id provider
            // Note that this ignores the immutable flag, as we allow
            // password changes.
            if (accountReceived.getUserId() != null && isForCSpaceIdentityProvider(accountReceived.getPassword())) {
                userStorageClient.update(jpaConnectionContext,
                        accountReceived.getUserId(),
                        accountReceived.getPassword());
            }
            DocumentWrapper<AccountsCommon> wrapDoc =
            		new DocumentWrapperImpl<AccountsCommon>(accountFound);
            handler.handle(Action.UPDATE, wrapDoc);
            handler.complete(Action.UPDATE, wrapDoc); 
            jpaConnectionContext.commitTransaction();
        } catch (BadRequestException bre) {
        	jpaConnectionContext.markForRollback();
            throw bre;
        } catch (DocumentException de) {
        	jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
        	jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }

    @Override
    public void delete(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("deleting entity with id=" + id);
        }
        
    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();        
        try {
            AccountsCommon accountFound = getAccount(jpaConnectionContext, id);
            jpaConnectionContext.beginTransaction();
            //if userid gives any indication about the id provider, it should
            //be used to avoid  delete
            userStorageClient.delete(jpaConnectionContext, accountFound.getUserId());
            jpaConnectionContext.remove(accountFound);
            jpaConnectionContext.commitTransaction();
        } catch (DocumentException de) {
        	jpaConnectionContext.markForRollback();
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            jpaConnectionContext.markForRollback();
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }

    private AccountsCommon getAccount(JPATransactionContext jpaConnectionContext, String id) throws DocumentNotFoundException {
        AccountsCommon accountFound = (AccountsCommon) jpaConnectionContext.find(AccountsCommon.class, id);
        if (accountFound == null) {
            String msg = "could not find account with id=" + id;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        
        return accountFound;
    }

    private boolean checkAllowedUpdates(AccountsCommon toAccount, AccountsCommon fromAccount) throws BadRequestException {
        if (!fromAccount.getUserId().equals(toAccount.getUserId())) {
            String msg = "userId=" + toAccount.getUserId() + " of existing account does not match "
                    + "the userId=" + fromAccount.getUserId()
                    + " with csid=" + fromAccount.getCsid();
            logger.error(msg);
            if (logger.isDebugEnabled()) {
                logger.debug(msg + " found userid=" + fromAccount.getUserId());
            }
            throw new BadRequestException(msg);
        }
        return true;
    }

    /**
     * isForCSIdP deteremines if the create/update is also needed for CS IdP
     * @param bpass
     * @return
     */
    private boolean isForCSpaceIdentityProvider(byte[] bpass) {
        return bpass != null && bpass.length > 0;
    }
//    private UserTenant createTenantAssoc(AccountsCommon accountReceived) {
//        UserTenant userTenant = new UserTenant();
//        userTenant.setUserId(accountReceived.getUserId());
//        List<AccountsCommon.Tenant> atl = accountReceived.getTenant();
//        List<UserTenant.Tenant> utl =
//                new ArrayList<UserTenant.Tenant>();
//        for (AccountsCommon.Tenant at : atl) {
//            UserTenant.Tenant ut = new UserTenant.Tenant();
//            ut.setId(at.getId());
//            ut.setName(at.getName());
//            utl.add(ut);
//        }
//        userTenant.setTenant(utl);
//        return userTenant;
//    }
}
