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
import org.jboss.resteasy.client.ClientResponse;
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

    /**
     * Creates the org authority instance.
     *
     * @param displayName the display name
     * @param refName the ref name
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static MultipartOutput createOrgAuthorityInstance(
    		String displayName, String refName, String headerLabel ) {
        OrgauthoritiesCommon orgAuthority = new OrgauthoritiesCommon();
        orgAuthority.setDisplayName(displayName);
        orgAuthority.setRefName(refName);
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
     * @param vcsid the vcsid
     * @param orgAuthorityRefName the org authority ref name
     * @param orgInfo the org info
     * @param client the client
     * @return the string
     */
    public static String createItemInAuthority(String vcsid, 
    		String orgAuthorityRefName, Map<String, String> orgInfo,
    		OrgAuthorityClient client) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
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
    	String refName = createOrganizationRefName(orgAuthorityRefName, displayName, true);

    	if(logger.isDebugEnabled()){
    		logger.debug("Import: Create Item: \""+displayName
    				+"\" in orgAuthority: \"" + orgAuthorityRefName +"\"");
    	}
    	MultipartOutput multipart =
    		createOrganizationInstance(vcsid, refName, orgInfo, client.getItemCommonPartName());
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
    	String result;
    	try {	
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+displayName
	    				+"\" in orgAuthority: \"" + orgAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+ displayName
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
     * @param orgRefName the org ref name
     * @param orgInfo the org info
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static MultipartOutput createOrganizationInstance(String inAuthority, 
    		String orgRefName, Map<String, String> orgInfo, String headerLabel){
        OrganizationsCommon organization = new OrganizationsCommon();
        organization.setInAuthority(inAuthority);
       	organization.setRefName(orgRefName);
       	String value = null;
    	value = orgInfo.get(OrganizationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	boolean displayNameComputed = (value==null) || value.equalsIgnoreCase("true"); 
   		organization.setDisplayNameComputed(displayNameComputed);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.DISPLAY_NAME))!=null)
        	organization.setDisplayName(value);
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
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.STATUS))!=null)
        	organization.setStatus(value);
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
     * @param orgAuthorityName the org authority name
     * @param withDisplaySuffix the with display suffix
     * @return the string
     */
    public static String createOrgAuthRefName(String orgAuthorityName, boolean withDisplaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:orgauthority:name("
    			+orgAuthorityName+")";
    	if(withDisplaySuffix)
    		refName += "'"+orgAuthorityName+"'";
    	return refName;
    }

    /**
     * Creates the organization ref name.
     *
     * @param orgAuthRefName the org auth ref name
     * @param orgName the org name
     * @param withDisplaySuffix the with display suffix
     * @return the string
     */
    public static String createOrganizationRefName(
    						String orgAuthRefName, String orgName, boolean withDisplaySuffix) {
    	String refName = orgAuthRefName+":organization:name("+orgName+")";
    	if(withDisplaySuffix)
    		refName += "'"+orgName+"'";
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
    
}
