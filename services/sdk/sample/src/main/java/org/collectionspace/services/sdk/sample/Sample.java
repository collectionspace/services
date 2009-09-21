package org.collectionspace.services.sdk.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.testng.Assert;

import org.jboss.resteasy.client.ClientResponse;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;

public class Sample {

	private static CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String csid = createCollectionObject();
		System.out.println("Created a new collection object with CSID=" + csid);
		
		CollectionObject co = readCollectionObject(csid);
		System.out.println("Got a collection object with CSID=" + csid);
	}
	
	static String createCollectionObject() {
		String result = null;

		CollectionObject co = new CollectionObject();
		co.setObjectName("Keiko CollectionObject");
		
	    ClientResponse<Response> response = collectionObjectClient.create(co);
	    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
	    result = extractId(response);
	    
	    return result;
	}
	
	static CollectionObject readCollectionObject(String csid) {
		CollectionObject result = null;
		
		ClientResponse<CollectionObject> response = collectionObjectClient.read(csid);
		Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		result = response.getEntity();
		
		return result;
	}
	
	static String extractId(ClientResponse<Response> res) {
		String result = null;
		
		MultivaluedMap mvm = res.getMetadata();
		String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
		String[] segments = uri.split("/");
		result = segments[segments.length - 1];
		
		return result;
	}	

}
