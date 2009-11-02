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

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.VocabularyItemJAXBSchema;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandler;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.vocabulary.VocabularyitemsCommon;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList.VocabularyitemListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VocabularyItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class VocabularyItemDocumentModelHandler
        extends RemoteDocumentModelHandler<VocabularyitemsCommon, VocabularyitemsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(VocabularyItemDocumentModelHandler.class);
    /**
     * vocabularyItem is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private VocabularyitemsCommon vocabularyItem;
    /**
     * vocabularyItemList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private VocabularyitemsCommonList vocabularyItemList;
    
    /**
     * inVocabulary is the parent vocabulary for this context
     */
    private String inVocabulary;

    public String getInVocabulary() {
		return inVocabulary;
	}

	public void setInVocabulary(String inVocabulary) {
		this.inVocabulary = inVocabulary;
	}

	@Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

    /**
     * getCommonPart get associated vocabularyItem
     * @return
     */
    @Override
    public VocabularyitemsCommon getCommonPart() {
        return vocabularyItem;
    }

    /**
     * setCommonPart set associated vocabularyItem
     * @param vocabularyItem
     */
    @Override
    public void setCommonPart(VocabularyitemsCommon vocabularyItem) {
        this.vocabularyItem = vocabularyItem;
    }

    /**
     * getCommonPartList get associated vocabularyItem (for index/GET_ALL)
     * @return
     */
    @Override
    public VocabularyitemsCommonList getCommonPartList() {
        return vocabularyItemList;
    }

    @Override
    public void setCommonPartList(VocabularyitemsCommonList vocabularyItemList) {
        this.vocabularyItemList = vocabularyItemList;
    }

    @Override
    public VocabularyitemsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(VocabularyitemsCommon vocabularyItemObject, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public VocabularyitemsCommonList extractCommonPartList(DocumentWrapper wrapDoc) 
    	throws Exception {
        VocabularyitemsCommonList coList = new VocabularyitemsCommonList();
        try{
	        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();
	
	        List<VocabularyitemsCommonList.VocabularyitemListItem> list = 
	        	coList.getVocabularyitemListItem();
	
	        //FIXME: iterating over a long list of documents is not a long term
	        //strategy...need to change to more efficient iterating in future
	        Iterator<DocumentModel> iter = docList.iterator();
	        while(iter.hasNext()){
	            DocumentModel docModel = iter.next();
	            String parentVocab = (String)docModel.getProperty(getServiceContext().getCommonPartLabel("vocabularyItems"),
	                    VocabularyItemJAXBSchema.IN_VOCABULARY); 
	            if( !inVocabulary.equals(parentVocab))
	            	continue;
	            VocabularyitemListItem ilistItem = new VocabularyitemListItem();
	            ilistItem.setDisplayName((String) docModel.getProperty(getServiceContext().getCommonPartLabel("vocabularyItems"),
	                    VocabularyItemJAXBSchema.DISPLAY_NAME));
                    String id = NuxeoUtils.extractId(docModel.getPathAsString());
	            ilistItem.setUri("/vocabularies/"+inVocabulary+"/items/" + id);
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


    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getDocumentType()
     */
    @Override
    public String getDocumentType() {
        return VocabularyItemConstants.NUXEO_DOCTYPE;
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
}

