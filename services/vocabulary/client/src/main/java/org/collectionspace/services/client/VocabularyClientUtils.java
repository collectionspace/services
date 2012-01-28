package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.jboss.resteasy.client.ClientResponse;
//import org.jboss.resteasy.plugins.providers.multipart.PoxPayloadOut;
//import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VocabularyClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(VocabularyClientUtils.class);
    
    public static PoxPayloadOut createEnumerationInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        VocabulariesCommon vocabulary = new VocabulariesCommon();
        vocabulary.setDisplayName(displayName);
        vocabulary.setShortIdentifier(shortIdentifier);
        //String refName = createVocabularyRefName(shortIdentifier, displayName);
        //vocabulary.setRefName(refName);
        vocabulary.setVocabType("enum");
        PoxPayloadOut multipart = new PoxPayloadOut(VocabularyClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(vocabulary, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, vocabulary common for enumeration ", 
        				vocabulary, VocabulariesCommon.class);
        }

        return multipart;
    }

		// Note that we do not use the map, but we will once we add more info to the 
		// items
    public static PoxPayloadOut createVocabularyItemInstance( 
    		String vocabularyRefName, Map<String, String> vocabItemInfo, String headerLabel){
        VocabularyitemsCommon vocabularyItem = new VocabularyitemsCommon();
    	String shortId = vocabItemInfo.get(AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
    	String displayName = vocabItemInfo.get(AuthorityItemJAXBSchema.DISPLAY_NAME);
       	vocabularyItem.setShortIdentifier(shortId);
       	vocabularyItem.setDisplayName(displayName);
    	//String refName = createVocabularyItemRefName(vocabularyRefName, shortId, displayName);
       	//vocabularyItem.setRefName(refName);
       	PoxPayloadOut multipart = new PoxPayloadOut(VocabularyClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(vocabularyItem,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, vocabularyItem common ", vocabularyItem, VocabularyitemsCommon.class);
        }

        return multipart;
    }

    public static String createItemInVocabulary(String vcsid, 
    		String vocabularyRefName, Map<String,String> itemMap,
    		VocabularyClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+itemMap.get(AuthorityItemJAXBSchema.SHORT_IDENTIFIER)
    				+"\" in personAuthority: \"" + vcsid +"\"");
    	}
    	PoxPayloadOut multipart = createVocabularyItemInstance(null, //vocabularyRefName,
    				itemMap, client.getItemCommonPartName());
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \"" + itemMap.get(AuthorityItemJAXBSchema.DISPLAY_NAME)
    				+ "\" in personAuthority: \"" + vcsid //vocabularyRefName
    				+ "\" " + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+itemMap.get(AuthorityItemJAXBSchema.DISPLAY_NAME)
    				+ "\" in personAuthority: \"" + vcsid /*vocabularyRefName*/ + "\", Status:" + statusCode);
    	}

    	return extractId(res);
    }

    /**
     * Returns an error message indicating that the status code returned by a
     * specific call to a service does not fall within a set of valid status
     * codes for that service.
     *
     * @param serviceRequestType  A type of service request (e.g. CREATE, DELETE).
     *
     * @param statusCode  The invalid status code that was returned in the response,
     *                    from submitting that type of request to the service.
     *
     * @return An error message.
     */
    public static String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode + "' in response is NOT within the expected set: " +
                requestType.validStatusCodesAsString();
    }

    public static String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Location")).get(0);
        if(logger.isDebugEnabled()){
        	logger.info("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if(logger.isDebugEnabled()){
        	logger.debug("id=" + id);
        }
        return id;
    }
    
    /*
    public static String createVocabularyRefName(String shortIdentifier, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:vocabulary:name("
    			+ shortIdentifier + ")";
    	if(displaySuffix != null && !displaySuffix.isEmpty())
    		refName += "'" + displaySuffix + "'";
    	return refName;
    }

    public static String createVocabularyItemRefName(
    						String vocabularyRefName, String shortIdentifier, String displaySuffix) {
    	String refName = vocabularyRefName+":item:name("+shortIdentifier+")";
    	if(displaySuffix != null && !displaySuffix.isEmpty())
    		refName += "'" + displaySuffix + "'";
    	return refName;
    }
    */

}
