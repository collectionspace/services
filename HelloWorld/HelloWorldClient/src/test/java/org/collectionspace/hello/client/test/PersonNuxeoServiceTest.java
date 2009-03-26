package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.PersonNuxeo;
import org.collectionspace.hello.People;
import org.collectionspace.hello.client.PersonNuxeoClient;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A PersonNuxeoServiceTest.
 * 
 * @version $Revision:$
 */
public class PersonNuxeoServiceTest {

    private PersonNuxeoClient personClient = PersonNuxeoClient.getInstance();
    private String updateId = "";
    private String deleteId = "";

    @Test
    public void createPerson() {
        PersonNuxeo person = createPerson("Chris", "Hoffman");
        ClientResponse<Response> res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        //store updateId locally
        updateId = extractId(res);
    }

    @Test
    public void createTeam() {
        PersonNuxeo person = createPerson("Sanjay", "Dalal");
        ClientResponse<Response> res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        deleteId = extractId(res);

        person = createPerson("Aron", "Roberts");
        res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());

        person = createPerson("Richard", "Millet");
        res = personClient.createPerson(person);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());

    }

    @Test(dependsOnMethods = {"createPerson"})
    public void updatePerson() {
        PersonNuxeo touPerson = new PersonNuxeo();//personClient.getPerson(updateId).getEntity();
        touPerson.setId(updateId);
        verbose("got person to update", touPerson, PersonNuxeo.class);
        touPerson.setFirstName("Patrick");
        touPerson.setLastName("Schmitz");
        PersonNuxeo uPerson = personClient.updatePerson(updateId, touPerson).getEntity();
        verbose("updated person", uPerson, PersonNuxeo.class);
        //Assert.assertNotSame(uPerson.getVersion(), initialVersion);
        Assert.assertEquals(uPerson.getFirstName(), "Patrick");
    }

    @Test(dependsOnMethods = {"createTeam"})
    public void getPeople() {
        //the resource method is expected to return at least an empty list
        People people = personClient.getPeople().getEntity();
        List<People.PeopleItem> list = people.getPeopleItem();
        int i = 0;
        for(People.PeopleItem pli : list){
            verbose("getPeople: list-item[" + i + "] title=" + pli.getTitle());
            verbose("getPeople: list-item[" + i + "] id=" + pli.getId());
            verbose("getPeople: list-item[" + i + "] uri=" + pli.getUri());
            i++;
        }
    }

    @Test(dependsOnMethods = {"updatePerson"})
    public void deletePerson() {
        ClientResponse<Response> res = personClient.deletePerson(deleteId);
        verbose("deletePerson: id=" + deleteId);
        verbose("deletePerson: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    private PersonNuxeo createPerson(String firstName, String lastName) {
        PersonNuxeo person = new PersonNuxeo();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setStreet("2195 Hearst Ave.");
        person.setCity("Berkeley");
        person.setState("CA");
        person.setZip("94704");
        person.setCountry("US");
        person.setVersion("1.0");
        return person;
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("id=" + id);
        return id;
    }

    private void verbose(String msg) {
        System.out.println("PersonServiceTest : " + msg);
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

    private void verboseMap(MultivaluedMap map) {
        for(Object entry : map.entrySet()){
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            verbose("    name=" + mentry.getKey() + " value=" + mentry.getValue());
        }
    }
}
