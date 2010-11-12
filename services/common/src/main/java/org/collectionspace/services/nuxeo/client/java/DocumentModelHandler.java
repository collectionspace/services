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
package org.collectionspace.services.nuxeo.client.java;

import java.util.List;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.nuxeo.client.*;
import org.collectionspace.services.common.profile.Profiler;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DocumentModelHandler is a base abstract Nuxeo document handler
 * using Nuxeo Java Remote APIs for CollectionSpace services
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class DocumentModelHandler<T, TL>
        extends AbstractMultipartDocumentHandlerImpl<T, TL, DocumentModel, DocumentModelList> {

    private final Logger logger = LoggerFactory.getLogger(DocumentModelHandler.class);
    private RepositoryInstance repositorySession;
    //key=schema, value=documentpart

    public final static String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core";
    public final static String COLLECTIONSPACE_CORE_TENANTID = "tenantId";
    public final static String COLLECTIONSPACE_CORE_CREATED_AT = "createdAt";
    public final static String COLLECTIONSPACE_CORE_UPDATED_AT = "updatedAt";

    /**
     * getRepositorySession returns Nuxeo Repository Session
     * @return
     */
    public RepositoryInstance getRepositorySession() {
        return repositorySession;
    }

    /**
     * setRepositorySession sets repository session
     * @param repoSession
     */
    public void setRepositorySession(RepositoryInstance repoSession) {
        this.repositorySession = repoSession;
    }

    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// TODO for sub-docs - check to see if the current service context is a multipart input, 
    	// OR a docfragment, and call a variant to fill the DocModel.
        fillAllParts(wrapDoc, Action.CREATE);
    }
    
    // TODO for sub-docs - Add completeCreate in which we look for set-aside doc fragments 
    // and create the subitems. We will create service contexts with the doc fragments
    // and then call 


    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// TODO for sub-docs - check to see if the current service context is a multipart input, 
    	// OR a docfragment, and call a variant to fill the DocModel.
        fillAllParts(wrapDoc, Action.UPDATE);
    }

    @Override
    public void handleGet(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        extractAllParts(wrapDoc);
    }

    @Override
    public void handleGetAll(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
    	Profiler profiler = new Profiler(this, 2);
    	profiler.start();
        setCommonPartList(extractCommonPartList(wrapDoc));
        profiler.stop();
    }

    @Override
    public abstract void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract T extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception;

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception;

    @Override
    public abstract T getCommonPart();

    @Override
    public abstract void setCommonPart(T obj);

    @Override
    public abstract TL getCommonPartList();

    @Override
    public abstract void setCommonPartList(TL obj);
    
    @Override
    public DocumentFilter createDocumentFilter() {
    	DocumentFilter filter = new NuxeoDocumentFilter(this.getServiceContext());
    	return filter;
    }
    
    /**
     * Gets the authority refs.
     *
     * @param docWrapper the doc wrapper
     * @param authRefFields the auth ref fields
     * @return the authority refs
     * @throws PropertyException the property exception
     */
    abstract public AuthorityRefList getAuthorityRefs(
            DocumentWrapper<DocumentModel> docWrapper,
		List<String> authRefFields) throws PropertyException;    

}
