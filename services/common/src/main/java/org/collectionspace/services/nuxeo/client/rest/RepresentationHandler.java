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
package org.collectionspace.services.nuxeo.client.rest;

import org.collectionspace.services.common.repository.DocumentWrapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.collectionspace.services.common.repository.AbstractDocumentHandler;
import org.collectionspace.services.nuxeo.client.*;
import org.w3c.dom.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RepresentationHandler is a base abstract Nuxeo document handler
 * using Nuxeo RESTful APIs for CollectionSpace services
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class RepresentationHandler<T, TL>
        extends AbstractDocumentHandler<T, TL> {

    private final Logger logger = LoggerFactory.getLogger(RepresentationHandler.class);
    private List<String> pathParams = new ArrayList<String>();
    private Map<String, String> queryParams = new HashMap<String, String>();
    private Document document;
    private InputStream inputStream = null;

    @Override
    public void handleCreate(DocumentWrapper wrapDoc) throws Exception {
        fillAllParts(wrapDoc);
    }

    @Override
    public void handleUpdate(DocumentWrapper wrapDoc) throws Exception {
        fillAllParts(wrapDoc);
    }

    @Override
    public void handleGet(DocumentWrapper wrapDoc) throws Exception {
        extractAllParts(wrapDoc);
    }

    @Override
    public void handleGetAll(DocumentWrapper wrapDoc) throws Exception {
        setCommonPartList(extractCommonPartList(wrapDoc));
    }

    @Override
    public void extractAllParts(DocumentWrapper wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));

        //FIXME retrive other parts as well
    }

    @Override
    public abstract T extractCommonPart(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public void fillAllParts(DocumentWrapper wrapDoc) throws Exception {
        if(getCommonPart() == null){
            String msg = "Error creating document: Missing input data";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        //FIXME set other parts as well
        fillCommonPart(getCommonPart(), wrapDoc);
    }

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract T getCommonPart();

    @Override
    public abstract void setCommonPart(T obj);

    @Override
    public abstract TL getCommonPartList();

    @Override
    public abstract void setCommonPartList(TL obj);

    /**
     * @return the pathParams
     */
    public List<String> getPathParams() {
        return pathParams;
    }

    /**
     * @param pathParams the pathParams to set
     */
    public void setPathParams(List<String> pathParams) {
        this.pathParams = pathParams;
    }

    /**
     * @return the queryParams
     */
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * @param queryParams the queryParams to set
     */
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    /**
     * getInputStream to retrieve input stream by client for posting a document
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * setInputStream to set input stream to read for posting document
     * @param inputStream the inputStream to set
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }
}
