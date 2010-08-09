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
import org.collectionspace.services.client.PersonAuthorityClientUtils;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.collectionspace.services.person.PersonsCommon;
import org.collectionspace.services.person.PersonsCommonList;
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

    public void createPersonAuthority(String displayName, String shortId, 
    		List<Map<String, String>> personMaps ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create personAuthority: \"" + displayName +"\"");
    	}
    	String basePersonRefName = 
    		PersonAuthorityClientUtils.createPersonAuthRefName(shortId, null);
    	MultipartOutput multipart = 
    		PersonAuthorityClientUtils.createPersonAuthorityInstance(
  				displayName, shortId, client.getCommonPartName());
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+displayName
    				+"\" "+ PersonAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+displayName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newPersonAuthorityId = PersonAuthorityClientUtils.extractId(res);
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Created personAuthorityulary: \"" + displayName +"\" ID:"
    				+newPersonAuthorityId );
    	}

       /*
        *TODO: Laramie20100728  this code is out of date, and needs to use the new API.  Commenting out for now.
        *
        *for(Map<String,String> personMap : personMaps){
    	*	PersonAuthorityClientUtils.createItemInAuthority(
   		*		newPersonAuthorityId, basePersonRefName, personMap, client);
    	*}
    	*/
        logger.error("Method PersonAuthorityBaseImport.createPersonAuthority not implemented properly.");

    }
    
	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		logger.info("PersonAuthorityBaseImport starting...");

		PersonAuthorityBaseImport pabi = new PersonAuthorityBaseImport();
		final String demoPersonAuthorityName = "Demo Person Authority";
		final String demoPersonAuthorityShortId = "demoPersonAuth";

		/* Strings are:  
			shortName, longName, nameAdditions, contactName, 
	        foundingDate, dissolutionDate, foundingPlace, function, description
         */		
        Map<String, String> johnWayneMap = new HashMap<String,String>();
        johnWayneMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, "johnWayne_Actor");
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
        patrickSchmitzMap.put(PersonJAXBSchema.SHORT_IDENTIFIER, "plSchmitz_Geek");
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

        pabi.createPersonAuthority(demoPersonAuthorityName, 
        					demoPersonAuthorityShortId, personsMaps);

		logger.info("PersonAuthorityBaseImport complete.");
	}
}
