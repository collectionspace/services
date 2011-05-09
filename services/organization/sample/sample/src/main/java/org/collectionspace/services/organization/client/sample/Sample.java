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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClientUtils;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrganizationsCommonList;
import org.jboss.resteasy.client.ClientResponse;
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
    protected String createOrgAuthRefName(String orgAuthorityName) {
    	return "urn:cspace:org.collectionspace.demo:orgauthority:name("
    			+orgAuthorityName+")";
    }

    protected String createOrganizationRefName(
    						String orgAuthRefName, String orgName) {
    	return orgAuthRefName+":organization:name("+orgName+")";
    }

    

    public void createOrgAuthority(String orgAuthName, List<Map<String,String>> orgInfos ) {

    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

    	logger.info("Import: Create orgAuthority: \"" + orgAuthName +"\"");
    	String baseOrgAuthRefName = createOrgAuthRefName(orgAuthName);
    	String fullOrgAuthRefName = baseOrgAuthRefName+"'"+orgAuthName+"'";
    	PoxPayloadOut multipart = 
    		OrgAuthorityClientUtils.createOrgAuthorityInstance(
				orgAuthName, fullOrgAuthRefName, 
				client.getCommonPartName());
    	ClientResponse<Response> res = client.create(multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create enumeration: \""+orgAuthName
    				+"\" "+ OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating enumeration: \""
    				+orgAuthName +"\", Status:"+ statusCode);
    	}

    	// Store the ID returned from this create operation
    	// for additional tests below.
    	String newOrgAuthId = OrgAuthorityClientUtils.extractId(res);
        logger.info("Import: Created orgAuthority: \"" + orgAuthName +"\" ID:"
    				+newOrgAuthId );
        
        // Add items to the orgAuthority
       	for(Map<String,String> orgInfo : orgInfos){
    		createItemInOrgAuth(newOrgAuthId, baseOrgAuthRefName, orgInfo);
    	}
        
    }
    
    private String createItemInOrgAuth(String vcsid, 
    		String orgAuthorityRefName, Map<String,String> orgInfo) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
    	String shortName = orgInfo.get(OrganizationJAXBSchema.SHORT_NAME);
    	String refName = createOrganizationRefName(
    						orgAuthorityRefName, shortName)+"'"+shortName+"'";


    	logger.info("Import: Create Item: \""+shortName+
    			"\" in orgAuthority: \"" + orgAuthorityRefName +"\"");
        PoxPayloadOut multipart = 
        	OrgAuthorityClientUtils.createOrganizationInstance(refName, orgInfo, client.getItemCommonPartName() );

    	ClientResponse<Response> res = client.createItem(vcsid, multipart);

    	int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not create Item: \""+shortName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName
    				+"\" "+ OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
    	}
    	if(statusCode != EXPECTED_STATUS_CODE) {
    		throw new RuntimeException("Unexpected Status when creating Item: \""+shortName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName +
    				"\", Status:"+ statusCode);
    	}

    	return OrgAuthorityClientUtils.extractId(res);
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
                + OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
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
            ClientResponse<String> res = client.read(orgAuthId);
            int statusCode = res.getStatus();
            if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not read orgAuthority"
                    + OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if(statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when reading " +
                    "orgAuthority, Status:"+ statusCode);
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            PayloadInputPart orgAuthorityPart = input.getPart(client.getCommonPartName());
            orgAuthority = (OrgauthoritiesCommon) orgAuthorityPart.getBody();
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

        //was: ClientResponse<OrganizationsCommonList> res = client.readItemList(orgAuthId);
        //new API: readItemList(String inAuthority, String partialTerm, String keywords)
        ClientResponse<OrganizationsCommonList> res = client.readItemList(orgAuthId, "", "");//TODO:   .New call, most certainly wrong.  Just trying to get this to compile. Laramie20100728

        OrganizationsCommonList list = res.getEntity();

        int statusCode = res.getStatus();

    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
    		throw new RuntimeException("Could not read items in orgAuthority: "
                + OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
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
                + OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
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
                + OrgAuthorityClientUtils.invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
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
            sb.append("displayName=" + item.getDisplayName() + "\n");
            // sb.append("URI=" + item.getUri() + "\n");
        return sb.toString();
    }

//    private Object extractPart(PoxPayloadIn input, String label,
//        Class clazz) throws Exception {
//        Object obj = null;
//        for(PayloadInputPart part : input.getParts()){
//            String partLabel = part.getHeaders().getFirst("label");
//            if(label.equalsIgnoreCase(partLabel)){
//                String partStr = part.getBodyAsString();
//                if(logger.isDebugEnabled()){
//                    logger.debug("extracted part str=\n" + partStr);
//                }
//                obj = part.getBody(clazz, null);
//                if(logger.isDebugEnabled()){
//                    logger.debug("extracted part obj=\n", obj, clazz);
//                }
//                break;
//            }
//        }
//        return obj;
//    }

	public static void main(String[] args) {

        // Configure logging.
		BasicConfigurator.configure();

        logger.info("OrgAuthority Sample starting...");

		Sample sample = new Sample();
        OrgauthoritiesCommonList orgAuthorities;
        List<String> orgAuthIds;
        String details = "";

        // Optionally delete all orgAuthorities and organizations.

        boolean ENABLE_DELETE_ALL = false;
        if (ENABLE_DELETE_ALL) {

            logger.info("Deleting all organizations and orgAuthorities ...");

            // For each orgAuthority ...
            orgAuthorities = sample.readOrgAuthorities();
            orgAuthIds = sample.readOrgAuthorityIds(orgAuthorities);
            for (String orgAuthId : orgAuthIds) {
                logger.info("Deleting all organizations for orgAuthority ...");
                sample.deleteAllItemsForOrgAuth(orgAuthId);
                logger.info("Deleting orgAuthority ...");
                sample.deleteOrgAuthority(orgAuthId);
            }

            logger.info("Reading orgAuthorities after deletion ...");
            orgAuthorities = sample.readOrgAuthorities();
            details = sample.displayAllOrgAuthorities(orgAuthorities);
            logger.info(details);

            logger.info("Reading items in each orgAuthority after deletion ...");
            orgAuthIds = sample.readOrgAuthorityIds(orgAuthorities);
            for (String orgAuthId : orgAuthIds) {
                OrganizationsCommonList items = sample.readItemsInOrgAuth(orgAuthId);
                details = sample.displayAllOrganizations(items);
                logger.info(details);
            }

        }

        // Create new authorities, each populated with organizations.
        Map<String, String> mmiOrgMap = new HashMap<String,String>();
        mmiOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "MMI");
        mmiOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "Museum of the Moving Image");
        //mmiOrgMap.put(OrganizationJAXBSchema.CONTACT_NAME, "Megan Forbes");
        mmiOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "1984");
        mmiOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "Astoria, NY");
        Map<String, String> pahmaOrgMap = new HashMap<String,String>();
        pahmaOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "PAHMA");
        pahmaOrgMap.put(OrganizationJAXBSchema.LONG_NAME, "Phoebe A. Hearst Museum of Anthropology");
        pahmaOrgMap.put(OrganizationJAXBSchema.NAME_ADDITIONS, "University of California, Berkeley");
        //pahmaOrgMap.put(OrganizationJAXBSchema.CONTACT_NAME, "Michael Black");
        pahmaOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "1901");
        pahmaOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "Berkeley, CA");
        Map<String, String> savoyOrgMap = new HashMap<String,String>();
        savoyOrgMap.put(OrganizationJAXBSchema.SHORT_NAME, "Savoy Theatre");
        savoyOrgMap.put(OrganizationJAXBSchema.FOUNDING_DATE, "1900");
        savoyOrgMap.put(OrganizationJAXBSchema.DISSOLUTION_DATE, "1952");
        savoyOrgMap.put(OrganizationJAXBSchema.FOUNDING_PLACE, "New York, NY");
        List<Map<String, String>> orgMaps = 
        	Arrays.asList(mmiOrgMap, pahmaOrgMap, savoyOrgMap );
        
		sample.createOrgAuthority("Sample Org Authority", orgMaps);

		logger.info("OrgAuthority Sample complete.");

        logger.info("Reading orgAuthorities and items ...");
        // Get a list of orgAuthorities.
        orgAuthorities = sample.readOrgAuthorities();
        // For each orgAuthority ...
        for (OrgauthoritiesCommonList.OrgauthorityListItem
            orgAuthority : orgAuthorities.getOrgauthorityListItem()) {
            // Get its display name.
            logger.info(orgAuthority.getDisplayName());
            // Get a list of the organizations in this orgAuthority.
            OrganizationsCommonList items =
                sample.readItemsInOrgAuth(orgAuthority.getCsid());
            // For each organization ...
            for (OrganizationsCommonList.OrganizationListItem
                item : items.getOrganizationListItem()) {
                // Get its display name.
                logger.info(" " + item.getDisplayName());
            }
        }

        // Sample alternate methods of reading all orgAuthorities and
        // organizations separately.
        boolean RUN_ADDITIONAL_SAMPLES = false;
        if (RUN_ADDITIONAL_SAMPLES) {

            logger.info("Reading all orgAuthorities ...");
            details = sample.displayAllOrgAuthorities(orgAuthorities);
            logger.info(details);

            logger.info("Reading all organizations ...");
            orgAuthIds = sample.readOrgAuthorityIds(orgAuthorities);
            for (String orgAuthId : orgAuthIds) {
                OrganizationsCommonList items = sample.readItemsInOrgAuth(orgAuthId);
                details = sample.displayAllOrganizations(items);
                logger.info(details);
            }

        }

	}

}
