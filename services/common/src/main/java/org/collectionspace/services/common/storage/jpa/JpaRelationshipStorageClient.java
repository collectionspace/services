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
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.PermissionValue;
import org.collectionspace.services.authorization.Role;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.PermissionRoleRel;

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
public class JpaRelationshipStorageClient<T> extends JpaStorageClientImpl {

    private final Logger logger = LoggerFactory.getLogger(JpaRelationshipStorageClient.class);

    public static PermissionValue createPermissionValue(Permission permission) {
    	PermissionValue result = new PermissionValue();
    	result.setPermissionId(permission.getCsid());
    	result.setResourceName(permission.getResourceName());
    	result.setActionGroup(permission.getActionGroup());
    	return result;
    }
    
    public static RoleValue createRoleValue(Role role) {
    	RoleValue result = new RoleValue();
    	result.setRoleId(role.getCsid());
    	result.setRoleName(role.getRoleName());
    	return result;
    }
    
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

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "create : ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "create: handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.CREATE);
            List<T> rl = new ArrayList<T>();
            DocumentWrapper<List<T>> wrapDoc =
                    new DocumentWrapperImpl<List<T>>(rl);
            handler.handle(Action.CREATE, wrapDoc);
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            for (T r : rl) {
                JaxbUtils.setValue(r, "setCreatedAtItem", Date.class, new Date());
                em.persist(r);
            }
            em.getTransaction().commit();
            handler.complete(Action.CREATE, wrapDoc);
            return UUID.randomUUID().toString(); //filler, not useful
        } catch (BadRequestException bre) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw bre;
        } catch (PersistenceException pe) {
        	throw pe;
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
                JpaStorageUtils.releaseEntityManagerFactory(emf);
            }
        }
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
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "get: ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "get: handler is missing");
        }
        if (getObject(ctx, id) == null) {
            String msg = "get: "
                    + "could not find the object with id=" + id;
            logger.error(msg);
            throw new DocumentNotFoundException(msg);
        }
        String objectId = getObjectId(ctx);
        if (logger.isDebugEnabled()) {
            logger.debug("get: using objectId=" + objectId);
        }
        Class objectClass = getObjectClass(ctx);
        if (logger.isDebugEnabled()) {
            logger.debug("get: using object class=" + objectClass.getName());
        }
        DocumentFilter docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            docFilter = handler.createDocumentFilter();
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.GET);
            StringBuilder queryStrBldr = new StringBuilder("SELECT a FROM ");
            queryStrBldr.append(getEntityName(ctx));
            queryStrBldr.append(" a");

            queryStrBldr.append(" WHERE " + objectId + " = :objectId");
            String where = docFilter.getWhereClause();
            if ((null != where) && (where.length() > 0)) {
                queryStrBldr.append(" AND " + where);
            }
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            String queryStr = queryStrBldr.toString(); //for debugging
            if (logger.isDebugEnabled()) {
                logger.debug("get: jql=" + queryStr.toString());
            }
            Query q = em.createQuery(queryStr);
            q.setParameter("objectId", id);

            List<T> rl = new ArrayList<T>();
            try {
                //require transaction for get?
                em.getTransaction().begin();
                rl = q.getResultList();
                em.getTransaction().commit();
            } catch (NoResultException nre) {
                if (em != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                String msg = "get(1): "
                        + " could not find relationships for object class="
                        + objectClass.getName() + " id=" + id;
                if (logger.isDebugEnabled()) {
                    logger.debug(msg, nre);
                }
            }
            if (rl.size() == 0) {
                String msg = "get(2): "
                        + " could not find relationships for object class="
                        + objectClass.getName() + " id=" + id;
                if (logger.isDebugEnabled()) {
                    logger.debug(msg);
                }
            }
            DocumentWrapper<List<T>> wrapDoc =
                    new DocumentWrapperImpl<List<T>>(rl);
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
    private T getRelationship(EntityManager em, T relationship)
    		throws DocumentNotFoundException {
    	Long id = getId(relationship);
    	
        T relationshipFound = (T)em.find(relationship.getClass(), id);
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

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete : ctx is missing");
        }
        if (getObject(ctx, id) == null) {
            String msg = "delete : "
                    + "could not find the object with id=" + id;
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
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            StringBuilder deleteStr = new StringBuilder("DELETE FROM ");
            String entityName = getEntityName(ctx);
            deleteStr.append(entityName);
            deleteStr.append(" WHERE " + objectId + " = :objectId");
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            if (logger.isDebugEnabled()) {
                logger.debug("delete: jql=" + deleteStr.toString());
            }
            Query q = em.createQuery(deleteStr.toString());
            q.setParameter("objectId", id);
            int rcount = 0;
            em.getTransaction().begin();
            if (logger.isDebugEnabled() == true) {
            	logger.debug(q.toString());
            }
            rcount = q.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("deleted " + rcount + " relationships for entity " + entityName
                        + " with objectId=" + objectId);
            }
            em.getTransaction().commit();

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
    public void delete(ServiceContext ctx, String id, DocumentHandler handler)
            throws DocumentNotFoundException, DocumentException {

        if (ctx == null) {
            throw new IllegalArgumentException(
                    "delete : ctx is missing");
        }
        if (handler == null) {
            throw new IllegalArgumentException(
                    "delete : handler is missing");
        }
        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            handler.prepare(Action.DELETE);
            List<T> rl = new ArrayList<T>();
            DocumentWrapper<List<T>> wrapDoc =
                    new DocumentWrapperImpl<List<T>>(rl);
            handler.handle(Action.DELETE, wrapDoc);
            emf = JpaStorageUtils.getEntityManagerFactory();
            em = emf.createEntityManager();
            em.getTransaction().begin();
            //the following could be much more efficient if done with a single
            //sql/jql
            for (T r : rl) {
            	em.remove(getRelationship(em, r));
            }
            em.getTransaction().commit();
            handler.complete(Action.DELETE, wrapDoc);
        } catch (DocumentException de) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw de;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("delete(ctx, ix, handler): Caught exception ", e);
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

    /**
     * getObjectId returns the id of the object in a relationship
     * @param ctx
     * @return
     */
    protected String getObjectId(ServiceContext ctx) {
        String objectId = (String) ctx.getProperty(ServiceContextProperties.OBJECT_ID);
        if (objectId == null) {
            String msg = ServiceContextProperties.OBJECT_ID
                    + " property is missing in the context";
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
            String msg = ServiceContextProperties.OBJECT_CLASS
                    + " property is missing in the context";
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
        return JpaStorageUtils.getEntity(id, objectClass);
    }
}
