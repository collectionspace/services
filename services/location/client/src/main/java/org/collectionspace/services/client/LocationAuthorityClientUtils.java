package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.location.*;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
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
    		String locationAuthRefName,
    		Map<String, String> locationInfo, 
		List<LocTermGroup> terms, 
		String headerLabel) {
        LocationsCommon location = new LocationsCommon();
    	String shortId = locationInfo.get(LocationJAXBSchema.SHORT_IDENTIFIER);
    	String displayName = locationInfo.get(LocationJAXBSchema.DISPLAY_NAME);
    	location.setShortIdentifier(shortId);
    	// String locationRefName = createLocationRefName(locationAuthRefName, shortId, displayName);
       	// location.setRefName(locationRefName);
       	String value = null;
    	value = locationInfo.get(LocationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true");
        
        // Set values in the Term Information Group
        LocTermGroupList termList = new LocTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getLocTermGroup().addAll(terms); 
        location.setLocTermGroupList(termList);
        
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
		String locationAuthorityRefName,
		Map<String,String> locationMap,
		List<LocTermGroup> terms,
		LocationAuthorityClient client ) {
        
        String displayName = "";
        if (terms !=null && !terms.isEmpty()) {
            displayName = terms.get(0).getTermDisplayName();
        }
    	
    	if(logger.isDebugEnabled()){
    		logger.debug("Creating item with display name: \"" + displayName
    				+"\" in locationAuthority: \"" + vcsid +"\"");
    	}
        
    	PoxPayloadOut multipart = createLocationInstance( locationAuthorityRefName,
    			locationMap, terms, client.getItemCommonPartName() );
    	String newID = null;
    	Response res = client.createItem(vcsid, multipart);
        try {
        	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
	    	int statusCode = res.getStatus();
	
	    	if (!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""
	    				+locationMap.get(LocationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in locationAuthority: \"" + locationAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if (statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""
	    				+locationMap.get(LocationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in locationAuthority: \"" + locationAuthorityRefName +"\", Status:"+ statusCode);
	    	}
	        newID = extractId(res);
        } finally {
        	res.close();
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
    	Response res = client.createItem(vcsid, multipart);
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
        	res.close();
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

    public static String extractId(Response res) {
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
    
    public static List<LocTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }
        List<LocTermGroup> terms = new ArrayList<LocTermGroup>();
        LocTermGroup term = new LocTermGroup();
        term.setTermDisplayName(identifier);
        term.setTermName(identifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime(); 
   }

    public static PoxPayloadOut createLocationInstance(String shortIdentifier, String displayName,
            String serviceItemCommonPartName) {
        List<LocTermGroup> terms = getTermGroupInstance(shortIdentifier, displayName);
        
        Map<String, String> locationInfo = new HashMap<String, String>();
        locationInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);

        return createLocationInstance(null, locationInfo, terms, serviceItemCommonPartName);
    }

    private static List<LocTermGroup> getTermGroupInstance(String shortIdentifier, String displayName) {
        if (Tools.isBlank(shortIdentifier)) {
            shortIdentifier = getGeneratedIdentifier();
        }
        if (Tools.isBlank(shortIdentifier)) {
            displayName = shortIdentifier;
        }
        
        List<LocTermGroup> terms = new ArrayList<LocTermGroup>();
        LocTermGroup term = new LocTermGroup();
        term.setTermDisplayName(displayName);
        term.setTermName(shortIdentifier);
        terms.add(term);
        return terms;
    }

}
