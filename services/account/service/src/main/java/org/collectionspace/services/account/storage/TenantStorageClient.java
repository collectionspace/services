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
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
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
@SuppressWarnings({"rawtypes", "unchecked"})
public class TenantStorageClient extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(TenantStorageClient.class);

    public TenantStorageClient() {
    }

	@Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {
        String result = null;
		
    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();    	
        Tenant tenant = (Tenant) handler.getCommonPart();
        try {
            handler.prepare(Action.CREATE);
            DocumentWrapper<Tenant> wrapDoc =
                    new DocumentWrapperImpl<Tenant>(tenant);
            handler.handle(Action.CREATE, wrapDoc);
            jpaConnectionContext.beginTransaction();
            tenant.setCreatedAtItem(new Date());
            jpaConnectionContext.persist(tenant);
            handler.complete(Action.CREATE, wrapDoc);
            jpaConnectionContext.commitTransaction();
            
            result = (String)JaxbUtils.getValue(tenant, "getId");
        } catch (BadRequestException bre) {
        	jpaConnectionContext.markForRollback();
            throw bre;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            boolean uniqueConstraint = false;
            try {
            	if(jpaConnectionContext.find(Tenant.class, tenant.getId()) != null) {
            		//might be unique constraint violation
            		uniqueConstraint = true;
            	} 
            } catch(Exception ignored) {
            	//Ignore - we just care if exists
            }
        	jpaConnectionContext.markForRollback();
            if (uniqueConstraint) {
                String msg = "TenantId exists. Non unique tenantId=" + tenant.getId();
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

    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();    	
        try {
            handler.prepare(Action.GET);
            Object o = null;
            String whereClause = " where id = :id";
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);

            o = JpaStorageUtils.getEntity(
                    "org.collectionspace.services.account.Tenant", whereClause, params);
            if (null == o) {
                String msg = "Could not find entity with id=" + id;
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
            handler.prepare(Action.UPDATE);
            
            Tenant tenantReceived = (Tenant) handler.getCommonPart();
            jpaConnectionContext.beginTransaction();
            Tenant tenantFound = getTenant(jpaConnectionContext, id);
            checkAllowedUpdates(tenantReceived, tenantFound);
            DocumentWrapper<Tenant> wrapDoc =
                    new DocumentWrapperImpl<Tenant>(tenantFound);
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
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
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
            Tenant tenantFound = getTenant(jpaConnectionContext, id);
            jpaConnectionContext.beginTransaction();
            jpaConnectionContext.remove(tenantFound);
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

    private Tenant getTenant(JPATransactionContext jpaConnectionContext, String id) throws DocumentNotFoundException {
        Tenant tenantFound = (Tenant) jpaConnectionContext.find(Tenant.class, id);
        if (tenantFound == null) {
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
