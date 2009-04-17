package org.collectionspace.hello.services;

import java.io.ByteArrayInputStream;
import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.*;

import org.collectionspace.world.DublincoreNuxeo;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/multipart")
@Consumes("application/xml")
@Produces("application/xml")
public class MultipartResource extends CollectionSpaceResource {

    final Logger logger = LoggerFactory.getLogger(MultipartResource.class);

    public MultipartResource() {
    }

    @POST
    @Consumes("multipart/form-data")
    public Response createPerson(MultipartFormDataInput multipart) {

        PersonNuxeo p = new PersonNuxeo();
        DublincoreNuxeo dc = new DublincoreNuxeo();
        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add(CS_PERSON_WORKSPACE_UID);
        pathParams.add("createDocument");
        queryParams.put("docType", "Hello");

        try{
            if(multipart.getFormData().containsKey("dublincore")){
                dc = multipart.getFormDataPart("dublincore", DublincoreNuxeo.class, null);
                if(dc.getTitle() != null){
                    queryParams.put("dublincore:title", dc.getTitle());
                }
            }
            if(multipart.getFormData().containsKey("hello")){
                p = multipart.getFormDataPart("hello", PersonNuxeo.class, null);
                queryParams.put("hello:cversion", Integer.valueOf(1).toString());
                if(p.getFirstName() != null){
                    queryParams.put("hello:firstName", p.getFirstName());
                }
                if(p.getLastName() != null){
                    queryParams.put("hello:lastName", p.getLastName());
                }
                if(p.getStreet() != null){
                    queryParams.put("hello:street", p.getStreet());
                }
                if(p.getCity() != null){
                    queryParams.put("hello:city", p.getCity());
                }
                if(p.getState() != null){
                    queryParams.put("hello:state", p.getState());
                }
                if(p.getZip() != null){
                    queryParams.put("hello:zip", p.getZip());
                }
                if(p.getCountry() != null){
                    queryParams.put("hello:country", p.getCountry());
                }
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
            Representation res = nxClient.post(pathParams, queryParams, bais);

            SAXReader reader = new SAXReader();

            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if("docRef".equals(element.getName())){
                    String id = (String) element.getData();
                    p.setId(id);
                }
            }
        }catch(Exception e){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        verbosePerson("createPerson: person", p);
        verboseDublin("createPerson: dublincore", dc);
        UriBuilder path = UriBuilder.fromResource(MultipartResource.class);
        path.path("" + p.getId());
        Response response = Response.created(path.build()).build();
        return response;
    }

    @GET
    @Path("{id}")
    @Produces("multipart/form-data")
    public MultipartFormDataOutput getPerson(@PathParam("id") String id) {

        PersonNuxeo person = new PersonNuxeo();
        DublincoreNuxeo dublin = new DublincoreNuxeo();
        MultipartFormDataOutput output = new MultipartFormDataOutput();

        try{
            NuxeoRESTClient nxClient = getClient();

            List<String> pathParams = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();

            pathParams.add("default");
            pathParams.add(id);
            pathParams.add("export");
            queryParams.put("format", "XML");

            Representation res = nxClient.get(pathParams, queryParams);
            SAXReader reader = new SAXReader();

            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();

            //TODO: recognize schema thru namespace uri
//            Namespace ns = new Namespace("hello", "http://collectionspace.org/hello");
            Iterator<Element> siter = root.elementIterator("schema");
            while(siter.hasNext()){

                Element s = siter.next();

                //TODO: recognize schema thru namespace uri
                if("hello".equals(s.attribute("name").getValue())){
                    person.setId(id);
                    Element ele = s.element("cversion");
                    if(ele != null){
                        person.setVersion((String) ele.getData());
                    }
                    ele = s.element("firstName");
                    if(ele != null){
                        person.setFirstName((String) ele.getData());
                    }
                    ele = s.element("lastName");
                    if(ele != null){
                        person.setLastName((String) ele.getData());
                    }
                    ele = s.element("city");
                    if(ele != null){
                        person.setCity((String) ele.getData());
                    }
                    ele = s.element("state");
                    if(ele != null){
                        person.setState((String) ele.getData());
                    }
                    ele = s.element("zip");
                    if(ele != null){
                        person.setZip((String) ele.getData());
                    }
                    ele = s.element("country");
                    if(ele != null){
                        person.setCountry((String) ele.getData());
                    }
                }else if("dublincore".equals(s.attribute("name").getValue())){
                    Element ele = s.element("title");
                    if(ele != null){
                        dublin.setTitle((String) ele.getData());
                    }
                }
            }//while
            verbosePerson("getPerson:hello:", person);
            output.addFormData("hello", person, MediaType.APPLICATION_XML_TYPE);
            verboseDublin("getPerson:dublincore:", dublin);
            output.addFormData("dublincore", dublin, MediaType.APPLICATION_XML_TYPE);

        }catch(Exception e){
            e.printStackTrace();
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if(person == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested person ID:" + id + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        return output;
    }

    @PUT
    @Path("{id}")
    public PersonNuxeo updatePerson(
            @PathParam("id") String id,
            PersonNuxeo update) {

        verbosePerson("updating person input", update);

        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add(update.getId());
        pathParams.add("updateDocumentRestlet");
        queryParams.put("dublincore:title", "change title");
        //todo: intelligent merge needed
        if(update.getFirstName() != null){
            queryParams.put("hello:firstName", update.getFirstName());
        }

        if(update.getLastName() != null){
            queryParams.put("hello:lastName", update.getLastName());
        }

        if(update.getFirstName() != null && update.getLastName() != null){
            queryParams.put("dublincore:title", update.getFirstName() + " " + update.getLastName());
        }

        if(update.getStreet() != null){
            queryParams.put("hello:street", update.getStreet());
        }

        if(update.getCity() != null){
            queryParams.put("hello:city", update.getCity());
        }

        if(update.getState() != null){
            queryParams.put("hello:state", update.getState());
        }

        if(update.getZip() != null){
            queryParams.put("hello:zip", update.getZip());
        }

        if(update.getCountry() != null){
            queryParams.put("hello:country", update.getCountry());
        }

        Representation res = nxClient.get(pathParams, queryParams);
        SAXReader reader = new SAXReader();
        String status = "";
        try{
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if("docRef".equals(element.getName())){
                    status = (String) element.getData();
                    verbose("updatePerson: response=" + status);
                }

            }
        }catch(Exception e){
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return update;
    }

    @DELETE
    @Path("{id}")
    public void deletePerson(@PathParam("id") String id) {
        verbose("deleting person with id=" + id);
        NuxeoRESTClient nxClient = getClient();
        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add(id);
        pathParams.add("deleteDocumentRestlet");
        Representation res = nxClient.get(pathParams, queryParams);
        SAXReader reader = new SAXReader();
        String status = "";
        try{
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if("docRef".equals(element.getName())){
                    status = (String) element.getData();
                    verbose("deletePerson: response=" + status);
                }

            }
        }catch(Exception e){
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    private void verbosePerson(String msg, PersonNuxeo person) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    PersonNuxeo.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(person, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void verboseDublin(String msg, DublincoreNuxeo dubin) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    DublincoreNuxeo.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(dubin, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private void verbose(String msg) {
        System.out.println("MultipartResource: " + msg);
    }
}
