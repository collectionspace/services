package org.collectionspace.hello.services;

import java.io.ByteArrayInputStream;
import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.*;


import org.collectionspace.hello.People.PeopleItem;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/persons")
@Consumes("application/xml")
@Produces("application/xml")
public class PersonNuxeoResource {

    final Logger logger = LoggerFactory.getLogger(PersonNuxeoResource.class);

    public PersonNuxeoResource() {
    }

    @GET
    public People getPeople(@Context UriInfo ui) {
        People p = new People();
        try{
            List<People.PeopleItem> list = p.getPeopleItem();
            NuxeoRESTClient nxClient = getClient();

            List<String> pathParams = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();
            //browse default repository for People
            //For sanjay, People repository id is f084243e-4b81-42a1-9a05-518e974facbd
            //For Richard, workspace repos ID is 77187c27-0467-4c3d-b395-122b82113f4d
            pathParams = Arrays.asList("default", "1b58eef7-4fff-430b-b773-8c98724f19de", "browse");
            Representation res = nxClient.get(pathParams, queryParams);
            SAXReader reader = new SAXReader();
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                PeopleItem pli = new PeopleItem();
                pli.setTitle(element.attributeValue("title"));
                pli.setUri(element.attributeValue("url"));
                pli.setId(element.attributeValue("id"));
                list.add(pli);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return p;
    }

    @POST
    public Response createPerson(PersonNuxeo p) {

        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add("1b58eef7-4fff-430b-b773-8c98724f19de");
        pathParams.add("createDocument");
        queryParams.put("docType", "Hello");
        queryParams.put("dublincore:title", p.getFirstName() + " " + p.getLastName());
        queryParams.put("hello:cversion", Integer.valueOf(1).toString());
        queryParams.put("hello:firstName", p.getFirstName());
        queryParams.put("hello:lastName", p.getLastName());
        queryParams.put("hello:street", p.getStreet());
        queryParams.put("hello:city", p.getCity());
        queryParams.put("hello:state", p.getState());
        queryParams.put("hello:zip", p.getZip());
        queryParams.put("hello:country", p.getCountry());
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Representation res = nxClient.post(pathParams, queryParams, bais);

        SAXReader reader = new SAXReader();
        try{
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

        verbose("created person", p);
        UriBuilder path = UriBuilder.fromResource(PersonNuxeoResource.class);
        path.path("" + p.getId());
        Response response = Response.created(path.build()).build();
        return response;
    }

    @GET
    @Path("{id}")
    public PersonNuxeo getPerson(@PathParam("id") String id) {

        PersonNuxeo p = null;
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
            p = new PersonNuxeo();
            //TODO: recognize schema thru namespace uri
//            Namespace ns = new Namespace("hello", "http://collectionspace.org/hello");
            Iterator<Element> siter = root.elementIterator("schema");
            while(siter.hasNext()){

                Element s = siter.next();
                
                System.err.println("PersonNuxeo.getPerson() called.");

                //TODO: recognize schema thru namespace uri
                if("hello".equals(s.attribute("name").getValue())){
                    p.setId(id);
                    Element ele = s.element("cversion");
                    if(ele != null){
                        p.setVersion((String) ele.getData());
                    }
                    ele = s.element("firstName");
                    if(ele != null){
                        p.setFirstName((String) ele.getData());
                    }
                    ele = s.element("lastName");
                    if(ele != null){
                        p.setLastName((String) ele.getData());
                    }
                    ele = s.element("city");
                    if(ele != null){
                        p.setCity((String) ele.getData());
                    }
                    ele = s.element("state");
                    if(ele != null){
                        p.setState((String) ele.getData());
                    }
                    ele = s.element("zip");
                    if(ele != null){
                        p.setZip((String) ele.getData());
                    }
                    ele = s.element("country");
                    if(ele != null){
                        p.setCountry((String) ele.getData());
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if(p == null){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested person ID:" + id + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        verbose("get person", p);
        return p;
    }

    @PUT
    @Path("{id}")
    public PersonNuxeo updatePerson(
            @PathParam("id") String id,
            PersonNuxeo update) {

        verbose("updating person input", update);

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

    private void verbose(String msg, PersonNuxeo p) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    PersonNuxeo.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(p, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

//    private void getQueryModel() throws IOException {
//        NuxeoRESTClient nxClient = getClient();
//
//        List<String> pathParams = new ArrayList<String>();
//        Map<String, String> queryParams = new HashMap<String, String>();
//
//        //query model for user documents
//        pathParams = Arrays.asList("execQueryModel", "USER_DOCUMENTS");
//        queryParams.put("QP1", "Administrator");
//        queryParams.put("format", "XML");
//
//
//        Representation res = nxClient.get(pathParams, queryParams);
//        String resStr = res.getText();
//        verbose("getQueryModel:" + resStr);
//
//    }
//
//    private void getVocabulary() throws IOException {
//        NuxeoRESTClient nxClient = getClient();
//
//        List<String> pathParams = new ArrayList<String>();
//        Map<String, String> queryParams = new HashMap<String, String>();
//        //get vocabulary
//        pathParams = Arrays.asList("vocabulary", "continent_country");
//        queryParams.put("lang", "en");
//
//        Representation res = nxClient.get(pathParams, queryParams);
//        String resStr = res.getText();
//        verbose("getVocabulary:" + resStr);
//
//    }
    private NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient("http://127.0.0.1:8080/nuxeo");
        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");
        return nxClient;
    }

    private void verbose(String msg) {
        System.out.println("PersonNuxeoResource: " + msg);
    }
}
