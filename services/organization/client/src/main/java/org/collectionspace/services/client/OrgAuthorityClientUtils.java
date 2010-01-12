package org.collectionspace.services.client;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrgauthoritiesCommon;
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

}
