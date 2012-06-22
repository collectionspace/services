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
package org.collectionspace.services.vocabulary.nuxeo;

import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.vocabulary.VocabularyItemJAXBSchema;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * VocabularyItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class VocabularyItemDocumentModelHandler
		extends AuthorityItemDocumentModelHandler<VocabularyitemsCommon> {

    private static final String COMMON_PART_LABEL = "vocabularyitems_common";   
    
    public VocabularyItemDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
    }

    @Override
    public String getAuthorityServicePath(){
        return VocabularyClient.SERVICE_PATH_COMPONENT;    // CSPACE-3932
    }
    
    @Override
    protected String getRefPropName() {
    	return ServiceBindingUtils.TERM_REF_PROP;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return VocabularyItemConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
    
    /*
     * Because the Vocabulary service's item schema is not standard, we need to override the default authority item schema behavior.
     * (non-Javadoc)
     * @see org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler#getPrimaryDisplayName(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, java.lang.String, java.lang.String)
     */
	@Override
	protected String getPrimaryDisplayName(DocumentModel docModel,
			String schema, String complexPropertyName, String fieldName) { // ignore 'complexPropertyName', and 'fieldName' -use VocabularyItem specific alternatives instead.
		String result = null;

		try {
			result = (String) docModel.getProperty(schema, VocabularyItemJAXBSchema.DISPLAY_NAME);
		} catch (Exception e) {
			throw new RuntimeException("Unknown problem retrieving property {"
					+ schema + ":" + fieldName + "}." + e.getLocalizedMessage());
		}

		return result;
	}
    
    /*
     * Because the Vocabulary service's item schema is not standard, we need to override this method.
     */
    @Override
	protected ListResultField getListResultsDisplayNameField() {
		ListResultField result = new ListResultField();

		result.setElement(VocabularyItemJAXBSchema.DISPLAY_NAME);
		result.setXpath(VocabularyItemJAXBSchema.DISPLAY_NAME);

		return result;
	}
    
    protected ListResultField getListResultsTermStatusField() {
		ListResultField result = new ListResultField();

		result.setElement(AuthorityItemJAXBSchema.TERM_STATUS);
		result.setXpath(AuthorityItemJAXBSchema.TERM_STATUS);

		return result;
    }    
    
}

