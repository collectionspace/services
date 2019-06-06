/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 University of California, Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;

import org.collectionspace.services.organization.StructuredDateGroup;
import org.collectionspace.services.organization.ContactNameList;
import org.collectionspace.services.organization.FunctionList;
import org.collectionspace.services.organization.GroupList;
import org.collectionspace.services.organization.HistoryNoteList;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.organization.OrgTermGroup;
import org.collectionspace.services.organization.OrgTermGroupList;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrgAuthorityClientUtils.
 */
public class OrgAuthorityClientUtils {
    
    /** The Constant logger. */
    private static final Logger logger =
        LoggerFactory.getLogger(OrgAuthorityClientUtils.class);
	private static final ServiceRequestType READ_REQ = ServiceRequestType.READ;

    /**
     * @param csid the id of the OrgAuthority
     * @param client if null, creates a new client
     * @return
     * @throws Exception 
     */
    public static String getAuthorityRefName(String csid, OrganizationClient client) throws Exception{
    	if (client==null) {
    		client = new OrganizationClient();
    	}
    	
        Response res = client.read(csid);
        try {
	        int statusCode = res.getStatus();
	        if (!READ_REQ.isValidStatusCode(statusCode) || statusCode != CollectionSpaceClientUtils.STATUS_OK) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            PoxPayloadIn input = new PoxPayloadIn(res.readEntity(String.class));
	            OrgauthoritiesCommon orgAuthority = 
	            	(OrgauthoritiesCommon) CollectionSpaceClientUtils.extractPart(input,
	                    client.getCommonPartName(), OrgauthoritiesCommon.class);
		        if(orgAuthority==null) {
		    		throw new RuntimeException("Null orgAuthority returned from service.");
		        }
	            return orgAuthority.getRefName();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.close();
        }
    }

    /**
     * @param inAuthority the ID of the parent OrgAuthority
     * @param csid the ID of the Organization
     * @param client if null, creates a new client
     * @return
     * @throws Exception 
     */
    public static String getOrgRefName(String inAuthority, String csid, OrganizationClient client) throws Exception{
    	if (client == null) {
    		client = new OrganizationClient();
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
	            OrganizationsCommon org = 
	            	(OrganizationsCommon) CollectionSpaceClientUtils.extractPart(input,
	                    client.getItemCommonPartName(), OrganizationsCommon.class);
		        if(org==null) {
		    		throw new RuntimeException("Null Organization returned from service.");
		        }
	            return org.getRefName();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.close();
        }
    }

    /**
     * Creates the org authority instance.
     *
     * @param displayName the display name
     * @param shortIdentifier the short Id 
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createOrgAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        OrgauthoritiesCommon orgAuthority = new OrgauthoritiesCommon();
        orgAuthority.setDisplayName(displayName);
        orgAuthority.setShortIdentifier(shortIdentifier);
        //String refName = createOrgAuthRefName(shortIdentifier, displayName);
        //orgAuthority.setRefName(refName);
        orgAuthority.setVocabType("OrgAuthority");
        PoxPayloadOut multipart = new PoxPayloadOut(OrganizationClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(orgAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, orgAuthority common ",
                        orgAuthority, OrgauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * Creates the item in authority.
     *
     * @param inAuthority the owning authority
     * @param orgAuthorityRefName the owning Authority ref name
     * @param orgInfo the org info. OrganizationJAXBSchema.SHORT_IDENTIFIER is REQUIRED.
     * @param client the client
     * @return the string
     */
    public static String createItemInAuthority( String inAuthority,
    		String orgAuthorityRefName, Map<String, String> orgInfo, List<OrgTermGroup> terms,
                Map<String, List<String>> orgRepeatablesInfo, OrganizationClient client) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

        String displayName = "";
        if ((terms !=null) && (! terms.isEmpty())) {
            displayName = terms.get(0).getTermDisplayName();
        }

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName +"\"");
    	}
    	PoxPayloadOut multipart =
    		createOrganizationInstance(orgAuthorityRefName, 
    				orgInfo, terms, orgRepeatablesInfo, client.getItemCommonPartName());

    	Response res = client.createItem(inAuthority, multipart);
    	String result;
    	try {	
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
   	    		throw new RuntimeException("Could not create Item: \""+orgInfo.get(OrganizationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in orgAuthority: \"" + orgAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+ orgInfo.get(OrganizationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in orgAuthority: \"" + orgAuthorityRefName +"\", Status:"+ statusCode);
	    	}
	
	    	result = extractId(res);
    	} finally {
    		res.close();
    	}
    	
    	return result;
    }
    
    public static List<OrgTermGroup> getTermGroupInstance(String shortIdentifier, String displayName) {
        if (Tools.isBlank(shortIdentifier)) {
            shortIdentifier = getGeneratedIdentifier();
        }
        if (Tools.isBlank(shortIdentifier)) {
            displayName = shortIdentifier;
        }
        
        List<OrgTermGroup> terms = new ArrayList<OrgTermGroup>();
        OrgTermGroup term = new OrgTermGroup();
        term.setTermDisplayName(displayName);
        term.setTermName(shortIdentifier);
        terms.add(term);
        return terms;
    }
    
    /*
     * Create a very simple Organization term -just a short ID and display name.
     */
    public static PoxPayloadOut createOrganizationInstance(String shortIdentifier, String displayName,
            String headerLabel) {
        List<OrgTermGroup> terms = getTermGroupInstance(shortIdentifier, displayName);
        
        Map<String, String> orgInfo = new HashMap<String, String>();
        orgInfo.put(OrganizationJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);
        
        final Map<String, List<String>> EMPTY_ORG_REPEATABLES_INFO = new HashMap<String, List<String>>();

        return createOrganizationInstance(null, orgInfo, terms, EMPTY_ORG_REPEATABLES_INFO, headerLabel);
    }      

    /**
     * Creates the organization instance.
     *
     * @param orgAuthRefName the owning Authority ref name
     * @param orgInfo the org info
     * @param headerLabel the header label
     * @return the multipart output
     */
	public static PoxPayloadOut createOrganizationInstance(
			String orgAuthRefName,
			Map<String,	String> orgInfo,
			List<OrgTermGroup> terms,
			String headerLabel) {
		final Map<String, List<String>> EMPTY_ORG_REPEATABLES_INFO = new HashMap<String, List<String>>();
		return createOrganizationInstance(orgAuthRefName, orgInfo, terms,
				EMPTY_ORG_REPEATABLES_INFO, headerLabel);
	}

    /**
     * Creates the organization instance.
     *
     * @param orgAuthRefName the owning Authority ref name
     * @param orgInfo the org info
     * @param orgRepeatablesInfo names and values of repeatable scalar
     *        fields in the Organization record
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createOrganizationInstance( 
        		String orgAuthRefName, 
        		Map<String, String> orgInfo, 
        		List<OrgTermGroup> terms,
        		Map<String, List<String>> orgRepeatablesInfo,
        		String headerLabel) {
        
        OrganizationsCommon organization = new OrganizationsCommon();
        	String shortId = orgInfo.get(OrganizationJAXBSchema.SHORT_IDENTIFIER);
        	if (shortId == null || shortId.isEmpty()) {
        		throw new IllegalArgumentException("shortIdentifier cannot be null or empty");
        	}      	
        	organization.setShortIdentifier(shortId);
       	String value = null;
        List<String> values = null;
        
        // Set values in the Term Information Group
        OrgTermGroupList termList = new OrgTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getOrgTermGroup().addAll(terms); 
        organization.setOrgTermGroupList(termList);
        
        if ((values = (List<String>)orgRepeatablesInfo.get(OrganizationJAXBSchema.CONTACT_NAMES))!=null) {
                ContactNameList contactsList = new ContactNameList();
                List<String> contactNames = contactsList.getContactName();
        	contactNames.addAll(values);
                organization.setContactNames(contactsList);
        }
        if ((value = (String)orgInfo.get(OrganizationJAXBSchema.FOUNDING_DATE))!=null) {
            StructuredDateGroup foundingDate = new StructuredDateGroup();
            foundingDate.setDateDisplayDate(value);
            organization.setFoundingDateGroup(foundingDate);
        }
        if ((value = (String)orgInfo.get(OrganizationJAXBSchema.DISSOLUTION_DATE))!=null) {
            StructuredDateGroup dissolutionDate = new StructuredDateGroup();
            dissolutionDate.setDateDisplayDate(value);
            organization.setDissolutionDateGroup(dissolutionDate);
        }
        if ((value = (String)orgInfo.get(OrganizationJAXBSchema.FOUNDING_PLACE))!=null)
        	organization.setFoundingPlace(value);
        if((values = (List<String>)orgRepeatablesInfo.get(OrganizationJAXBSchema.GROUPS))!=null) {
                GroupList groupsList = new GroupList();
                List<String> groups = groupsList.getGroup();
        	groups.addAll(values);
                organization.setGroups(groupsList);
        }
        if ((values = (List<String>)orgRepeatablesInfo.get(OrganizationJAXBSchema.FUNCTIONS))!=null) {
                FunctionList functionsList = new FunctionList();
                List<String> functions = functionsList.getFunction();
        	functions.addAll(values);
                organization.setFunctions(functionsList);
        }
        if ((values = (List<String>)orgRepeatablesInfo.get(OrganizationJAXBSchema.HISTORY_NOTES))!=null) {
                HistoryNoteList historyNotesList = new HistoryNoteList();
                List<String> historyNotes = historyNotesList.getHistoryNote();
        	historyNotes.addAll(values);
                organization.setHistoryNotes(historyNotesList);
        }

        PoxPayloadOut multipart = new PoxPayloadOut(OrganizationClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(organization,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

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
    public static String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode + "' in response is NOT within the expected set: " +
                requestType.validStatusCodesAsString();
    }

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    public static String extractId(Response res) {
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
    
    /**
     * Creates the org auth ref name.
     *
     * @param shortId the orgAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    /*
    public static String createOrgAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:orgauthority:name("
			+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }
    */

    /**
     * Creates the organization ref name.
     *
     * @param orgAuthRefName the org auth ref name
     * @param shortId the person shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    /*
    public static String createOrganizationRefName(
			String orgAuthRefName, String shortId, String displaySuffix) {
    	String refName = orgAuthRefName+":organization:name("+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }
    */

    /**
     * Produces a default displayName from the basic name and foundingPlace fields.
     * @see OrgAuthorityDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param shortName
     * @param foundingPlace
     * @return
     * @throws Exception
     */
    public static String prepareDefaultDisplayName(
    		String shortName, String foundingPlace ) {
    	StringBuilder newStr = new StringBuilder();
		final String sep = " ";
		boolean firstAdded = false;
		if(null != shortName ) {
			newStr.append(shortName);
			firstAdded = true;
		}
    	// Now we add the place
		if(null != foundingPlace ) {
			if(firstAdded) {
				newStr.append(sep);
			}
			newStr.append(foundingPlace);
		}
		return newStr.toString();
    }

    public static List<OrgTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }
        List<OrgTermGroup> terms = new ArrayList<OrgTermGroup>();
        OrgTermGroup term = new OrgTermGroup();
        term.setTermDisplayName(identifier);
        term.setTermName(identifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime(); 
   }
    
}
