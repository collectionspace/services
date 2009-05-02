package org.collectionspace.services.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;
import org.collectionspace.services.client.CollectionObjectClient;

/**
 * A CollectionObjectNuxeoServiceTest.
 * 
 * @version $Revision:$
 */
public class CollectionObjectServiceTest {

    private CollectionObjectClient collectionObjectClient = CollectionObjectClient.getInstance();
    private String updateId = null;
    private String deleteId = null;

    @Test
    public void createCollectionObject() {
    	long identifier = this.createIdentifier();
    	
    	CollectionObject collectionObject = createCollectionObject(identifier);
        ClientResponse<Response> res = collectionObjectClient.createCollectionObject(collectionObject);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        
        //store updateId locally for "update" test
        if (updateId == null) {
        	updateId = extractId(res);
        } else {
        	deleteId = extractId(res);
        	System.out.println("Set deleteId: " + deleteId);
        }
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void updateCollectionObject() {
    	ClientResponse<CollectionObject> res = collectionObjectClient.getCollectionObject(updateId);
        CollectionObject collectionObject = res.getEntity();
        verbose("Got CollectionObject to update with ID: " + updateId,
        		collectionObject, CollectionObject.class);
        
        //collectionObject.setCsid("updated-" + updateId);
        collectionObject.setObjectNumber("updated-" + collectionObject.getObjectNumber());
        collectionObject.setObjectName("updated-" + collectionObject.getObjectName());
        
        // make call to update service
        res = collectionObjectClient.updateCollectionObject(updateId, collectionObject);
        
        // check the response
        CollectionObject updatedCollectionObject = res.getEntity();        
        Assert.assertEquals(updatedCollectionObject.getObjectName(), collectionObject.getObjectName());
        verbose("updateCollectionObject: ", updatedCollectionObject, CollectionObject.class);
        
        return;
    }

    @Test(dependsOnMethods = {"createCollectionObject"})
    public void createCollection() {
    	for (int i = 0; i < 3; i++) {
    		this.createCollectionObject();
    	}
    }
    
    @Test(dependsOnMethods = {"createCollection"})
    public void getCollectionObjectList() {
        //the resource method is expected to return at least an empty list
        CollectionObjectList coList = collectionObjectClient.getCollectionObjectList().getEntity();
        List<CollectionObjectList.CollectionObjectListItem> coItemList = coList.getCollectionObjectListItem();
        int i = 0;
        for(CollectionObjectList.CollectionObjectListItem pli : coItemList) {
            verbose("getCollectionObjectList: list-item[" + i + "] csid=" + pli.getCsid());
            verbose("getCollectionObjectList: list-item[" + i + "] objectNumber=" + pli.getObjectNumber());
            verbose("getCollectionObjectList: list-item[" + i + "] URI=" + pli.getUri());
            i++;
        }
    }

    @Test(dependsOnMethods = {"createCollection"})
    public void deleteCollectionObject() {
    	System.out.println("Calling deleteCollectionObject:" + deleteId);
        ClientResponse<Response> res = collectionObjectClient.deleteCollectionObject(deleteId);
        verbose("deleteCollectionObject: csid=" + deleteId);
        verbose("deleteCollectionObject: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    private CollectionObject createCollectionObject(long identifier) {
    	CollectionObject collectionObject = createCollectionObject("objectNumber-" + identifier,
    			"objectName-" + identifier);    	

        return collectionObject;
    }

    private CollectionObject createCollectionObject(String objectNumber, String objectName) {
    	CollectionObject collectionObject = new CollectionObject();
    	
    	collectionObject.setObjectNumber(objectNumber);
    	collectionObject.setObjectName(objectName);

        return collectionObject;
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
        System.out.println("CollectionObject Test: " + msg);
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
    
    private long createIdentifier() {
    	long identifier = System.currentTimeMillis();
    	return identifier;
    }
}
