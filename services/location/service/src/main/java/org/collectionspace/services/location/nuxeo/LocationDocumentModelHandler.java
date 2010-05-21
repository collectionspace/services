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
package org.collectionspace.services.location.nuxeo;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.location.LocationsCommon;
import org.collectionspace.services.location.LocationsCommonList;
import org.collectionspace.services.location.LocationsCommonList.LocationListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocationDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
/**
 * @author pschmitz
 *
 */
public class LocationDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<LocationsCommon, LocationsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(LocationDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "locations_common";
    
    /**
     * location is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private LocationsCommon location;
    /**
     * locationList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private LocationsCommonList locationList;
    
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
     * Handle display name.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
    private void handleDisplayName(DocumentModel docModel) throws Exception {
    	String commonPartLabel = getServiceContext().getCommonPartLabel("locations");
    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			LocationJAXBSchema.DISPLAY_NAME_COMPUTED);
    	if (displayNameComputed) {
    		String displayName = prepareDefaultDisplayName(
			(String)docModel.getProperty(commonPartLabel, LocationJAXBSchema.NAME ));
			docModel.setProperty(commonPartLabel, LocationJAXBSchema.DISPLAY_NAME,
					displayName);
    	}
    }
	
    /**
     * Produces a default displayName from the basic name and dates fields.
     * @see LocationAuthorityClientUtils.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param foreName	
     * @param middleName
     * @param surName
     * @param birthDate
     * @param deathDate
     * @return
     * @throws Exception
     */
    private static String prepareDefaultDisplayName(
    		String name ) throws Exception {
    	StringBuilder newStr = new StringBuilder();
			newStr.append(name);
			return newStr.toString();
    }
    
    /**
     * getCommonPart get associated location
     * @return
     */
    @Override
    public LocationsCommon getCommonPart() {
        return location;
    }

    /**
     * setCommonPart set associated location
     * @param location
     */
    @Override
    public void setCommonPart(LocationsCommon location) {
        this.location = location;
    }

    /**
     * getCommonPartList get associated location (for index/GET_ALL)
     * @return
     */
    @Override
    public LocationsCommonList getCommonPartList() {
        return locationList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(LocationsCommonList locationList) {
        this.locationList = locationList;
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
    public LocationsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(LocationsCommon locationObject, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
	public LocationsCommonList extractCommonPartList(
			DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
		LocationsCommonList coList = extractPagingInfo(new LocationsCommonList(), wrapDoc);
		List<LocationsCommonList.LocationListItem> list = coList.getLocationListItem();
		Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
		String commonPartLabel = getServiceContext().getCommonPartLabel(
				"locations");
		while (iter.hasNext()) {
			DocumentModel docModel = iter.next();
			LocationListItem ilistItem = new LocationListItem();
			ilistItem.setDisplayName((String) docModel.getProperty(
					commonPartLabel, LocationJAXBSchema.DISPLAY_NAME));
			ilistItem.setRefName((String) docModel.getProperty(commonPartLabel,
					LocationJAXBSchema.REF_NAME));
			String id = NuxeoUtils.extractId(docModel.getPathAsString());
			ilistItem.setUri("/locationauthorities/" + inAuthority + "/items/"
					+ id);
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
        return LocationConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

