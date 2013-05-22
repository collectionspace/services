/**	
 * CitationAuthorityClientUtils.java
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

import java.util.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.CitationJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.citation.CitationTermGroup;
import org.collectionspace.services.citation.CitationTermGroupList;
import org.collectionspace.services.citation.CitationsCommon;
import org.collectionspace.services.citation.CitationauthoritiesCommon;
import org.jboss.resteasy.client.ClientResponse;
//import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.collectionspace.services.citation.StructuredDateGroup;

/**
 * The Class CitationAuthorityClientUtils.
 */
public class CitationAuthorityClientUtils {
    
    /** The Constant logger. */
    private static final Logger logger =
        LoggerFactory.getLogger(CitationAuthorityClientUtils.class);
	private static final ServiceRequestType READ_REQ = ServiceRequestType.READ;

    /**
     * @param csid the id of the CitationAuthority
     * @param client if null, creates a new client
     * @return
     */
    public static String getAuthorityRefName(String csid, CitationAuthorityClient client){
    	if (client == null) {
    		client = new CitationAuthorityClient();
    	}
        ClientResponse<String> res = client.read(csid);
        try {
	        int statusCode = res.getStatus();
	        if(!READ_REQ.isValidStatusCode(statusCode)
	        	||(statusCode != CollectionSpaceClientUtils.STATUS_OK)) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	            CitationauthoritiesCommon citationAuthority = 
	            	(CitationauthoritiesCommon) CollectionSpaceClientUtils.extractPart(input,
	                    client.getCommonPartName(), CitationauthoritiesCommon.class);
		        if(citationAuthority == null) {
		    		throw new RuntimeException("Null citationAuthority returned from service.");
		        }
	            return citationAuthority.getRefName();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.releaseConnection();
        }
    }

    /**
     * @param csid the id of the CitationAuthority
     * @param client if null, creates a new client
     * @return
     */
    public static String getCitationRefName(String inAuthority, String csid, CitationAuthorityClient client){
    	if ( client == null) {
    		client = new CitationAuthorityClient();
    	}
        ClientResponse<String> res = client.readItem(inAuthority, csid);
        try {
	        int statusCode = res.getStatus();
	        if(!READ_REQ.isValidStatusCode(statusCode)
		        	||(statusCode != CollectionSpaceClientUtils.STATUS_OK)) {
	    		throw new RuntimeException("Invalid status code returned: "+statusCode);
	        }
	        //FIXME: remove the following try catch once Aron fixes signatures
	        try {
	            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
	            CitationsCommon citation = 
	            	(CitationsCommon) CollectionSpaceClientUtils.extractPart(input,
	                    client.getItemCommonPartName(), CitationsCommon.class);
		        if (citation == null) {
		    		throw new RuntimeException("Null citation returned from service.");
		        }
	            return citation.getRefName();
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
        } finally {
        	res.releaseConnection();
        }
    }

    /**
     * Creates the citation authority instance.
     *
     * @param displayName the display name
     * @param shortIdentifier the short Id 
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createCitationAuthorityInstance(
    		String displayName, String shortIdentifier, String headerLabel ) {
        CitationauthoritiesCommon citationAuthority = new CitationauthoritiesCommon();
        citationAuthority.setDisplayName(displayName);
        citationAuthority.setShortIdentifier(shortIdentifier);
        //String refName = createCitationAuthRefName(shortIdentifier, displayName);
        //citationAuthority.setRefName(refName);
        citationAuthority.setVocabType("CitationAuthority");
        PoxPayloadOut multipart = new PoxPayloadOut(CitationAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(citationAuthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, citationAuthority common ", 
        				citationAuthority, CitationauthoritiesCommon.class);
        }

        return multipart;
    }

    /**
     * Creates a citation instance.
     *
     * @param inAuthority the owning authority
     * @param citationAuthRefName the owning Authority ref name
     * @param citationInfo the citation info
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createCitationInstance(String inAuthority,
    		String citationAuthRefName,
    		Map<String, String> citationInfo,
                List<CitationTermGroup> terms,
    		String headerLabel){
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        final Map<String, List<String>> EMPTY_CITATION_REPEATABLES_INFO =
                new HashMap<String, List<String>>();
        return createCitationInstance(inAuthority, null /*citationAuthRefName*/,
                citationInfo, terms, EMPTY_CITATION_REPEATABLES_INFO, headerLabel);
    }

    /**
     * Creates a citation instance.
     *
     * @param inAuthority the owning authority
     * @param citationAuthRefName the owning Authority ref name
     * @param citationInfo the citation info
     * @param terms a list of Citation terms
     * @param citationRepeatablesInfo names and values of repeatable scalar fields in the Citation record
     * @param headerLabel the header label
     * @return the multipart output
     */
    public static PoxPayloadOut createCitationInstance(String inAuthority, 
    		String citationAuthRefName, Map<String, String> citationInfo,
                List<CitationTermGroup> terms,
                Map<String, List<String>> citationRepeatablesInfo, String headerLabel){
        CitationsCommon citation = new CitationsCommon();
        citation.setInAuthority(inAuthority);
    	String shortId = citationInfo.get(CitationJAXBSchema.SHORT_IDENTIFIER);
    	if (shortId == null || shortId.isEmpty()) {
    		throw new IllegalArgumentException("shortIdentifier cannot be null or empty");
    	}      	
    	citation.setShortIdentifier(shortId);
    	
    	String value;
        List<String> values = null;
        
        // Set values in the Term Information Group
        CitationTermGroupList termList = new CitationTermGroupList();
        if (terms == null || terms.isEmpty()) {
            terms = getTermGroupInstance(getGeneratedIdentifier());
        }
        termList.getCitationTermGroup().addAll(terms); 
        citation.setCitationTermGroupList(termList);               
        
        PoxPayloadOut multipart = new PoxPayloadOut(CitationAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(citation,
            MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if(logger.isDebugEnabled()){
        	logger.debug("to be created, citation common ", citation, CitationsCommon.class);
        }

        return multipart;
    }
    
    /**
     * Creates the item in authority.
     *
     * @param vcsid the vcsid
     * @param citationAuthorityRefName the citation authority ref name
     * @param citationMap the citation map. CitationJAXBSchema.SHORT_IDENTIFIER is REQUIRED.
     * @param client the client
     * @return the string
     */
    public static String createItemInAuthority(String vcsid, 
    		String citationAuthorityRefName, Map<String,String> citationMap,
                List<CitationTermGroup> terms, Map<String, List<String>> citationRepeatablesMap,
                CitationAuthorityClient client ) {
    	// Expected status code: 201 Created
    	int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
    	// Type of service request being tested
    	ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;
        
        String displayName = "";
        if (terms !=null && terms.size() > 0) {
            displayName = terms.get(0).getTermDisplayName();
        }
    	
    	if(logger.isDebugEnabled()){
    		logger.debug("Creating item with display name: \"" + displayName
    				+"\" in citationAuthority: \"" + vcsid +"\"");
    	}
    	PoxPayloadOut multipart = 
    		createCitationInstance(vcsid, null /*citationAuthorityRefName*/,
    			citationMap, terms, citationRepeatablesMap, client.getItemCommonPartName());
    	
    	String result = null;
    	ClientResponse<Response> res = client.createItem(vcsid, multipart);
    	try {
	    	int statusCode = res.getStatus();
	
	    	if(!REQUEST_TYPE.isValidStatusCode(statusCode)) {
	    		throw new RuntimeException("Could not create Item: \""+citationMap.get(CitationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in citationAuthority: \"" + vcsid //citationAuthorityRefName
	    				+"\" "+ invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
	    	}
	    	if(statusCode != EXPECTED_STATUS_CODE) {
	    		throw new RuntimeException("Unexpected Status when creating Item: \""+citationMap.get(CitationJAXBSchema.SHORT_IDENTIFIER)
	    				+"\" in citationAuthority: \"" + vcsid /*citationAuthorityRefName*/ +"\", Status:"+ statusCode);
	    	}
	
	    	result = extractId(res);
    	} finally {
    		res.releaseConnection();
    	}
    	
    	return result;
    }

    /**
     * Creates the citationAuthority ref name.
     *
     * @param shortId the citationAuthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    /*
    public static String createCitationAuthRefName(String shortId, String displaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:citationauthority:name("
    			+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }
    */

    /**
     * Creates the citation ref name.
     *
     * @param citationAuthRefName the citation auth ref name
     * @param shortId the citation shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    /*
    public static String createCitationRefName(
    						String citationAuthRefName, String shortId, String displaySuffix) {
    	String refName = citationAuthRefName+":citation:name("+shortId+")";
    	if(displaySuffix!=null&&!displaySuffix.isEmpty())
    		refName += "'"+displaySuffix+"'";
    	return refName;
    }
    */

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    public static String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        // FIXME: This may throw an NPE if the Location: header isn't present
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
     * Produces a default displayName from the basic name and dates fields.
     * @see CitationDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param foreName	
     * @param middleName
     * @param surName
     * @param birthDate
     * @param deathDate
     * @return display name
     */
    public static String prepareDefaultDisplayName(
    		String foreName, String middleName, String surName, 
            String birthDate, String deathDate) {
    	StringBuilder newStr = new StringBuilder();
		final String sep = " ";
		final String dateSep = "-";
		List<String> nameStrings = 
			Arrays.asList(foreName, middleName, surName);
		boolean firstAdded = false;
    	for(String partStr : nameStrings ){
			if(null != partStr ) {
				if(firstAdded) {
					newStr.append(sep);
				}
				newStr.append(partStr);
				firstAdded = true;
			}
    	}
    	// Now we add the dates. In theory could have dates with no name, but that is their problem.
    	boolean foundBirth = false;
        if(null != birthDate) {
         if(firstAdded) {
             newStr.append(sep);
         }
         newStr.append(birthDate);
                 newStr.append(dateSep);     // Put this in whether there is a death date or not
         foundBirth = true;
        }
        if(null != deathDate) {
         if(!foundBirth) {
             if(firstAdded) {
                 newStr.append(sep);
             }
             newStr.append(dateSep);
         }
         newStr.append(deathDate);
        }
		return newStr.toString();
    }
    
    public static List<CitationTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }
        List<CitationTermGroup> terms = new ArrayList<CitationTermGroup>();
        CitationTermGroup term = new CitationTermGroup();
        term.setTermDisplayName(identifier);
        term.setTermName(identifier);
        terms.add(term);
        return terms;
    }
    
    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime(); 
   }

}
