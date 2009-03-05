package org.collectionspace.hello.client.test;

import org.collectionspace.hello.client.*;
import java.util.ArrayList;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.Identifier;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A IdentifierServiceTest.
 * 
 * @version $Revision:$
 */
public class IdentifierServiceTest {

    private IdentifierClient identifierClient = IdentifierClient.getInstance();
    private Long id = 0L;

    @Test
    public void createIdentifier() {
        Identifier identifier = new Identifier();
        identifier.setNamespace("org.bnhm");
        ClientResponse<Response> res = identifierClient.createIdentifier(identifier);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        id = extractId(res);
    }

    @Test(dependsOnMethods = {"createIdentifier"})
    public void getIdentifier() {
        Identifier i = identifierClient.getIdentifier(id).getEntity();
        verbose("received with get", i);
    }

    private Long extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        System.out.println("id=" + segments[segments.length - 1]);
        return Long.valueOf(segments[segments.length - 1]);
    }

    private void verbose(String msg, Identifier p) {
        try {
            System.out.println(msg);
            JAXBContext jc = JAXBContext.newInstance(Identifier.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(p, System.out);
            //m.marshal(new JAXBElement(new QName("uri", "local"), Identifier.class, p), System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
