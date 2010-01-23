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

import org.collectionspace.services.common.document.AbstractMultipartDocumentHandler;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.*;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
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
        extends AbstractMultipartDocumentHandler<T, TL, DocumentModel, DocumentModelList> {

    private final Logger logger = LoggerFactory.getLogger(DocumentModelHandler.class);
    private RepositoryInstance repositorySession;
    //key=schema, value=documentpart

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
        fillAllParts(wrapDoc);
    }

    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        fillAllParts(wrapDoc);
    }

    @Override
    public void handleGet(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        extractAllParts(wrapDoc);
    }

    @Override
    public void handleGetAll(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        setCommonPartList(extractCommonPartList(wrapDoc));
    }

    @Override
    public abstract void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract T extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

    @Override
    public abstract void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception;

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
        return new NuxeoDocumentFilter();
    }

}
