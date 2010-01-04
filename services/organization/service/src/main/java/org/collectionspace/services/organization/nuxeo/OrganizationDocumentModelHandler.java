/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.organization.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandler;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.collectionspace.services.organization.OrganizationsCommonList;
import org.collectionspace.services.organization.OrganizationsCommonList.OrganizationListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrganizationDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class OrganizationDocumentModelHandler
        extends RemoteDocumentModelHandler<OrganizationsCommon, OrganizationsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(OrganizationDocumentModelHandler.class);
    /**
     * organization is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private OrganizationsCommon organization;
    /**
     * organizationList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private OrganizationsCommonList organizationList;
    
    /**
     * inOrgAuthority is the parent OrgAuthority for this context
     */
    private String inOrgAuthority;

    public String getInOrgAuthority() {
		return inOrgAuthority;
	}

	public void setInOrgAuthority(String inOrgAuthority) {
		this.inOrgAuthority = inOrgAuthority;
	}

	@Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

    /**
     * getCommonPart get associated organization
     * @return
     */
    @Override
    public OrganizationsCommon getCommonPart() {
        return organization;
    }

    /**
     * setCommonPart set associated organization
     * @param organization
     */
    @Override
    public void setCommonPart(OrganizationsCommon organization) {
        this.organization = organization;
    }

    /**
     * getCommonPartList get associated organization (for index/GET_ALL)
     * @return
     */
    @Override
    public OrganizationsCommonList getCommonPartList() {
        return organizationList;
    }

    @Override
    public void setCommonPartList(OrganizationsCommonList organizationList) {
        this.organizationList = organizationList;
    }

    @Override
    public OrganizationsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(OrganizationsCommon organizationObject, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public OrganizationsCommonList extractCommonPartList(DocumentWrapper wrapDoc) 
    	throws Exception {
        OrganizationsCommonList coList = new OrganizationsCommonList();
        try{
	        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();
	
	        List<OrganizationsCommonList.OrganizationListItem> list = 
	        	coList.getOrganizationListItem();
	
	        //FIXME: iterating over a long list of documents is not a long term
	        //strategy...need to change to more efficient iterating in future
	        Iterator<DocumentModel> iter = docList.iterator();
	        while(iter.hasNext()){
	            DocumentModel docModel = iter.next();
	            OrganizationListItem ilistItem = new OrganizationListItem();
	            ilistItem.setDisplayName(
									(String) docModel.getProperty(getServiceContext().getCommonPartLabel("organizations"),
									OrganizationJAXBSchema.DISPLAY_NAME));
	            ilistItem.setRefName(
									(String) docModel.getProperty(getServiceContext().getCommonPartLabel("organizations"),
									OrganizationJAXBSchema.REF_NAME));
							/*
							 * These are not currently included in the listing - only in the details
	            ilistItem.setLongName(
									(String) docModel.getProperty(getServiceContext().getCommonPartLabel("organizations"),
									OrganizationJAXBSchema.LONG_NAME));
	            ilistItem.setDescription(
									(String) docModel.getProperty(getServiceContext().getCommonPartLabel("organizations"),
									OrganizationJAXBSchema.DESCRIPTION));
							 */
							String id = NuxeoUtils.extractId(docModel.getPathAsString());
	            ilistItem.setUri("/organizations/"+inOrgAuthority+"/items/" + id);
	            ilistItem.setCsid(id);
	            list.add(ilistItem);
	        }
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception in extractCommonPartList", e);
            }
            throw e;
        }
        return coList;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return OrganizationConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

