package org.collectionspace.services.sdk.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.testng.Assert;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;

public class Sample {

	private static CollectionObjectClient collectionObjectClient = new CollectionObjectClient();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Base URL is: " + collectionObjectClient.getBaseURL());
		
		String csid = createCollectionObject();
		System.out.println("Created a new collection object with CSID=" + csid);
		
		CollectionobjectsCommon co = readCollectionObject(csid);
		System.out.println("Got a collection object with CSID=" + csid);
	}
	
	static String createCollectionObject() {
		String result = null;

		CollectionobjectsCommon co = new CollectionobjectsCommon();
		co.setObjectName("Keiko CollectionobjectsCommon");
	
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(co, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", collectionObjectClient.getCommonPartName());
		
	    ClientResponse<Response> response = collectionObjectClient.create(multipart);
	    Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
	    result = extractId(response);
	    
	    return result;
	}
	
	static CollectionobjectsCommon readCollectionObject(String csid) {
		CollectionobjectsCommon result = null;
		
        ClientResponse<MultipartInput> response = collectionObjectClient.read(csid);
		Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        try{
            MultipartInput input = (MultipartInput) response.getEntity();
            result = (CollectionobjectsCommon) extractPart(input,
            		collectionObjectClient.getCommonPartName(), CollectionobjectsCommon.class);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
		
		return result;
	}	
//
// Utility methods that belong somewhere in the SDK and NOT the sample.
//	
	static String extractId(ClientResponse<Response> res) {
		String result = null;
		
		MultivaluedMap mvm = res.getMetadata();
		String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
		String[] segments = uri.split("/");
		result = segments[segments.length - 1];
		
		return result;
	}	

    static Object extractPart(MultipartInput input, String label, Class clazz) throws Exception {
        Object obj = null;
        for(InputPart part : input.getParts()){
            String partLabel = part.getHeaders().getFirst("label");
            if(label.equalsIgnoreCase(partLabel)){
                String partStr = part.getBodyAsString();
                obj = part.getBody(clazz, null);
                break;
            }
        }
        return obj;
    }
	
}
