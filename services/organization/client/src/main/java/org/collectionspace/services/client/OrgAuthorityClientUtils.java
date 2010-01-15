package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.List;
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

public class OrgAuthorityClientUtils {
    private static final Logger logger =
        LoggerFactory.getLogger(OrgAuthorityClientUtils.class);

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

    public static MultipartOutput createOrganizationInstance(String inAuthority, 
    		String orgRefName, Map<String, String> orgInfo, String headerLabel){
        OrganizationsCommon organization = new OrganizationsCommon();
        organization.setInAuthority(inAuthority);
       	organization.setRefName(orgRefName);
       	String value = null;
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
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.FUNCTION))!=null)
        	organization.setFunction(value);
        if((value = (String)orgInfo.get(OrganizationJAXBSchema.DESCRIPTION))!=null)
        	organization.setDescription(value);
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
    
    public static String createOrgAuthRefName(String orgAuthorityName, boolean withDisplaySuffix) {
    	String refName = "urn:cspace:org.collectionspace.demo:orgauthority:name("
    			+orgAuthorityName+")";
    	if(withDisplaySuffix)
    		refName += "'"+orgAuthorityName+"'";
    	return refName;
    }

    public static String createOrganizationRefName(
    						String orgAuthRefName, String orgName, boolean withDisplaySuffix) {
    	String refName = orgAuthRefName+":organization:name("+orgName+")";
    	if(withDisplaySuffix)
    		refName += "'"+orgName+"'";
    	return refName;
    }

}
