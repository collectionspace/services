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
package org.collectionspace.services;

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

import org.collectionspace.services.RelationService;
import org.collectionspace.services.relation.*;
import org.collectionspace.services.relation.RelationList.*;
import org.collectionspace.services.RelationJAXBSchema;
import org.collectionspace.services.common.ServiceMain;

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
	final Logger logger = LoggerFactory
			.getLogger(RelationResource.class);

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
	 * Gets the relation list.
	 * 
	 * @param ui the ui
	 * 
	 * @return the relation list
	 */
	@GET
	public RelationList getRelationList(@Context UriInfo ui) {
		
		URI absoluteURI = ui.getAbsolutePath();
		String uriString = absoluteURI.toString();
		
		RelationList p = new RelationList();
		try {
			Document document = service.getRelationList();
			Element root = document.getRootElement();

			// debug
			System.err.println(document.asXML());

			List<RelationList.RelationListItem> list = p
					.getRelationListItem();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				// debug
				System.err.println();
				element.asXML();

				// set the Relation list item entity elements
				RelationListItem pli = new RelationListItem();
				pli.setUri(element.attributeValue("url"));
				pli.setCsid(element.attributeValue("id"));
				list.add(pli);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return p;
	}

	/**
	 * Creates the relation.
	 * 
	 * @param ui the ui
	 * @param co the co
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
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Create failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

		//debug
		verbose("createRelation: ", co);
		
		UriBuilder uriBuilder = ui.getAbsolutePathBuilder();
		uriBuilder.path(csid);
		URI uri = uriBuilder.build();
		
		//debug
		System.out.println(uri.toString());
		
		Response response = Response.created(uri).build();
		return response;
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
					.elementIterator("rel:" + RelationJAXBSchema.REL_ROOT_ELEM_NAME);
					
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
			e.printStackTrace();
			Response response = Response.status(
					Response.Status.INTERNAL_SERVER_ERROR).entity("Get failed")
					.type("text/plain").build();
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
		verbose("getRelation: ", co);

		return co;
	}

	/**
	 * Update relation.
	 * 
	 * @param csid the csid
	 * @param theUpdate the the update
	 * 
	 * @return the relation
	 */
	@PUT
	@Path("{csid}")
	public Relation updateRelation(
			@PathParam("csid") String csid, Relation theUpdate) {

		verbose("updateRelation with input: ", theUpdate);

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
			// FIXME: NOT_FOUND?
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Update failed ").type("text/plain").build();
			throw new WebApplicationException(response);
		}

		return theUpdate;
	}

	/**
	 * Delete relation.
	 * 
	 * @param csid the csid
	 */
	@DELETE
	@Path("{csid}")
	public void deleteRelation(@PathParam("csid") String csid) {

		verbose("deleteRelation with csid=" + csid);
		try {
			
			Document document = service.deleteRelation(csid);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					String status = (String) element.getData();
					verbose("deleteRelationt response: " + status);
				}
			}
		} catch (Exception e) {
			// FIXME: NOT_FOUND?
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Delete failed ").type("text/plain").build();
			throw new WebApplicationException(response);
		}

	}
	
	/**
	 * Verbose.
	 * 
	 * @param msg the msg
	 * @param co the co
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
	 * @param msg the msg
	 */
	private void verbose(String msg) {
		System.out.println("RelationResource. " + msg);
	}

}
