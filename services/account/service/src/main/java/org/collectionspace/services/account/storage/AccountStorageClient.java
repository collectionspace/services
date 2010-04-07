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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.apache.commons.codec.binary.Base64;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.authentication.User;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountStorageClient deals with both Account and Default Identity Provider's
 * state in persistent storage
 * @author 
 */
public class AccountStorageClient extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(AccountStorageClient.class);

    public AccountStorageClient() {
    }

    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {

        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.CREATE);
            AccountsCommon account = (AccountsCommon) handler.getCommonPart();
            DocumentWrapper<AccountsCommon> wrapDoc =
                    new DocumentWrapperImpl<AccountsCommon>(account);
            handler.handle(Action.CREATE, wrapDoc);
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            //if userid and password are given, add to default id provider
            if (account.getUserId() != null && account.getPassword() != null) {
                User user = createUser(account);
                em.persist(user);
            }
//            if (account.getTenant() != null) {
//                UserTenant ut = createTenantAssoc(account);
//                em.persist(ut);
//            }
            account.setCreatedAtItem(new Date());
            em.persist(account);
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return (String) getValue(account, "getCsid");
        } catch (BadRequestException bre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw bre;
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (em != null) {
                releaseEntityManagerFactory(emf);
            }
        }
    }

    @Override
    public void update(ServiceContext ctx, String id, DocumentHandler handler)
            throws BadRequestException, DocumentNotFoundException,
            DocumentException {
        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "AccountStorageClient.update: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.UPDATE);
            AccountsCommon account = (AccountsCommon) handler.getCommonPart();
            DocumentWrapper<AccountsCommon> wrapDoc =
                    new DocumentWrapperImpl<AccountsCommon>(account);
            setCsid(account, id); //set id just in case it was not populated by consumer
            handler.handle(Action.UPDATE, wrapDoc);
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            AccountsCommon accountFound = getAccount(em, id);
            Date now = new Date();
            checkAllowedUpdates(account, accountFound);
            //if userid and password are given, add to default id provider
            if (account.getUserId() != null && hasPassword(account.getPassword())) {
                updateUser(em, account);
            }
            account = em.merge(account);
            account.setUpdatedAtItem(now);
            if (logger.isDebugEnabled()) {
                logger.debug("merged account=" + account.toString());
            }
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
                releaseEntityManagerFactory(emf);
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
        String docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = getEntityManagerFactory();
            em = emf.createEntityManager();
            //TODO investigate if deep delete is possible
            //query an delete is inefficient
            AccountsCommon accountFound = getAccount(em, id);

            StringBuilder accDelStr = new StringBuilder("DELETE FROM ");
            accDelStr.append(getEntityName(ctx));
            accDelStr.append(" WHERE csid = :csid");
            //TODO: add tenant id
            Query accDel = em.createQuery(accDelStr.toString());
            accDel.setParameter("csid", id);
            //TODO: add tenant id

            //if userid gives any indication about the id provider, it should
            //be used to avoid the following approach
            Query usrDel = null;
            User userLocal = getUser(em, accountFound);
            if (userLocal != null) {
                StringBuilder usrDelStr = new StringBuilder("DELETE FROM ");
                usrDelStr.append(User.class.getCanonicalName());
                usrDelStr.append(" WHERE username = :username");
                //TODO: add tenant id
                usrDel = em.createQuery(usrDelStr.toString());
                usrDel.setParameter("username", accountFound.getUserId());
            }
            em.getTransaction().begin();
//            int accDelCount = accDel.executeUpdate();
//            if (accDelCount != 1) {
//                if (em != null && em.getTransaction().isActive()) {
//                    em.getTransaction().rollback();
//                }
//            }
            if (userLocal != null) {
                int usrDelCount = usrDel.executeUpdate();
                if (usrDelCount != 1) {
                    if (em != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                }
                if (usrDelCount != 1) {
                    String msg = "could not find user with username=" + accountFound.getUserId();
                    logger.error(msg);
                    throw new DocumentNotFoundException(msg);
                }
            }
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
                releaseEntityManagerFactory(emf);
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
            String msg = "User id " + toAccount.getUserId() + " not found!";
            logger.error(msg);
            logger.debug(msg + " found userid=" + fromAccount.getUserId());
            throw new BadRequestException(msg);
        }
        return true;
    }

    private User createUser(AccountsCommon account) {
        User user = new User();
        user.setUsername(account.getUserId());
        if (hasPassword(account.getPassword())) {
            user.setPasswd(getEncPassword(account));
        }
        user.setCreatedAtItem(new Date());
        return user;
    }

    private User getUser(EntityManager em, AccountsCommon account) throws DocumentNotFoundException {
        User userFound = em.find(User.class, account.getUserId());
        if (userFound == null) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            String msg = "could not find user with id=" + account.getUserId();
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        return userFound;
    }

    private void updateUser(EntityManager em, AccountsCommon account) throws Exception {
        User userFound = getUser(em, account);
        if (userFound != null) {
            userFound.setPasswd(getEncPassword(account));
            userFound.setUpdatedAtItem(new Date());
            if (logger.isDebugEnabled()) {
                logger.debug("updated user=" + userFound.toString());
            }
            em.persist(userFound);
        }
    }

    private String getEncPassword(AccountsCommon account) {
        //jaxb unmarshaller already unmarshal xs:base64Binary, no need to b64 decode
        //byte[] bpass = Base64.decodeBase64(account.getPassword());
        SecurityUtils.validatePassword(new String(account.getPassword()));
        String secEncPasswd = SecurityUtils.createPasswordHash(
                account.getUserId(), new String(account.getPassword()));
        return secEncPasswd;
    }

    private boolean hasPassword(byte[] bpass) {
        return bpass != null && bpass.length > 0;
    }
//    private UserTenant createTenantAssoc(AccountsCommon account) {
//        UserTenant userTenant = new UserTenant();
//        userTenant.setUserId(account.getUserId());
//        List<AccountsCommon.Tenant> atl = account.getTenant();
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
