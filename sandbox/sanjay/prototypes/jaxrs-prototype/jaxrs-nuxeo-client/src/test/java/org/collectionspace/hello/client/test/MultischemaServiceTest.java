package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.PersonNuxeo;
import org.collectionspace.hello.client.MultischemaClient;
import org.collectionspace.world.DublincoreNuxeo;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A MultipartTest.
 * 
 * @version $Revision:$
 */
public class MultischemaServiceTest {

    private MultischemaClient client = MultischemaClient.getInstance();
    private String updateId = "";
    private String deleteId = "";
    final Logger logger = LoggerFactory.getLogger(MultischemaServiceTest.class);
    @Test
    public void createPerson() {
        MultipartFormDataOutput multipartPerson = createPerson("Mr.", "Chris", "Hoffman");
        ClientResponse<Response> res = client.createPerson(multipartPerson);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        //store updateId locally
        updateId = extractId(res);
    }

    @Test
    public void createTeam() {
        MultipartFormDataOutput multipartPerson = createPerson("Mr.", "Sanjay", "Dalal");
        ClientResponse<Response> res = client.createPerson(multipartPerson);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        deleteId = extractId(res);

        multipartPerson = createPerson("Mr.", "Aron", "Roberts");
        res = client.createPerson(multipartPerson);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());

        multipartPerson = createPerson("Mr.", "Richard", "Millet");
        res = client.createPerson(multipartPerson);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createPerson"})
    public void updatePerson() throws Exception {
        MultipartFormDataInput mdip = client.getPerson(updateId).getEntity();
        PersonNuxeo touPerson = mdip.getFormDataPart("hello", PersonNuxeo.class, null);
        touPerson.setId(updateId);
        verbose("got person to update", touPerson, PersonNuxeo.class);
        touPerson.setFirstName("Patrick");
        touPerson.setLastName("Schmitz");
        PersonNuxeo uPerson = client.updatePerson(updateId, touPerson).getEntity();
        verbose("updated person", uPerson, PersonNuxeo.class);
        //Assert.assertNotSame(uPerson.getVersion(), initialVersion);
        Assert.assertEquals(uPerson.getFirstName(), "Patrick");
    }

    @Test(dependsOnMethods = {"createTeam"})
    public void deletePerson() {
        ClientResponse<Response> res = client.deletePerson(deleteId);
        verbose("deletePerson: id=" + deleteId);
        verbose("deletePerson: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    private MultipartFormDataOutput createPerson(String title, String firstName, String lastName) {
        PersonNuxeo person = new PersonNuxeo();

        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setStreet("2195 Hearst Ave.");
        person.setCity("Berkeley");
        person.setState("CA");
        person.setZip("94704");
        person.setCountry("US");
        person.setVersion("1.0");

        DublincoreNuxeo dublin = new DublincoreNuxeo();
        dublin.setTitle(title);
        MultipartFormDataOutput multipartPerson = new MultipartFormDataOutput();
        multipartPerson.addFormData("hello", person, MediaType.APPLICATION_XML_TYPE);
        multipartPerson.addFormData("dublincore", dublin, MediaType.APPLICATION_XML_TYPE);
        return multipartPerson;
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("extractId: id=" + id);
        return id;
    }

    private void verbose(String msg) {
        logger.info(msg);
    }

    private void verbose(String msg, Object o, Class clazz) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
