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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.relation.nuxeo.RelationsUtils;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
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
@Consumes("application/xml")
@Produces("application/xml")
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

	/**
	 * Creates the relation.
	 * 
	 * @param input the input
	 * 
	 * @return the response
	 */
	@POST
	public Response createRelation(String xmlText) {
		try {
        	        PoxPayloadIn input = new PoxPayloadIn(xmlText);
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(input);
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
		} catch (BadRequestException bre) {
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("Create failed reason " + bre.getErrorReason())
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
	 * @param ui the ui
	 * @param csid the csid
	 * @return the relation
	 */
	@GET
	@Path("{csid}")
	public byte[] getRelation(@Context UriInfo ui,
			@PathParam("csid") String csid) {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
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
		PoxPayloadOut result = null;
		try {
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
			DocumentHandler handler = createDocumentHandler(ctx);
			getRepositoryClient(ctx).get(ctx, csid, handler);
			result = ctx.getOutput();
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
		return result.getBytes();
	}

	/**
	 * Gets the relation list.
	 * 
	 * @param ui the ui
	 * @param subjectCsid 
	 * @param predicate 
	 * @param objectCsid 
	 * 
	 * @return the relation list
	 */
	@GET
	@Produces("application/xml")
	public RelationsCommonList getRelationList(@Context UriInfo ui,
			@QueryParam(IRelationsManager.SUBJECT_QP) String subjectCsid,
			@QueryParam(IRelationsManager.SUBJECT_TYPE_QP) String subjectType,
			@QueryParam(IRelationsManager.PREDICATE_QP) String predicate,
			@QueryParam(IRelationsManager.OBJECT_QP) String objectCsid,
			@QueryParam(IRelationsManager.OBJECT_TYPE_QP) String objectType) {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		return this.getRelationList(queryParams, subjectCsid, subjectType,
				predicate,
				objectCsid, objectType);
	}

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
	public byte[] updateRelation(@PathParam("csid") String csid,
			String xmlText) {
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
		PoxPayloadOut result = null;
		try {
			PoxPayloadIn update = new PoxPayloadIn(xmlText);
        	        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(update);
			DocumentHandler handler = createDocumentHandler(ctx);
			getRepositoryClient(ctx).update(ctx, csid, handler);
			result = ctx.getOutput();
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
		return result.getBytes();
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
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
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
	 * Gets the relation list.
	 *
	 * @param queryParams the query params
	 * @param subjectCsid the subject csid
	 * @param subjectType 
	 * @param predicate the predicate
	 * @param objectCsid the object csid
	 * @param objectType 
	 * @return the relation list
	 * @throws WebApplicationException the web application exception
	 */
	public RelationsCommonList getRelationList(
			MultivaluedMap<String, String> queryParams,
			String subjectCsid,
			String subjectType,
			String predicate, 
			String objectCsid,
			String objectType) throws WebApplicationException {
		RelationsCommonList relationList = new RelationsCommonList();
		try {
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
			DocumentHandler handler = createDocumentHandler(ctx);
			String relationClause = RelationsUtils.buildWhereClause(subjectCsid, subjectType, predicate,
					objectCsid, objectType);
			handler.getDocumentFilter().appendWhereClause(relationClause, IQueryManager.SEARCH_QUALIFIER_AND);			
			getRepositoryClient(ctx).getFiltered(ctx, handler);
			relationList = (RelationsCommonList)handler.getCommonPartList();
		} catch (UnauthorizedException ue) {
			Response response = Response.status(Response.Status.UNAUTHORIZED).entity(
					"Get relations failed reason " +
					ue.getErrorReason()).type("text/plain").build();
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
