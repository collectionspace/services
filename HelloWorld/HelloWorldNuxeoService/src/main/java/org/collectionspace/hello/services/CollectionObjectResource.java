package org.collectionspace.hello.services;

import java.io.ByteArrayInputStream;
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


import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;
import org.collectionspace.hello.CollectionObjectList.CollectionObjectListItem;
import org.collectionspace.hello.services.CollectionObjectJAXBSchema;
import org.collectionspace.hello.services.CollectionObjectListItemJAXBSchema;

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
public class CollectionObjectResource implements CollectionSpaceResource {

	final static String CO_NUXEO_DOCTYPE = "CollectionObject";
	final static String CO_NUXEO_SCHEMA_NAME = "collectionobject";
	final static String CO_NUXEO_DC_TITLE = "CollectionSpace-CollectionObject";
	
    final Logger logger = LoggerFactory.getLogger(CollectionObjectResource.class);

    public CollectionObjectResource() {
    	// do nothing
    }

    @GET
    public CollectionObjectList getCollectionObjectList(@Context UriInfo ui) {
    	CollectionObjectList p = new CollectionObjectList();
        try{
            NuxeoRESTClient nxClient = getClient();

            List<String> pathParams = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();
            pathParams = Arrays.asList("default", CS_NUXEO_WORKSPACE_UID, "browse");
            Representation res = nxClient.get(pathParams, queryParams);
            SAXReader reader = new SAXReader();
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            
            System.err.println(res.toString());
            System.err.println(document.toString());

            List<CollectionObjectList.CollectionObjectListItem> list = p.getCollectionObjectListItem();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();

                // set the CollectionObject list item entity elements                
                CollectionObjectListItem pli = new CollectionObjectListItem();
                pli.setObjectNumber(element.attributeValue("title"));
                pli.setUri(element.attributeValue("url"));
                pli.setCsid(element.attributeValue("id"));
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
        pathParams.add(CS_NUXEO_WORKSPACE_UID);
        pathParams.add("createDocument");
        queryParams.put("docType", CO_NUXEO_DOCTYPE);
        
        // a default title for the Dublin Core schema
        queryParams.put("dublincore:title", CO_NUXEO_DC_TITLE);
        
        // CollectionObject core values
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.OBJECT_NUMBER, 
        		co.getObjectNumber());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.OTHER_NUMBER, 
        		co.getOtherNumber());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.BRIEF_DESCRIPTION,
        		co.getBriefDescription());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.COMMENTS,
        		co.getComments());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.DIST_FEATURES,
        		co.getDistFeatures());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.OBJECT_NAME,
        		co.getObjectName());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.RESPONSIBLE_DEPT,
        		co.getResponsibleDept());
        queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.TITLE,
        		co.getTitle());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        Representation res = nxClient.post(pathParams, queryParams, bais);

        String csid = null;
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if ("docRef".equals(element.getName())){
                    csid = (String) element.getData();
                    co.setCsid(csid);
                }
            }
        } catch(Exception e){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }

        
        verbose("createCollectionObject: ", co);
        UriBuilder path = UriBuilder.fromResource(PersonNuxeoResource.class);
        path.path("" + csid);
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
                if (CO_NUXEO_SCHEMA_NAME.equals(schemaElement.attribute("name").getValue())){
                    Element ele = schemaElement.element(CollectionObjectJAXBSchema.OBJECT_NUMBER);
                    if(ele != null){
                        co.setObjectNumber((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.OTHER_NUMBER);
                    if(ele != null){
                        co.setOtherNumber((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.BRIEF_DESCRIPTION);
                    if(ele != null){
                        co.setBriefDescription((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.COMMENTS);
                    if(ele != null){
                        co.setComments((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.DIST_FEATURES);
                    if(ele != null){
                        co.setDistFeatures((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.OBJECT_NAME);
                    if(ele != null){
                        co.setObjectName((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.RESPONSIBLE_DEPT);
                    if(ele != null){
                        co.setResponsibleDept((String) ele.getData());
                    }
                    ele = schemaElement.element(CollectionObjectJAXBSchema.TITLE);
                    if(ele != null){
                        co.setTitle((String) ele.getData());
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
        verbose("getCollectionObject: ", co);
        
        return co;
    }

    @PUT
    @Path("{csid}")
    public CollectionObject updateCollectionObject(
            @PathParam("csid") String csid,
            CollectionObject theUpdate) {

        verbose("updateCollectionObject with input: ", theUpdate);

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        pathParams.add("default");
        pathParams.add(csid);
        pathParams.add("updateDocumentRestlet");
        
        //todo: intelligent merge needed
        if(theUpdate.getObjectNumber() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.OBJECT_NUMBER, 
            		theUpdate.getObjectNumber());
        }

        if(theUpdate.getOtherNumber() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.OTHER_NUMBER, 
            		theUpdate.getOtherNumber());
        }

        if(theUpdate.getBriefDescription()!= null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.BRIEF_DESCRIPTION, 
            		theUpdate.getBriefDescription());
        }

        if(theUpdate.getComments() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.COMMENTS, 
            		theUpdate.getComments());
        }

        if(theUpdate.getDistFeatures() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.DIST_FEATURES, 
            		theUpdate.getDistFeatures());
        }

        if(theUpdate.getObjectName() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.OBJECT_NAME, 
            		theUpdate.getObjectName());
        }

        if(theUpdate.getResponsibleDept() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.RESPONSIBLE_DEPT, 
            		theUpdate.getResponsibleDept());
        }

        if(theUpdate.getTitle() != null){
            queryParams.put(CO_NUXEO_SCHEMA_NAME + ":" + CollectionObjectJAXBSchema.TITLE, 
            		theUpdate.getTitle());
        }

        NuxeoRESTClient nxClient = getClient();
        Representation res = nxClient.get(pathParams, queryParams);
        SAXReader reader = new SAXReader();
        String status = null;
        try {
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for(Iterator i = root.elementIterator(); i.hasNext();){
                Element element = (Element) i.next();
                if("docRef".equals(element.getName())){
                    status = (String) element.getData();
                    verbose("updateCollectionObject response: " + status);
                }
            }
        } catch(Exception e) {
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        
        return theUpdate;
    }

    @DELETE
    @Path("{csid}")
    public void deleteCollectionObject(@PathParam("csid") String csid) {

    	verbose("deleteCollectionObject with csid=" + csid);
        
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
                    verbose("deleteCollectionObjectt response: " + status);
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
        System.out.println("CollectionObjectResource. " + msg);
    }
}
