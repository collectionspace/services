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

import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.OrganizationJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.organization.OrganizationsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OrganizationDocumentModelHandler
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class OrganizationDocumentModelHandler
		extends AuthorityItemDocumentModelHandler<OrganizationsCommon> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(OrganizationDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "organizations_common";   
    
    public OrganizationDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return OrgAuthorityClient.SERVICE_PATH_COMPONENT;    //  CSPACE-3932
    }
	
    /**
     * Check the logic around the computed displayName
     * 
     * @param docModel
     * 
     * @throws Exception the exception
     */
//    @Override
//    protected void handleComputedDisplayNames(DocumentModel docModel) throws Exception {
//    	String commonPartLabel = getServiceContext().getCommonPartLabel("organizations");
//    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
//    			OrganizationJAXBSchema.DISPLAY_NAME_COMPUTED);
//    	Boolean shortDisplayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
//    			OrganizationJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED);
//    	if(displayNameComputed==null)
//    		displayNameComputed = Boolean.TRUE;
//    	if(shortDisplayNameComputed==null)
//    		shortDisplayNameComputed = Boolean.TRUE;
//    	if (displayNameComputed.booleanValue() || shortDisplayNameComputed.booleanValue()) {
//        	String shortName = getStringValueInPrimaryRepeatingComplexProperty(
//        			docModel, commonPartLabel, OrganizationJAXBSchema.MAIN_BODY_GROUP_LIST, 
//        			OrganizationJAXBSchema.SHORT_NAME);
//            // FIXME: Determine how to handle cases where primary short name is null or empty.
//    		if(shortDisplayNameComputed.booleanValue()) {
//	    		String displayName = prepareDefaultDisplayName(shortName, null);
//	    		docModel.setProperty(commonPartLabel, OrganizationJAXBSchema.SHORT_DISPLAY_NAME,
//	    				displayName);
//    		}
//    		if(displayNameComputed.booleanValue()) {
//            	String foundingPlace = (String) docModel.getProperty(commonPartLabel,
//						OrganizationJAXBSchema.FOUNDING_PLACE);
//	       		String displayName = prepareDefaultDisplayName(shortName, foundingPlace);
//				docModel.setProperty(commonPartLabel, OrganizationJAXBSchema.DISPLAY_NAME,
//							displayName);
//    		}
//    	}
//    }

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
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return OrganizationConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }

}

