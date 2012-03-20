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
import org.collectionspace.services.PlaceJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.place.PlaceauthoritiesCommon;
import org.collectionspace.services.place.PlaceNameGroup;
import org.collectionspace.services.place.PlaceNameGroupList;
import org.collectionspace.services.place.PlacesCommon;
import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(PlaceAuthorityClientUtils.class);

    /**
     * Creates a new Place Authority
     * @param displayName	The displayName used in UI, etc.
     * @param refName		The proper refName for this authority
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createPlaceAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        PlaceauthoritiesCommon placeAuthority = new PlaceauthoritiesCommon();
        placeAuthority.setDisplayName(displayName);
        placeAuthority.setShortIdentifier(shortIdentifier);
        String refName = createPlaceAuthRefName(shortIdentifier, displayName);
        placeAuthority.setRefName(refName);
        placeAuthority.setVocabType("PlaceAuthority"); //FIXME: REM - Should this really be hard-coded?
        PoxPayloadOut multipart = new PoxPayloadOut(PlaceAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(placeAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, placeAuthority common ", 
        				placeAuthority, PlaceauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * @param placeRefName  The proper refName for this authority
     * @param placeInfo the properties for the new Place. Can pass in one condition
     * 						note and date string.
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createPlaceInstance( 
    		String placeAuthRefName, Map<String, String> placeInfo, 
				String headerLabel){
        PlacesCommon place = new PlacesCommon();
    	String shortId = placeInfo.get(PlaceJAXBSchema.SHORT_IDENTIFIER);
    	String displayName = placeInfo.get(PlaceJAXBSchema.DISPLAY_NAME);
    	place.setShortIdentifier(shortId);
    	String placeRefName = createPlaceRefName(placeAuthRefName, shortId, displayName);
       	place.setRefName(placeRefName);
       	String value = null;
    	value = placeInfo.get(PlaceJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
    	place.setDisplayNameComputed(displayNameComputed);
			if((value = (String)placeInfo.get(PlaceJAXBSchema.DISPLAY_NAME))!=null)
				place.setDisplayName(value);
    	value = placeInfo.get(PlaceJAXBSchema.DISPLAY_NAME_COMPUTED);
    	displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
			place.setDisplayNameComputed(displayNameComputed);
        /* TODO - think about how much to support. This approach to the client
	 * does not scale! We should really favor the document/payload approach. */
        if((value = (String)placeInfo.get(PlaceJAXBSchema.NAME))!=null) {
            PlaceNameGroupList placeNameGroupList = new PlaceNameGroupList();
            List<PlaceNameGroup> placeNameGroups = placeNameGroupList.getPlaceNameGroup();
            PlaceNameGroup placeNameGroup = new PlaceNameGroup();
            placeNameGroup.setName(value);
            placeNameGroups.add(placeNameGroup);
            place.setPlaceNameGroupList(placeNameGroupList);
        }
        
					
        if((value = (String)placeInfo.get(PlaceJAXBSchema.TERM_STATUS))!=null)
        	place.setTermStatus(value);

        PoxPayloadOut multipart = new PoxPayloadOut(PlaceAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(place,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, place common ", place, PlacesCommon.class);
        }

        return multipart;
    }
    
    /**
     * @param vcsid CSID of the authority to create a new placplace
     * @param placeAuthorityRefName The refName for the authority
     * @param placeMap the properties for the new Place
     * @param client the service client
     * @return the CSID of the new item
     */
    public static String createItemInAuthority(String vcsid, 
    		String placeAuthorityRefName, Map<String,String> placeMap,
    		PlaceAuthorityClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	String displayName = placeMap.get(PlaceJAXBSchema.DISPLAY_NAME);
    	String displayNameComputedStr = placeMap.get(PlaceJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (displayNameComputedStr==null) || displayNameComputedStr.equalsIgnoreCase("true");
    	if( displayName == null ) {
    		if(!displayNameComputed) {
	    		throw new RuntimeException(
	    		"CreateItem: Must supply a displayName if displayNameComputed is set to false.");
    		}
				/* Could try to pull name out of first placeNameGroup
        	displayName = 
        		prepareDefaultDisplayName(
    		    	placeMap.get(PlaceJAXBSchema.NAME));
							*/
    	}
    	
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in placeAuthority: \"" + placeAuthorityRefName +"\"");
    	}
    	PoxPayloadOut multipart = 
    		createPlaceInstance( placeAuthorityRefName,
    			placeMap, client.getItemCommonPartName() );
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""
	    				+placeMap.get(PlaceJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in placeAuthority: \"" + placeAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""
	    				+placeMap.get(PlaceJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in placeAuthority: \"" + placeAuthorityRefName +"\", Status:"+ statusCode);
	    	}
	        newID = extractId(res);
        } finally {
        	res.releaseConnection();
        }

    	return newID;
    }

    public static PoxPayloadOut createPlaceInstance(
    		String commonPartXML, String headerLabel)  throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(PlaceAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, place common ", commonPartXML);
        }

        return multipart;
    }
    
    public static String createItemInAuthority(String vcsid,
    		String commonPartXML,
    		PlaceAuthorityClient client ) throws DocumentException {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	PoxPayloadOut multipart = 
    		createPlaceInstance(commonPartXML, client.getItemCommonPartName());
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+commonPartXML
	    				+"\" in placeAuthority: \"" + vcsid
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+commonPartXML
	    				+"\" in placeAuthority: \"" + vcsid +"\", Status:"+ statusCode);
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
    		PlaceAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
    	return createItemInAuthority(vcsid, commonPartXML, client );
    }    

    /**
     * Creates the placeAuthority ref name.
     *
     * @param shortId the placeAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createPlaceAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:placeauthority:name("
			+shortId+")";
		if(displaySuffix!=null&&!displaySuffix.isEmpty())
			refName += "'"+displaySuffix+"'";
    	return refName;
    }

    /**
     * Creates the place ref name.
     *
     * @param placeAuthRefName the placeAuthority ref name
     * @param shortId the place shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createPlaceRefName(
    						String placeAuthRefName, String shortId, String displaySuffix) {
    	String refName = placeAuthRefName+":place:name("+shortId+")";
		if(displaySuffix!=null&&!displaySuffix.isEmpty())
			refName += "'"+displaySuffix+"'";
    	return refName;
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


    
    /**
     * Produces a default displayName from one or more supplied field(s).
     * @see PlaceAuthorityDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param name	
     * @return a display name
     */
    public static String prepareDefaultDisplayName(
    		String name ) {
    	StringBuilder newStr = new StringBuilder();
			newStr.append(name);
		return newStr.toString();
    }
    


}
