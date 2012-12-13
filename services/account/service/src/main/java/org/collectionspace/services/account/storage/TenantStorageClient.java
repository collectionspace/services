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
import org.collectionspace.services.account.Tenant;
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
 * TenantStorageClient deals with both Account and CSIdP's
 * state in persistent storage. The rationale behind creating this class is that
 * this class manages persistence for both account and CSIP's user. Transactions
 * are used where possible to perform the persistence operations atomically.
 * @author 
 */
public class TenantStorageClient extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(TenantStorageClient.class);

    public TenantStorageClient() {
    }

    @Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {

    	/*
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "TenantStorageClient.create : ctx is missing");
        }
        */
        if (handler == null) {
            throw new IllegalArgumentException(
                    "TenantStorageClient.create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        Tenant tenant = (Tenant) handler.getCommonPart();
        try {
            handler.prepare(Action.CREATE);
            DocumentWrapper<Tenant> wrapDoc =
                    new DocumentWrapperImpl<Tenant>(tenant);
            handler.handle(Action.CREATE, wrapDoc);
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            tenant.setCreatedAtItem(new Date());
            em.persist(tenant);
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return (String) JaxbUtils.getValue(tenant, "getId");
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
            try {
            	if(em.find(Tenant.class, tenant.getId()) != null) {
            		//might be unique constraint violation
            		uniqueConstraint = true;
            	} 
            } catch(Exception ignored) {
            	//Ignore - we just care if exists
            }
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (uniqueConstraint) {
                String msg = "TenantId exists. Non unique tenantId=" + tenant.getId();
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
        /*
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "get: ctx is missing");
        } 
        */
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
            String whereClause = " where id = :id";
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);

            o = JpaStorageUtils.getEntity(
                    "org.collectionspace.services.account.Tenant", whereClause, params);
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
        /*
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "TenantStorageClient.update : ctx is missing");
        }
         */
        if (handler == null) {
            throw new IllegalArgumentException(
                    "TenantStorageClient.update: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.UPDATE);
            Tenant tenantReceived = (Tenant) handler.getCommonPart();
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            Tenant tenantFound = getTenant(em, id);
            checkAllowedUpdates(tenantReceived, tenantFound);
            DocumentWrapper<Tenant> wrapDoc =
                    new DocumentWrapperImpl<Tenant>(tenantFound);
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
        /*
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "TenantStorageClient.delete : ctx is missing");
        }
        */
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();

            Tenant tenantFound = getTenant(em, id);
            em.getTransaction().begin();
            em.remove(tenantFound);
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

    private Tenant getTenant(EntityManager em, String id) throws DocumentNotFoundException {
        Tenant tenantFound = em.find(Tenant.class, id);
        if (tenantFound == null) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            String msg = "could not find account with id=" + id;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        return tenantFound;
    }

    private boolean checkAllowedUpdates(Tenant toTenant, Tenant fromTenant) throws BadRequestException {
        if (!fromTenant.getId().equals(toTenant.getId())) {
            String msg = "Cannot change Tenant Id!";
            logger.error(msg);
            throw new BadRequestException(msg);
        }
        return true;
    }

}
