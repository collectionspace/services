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

package org.collectionspace.services.location.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.client.LocationAuthorityClientUtils;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.location.LocationauthoritiesCommon;
import org.collectionspace.services.location.LocationauthoritiesCommonList;
import org.collectionspace.services.location.LocationsCommon;
import org.collectionspace.services.location.LocationsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.PoxPayloadOut;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocationAuthorityServiceTest, carries out tests against a
 * deployed and running LocationAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class LocationAuthorityBaseImport {
    private static final Logger logger =
        LoggerFactory.getLogger(LocationAuthorityBaseImport.class);

    // Instance variables specific to this test.
    private LocationAuthorityClient client = new LocationAuthorityClient();

    public void createLocationAuthority(String locationAuthorityName, 
    		List<Map<String, String>> locationMaps ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create locationAuthority: \"" + locationAuthorityName +"\"");
    	}



        String displaySuffix = "displayName-" + System.currentTimeMillis(); //TODO: this is just made-up.  Laramie20100728 temp fix, made-up displaySuffix.
        String baseLocationRefName = LocationAuthorityClientUtils.createLocationAuthRefName(locationAuthorityName, displaySuffix);//TODO: make displaySuffix correct. Laramie20100729 temp fix, made-up displaySuffix.  was (locationAuthorityName, false);
        String fullLocationRefName = LocationAuthorityClientUtils.createLocationAuthRefName(locationAuthorityName, displaySuffix);//TODO: make displaySuffix correct. Laramie20100729 temp fix, made-up displaySuffix.  was (locationAuthorityName, true);

        PoxPayloadOut multipart =  LocationAuthorityClientUtils.createLocationAuthorityInstance(locationAuthorityName, fullLocationRefName, client.getCommonPartName());
        ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+locationAuthorityName
    				+"\" "+ LocationAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+locationAuthorityName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newLocationAuthorityId = LocationAuthorityClientUtils.extractId(res);
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Created locationAuthorityulary: \"" + locationAuthorityName +"\" ID:"
    				+newLocationAuthorityId );
    	}
    	for(Map<String,String> locationMap : locationMaps){
    		LocationAuthorityClientUtils.createItemInAuthority(
   				newLocationAuthorityId, baseLocationRefName, locationMap, client);
    	}
    }
    
	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		logger.info("LocationAuthorityBaseImport starting...");

		LocationAuthorityBaseImport pabi = new LocationAuthorityBaseImport();
		final String demoLocationAuthorityName = "Demo Location Authority";

		/* Strings are:  
			shortName, longName, nameAdditions, contactName, 
	        foundingDate, dissolutionDate, foundingPlace, function, description
         */		


        /*   TODO: Laramie20100728 This is broken.  Removing for now.
        Map<String, String> johnWayneMap = new HashMap<String,String>();
        johnWayneMap.put(LocationJAXBSchema.FORE_NAME, "John");
        johnWayneMap.put(LocationJAXBSchema.SUR_NAME, "Wayne");
        johnWayneMap.put(LocationJAXBSchema.GENDER, "male");
        johnWayneMap.put(LocationJAXBSchema.BIRTH_DATE, "May 26, 1907");
        johnWayneMap.put(LocationJAXBSchema.BIRTH_PLACE, "Winterset, Iowa");
        johnWayneMap.put(LocationJAXBSchema.DEATH_DATE, "June 11, 1979");
        johnWayneMap.put(LocationJAXBSchema.BIO_NOTE, "born Marion Robert Morrison and better" +
        		"known by his stage name John Wayne, was an American film actor, director " +
        		"and producer. He epitomized rugged masculinity and has become an enduring " +
        		"American icon. He is famous for his distinctive voice, walk and height. " +
        		"He was also known for his conservative political views and his support in " +
        		"the 1950s for anti-communist positions.");
        Map<String, String> patrickSchmitzMap = new HashMap<String,String>();
        patrickSchmitzMap.put(LocationJAXBSchema.FORE_NAME, "Patrick");
        patrickSchmitzMap.put(LocationJAXBSchema.SUR_NAME, "Schmitz");
        patrickSchmitzMap.put(LocationJAXBSchema.GENDER, "male");
        patrickSchmitzMap.put(LocationJAXBSchema.BIRTH_DATE, "7/15/1960");
        patrickSchmitzMap.put(LocationJAXBSchema.BIRTH_PLACE, "MI");
        Map<String, String> janeDoeMap = new HashMap<String,String>();
        janeDoeMap.put(LocationJAXBSchema.FORE_NAME, "Jane");
        janeDoeMap.put(LocationJAXBSchema.SUR_NAME, "Doe");
        janeDoeMap.put(LocationJAXBSchema.GENDER, "female");
        janeDoeMap.put(LocationJAXBSchema.BIRTH_DATE, "7/04/1901");
        janeDoeMap.put(LocationJAXBSchema.DEATH_DATE, "4/01/1999");
        janeDoeMap.put(LocationJAXBSchema.BIRTH_PLACE, "Anytown, USA");
        janeDoeMap.put(LocationJAXBSchema.BIRTH_PLACE, "Nowheresville, USA");
        List<Map<String, String>> locationsMaps = 
        	Arrays.asList(johnWayneMap, patrickSchmitzMap, janeDoeMap );

        pabi.createLocationAuthority(demoLocationAuthorityName, locationsMaps);
        */
        logger.info("LocationAuthorityBaseImport test is empty because LocationJAXBSchema had code changes.  See org.collectionspace.services.location.importer.LocationAuthorityBaseImport");
		logger.info("LocationAuthorityBaseImport complete.");
	}
}
