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

    public void createOrgAuthority(String orgAuthorityName, 
    		List<List<String>> orgInfo ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create orgAuthority: \"" + orgAuthorityName +"\"");
    	}
    	String baseOrgAuthRefName = createOrgAuthRefName(orgAuthorityName);
    	String fullOrgAuthRefName = baseOrgAuthRefName+"'"+orgAuthorityName+"'";
    	MultipartOutput multipart = createOrgAuthorityInstance(orgAuthorityName, 
    			fullOrgAuthRefName);
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
    	for(List<String> orgStrings : orgInfo){
    		createItemInAuthority(newOrgAuthorityId, baseOrgAuthRefName, orgStrings);
    	}
    }
    
    private String createItemInAuthority(String vcsid, 
    		String orgAuthorityRefName, List<String> orgStrings) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
		/* Strings are:  
			shortName, longName, nameAdditions, contactName, 
	        foundingDate, dissolutionDate, foundingPlace, function, description
		 */		
    	String shortName = orgStrings.get(0);
    	String refName = createOrganizationRefName(orgAuthorityRefName, shortName)+"'"+shortName+"'";

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+shortName
    				+"\" in orgAuthorityulary: \"" + orgAuthorityRefName +"\"");
    	}
    	MultipartOutput multipart = createOrganizationInstance( vcsid, refName,
    			orgStrings );
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \""+shortName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+shortName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName +"\", Status:"+ statusCode);
    	}

    	return extractId(res);
    }

    // ---------------------------------------------------------------
    // Utility methods used by methods above
    // ---------------------------------------------------------------

    private MultipartOutput createOrgAuthorityInstance(
    		String displayName, String refName ) {
        OrgauthoritiesCommon orgAuthority = new OrgauthoritiesCommon();
        orgAuthority.setDisplayName(displayName);
        orgAuthority.setRefName(refName);
        orgAuthority.setVocabType("OrgAuthority");
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
    		String orgRefName, List<String> orgStrings){
        OrganizationsCommon organization = new OrganizationsCommon();
        organization.setInAuthority(inAuthority);
        organization.setShortName(orgStrings.get(0));
       	organization.setRefName(orgRefName);
       	String longName = orgStrings.get(1);
        if(longName!=null)
        	organization.setLongName(longName);
       	String nameAdditions = orgStrings.get(2);
        if(nameAdditions!=null)
        	organization.setNameAdditions(nameAdditions);
       	String contactName = orgStrings.get(3);
        if(contactName!=null)
        	organization.setContactName(contactName);
       	String foundingDate = orgStrings.get(4);
        if(foundingDate!=null)
        	organization.setFoundingDate(foundingDate);
       	String dissolutionDate = orgStrings.get(5);
        if(dissolutionDate!=null)
        	organization.setDissolutionDate(dissolutionDate);
       	String foundingPlace = orgStrings.get(6);
        if(foundingPlace!=null)
        	organization.setFoundingPlace(foundingPlace);
       	String function = orgStrings.get(7);
        if(function!=null)
        	organization.setFunction(function);
       	String description = orgStrings.get(8);
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
    
    protected String createOrgAuthRefName(String orgAuthorityName) {
    	return "urn:cspace:org.collectionspace.demo:orgauthority:name("
    			+orgAuthorityName+")";
    }

    protected String createOrganizationRefName(
    						String orgAuthRefName, String orgName) {
    	return orgAuthRefName+":organization:name("+orgName+")";
    }

	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		logger.info("OrgAuthorityBaseImport starting...");

		OrgAuthorityBaseImport oabi = new OrgAuthorityBaseImport();
		final String demoOrgAuthorityName = "Demo Org Authority";

		/* Strings are:  
			shortName, longName, nameAdditions, contactName, 
	        foundingDate, dissolutionDate, foundingPlace, function, description
         */		
        List<String> mmiOrgStrings = 
			Arrays.asList("MMI","Museum of the Moving Image",null,"Megan Forbes",
					"1984", null, "Astoria, NY", null, null);
        List<String> pahmaOrgStrings = 
			Arrays.asList("PAHMA","Phoebe A. Hearst Museum of Anthropology",
							"University of California, Berkeley","Michael Black",
					"1901", null, "Berkeley, CA", null, null);
        List<String> savoyOrgStrings = 
			Arrays.asList("Savoy Theatre",null,null,null,
					"1900", "1952", "New York, NY", null, null);
        List<List<String>> orgsStrings = 
        	Arrays.asList(mmiOrgStrings, pahmaOrgStrings, savoyOrgStrings );

		oabi.createOrgAuthority(demoOrgAuthorityName, orgsStrings);

		logger.info("OrgAuthorityBaseImport complete.");
	}
}
