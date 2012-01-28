package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.location.LocationsCommon;
import org.collectionspace.services.location.ConditionGroupList;
import org.collectionspace.services.location.ConditionGroup;
import org.collectionspace.services.location.LocationauthoritiesCommon;
import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(LocationAuthorityClientUtils.class);

    /**
     * Creates a new Location Authority
     * @param displayName	The displayName used in UI, etc.
     * @param refName		The proper refName for this authority
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createLocationAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
    	
        LocationauthoritiesCommon locationAuthority = new LocationauthoritiesCommon();
        locationAuthority.setDisplayName(displayName);
        locationAuthority.setShortIdentifier(shortIdentifier);
        // String refName = createLocationAuthRefName(shortIdentifier, displayName);
        // locationAuthority.setRefName(refName);
        locationAuthority.setVocabType("LocationAuthority"); //FIXME: REM - Should this really be hard-coded?
        
        PoxPayloadOut multipart = new PoxPayloadOut(LocationAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(headerLabel, locationAuthority);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, locationAuthority common ", 
        				locationAuthority, LocationauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * @param locationRefName  The proper refName for this authority
     * @param locationInfo the properties for the new Location. Can pass in one condition
     * 						note and date string.
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createLocationInstance( 
    		String locationAuthRefName, Map<String, String> locationInfo, 
				String headerLabel){
        LocationsCommon location = new LocationsCommon();
    	String shortId = locationInfo.get(LocationJAXBSchema.SHORT_IDENTIFIER);
    	String displayName = locationInfo.get(LocationJAXBSchema.DISPLAY_NAME);
    	location.setShortIdentifier(shortId);
    	// String locationRefName = createLocationRefName(locationAuthRefName, shortId, displayName);
       	// location.setRefName(locationRefName);
       	String value = null;
    	value = locationInfo.get(LocationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
    	location.setDisplayNameComputed(displayNameComputed);
        if((value = (String)locationInfo.get(LocationJAXBSchema.NAME))!=null)
        	location.setName(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.CONDITION_NOTE))!=null) {
            ConditionGroupList conditionGroupList = new ConditionGroupList();
            List<ConditionGroup> conditionGroups = conditionGroupList.getConditionGroup();
            ConditionGroup conditionGroup = new ConditionGroup();
            conditionGroup.setConditionNote(value);
            if((value = (String)locationInfo.get(LocationJAXBSchema.CONDITION_NOTE_DATE))!=null)
            	conditionGroup.setConditionNoteDate(value);
            conditionGroups.add(conditionGroup);
            location.setConditionGroupList(conditionGroupList);
        }
        if((value = (String)locationInfo.get(LocationJAXBSchema.SECURITY_NOTE))!=null)
        	location.setSecurityNote(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.ACCESS_NOTE))!=null)
        	location.setAccessNote(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.LOCATION_TYPE))!=null)
        	location.setLocationType(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.ADDRESS))!=null)
        	location.setAddress(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.TERM_STATUS))!=null)
        	location.setTermStatus(value);

        PoxPayloadOut multipart = new PoxPayloadOut(LocationAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(location,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, location common ", location, LocationsCommon.class);
        }

        return multipart;
    }
    
    /**
     * @param vcsid CSID of the authority to create a new location in
     * @param locationAuthorityRefName The refName for the authority
     * @param locationMap the properties for the new Location
     * @param client the service client
     * @return the CSID of the new item
     */
    public static String createItemInAuthority(String vcsid, 
    		String locationAuthorityRefName, Map<String,String> locationMap,
    		LocationAuthorityClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	String displayName = locationMap.get(LocationJAXBSchema.DISPLAY_NAME);
    	String displayNameComputedStr = locationMap.get(LocationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (displayNameComputedStr==null) || displayNameComputedStr.equalsIgnoreCase("true");
    	if( displayName == null ) {
    		if(!displayNameComputed) {
	    		throw new RuntimeException(
	    		"CreateItem: Must supply a displayName if displayNameComputed is set to false.");
    		}
        	displayName = 
        		prepareDefaultDisplayName(
    		    	locationMap.get(LocationJAXBSchema.NAME));
    	}
    	
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in locationAuthority: \"" + locationAuthorityRefName +"\"");
    	}
    	PoxPayloadOut multipart = 
    		createLocationInstance( locationAuthorityRefName,
    			locationMap, client.getItemCommonPartName() );
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""
	    				+locationMap.get(LocationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in locationAuthority: \"" + locationAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""
	    				+locationMap.get(LocationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in locationAuthority: \"" + locationAuthorityRefName +"\", Status:"+ statusCode);
	    	}
	        newID = extractId(res);
        } finally {
        	res.releaseConnection();
        }

    	return newID;
    }

    public static PoxPayloadOut createLocationInstance(
    		String commonPartXML, String headerLabel)  throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(LocationAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, location common ", commonPartXML);
        }

        return multipart;
    }
    
    public static String createItemInAuthority(String vcsid,
    		String commonPartXML,
    		LocationAuthorityClient client ) throws DocumentException {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	PoxPayloadOut multipart = 
    		createLocationInstance(commonPartXML, client.getItemCommonPartName());
    	String newID = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+commonPartXML
	    				+"\" in locationAuthority: \"" + vcsid
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+commonPartXML
	    				+"\" in locationAuthority: \"" + vcsid +"\", Status:"+ statusCode);
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
    		LocationAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
    	return createItemInAuthority(vcsid, commonPartXML, client );
    }    

    /**
     * Creates the locationAuthority ref name.
     *
     * @param shortId the locationAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createLocationAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:locationauthority:name("
			+shortId+")";
		if(displaySuffix!=null&&!displaySuffix.isEmpty())
			refName += "'"+displaySuffix+"'";
    	return refName;
    }

    /**
     * Creates the location ref name.
     *
     * @param locationAuthRefName the locationAuthority ref name
     * @param shortId the location shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createLocationRefName(
    						String locationAuthRefName, String shortId, String displaySuffix) {
    	String refName = locationAuthRefName+":location:name("+shortId+")";
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
     * Produces a default displayName from the basic name and dates fields.
     * @see LocationDocumentModelHandler.prepareDefaultDisplayName() which
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
