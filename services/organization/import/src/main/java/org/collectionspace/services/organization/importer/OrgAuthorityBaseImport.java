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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
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

    public void createOrgAuthority(String orgAuthorityName, List<String> enumValues ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create orgAuthority: \"" + orgAuthorityName +"\"");
    	}
    	MultipartOutput multipart = createOrgAuthorityInstance(orgAuthorityName, 
    			createRefName(orgAuthorityName), "enum");
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+orgAuthorityName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+orgAuthorityName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newOrgAuthorityId = extractId(res);
    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Created orgAuthorityulary: \"" + orgAuthorityName +"\" ID:"
    				+newOrgAuthorityId );
    	}
    	for(String itemName : enumValues){
    		createItemInAuthority(newOrgAuthorityId, orgAuthorityName, itemName, createRefName(itemName));
    	}
    }
    
    private String createItemInAuthority(String vcsid, String orgAuthorityName, String itemName, String refName) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+itemName+"\" in orgAuthorityulary: \"" + orgAuthorityName +"\"");
    	}
    	MultipartOutput multipart = createOrganizationInstance(itemName, refName,
    			null, null, null, null, null, null, null, null, null );
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \""+itemName
    				+"\" in orgAuthority: \"" + orgAuthorityName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+itemName
    				+"\" in orgAuthority: \"" + orgAuthorityName +"\", Status:"+ statusCode);
    	}

    	return extractId(res);
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------

    private MultipartOutput createOrgAuthorityInstance(
    		String displayName, String refName, String vocabType) {
        OrgauthoritiesCommon orgAuthority = new OrgauthoritiesCommon();
        orgAuthority.setDisplayName(displayName);
        orgAuthority.setRefName(refName);
        orgAuthority.setVocabType(vocabType);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(orgAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getCommonPartName());

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, orgAuthority common ", 
        				orgAuthority, OrgauthoritiesCommon.class);
        }

        return multipart;
    }

    private MultipartOutput createOrganizationInstance(String inAuthority,
        String shortName, String refName, String longName, 
        String nameAdditions, String contactName, 
        String foundingDate, String dissolutionDate, String foundingPlace,
        String function, String description ) {
        OrganizationsCommon organization = new OrganizationsCommon();
        organization.setShortName(shortName);
        if(refName!=null)
        	organization.setRefName(refName);
        if(longName!=null)
        	organization.setLongName(longName);
        if(nameAdditions!=null)
        	organization.setNameAdditions(nameAdditions);
        if(contactName!=null)
        	organization.setContactName(contactName);
        if(foundingDate!=null)
        	organization.setFoundingDate(foundingDate);
        if(dissolutionDate!=null)
        	organization.setDissolutionDate(dissolutionDate);
        if(foundingPlace!=null)
        	organization.setFoundingPlace(foundingPlace);
        if(function!=null)
        	organization.setFunction(function);
        if(description!=null)
        	organization.setDescription(description);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(organization,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, organization common ", organization, OrganizationsCommon.class);
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
    
    protected String createRefName(String displayName) {
    	return displayName.replaceAll("\\W", "");
    }

	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		logger.info("OrgAuthorityBaseImport starting...");

		OrgAuthorityBaseImport oabi = new OrgAuthorityBaseImport();
		final String acquisitionMethodsOrgAuthorityName = "Acquisition Methods";
		final String entryMethodsOrgAuthorityName = "Entry Methods";
		final String entryReasonsOrgAuthorityName = "Entry Reasons";
		final String responsibleDeptsOrgAuthorityName = "Responsible Departments";

		List<String> acquisitionMethodsEnumValues = 
			Arrays.asList("Gift","Purchase","Exchange","Transfer","Treasure");
		List<String> entryMethodsEnumValues = 
			Arrays.asList("In person","Post","Found on doorstep");
		List<String> entryReasonsEnumValues = 
			Arrays.asList("Enquiry","Commission","Loan");
		List<String> respDeptNamesEnumValues = 
			Arrays.asList("Antiquities","Architecture and Design","Decorative Arts",
									"Ethnography","Herpetology","Media and Performance Art",
									"Paintings and Sculpture","Paleobotany","Photographs",
									"Prints and Drawings");

		oabi.createOrgAuthority(acquisitionMethodsOrgAuthorityName, acquisitionMethodsEnumValues);
		oabi.createOrgAuthority(entryMethodsOrgAuthorityName, entryMethodsEnumValues);
		oabi.createOrgAuthority(entryReasonsOrgAuthorityName, entryReasonsEnumValues);
		oabi.createOrgAuthority(responsibleDeptsOrgAuthorityName, respDeptNamesEnumValues);

		logger.info("OrgAuthorityBaseImport complete.");
	}
}
