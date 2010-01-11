/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.collectionspace.services.person.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.collectionspace.services.PersonJAXBSchema;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonsCommon;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PersonAuthorityServiceTest, carries out tests against a
 * deployed and running PersonAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class PersonAuthorityBaseImport {
    private static final Logger logger =
        LoggerFactory.getLogger(PersonAuthorityBaseImport.class);

    // Instance variables specific to this test.
    private PersonAuthorityClient client = new PersonAuthorityClient();
    final String SERVICE_PATH_COMPONENT = "personauthorities";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";

    public void createPersonAuthority(String personAuthorityName, 
    		List<Map<String, String>> personMaps ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create personAuthority: \"" + personAuthorityName +"\"");
    	}
    	String basePersonRefName = createPersonAuthRefName(personAuthorityName);
    	String fullPersonRefName = basePersonRefName+"'"+personAuthorityName+"'";
    	MultipartOutput multipart = createPersonAuthorityInstance(personAuthorityName, 
    			fullPersonRefName);
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+personAuthorityName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+personAuthorityName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newPersonAuthorityId = extractId(res);
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Created personAuthorityulary: \"" + personAuthorityName +"\" ID:"
    				+newPersonAuthorityId );
    	}
    	for(Map<String,String> personMap : personMaps){
    		createItemInAuthority(newPersonAuthorityId, basePersonRefName, personMap);
    	}
    }
    
    private String createItemInAuthority(String vcsid, 
    		String personAuthorityRefName, Map<String,String> personMap) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	String foreName = personMap.get(PersonJAXBSchema.FORE_NAME);
    	String middleName = personMap.get(PersonJAXBSchema.MIDDLE_NAME);
    	String surName = personMap.get(PersonJAXBSchema.SUR_NAME);
    	String birthDate = personMap.get(PersonJAXBSchema.BIRTH_DATE);
    	String deathDate = personMap.get(PersonJAXBSchema.DEATH_DATE);
    	StringBuilder builtName = new StringBuilder();
    	if(foreName!=null)
    		builtName.append(foreName);
    	if(middleName!=null)
    		builtName.append(middleName);
    	if(surName!=null)
    		builtName.append(surName);
    	if(birthDate!=null)
    		builtName.append(birthDate);
		builtName.append("-");
    	if(deathDate!=null)
    		builtName.append(deathDate);
    	String refName = createPersonRefName(personAuthorityRefName, builtName.toString());

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+builtName.toString()
    				+"\" in personAuthorityulary: \"" + personAuthorityRefName +"\"");
    	}
    	MultipartOutput multipart = createPersonInstance( vcsid, refName,
    			personMap );
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

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

    	return extractId(res);
    }

    // ---------------------------------------------------------------
    // Utility methods used by methods above
    // ---------------------------------------------------------------

    private MultipartOutput createPersonAuthorityInstance(
    		String displayName, String refName ) {
        PersonauthoritiesCommon personAuthority = new PersonauthoritiesCommon();
        personAuthority.setDisplayName(displayName);
        personAuthority.setRefName(refName);
        personAuthority.setVocabType("PersonAuthority");
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(personAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, personAuthority common ", 
        				personAuthority, PersonauthoritiesCommon.class);
        }

        return multipart;
    }

    private MultipartOutput createPersonInstance(String inAuthority, 
    		String refName, Map<String, String> personInfo ) {
    	/*
            String foreName, String middleName, String lastName, 
            String initials, String salutations, String title, String nameAdditions,  
            String birthDate, String deathDate, String birthPlace, String deathPlace, 
            String group, String nationality, String gender, String occupation, 
            String schoolOrStyle, String bioNote, String nameNote ) {
          */
            PersonsCommon person = new PersonsCommon();
            person.setInAuthority(inAuthority);
           	person.setRefName(refName);
           	String value = null;
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
            commonPart.getHeaders().add("label", client.getItemCommonPartName());

            if(logger.isDebugEnabled()){
                logger.debug("to be created, person common"+person);
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
    protected String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode + "' in response is NOT within the expected set: " +
                requestType.validStatusCodesAsString();
    }

    protected String extractId(ClientResponse<Response> res) {
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
    
    protected String createPersonAuthRefName(String personAuthorityName) {
    	return "urn:cspace:org.collectionspace.demo:personauthority:name("
    			+personAuthorityName+")";
    }

    protected String createPersonRefName(
    						String personAuthRefName, String personName) {
    	return personAuthRefName+":person:name("+personName+")";
    }

	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		logger.info("PersonAuthorityBaseImport starting...");

		PersonAuthorityBaseImport pabi = new PersonAuthorityBaseImport();
		final String demoPersonAuthorityName = "Demo Person Authority";

		/* Strings are:  
			shortName, longName, nameAdditions, contactName, 
	        foundingDate, dissolutionDate, foundingPlace, function, description
         */		
        Map<String, String> johnWayneMap = new HashMap<String,String>();
        johnWayneMap.put(PersonJAXBSchema.FORE_NAME, "John");
        johnWayneMap.put(PersonJAXBSchema.SUR_NAME, "Wayne");
        johnWayneMap.put(PersonJAXBSchema.GENDER, "male");
        johnWayneMap.put(PersonJAXBSchema.BIRTH_DATE, "May 26, 1907");
        johnWayneMap.put(PersonJAXBSchema.BIRTH_PLACE, "Winterset, Iowa");
        johnWayneMap.put(PersonJAXBSchema.DEATH_DATE, "June 11, 1979");
        johnWayneMap.put(PersonJAXBSchema.BIO_NOTE, "born Marion Robert Morrison and better" +
        		"known by his stage name John Wayne, was an American film actor, director " +
        		"and producer. He epitomized rugged masculinity and has become an enduring " +
        		"American icon. He is famous for his distinctive voice, walk and height. " +
        		"He was also known for his conservative political views and his support in " +
        		"the 1950s for anti-communist positions.");
        Map<String, String> patrickSchmitzMap = new HashMap<String,String>();
        patrickSchmitzMap.put(PersonJAXBSchema.FORE_NAME, "Patrick");
        patrickSchmitzMap.put(PersonJAXBSchema.SUR_NAME, "Schmitz");
        patrickSchmitzMap.put(PersonJAXBSchema.GENDER, "male");
        patrickSchmitzMap.put(PersonJAXBSchema.BIRTH_DATE, "7/15/1960");
        patrickSchmitzMap.put(PersonJAXBSchema.BIRTH_PLACE, "MI");
        Map<String, String> janeDoeMap = new HashMap<String,String>();
        janeDoeMap.put(PersonJAXBSchema.FORE_NAME, "Jane");
        janeDoeMap.put(PersonJAXBSchema.SUR_NAME, "Doe");
        janeDoeMap.put(PersonJAXBSchema.GENDER, "female");
        janeDoeMap.put(PersonJAXBSchema.BIRTH_DATE, "7/04/1901");
        janeDoeMap.put(PersonJAXBSchema.DEATH_DATE, "4/01/1999");
        janeDoeMap.put(PersonJAXBSchema.BIRTH_PLACE, "Anytown, USA");
        janeDoeMap.put(PersonJAXBSchema.BIRTH_PLACE, "Nowheresville, USA");
        List<Map<String, String>> personsMaps = 
        	Arrays.asList(johnWayneMap, patrickSchmitzMap, janeDoeMap );

        pabi.createPersonAuthority(demoPersonAuthorityName, personsMaps);

		logger.info("PersonAuthorityBaseImport complete.");
	}
}
