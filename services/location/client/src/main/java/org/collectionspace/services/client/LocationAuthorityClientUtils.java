package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.location.LocationsCommon;
import org.collectionspace.services.location.LocationauthoritiesCommon;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(LocationAuthorityClientUtils.class);

    public static MultipartOutput createLocationAuthorityInstance(
    		String displayName, String refName, String headerLabel ) {
        LocationauthoritiesCommon locationAuthority = new LocationauthoritiesCommon();
        locationAuthority.setDisplayName(displayName);
        locationAuthority.setRefName(refName);
        locationAuthority.setVocabType("LocationAuthority");
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(locationAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, locationAuthority common ", 
        				locationAuthority, LocationauthoritiesCommon.class);
        }

        return multipart;
    }

    public static MultipartOutput createLocationInstance(String inAuthority, 
    		String locationRefName, Map<String, String> locationInfo, String headerLabel){
        LocationsCommon location = new LocationsCommon();
        location.setInAuthority(inAuthority);
       	location.setRefName(locationRefName);
       	String value = null;
    	value = locationInfo.get(LocationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
    	location.setDisplayNameComputed(displayNameComputed);
        if((value = (String)locationInfo.get(LocationJAXBSchema.NAME))!=null)
        	location.setName(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.CONDITION_NOTE))!=null)
        	location.setConditionNote(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.CONDITION_NOTE_DATE))!=null)
        	location.setConditionNoteDate(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.SECURITY_NOTE))!=null)
        	location.setSecurityNote(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.LOCATION_TYPE))!=null)
        	location.setLocationType(value);
        if((value = (String)locationInfo.get(LocationJAXBSchema.STATUS))!=null)
        	location.setStatus(value);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(location,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, location common ", location, LocationsCommon.class);
        }

        return multipart;
    }
    
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
    	
    	String refName = createLocationRefName(locationAuthorityRefName, displayName, true);

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in locationAuthority: \"" + locationAuthorityRefName +"\"");
    	}
    	MultipartOutput multipart = 
    		createLocationInstance( vcsid, refName,
    			locationMap, client.getItemCommonPartName() );
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \""+refName
    				+"\" in locationAuthority: \"" + locationAuthorityRefName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+refName
    				+"\" in locationAuthority: \"" + locationAuthorityRefName +"\", Status:"+ statusCode);
    	}

    	return extractId(res);
    }

    public static String createLocationAuthRefName(String locationAuthorityName, boolean withDisplaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:locationauthority:name("
    			+locationAuthorityName+")";
    	if(withDisplaySuffix)
    		refName += "'"+locationAuthorityName+"'";
    	return refName;
    }

    public static String createLocationRefName(
    						String locationAuthRefName, String locationName, boolean withDisplaySuffix) {
    	String refName = locationAuthRefName+":location:name("+locationName+")";
    	if(withDisplaySuffix)
    		refName += "'"+locationName+"'";
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
