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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
public class AccountStorageClient extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(AccountStorageClient.class);
    private UserStorageClient userStorageClient = new UserStorageClient();

    public AccountStorageClient() {
    }

    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.create : ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        AccountsCommon account = (AccountsCommon) handler.getCommonPart();
        try {
            handler.prepare(Action.CREATE);
            DocumentWrapper<AccountsCommon> wrapDoc =
                    new DocumentWrapperImpl<AccountsCommon>(account);
            handler.handle(Action.CREATE, wrapDoc);
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            //if userid and password are given, add to default id provider
            if (account.getUserId() != null &&
                    isForCSIdP(account.getPassword())) {
                User user = userStorageClient.create(account.getUserId(),
                        account.getPassword());
                em.persist(user);
            }
//            if (accountReceived.getTenant() != null) {
//                UserTenant ut = createTenantAssoc(accountReceived);
//                em.persist(ut);
//            }
            account.setCreatedAtItem(new Date());
            em.persist(account);
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return (String) JaxbUtils.getValue(account, "getCsid");
        } catch (BadRequestException bre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw bre;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            boolean uniqueConstraint = false;
            if (userStorageClient.get(em, account.getUserId()) != null) {
                //might be unique constraint violation
                uniqueConstraint = true;
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (uniqueConstraint) {
                String msg = "UserId exists. Non unique userId=" + account.getUserId();
                logger.error(msg);
                throw new BadRequestException(msg);
            }
            throw new DocumentException(e);
        } finally {
            if (em != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

        @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "get: ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "get: handler is missing");
        }
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET);
            Object o = null;
            String whereClause = " JOIN a.tenants as at where csid = :csid and at.tenantId = :tenantId";
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("csid", id);
            params.put("tenantId", ctx.getTenantId());

            o = JpaStorageUtils.getEntity(
                    "org.collectionspace.services.account.AccountsCommon", whereClause, params);
            if (null == o) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
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
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    @Override
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.update : ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.update: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.UPDATE);
            AccountsCommon accountReceived = (AccountsCommon) handler.getCommonPart();
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            AccountsCommon accountFound = getAccount(em, id);
            checkAllowedUpdates(accountReceived, accountFound);
            //if userid and password are given, add to default id provider
            // Note that this ignores the immutable flag, as we allow
            // password changes.
            if (accountReceived.getUserId() != null
                    && isForCSIdP(accountReceived.getPassword())) {
                userStorageClient.update(em,
                        accountReceived.getUserId(),
                        accountReceived.getPassword());
            }
            DocumentWrapper<AccountsCommon> wrapDoc =
                    new DocumentWrapperImpl<AccountsCommon>(accountFound);
            handler.handle(Action.UPDATE, wrapDoc);
            em.getTransaction().commit();
            handler.complete(Action.UPDATE, wrapDoc);
        } catch (BadRequestException bre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw bre;
        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    @Override
    public void delete(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("deleting entity with id=" + id);
        }
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.delete : ctx is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();

            AccountsCommon accountFound = getAccount(em, id);
            em.getTransaction().begin();
            //if userid gives any indication about the id provider, it should
            //be used to avoid  delete
            userStorageClient.delete(em, accountFound.getUserId());
            em.remove(accountFound);
            em.getTransaction().commit();

        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new DocumentException(e);
        } finally {
            if (emf != null) {
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
    }

    private AccountsCommon getAccount(EntityManager em, String id) throws DocumentNotFoundException {
        AccountsCommon accountFound = em.find(AccountsCommon.class, id);
        if (accountFound == null) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
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
    private boolean isForCSIdP(byte[] bpass) {
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
