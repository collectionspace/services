/**	
 * RelationResource.java
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
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.relation;

import java.util.Iterator;
import java.util.List;
import java.net.URI;

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

import org.collectionspace.services.relation.RelationService;
import org.collectionspace.services.relation.*;
import org.collectionspace.services.relation.RelationList.*;

import org.collectionspace.services.relation.nuxeo.RelationServiceNuxeoImpl;
import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.common.relation.RelationListItemJAXBSchema;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.DocumentException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;

import org.dom4j.Document;
import org.dom4j.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RelationResource.
 */
@Path("/relations")
@Consumes("application/xml")
@Produces("application/xml")
public class RelationResource {

	/** The logger. */
	final Logger logger = LoggerFactory.getLogger(RelationResource.class);

	// This should be a DI wired by a container like Spring, Seam, or EJB3
	/** The Constant service. */
	final static RelationService service = new RelationServiceNuxeoImpl();

	/**
	 * Instantiates a new relation resource.
	 */
	public RelationResource() {
		// do nothing
	}

	/**
	 * Returns a list of *all* relation entities.
	 * 
	 * @param ui
	 *            the ui
	 * 
	 * @return the relation list
	 */
	@GET
	public RelationList getRelationList(@Context UriInfo ui) {
		RelationList relationList = this.getRequestedRelationList(ui, null,
				null, null);

		return relationList;
	}

	/**
	 * Gets a list of relations with the subject=
	 * 
	 * @param ui
	 *            the ui
	 * @param subjectCsid
	 *            the subject == subjectCsid
	 * 
	 * @return the relation list_ s
	 */
	@GET
	@Path("subject/{subjectCsid}")
	public RelationList getRelationList_S(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid) {
		RelationList relationList = this.getRequestedRelationList(ui,
				subjectCsid, null, null);

		return relationList;
	}

	/**
	 * Gets a list of relations with predicate == predicate.
	 * 
	 * @param ui
	 *            the ui
	 * @param predicate
	 *            the predicate
	 * 
	 * @return the relation list of type
	 */
	@GET
	@Path("type/{predicate}")
	public RelationList getRelationList_P(@Context UriInfo ui,
			@PathParam("predicate") String predicate) {
		RelationList relationList = this.getRequestedRelationList(ui, null,
				predicate, null);

		return relationList;
	}

	/**
	 * Gets a list of relations with object == objectCsid
	 * 
	 * @param ui
	 *            the ui
	 * @param objectCsid
	 *            the object csid
	 * 
	 * @return the relation list_ o
	 */
	@GET
	@Path("object/{objectCsid}")
	public RelationList getRelationList_O(@Context UriInfo ui,
			@PathParam("objectCsid") String objectCsid) {
		RelationList relationList = this.getRequestedRelationList(ui, null,
				null, objectCsid);

		return relationList;
	}

	/**
	 * Gets a list of relations with predicate == predicate *and* subject ==
	 * subjectCsid
	 * 
	 * @param ui
	 *            the ui
	 * @param predicate
	 *            the predicate
	 * @param subjectCsid
	 *            the subject subjectCsid
	 * 
	 * @return the relation list of type with subject
	 */
	@GET
	@Path("type/{predicate}/subject/{subjectCsid}")
	public RelationList getRelationList_PS(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("subjectCsid") String subjectCsid) {
		RelationList relationList = this.getRequestedRelationList(ui,
				subjectCsid, predicate, null);

		return relationList;
	}

	/**
	 * Gets a list of relations with subject == subjectCsid *and* predicate ==
	 * predicate
	 * 
	 * @param ui
	 *            the ui
	 * @param subjectCsid
	 *            the subject csid
	 * @param predicate
	 *            the predicate
	 * 
	 * @return the relation list_ sp
	 */
	@GET
	@Path("subject/{subjectCsid}/type/{predicate}")
	public RelationList getRelationList_SP(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate) {
		RelationList relationList = this.getRequestedRelationList(ui,
				subjectCsid, predicate, null);

		return relationList;
	}

	/**
	 * Gets a list of relations with predicate == predicate *and* object ==
	 * objectCsid
	 * 
	 * @param ui
	 *            the ui
	 * @param predicate
	 *            the predicate
	 * @param objectCsid
	 *            the object csid
	 * 
	 * @return the relation list of type with object
	 */
	@GET
	@Path("type/{predicate}/object/{objectCsid}")
	public RelationList getRelationList_PO(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid) {
		RelationList relationList = this.getRequestedRelationList(ui, null,
				predicate, objectCsid);

		return relationList;
	}

	/**
	 * Gets a list of relations with object == objectCsid *and* predicate ==
	 * predicate
	 * 
	 * @param ui
	 *            the ui
	 * @param objectCsid
	 *            the object csid
	 * @param predicate
	 *            the predicate
	 * 
	 * @return the relation list_ op
	 */
	@GET
	@Path("object/{objectCsid}/type/{predicate}")
	public RelationList getRelationList_OP(@Context UriInfo ui,
			@PathParam("objectCsid") String objectCsid,
			@PathParam("predicate") String predicate) {
		RelationList relationList = this.getRequestedRelationList(ui, null,
				predicate, objectCsid);

		return relationList;
	}

	/**
	 * Gets a list of relations with predicate == predicate *and* subject ==
	 * subjectCsid *and* object == objectCsid
	 * 
	 * @param ui
	 *            the ui
	 * @param predicate
	 *            the predicate
	 * @param subjectCsid
	 *            the subject csid
	 * @param objectCsid
	 *            the object csid
	 * 
	 * @return the relation list
	 */
	@GET
	@Path("type/{predicate}/subject/{subjectCsid}/object/{objectCsid}")
	public RelationList getRelationList_PSO(@Context UriInfo ui,
			@PathParam("predicate") String predicate,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("objectCsid") String objectCsid) {
		RelationList relationList = this.getRequestedRelationList(ui,
				predicate, subjectCsid, objectCsid);

		return relationList;
	}

	/**
	 * Gets a list of relations with subject == subjectCsid *and* predicate ==
	 * predicate *and* object == objectCsid
	 * 
	 * @param ui
	 *            the ui
	 * @param subjectCsid
	 *            the subject csid
	 * @param predicate
	 *            the predicate
	 * @param objectCsid
	 *            the object csid
	 * 
	 * @return the relation list_ spo
	 */
	@GET
	@Path("subject/{subjectCsid}/type/{predicate}/object/{objectCsid}")
	public RelationList getRelationList_SPO(@Context UriInfo ui,
			@PathParam("subjectCsid") String subjectCsid,
			@PathParam("predicate") String predicate,
			@PathParam("objectCsid") String objectCsid) {
		RelationList relationList = this.getRequestedRelationList(ui,
				subjectCsid, predicate, objectCsid);

		return relationList;
	}

	/**
	 * Creates the relation.
	 * 
	 * @param ui
	 *            the ui
	 * @param co
	 *            the co
	 * 
	 * @return the response
	 */
	@POST
	public Response createRelation(@Context UriInfo ui, Relation co) {
		String csid = null;

		try {
			Document document = service.postRelation(co);
			Element root = document.getRootElement();
			csid = root.attributeValue("id");
			co.setCsid(csid);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in createRelation", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

		UriBuilder uriBuilder = ui.getAbsolutePathBuilder();
		uriBuilder.path(csid);
		URI uri = uriBuilder.build();

		Response response = Response.created(uri).build();
		return response;
	}

	/**
	 * Gets the relation.
	 * 
	 * @param csid
	 *            the csid
	 * 
	 * @return the relation
	 */
	@GET
	@Path("{csid}")
	public Relation getRelation(@PathParam("csid") String csid) {

		Relation co = null;
		try {
			Document document = service.getRelation(csid);
			Element root = document.getRootElement();
			co = new Relation();

			// TODO: recognize schema thru namespace uri
			// Namespace ns = new Namespace("relation",
			// "http://collectionspace.org/relation");

			Iterator<Element> siter = root.elementIterator("schema");
			while (siter.hasNext()) {
				Element schemaElement = siter.next();
				System.err.println("Relation.getRelation() called.");

				// TODO: recognize schema thru namespace uri
				if (RelationService.REL_SCHEMA_NAME.equals(schemaElement
						.attribute("name").getValue())) {
					Iterator<Element> relIter = schemaElement
							.elementIterator(RelationJAXBSchema.REL_ROOT_ELEM_NAME);
					Iterator<Element> relIter2 = schemaElement
							.elementIterator("rel:"
									+ RelationJAXBSchema.REL_ROOT_ELEM_NAME);

					while (relIter.hasNext()) {
						Element relElement = relIter.next();

						Element ele = relElement
								.element(RelationJAXBSchema.DOCUMENT_ID_1);
						if (ele != null) {
							co.setDocumentId1((String) ele.getData());
						}
						ele = relElement
								.element(RelationJAXBSchema.DOCUMENT_TYPE_1);
						if (ele != null) {
							co.setDocumentType1((String) ele.getData());
						}
						ele = relElement
								.element(RelationJAXBSchema.DOCUMENT_ID_2);
						if (ele != null) {
							co.setDocumentId2((String) ele.getData());
						}
						ele = relElement
								.element(RelationJAXBSchema.DOCUMENT_TYPE_2);
						if (ele != null) {
							co.setDocumentType2((String) ele.getData());
						}
						ele = relElement
								.element(RelationJAXBSchema.RELATIONSHIP_TYPE);
						if (ele != null) {
							RelationshipType rt = RelationshipType
									.fromValue((String) ele.getData());
							co.setRelationshipType(rt);
						}
					}
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in getRelation", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
		if (co == null) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(
							"Get failed, the requested Relation CSID:" + csid
									+ ": was not found.").type("text/plain")
					.build();
			throw new WebApplicationException(response);
		}

		if (logger.isDebugEnabled() == true) {
			verbose("getRelation: ", co);
		}

		return co;
	}

	/**
	 * Update relation.
	 * 
	 * @param csid
	 *            the csid
	 * @param theUpdate
	 *            the the update
	 * 
	 * @return the relation
	 */
	@PUT
	@Path("{csid}")
	public Relation updateRelation(@PathParam("csid") String csid,
			Relation theUpdate) {

		if (logger.isDebugEnabled() == true) {
			verbose("updateRelation with input: ", theUpdate);
		}

		String status = null;
		try {

			Document document = service.putRelation(csid, theUpdate);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					status = (String) element.getData();
					verbose("updateRelation response: " + status);
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in updateRelation", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

		return theUpdate;
	}

	/**
	 * Delete relation.
	 * 
	 * @param csid
	 *            the csid
	 */
	@DELETE
	@Path("{csid}")
	public void deleteRelation(@PathParam("csid") String csid) {

		verbose("deleteRelation with csid=" + csid);
		try {
			service.deleteRelation(csid);
		} catch (DocumentNotFoundException dnfe) {
			if (logger.isDebugEnabled()) {
				logger.debug("caught exception in deleteRelation", dnfe);
			}
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Delete failed on Relation csid=" + csid)
					.type("text/plain").build();
			throw new WebApplicationException(response);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in deleteRelation", e);
			}
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity(
					"Index failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}
	}

	/*
	 * Private Methods
	 */

	/**
	 * Gets the relation list common.
	 * 
	 * @param ui
	 *            the ui
	 * @param subjectCsid
	 *            the subject csid
	 * @param predicate
	 *            the predicate
	 * @param objectCsid
	 *            the object csid
	 * 
	 * @return the relation list common
	 * 
	 * @throws WebApplicationException
	 *             the web application exception
	 */
	private RelationList getRequestedRelationList(@Context UriInfo ui,
			String subjectCsid, String predicate, String objectCsid)
			throws WebApplicationException {

		URI absoluteURI = ui.getAbsolutePath();
		String uriString = absoluteURI.toString();

		RelationList relationList = null;
		try {
			relationList = this.getRelationList(subjectCsid, predicate,
					objectCsid);
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

	/**
	 * Gets the relation list.
	 * 
	 * @param subjectCsid
	 *            the subject csid
	 * @param predicate
	 *            the predicate
	 * @param objectCsid
	 *            the object csid
	 * 
	 * @return the relation list
	 * 
	 * @throws DocumentException
	 *             the document exception
	 */
	private RelationList getRelationList(String subjectCsid, String predicate,
			String objectCsid) throws DocumentException {
		RelationList relationList = new RelationList();
		try {
			Document document = service.getRelationList(subjectCsid, predicate,
					objectCsid);
			if (logger.isDebugEnabled() == true) {
				System.err.println(document.asXML());
			}

			Element root = document.getRootElement();
			List<RelationList.RelationListItem> list = relationList
					.getRelationListItem();
			Element node = null;
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				node = (Element) i.next();
				if (logger.isDebugEnabled() == true) {
					System.out.println();
					node.asXML();
				}

				// set the Relation list item entity elements
				RelationListItem listItem = new RelationListItem();
				listItem.setUri(node
						.attributeValue(RelationListItemJAXBSchema.URI));
				listItem.setCsid(node
						.attributeValue(RelationListItemJAXBSchema.CSID));
				list.add(listItem);
			}

		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Caught exception in getRelationListOfType", e);
			}
			throw new DocumentException(e);
		}

		return relationList;
	}

	/**
	 * Verbose.
	 * 
	 * @param msg
	 *            the msg
	 * @param co
	 *            the co
	 */
	private void verbose(String msg, Relation co) {
		try {
			verbose(msg);
			JAXBContext jc = JAXBContext.newInstance(Relation.class);

			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(co, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verbose.
	 * 
	 * @param msg
	 *            the msg
	 */
	private void verbose(String msg) {
		System.out.println("RelationResource. " + msg);
	}

}
