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

import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.common.repository.DocumentException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.collectionspace.services.nuxeo.client.*;
import org.dom4j.Document;
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
        implements DocumentHandler<T, TL> {

    private final Logger logger = LoggerFactory.getLogger(RepresentationHandler.class);
    private Map<String, Object> properties = new HashMap<String, Object>();
    private List<String> pathParams = new ArrayList<String>();
    private Map<String, String> queryParams = new HashMap<String, String>();
    private Document document;
    private InputStream inputStream = null;

    @Override
    public abstract void prepare(Action action) throws Exception;

    @Override
    public void handle(Action action, DocumentWrapper wrapDoc) throws Exception {
        switch(action){
            case CREATE:
                handleCreate(wrapDoc);
                break;
            case UPDATE:
                handleUpdate(wrapDoc);
                break;
            case GET:
                handleGet(wrapDoc);
                break;
            case GET_ALL:
                handleGetAll(wrapDoc);
                break;
        }
    }

    @Override
    public void handleCreate(DocumentWrapper wrapDoc) throws Exception {
        if(getCommonObject() == null){
            String msg = "Error creating document: Missing input data";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        //FIXME set other parts as well
        fillCommonObject(getCommonObject(), wrapDoc);
    }

    @Override
    public void handleUpdate(DocumentWrapper wrapDoc) throws Exception {
        if(getCommonObject() == null){
            String msg = "Error updating document: Missing input data";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        //FIXME set other parts as well
        fillCommonObject(getCommonObject(), wrapDoc);
    }

    @Override
    public void handleGet(DocumentWrapper wrapDoc) throws Exception {
        setCommonObject(extractCommonObject(wrapDoc));

        //FIXME retrive other parts as well
    }

    @Override
    public void handleGetAll(DocumentWrapper wrapDoc) throws Exception {
        setCommonObjectList(extractCommonObjectList(wrapDoc));
    }

    @Override
    public abstract T extractCommonObject(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract void fillCommonObject(T obj, DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract TL extractCommonObjectList(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract void fillCommonObjectList(TL obj, DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract T getCommonObject();

    @Override
    public abstract void setCommonObject(T obj);

    @Override
    public abstract TL getCommonObjectList();

    @Override
    public abstract void setCommonObjectList(TL obj);

    @Override
    public Document getDocument(DocumentWrapper wrapDoc) throws DocumentException {
        throw new UnsupportedOperationException("DocumentHandler.getDocument(wrapDoc)");
    }

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

    /**
     * @return the properties
     */
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
