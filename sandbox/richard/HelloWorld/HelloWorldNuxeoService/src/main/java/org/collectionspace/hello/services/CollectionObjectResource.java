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


import org.collectionspace.hello.CollectionObjectList.CollectionObjectListItem;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/collectionobjects")
@Consumes("application/xml")
@Produces("application/xml")
public class CollectionObjectResource {

	final static String NUXEO_WORKSPACE_UID = "776a8787-9d81-41b0-a02c-1ba674638c0a";
	final static String NUXEO_DOCTYPE = "CollectionObject";
	
    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);

    public CollectionObjectResource() {
    	// do nothing
    }

    @GET
    public CollectionObjectList getCollectionObjectList(@Context UriInfo ui) {
    	CollectionObjectList p = new CollectionObjectList();
        try{
            List<CollectionObjectList.CollectionObjectListItem> list = p.getCollectionObjectListItem();
            NuxeoRESTClient nxClient = getClient();

            List<String> pathParams = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();
            pathParams = Arrays.asList("default", NUXEO_WORKSPACE_UID, "browse");
            Representation res = nxClient.get(pathParams, queryParams);
            SAXReader reader = new SAXReader();
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                CollectionObjectListItem pli = new CollectionObjectListItem();
                //
                pli.setCsid(element.attributeValue("csid"));
                pli.setUri(element.attributeValue("url"));
                pli.setIdentifier(element.attributeValue("identifier"));
                list.add(pli);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return p;
    }

    @POST
    public Response createCollectionObject(CollectionObject co) {

        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add(NUXEO_WORKSPACE_UID);
        pathParams.add("createDocument");
        queryParams.put("docType", NUXEO_DOCTYPE);
        
        queryParams.put("dublincore:title", co.getIdentifier());
        // CollectionObject core values
        queryParams.put("collectionobject:csid", Integer.valueOf(1).toString());
        queryParams.put("collectionobject:identifier", co.getIdentifier());
        queryParams.put("collectionobject:description", co.getDescription());

        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Representation res = nxClient.post(pathParams, queryParams, bais);

        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if ("docRef".equals(element.getName())){
                    String id = (String) element.getData();
                    co.setCsid(id);
                }
            }
        } catch(Exception e){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        verbose("created collectionobject", co);
        UriBuilder path = UriBuilder.fromResource(PersonNuxeoResource.class);
        path.path("" + co.getCsid());
        Response response = Response.created(path.build()).build();
        
        return response;
    }

    @GET
    @Path("{csid}")
    public CollectionObject getCollectionObject(@PathParam("csid") String csid) {

        CollectionObject co = null;
        try {
            List<String> pathParams = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();

            pathParams.add("default");
            pathParams.add(csid);
            pathParams.add("export");
            queryParams.put("format", "XML");

            NuxeoRESTClient nxClient = getClient();
            Representation res = nxClient.get(pathParams, queryParams);

            SAXReader reader = new SAXReader();
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            co = new CollectionObject();

 //			TODO: recognize schema thru namespace uri
//          Namespace ns = new Namespace("collectionobject", "http://collectionspace.org/collectionobject");

            Iterator<Element> siter = root.elementIterator("schema");
            while (siter.hasNext()) {

                Element schemaElement = siter.next();
                System.err.println("CollectionObject.getCollectionObject() called.");

                //TODO: recognize schema thru namespace uri
                if ("collectionobject".equals(schemaElement.attribute("name").getValue())){
                    co.setCsid(csid);
                    Element ele = schemaElement.element("identifier");
                    if(ele != null){
                        co.setIdentifier((String) ele.getData());
                    }
                    ele = schemaElement.element("description");
                    if(ele != null){
                        co.setDescription((String) ele.getData());
                    }
                }
            }

        } catch(Exception e){
            e.printStackTrace();
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        if (co == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested CollectionObject CSID:" + csid + ": was not found.").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        verbose("get collectionobject", co);
        
        return co;
    }

    @PUT
    @Path("{csid}")
    public CollectionObject updateCollectionObject(
            @PathParam("csid") String csid,
            CollectionObject update) {

        verbose("updating collectionobject input", update);

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add(update.getCsid());
        pathParams.add("updateDocumentRestlet");
        
        //todo: intelligent merge needed
        if(update.getIdentifier() != null){
            queryParams.put("collectionobject:identifier", update.getIdentifier());
        }

        if(update.getDescription() != null){
            queryParams.put("collectionobject:description", update.getDescription());
        }

        NuxeoRESTClient nxClient = getClient();
        Representation res = nxClient.get(pathParams, queryParams);
        SAXReader reader = new SAXReader();
        String status = "";
        try {
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if("docRef".equals(element.getName())){
                    status = (String) element.getData();
                    verbose("update collectionobject: response=" + status);
                }

            }
        } catch(Exception e) {
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        
        return update;
    }

    @DELETE
    @Path("{csid}")
    public void deleteCollectionObject(@PathParam("csid") String csid) {

    	verbose("deleting collectionobject with csid=" + csid);
        
    	NuxeoRESTClient nxClient = getClient();
        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();

        pathParams.add("default");
        pathParams.add(csid);
        pathParams.add("deleteDocumentRestlet");
        Representation res = nxClient.get(pathParams, queryParams);
        SAXReader reader = new SAXReader();
        String status = "";
        
        try {
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if("docRef".equals(element.getName())){
                    status = (String) element.getData();
                    verbose("delete collectionobject: response=" + status);
                }

            }
        }catch(Exception e){
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }

    }

    private void verbose(String msg, CollectionObject co) {
        try {
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(
                    CollectionObject.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(co, System.out);
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    private NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient("http://127.0.0.1:8080/nuxeo");
        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");
        return nxClient;
    }

    private void verbose(String msg) {
        System.out.println("CollectionObjectResource: " + msg);
    }
}
