package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.collectionspace.services.TaxonomyJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.taxonomy.TaxonomyCommon;
import org.collectionspace.services.taxonomy.TaxonomyauthorityCommon;
import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class TaxonomyAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(TaxonomyAuthorityClientUtils.class);

    /**
     * Creates a new Taxonomy Authority
     * @param displayName	The displayName used in UI, etc.
     * @param refName		The proper refName for this authority
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createTaxonomyAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        TaxonomyauthorityCommon Taxonomyauthority = new TaxonomyauthorityCommon();
        Taxonomyauthority.setDisplayName(displayName);
        Taxonomyauthority.setShortIdentifier(shortIdentifier);
        String refName = createTaxonomyAuthRefName(shortIdentifier, displayName);
        Taxonomyauthority.setRefName(refName);
        Taxonomyauthority.setVocabType("Taxonomyauthority"); //FIXME: REM - Should this really be hard-coded?
        PoxPayloadOut multipart = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(Taxonomyauthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, Taxonomyauthority common ", 
        				Taxonomyauthority, TaxonomyauthorityCommon.class);
        }

        return multipart;
    }

    /**
     * @param taxonomyRefName  The proper refName for this authority
     * @param taxonomyInfo the properties for the new Taxonomy. Can pass in one condition
     * 						note and date string.
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createTaxonomyInstance( 
    		String taxonomyAuthRefName, Map<String, String> taxonomyInfo, 
				String headerLabel){
        TaxonomyCommon taxonomy = new TaxonomyCommon();
    	String shortId = taxonomyInfo.get(TaxonomyJAXBSchema.SHORT_IDENTIFIER);
    	String displayName = taxonomyInfo.get(TaxonomyJAXBSchema.DISPLAY_NAME);
    	taxonomy.setShortIdentifier(shortId);
    	String taxonomyRefName = createTaxonomyRefName(taxonomyAuthRefName, shortId, displayName);
       	taxonomy.setRefName(taxonomyRefName);
       	String value = null;
    	value = taxonomyInfo.get(TaxonomyJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
    	taxonomy.setDisplayNameComputed(displayNameComputed);
        if((value = (String)taxonomyInfo.get(TaxonomyJAXBSchema.NAME))!=null)
        	taxonomy.setTaxonFullName(value);
        if((value = (String)taxonomyInfo.get(TaxonomyJAXBSchema.RANK))!=null)
        	taxonomy.setTaxonRank(value);
        if((value = (String)taxonomyInfo.get(TaxonomyJAXBSchema.TERM_STATUS))!=null)
        	taxonomy.setTermStatus(value);

        PoxPayloadOut multipart = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(taxonomy,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, taxonomy common ", taxonomy, TaxonomyCommon.class);
        }

        return multipart;
    }
    
    /**
     * @param vcsid CSID of the authority to create a new taxonomy in
     * @param TaxonomyauthorityRefName The refName for the authority
     * @param taxonomyMap the properties for the new Taxonomy
     * @param client the service client
     * @return the CSID of the new item
     */
    public static String createItemInAuthority(String vcsid, 
    		String TaxonomyauthorityRefName, Map<String,String> taxonomyMap,
    		TaxonomyAuthorityClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	String displayName = taxonomyMap.get(TaxonomyJAXBSchema.DISPLAY_NAME);
    	String displayNameComputedStr = taxonomyMap.get(TaxonomyJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (displayNameComputedStr==null) || displayNameComputedStr.equalsIgnoreCase("true");
    	if( displayName == null ) {
    		if(!displayNameComputed) {
	    		throw new RuntimeException(
	    		"CreateItem: Must supply a displayName if displayNameComputed is set to false.");
    		}
        	displayName = 
        		prepareDefaultDisplayName(
    		    	taxonomyMap.get(TaxonomyJAXBSchema.NAME));
    	}
    	
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in Taxonomyauthority: \"" + TaxonomyauthorityRefName +"\"");
    	}
    	PoxPayloadOut multipart = 
    		createTaxonomyInstance( TaxonomyauthorityRefName,
    			taxonomyMap, client.getItemCommonPartName() );
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""
	    				+taxonomyMap.get(TaxonomyJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in Taxonomyauthority: \"" + TaxonomyauthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""
	    				+taxonomyMap.get(TaxonomyJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in Taxonomyauthority: \"" + TaxonomyauthorityRefName +"\", Status:"+ statusCode);
	    	}
	        newID = extractId(res);
        } finally {
        	res.releaseConnection();
        }

    	return newID;
    }

    public static PoxPayloadOut createTaxonomyInstance(
    		String commonPartXML, String headerLabel)  throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, taxonomy common ", commonPartXML);
        }

        return multipart;
    }
    
    public static String createItemInAuthority(String vcsid,
    		String commonPartXML,
    		TaxonomyAuthorityClient client ) throws DocumentException {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	PoxPayloadOut multipart = 
    		createTaxonomyInstance(commonPartXML, client.getItemCommonPartName());
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+commonPartXML
	    				+"\" in Taxonomyauthority: \"" + vcsid
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+commonPartXML
	    				+"\" in Taxonomyauthority: \"" + vcsid +"\", Status:"+ statusCode);
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
    		TaxonomyAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
    	return createItemInAuthority(vcsid, commonPartXML, client );
    }    

    /**
     * Creates the Taxonomyauthority ref name.
     *
     * @param shortId the Taxonomyauthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createTaxonomyAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:taxonomyauthority:name("
			+shortId+")";
		if(displaySuffix!=null&&!displaySuffix.isEmpty())
			refName += "'"+displaySuffix+"'";
    	return refName;
    }

    /**
     * Creates the taxonomy ref name.
     *
     * @param taxonomyAuthRefName the Taxonomyauthority ref name
     * @param shortId the taxonomy shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createTaxonomyRefName(
    						String taxonomyAuthRefName, String shortId, String displaySuffix) {
    	String refName = taxonomyAuthRefName+":taxonomy:name("+shortId+")";
		if(displaySuffix!=null&&!displaySuffix.isEmpty())
			refName += "'"+displaySuffix+"'";
    	return refName;
    }

    public static String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Taxonomy")).get(0);
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


    
    /**
     * Produces a default displayName from the basic name and dates fields.
     * @see TaxonomyDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param name	
     * @return
     */
    public static String prepareDefaultDisplayName(
    		String name ) {
    	StringBuilder newStr = new StringBuilder();
			newStr.append(name);
		return newStr.toString();
    }
    


}
