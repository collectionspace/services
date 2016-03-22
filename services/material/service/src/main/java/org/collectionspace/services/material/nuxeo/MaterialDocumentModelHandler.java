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
package org.collectionspace.services.material.nuxeo;

import org.collectionspace.services.MaterialJAXBSchema;
import org.collectionspace.services.client.MaterialAuthorityClient;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.material.MaterialsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * MaterialDocumentModelHandler
 *
 */
public class MaterialDocumentModelHandler
        extends AuthorityItemDocumentModelHandler<MaterialsCommon> {

    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "materials_common";
    
    public MaterialDocumentModelHandler() {
        super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return MaterialAuthorityClient.SERVICE_PATH_COMPONENT;    //  CSPACE-3932
    }

        /**
     * Handle display name.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
//    @Override
//    protected void handleComputedDisplayNames(DocumentModel docModel) throws Exception {
//        String commonPartLabel = getServiceContext().getCommonPartLabel("materials");
//      Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
//              MaterialJAXBSchema.DISPLAY_NAME_COMPUTED);
//      Boolean shortDisplayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
//              MaterialJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED);
//      if(displayNameComputed==null)
//          displayNameComputed = true;
//      if(shortDisplayNameComputed==null)
//          shortDisplayNameComputed = true;
//      if (displayNameComputed || shortDisplayNameComputed) {
//                // Obtain the primary material name from the list of material names, for computing the display name.
//          String xpathToMaterialName = MaterialJAXBSchema.MATERIAL_TERM_NAME_GROUP_LIST 
//                        + "/[0]/" + MaterialeJAXBSchema.MATERIAL_TERM_NAME;
//          String materialName = getXPathStringValue(docModel, COMMON_PART_LABEL, xpathToMaterialName);
//          String displayName = prepareDefaultDisplayName(materialName);
//          if (displayNameComputed) {
//              docModel.setProperty(commonPartLabel, MaterialJAXBSchema.DISPLAY_NAME,
//                      displayName);
//          }
//          if (shortDisplayNameComputed) {
//              docModel.setProperty(commonPartLabel, MaterialJAXBSchema.SHORT_DISPLAY_NAME,
//                      displayName);
//          }
//      }
//    }
    
    /**
     * Produces a default displayName from one or more supplied fields.
     * @see MaterialAuthorityClientUtils.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param materialName
     * @return the default display name
     * @throws Exception
     */
    private static String prepareDefaultDisplayName(
            String materialName ) throws Exception {
        StringBuilder newStr = new StringBuilder();
            newStr.append(materialName);
            return newStr.toString();
    }
    
    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return MaterialConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

