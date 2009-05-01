package org.collectionspace.services;

import java.util.Iterator;
import java.util.List;
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

import org.collectionspace.services.CollectionObjectService;
import org.collectionspace.services.collectionobject.*;
import org.collectionspace.services.collectionobject.CollectionObjectList.*;
import org.collectionspace.services.CollectionObjectJAXBSchema;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/collectionobjects")
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionObjectResource implements CollectionSpaceResource {

	final Logger logger = LoggerFactory
			.getLogger(CollectionObjectResource.class);

	// This should be a DI wired by a container like Spring, Seam, or EJB3
	final static CollectionObjectService service = new CollectionObjectServiceNuxeoImpl();

	public CollectionObjectResource() {
		// do nothing
	}

	@GET
	public CollectionObjectList getCollectionObjectList(@Context UriInfo ui) {
		CollectionObjectList p = new CollectionObjectList();
		try {
			Document document = service.getCollectionObjectList();
			Element root = document.getRootElement();

			// debug
			System.err.println(document.asXML());

			List<CollectionObjectList.CollectionObjectListItem> list = p
					.getCollectionObjectListItem();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				// debug
				System.err.println();
				element.asXML();

				// set the CollectionObject list item entity elements
				CollectionObjectListItem pli = new CollectionObjectListItem();
				pli.setObjectNumber(element.attributeValue("title"));
				pli.setUri(element.attributeValue("url"));
				pli.setCsid(element.attributeValue("id"));
				list.add(pli);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return p;
	}

	@POST
	public Response createCollectionObject(CollectionObject co) {
		
		String csid = null;
		try {

			Document document = service.postCollectionObject(co);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					csid = (String) element.getData();
					co.setCsid(csid);
				}
			}
		} catch (Exception e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Create failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

		verbose("createCollectionObject: ", co);
		UriBuilder path = UriBuilder
				.fromResource(CollectionObjectResource.class);
		path.path("" + csid);
		Response response = Response.created(path.build()).build();

		return response;
	}

	@GET
	@Path("{csid}")
	public CollectionObject getCollectionObject(@PathParam("csid") String csid) {

		CollectionObject co = null;
		try {
			Document document = service.getCollectionObject(csid);
			Element root = document.getRootElement();
			co = new CollectionObject();

			// TODO: recognize schema thru namespace uri
			// Namespace ns = new Namespace("collectionobject",
			// "http://collectionspace.org/collectionobject");

			Iterator<Element> siter = root.elementIterator("schema");
			while (siter.hasNext()) {

				Element schemaElement = siter.next();
				System.err
						.println("CollectionObject.getCollectionObject() called.");

				// TODO: recognize schema thru namespace uri
				if (CollectionObjectService.CO_SCHEMA_NAME.equals(schemaElement.attribute("name")
						.getValue())) {
					Element ele = schemaElement
							.element(CollectionObjectJAXBSchema.OBJECT_NUMBER);
					if (ele != null) {
						co.setObjectNumber((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.OTHER_NUMBER);
					if (ele != null) {
						co.setOtherNumber((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.BRIEF_DESCRIPTION);
					if (ele != null) {
						co.setBriefDescription((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.COMMENTS);
					if (ele != null) {
						co.setComments((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.DIST_FEATURES);
					if (ele != null) {
						co.setDistFeatures((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.OBJECT_NAME);
					if (ele != null) {
						co.setObjectName((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.RESPONSIBLE_DEPT);
					if (ele != null) {
						co.setResponsibleDept((String) ele.getData());
					}
					ele = schemaElement
							.element(CollectionObjectJAXBSchema.TITLE);
					if (ele != null) {
						co.setTitle((String) ele.getData());
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
							"Get failed, the requested CollectionObject CSID:"
									+ csid + ": was not found.").type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		verbose("getCollectionObject: ", co);

		return co;
	}

	@PUT
	@Path("{csid}")
	public CollectionObject updateCollectionObject(
			@PathParam("csid") String csid, CollectionObject theUpdate) {

		verbose("updateCollectionObject with input: ", theUpdate);

		String status = null;
		try {

			Document document = service.putCollectionObject(csid, theUpdate);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					status = (String) element.getData();
					verbose("updateCollectionObject response: " + status);
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

	@DELETE
	@Path("{csid}")
	public void deleteCollectionObject(@PathParam("csid") String csid) {

		verbose("deleteCollectionObject with csid=" + csid);
		try {
			
			Document document = service.deleteCollectionObject(csid);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					String status = (String) element.getData();
					verbose("deleteCollectionObjectt response: " + status);
				}
			}
		} catch (Exception e) {
			// FIXME: NOT_FOUND?
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Delete failed ").type("text/plain").build();
			throw new WebApplicationException(response);
		}

	}

	private void verbose(String msg, CollectionObject co) {
		try {
			verbose(msg);
			JAXBContext jc = JAXBContext.newInstance(CollectionObject.class);

			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(co, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void verbose(String msg) {
		System.out.println("CollectionObjectResource. " + msg);
	}

}
