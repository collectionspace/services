package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.Person;
import org.collectionspace.hello.Persons;
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
    private Long updateId = 0L;

    @Test
    public void createPerson() {
        Person person = createPerson("Chris", "Hoffman");
        ClientResponse<Response> res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        //store updateId locally
        updateId = extractId(res);
    }

    @Test
    public void createPersons() {
        Person person = createPerson("Aron", "Roberts");
        ClientResponse<Response> res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        person = createPerson("Dan", "Sheppard");
        res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createPerson"})
    public void updatePerson() {
        Person touPerson = personClient.getPerson(updateId).getEntity();
        verbose("got person to update", touPerson, Person.class);
        touPerson.setFirstName("Richard");
        touPerson.setLastName("Millet");
        int initialVersion = touPerson.getVersion();
        Person uPerson = personClient.updatePerson(updateId, touPerson).getEntity();
        verbose("updated person", uPerson, Person.class);
        Assert.assertNotSame(uPerson.getVersion(), initialVersion);
        Assert.assertEquals(uPerson.getFirstName(), "Richard");
    }

    @Test(dependsOnMethods = {"createPerson"})
    public void getPersons() {
        //the resource method is expected to return at least an empty list
        Persons persons = personClient.getPersons().getEntity();
        List<Persons.PersonListItem> list = persons.getPersonListItem();
        int i = 0;
        for (Persons.PersonListItem pli : list) {
            verbose("getPersons: list-item[" + i + "] firstName=" + pli.getFirstName());
            verbose("getPersons: list-item[" + i + "] lastName=" + pli.getLastName());
            verbose("getPersons: list-item[" + i + "] uri=" + pli.getUri());
            i++;
        }
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

    @Test(dependsOnMethods = {"updatePerson"})
    public void updateWrongPerson() {
        Person touPerson = personClient.getPerson(updateId).getEntity();
        verbose("updateWrongPerson: got person to update", touPerson, Person.class);
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


    @Test(dependsOnMethods = {"updateWrongPerson"})
    public void deletePerson() {
        ClientResponse<Response> res = personClient.deletePerson(updateId);
        verbose("deletePerson: id=" + updateId);
        verbose("deletePerson: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }
    
    private Person createPerson(String firstName, String lastName) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setStreet("2195 Hearst Ave.");
        person.setCity("Berkeley");
        person.setState("CA");
        person.setZip("94704");
        person.setCountry("US");
        return person;
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

    private void verbose(String msg, Object o, Class clazz) {
        try {
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
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
