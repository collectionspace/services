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

import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.person.GroupList;
import org.collectionspace.services.person.NationalityList;
import org.collectionspace.services.person.OccupationList;
import org.collectionspace.services.person.PersonTermGroup;
import org.collectionspace.services.person.PersonTermGroupList;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.SchoolOrStyleList;
import org.collectionspace.services.person.StructuredDateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PersonAuthorityClientUtils.
 */
public class PersonAuthorityClientUtils {
    
    /** The Constant logger. */
    private static final Logger logger =
        LoggerFactory.getLogger(PersonAuthorityClientUtils.class);
	private static final ServiceRequestType READ_REQ = ServiceRequestType.READ;
    static private final Random random = new Random(System.currentTimeMillis());

    /**
     * @param csid the id of the PersonAuthority
     * @param client if null, creates a new client
     * @return
     * @throws Exception 
     */
    public static String getAuthorityRefName(String csid, PersonClient client) throws Exception{
    	if (client == null) {
    		client = new PersonClient();
    	}
        Response res = client.read(csid);
        try {
	        int statusCode = res.getStatus();
	        if(!READ_REQ.isValidStatusCode(statusCode)
	        	||(statusCode != CollectionSpaceClientUtils.STATUS_OK)) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	            PersonauthoritiesCommon personAuthority = 
	            	(PersonauthoritiesCommon) CollectionSpaceClientUtils.extractPart(input,
	                    client.getCommonPartName(), PersonauthoritiesCommon.class);
		        if(personAuthority == null) {
		    		throw new RuntimeException("Null personAuthority returned from service.");
		        }
	            return personAuthority.getRefName();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.close();
        }
    }

    /**
     * @param csid the id of the PersonAuthority
     * @param client if null, creates a new client
     * @return
     * @throws Exception 
     */
    public static String getPersonRefName(String inAuthority, String csid, PersonClient client) throws Exception{
    	if ( client == null) {
    		client = new PersonClient();
    	}
        Response res = client.readItem(inAuthority, csid);
        try {
	        int statusCode = res.getStatus();
	        if(!READ_REQ.isValidStatusCode(statusCode)
		        	||(statusCode != CollectionSpaceClientUtils.STATUS_OK)) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	            PersonsCommon person = 
	            	(PersonsCommon) CollectionSpaceClientUtils.extractPart(input,
	                    client.getItemCommonPartName(), PersonsCommon.class);
		        if (person == null) {
		    		throw new RuntimeException("Null person returned from service.");
		        }
	            return person.getRefName();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.close();
        }
    }
    
    /**
     * Creates the person authority instance.
     *
     * @param displayName the display name
     * @param shortIdentifier the short Id 
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createPersonAuthorityInstance(String displayName, String shortIdentifier, String headerLabel ) {
        PersonauthoritiesCommon personAuthority = new PersonauthoritiesCommon();
        personAuthority.setDisplayName(displayName);
        personAuthority.setShortIdentifier(shortIdentifier);
        //String refName = createPersonAuthRefName(shortIdentifier, displayName);
        //personAuthority.setRefName(refName);
        personAuthority.setVocabType("PersonAuthority");
        PoxPayloadOut multipart = new PoxPayloadOut(PersonClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(personAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if (logger.isDebugEnabled()) {
        	logger.debug("To be created, personAuthority common: ", 
        				personAuthority, PersonauthoritiesCommon.class);
        }

        return multipart;
    }

    /*
     * Create a very simple Person term -just a short ID and display name.
     */
	public static PoxPayloadOut createPersonInstance(String shortIdentifier, String displayName,
			String headerLabel) {
		List<PersonTermGroup> terms = getTermGroupInstance(shortIdentifier, displayName);
		
		Map<String, String> personInfo = new HashMap<String, String>();
    	personInfo.put(PersonJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);

		return createPersonInstance(null, null, personInfo, terms, null, headerLabel);
	}
    
    /**
     * Creates a person instance.
     *
     * @param inAuthority the owning authority
     * @param personAuthRefName the owning Authority ref name
     * @param personInfo the person info
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createPersonInstance(
    		String inAuthority,
    		String personAuthRefName,
    		Map<String, String> personInfo,
            List<PersonTermGroup> terms,
    		String headerLabel) {
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        final Map<String, List<String>> EMPTY_PERSON_REPEATABLES_INFO = new HashMap<String, List<String>>();
        return createPersonInstance(inAuthority, null /*personAuthRefName*/,
                personInfo, terms, EMPTY_PERSON_REPEATABLES_INFO, headerLabel);
    }

    /**
     * Creates a person instance.
     *
     * @param inAuthority the owning authority
     * @param personAuthRefName the owning Authority ref name
     * @param personInfo the person info
     * @param terms a list of Person terms
     * @param personRepeatablesInfo names and values of repeatable scalar fields in the Person record
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createPersonInstance(
    		String inAuthority,
    		String personAuthRefName,
    		Map<String, String> personInfo,
            List<PersonTermGroup> terms,
            Map<String, List<String>> personRepeatablesInfo,
            String headerLabel) {
    	
        PersonsCommon person = new PersonsCommon();
        person.setInAuthority(inAuthority);
    	String shortId = personInfo.get(PersonJAXBSchema.SHORT_IDENTIFIER);
    	if (shortId == null || shortId.isEmpty()) {
    		throw new IllegalArgumentException("shortIdentifier cannot be null or empty");
    	}      	
    	person.setShortIdentifier(shortId);
    	
        if (personInfo != null) {
        	String value;

			if ((value = personInfo.get(PersonJAXBSchema.BIRTH_DATE)) != null) {
				StructuredDateGroup birthDate = new StructuredDateGroup();
				birthDate.setDateDisplayDate(value);
				person.setBirthDateGroup(birthDate);
			}
			if ((value = personInfo.get(PersonJAXBSchema.DEATH_DATE)) != null) {
				StructuredDateGroup deathDate = new StructuredDateGroup();
				deathDate.setDateDisplayDate(value);
				person.setDeathDateGroup(deathDate);
			}
			if ((value = personInfo.get(PersonJAXBSchema.BIRTH_PLACE)) != null)
				person.setBirthPlace(value);
			if ((value = personInfo.get(PersonJAXBSchema.DEATH_PLACE)) != null)
				person.setDeathPlace(value);
			if ((value = personInfo.get(PersonJAXBSchema.GENDER)) != null)
				person.setGender(value);
			if ((value = personInfo.get(PersonJAXBSchema.BIO_NOTE)) != null)
				person.setBioNote(value);
			if ((value = personInfo.get(PersonJAXBSchema.NAME_NOTE)) != null)
				person.setNameNote(value);
        }
        
        // Set values in the Term Information Group
        PersonTermGroupList termList = new PersonTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getPersonTermGroup().addAll(terms); 
        person.setPersonTermGroupList(termList);
        
		if (personRepeatablesInfo != null) {
			List<String> values = null;

			if ((values = (List<String>) personRepeatablesInfo.get(PersonJAXBSchema.GROUPS)) != null) {
				GroupList groupsList = new GroupList();
				List<String> groups = groupsList.getGroup();
				groups.addAll(values);
				person.setGroups(groupsList);
			}
			if ((values = (List<String>) personRepeatablesInfo.get(PersonJAXBSchema.NATIONALITIES)) != null) {
				NationalityList nationalitiesList = new NationalityList();
				List<String> nationalities = nationalitiesList.getNationality();
				nationalities.addAll(values);
				person.setNationalities(nationalitiesList);
			}

			if ((values = (List<String>) personRepeatablesInfo.get(PersonJAXBSchema.OCCUPATIONS)) != null) {
				OccupationList occupationsList = new OccupationList();
				List<String> occupations = occupationsList.getOccupation();
				occupations.addAll(values);
				person.setOccupations(occupationsList);
			}
			if ((values = (List<String>) personRepeatablesInfo.get(PersonJAXBSchema.SCHOOLS_OR_STYLES)) != null) {
				SchoolOrStyleList schoolOrStyleList = new SchoolOrStyleList();
				List<String> schoolsOrStyles = schoolOrStyleList.getSchoolOrStyle();
				schoolsOrStyles.addAll(values);
				person.setSchoolsOrStyles(schoolOrStyleList);
			}
		}

        PoxPayloadOut multipart = new PoxPayloadOut(PersonClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(person, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if (logger.isDebugEnabled()) {
        	logger.debug("to be created, person common ", person, PersonsCommon.class);
        }

        return multipart;
    }
    
    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param personAuthorityRefName the person authority ref name
     * @param personMap the person map. PersonJAXBSchema.SHORT_IDENTIFIER is REQUIRED.
     * @param client the client
     * @return the string
     */
    public static String createItemInAuthority(String vcsid, 
    		String personAuthorityRefName, Map<String,String> personMap,
                List<PersonTermGroup> terms, Map<String, List<String>> personRepeatablesMap,
                PersonClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
        
        String displayName = "";
        if (terms !=null && terms.size() > 0) {
            displayName = terms.get(0).getTermDisplayName();
        }
    	
    	if(logger.isDebugEnabled()){
    		logger.debug("Creating item with display name: \"" + displayName
    				+"\" in personAuthority: \"" + vcsid +"\"");
    	}
    	PoxPayloadOut multipart = 
    		createPersonInstance(vcsid, null /*personAuthorityRefName*/,
    			personMap, terms, personRepeatablesMap, client.getItemCommonPartName());
    	
    	String result = null;
    	Response res = client.createItem(vcsid, multipart);
    	try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+personMap.get(PersonJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in personAuthority: \"" + vcsid //personAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+personMap.get(PersonJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in personAuthority: \"" + vcsid /*personAuthorityRefName*/ +"\", Status:"+ statusCode);
	    	}
	
	    	result = extractId(res);
    	} finally {
    		res.close();
    	}
    	
    	return result;
    }

    /**
     * Creates the personAuthority ref name.
     *
     * @param shortId the personAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    /*
    public static String createPersonAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:personauthority:name("
    			+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }
    */

    /**
     * Creates the person ref name.
     *
     * @param personAuthRefName the person auth ref name
     * @param shortId the person shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    /*
    public static String createPersonRefName(
    						String personAuthRefName, String shortId, String displaySuffix) {
    	String refName = personAuthRefName+":person:name("+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }
    */

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    public static String extractId(Response res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        // FIXME: This may throw an NPE if the Location: header isn't present
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
            String birthDate, String deathDate) {
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
                 newStr.append(dateSep);     // Put this in whether there is a death date or not
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
    
    private static List<PersonTermGroup> getTermGroupInstance(String shortIdentifier) {
    	return getTermGroupInstance(shortIdentifier, shortIdentifier);
    }
    
    public static List<PersonTermGroup> getTermGroupInstance(String shortIdentifier, String displayName) {
        if (Tools.isBlank(shortIdentifier)) {
        	shortIdentifier = getGeneratedIdentifier();
        }
        if (Tools.isBlank(shortIdentifier)) {
            displayName = shortIdentifier;
        }
        
        List<PersonTermGroup> terms = new ArrayList<PersonTermGroup>();
        PersonTermGroup term = new PersonTermGroup();
        term.setTermDisplayName(displayName);
        term.setTermName(shortIdentifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + createIdentifier();
   }
    
    private static String createIdentifier() {
        long identifier = System.currentTimeMillis() + random.nextInt();
        return Long.toString(identifier);
    }
}
