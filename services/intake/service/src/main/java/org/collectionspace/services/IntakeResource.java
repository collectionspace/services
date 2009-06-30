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

import org.collectionspace.services.IntakeService;
import org.collectionspace.services.intake.*;
import org.collectionspace.services.intake.IntakeList.*;
import org.collectionspace.services.IntakeJAXBSchema;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/intakes")
@Consumes("application/xml")
@Produces("application/xml")
public class IntakeResource {

	final Logger logger = LoggerFactory
			.getLogger(IntakeResource.class);

	// This should be a DI wired by a container like Spring, Seam, or EJB3
	final static IntakeService service = new IntakeServiceNuxeoImpl();

	public IntakeResource() {
		// do nothing
	}

	@GET
	public IntakeList getIntakeList(@Context UriInfo ui) {
		IntakeList p = new IntakeList();
		try {
			Document document = service.getIntakeList();
			Element root = document.getRootElement();

			// debug
			System.err.println(document.asXML());

			List<IntakeList.IntakeListItem> list = p
					.getIntakeListItem();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				// debug
				System.err.println();
				element.asXML();

				// set the Intake list item entity elements
				IntakeListItem pli = new IntakeListItem();
				pli.setEntryNumber(element.attributeValue("entryNumber"));
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
	public Response createIntake(Intake intake) {
		
		String csid = null;
		try {
			Document document = service.postIntake(intake);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					csid = (String) element.getData();
					intake.setCsid(csid);
				}
			}
		} catch (Exception e) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Create failed").type("text/plain").build();
			throw new WebApplicationException(response);
		}

		verbose("createIntake: ", intake);
		UriBuilder path = UriBuilder
				.fromResource(IntakeResource.class);
		path.path("" + csid);
		Response response = Response.created(path.build()).build();

		return response;
	}

	@GET
	@Path("{csid}")
	public Intake getIntake(@PathParam("csid") String csid) {

		Intake intake = null;
		try {
			Document document = service.getIntake(csid);
			Element root = document.getRootElement();
			intake = new Intake();

			// TODO: recognize schema thru namespace uri
			// Namespace ns = new Namespace("intake",
			// "http://collectionspace.org/intake");

			Iterator<Element> siter = root.elementIterator("schema");
			while (siter.hasNext()) {

				Element schemaElement = siter.next();
				System.err
						.println("Intake.getIntake() called.");

				// TODO: recognize schema thru namespace uri
				if (IntakeService.INTAKE_SCHEMA_NAME.equals(schemaElement.attribute("name")
						.getValue())) {
					Element ele = schemaElement
							.element(IntakeJAXBSchema.CURRENT_OWNER);
					if (ele != null) {
						intake.setCurrentOwner((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.DEPOSITOR);
					if (ele != null) {
						intake.setDepositor((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS);
					if (ele != null) {
						intake.setDepositorsRequirements((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.ENTRY_DATE);
					if (ele != null) {
						intake.setEntryDate((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.ENTRY_METHOD);
					if (ele != null) {
						intake.setEntryMethod((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.ENTRY_NOTE);
					if (ele != null) {
						intake.setEntryNote((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.ENTRY_NUMBER);
					if (ele != null) {
						intake.setEntryNumber((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.ENTRY_REASON);
					if (ele != null) {
						intake.setEntryReason((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.PACKING_NOTE);
					if (ele != null) {
						intake.setPackingNote((String) ele.getData());
					}
					ele = schemaElement
							.element(IntakeJAXBSchema.RETURN_DATE);
					if (ele != null) {
						intake.setReturnDate((String) ele.getData());
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
		if (intake == null) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(
							"Get failed, the requested Intake CSID:"
									+ csid + ": was not found.").type(
							"text/plain").build();
			throw new WebApplicationException(response);
		}
		verbose("getIntake: ", intake);

		return intake;
	}

	@PUT
	@Path("{csid}")
	public Intake updateIntake(
			@PathParam("csid") String csid, Intake theUpdate) {

		verbose("updateIntake with input: ", theUpdate);

		String status = null;
		try {

			Document document = service.putIntake(csid, theUpdate);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					status = (String) element.getData();
					verbose("updateIntake response: " + status);
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
	public void deleteIntake(@PathParam("csid") String csid) {

		verbose("deleteIntake with csid=" + csid);
		try {
			
			Document document = service.deleteIntake(csid);
			Element root = document.getRootElement();
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				Element element = (Element) i.next();
				if ("docRef".equals(element.getName())) {
					String status = (String) element.getData();
					verbose("deleteIntaket response: " + status);
				}
			}
		} catch (Exception e) {
			// FIXME: NOT_FOUND?
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity("Delete failed ").type("text/plain").build();
			throw new WebApplicationException(response);
		}

	}

	private void verbose(String msg, Intake intake) {
		try {
			verbose(msg);
			JAXBContext jc = JAXBContext.newInstance(Intake.class);

			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.marshal(intake, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void verbose(String msg) {
		System.out.println("IntakeResource. " + msg);
	}

}
