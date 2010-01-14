package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.VocabularyItemJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.vocabulary.VocabulariesCommon;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VocabularyClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(VocabularyClientUtils.class);

    public static MultipartOutput createEnumerationInstance(
    		String displayName, String refName, String headerLabel ) {
        VocabulariesCommon vocabulary = new VocabulariesCommon();
        vocabulary.setDisplayName(displayName);
        vocabulary.setRefName(refName);
        vocabulary.setVocabType("enum");
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(vocabulary, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, vocabulary common for enumeration ", 
        				vocabulary, VocabulariesCommon.class);
        }

        return multipart;
    }

		// Note that we do not use the map, but we will once we add more info to the 
		// items
    public static MultipartOutput createVocabularyItemInstance(String inVocabulary, 
    		String vocabItemRefName, Map<String, String> vocabItemInfo, String headerLabel){
        VocabularyitemsCommon vocabularyItem = new VocabularyitemsCommon();
        vocabularyItem.setInVocabulary(inVocabulary);
       	vocabularyItem.setRefName(vocabItemRefName);
       	String value = null;
        if((value = (String)vocabItemInfo.get(VocabularyItemJAXBSchema.DISPLAY_NAME))!=null)
        	vocabularyItem.setDisplayName(value);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(vocabularyItem,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, vocabularyItem common ", vocabularyItem, VocabularyitemsCommon.class);
        }

        return multipart;
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
    
    public static String createVocabularyRefName(String vocabularyName) {
    	return "urn:cspace:org.collectionspace.demo:vocabulary:name("
    			+vocabularyName+")";
    }

    public static String createVocabularyItemRefName(
    						String vocabularyRefName, String vocabItemName) {
    	return vocabularyRefName+":item:name("+vocabItemName+")";
    }

}
