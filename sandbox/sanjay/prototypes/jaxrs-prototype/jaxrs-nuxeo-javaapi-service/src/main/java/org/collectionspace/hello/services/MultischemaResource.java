package org.collectionspace.hello.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

import org.collectionspace.hello.PersonNuxeo;

import org.collectionspace.services.nuxeo.NuxeoConnector;
import org.collectionspace.world.DublincoreNuxeo;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;


import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.DocumentModel;


import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.client.NuxeoClient;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.SingleDocumentReader;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDocumentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/multischema")
@Consumes("application/xml")
@Produces("application/xml")
public class MultischemaResource extends CollectionSpaceResource {

    final Logger logger = LoggerFactory.getLogger(MultischemaResource.class);

    public MultischemaResource() {
    }

    @POST
    @Consumes("multipart/form-data")
    public Response createPerson(MultipartFormDataInput multipart) {

        PersonNuxeo personPart = new PersonNuxeo();
        DublincoreNuxeo dcPart = new DublincoreNuxeo();

        RepositoryInstance repoSession = null;
        try{
            if(multipart.getFormData().containsKey("dublincore")){
                dcPart = multipart.getFormDataPart("dublincore", DublincoreNuxeo.class, null);
            }
            if(multipart.getFormData().containsKey("hello")){
                personPart = multipart.getFormDataPart("hello", PersonNuxeo.class, null);
            }

            repoSession = getRepositorySession();
            DocumentRef nuxeoWspace = new IdRef(CS_PERSON_WORKSPACE_UID);
            DocumentModel wspacePeople = repoSession.getDocument(nuxeoWspace);
            String wspacePath = wspacePeople.getPathAsString();
            String docType = "Hello";
            String id = IdUtils.generateId("New " + docType);
            //create document model
            DocumentModel helloDoc = repoSession.createDocumentModel(wspacePath, id, docType);
            fillDocument(personPart, helloDoc);
            //create document with documentmodel
            helloDoc = repoSession.createDocument(helloDoc);
            repoSession.save();

            personPart.setId(helloDoc.getId());

        }catch(Exception e){
            e.printStackTrace();
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Create failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }

        if(logger.isDebugEnabled()){
            verboseObject("createPerson: person", PersonNuxeo.class, personPart);
            verboseObject("createPerson: dublincore", DublincoreNuxeo.class, dcPart);
        }
        UriBuilder path = UriBuilder.fromResource(MultischemaResource.class);

        path.path("" + personPart.getId());
        Response response = Response.created(path.build()).build();
        return response;
    }

    @GET
    @Path("{id}")
    @Produces("multipart/form-data")
    public MultipartFormDataOutput getPerson(
            @PathParam("id") String id) {

        PersonNuxeo personPart = new PersonNuxeo();
        DublincoreNuxeo dublinPart = new DublincoreNuxeo();
        MultipartFormDataOutput output = new MultipartFormDataOutput();
        RepositoryInstance repoSession = null;

        try{
            repoSession = getRepositorySession();
            DocumentRef helloDocRef = new IdRef(id);
            DocumentModel helloDoc = repoSession.getDocument(helloDocRef);
            if(helloDoc == null){
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                        "Get failed, the requested person ID:" + id + ": was not found.").type("text/plain").build();
                throw new WebApplicationException(response);
            }
            Document doc = getDocument(repoSession, helloDoc);
            Element root = doc.getRootElement();
            //TODO: recognize schema thru namespace uri
            //Namespace ns = new Namespace("hello", "http://collectionspace.org/hello");
            Iterator<Element> siter = root.elementIterator("schema");
            while(siter.hasNext()){

                Element s = siter.next();

                //TODO: recognize schema thru namespace uri
                if("hello".equals(s.attribute("name").getValue())){
                    personPart.setId(id);
                    Element ele = s.element("cversion");
                    if(ele != null){
                        personPart.setVersion((String) ele.getData());
                    }
                    ele = s.element("firstName");
                    if(ele != null){
                        personPart.setFirstName((String) ele.getData());
                    }
                    ele = s.element("lastName");
                    if(ele != null){
                        personPart.setLastName((String) ele.getData());
                    }
                    ele = s.element("city");
                    if(ele != null){
                        personPart.setCity((String) ele.getData());
                    }
                    ele = s.element("state");
                    if(ele != null){
                        personPart.setState((String) ele.getData());
                    }
                    ele = s.element("zip");
                    if(ele != null){
                        personPart.setZip((String) ele.getData());
                    }
                    ele = s.element("country");
                    if(ele != null){
                        personPart.setCountry((String) ele.getData());
                    }
                }else if("dublincore".equals(s.attribute("name").getValue())){
                    Element ele = s.element("title");
                    if(ele != null){
                        dublinPart.setTitle((String) ele.getData());
                    }
                }
            }//while
            if(logger.isDebugEnabled()){
                verboseObject("getPerson:hello:", PersonNuxeo.class, personPart);
                verboseObject("getPerson:dublincore:", DublincoreNuxeo.class, dublinPart);
            }
            output.addFormData("hello", personPart, MediaType.APPLICATION_XML_TYPE);
            output.addFormData("dublincore", dublinPart, MediaType.APPLICATION_XML_TYPE);

        }catch(Exception e){
            if(e instanceof WebApplicationException){
                throw (WebApplicationException) e;
            }
            if(logger.isDebugEnabled()){
                e.printStackTrace();
            }
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "Get failed").type("text/plain").build();
            throw new WebApplicationException(response);
        }finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }
        return output;
    }

    @PUT
    @Path("{id}")
    public PersonNuxeo updatePerson(
            @PathParam("id") String id,
            PersonNuxeo personPart) {
        if(logger.isDebugEnabled()){
            verboseObject("updating person input", PersonNuxeo.class, personPart);
        }
        RepositoryInstance repoSession = null;
        try{
            repoSession = getRepositorySession();
            DocumentRef helloDocRef = new IdRef(id);
            DocumentModel helloDoc = repoSession.getDocument(helloDocRef);
            if(helloDoc == null){
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                        "Get failed, the requested person ID:" + id + ": was not found.").type("text/plain").build();
                throw new WebApplicationException(response);
            }
            fillDocument(personPart, helloDoc);
            repoSession.saveDocument(helloDoc);
            repoSession.save();
        }catch(Exception e){
            if(e instanceof WebApplicationException){
                throw (WebApplicationException) e;
            }
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Update failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }
        return personPart;
    }

    @DELETE
    @Path("{id}")
    public void deletePerson(@PathParam("id") String id) {
        if(logger.isDebugEnabled()){
            logger.debug("deleting person with id=" + id);
        }
        RepositoryInstance repoSession = null;
        try{
            repoSession = getRepositorySession();
            DocumentRef helloDocRef = new IdRef(id);
            repoSession.removeDocument(helloDocRef);
            repoSession.save();
        }catch(Exception e){
            //FIXME: NOT_FOUND?
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Delete failed ").type("text/plain").build();
            throw new WebApplicationException(response);
        }finally{
            if(repoSession != null){
                releaseRepositorySession(repoSession);
            }
        }
    }

    private RepositoryInstance getRepositorySession() throws Exception {
        NuxeoConnector nuxeoConnector = NuxeoConnector.getInstance();
        nuxeoConnector.initialize();
        NuxeoClient client = nuxeoConnector.getClient();
        //FIXME: is it possible to reuse repository session?
        //Authentication failures happen while trying to reuse the session
        RepositoryInstance repoSession = client.openRepository();
        if(logger.isDebugEnabled()){
            logger.debug("getRepository() repository root: " +
                    repoSession.getRootDocument());
        }
        return repoSession;
    }

    private void releaseRepositorySession(RepositoryInstance repoSession) {
        try{
            //release session
            NuxeoClient.getInstance().releaseRepository(repoSession);
        }catch(Exception e){
            logger.error("Could not close the repository session", e);
        //no need to throw this service specific exception
        }
    }

    private Document getDocument(RepositoryInstance repoSession, DocumentModel helloDoc)
            throws Exception {
        Document doc = null;
        DocumentWriter writer = null;
        DocumentReader reader = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try{
            baos = new ByteArrayOutputStream();
            //nuxeo io.impl begin
            reader = new SingleDocumentReader(repoSession, helloDoc);
            writer = new XMLDocumentWriter(baos);
            DocumentPipe pipe = new DocumentPipeImpl();
            //nuxeo io.impl end
            pipe.setReader(reader);
            pipe.setWriter(writer);
            pipe.run();
            bais = new ByteArrayInputStream(baos.toByteArray());
            SAXReader saxReader = new SAXReader();
            doc = saxReader.read(bais);
        }finally{
            if(reader != null){
                reader.close();
            }
            if(writer != null){
                writer.close();
            }
            try{
                if(bais != null){
                    bais.close();
                }
                if(baos != null){
                    baos.close();
                }
            }catch(IOException ioe){
                logger.error("Failed to close io streams with {}", ioe);
                throw new WebApplicationException();
            }
        }
        return doc;
    }

    private void fillDocument(PersonNuxeo p, DocumentModel helloDoc) throws Exception {
        if(p.getFirstName() != null){
            helloDoc.setPropertyValue("dublincore:title", p.getFirstName() + " " + p.getLastName());
            helloDoc.setPropertyValue("hello:firstName", p.getFirstName());
        }
        if(p.getLastName() != null){
            helloDoc.setPropertyValue("hello:lastName", p.getLastName());
        }
        if(p.getStreet() != null){
            helloDoc.setPropertyValue("hello:street", p.getStreet());
        }
        if(p.getCity() != null){
            helloDoc.setPropertyValue("hello:city", p.getCity());
        }
        if(p.getState() != null){
            helloDoc.setPropertyValue("hello:state", p.getState());
        }
        if(p.getZip() != null){
            helloDoc.setPropertyValue("hello:zip", p.getZip());
        }
        if(p.getCountry() != null){
            helloDoc.setPropertyValue("hello:country", p.getCountry());
        }
    }

    private void verboseObject(String msg, Class klass, Object obj) {
        try{
            if(logger.isDebugEnabled()){
                logger.debug(msg);
            }
            JAXBContext jc = JAXBContext.newInstance(klass);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(obj, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
