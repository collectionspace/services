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

import org.collectionspace.services.LocationJAXBSchema;
import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.location.LocationsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

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
        extends AuthorityItemDocumentModelHandler<LocationsCommon> {

    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "locations_common";
    
    public LocationDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return LocationAuthorityClient.SERVICE_PATH_COMPONENT;    //  CSPACE-3932
    }
	
    /**
     * Handle display name.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
//    @Override
//    protected void handleComputedDisplayNames(DocumentModel docModel) throws Exception {
//    	String commonPartLabel = getServiceContext().getCommonPartLabel("locations");
//    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
//    			LocationJAXBSchema.DISPLAY_NAME_COMPUTED);
//    	Boolean shortDisplayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
//    			LocationJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED);
//    	if(displayNameComputed==null)
//    		displayNameComputed = true;
//    	if(shortDisplayNameComputed==null)
//    		shortDisplayNameComputed = true;
//    	if (displayNameComputed || shortDisplayNameComputed) {
//    		String displayName = prepareDefaultDisplayName(
//			(String)docModel.getProperty(commonPartLabel, LocationJAXBSchema.NAME ));
//    		if (displayNameComputed) {
//    			docModel.setProperty(commonPartLabel, LocationJAXBSchema.DISPLAY_NAME,
//    					displayName);
//    		}
//    		if (shortDisplayNameComputed) {
//    			docModel.setProperty(commonPartLabel, LocationJAXBSchema.SHORT_DISPLAY_NAME,
//    					displayName);
//    		}
//    	}
//    }
	
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
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return LocationConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

