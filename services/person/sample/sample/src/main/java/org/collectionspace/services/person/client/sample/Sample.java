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

package org.collectionspace.services.person.client.sample;

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
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PersonAuthority Sample, carries out tests against a
 * deployed and running PersonAuthority Service.
 *
 * $LastChangedRevision: 1055 $
 * $LastChangedDate: 2009-12-09 12:25:15 -0800 (Wed, 09 Dec 2009) $
 */
public class Sample {
    private static final Logger logger =
        LoggerFactory.getLogger(Sample.class);

    // Instance variables specific to this test.
    private PersonAuthorityClient client = new PersonAuthorityClient();
    final String SERVICE_PATH_COMPONENT = "persons";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";


    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    public void createPersonAuthority(String personAuthorityName, 
    		List<Map<String, String>> personMaps ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	logger.info("Import: Create personAuthority: \"" + personAuthorityName +"\"");


        String displaySuffix = "displayName-" + System.currentTimeMillis(); //TODO: Laramie20100728 temp fix, made-up displaySuffix.
        String basePersonRefName = PersonAuthorityClientUtils.createPersonAuthRefName(personAuthorityName, displaySuffix);//TODO: Laramie20100728 temp fix  was personAuthorityName, false
    	String fullPersonRefName = PersonAuthorityClientUtils.createPersonAuthRefName(personAuthorityName, displaySuffix); //TODO: Laramie20100728 temp fix  was personAuthorityName, true
    	PoxPayloadOut multipart = 
    		PersonAuthorityClientUtils.createPersonAuthorityInstance(
  				personAuthorityName, fullPersonRefName, client.getCommonPartName() );
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+personAuthorityName
    				+"\" "+ PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+personAuthorityName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newPersonAuthId = PersonAuthorityClientUtils.extractId(res);
        logger.info("Import: Created personAuthority: \"" + personAuthorityName +"\" ID:"
    				+newPersonAuthId );
        
        // Add items to the personAuthority
    	for(Map<String,String> personMap : personMaps){
    		createItemInAuthority(newPersonAuthId, basePersonRefName, personMap);
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

        String displaySuffix = "displayName-" + System.currentTimeMillis(); //TODO: Laramie20100728 temp fix, made-up displaySuffix.

    	String refName = PersonAuthorityClientUtils.createPersonRefName(personAuthorityRefName, builtName.toString(), displaySuffix); //TODO was ...,true);
    	logger.info("Import: Create Item: \""+refName+"\" in personAuthority: \"" + personAuthorityRefName +"\"");

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+builtName.toString()
    				+"\" in personAuthorityulary: \"" + personAuthorityRefName +"\"");
    	}
    	PoxPayloadOut multipart = createPersonInstance( vcsid, refName,
    			personMap );
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \""+refName
    				+"\" in personAuthority: \"" + personAuthorityRefName
    				+"\" "+ PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+refName
    				+"\" in personAuthority: \"" + personAuthorityRefName +"\", Status:"+ statusCode);
    	}

    	return PersonAuthorityClientUtils.extractId(res);
    }


   // ---------------------------------------------------------------
   // Read
   // ---------------------------------------------------------------

   private PersonauthoritiesCommonList readPersonAuthorities() {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        ClientResponse<PersonauthoritiesCommonList> res = client.readList();
        PersonauthoritiesCommonList list = res.getEntity();

        int statusCode = res.getStatus();
    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read list of personAuthorities: "
                + PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when reading " +
                "list of personAuthorities, Status:"+ statusCode);
    	}

        return list;
   }

    private List<String> readPersonAuthorityIds(PersonauthoritiesCommonList list) {

        List<String> ids = new ArrayList<String>();
        List<PersonauthoritiesCommonList.PersonauthorityListItem> personAuthorities =
            list.getPersonauthorityListItem();
        for (PersonauthoritiesCommonList.PersonauthorityListItem personAuthority : personAuthorities) {
            ids.add(personAuthority.getCsid());
        }
        return ids;
   }
    
   private PersonauthoritiesCommon readPersonAuthority(String personAuthId) {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        PersonauthoritiesCommon personAuthority = null;
        try {
            ClientResponse<String> res = client.read(personAuthId);
            int statusCode = res.getStatus();
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not read personAuthority"
                    + PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when reading " +
                    "personAuthority, Status:"+ statusCode);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            personAuthority = (PersonauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), PersonauthoritiesCommon.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not read personAuthority: ", e);
        }

        return personAuthority;
    }

    private PersonsCommonList readItemsInPersonAuth(String personAuthId) {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        ClientResponse<PersonsCommonList> res = client.readItemList(personAuthId, "", ""); //TODO: Laramie201007289  added empty strings to satisfy api
        PersonsCommonList list = res.getEntity();

        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read items in personAuthority: "
                + PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when reading " +
                "items in personAuthority, Status:"+ statusCode);
    	}

        return list;
    }

    private List<String> readPersonIds(PersonsCommonList list) {

        List<String> ids = new ArrayList<String>();
        List<PersonsCommonList.PersonListItem> items =
            list.getPersonListItem();
        for (PersonsCommonList.PersonListItem item : items) {
            ids.add(item.getCsid());
        }
        return ids;
   }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    private void deletePersonAuthority(String vcsid) {
         // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

        ClientResponse<Response> res = client.delete(vcsid);
        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not delete personAuthority: "
                + PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when deleting " +
                "personAuthority, Status:"+ statusCode);
    	}
    }

    private void deleteAllPersonAuthorities() {
        List<String> ids = readPersonAuthorityIds(readPersonAuthorities());
        for (String id : ids) {
            deletePersonAuthority(id);
        }
    }

        private void deletePerson(String vcsid, String itemcsid) {
         // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

        ClientResponse<Response> res = client.deleteItem(vcsid, itemcsid);
        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not delete personAuthority item: "
                + PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when deleting " +
                "personAuthority item, Status:"+ statusCode);
    	}
    }

    private void deleteAllItemsForPersonAuth(String personAuthId) {
        List<String> itemIds = readPersonIds(readItemsInPersonAuth(personAuthId));
        for (String itemId : itemIds) {
            deletePerson(personAuthId, itemId);
        }
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    /*
    private PoxPayloadOut createPersonAuthorityInstance(
    		String displayName, String refName ) {
        PersonauthoritiesCommon personAuthority = new PersonauthoritiesCommon();
        personAuthority.setDisplayName(displayName);
        personAuthority.setRefName(refName);
        personAuthority.setVocabType("PersonAuthority");
        PoxPayloadOut multipart = new PoxPayloadOut();
        OutputPart commonPart = multipart.addPart(personAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, personAuthority common ",
        				personAuthority, PersonauthoritiesCommon.class);
        }

        return multipart;
    }
    */

    private PoxPayloadOut createPersonInstance(String inAuthority, 
    		String refName, Map<String, String> personInfo ) {
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
            if((value = (String)personInfo.get(PersonJAXBSchema.BIRTH_DATE))!=null) {
                StructuredDateGroup birthDate = new StructuredDateGroup();
                birthDate.setDateDisplayDate(value);
                person.setBirthDateGroup(birthDate);
            }
            if((value = (String)personInfo.get(PersonJAXBSchema.DEATH_DATE))!=null) {
                StructuredDateGroup deathDate = new StructuredDateGroup();
                deathDate.setDateDisplayDate(value);
                person.setDeathDateGroup(deathDate);
            }
            if((value = (String)personInfo.get(PersonJAXBSchema.BIRTH_PLACE))!=null)
            	person.setBirthPlace(value);
            if((value = (String)personInfo.get(PersonJAXBSchema.DEATH_PLACE))!=null)
            	person.setDeathPlace(value);

            /* TODO: Laramie20100728  removed missing member calls
            if((value = (String)personInfo.get(PersonJAXBSchema.GROUP))!=null)
            	person.setGroup(value);
            if((value = (String)personInfo.get(PersonJAXBSchema.NATIONALITY))!=null)
            	person.setNationality(value);
            if((value = (String)personInfo.get(PersonJAXBSchema.OCCUPATION))!=null)
            	person.setOccupation(value);
            if((value = (String)personInfo.get(PersonJAXBSchema.SCHOOL_OR_STYLE))!=null)
            	person.setSchoolOrStyle(value);
            */
        
            if((value = (String)personInfo.get(PersonJAXBSchema.GENDER))!=null)
                        person.setGender(value);
            if((value = (String)personInfo.get(PersonJAXBSchema.BIO_NOTE))!=null)
            	person.setBioNote(value);
            if((value = (String)personInfo.get(PersonJAXBSchema.NAME_NOTE))!=null)
            	person.setNameNote(value);
            PoxPayloadOut multipart = new PoxPayloadOut();
            OutputPart commonPart = multipart.addPart(person,
                MediaType.APPLICATION_XML_TYPE);
            commonPart.getHeaders().add("label", client.getItemCommonPartName());

            if(logger.isDebugEnabled()){
                logger.debug("to be created, person common"+person);
            }

            return multipart;
        }

    // Retrieve individual fields of personAuthority records.

    private String displayAllPersonAuthorities(PersonauthoritiesCommonList list) {
        StringBuffer sb = new StringBuffer();
            List<PersonauthoritiesCommonList.PersonauthorityListItem> personAuthorities =
                    list.getPersonauthorityListItem();
            int i = 0;
        for (PersonauthoritiesCommonList.PersonauthorityListItem personAuthority : personAuthorities) {
            sb.append("personAuthority [" + i + "]" + "\n");
            sb.append(displayPersonAuthorityDetails(personAuthority));
            i++;
        }
        return sb.toString();
    }

    private String displayPersonAuthorityDetails(
        PersonauthoritiesCommonList.PersonauthorityListItem personAuthority) {
            StringBuffer sb = new StringBuffer();
            sb.append("displayName=" + personAuthority.getDisplayName() + "\n");
            sb.append("vocabType=" + personAuthority.getVocabType() + "\n");
            // sb.append("csid=" + personAuthority.getCsid() + "\n");
            sb.append("URI=" + personAuthority.getUri() + "\n");
        return sb.toString();
    }

    // Retrieve individual fields of person records.

    private String displayAllPersons(PersonsCommonList list) {
        StringBuffer sb = new StringBuffer();
        List<PersonsCommonList.PersonListItem> items =
                list.getPersonListItem();
        int i = 0;
        for (PersonsCommonList.PersonListItem item : items) {
            sb.append("person [" + i + "]" + "\n");
            sb.append(displayPersonDetails(item));
            i++;
        }
        return sb.toString();
    }

    private String displayPersonDetails(
        PersonsCommonList.PersonListItem item) {
            StringBuffer sb = new StringBuffer();
            sb.append("csid=" + item.getCsid() + "\n");
            sb.append("displayName=" + item.getDisplayName() + "\n");
            // sb.append("URI=" + item.getUri() + "\n");
        return sb.toString();
    }

    private Object extractPart(PoxPayloadIn input, String label,
        Class clazz) throws Exception {
        Object obj = null;
        for(InputPart part : input.getParts()){
            String partLabel = part.getHeaders().getFirst("label");
            if(label.equalsIgnoreCase(partLabel)){
                String partStr = part.getBodyAsString();
                if(logger.isDebugEnabled()){
                    logger.debug("extracted part str=\n" + partStr);
                }
                obj = part.getBody(clazz, null);
                if(logger.isDebugEnabled()){
                    logger.debug("extracted part obj=\n", obj, clazz);
                }
                break;
            }
        }
        return obj;
    }

	public static void main(String[] args) {

        // Configure logging.
		BasicConfigurator.configure();

        logger.info("PersonAuthority Sample starting...");

		Sample sample = new Sample();
        PersonauthoritiesCommonList personAuthorities;
        List<String> personAuthIds;
        String details = "";

        // Optionally delete all personAuthorities and persons.

        boolean ENABLE_DELETE_ALL = false;
        if (ENABLE_DELETE_ALL) {

            logger.info("Deleting all persons and personAuthorities ...");

            // For each personAuthority ...
            personAuthorities = sample.readPersonAuthorities();
            personAuthIds = sample.readPersonAuthorityIds(personAuthorities);
            for (String personAuthId : personAuthIds) {
                logger.info("Deleting all persons for personAuthority ...");
                sample.deleteAllItemsForPersonAuth(personAuthId);
                logger.info("Deleting personAuthority ...");
                sample.deletePersonAuthority(personAuthId);
            }

            logger.info("Reading personAuthorities after deletion ...");
            personAuthorities = sample.readPersonAuthorities();
            details = sample.displayAllPersonAuthorities(personAuthorities);
            logger.info(details);

            logger.info("Reading items in each personAuthority after deletion ...");
            personAuthIds = sample.readPersonAuthorityIds(personAuthorities);
            for (String personAuthId : personAuthIds) {
                PersonsCommonList items = sample.readItemsInPersonAuth(personAuthId);
                details = sample.displayAllPersons(items);
                logger.info(details);
            }

        }

        // Create new authorities, each populated with persons.

        Map<String, String> johnWayneMap = new HashMap<String,String>();
        johnWayneMap.put(PersonJAXBSchema.FORE_NAME, "John");
        johnWayneMap.put(PersonJAXBSchema.SUR_NAME, "Wayne");
        johnWayneMap.put(PersonJAXBSchema.GENDER, "male");
        Map<String, String> patrickSchmitzMap = new HashMap<String,String>();
        patrickSchmitzMap.put(PersonJAXBSchema.FORE_NAME, "Patrick");
        patrickSchmitzMap.put(PersonJAXBSchema.SUR_NAME, "Schmitz");
        patrickSchmitzMap.put(PersonJAXBSchema.GENDER, "male");
        Map<String, String> janeDoeMap = new HashMap<String,String>();
        janeDoeMap.put(PersonJAXBSchema.FORE_NAME, "Jane");
        janeDoeMap.put(PersonJAXBSchema.SUR_NAME, "Doe");
        janeDoeMap.put(PersonJAXBSchema.GENDER, "female");
        List<Map<String, String>> personsMaps = 
        	Arrays.asList(johnWayneMap, patrickSchmitzMap, janeDoeMap );
        
        sample.createPersonAuthority("Sample Person Auth", personsMaps);

		logger.info("PersonAuthority Sample complete.");

        logger.info("Reading personAuthorities and items ...");
        // Get a list of personAuthorities.
        personAuthorities = sample.readPersonAuthorities();
        // For each personAuthority ...
        for (PersonauthoritiesCommonList.PersonauthorityListItem
            personAuthority : personAuthorities.getPersonauthorityListItem()) {
            // Get its display name.
            logger.info(personAuthority.getDisplayName());
            // Get a list of the persons in this personAuthority.
            PersonsCommonList items =
            	sample.readItemsInPersonAuth(personAuthority.getCsid());
            // For each person ...
            for (PersonsCommonList.PersonListItem
                item : items.getPersonListItem()) {
                // Get its short name.
                logger.info(" " + item.getDisplayName());
            }
        }

        // Sample alternate methods of reading all personAuthorities and
        // persons separately.
        boolean RUN_ADDITIONAL_SAMPLES = false;
        if (RUN_ADDITIONAL_SAMPLES) {

            logger.info("Reading all personAuthorities ...");
            details = sample.displayAllPersonAuthorities(personAuthorities);
            logger.info(details);

            logger.info("Reading all persons ...");
            personAuthIds = sample.readPersonAuthorityIds(personAuthorities);
            for (String personAuthId : personAuthIds) {
                PersonsCommonList items = sample.readItemsInPersonAuth(personAuthId);
                details = sample.displayAllPersons(items);
                logger.info(details);
            }

        }

	}

}
