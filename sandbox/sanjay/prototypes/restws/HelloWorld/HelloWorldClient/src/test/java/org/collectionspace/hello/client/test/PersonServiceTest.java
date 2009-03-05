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
        id = extractId(res);
    }

    @Test(dependsOnMethods = {"createPerson"})
    public void updatePerson() {
        Person me = personClient.getPerson(id).getEntity();
        verbose("received with get", me);
        me.setFirstName("Richard");
        me.setLastName("Millet");
        int initialVersion = me.getVersion();
        Person updated = personClient.updatePerson(id, me).getEntity();
        verbose("updated", updated);
        Assert.assertNotSame(updated.getVersion(), initialVersion);
        Assert.assertEquals(updated.getFirstName(), "Richard");
    }

    private Long extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        System.out.println("id=" + segments[segments.length - 1]);
        return Long.valueOf(segments[segments.length - 1]);
    }

    private void verbose(String msg, Person p) {
        try {
            System.out.println(msg);
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
}
