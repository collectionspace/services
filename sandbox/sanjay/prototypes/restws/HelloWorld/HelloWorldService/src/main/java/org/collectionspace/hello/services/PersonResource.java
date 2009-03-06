package org.collectionspace.hello.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/persons")
@Consumes("application/xml")
@Produces("application/xml")
public class PersonResource {

    final Logger logger = LoggerFactory.getLogger(PersonResource.class);
    private Map<Long, Person> personDB = new ConcurrentHashMap<Long, Person>();
    private AtomicLong idCounter = new AtomicLong();

    public PersonResource() {
    }

    @POST
    public Response createPerson(Person p) {
        p.setId(idCounter.incrementAndGet());
        p.setVersion(1);
        personDB.put(p.getId(), p);
        verbose("created person", p);
        UriBuilder path = UriBuilder.fromResource(PersonResource.class);
        path.path("" + p.getId());
        Response response = Response.created(path.build()).build();
        return response;
    }

    @GET
    @Path("{id}")
    public Person getPerson(@PathParam("id") Long id) {
        Person p = personDB.get(id);
        if (p == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested person ID:" + id + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        verbose("get person", p);
        return p;
    }

    @PUT
    @Path("{id}")
    public Person updatePerson(@PathParam("id") Long id, Person update) {
        Person current = personDB.get(id);
        if (current == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed, the person ID:" + id + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        verbose("update person input", update);
        //todo: intelligent merge needed
        current.setFirstName(update.getFirstName());
        current.setLastName(update.getLastName());
        current.setStreet(update.getStreet());
        current.setState(update.getState());
        current.setZip(update.getZip());
        current.setCountry(update.getCountry());
        current.setVersion(current.getVersion() + 1);
        verbose("update person output", current);
        return current;
    }

    private void verbose(String msg, Person p) {
        try {
            System.out.println("PersonResource : " + msg);
            JAXBContext jc = JAXBContext.newInstance(Person.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(p, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @POST
//    @Consumes("application/xml")
//    public Response createPerson(InputStream is) {
//        Person p = readPerson(is);
//        p.setId(idCounter.incrementAndGet());
//        p.setVersion(1);
//        personDB.put(p.getId(), p);
//        try {
//            System.out.println("Created Person " + p.getId());
//            outputPerson(System.out, p);
//        } catch (IOException ioe) {
//            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//        }
//        return Response.created(URI.create("/persons/" + p.getId())).build();
//
//    }
//
//    @GET
//    @Path("{id}")
//    @Produces("application/xml")
//    public StreamingOutput getPerson(@PathParam("id") int id) {
//        final Person p = personDB.get(id);
//        if (p == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//        return new StreamingOutput() {
//
//            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
//                outputPerson(outputStream, p);
//            }
//        };
//    }
//
//    @PUT
//    @Path("{id}")
//    @Consumes("application/xml")
//    @Produces("application/xml")
//    public StreamingOutput updatePerson(@PathParam("id") int id, InputStream is) {
//        Person update = readPerson(is);
//        Person current = personDB.get(id);
//        if (current == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//
//        current.setFirstName(update.getFirstName());
//        current.setLastName(update.getLastName());
//        current.setStreet(update.getStreet());
//        current.setState(update.getState());
//        current.setZip(update.getZip());
//        current.setCountry(update.getCountry());
//        current.setVersion(current.getVersion() + 1);
//        final Person scurrent = current;
//        return new StreamingOutput() {
//
//            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
//                outputPerson(outputStream, scurrent);
//            }
//        };
//    }
//
//    protected void outputPerson(OutputStream os, Person p) throws IOException {
//        PrintStream writer = new PrintStream(os);
//        writer.println("<Person id=\"" + p.getId() + "\" version=\"" + p.getVersion() + "\">");
//        writer.println("   <first-name>" + p.getFirstName() + "</first-name>");
//        writer.println("   <last-name>" + p.getLastName() + "</last-name>");
//        writer.println("   <street>" + p.getStreet() + "</street>");
//        writer.println("   <city>" + p.getCity() + "</city>");
//        writer.println("   <state>" + p.getState() + "</state>");
//        writer.println("   <zip>" + p.getZip() + "</zip>");
//        writer.println("   <country>" + p.getCountry() + "</country>");
//        writer.println("</Person>");
//    }
//
//    protected Person readPerson(InputStream is) {
//        try {
//            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document doc = builder.parse(is);
//            Element root = doc.getDocumentElement();
//            Person p = new Person();
//            if (root.getAttribute("id") != null && !root.getAttribute("id").trim().equals("")) {
//                p.setId(Integer.valueOf(root.getAttribute("id")));
//            }
//            if (root.getAttribute("version") != null && !root.getAttribute("version").trim().equals("")) {
//                p.setVersion(Integer.valueOf(root.getAttribute("version")));
//            }
//            NodeList nodes = root.getChildNodes();
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Element element = (Element) nodes.item(i);
//                if (element.getTagName().equals("first-name")) {
//                    p.setFirstName(element.getTextContent());
//                } else if (element.getTagName().equals("last-name")) {
//                    p.setLastName(element.getTextContent());
//                } else if (element.getTagName().equals("street")) {
//                    p.setStreet(element.getTextContent());
//                } else if (element.getTagName().equals("city")) {
//                    p.setCity(element.getTextContent());
//                } else if (element.getTagName().equals("state")) {
//                    p.setState(element.getTextContent());
//                } else if (element.getTagName().equals("zip")) {
//                    p.setZip(element.getTextContent());
//                } else if (element.getTagName().equals("country")) {
//                    p.setCountry(element.getTextContent());
//                }
//            }
//            return p;
//        } catch (Exception e) {
//            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
//        }
//    }
}
