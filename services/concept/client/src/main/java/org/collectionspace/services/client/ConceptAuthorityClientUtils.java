package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.concept.ConceptTermGroup;
import org.collectionspace.services.concept.ConceptauthoritiesCommon;
import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(ConceptAuthorityClientUtils.class);

    /**
     * Creates a new Concept Authority
     * @param displayName	The displayName used in UI, etc.
     * @param refName		The proper refName for this authority
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createConceptAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        ConceptauthoritiesCommon conceptAuthority = new ConceptauthoritiesCommon();
        conceptAuthority.setDisplayName(displayName);
        conceptAuthority.setShortIdentifier(shortIdentifier);
        conceptAuthority.setVocabType("ConceptAuthority"); //FIXME: REM - Should this really be hard-coded?
        PoxPayloadOut multipart = new PoxPayloadOut(ConceptAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(conceptAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, conceptAuthority common ", 
        				conceptAuthority, ConceptauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * @param commonPartXML the XML payload for the common part.
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     * @throws DocumentException
     */
    public static PoxPayloadOut createConceptInstance(
    		String commonPartXML, String headerLabel)  throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(ConceptAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        /*
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);
        */
        PayloadOutputPart commonPart = multipart.addPart(
        		ConceptAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME,
        		commonPartXML);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, concept common ", commonPart.asXML());
        }

        return multipart;
    }
    
    public static String createItemInAuthority(String vcsid,
    		String commonPartXML,
    		ConceptAuthorityClient client ) throws DocumentException {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	PoxPayloadOut multipart = 
    		createConceptInstance(commonPartXML, client.getItemCommonPartName());
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+commonPartXML
	    				+"\" in conceptAuthority: \"" + vcsid
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+commonPartXML
	    				+"\" in conceptAuthority: \"" + vcsid +"\", Status:"+ statusCode);
	    	}
	        newID = extractId(res);
        } finally {
        	res.releaseConnection();
        }

    	return newID;
    }
    
    /**
     * Creates the from xml file.
     *
     * @param fileName the file name
     * @return new CSID as string
     * @throws Exception the exception
     */
    private String createItemInAuthorityFromXmlFile(String vcsid, String commonPartFileName, 
    		ConceptAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
    	return createItemInAuthority(vcsid, commonPartXML, client );
    }    

    public static String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Location")).get(0);
        if(logger.isDebugEnabled()){
        	logger.debug("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if(logger.isDebugEnabled()){
        	logger.debug("id=" + id);
        }
        return id;
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
    
     public static List<ConceptTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }
        List<ConceptTermGroup> terms = new ArrayList<ConceptTermGroup>();
        ConceptTermGroup term = new ConceptTermGroup();
        term.setTermDisplayName(identifier);
        term.setTermName(identifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime(); 
   }

}
