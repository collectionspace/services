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
package org.collectionspace.services.taxonomy.nuxeo;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.TaxonomyJAXBSchema;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityItemDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.taxonomy.TaxonCommon;
import org.collectionspace.services.taxonomy.TaxonCommonList;
import org.collectionspace.services.taxonomy.TaxonCommonList.TaxonListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TaxonomyDocumentModelHandler
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
/**
 * @author pschmitz
 *
 */
public class TaxonDocumentModelHandler
        extends AuthorityItemDocumentModelHandler<TaxonCommon, TaxonCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(TaxonDocumentModelHandler.class);
    /**
     * Common part schema label
     */
    private static final String COMMON_PART_LABEL = "taxonomy_common";
    
    public TaxonDocumentModelHandler() {
    	super(COMMON_PART_LABEL);
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
    	String commonPartLabel = getServiceContext().getCommonPartLabel("taxonomy");
    	Boolean displayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			TaxonomyJAXBSchema.DISPLAY_NAME_COMPUTED);
    	Boolean shortDisplayNameComputed = (Boolean) docModel.getProperty(commonPartLabel,
    			TaxonomyJAXBSchema.SHORT_DISPLAY_NAME_COMPUTED);
    	if(displayNameComputed==null)
    		displayNameComputed = true;
    	if(shortDisplayNameComputed==null)
    		shortDisplayNameComputed = true;
    	if (displayNameComputed || shortDisplayNameComputed) {
    		String displayName = prepareDefaultDisplayName(
			(String)docModel.getProperty(commonPartLabel, TaxonomyJAXBSchema.NAME ));
    		if (displayNameComputed) {
    			docModel.setProperty(commonPartLabel, TaxonomyJAXBSchema.DISPLAY_NAME,
    					displayName);
    		}
    		if (shortDisplayNameComputed) {
    			docModel.setProperty(commonPartLabel, TaxonomyJAXBSchema.SHORT_DISPLAY_NAME,
    					displayName);
    		}
    	}
    }
	
    /**
     * Produces a default displayName from the basic name and dates fields.
     * @see TaxonomyAuthorityClientUtils.prepareDefaultDisplayName() which
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
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
	public TaxonCommonList extractCommonPartList(
			DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
		TaxonCommonList coList = extractPagingInfo(new TaxonCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("displayName|refName|shortIdentifier|uri|csid");
		List<TaxonCommonList.TaxonListItem> list = coList.getTaxonListItem();
		Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
		String commonPartLabel = getServiceContext().getCommonPartLabel(
				"taxonomy");
		while (iter.hasNext()) {
			DocumentModel docModel = iter.next();
			TaxonListItem ilistItem = new TaxonListItem();
			ilistItem.setDisplayName((String) docModel.getProperty(
					commonPartLabel, AuthorityItemJAXBSchema.DISPLAY_NAME));
			ilistItem.setShortIdentifier((String) docModel.getProperty(commonPartLabel,
					AuthorityItemJAXBSchema.SHORT_IDENTIFIER));
			ilistItem.setRefName((String) docModel.getProperty(commonPartLabel,
					AuthorityItemJAXBSchema.REF_NAME));
			String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
			ilistItem.setUri("/taxonomyauthorities/" + inAuthority + "/items/"
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
        return TaxonomyConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

