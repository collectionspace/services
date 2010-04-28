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
import java.util.Map;

import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
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
        extends RemoteDocumentModelHandlerImpl<OrganizationsCommon, OrganizationsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(OrganizationDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "organizations_common";    
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
     * inAuthority is the parent OrgAuthority for this context
     */
    private String inAuthority;

    /**
     * Gets the in authority.
     *
     * @return the in authority
     */
    public String getInAuthority() {
		return inAuthority;
	}

	/**
	 * Sets the in authority.
	 *
	 * @param inAuthority the new in authority
	 */
	public void setInAuthority(String inAuthority) {
		this.inAuthority = inAuthority;
	}

	
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// first fill all the parts of the document
    	super.handleCreate(wrapDoc);    	
    	handleDisplayName(wrapDoc.getWrappedObject());
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	super.handleUpdate(wrapDoc);
    	handleDisplayName(wrapDoc.getWrappedObject());
    }

    /**
     * Check the logic around the computed displayName
     * 
     * @param docModel
     * 
     * @throws Exception the exception
     */
    private void handleDisplayName(DocumentModel docModel) throws Exception {
    	String commonPartLabel = getServiceContext().getCommonPartLabel("organizations");
    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			OrganizationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	if (displayNameComputed) {
    		String displayName = prepareDefaultDisplayName(
        			(String) docModel.getProperty(commonPartLabel,OrganizationJAXBSchema.SHORT_NAME),
        			(String) docModel.getProperty(commonPartLabel,OrganizationJAXBSchema.FOUNDING_PLACE));
			docModel.setProperty(commonPartLabel, OrganizationJAXBSchema.DISPLAY_NAME,
						displayName);
    	}
    }

    /**
     * Produces a default displayName from the basic name and foundingPlace fields.
     * @see OrgAuthorityClientUtils.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param shortName
     * @param foundingPlace
     * @return
     * @throws Exception
     */
    private static String prepareDefaultDisplayName(
    		String shortName, String foundingPlace ) throws Exception {
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

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(OrganizationsCommonList organizationList) {
        this.organizationList = organizationList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);
    	
    	// Add the CSID to the common part
    	if (partMeta.getLabel().equalsIgnoreCase(COMMON_PART_LABEL)) {
	    	String csid = NuxeoUtils.extractId(docModel.getPathAsString());
	    	unQObjectProperties.put("csid", csid);
    	}
    	
    	return unQObjectProperties;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public OrganizationsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(OrganizationsCommon organizationObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public OrganizationsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) 
    		throws Exception {
        OrganizationsCommonList coList = this.extractPagingInfo(new OrganizationsCommonList(), wrapDoc);
        List<OrganizationsCommonList.OrganizationListItem> list = coList.getOrganizationListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        String commonPartLabel = getServiceContext().getCommonPartLabel("organizations");
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            OrganizationListItem ilistItem = new OrganizationListItem();
            ilistItem.setDisplayName((String)
            		docModel.getProperty(commonPartLabel,OrganizationJAXBSchema.DISPLAY_NAME ));
			ilistItem.setRefName((String) 
					docModel.getProperty(commonPartLabel, OrganizationJAXBSchema.REF_NAME));
			String id = NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri("/orgauthorities/" + this.inAuthority + "/items/" + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
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

