/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
package org.collectionspace.services.common.storage.jpa;

import org.collectionspace.services.common.context.ServiceContextProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.common.api.Tools;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaRelationshipStorageClient deals with a relationship
 * in persistent storage. This storage client deals with bulk operations, i.e.
 * create/post inserts multiple tuples between the given object and subjects
 * get retrieves all subjects for the given object in relationship
 * delete deletes all subjects for the given object in relationship
 * @author 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class JpaRelationshipStorageClient<T> extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(JpaRelationshipStorageClient.class);
    
    public JpaRelationshipStorageClient() {
    	//empty
    }

    /**
     * create of a relationship creates one or more relationships between
     * permission and role
     * the object and subjects of the relationship is chosen (by doc handler) from
     * the payload
     * @param ctx
     * @param handler
     * @return
     * @throws BadRequestException
     * @throws DocumentException
     */
	@Override
    public String create(ServiceContext ctx,
            DocumentHandler handler) throws BadRequestException,
            DocumentException {
    	String result = null;
    	
    	JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();    	
        try {
            jpaTransactionContext.beginTransaction();
            handler.prepare(Action.CREATE);
            List<T> relationshipList = new ArrayList<T>();
            DocumentWrapper<List<T>> wrapDoc = new DocumentWrapperImpl<List<T>>(relationshipList);
            handler.handle(Action.CREATE, wrapDoc);
            for (T relationship : relationshipList) {
                JaxbUtils.setValue(relationship, "setCreatedAtItem", Date.class, new Date());
                jpaTransactionContext.persist(relationship);
            }
            handler.complete(Action.CREATE, wrapDoc); 
            jpaTransactionContext.commitTransaction();
            result = "0"; // meaningless result
        } catch (BadRequestException bre) {
            throw bre;
        } catch (PersistenceException pe) {
        	throw pe;
        } catch (DocumentException de) {
        	throw de;
        } catch (Exception e) {
            throw new DocumentException(e);
        } finally {
        	if (result == null) {
            	jpaTransactionContext.markForRollback();  // If result == null, we failed and must mark the current tx for rollback
        	}
            ctx.closeConnection();
        }
        
        return result;
    }

    /**
     * get retrieves all relationships for the object in the relationship
     * identified by the id. the object could be a permission or a role
     * @param ctx
     * @param id of the object in the relationship
     * @param handler
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
    @Override
    public void get(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {

    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();
        try {
	        if (getObject(ctx, id) == null) {
	            String msg = "get: " + "could not find the object with id=" + id;
	            logger.error(msg);
	            throw new DocumentNotFoundException(msg);
	        }
	        
	        String objectId = getObjectId(ctx);
	        DocumentFilter docFilter = handler.getDocumentFilter();
	        if (docFilter == null) {
	            docFilter = handler.createDocumentFilter();
	        }
        
            handler.prepare(Action.GET);
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(getEntityName(ctx));
            queryStrBldr.append(" a");
            
            String joinFetch = docFilter.getJoinFetchClause();
            if (Tools.notBlank(joinFetch)) {
                queryStrBldr.append(" " + joinFetch);
            }

            queryStrBldr.append(" WHERE " + objectId + " = :objectId");
            String where = docFilter.getWhereClause();
            if ((null != where) && (where.length() > 0)) {
                queryStrBldr.append(" AND " + where);
            }

            String queryStr = queryStrBldr.toString(); //for debugging
            if (logger.isDebugEnabled()) {
                logger.debug("get: jql=" + queryStr.toString());
            }
            Query q = jpaConnectionContext.createQuery(queryStr);
            q.setParameter("objectId", id);

            List<T> relList = new ArrayList<T>();
        	jpaConnectionContext.beginTransaction();
            try {
                relList = q.getResultList();
            } catch (NoResultException nre) {
            	// Quietly consume.  relList will just be an empty list
            }

            DocumentWrapper<List<T>> wrapDoc = new DocumentWrapperImpl<List<T>>(relList);
            handler.handle(Action.GET, wrapDoc);
            handler.complete(Action.GET, wrapDoc);
            jpaConnectionContext.commitTransaction();
        } catch (DocumentNotFoundException nfe) {
        	jpaConnectionContext.markForRollback();
        	throw nfe;
        } catch (DocumentException de) {
        	jpaConnectionContext.markForRollback();
        	throw de;
        } catch (Exception e) {
        	jpaConnectionContext.markForRollback();
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }
    
    /**
     * Gets the id.
     *
     * @param relationship the relationship
     * @return the id
     */
    private Long getId(T relationship) {
    	Long result = null;
    	
    	if (relationship != null) {
	    	if (relationship instanceof AccountRoleRel) {
	    		AccountRoleRel accountRoleRel = (AccountRoleRel)relationship;
	    		result = accountRoleRel.getHjid();
	    	} else if (relationship instanceof PermissionRoleRel) {
	    		PermissionRoleRel permissionRoleRel = (PermissionRoleRel)relationship;
	    		result = permissionRoleRel.getHjid();
	    	}
    	}
    	
    	return result;
    }
    
    /**
     * Gets the relationship.
     *
     * @param em the em
     * @param relationship the relationship
     * @return the relationship
     * @throws DocumentNotFoundException the document not found exception
     */
    private T getRelationship(JPATransactionContext jpaTransactionContext, T relationship)
    		throws DocumentNotFoundException {
    	Long id = getId(relationship);
    	
        T relationshipFound = (T)jpaTransactionContext.find(relationship.getClass(), id);
        if (relationshipFound == null) {
            String msg = "Could not find relationship with id=" + id;
            if (logger.isErrorEnabled() == true) {
            	logger.error(msg);
            }
            throw new DocumentNotFoundException(msg);
        }
        
        return relationshipFound;
    }

    /**
     * delete removes all the relationships for the object in the relationship
     * identified by the id. the object could be a permission or a role
     * @param ctx
     * @param id of the object in the relationship
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
    @Override
    public void delete(ServiceContext ctx, String id)
            throws DocumentNotFoundException,
            DocumentException {

        if (getObject(ctx, id) == null) {
            String msg = "delete : " + "could not find the object with id=" + id;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        
        String objectId = getObjectId(ctx);
        if (logger.isDebugEnabled()) {
            logger.debug("delete: using objectId=" + objectId);
        }
        Class objectClass = getObjectClass(ctx);
        if (logger.isDebugEnabled()) {
            logger.debug("delete: using object class=" + objectClass.getName());
        }
        
    	JPATransactionContext jpaConnectionContext = (JPATransactionContext)ctx.openConnection();    	
        try {
            StringBuilder deleteStr = new StringBuilder("DELETE FROM ");
            String entityName = getEntityName(ctx);
            deleteStr.append(entityName);
            deleteStr.append(" WHERE " + objectId + " = :objectId");
            if (logger.isDebugEnabled()) {
                logger.debug("delete: jql=" + deleteStr.toString());
            }
            Query q = jpaConnectionContext.createQuery(deleteStr.toString());
            q.setParameter("objectId", id);
            int rcount = 0;
            jpaConnectionContext.beginTransaction();
            if (logger.isDebugEnabled() == true) {
            	logger.debug(q.toString());
            }
            rcount = q.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("deleted " + rcount + " relationships for entity " + entityName
                        + " with objectId=" + objectId);
            }
            jpaConnectionContext.commitTransaction();
        } catch (Exception e) {
        	jpaConnectionContext.markForRollback();
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
            ctx.closeConnection();
        }
    }

    /**
     * delete of a relationship deletes one or more relationships between
     * permission and role
     * the object and subjects of the relationship is chosen (by doc handler) from
     * the payload
     * @param ctx
     * @param handler
     * @return
     * @throws DocumentNotFoundException
     * @throws DocumentException
     */
    @Override
    public boolean delete(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {
    	boolean result = true;
    	
    	JPATransactionContext jpaTransactionContext = (JPATransactionContext)ctx.openConnection();    	
        try {
        	jpaTransactionContext.beginTransaction();
            handler.prepare(Action.DELETE);
            List<T> relationshipList = new ArrayList<T>();
            DocumentWrapper<List<T>> wrapDoc = new DocumentWrapperImpl<List<T>>(relationshipList);
            handler.handle(Action.DELETE, wrapDoc);
            //
            //the following could be much more efficient if done with a single sql/jql
            //
            for (T relationship : relationshipList) {
            	jpaTransactionContext.remove(getRelationship(jpaTransactionContext, relationship));
            }
            handler.complete(Action.DELETE, wrapDoc); // Delete from the Spring Security tables.  Would be better if this was part of the earlier transaction.
            jpaTransactionContext.commitTransaction();
        } catch (DocumentException de) {
        	jpaTransactionContext.markForRollback();
            throw de;
        } catch (Exception e) {
        	jpaTransactionContext.markForRollback();
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, ix, handler): Caught exception ", e);
            }
            throw new DocumentException(e);
        } finally {
        	ctx.closeConnection();
        }
        
        return result;
    }

    /**
     * getObjectId returns the id of the object in a relationship
     * @param ctx
     * @return
     */
    protected String getObjectId(ServiceContext ctx) {
        String objectId = (String) ctx.getProperty(ServiceContextProperties.OBJECT_ID);
        
        if (objectId == null) {
            String msg = ServiceContextProperties.OBJECT_ID + " property is missing in the context";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return objectId;
    }

    /**
     * getObjectClass returns the class of the object in a relationship
     * @param ctx
     * @return
     */
    protected Class getObjectClass(ServiceContext ctx) {
        Class objectClass = (Class) ctx.getProperty(ServiceContextProperties.OBJECT_CLASS);
        
        if (objectClass == null) {
            String msg = ServiceContextProperties.OBJECT_CLASS + " property is missing in the context";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        return objectClass;
    }

    /**
     * getObject returns the object in the relationship
     * @param ctx
     * @param id
     * @return
     */
    protected Object getObject(ServiceContext ctx, String id)
    		throws DocumentNotFoundException {
        Class objectClass = getObjectClass(ctx);
        return JpaStorageUtils.getEntity((JPATransactionContext)ctx.getCurrentTransactionContext(), id, objectClass);
    }
}
