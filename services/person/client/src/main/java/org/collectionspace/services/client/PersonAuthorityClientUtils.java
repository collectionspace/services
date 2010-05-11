/**	
 * PersonAuthorityClientUtils.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PersonAuthorityClientUtils.
 */
public class PersonAuthorityClientUtils {
    
    /** The Constant logger. */
    private static final Logger logger =
        LoggerFactory.getLogger(PersonAuthorityClientUtils.class);

    /**
     * Creates the person authority instance.
     *
     * @param displayName the display name
     * @param refName the ref name
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static MultipartOutput createPersonAuthorityInstance(
    		String displayName, String refName, String headerLabel ) {
        PersonauthoritiesCommon personAuthority = new PersonauthoritiesCommon();
        personAuthority.setDisplayName(displayName);
        personAuthority.setRefName(refName);
        personAuthority.setVocabType("PersonAuthority");
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(personAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, personAuthority common ", 
        				personAuthority, PersonauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * Creates the person instance.
     *
     * @param inAuthority the in authority
     * @param personRefName the person ref name
     * @param personInfo the person info
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static MultipartOutput createPersonInstance(String inAuthority, 
    		String personRefName, Map<String, String> personInfo, String headerLabel){
        PersonsCommon person = new PersonsCommon();
        person.setInAuthority(inAuthority);
       	person.setRefName(personRefName);
       	String value = null;
    	value = personInfo.get(PersonJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
    	person.setDisplayNameComputed(displayNameComputed);
        if((value = (String)personInfo.get(PersonJAXBSchema.FORE_NAME))!=null)
        	person.setForeName(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.MIDDLE_NAME))!=null)
        	person.setMiddleName(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.SUR_NAME))!=null)
        	person.setSurName(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.INITIALS))!=null)
        	person.setInitials(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.SALUTATIONS))!=null)
        	person.setSalutation(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.TITLE))!=null)
        	person.setTitle(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.NAME_ADDITIONS))!=null)
        	person.setNameAdditions(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.BIRTH_DATE))!=null)
        	person.setBirthDate(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.DEATH_DATE))!=null)
        	person.setDeathDate(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.BIRTH_PLACE))!=null)
        	person.setBirthPlace(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.DEATH_PLACE))!=null)
        	person.setDeathPlace(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.GROUP))!=null)
        	person.setGroup(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.NATIONALITY))!=null)
        	person.setNationality(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.GENDER))!=null)
        	person.setGender(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.OCCUPATION))!=null)
        	person.setOccupation(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.SCHOOL_OR_STYLE))!=null)
        	person.setSchoolOrStyle(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.BIO_NOTE))!=null)
        	person.setBioNote(value);
        if((value = (String)personInfo.get(PersonJAXBSchema.NAME_NOTE))!=null)
        	person.setNameNote(value);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(person,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, person common ", person, PersonsCommon.class);
        }

        return multipart;
    }
    
    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param personAuthorityRefName the person authority ref name
     * @param personMap the person map
     * @param client the client
     * @return the string
     */
    public static String createItemInAuthority(String vcsid, 
    		String personAuthorityRefName, Map<String,String> personMap,
    		PersonAuthorityClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	
    	String displayName = personMap.get(PersonJAXBSchema.DISPLAY_NAME);
    	String displayNameComputedStr = personMap.get(PersonJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (displayNameComputedStr==null) || displayNameComputedStr.equalsIgnoreCase("true");
    	if( displayName == null ) {
    		if(!displayNameComputed) {
	    		throw new RuntimeException(
	    		"CreateItem: Must supply a displayName if displayNameComputed is set to false.");
    		}
        	displayName = 
        		prepareDefaultDisplayName(
    		    	personMap.get(PersonJAXBSchema.FORE_NAME),
    		    	personMap.get(PersonJAXBSchema.MIDDLE_NAME),
    		    	personMap.get(PersonJAXBSchema.SUR_NAME),
    		    	personMap.get(PersonJAXBSchema.BIRTH_DATE),
    		    	personMap.get(PersonJAXBSchema.DEATH_DATE));
    	}
    	
    	String refName = createPersonRefName(personAuthorityRefName, displayName, true);

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in personAuthorityulary: \"" + personAuthorityRefName +"\"");
    	}
    	MultipartOutput multipart = 
    		createPersonInstance( vcsid, refName,
    			personMap, client.getItemCommonPartName() );
    	
    	String result = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
    	try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+refName
	    				+"\" in personAuthority: \"" + personAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+refName
	    				+"\" in personAuthority: \"" + personAuthorityRefName +"\", Status:"+ statusCode);
	    	}
	
	    	result = extractId(res);
    	} finally {
    		res.releaseConnection();
    	}
    	
    	return result;
    }

    /**
     * Creates the person auth ref name.
     *
     * @param personAuthorityName the person authority name
     * @param withDisplaySuffix the with display suffix
     * @return the string
     */
    public static String createPersonAuthRefName(String personAuthorityName, boolean withDisplaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:personauthority:name("
    			+personAuthorityName+")";
    	if(withDisplaySuffix)
    		refName += "'"+personAuthorityName+"'";
    	return refName;
    }

    /**
     * Creates the person ref name.
     *
     * @param personAuthRefName the person auth ref name
     * @param personName the person name
     * @param withDisplaySuffix the with display suffix
     * @return the string
     */
    public static String createPersonRefName(
    						String personAuthRefName, String personName, boolean withDisplaySuffix) {
    	String refName = personAuthRefName+":person:name("+personName+")";
    	if(withDisplaySuffix)
    		refName += "'"+personName+"'";
    	return refName;
    }

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
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
     * @see PersonDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param foreName	
     * @param middleName
     * @param surName
     * @param birthDate
     * @param deathDate
     * @return display name
     */
    public static String prepareDefaultDisplayName(
    		String foreName, String middleName, String surName,
    		String birthDate, String deathDate ) {
    	StringBuilder newStr = new StringBuilder();
		final String sep = " ";
		final String dateSep = "-";
		List<String> nameStrings = 
			Arrays.asList(foreName, middleName, surName);
		boolean firstAdded = false;
    	for(String partStr : nameStrings ){
			if(null != partStr ) {
				if(firstAdded) {
					newStr.append(sep);
				}
				newStr.append(partStr);
				firstAdded = true;
			}
    	}
    	// Now we add the dates. In theory could have dates with no name, but that is their problem.
    	boolean foundBirth = false;
		if(null != birthDate) {
			if(firstAdded) {
				newStr.append(sep);
			}
			newStr.append(birthDate);
	    	newStr.append(dateSep);		// Put this in whether there is a death date or not
			foundBirth = true;
		}
		if(null != deathDate) {
			if(!foundBirth) {
				if(firstAdded) {
					newStr.append(sep);
				}
		    	newStr.append(dateSep);
			}
			newStr.append(deathDate);
		}
		return newStr.toString();
    }
    


}
