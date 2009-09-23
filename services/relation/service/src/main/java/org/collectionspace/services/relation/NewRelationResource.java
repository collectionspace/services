/**	
 * NewRelationResource.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;


import org.collectionspace.services.common.AbstractCollectionSpaceResource;
import org.collectionspace.services.common.NuxeoClientType;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.relation.RelationsManager;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/relations")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class NewRelationResource extends AbstractCollectionSpaceResource {

	public final static String serviceName = "relations";
	final Logger logger = LoggerFactory.getLogger(NewRelationResource.class);
	

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public DocumentHandler createDocumentHandler(ServiceContext ctx) throws Exception {
        DocumentHandler docHandler = RelationHandlerFactory.getInstance().getHandler(
                ctx.getRepositoryClientType().toString());
        docHandler.setServiceContext(ctx);
        if(ctx.getInput() != null){
            Object obj = ctx.getInputPart(ctx.getCommonPartLabel(), CollectionobjectsCommon.class);
            if(obj != null){
                docHandler.setCommonPart((CollectionobjectsCommon) obj);
            }
        }
        return docHandler;
    }

	
//	public NewRelationResource() {
//	}

	@POST
	public Response createRelation(Relation relation) {

		String csid = null;
		try {
			getDefaultHandler().setCommonObject(relation);
			csid = getDefaultClient().create(SERVICE_NAME, getDefaultHandler());
			relation.setCsid(csid);
			if (logger.isDebugEnabled()) {
				verbose("createRelation: ", relation);
			}
			UriBuilder path = UriBuilder.fromResource(RelationResource.class);
			path.path("" + csid);
			Response response = Response.created(path.build()).build();
			return response;
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
	
	/*
	 * Prototype for using NXQL -will move away from here once protyping is finished.
	 */
	@GET
	@Path("query/{queryValue}")
	public Response getQuery(@PathParam("queryValue") String queryString) {
		
		Response result = null;
		
		if (logger.isDebugEnabled() == true) {
			logger.debug("Query string is: " + queryString);
		}
		
		//getDefaultClient().
				
		result = Response.status(Response.Status.ACCEPTED).entity(
						"Query performed. Look in $JBOSS_HOME/server/cspace/log/" +
						"directory for results ").type("text/plain").build();
		
		return result;
	}

	@GET
	@Path("{csid}")
	public Relation getRelation(@PathParam("csid") String csid) {
		if (logger.isDebugEnabled()) {
			verbose("getRelation with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("getRelation: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("get failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		Relation relation = null;
		try {
			getDefaultClient().get(csid, getDefaultHandler());
			relation = (Relation) getDefaultHandler().getCommonObject();
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

		if (relation == null) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(
							"Get failed, the requested Relation CSID:" + csid
									+ ": was not found.").type("text/plain")
					.build();
			throw new WebApplicationException(response);
		}
		if (logger.isDebugEnabled()) {
			verbose("getRelation: ", relation);
		}
		return relation;
	}
	
	/*
	 * BEGIN OF GET LIST
	 */

	@GET
	public RelationList getRelationList(@Context UriInfo ui) {
		return this.getRelationListRequest(null, null, null);
	}

	@GET
	@Path("subject/{subjectCsid}")
	public RelationList getRelationList_S(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid) {
		return this.getRelationListRequest(subjectCsid, null, null);
	}
	
	@GET
	@Path("type/{predicate}")
	public RelationList getRelationList_P(@Context UriInfo ui,
			@PathParam("predicate") String predicate) {
		return this.getRelationListRequest(null, predicate, null);
	}

	@GET
	@Path("object/{objectCsid}")
	public RelationList getRelationList_O(@Context UriInfo ui,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationListRequest(null, null, objectCsid);
	}

	@GET
	@Path("type/{predicate}/subject/{subjectCsid}")
	public RelationList getRelationList_PS(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("subjectCsid") String subjectCsid) {
		return this.getRelationListRequest(subjectCsid, predicate, null);
	}
	
	@GET
	@Path("subject/{subjectCsid}/type/{predicate}")
	public RelationList getRelationList_SP(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate) {
		return this.getRelationListRequest(subjectCsid, predicate, null);
	}
	
	@GET
	@Path("type/{predicate}/object/{objectCsid}")
	public RelationList getRelationList_PO(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationListRequest(null, predicate, objectCsid);
	}	
	
	@GET
	@Path("object/{objectCsid}/type/{predicate}")
	public RelationList getRelationList_OP(@Context UriInfo ui,
			@PathParam("objectCsid") String objectCsid,
			@PathParam("predicate") String predicate) {
		return this.getRelationListRequest(null, predicate, objectCsid);
	}
	
	@GET
	@Path("type/{predicate}/subject/{subjectCsid}/object/{objectCsid}")
	public RelationList getRelationList_PSO(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationListRequest(subjectCsid, predicate, objectCsid);
	}

	@GET
	@Path("subject/{subjectCsid}/type/{predicate}/object/{objectCsid}")
	public RelationList getRelationList_SPO(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid) {
		return this.getRelationListRequest(subjectCsid, predicate, objectCsid);
	}
	
	/*
	 * END OF GET LIST
	 */
	
	@PUT
	@Path("{csid}")
	public Relation updateRelation(@PathParam("csid") String csid,
			Relation theUpdate) {
		if (logger.isDebugEnabled()) {
			verbose("updateRelation with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("updateRelation: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("update failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		if (logger.isDebugEnabled()) {
			verbose("updateRelation with input: ", theUpdate);
		}
		try {
			getDefaultHandler().setCommonObject(theUpdate);
			getDefaultClient().update(csid, getDefaultHandler());
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
		return theUpdate;
	}

	@DELETE
	@Path("{csid}")
	public Response deleteRelation(@PathParam("csid") String csid) {

		if (logger.isDebugEnabled()) {
			verbose("deleteRelation with csid=" + csid);
		}
		if (csid == null || "".equals(csid)) {
			logger.error("deleteRelation: missing csid!");
			Response response = Response.status(Response.Status.BAD_REQUEST)
					.entity("delete failed on Relation csid=" + csid).type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		try {
			getDefaultClient().delete(csid);
			return Response.status(HttpResponseCodes.SC_OK).build();
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

	/*
	 * Private Methods
	 */

	/**
	 * Gets the relation list request.
	 * 
	 * @return the relation list request
	 * 
	 * @throws WebApplicationException the web application exception
	 */
	private RelationList getRelationListRequest(String subjectCsid,
			String predicate,
			String objectCsid)
				throws WebApplicationException {
		RelationList relationList = new RelationList();
		try {
			Map propsFromPath = getDefaultHandler().getProperties();
			propsFromPath.put(RelationsManager.SUBJECT, subjectCsid);
			propsFromPath.put(RelationsManager.PREDICATE, predicate);
			propsFromPath.put(RelationsManager.OBJECT, objectCsid);

			getDefaultClient().getAll(SERVICE_NAME, getDefaultHandler());
			relationList = (RelationList) getDefaultHandler().getCommonObjectList();
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

	private void verbose(String msg, Relation relation) {
		try {
			verbose(msg);
			JAXBContext jc = JAXBContext.newInstance(Relation.class);

			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(relation, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void verbose(String msg) {
		System.out.println("RelationResource. " + msg);
	}
}
