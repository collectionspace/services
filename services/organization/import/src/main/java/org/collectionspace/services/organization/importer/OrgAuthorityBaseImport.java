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

package org.collectionspace.services.organization.importer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrgAuthorityServiceTest, carries out tests against a
 * deployed and running OrgAuthority Service.
 *
 * $LastChangedRevision: 753 $
 * $LastChangedDate: 2009-09-23 11:03:36 -0700 (Wed, 23 Sep 2009) $
 */
public class OrgAuthorityBaseImport {
    private static final Logger logger =
        LoggerFactory.getLogger(OrgAuthorityBaseImport.class);

    // Instance variables specific to this test.
    private OrgAuthorityClient client = new OrgAuthorityClient();
    final String SERVICE_PATH_COMPONENT = "orgauthorities";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";

    public void createOrgAuthority(String orgAuthorityDisplayName, String orgAuthorityShortId, 
    		List<Map<String,String>> orgInfos ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create orgAuthority: \"" + orgAuthorityShortId +"\"");
    	}
    	String baseOrgAuthRefName = OrgAuthorityClientUtils.createOrgAuthRefName(orgAuthorityShortId, null);
    	MultipartOutput multipart = 
    	    OrgAuthorityClientUtils.createOrgAuthorityInstance(
    	    		orgAuthorityDisplayName, orgAuthorityShortId, client.getCommonPartName());
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+orgAuthorityShortId
    				+"\" "+ OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+orgAuthorityShortId +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newOrgAuthorityId = OrgAuthorityClientUtils.extractId(res);
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Created orgAuthorityulary: \"" + orgAuthorityShortId +"\" ID:"
    				+newOrgAuthorityId );
    	}

        
        /*
        *TODO: Laramie20100728  this code is out of date, and needs to use the new API.  Commenting out for now.
        *
        *orgRepeatablesInfo is undefined;
    	*for(Map<String,String> orgInfo : orgInfos){
        *    OrgAuthorityClientUtils.createItemInAuthority(newOrgAuthorityId, baseOrgAuthRefName, orgInfo, orgRepeatablesInfo, client);
    	*}
    	*/
         logger.error("MethodcreateOrgAuthority(String orgAuthorityDisplayName, String orgAuthorityShortId,List<Map<String,String>> orgInfos ) not implemented properly.");
    }
    
    // ---------------------------------------------------------------
    // Utility methods used by methods above
    // ---------------------------------------------------------------

	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		logger.info("OrgAuthorityBaseImport starting...");

		OrgAuthorityBaseImport oabi = new OrgAuthorityBaseImport();
		final String demoOrgAuthorityName = "Demo Org Authority";
		final String demoOrgAuthorityShortId = "demoOrgAuth";

        Map<String, String> mmiOrgMap = new HashMap<String,String>();
        mmiOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, "mmi");
        mmiOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "MMI");
        mmiOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "Museum of the Moving Image");
        //TODO: CONTACT_NAME is not in API any more.  mmiOrgMap.put(OrganizationJAXBSchema.CONTACT_NAME, "Megan Forbes");
        mmiOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "1984");
        mmiOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "Astoria, NY");
        Map<String, String> pahmaOrgMap = new HashMap<String,String>();
        pahmaOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, "pahma");
        pahmaOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "PAHMA");
        pahmaOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "Phoebe A. Hearst Museum of Anthropology");
        pahmaOrgMap.put(OrganizationJAXBSchema.NAME_ADDITIONS, "University of California, Berkeley");
        //TODO: CONTACT_NAME is not in API any more.   pahmaOrgMap.put(OrganizationJAXBSchema.CONTACT_NAME, "Michael Black");
        pahmaOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "1901");
        pahmaOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "Berkeley, CA");
        Map<String, String> savoyOrgMap = new HashMap<String,String>();
        savoyOrgMap.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, "savoyTh");
        savoyOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "Savoy Theatre");
        savoyOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "1900");
        savoyOrgMap.put(OrganizationJAXBSchema.DISSOLUTION_DATE, "1952");
        savoyOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "New York, NY");
        List<Map<String, String>> orgMaps = 
        	Arrays.asList(mmiOrgMap, pahmaOrgMap, savoyOrgMap );

		oabi.createOrgAuthority(demoOrgAuthorityName, demoOrgAuthorityShortId, orgMaps);

		logger.info("OrgAuthorityBaseImport complete.");
	}
}
