package org.collectionspace.hello.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.hello.DomainIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/domainidentifiers")
@Consumes("application/xml")
@Produces("application/xml")
public class DomainIdentifierResource implements CollectionSpaceResource {

    final Logger logger = LoggerFactory.getLogger(IdentifierResource.class);
    private Map<String, DomainIdentifier> idDB = new ConcurrentHashMap<String, DomainIdentifier>();

    public DomainIdentifierResource() {
    	// do nothing
    }

    @POST
    public Response createIdentifier(DomainIdentifier id) {
    	DomainIdentifier newId = id;
    	
        if (newId.getDsid() == null) {
        	newId.setDsid("org.collectionspace");
        }
        newId.setDsid(newId.getDsid() + System.currentTimeMillis());
        idDB.put(newId.getDsid(), newId);
        
        verbose("createIdentifier: ", newId);
        UriBuilder path = UriBuilder.fromResource(DomainIdentifierResource.class);
        path.path("" + newId.getDsid());
        Response response = Response.created(path.build()).build();
        
        return response;
    }

    @GET
    @Path("{id}")
    public DomainIdentifier getIdentifier(@PathParam("id") String id) {
        DomainIdentifier result = idDB.get(id);
        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "The requested DomainIdentifier was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        verbose("getIdentifier: ", result);
        
        return result;
    }

    private void verbose(String msg, DomainIdentifier id) {
        try {
            System.out.println("DomainIdentifierResource: " + msg);
            JAXBContext jc = JAXBContext.newInstance(DomainIdentifier.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(id, System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
