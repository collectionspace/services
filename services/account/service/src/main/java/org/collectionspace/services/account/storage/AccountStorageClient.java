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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.apache.commons.codec.binary.Base64;
import org.collectionspace.services.account.AccountsCommon;
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
import org.collectionspace.services.common.storage.jpa.JpaStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AccountStorageClient deals with both Account and Default Identity Provider's
 * state in persistent storage
 * @author 
 */
public class AccountStorageClient extends JpaStorageClient {

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
            account.setTenantid(ctx.getTenantId());
            em.persist(account);
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return (String) getValue(account, "getCsid");
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
            AccountsCommon accountFound = em.find(AccountsCommon.class, id);
            if (accountFound == null) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "could not find entity with id=" + id;
                logger.error(msg);
                throw new DocumentNotFoundException(msg);
            }

            StringBuilder accDelStr = new StringBuilder("DELETE FROM ");
            accDelStr.append(getEntityName(ctx));
            accDelStr.append(" WHERE csid = :csid");
            //TODO: add tenant id
            Query accDel = em.createQuery(accDelStr.toString());
            accDel.setParameter("csid", id);
            //TODO: add tenant id

            //if userid gives any indication about the id provider, it should
            //be used to avoid the following approach
            User userLocal = em.find(User.class, accountFound.getUserId());
            Query usrDel = null;
            if (userLocal != null) {
                StringBuilder usrDelStr = new StringBuilder("DELETE FROM ");
                usrDelStr.append(User.class.getCanonicalName());
                usrDelStr.append(" WHERE username = :username");
                //TODO: add tenant id
                usrDel = em.createQuery(usrDelStr.toString());
                usrDel.setParameter("username", accountFound.getUserId());
            }
            em.getTransaction().begin();
            int accDelCount = accDel.executeUpdate();
            if (accDelCount != 1) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
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
            em.getTransaction().commit();

        } catch (DocumentException de) {
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

    private User createUser(AccountsCommon account) {
        User user = new User();
        user.setUsername(account.getUserId());
        byte[] bpass = Base64.decodeBase64(account.getPassword());
        SecurityUtils.validatePassword(new String(bpass));
        String secEncPasswd = SecurityUtils.createPasswordHash(
                account.getUserId(), new String(bpass));
        user.setPasswd(secEncPasswd);
        return user;
    }
}
