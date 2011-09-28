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
package org.collectionspace.services.place.nuxeo;

import org.collectionspace.services.PlaceJAXBSchema;
import org.collectionspace.services.client.PlaceAuthorityClient;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.place.PlacesCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * PlaceDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
/**
 * @author pschmitz
 *
 */
public class PlaceDocumentModelHandler
        extends AuthorityItemDocumentModelHandler<PlacesCommon> {

    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "places_common";
    
    public PlaceDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return PlaceAuthorityClient.SERVICE_PATH_COMPONENT;    //  CSPACE-3932
    }
	
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// first fill all the parts of the document
    	super.handleCreate(wrapDoc);    	
    	handleDisplayNames(wrapDoc.getWrappedObject());
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	super.handleUpdate(wrapDoc);
    	handleDisplayNames(wrapDoc.getWrappedObject());
    }

    /**
     * Handle display name.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
    private void handleDisplayNames(DocumentModel docModel) throws Exception {
    	String commonPartLabel = getServiceContext().getCommonPartLabel("places");
    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			PlaceJAXBSchema.DISPLAY_NAME_COMPUTED);
    	Boolean shortDisplayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			PlaceJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED);
    	if(displayNameComputed==null)
    		displayNameComputed = true;
    	if(shortDisplayNameComputed==null)
    		shortDisplayNameComputed = true;
    	if (displayNameComputed || shortDisplayNameComputed) {
    		String xpathToName = "placeNameGroupList/[0]/name";
    		String name = getXPathStringValue(docModel, COMMON_PART_LABEL, xpathToName);
    		String displayName = prepareDefaultDisplayName(name);
    		if (displayNameComputed) {
    			docModel.setProperty(commonPartLabel, PlaceJAXBSchema.DISPLAY_NAME,
    					displayName);
    		}
    		if (shortDisplayNameComputed) {
    			docModel.setProperty(commonPartLabel, PlaceJAXBSchema.SHORT_DISPLAY_NAME,
    					displayName);
    		}
    	}
    }
	
    /**
     * Produces a default displayName from the basic name and dates fields.
     * @see PlaceAuthorityClientUtils.prepareDefaultDisplayName() which
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
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return PlaceConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

