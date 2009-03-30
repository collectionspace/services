package org.collectionspace.hello.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.collectionspace.hello.CollectionObject;
import org.collectionspace.hello.CollectionObjectList;
import org.collectionspace.hello.CollectionObjectListItem;
import org.collectionspace.hello.DefaultCollectionObject;
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
    private String nonexistentId = "foo";


    @Test
    public void createCollectionObject() {
        CollectionObject co =
          createCollectionObject("1984.021.0049", "Radio News, vol. 10, no. 2, August 1928");
        ClientResponse<Response> res = client.createCollectionObject(co);
        verbose("created status=" + Response.Status.CREATED.getStatusCode());
        verbose("response status=" + Response.Status.CREATED.getStatusCode());
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        //store updateId locally
        updateId = extractId(res);
    }

    @Test
    public void createCollectionObjectList() {
        CollectionObject co =
          createCollectionObject("1997.005.0437", "Toy, Gotham City Police Helicopter, 1992");
        ClientResponse<Response> res = client.createCollectionObject(co);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        co = createCollectionObject(
          "1984.052.0001", " Pathescope Model 9061 28mm motion picture projector, 1914");
        res = client.createCollectionObject(co);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void updateCollectionObject() {
        CollectionObject touCollectionObject =
          client.getCollectionObject(updateId).getEntity();
        verbose("got CollectionObject to update", touCollectionObject, CollectionObject.class);
        if ( touCollectionObject.getDefaultCollectionObject() == null ) {
          touCollectionObject.setDefaultCollectionObject( new DefaultCollectionObject() );
        }
        String updatedObjNum = "1981.297.0049";
        touCollectionObject.getDefaultCollectionObject().setObjectNumber(updatedObjNum);
        touCollectionObject.getDefaultCollectionObject().setObjectName(
          "Preview slide, It's the Old Army Game, 1926");
        // int initialVersion = touCollectionObject.getVersion();
        CollectionObject uCollectionObject =
        client.updateCollectionObject(updateId, touCollectionObject).getEntity();
        verbose("updated CollectionObject", uCollectionObject, CollectionObject.class);
        // Assert.assertNotSame(uCollectionObject.getVersion(), initialVersion);
        Assert.assertEquals(
          uCollectionObject.getDefaultCollectionObject().getObjectNumber(), updatedObjNum);
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void getCollectionObjectList() {
        //the resource method is expected to return at least an empty list
        CollectionObjectList coList = client.getCollectionObjectList().getEntity();
        List<CollectionObjectListItem> list = coList.getCollectionObjectListItem();
        int i = 0;
        for (CollectionObjectListItem pli : list) {
            verbose("getCollectionObjectList: list-item[" + i + "] objectNumber=" + pli.getObjectNumber());
            verbose("getCollectionObjectList: list-item[" + i + "] objectName=" + pli.getObjectName());
            verbose("getCollectionObjectList: list-item[" + i + "] uri=" + pli.getUri());
            i++;
        }
    }


    @Test
    public void getNonExistingCollectionObject() {
        ClientResponse<CollectionObject> res = client.getCollectionObject(nonexistentId);

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

    @Test(dependsOnMethods = {"updateCollectionObject"})
    public void updateWrongCollectionObject() {
        CollectionObject touCollectionObject =
          client.getCollectionObject(updateId).getEntity();
        verbose("updateWrongCollectionObject: got CollectionObject to update", touCollectionObject, CollectionObject.class);
        if ( touCollectionObject.getDefaultCollectionObject() == null ) {
          touCollectionObject.setDefaultCollectionObject( new DefaultCollectionObject() );
        }
        String updatedObjNum = "1981.297.0049";
        touCollectionObject.getDefaultCollectionObject().setObjectNumber(updatedObjNum);
        touCollectionObject.getDefaultCollectionObject().setObjectName(
          "Preview slide, It's the Old Army Game, 1926");
        //use non existing CollectionObject to update
        ClientResponse<CollectionObject> res =
          client.updateCollectionObject(nonexistentId, touCollectionObject);
        if (res.getResponseStatus().equals(Response.Status.NOT_FOUND)) {
            verbose("updateWrongCollectionObject: Status=" + res.getResponseStatus().toString());
            String msg = res.getEntity(String.class, String.class);
            verbose("updateWrongCollectionObject: application error message=" + msg);
        }
    }


    @Test(dependsOnMethods = {"updateWrongCollectionObject"})
    public void deleteCollectionObject() {
        ClientResponse<Response> res = client.deleteCollectionObject(updateId);
        verbose("deleteCollectionObject: id=" + updateId);
        verbose("deleteCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }
    
    // ###################################################################################
    // Utility methods used by tests above
    // ###################################################################################
    
    private CollectionObject createCollectionObject(String objectNumber, String objectName) {
        verbose("objectNumber=" + objectNumber);
        verbose("objectName=" + objectName);
        verbose("Before new CollectionObject() ...");
        CollectionObject co = new CollectionObject();
        verbose("Before co.getDefaultCollectionObject().setObjectNumber ...");
        co.setDefaultCollectionObject( new DefaultCollectionObject() );
        co.getDefaultCollectionObject().setObjectNumber(objectNumber);
        co.getDefaultCollectionObject().setObjectName(objectName);
        return co;
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
        System.out.println("CollectionObjectServiceTest : " + msg);
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
