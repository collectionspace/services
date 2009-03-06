package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.Person;
import org.collectionspace.hello.client.PersonClient;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A PersonServiceTest.
 * 
 * @version $Revision:$
 */
public class PersonServiceTest {

    private PersonClient personClient = PersonClient.getInstance();
    private Long id = 0L;

    @Test
    public void createPerson() {
        Person person = new Person();
        person.setFirstName("Chris");
        person.setLastName("Hoffman");
        person.setStreet("2195 Hearst Ave.");
        person.setCity("Berkeley");
        person.setState("CA");
        person.setZip("94704");
        person.setCountry("US");
        ClientResponse<Response> res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        //store id locally
        id = extractId(res);
    }

    @Test(dependsOnMethods = {"createPerson"})
    public void updatePerson() {
        Person touPerson = personClient.getPerson(id).getEntity();
        verbose("got person to update", touPerson);
        touPerson.setFirstName("Richard");
        touPerson.setLastName("Millet");
        int initialVersion = touPerson.getVersion();
        Person uPerson = personClient.updatePerson(id, touPerson).getEntity();
        verbose("updated person", uPerson);
        Assert.assertNotSame(uPerson.getVersion(), initialVersion);
        Assert.assertEquals(uPerson.getFirstName(), "Richard");
    }

    @Test
    public void getNonExistingPerson() {
        ClientResponse<Person> res = personClient.getPerson(999L);

        Response.Status status = res.getResponseStatus();
        verbose(this.getClass().getName() + ": " +
                "getNonExistingPerson: Status: code=" + status.getStatusCode() +
                " message=" + status.toString());
        verbose("getNonExistingPerson: Metadata:");
        verboseMap(res.getMetadata());
        verbose("getNonExistingPerson: Headers:");
        verboseMap(res.getHeaders());
        if (status.equals(Response.Status.NOT_FOUND)) {
            String msg = res.getEntity(String.class, String.class);
            verbose("getNonExistingPerson: error message=" + msg);
        }
    }

    
    @Test
    public void updateWrongPerson() {
        Person touPerson = personClient.getPerson(id).getEntity();
        verbose("updateWrongPerson: got person to update", touPerson);
        touPerson.setFirstName("Richard");
        touPerson.setLastName("Millet");
        //use non existing person to update
        ClientResponse<Person> res = personClient.updatePerson(9999L, touPerson);
        if (res.getResponseStatus().equals(Response.Status.NOT_FOUND)) {
            verbose("updateWrongPerson: Status=" + res.getResponseStatus().toString());
            String msg = res.getEntity(String.class, String.class);
            verbose("updateWrongPerson: application error message=" + msg);
        }
    }

    private Long extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        verbose("id=" + segments[segments.length - 1]);
        return Long.valueOf(segments[segments.length - 1]);
    }

    private void verbose(String msg) {
        System.out.println("PersonServiceTest : " + msg);
    }

    private void verbose(String msg, Person p) {
        try {
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(Person.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(p, System.out);
        //m.marshal(new JAXBElement(new QName("uri", "local"), Person.class, p), System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verboseMap(MultivaluedMap map) {
        for (Object entry : map.entrySet()) {
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            verbose("    name=" + mentry.getKey() + " value=" + mentry.getValue());
        }
    }
}
