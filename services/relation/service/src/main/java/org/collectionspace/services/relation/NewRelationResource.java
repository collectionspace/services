/**	
 * NewRelationResource.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision$
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright � 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.relation;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
//import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.security.UnauthorizedException;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class NewRelationResource.
 */
@Path("/relations")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class NewRelationResource extends
		AbstractMultiPartCollectionSpaceResourceImpl {

	/** The Constant serviceName. */
	public final static String serviceName = "relations";
	
	/** The logger. */
	final Logger logger = LoggerFactory.getLogger(NewRelationResource.class);

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.AbstractCollectionSpaceResource#getVersionString()
	 */
	@Override
	protected String getVersionString() {
		/** The last change revision. */
		final String lastChangeRevision = "$LastChangedRevision$";
		return lastChangeRevision;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.AbstractCollectionSpaceResource#getServiceName()
	 */
	@Override
	public String getServiceName() {
		return serviceName;
	}

	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
	 */
	@Override
    public Class<RelationsCommon> getCommonPartClass() {
    	return RelationsCommon.class;
    }
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.AbstractCollectionSpaceResource#createDocumentHandler(org.collectionspace.services.common.context.ServiceContext)
	 */
//	@Override
//	public DocumentHandler createDocumentHandler(ServiceContext<MultipartInput, MultipartOutput> ctx)
//			throws Exception {
//		DocumentHandler docHandler = ctx.getDocumentHandler();
//		if (ctx.getInput() != null) {
//			Object obj = ((MultipartServiceContext) ctx).getInputPart(ctx
//					.getCommonPartLabel(), RelationsCommon.class);
//			if (obj != null) {
//				docHandler.setCommonPart((RelationsCommon) obj);
//			}
//		}
//		return docHandler;
//	}

	/**
	 * Creates the relation.
	 * 
	 * @param input the input
	 * 
	 * @return the response
	 */
	@POST
	public Response createRelation(MultipartInput input) {
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(input);
			DocumentHandler handler = createDocumentHandler(ctx);
			String csid = getRepositoryClient(ctx).create(ctx, handler);
			UriBuilder path = UriBuilder
					.fromResource(NewRelationResource.class);
			path.path("" + csid);
			Response response = Response.created(path.build()).build();
			return response;
		} catch (UnauthorizedException ue) {
			Response response = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Create failed reason " + ue.getErrorReason())
					.type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in createRelation", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Create failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
	}
	
	/**
	 * Gets the relation.
	 * 
	 * @param csid the csid
	 * 
	 * @return the relation
	 */
	@GET
	@Path("{csid}")
	public MultipartOutput getRelation(@PathParam("csid") String csid) {
		if (logger.isDebugEnabled()) {
			logger.debug("getRelation with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("getRelation: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("get failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		MultipartOutput result = null;
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
			DocumentHandler handler = createDocumentHandler(ctx);
			getRepositoryClient(ctx).get(ctx, csid, handler);
			result = (MultipartOutput) ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Get failed reason " + ue.getErrorReason()).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("getRelation", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Get failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("getRelation", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed")
					.type("text/plain").build();
			throw new WebApplicationException(response);
		}

		if (result == null) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(
							"Get failed, the requested Relation CSID:" + csid
									+ ": was not found.").type("text/plain")
					.build();
			throw new WebApplicationException(response);
		}
		return result;
	}

	/*
	 * BEGIN OF GET LIST
	 */
	/**
	 * Gets the relation list.
	 * 
	 * @param ui the ui
	 * 
	 * @return the relation list
	 */
	@GET
	@Produces("application/xml")
	public RelationsCommonList getRelationList(@Context UriInfo ui) {
		return this.getRelationList(null, null, null);
	}

	/**
	 * Gets the relation list_ s.
	 * 
	 * @param ui the ui
	 * @param subjectCsid the subject csid
	 * 
	 * @return the relation list_ s
	 */
	@GET
	@Path("subject/{subjectCsid}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_S(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid) {
		return this.getRelationList(subjectCsid, null, null);
	}

	/**
	 * Gets the relation list_ p.
	 * 
	 * @param ui the ui
	 * @param predicate the predicate
	 * 
	 * @return the relation list_ p
	 */
	@GET
	@Path("type/{predicate}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_P(@Context UriInfo ui,
			@PathParam("predicate") String predicate) {
		return this.getRelationList(null, predicate, null);
	}

	/**
	 * Gets the relation list_ o.
	 * 
	 * @param ui the ui
	 * @param objectCsid the object csid
	 * 
	 * @return the relation list_ o
	 */
	@GET
	@Path("object/{objectCsid}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_O(@Context UriInfo ui,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationList(null, null, objectCsid);
	}

	/**
	 * Gets the relation list_ ps.
	 * 
	 * @param ui the ui
	 * @param predicate the predicate
	 * @param subjectCsid the subject csid
	 * 
	 * @return the relation list_ ps
	 */
	@GET
	@Path("type/{predicate}/subject/{subjectCsid}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_PS(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("subjectCsid") String subjectCsid) {
		return this.getRelationList(subjectCsid, predicate, null);
	}

	/**
	 * Gets the relation list_ sp.
	 * 
	 * @param ui the ui
	 * @param subjectCsid the subject csid
	 * @param predicate the predicate
	 * 
	 * @return the relation list_ sp
	 */
	@GET
	@Path("subject/{subjectCsid}/type/{predicate}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_SP(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate) {
		return this.getRelationList(subjectCsid, predicate, null);
	}

	/**
	 * Gets the relation list_ po.
	 * 
	 * @param ui the ui
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * 
	 * @return the relation list_ po
	 */
	@GET
	@Path("type/{predicate}/object/{objectCsid}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_PO(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationList(null, predicate, objectCsid);
	}

	/**
	 * Gets the relation list_ op.
	 * 
	 * @param ui the ui
	 * @param objectCsid the object csid
	 * @param predicate the predicate
	 * 
	 * @return the relation list_ op
	 */
	@GET
	@Path("object/{objectCsid}/type/{predicate}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_OP(@Context UriInfo ui,
			@PathParam("objectCsid") String objectCsid,
			@PathParam("predicate") String predicate) {
		return this.getRelationList(null, predicate, objectCsid);
	}

	/**
	 * Gets the relation list_ pso.
	 * 
	 * @param ui the ui
	 * @param predicate the predicate
	 * @param subjectCsid the subject csid
	 * @param objectCsid the object csid
	 * 
	 * @return the relation list_ pso
	 */
	@GET
	@Path("type/{predicate}/subject/{subjectCsid}/object/{objectCsid}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_PSO(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationList(subjectCsid, predicate, objectCsid);
	}

	/**
	 * Gets the relation list_ spo.
	 * 
	 * @param ui the ui
	 * @param subjectCsid the subject csid
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * 
	 * @return the relation list_ spo
	 */
	@GET
	@Path("subject/{subjectCsid}/type/{predicate}/object/{objectCsid}")
	@Produces("application/xml")
	public RelationsCommonList getRelationList_SPO(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationList(subjectCsid, predicate, objectCsid);
	}

	/*
	 * END OF GET LIST
	 */

	/**
	 * Update relation.
	 * 
	 * @param csid the csid
	 * @param theUpdate the the update
	 * 
	 * @return the multipart output
	 */
	@PUT
	@Path("{csid}")
	public MultipartOutput updateRelation(@PathParam("csid") String csid,
			MultipartInput theUpdate) {
		if (logger.isDebugEnabled()) {
			logger.debug("updateRelation with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("updateRelation: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("update failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		MultipartOutput result = null;
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext(theUpdate);
			DocumentHandler handler = createDocumentHandler(ctx);
			getRepositoryClient(ctx).update(ctx, csid, handler);
			result = (MultipartOutput) ctx.getOutput();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Update failed reason " + ue.getErrorReason())
					.type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caugth exception in updateRelation", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Update failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Update failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		return result;
	}

	/**
	 * Delete relation.
	 * 
	 * @param csid the csid
	 * 
	 * @return the response
	 */
	@DELETE
	@Path("{csid}")
	public Response deleteRelation(@PathParam("csid") String csid) {

		if (logger.isDebugEnabled()) {
			logger.debug("deleteRelation with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("deleteRelation: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("delete failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
			getRepositoryClient(ctx).delete(ctx, csid);
			return Response.status(HttpResponseCodes.SC_OK).build();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(Response.Status.UNAUTHORIZED)
					.entity("Delete failed reason " + ue.getErrorReason())
					.type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught exception in deleteRelation", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Delete failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Delete failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

	}

	/**
	 * Gets the relation list request.
	 * 
	 * @return the relation list request
	 * 
	 * @throws WebApplicationException
	 *             the web application exception
	 */
	public RelationsCommonList getRelationList(String subjectCsid,
			String predicate, String objectCsid) throws WebApplicationException {
		RelationsCommonList relationList = new RelationsCommonList();
		try {
			ServiceContext<MultipartInput, MultipartOutput> ctx = createServiceContext();
			DocumentHandler handler = createDocumentHandler(ctx);
			Map<String, Object> propsFromPath = handler.getProperties();
			propsFromPath.put(IRelationsManager.SUBJECT, subjectCsid);
			propsFromPath.put(IRelationsManager.PREDICATE, predicate);
			propsFromPath.put(IRelationsManager.OBJECT, objectCsid);
			getRepositoryClient(ctx).getAll(ctx, handler);
			relationList = (RelationsCommonList) handler.getCommonPartList();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(Response.Status.UNAUTHORIZED)
					.entity(
							"Get relations failed reason "
									+ ue.getErrorReason()).type("text/plain")
					.build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in getRelationList", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		return relationList;
	}
}
