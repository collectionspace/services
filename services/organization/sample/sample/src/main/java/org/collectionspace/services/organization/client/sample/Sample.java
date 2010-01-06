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

package org.collectionspace.services.organization.client.sample;

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
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrganizationsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrgAuthority Sample, carries out tests against a
 * deployed and running OrgAuthority Service.
 *
 * $LastChangedRevision: 1055 $
 * $LastChangedDate: 2009-12-09 12:25:15 -0800 (Wed, 09 Dec 2009) $
 */
public class Sample {
    private static final Logger logger =
        LoggerFactory.getLogger(Sample.class);

    // Instance variables specific to this test.
    private OrgAuthorityClient client = new OrgAuthorityClient();
    final String SERVICE_PATH_COMPONENT = "organizations";
    final String ITEM_SERVICE_PATH_COMPONENT = "items";


    // ---------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------

    public void createEnumeration(String orgAuthName, List<String> enumValues ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	logger.info("Import: Create orgAuthority: \"" + orgAuthName +"\"");
    	MultipartOutput multipart = createOrgAuthorityInstance(orgAuthName, 
    			createRefName(orgAuthName), "enum");
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+orgAuthName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+orgAuthName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newOrgAuthId = extractId(res);
        logger.info("Import: Created orgAuthority: \"" + orgAuthName +"\" ID:"
    				+newOrgAuthId );
        
        // Add items to the orgAuthority
    	for(String itemName : enumValues){
    		createItemInOrgAuth(newOrgAuthId, orgAuthName, itemName, createRefName(itemName));
    	}
        
    }
    
    private String createItemInOrgAuth(String vcsid, String orgAuthName, String itemName, String refName) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	logger.info("Import: Create Item: \""+itemName+"\" in orgAuthority: \"" + orgAuthName +"\"");
    	MultipartOutput multipart = createOrganizationInstance(vcsid, itemName, refName);
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \""+itemName
    				+"\" in orgAuthority: \"" + orgAuthName
    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+itemName
    				+"\" in orgAuthority: \"" + orgAuthName +"\", Status:"+ statusCode);
    	}

    	return extractId(res);
    }


   // ---------------------------------------------------------------
   // Read
   // ---------------------------------------------------------------

   private OrgauthoritiesCommonList readOrgAuthorities() {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        ClientResponse<OrgauthoritiesCommonList> res = client.readList();
        OrgauthoritiesCommonList list = res.getEntity();

        int statusCode = res.getStatus();
    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read list of orgAuthorities: "
                + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when reading " +
                "list of orgAuthorities, Status:"+ statusCode);
    	}

        return list;
   }

    private List<String> readOrgAuthorityIds(OrgauthoritiesCommonList list) {

        List<String> ids = new ArrayList<String>();
        List<OrgauthoritiesCommonList.OrgauthorityListItem> orgAuthorities =
            list.getOrgauthorityListItem();
        for (OrgauthoritiesCommonList.OrgauthorityListItem orgAuthority : orgAuthorities) {
            ids.add(orgAuthority.getCsid());
        }
        return ids;
   }
    
   private OrgauthoritiesCommon readOrgAuthority(String orgAuthId) {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        OrgauthoritiesCommon orgAuthority = null;
        try {
            ClientResponse<MultipartInput> res = client.read(orgAuthId);
            int statusCode = res.getStatus();
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not read orgAuthority"
                    + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when reading " +
                    "orgAuthority, Status:"+ statusCode);
            }
            MultipartInput input = (MultipartInput) res.getEntity();
            orgAuthority = (OrgauthoritiesCommon) extractPart(input,
                    client.getCommonPartName(), OrgauthoritiesCommon.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not read orgAuthority: ", e);
        }

        return orgAuthority;
    }

    private OrganizationsCommonList readItemsInOrgAuth(String orgAuthId) {

        // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.READ;

        // Submit the request to the service and store the response.
        ClientResponse<OrganizationsCommonList> res =
                client.readItemList(orgAuthId);
        OrganizationsCommonList list = res.getEntity();

        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read items in orgAuthority: "
                + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when reading " +
                "items in orgAuthority, Status:"+ statusCode);
    	}

        return list;
    }

    private List<String> readOrganizationIds(OrganizationsCommonList list) {

        List<String> ids = new ArrayList<String>();
        List<OrganizationsCommonList.OrganizationListItem> items =
            list.getOrganizationListItem();
        for (OrganizationsCommonList.OrganizationListItem item : items) {
            ids.add(item.getCsid());
        }
        return ids;
   }

    // ---------------------------------------------------------------
    // Delete
    // ---------------------------------------------------------------

    private void deleteOrgAuthority(String vcsid) {
         // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

        ClientResponse<Response> res = client.delete(vcsid);
        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not delete orgAuthority: "
                + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when deleting " +
                "orgAuthority, Status:"+ statusCode);
    	}
    }

    private void deleteAllOrgAuthorities() {
        List<String> ids = readOrgAuthorityIds(readOrgAuthorities());
        for (String id : ids) {
            deleteOrgAuthority(id);
        }
    }

        private void deleteOrganization(String vcsid, String itemcsid) {
         // Expected status code: 200 OK
    	int EXPECTED_STATUS_CODE = Response.Status.OK.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.DELETE;

        ClientResponse<Response> res = client.deleteItem(vcsid, itemcsid);
        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not delete orgAuthority item: "
                + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when deleting " +
                "orgAuthority item, Status:"+ statusCode);
    	}
    }

    private void deleteAllItemsForOrgAuth(String orgAuthId) {
        List<String> itemIds = readOrganizationIds(readItemsInOrgAuth(orgAuthId));
        for (String itemId : itemIds) {
            deleteOrganization(orgAuthId, itemId);
        }
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

    private MultipartOutput createOrganizationInstance(
    		String inAuthority, String displayName, String refName) {
    	OrganizationsCommon organization = new OrganizationsCommon();
    	organization.setInAuthority(inAuthority);
    	organization.setShortName(displayName);
    	organization.setRefName(refName);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(organization, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", client.getItemCommonPartName());

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, organization common ", organization, OrganizationsCommon.class);
        }

        return multipart;
    }

    // Retrieve individual fields of orgAuthority records.

    private String displayAllOrgAuthorities(OrgauthoritiesCommonList list) {
        StringBuffer sb = new StringBuffer();
            List<OrgauthoritiesCommonList.OrgauthorityListItem> orgAuthorities =
                    list.getOrgauthorityListItem();
            int i = 0;
        for (OrgauthoritiesCommonList.OrgauthorityListItem orgAuthority : orgAuthorities) {
            sb.append("orgAuthority [" + i + "]" + "\n");
            sb.append(displayOrgAuthorityDetails(orgAuthority));
            i++;
        }
        return sb.toString();
    }

    private String displayOrgAuthorityDetails(
        OrgauthoritiesCommonList.OrgauthorityListItem orgAuthority) {
            StringBuffer sb = new StringBuffer();
            sb.append("displayName=" + orgAuthority.getDisplayName() + "\n");
            sb.append("vocabType=" + orgAuthority.getVocabType() + "\n");
            // sb.append("csid=" + orgAuthority.getCsid() + "\n");
            sb.append("URI=" + orgAuthority.getUri() + "\n");
        return sb.toString();
    }

    // Retrieve individual fields of organization records.

    private String displayAllOrganizations(OrganizationsCommonList list) {
        StringBuffer sb = new StringBuffer();
        List<OrganizationsCommonList.OrganizationListItem> items =
                list.getOrganizationListItem();
        int i = 0;
        for (OrganizationsCommonList.OrganizationListItem item : items) {
            sb.append("organization [" + i + "]" + "\n");
            sb.append(displayOrganizationDetails(item));
            i++;
        }
        return sb.toString();
    }

    private String displayOrganizationDetails(
        OrganizationsCommonList.OrganizationListItem item) {
            StringBuffer sb = new StringBuffer();
            sb.append("csid=" + item.getCsid() + "\n");
            sb.append("shortName=" + item.getShortName() + "\n");
            // sb.append("URI=" + item.getUri() + "\n");
        return sb.toString();
    }

    private Object extractPart(MultipartInput input, String label,
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
        	logger.info("extractId:uri=" + uri);
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

        // Configure logging.
		BasicConfigurator.configure();

        logger.info("OrgAuthority Sample starting...");

		Sample vbi = new Sample();
        OrgauthoritiesCommonList orgAuthorities;
        List<String> orgAuthIds;
        String details = "";

        // Optionally delete all orgAuthorities and organizations.

        boolean ENABLE_DELETE_ALL = false;
        if (ENABLE_DELETE_ALL) {

            logger.info("Deleting all organizations and orgAuthorities ...");

            // For each orgAuthority ...
            orgAuthorities = vbi.readOrgAuthorities();
            orgAuthIds = vbi.readOrgAuthorityIds(orgAuthorities);
            for (String orgAuthId : orgAuthIds) {
                logger.info("Deleting all organizations for orgAuthority ...");
                vbi.deleteAllItemsForOrgAuth(orgAuthId);
                logger.info("Deleting orgAuthority ...");
                vbi.deleteOrgAuthority(orgAuthId);
            }

            logger.info("Reading orgAuthorities after deletion ...");
            orgAuthorities = vbi.readOrgAuthorities();
            details = vbi.displayAllOrgAuthorities(orgAuthorities);
            logger.info(details);

            logger.info("Reading items in each orgAuthority after deletion ...");
            orgAuthIds = vbi.readOrgAuthorityIds(orgAuthorities);
            for (String orgAuthId : orgAuthIds) {
                OrganizationsCommonList items = vbi.readItemsInOrgAuth(orgAuthId);
                details = vbi.displayAllOrganizations(items);
                logger.info(details);
            }

        }

        // Create new authorities, each populated with organizations.

				/*
		final String acquisitionMethodsVocabName = "Acquisition Methods";
		final String entryMethodsVocabName = "Entry Methods";
		final String entryReasonsVocabName = "Entry Reasons";
		final String responsibleDeptsVocabName = "Responsible Departments";

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
        
		vbi.createEnumeration(acquisitionMethodsVocabName, acquisitionMethodsEnumValues);
		vbi.createEnumeration(entryMethodsVocabName, entryMethodsEnumValues);
		vbi.createEnumeration(entryReasonsVocabName, entryReasonsEnumValues);
		vbi.createEnumeration(responsibleDeptsVocabName, respDeptNamesEnumValues);
		*/

		logger.info("OrgAuthority Sample complete.");

        logger.info("Reading orgAuthorities and items ...");
        // Get a list of orgAuthorities.
        orgAuthorities = vbi.readOrgAuthorities();
        // For each orgAuthority ...
        for (OrgauthoritiesCommonList.OrgauthorityListItem
            orgAuthority : orgAuthorities.getOrgauthorityListItem()) {
            // Get its display name.
            logger.info(orgAuthority.getDisplayName());
            // Get a list of the organizations in this orgAuthority.
            OrganizationsCommonList items =
                vbi.readItemsInOrgAuth(orgAuthority.getCsid());
            // For each organization ...
            for (OrganizationsCommonList.OrganizationListItem
                item : items.getOrganizationListItem()) {
                // Get its short name.
                logger.info(" " + item.getShortName());
            }
        }

        // Sample alternate methods of reading all orgAuthorities and
        // organizations separately.
        boolean RUN_ADDITIONAL_SAMPLES = false;
        if (RUN_ADDITIONAL_SAMPLES) {

            logger.info("Reading all orgAuthorities ...");
            details = vbi.displayAllOrgAuthorities(orgAuthorities);
            logger.info(details);

            logger.info("Reading all organizations ...");
            orgAuthIds = vbi.readOrgAuthorityIds(orgAuthorities);
            for (String orgAuthId : orgAuthIds) {
                OrganizationsCommonList items = vbi.readItemsInOrgAuth(orgAuthId);
                details = vbi.displayAllOrganizations(items);
                logger.info(details);
            }

        }

	}

}
