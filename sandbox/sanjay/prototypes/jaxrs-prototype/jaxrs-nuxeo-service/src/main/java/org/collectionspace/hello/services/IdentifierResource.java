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
import org.collectionspace.hello.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/identifiers")
@Consumes("application/xml")
@Produces("application/xml")
public class IdentifierResource {

    final Logger logger = LoggerFactory.getLogger(IdentifierResource.class);
    private Map<Long, Identifier> idDB = new ConcurrentHashMap<Long, Identifier>();
    private AtomicLong idCounter = new AtomicLong();

    public IdentifierResource() {
    	// do nothing
    }

    @POST
    public Response createIdentifier(Identifier id) {
        if (id.getNamespace() == null) {
            id.setNamespace("edu.berkeley");
        }
        id.setId(idCounter.incrementAndGet());
        id.setVersion(1);
        UUID uuid = UUID.nameUUIDFromBytes(id.getNamespace().getBytes());
        id.setValue(uuid.toString());
        idDB.put(id.getId(), id);
        verbose("created Id", id);
        UriBuilder path = UriBuilder.fromResource(IdentifierResource.class);
        path.path("" + id.getId());
        Response response = Response.created(path.build()).build();
        return response;
    }

    @GET
    @Path("{id}")
    public Identifier getIdentifier(@PathParam("id") Long id) {
        Identifier i = idDB.get(id);
        if (i == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "The requested ID was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        verbose("get Id", i);
        return i;
    }

    private void verbose(String msg, Identifier id) {
        try {
            System.out.println("IdentifierResource : " + msg);
            JAXBContext jc = JAXBContext.newInstance(Identifier.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(id, System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
