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
package org.collectionspace.services.authorization;

import org.collectionspace.services.authorization.PermissionRole;
import org.collectionspace.services.authorization.PermissionRoleRel;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.authorization.perms.Permission;
import org.collectionspace.services.authorization.perms.PermissionAction;
import org.collectionspace.services.authorization.perms.PermissionsList;
import org.collectionspace.services.authorization.storage.PermissionDocumentHandler;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PermissionClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.SecurityResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorization_mgt.PermissionRoleUtil;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.TransactionContext;
import org.collectionspace.services.common.storage.jpa.JPATransactionContext;
import org.collectionspace.services.common.storage.jpa.JpaStorageClientImpl;

import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path(PermissionClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class PermissionResource extends SecurityResourceBase<Permission, Permission> {

    final Logger logger = LoggerFactory.getLogger(PermissionResource.class);
    final StorageClient storageClient = new JpaStorageClientImpl();

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision: 1165 $";
    }

    @Override
    public String getServiceName() {
        return  PermissionClient.SERVICE_NAME;
    }

    @Override
    public Class<Permission> getCommonPartClass() {
        return Permission.class;
    }

    @SuppressWarnings("unchecked")
	@Override
    public ServiceContextFactory<Permission, Permission> getServiceContextFactory() {
        return RemoteServiceContextFactory.get();
    }

    @Override
    public StorageClient getStorageClient(ServiceContext<Permission, Permission> ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    @POST
    public Response createPermission(Permission input) {
        return create(input);
    }
    
    public Response createPermission(JPATransactionContext jpaTransactionContext, Permission input) {
        return create(jpaTransactionContext, input);
    }
    
    public Permission createPermissionFromInstance(JPATransactionContext jpaTransactionContext, Permission input) {
    	Permission result = null;
    	
    	String permCsid = null;
    	Response response = createPermission(jpaTransactionContext, input);
    	if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
    		permCsid = CollectionSpaceClientUtils.extractId(response);
        	result = (Permission)get(jpaTransactionContext, permCsid, Permission.class); // return the result of a full lookup of what we just persisted
    	}
    	    	
    	return result;
    }

    @GET
    @Path("{csid}")
    public Permission getPermission(@PathParam("csid") String csid) {
        return (Permission)get(csid, Permission.class);
    }

    @GET
    @Produces("application/xml")
    public PermissionsList getPermissionList(@Context UriInfo ui) {
    	PermissionsList result = (PermissionsList)getList(ui, Permission.class);
    	if(logger.isTraceEnabled()) {
        	PayloadOutputPart ppo = new PayloadOutputPart(PermissionsList.class.getName(), result);
    		System.out.println(ppo.asXML());
    	}
    	
    	return result;
    }

    /**
     * Updates a permission by first deleting it and all of it's relationships (with roles, and perm-actions) and then
     * recreating it.  Unfortunately, we can't seem to be able to just update the perm-actions because of an issue with JPA.
     * 
     * @param ui
     * @param csid
     * @param theUpdate
     * @return
     * @throws Exception
     */
    @PUT
    @Path("{csid}")
    synchronized public Permission updatePermission(@Context UriInfo ui, @PathParam("csid") String csid, Permission theUpdate) throws Exception {
    	Permission result = null;
        ensureCSID(csid, ServiceMessages.UPDATE_FAILED + "permission ");
        
		ServiceContext<Permission, Permission> ctx = createServiceContext(null, Permission.class);
        PermissionDocumentHandler docHandler = (PermissionDocumentHandler) createDocumentHandler(ctx);

        TransactionContext transactionContext = ctx.openConnection();
        try {
        	transactionContext.beginTransaction();
        	//
        	// Get a copy of the currently persisted resource
        	//
        	Permission original = (Permission) get(transactionContext, csid, Permission.class);
        	if (original == null) {
        		throw new DocumentNotFoundException(String.format("The Permission resource CSID=%s could not be found.", csid));
        	} else if (isImmutable(original) == true) {
				String msg = String.format("Permission resource CSID=%s is immutable and cannot be updated.", csid);
				throw new DocumentException(msg);
        	}

        	Permission perm = copyForUpdate(original);  // If we upgrade to JPA 2.0, we could just "detach" the original instead of needing to copy it.
        	
        	//
        	// Before we start the update process, verify the payload is valid
        	//
        	ctx.setInput(theUpdate);
        	docHandler.prepare(Action.UPDATE);
        	
        	//
        	// Get a copy of the permission-role relationships
        	//
        	PermissionRole permRole = getPermissionRole(ctx, csid);

        	//
        	// Delete the Permission resource (and related perm-actions, and perm-roles) from storage and remove from current JPA context
        	//
            Response deletedRes = deletePermission(ctx, csid);
            if (deletedRes.getStatus() != Response.Status.OK.getStatusCode()) {
        		throw new DocumentException(String.format("Could not update Permission resource CSID=%s", csid));
            }
            
            //
            // Merge the "update" payload with the corresponding Permission resource payload
            //
            perm = docHandler.merge(perm, theUpdate);
            
        	//
        	// Recreate the Permission resource (and related perm-actions) using the same CSID and updated Permission object
        	//
        	ctx.setInput(perm);
        	ctx.setProperty(PermissionClient.PERMISSION_UPDATE_CSID, csid);
        	Response res = create(ctx, perm);
        	if (res.getStatus() != Response.Status.CREATED.getStatusCode()) {
        		throw new DocumentException(String.format("Could not update Permission resource CSID=%s", csid));
        	}
        	
        	//
        	// Recreate the permission-role relationships
        	//
        	if (PermissionRoleUtil.isEmpty(permRole) == false) {
	        	Response permRoleRes = createPermissionRole(ctx, csid, permRole);
	            if (permRoleRes.getStatus() != Response.Status.CREATED.getStatusCode()) {
	        		throw new DocumentException(String.format("Could not update Permission resource CSID=%s", csid));
	            }
        	}
            
            transactionContext.commitTransaction();
            result = perm;
        } catch (Exception e) {
        	transactionContext.markForRollback();
            throw bigReThrow(e, ServiceMessages.UPDATE_FAILED, csid);
        } finally {
        	if (result == null) {
        		//
        		// 
        		//
        	}
        	ctx.closeConnection();
        }
        
        return result;
    }

    /**
     * Return true if the permission is immutable.
     * 
     * @param original
     * @return
     */
	private boolean isImmutable(Permission original) {
		boolean result = false;

		if ((!Tools.isEmpty(original.getMetadataProtection()) && original.getMetadataProtection().equals(PermissionClient.IMMUTABLE))
				|| (!Tools.isEmpty(original.getActionsProtection())	&& original.getActionsProtection().equals(PermissionClient.IMMUTABLE))) {
			result = true;
		}

		return result;
	}

	private Permission copyForUpdate(Permission theOriginal) throws DocumentException {
		Permission result = null;
		
		if (theOriginal != null) {
			result = new Permission();
			result.setAttributeName(theOriginal.getAttributeName());
			result.setDescription(theOriginal.getDescription());
			result.setEffect(theOriginal.getEffect());
			result.setResourceName(theOriginal.getResourceName());
			result.setTenantId(theOriginal.getTenantId());
			result.setActionGroup(theOriginal.getActionGroup());
			
			for (PermissionAction permissionAction : theOriginal.getAction()) {
				result.getAction().add(copyForUpdate(permissionAction));
			}
		}
		
		return result;
	}

	private PermissionAction copyForUpdate(PermissionAction permissionAction) {
		PermissionAction result = new PermissionAction();
		
		result.setName(permissionAction.getName());
		result.setObjectIdentity(permissionAction.getObjectIdentity());
		result.setObjectIdentityResource(permissionAction.getObjectIdentityResource());
		
		return result;
	}

	/**
     * Deletes the Permission resource and its relationship(s) with any role(s).  Does not delete the actual low-level permission-action tuples.
     * See https://issues.collectionspace.org/browse/DRYD-223
     * 
     * @param csid
     * @return
     * @throws Exception
     */
	@DELETE
    @Path("{csid}")
    public Response deletePermission(@PathParam("csid") String csid) throws Exception {
        logger.debug("deletePermission with csid=" + csid);
        ensureCSID(csid, ServiceMessages.DELETE_FAILED + "permission ");

		ServiceContext<Permission, Permission> ctx = createServiceContext((Permission) null, Permission.class);
		return deletePermission(ctx, csid);
    }

    synchronized public Response deletePermission(ServiceContext<Permission, Permission> ctx, String csid) throws Exception {
        DocumentHandler docHandler = createDocumentHandler(ctx);

        TransactionContext transactionContext = ctx.openConnection();
        try {
        	transactionContext.beginTransaction();
        	//
        	// First, delete the relationships between the Permission resource and any Role resources.
        	//
        	try {
	            PermissionRoleSubResource subResource =
	                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
	            subResource.deletePermissionRole(ctx, csid, SubjectType.ROLE);
        	} catch (DocumentNotFoundException dnf) {
        		// ignore, just means we didn't find any relationships to delete
        	}
            //
            // Lastly, delete the Permission resource itself and commit the transaction
            //
            getStorageClient(ctx).delete(ctx, csid, docHandler);
            transactionContext.commitTransaction();
        } catch (Exception e) {
        	transactionContext.markForRollback();
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        } finally {
        	ctx.closeConnection();
        }
        
        return Response.status(HttpResponseCodes.SC_OK).build();
    }
    
	@POST
    @Path("{csid}/permroles")
    public Response createPermissionRole(
    		@QueryParam("_method") String method,
            @PathParam("csid") String permCsid,
			PermissionRole input) {
		if (method != null) {
			if ("delete".equalsIgnoreCase(method)) { // FIXME: How could 'method' ever equal "delete"
				return deletePermissionRole(permCsid, input);
			}
		}
		logger.debug("createPermissionRole with permCsid=" + permCsid);
		ensureCSID(permCsid, ServiceMessages.POST_FAILED + "permroles permission ");
				
		return createPermissionRole((ServiceContext<Permission, Permission>)null, permCsid, input);
	}
	
    protected Response createPermissionRole(
    		ServiceContext<Permission, Permission> ctx,
    		String permCsid,
			PermissionRole input) {				
		try {
			PermissionRoleSubResource subResource = new PermissionRoleSubResource(
					PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
			String permrolecsid = subResource.createPermissionRole(ctx, input, SubjectType.ROLE);
			UriBuilder path = UriBuilder.fromResource(PermissionResource.class);
			path.path(permCsid + "/permroles/" + permrolecsid);
			Response response = Response.created(path.build()).build();
			return response;
		} catch (Exception e) {
			throw bigReThrow(e, ServiceMessages.POST_FAILED, permCsid);
		}
	}	

	@GET
    @Path("{csid}/permroles/{id}")
    public PermissionRoleRel getPermissionRole(
            @PathParam("csid") String permCsid,
            @PathParam("id") String permrolecsid) {
        logger.debug("getPermissionRole with permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.GET_FAILED + "permroles permission ");
        PermissionRoleRel result = null;
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //get relationships for a permission
            result = subResource.getPermissionRoleRel((ServiceContext<Permission, Permission>)null, permCsid, SubjectType.ROLE, permrolecsid);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, permCsid);
        }
        checkResult(result, permCsid, ServiceMessages.GET_FAILED);
        return result;
    }

	@GET
    @Path("{csid}/permroles")
    public PermissionRole getPermissionRole(@PathParam("csid") String permCsid) {
        logger.debug("getPermissionRole with permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.GET_FAILED + "permroles permission ");

        PermissionRole result = getPermissionRole((ServiceContext<Permission, Permission>)null, permCsid);
        
        if (PermissionRoleUtil.isEmpty(result)) {
        	String msg = String.format("Could not find any permission-role relationships for Permission resource CSID=%s", permCsid);
            Response response = Response.status(Response.Status.NOT_FOUND).entity(msg).type("text/plain").build();
            throw new CSWebApplicationException(response);
        }
        
        return result;
    }
	
    private PermissionRole getPermissionRole(ServiceContext<Permission, Permission> ctx, String permCsid) {
        ensureCSID(permCsid, ServiceMessages.GET_FAILED + "permroles permission ");
        PermissionRole result = null;

        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            result = subResource.getPermissionRole(ctx, permCsid, SubjectType.ROLE);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, permCsid);
        }
        
        return result;
    }	
	
    private Response deletePermissionRole(String permCsid, PermissionRole input) {
        logger.debug("Delete payload of permrole relationships with permission permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.DELETE_FAILED + "permroles permission ");
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //
            // Delete all role relationships for a permission
            //
            subResource.deletePermissionRole((ServiceContext<Permission, Permission>)null, permCsid, SubjectType.ROLE, input);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, permCsid);
        }
    }

    @DELETE
    @Path("{csid}/permroles")    
    public Response deletePermissionRole(
            @PathParam("csid") String permCsid) {
        logger.debug("Delete all the role relationships of the permissions with permCsid=" + permCsid);
        ensureCSID(permCsid, ServiceMessages.DELETE_FAILED + "permroles permission ");
         
        try {
            PermissionRoleSubResource subResource =
                    new PermissionRoleSubResource(PermissionRoleSubResource.PERMISSION_PERMROLE_SERVICE);
            //delete all relationships for a permission
            subResource.deletePermissionRole((ServiceContext<Permission, Permission>)null, permCsid, SubjectType.ROLE);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, permCsid);
        }
    }
    
}
