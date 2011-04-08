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
package org.collectionspace.services.common.vocabulary.nuxeo;

import java.util.Map;

import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * AuthorityItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityItemDocumentModelHandler<AICommon, AICommonList>
        extends RemoteDocumentModelHandlerImpl<AICommon, AICommonList> {

	private String authorityItemCommonSchemaName;
	
    //private final Logger logger = LoggerFactory.getLogger(AuthorityItemDocumentModelHandler.class);
    /**
     * item is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    protected AICommon item;
    /**
     * itemList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    protected AICommonList itemList;
    
    /**
     * inVocabulary is the parent Authority for this context
     */
    protected String inAuthority;
    
    public AuthorityItemDocumentModelHandler(String authorityItemCommonSchemaName) {
    	this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }

    public String getInAuthority() {
		return inAuthority;
	}

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
    	handleInAuthority(wrapDoc.getWrappedObject());
    }
    
    /**
     * Check the logic around the parent pointer. Note that we only need do this on
     * create, since we have logic to make this read-only on update. 
     * 
     * @param docModel
     * 
     * @throws Exception the exception
     */
    private void handleInAuthority(DocumentModel docModel) throws Exception {
    	docModel.setProperty(authorityItemCommonSchemaName, 
    			AuthorityItemJAXBSchema.IN_AUTHORITY, inAuthority);
    }


    /**
     * getCommonPart get associated item
     * @return
     */
    @Override
    public AICommon getCommonPart() {
        return item;
    }

    /**
     * setCommonPart set associated item
     * @param vocabularyItem
     */
    @Override
    public void setCommonPart(AICommon item) {
        this.item = item;
    }

    /**
     * getCommonPartList get associated item (for index/GET_ALL)
     * @return
     */
    @Override
    public AICommonList getCommonPartList() {
        return itemList;
    }

    @Override
    public void setCommonPartList(AICommonList itemList) {
        this.itemList = itemList;
    }

    @Override
    public AICommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(AICommon itemObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);
    	
    	// Add the CSID to the common part
    	if (partMeta.getLabel().equalsIgnoreCase(authorityItemCommonSchemaName)) {
	    	String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
	    	unQObjectProperties.put("csid", csid);
    	}
    	
    	return unQObjectProperties;
    }
    
    /**
     * Filters out AuthorityItemJAXBSchema.IN_AUTHORITY, to ensure that
     * the parent link remains untouched.
     * @param objectProps the properties parsed from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
    		Map<String, Object> objectProps, ObjectPartType partMeta) {
    	super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
    	objectProps.remove(AuthorityItemJAXBSchema.IN_AUTHORITY);
    	objectProps.remove(AuthorityItemJAXBSchema.CSID);
    }

}

