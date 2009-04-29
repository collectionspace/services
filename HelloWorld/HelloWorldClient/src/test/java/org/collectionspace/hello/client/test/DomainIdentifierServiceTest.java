package org.collectionspace.hello.client.test;

import org.collectionspace.hello.client.DomainIdentifierClient;
import java.util.ArrayList;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.DomainIdentifier;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A DomainIdentifierServiceTest.
 * 
 * @version $Revision:$
 */
public class DomainIdentifierServiceTest {

    private DomainIdentifierClient identifierClient = DomainIdentifierClient.getInstance();
    private String id = null;

    @Test
    public void createIdentifier() {
        DomainIdentifier identifier = new DomainIdentifier();
        identifier.setDsid("org.bnhm");
        ClientResponse<Response> res = identifierClient.createIdentifier(identifier);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        id = extractId(res);
    }

    @Test(dependsOnMethods = {"createIdentifier"})
    public void getIdentifier() {
        DomainIdentifier i = identifierClient.getIdentifier(id).getEntity();
        verbose("got DomainIdentifier", i);
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        verbose("id=" + segments[segments.length - 1]);
        return segments[segments.length - 1];
    }

    private void verbose(String msg) {
        System.out.println("DomainIdentifierServiceTest : " + msg);
    }

    private void verbose(String msg, DomainIdentifier p) {
        try {
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(DomainIdentifier.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(p, System.out);
        //m.marshal(new JAXBElement(new QName("uri", "local"), DomainIdentifier.class, p), System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
