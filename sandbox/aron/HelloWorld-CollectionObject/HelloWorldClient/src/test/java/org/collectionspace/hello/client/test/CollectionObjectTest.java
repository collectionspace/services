package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.CollectionObject;
import org.collectionspace.hello.CollectionObjectList;
import org.collectionspace.hello.client.CollectionObjectClient;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A CollectionObjectTest.
 * 
 * @version $Revision:$
 */
public class CollectionObjectTest {

    private CollectionObjectClient client = CollectionObjectClient.getInstance();
    private String updateId = "1";

/*
    @Test
    public void createCollectionObject() {
        CollectionObject CollectionObject = createCollectionObject("Chris", "Hoffman");
        ClientResponse<Response> res = CollectionObjectClient.createCollectionObject(CollectionObject);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        //store updateId locally
        updateId = extractId(res);
    }

    @Test
    public void createCollectionObjectList() {
        CollectionObject CollectionObject = createCollectionObject("Aron", "Roberts");
        ClientResponse<Response> res = CollectionObjectClient.createCollectionObject(CollectionObject);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        CollectionObject = createCollectionObject("Dan", "Sheppard");
        res = CollectionObjectClient.createCollectionObject(CollectionObject);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void updateCollectionObject() {
        CollectionObject touCollectionObject = CollectionObjectClient.getCollectionObject(updateId).getEntity();
        verbose("got CollectionObject to update", touCollectionObject, CollectionObject.class);
        touCollectionObject.setFirstName("Richard");
        touCollectionObject.setLastName("Millet");
        int initialVersion = touCollectionObject.getVersion();
        CollectionObject uCollectionObject = CollectionObjectClient.updateCollectionObject(updateId, touCollectionObject).getEntity();
        verbose("updated CollectionObject", uCollectionObject, CollectionObject.class);
        Assert.assertNotSame(uCollectionObject.getVersion(), initialVersion);
        Assert.assertEquals(uCollectionObject.getFirstName(), "Richard");
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void getCollectionObjectList() {
        //the resource method is expected to return at least an empty list
        CollectionObjectList CollectionObjectList = CollectionObjectClient.getCollectionObjectList().getEntity();
        List<CollectionObjectList.CollectionObjectListItem> list = CollectionObjectList.getCollectionObjectListItem();
        int i = 0;
        for (CollectionObjectList.CollectionObjectListItem pli : list) {
            verbose("getCollectionObjectList: list-item[" + i + "] firstName=" + pli.getFirstName());
            verbose("getCollectionObjectList: list-item[" + i + "] lastName=" + pli.getLastName());
            verbose("getCollectionObjectList: list-item[" + i + "] uri=" + pli.getUri());
            i++;
        }
    }

*/
    @Test
    public void getNonExistingCollectionObject() {
        ClientResponse<CollectionObject> res = client.getCollectionObject("foo");

        Response.Status status = res.getResponseStatus();
        verbose(this.getClass().getName() + ": " +
                "getNonExistingCollectionObject: Status: code=" + status.getStatusCode() +
                " message=" + status.toString());
        verbose("getNonExistingCollectionObject: Metadata:");
        verboseMap(res.getMetadata());
        verbose("getNonExistingCollectionObject: Headers:");
        verboseMap(res.getHeaders());
        if (status.equals(Response.Status.NOT_FOUND)) {
            String msg = res.getEntity(String.class, String.class);
            verbose("getNonExistingCollectionObject: error message=" + msg);
        }
    }

/*
    @Test(dependsOnMethods = {"updateCollectionObject"})
    public void updateWrongCollectionObject() {
        CollectionObject touCollectionObject = CollectionObjectClient.getCollectionObject(updateId).getEntity();
        verbose("updateWrongCollectionObject: got CollectionObject to update", touCollectionObject, CollectionObject.class);
        touCollectionObject.setFirstName("Richard");
        touCollectionObject.setLastName("Millet");
        //use non existing CollectionObject to update
        ClientResponse<CollectionObject> res = CollectionObjectClient.updateCollectionObject(9999L, touCollectionObject);
        if (res.getResponseStatus().equals(Response.Status.NOT_FOUND)) {
            verbose("updateWrongCollectionObject: Status=" + res.getResponseStatus().toString());
            String msg = res.getEntity(String.class, String.class);
            verbose("updateWrongCollectionObject: application error message=" + msg);
        }
    }


    @Test(dependsOnMethods = {"updateWrongCollectionObject"})
    public void deleteCollectionObject() {
        ClientResponse<Response> res = CollectionObjectClient.deleteCollectionObject(updateId);
        verbose("deleteCollectionObject: id=" + updateId);
        verbose("deleteCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }
    
    private CollectionObject createCollectionObject(String firstName, String lastName) {
        CollectionObject CollectionObject = new CollectionObject();
        CollectionObject.setFirstName(firstName);
        CollectionObject.setLastName(lastName);
        CollectionObject.setStreet("2195 Hearst Ave.");
        CollectionObject.setCity("Berkeley");
        CollectionObject.setState("CA");
        CollectionObject.setZip("94704");
        CollectionObject.setCountry("US");
        return CollectionObject;
    }

    private Long extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        verbose("id=" + segments[segments.length - 1]);
        return Long.valueOf(segments[segments.length - 1]);
    }

*/

    private void verbose(String msg) {
        System.out.println("CollectionObjectListerviceTest : " + msg);
    }

    private void verbose(String msg, Object o, Class clazz) {
        try {
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
        //m.marshal(new JAXBElement(new QName("uri", "local"), CollectionObject.class, p), System.out);
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
