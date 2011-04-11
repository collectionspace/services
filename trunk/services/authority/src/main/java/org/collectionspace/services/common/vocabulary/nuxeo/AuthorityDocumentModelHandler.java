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

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * AuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityDocumentModelHandler<AuthCommon, AuthCommonList>
        extends RemoteDocumentModelHandlerImpl<AuthCommon, AuthCommonList> {

	private String authorityCommonSchemaName;
	
    /**
     * authority is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private AuthCommon authority;
    /**
     * authorityList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private AuthCommonList authorityList;


    public AuthorityDocumentModelHandler(String authorityCommonSchemaName) {
    	this.authorityCommonSchemaName = authorityCommonSchemaName;
    }

    /**
     * getCommonPart get associated authority
     * @return
     */
    @Override
    public AuthCommon getCommonPart() {
        return authority;
    }

    /**
     * setCommonPart set associated authority
     * @param authority
     */
    @Override
    public void setCommonPart(AuthCommon authority) {
        this.authority = authority;
    }

    /**
     * getCommonPartList get associated authority (for index/GET_ALL)
     * @return
     */
    @Override
    public AuthCommonList getCommonPartList() {
        return authorityList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(AuthCommonList authorityList) {
        this.authorityList = authorityList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public AuthCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(AuthCommon vocabularyObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
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
    	if (partMeta.getLabel().equalsIgnoreCase(authorityCommonSchemaName)) {
	    	String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
	    	unQObjectProperties.put("csid", csid);
    	}
    	
    	return unQObjectProperties;
    }
    
}

