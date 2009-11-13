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
package org.collectionspace.services.common.document;

import java.util.HashMap;
import java.util.Map;

import java.util.StringTokenizer;
import org.collectionspace.services.common.context.ServiceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractDocumentHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AbstractDocumentHandler<T, TL>
        implements DocumentHandler<T, TL> {

    private final Logger logger = LoggerFactory.getLogger(AbstractDocumentHandler.class);
    private Map<String, Object> properties = new HashMap<String, Object>();
    private DocumentFilter docFilter = new DocumentFilter();
    private ServiceContext serviceContext;

    public AbstractDocumentHandler() {
    }

    @Override
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    @Override
    public void setServiceContext(ServiceContext ctx) {
        serviceContext = ctx;
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

    /**
     * @return the DocumentFilter
     */
    @Override
    public DocumentFilter getDocumentFilter() {
        return docFilter;
    }

    /**
     * @param properties the DocumentFilter to set
     */
    @Override
    public void setDocumentFilter(DocumentFilter docFilter) {
        this.docFilter = docFilter;
    }

    @Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

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
    public abstract void handleCreate(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract void handleUpdate(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract void handleGet(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract void handleGetAll(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public void complete(Action action, DocumentWrapper wrapDoc) throws Exception {
        switch(action){
            //TODO: add more actions if needed
            case UPDATE:
                completeUpdate(wrapDoc);
                break;
        }
    }

    @Override
    public void completeUpdate(DocumentWrapper wrapDoc) throws Exception {
        //no specific action needed
    }

    @Override
    public abstract void extractAllParts(DocumentWrapper wrapDoc)
            throws Exception;

    @Override
    public abstract void fillAllParts(DocumentWrapper wrapDoc)
            throws Exception;

    @Override
    public abstract T extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception;

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper wrapDoc)
            throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper wrapDoc)
            throws Exception;

    @Override
    final public void fillCommonPartList(TL obj, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException("bulk create/update not yet supported");
    }

    @Override
    public abstract T getCommonPart();

    @Override
    public abstract void setCommonPart(T obj);

    @Override
    public abstract TL getCommonPartList();

    @Override
    public abstract void setCommonPartList(TL obj);

    @Override
    public abstract String getQProperty(String prop);

    @Override
    public String getUnQProperty(String qProp) {
        StringTokenizer tkz = new StringTokenizer(qProp, ":");
        if(tkz.countTokens() != 2){
            String msg = "Property must be in the form xxx:yyy, " +
                    "e.g. collectionobjects_common:objectNumber";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        tkz.nextToken(); //skip
        return tkz.nextToken();
    }

    @Override
    public String getServiceContextPath() {
        return "/" + getServiceContext().getServiceName().toLowerCase() + "/";
    }
}
