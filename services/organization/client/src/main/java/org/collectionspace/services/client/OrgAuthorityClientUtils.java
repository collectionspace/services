/**	
 * OrgAuthorityClientUtils.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonsCommon;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class OrgAuthorityClientUtils.
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
     */
    public static String getAuthorityRefName(String csid, OrgAuthorityClient client){
    	if(client==null)
    		client = new OrgAuthorityClient();
        ClientResponse<MultipartInput> res = client.read(csid);
        try {
	        int statusCode = res.getStatus();
	        if(!READ_REQ.isValidStatusCode(statusCode)
	        	||(statusCode != CollectionSpaceClientUtils.STATUS_OK)) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            MultipartInput input = (MultipartInput) res.getEntity();
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
        	res.releaseConnection();
        }
    }

    /**
     * @param csid the id of the PersonAuthority
     * @param client if null, creates a new client
     * @return
     */
    public static String getOrgRefName(String inAuthority, String csid, OrgAuthorityClient client){
    	if(client==null)
    		client = new OrgAuthorityClient();
        ClientResponse<MultipartInput> res = client.readItem(inAuthority, csid);
        try {
	        int statusCode = res.getStatus();
	        if(!READ_REQ.isValidStatusCode(statusCode)
		        	||(statusCode != CollectionSpaceClientUtils.STATUS_OK)) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            MultipartInput input = (MultipartInput) res.getEntity();
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
        	res.releaseConnection();
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
    public static MultipartOutput createOrgAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        OrgauthoritiesCommon orgAuthority = new OrgauthoritiesCommon();
        orgAuthority.setDisplayName(displayName);
        orgAuthority.setShortIdentifier(shortIdentifier);
        String refName = createOrgAuthRefName(shortIdentifier, displayName);
        orgAuthority.setVocabType("OrgAuthority");
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(orgAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

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
    public static String createItemInAuthority(String inAuthority, 
    		String orgAuthorityRefName, Map<String, String> orgInfo,
    		OrgAuthorityClient client) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
        String displayName = createDisplayName(orgInfo);

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName +"\"");
    	}
    	MultipartOutput multipart =
    		createOrganizationInstance(inAuthority, orgAuthorityRefName, 
    				orgInfo, client.getItemCommonPartName());
    	ClientResponse<Response> res = client.createItem(inAuthority, multipart);
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
    		res.releaseConnection();
    	}
    	
    	return result;
    }

    /**
     * Creates the organization instance.
     *
     * @param inAuthority the in authority
     * @param orgAuthRefName the owning Authority ref name
     * @param orgInfo the org info
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static MultipartOutput createOrganizationInstance(String inAuthority, 
    		String orgAuthRefName, Map<String, String> orgInfo, String headerLabel){
        OrganizationsCommon organization = new OrganizationsCommon();
        organization.setInAuthority(inAuthority);
    	String shortId = orgInfo.get(OrganizationJAXBSchema.SHORT_IDENTIFIER);
    	if (shortId == null || shortId.isEmpty()) {
    		throw new IllegalArgumentException("shortIdentifier cannot be null or empty");
    	}      	
    	organization.setShortIdentifier(shortId);
       	String value = null;
    	value = orgInfo.get(OrganizationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
   		organization.setDisplayNameComputed(displayNameComputed);
       	if((value = (String)orgInfo.get(OrganizationJAXBSchema.DISPLAY_NAME))!=null)
        	organization.setDisplayName(value);
   		
    	String refName = createOrganizationRefName(orgAuthRefName, shortId, value);
    	organization.setRefName(refName);

        if((value = (String)orgInfo.get(OrganizationJAXBSchema.SHORT_NAME))!=null)
        	organization.setShortName(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.LONG_NAME))!=null)
        	organization.setLongName(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.NAME_ADDITIONS))!=null)
        	organization.setNameAdditions(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.CONTACT_NAME))!=null)
        	organization.setContactName(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.FOUNDING_DATE))!=null)
        	organization.setFoundingDate(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.DISSOLUTION_DATE))!=null)
        	organization.setDissolutionDate(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.FOUNDING_PLACE))!=null)
        	organization.setFoundingPlace(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.GROUP))!=null)
        	organization.setGroup(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.FUNCTION))!=null)
        	organization.setFunction(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.SUB_BODY))!=null)
        	organization.setSubBody(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.HISTORY))!=null)
        	organization.setHistory(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.TERM_STATUS))!=null)
        	organization.setTermStatus(value);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart = multipart.addPart(organization,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", headerLabel);

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
    public static String extractId(ClientResponse<Response> res) {
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
    public static String createOrgAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:orgauthority:name("
			+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }

    /**
     * Creates the organization ref name.
     *
     * @param orgAuthRefName the org auth ref name
     * @param shortId the person shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createOrganizationRefName(
			String orgAuthRefName, String shortId, String displaySuffix) {
    	String refName = orgAuthRefName+":organization:name("+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }

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

    public static String createDisplayName(Map<String, String> orgInfo) {
        String displayName = orgInfo.get(OrganizationJAXBSchema.DISPLAY_NAME);
    	String displayNameComputedStr = orgInfo.get(OrganizationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (displayNameComputedStr==null) || displayNameComputedStr.equalsIgnoreCase("true");
    	if( displayName == null ) {
            if(!displayNameComputed) {
                throw new RuntimeException(
                "CreateItem: Must supply a displayName if displayNameComputed is set to false.");
            }
            displayName = prepareDefaultDisplayName(
                orgInfo.get(OrganizationJAXBSchema.SHORT_NAME ),
                orgInfo.get(OrganizationJAXBSchema.FOUNDING_PLACE ));
    	}
        return displayName;
    }
    
}
