package org.collectionspace.hello.services;

import java.net.URI;
import java.util.List;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.CollectionObject;
import org.collectionspace.hello.CollectionObjectList;
import org.collectionspace.hello.CollectionObjectListItem;
import org.collectionspace.hello.DefaultCollectionObject;
import org.collectionspace.hello.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/collectionobjects")
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionObjectResource {

    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);
    private Map<String, CollectionObject> CollectionObjectDB =
      new ConcurrentHashMap<String, CollectionObject>();
    private AtomicLong idCounter = new AtomicLong();

    public CollectionObjectResource() {
    }

    @POST
    public Response createCollectionObject(CollectionObject c) {
      if (c == null) {
        Response response = Response.status(Response.Status.BAD_REQUEST).entity(
          "Add failed, the CollectionObject provided was empty.").type("text/plain").build();
        throw new WebApplicationException(response);
      }
      Long id = idCounter.incrementAndGet();
      // c.getServiceMetadata().setCollectionSpaceId(id.toString());
      c.setServiceMetadata( new ServiceMetadata() );
      c.getServiceMetadata().setCollectionSpaceId("100");
      // c.setVersion(1);
      CollectionObjectDB.put(c.getServiceMetadata().getCollectionSpaceId(), c);
      verbose("created CollectionObject", c);
      UriBuilder path = UriBuilder.fromResource(CollectionObjectResource.class);
      path.path("" + c.getServiceMetadata().getCollectionSpaceId());
      Response response = Response.created(path.build()).build();
      return response;
    }

    @GET
    @Path("{id}")
    public CollectionObject getCollectionObject(@PathParam("id") String id) {
      CollectionObject c = CollectionObjectDB.get(id);
      if (c == null) {
        Response response = Response.status(Response.Status.NOT_FOUND).entity(
          "Get failed, the requested CollectionObject ID:" + id + ": was not found.").type("text/plain").build();
        throw new WebApplicationException(response);
      }
      verbose("get CollectionObject", c);
      return c;
    }

    @PUT
    @Path("{id}")
    public CollectionObject updateCollectionObject(@PathParam("id") String id, CollectionObject update) {
      CollectionObject current = CollectionObjectDB.get(id);
      if (current == null) {
        Response response = Response.status(Response.Status.NOT_FOUND).entity(
          "Update failed, the CollectionObject ID:" + id + ": was not found.").type("text/plain").build();
        throw new WebApplicationException(response);
      }
      verbose("update CollectionObject input", update);
      //todo: intelligent merge needed
      // current.getServiceMetadata().setLastUpdated( [current date/time here] );
      current.getDefaultCollectionObject().setObjectNumber(
        update.getDefaultCollectionObject().getObjectNumber());
      current.getDefaultCollectionObject().setOtherNumber(
        update.getDefaultCollectionObject().getOtherNumber());
      current.getDefaultCollectionObject().setBriefDescription(
        update.getDefaultCollectionObject().getBriefDescription());
      current.getDefaultCollectionObject().setComments(
        update.getDefaultCollectionObject().getComments());
      current.getDefaultCollectionObject().setDistinguishingFeatures(
        update.getDefaultCollectionObject().getDistinguishingFeatures());
      current.getDefaultCollectionObject().setObjectName(
        update.getDefaultCollectionObject().getObjectName());
      current.getDefaultCollectionObject().setResponsibleDepartment(
        update.getDefaultCollectionObject().getResponsibleDepartment());
        verbose("update CollectionObject output", current);
      return current;
    }

    // Get a list
    @GET
    public CollectionObjectList getCollectionObjectList(@Context UriInfo ui) {
      CollectionObjectList CollectionObjectList = new CollectionObjectList();
      // The auto-generated method called here has a potentially misleading name; it returns a List.
      List<CollectionObjectListItem> list =
        CollectionObjectList.getCollectionObjectListItem();
      // builder starts with current URI and has appended path of getCollectionObject method
      UriBuilder ub = ui.getAbsolutePathBuilder().path(this.getClass(), "getCollectionObject");
      for (CollectionObject c : CollectionObjectDB.values()) {
        CollectionObjectListItem cli = new CollectionObjectListItem();
        cli.setCollectionSpaceId(c.getServiceMetadata().getCollectionSpaceId());
        cli.setObjectNumber(c.getDefaultCollectionObject().getObjectNumber());
        cli.setObjectName(c.getDefaultCollectionObject().getObjectName());
        // builder has {id} variable that must be filled in for each customer
        URI uri = ub.build(c.getServiceMetadata().getCollectionSpaceId());
        cli.setUri(uri.toString());
        list.add(cli);
      }
      return CollectionObjectList;
    }

    @DELETE
    @Path("{id}")
    public void deleteCollectionObject(@PathParam("id") String id) {
      CollectionObject removed = CollectionObjectDB.remove(id);
      if (removed == null) {
        Response response = Response.status(Response.Status.NOT_FOUND).entity(
          "Delete failed, the CollectionObject ID:" + id + ": was not found.").type("text/plain").build();
        throw new WebApplicationException(response);
      }
      verbose("deleted CollectionObject", removed);
    }

    private void verbose(String msg, CollectionObject c) {
      try {
        System.out.println("CollectionObjectResource : " + msg);
        JAXBContext jc = JAXBContext.newInstance(CollectionObject.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(c, System.out);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

//    @POST
//    @Consumes("application/xml")
//    public Response createCollectionObject(InputStream is) {
//        CollectionObject c = readCollectionObject(is);
//        c.setId(idCounter.incrementAndGet());
//        c.setVersion(1);
//        CollectionObjectDB.put(c.getId(), c);
//        try {
//            System.out.println("Created CollectionObject " + c.getId());
//            outputCollectionObject(System.out, c);
//        } catch (IOException ioe) {
//            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
//        }
//        return Response.created(URI.create("/CollectionObjects/" + c.getId())).build();
//
//    }
//
//    @GET
//    @Path("{id}")
//    @Produces("application/xml")
//    public StreamingOutput getCollectionObject(@PathParam("id") int id) {
//        final CollectionObject c = CollectionObjectDB.get(id);
//        if (c == null) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//        return new StreamingOutput() {
//
//            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
//                outputCollectionObject(outputStream, c);
//            }
//        };
//    }
//
//    @PUT
//    @Path("{id}")
//    @Consumes("application/xml")
//    @Produces("application/xml")
//    public StreamingOutput updateCollectionObject(@PathParam("id") int id, InputStream is) {
//        CollectionObject update = readCollectionObject(is);
//        CollectionObject current = CollectionObjectDB.get(id);
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
//        final CollectionObject scurrent = current;
//        return new StreamingOutput() {
//
//            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
//                outputCollectionObject(outputStream, scurrent);
//            }
//        };
//    }
//
//    protected void outputCollectionObject(OutputStream os, CollectionObject c) throws IOException {
//        PrintStream writer = new PrintStream(os);
//        writer.println("<CollectionObject id=\"" + c.getId() + "\" version=\"" + c.getVersion() + "\">");
//        writer.println("   <first-name>" + c.getFirstName() + "</first-name>");
//        writer.println("   <last-name>" + c.getLastName() + "</last-name>");
//        writer.println("   <street>" + c.getStreet() + "</street>");
//        writer.println("   <city>" + c.getCity() + "</city>");
//        writer.println("   <state>" + c.getState() + "</state>");
//        writer.println("   <zip>" + c.getZip() + "</zip>");
//        writer.println("   <country>" + c.getCountry() + "</country>");
//        writer.println("</CollectionObject>");
//    }
//
//    protected CollectionObject readCollectionObject(InputStream is) {
//        try {
//            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            Document doc = builder.parse(is);
//            Element root = doc.getDocumentElement();
//            CollectionObject c = new CollectionObject();
//            if (root.getAttribute("id") != null && !root.getAttribute("id").trim().equals("")) {
//                c.setId(Integer.valueOf(root.getAttribute("id")));
//            }
//            if (root.getAttribute("version") != null && !root.getAttribute("version").trim().equals("")) {
//                c.setVersion(Integer.valueOf(root.getAttribute("version")));
//            }
//            NodeList nodes = root.getChildNodes();
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Element element = (Element) nodes.item(i);
//                if (element.getTagName().equals("first-name")) {
//                    c.setFirstName(element.getTextContent());
//                } else if (element.getTagName().equals("last-name")) {
//                    c.setLastName(element.getTextContent());
//                } else if (element.getTagName().equals("street")) {
//                    c.setStreet(element.getTextContent());
//                } else if (element.getTagName().equals("city")) {
//                    c.setCity(element.getTextContent());
//                } else if (element.getTagName().equals("state")) {
//                    c.setState(element.getTextContent());
//                } else if (element.getTagName().equals("zip")) {
//                    c.setZip(element.getTextContent());
//                } else if (element.getTagName().equals("country")) {
//                    c.setCountry(element.getTextContent());
//                }
//            }
//            return c;
//        } catch (Exception e) {
//            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
//        }
//    }
}
