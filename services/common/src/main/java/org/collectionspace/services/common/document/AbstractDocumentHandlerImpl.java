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
import java.util.List;
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
public abstract class AbstractDocumentHandlerImpl<T, TL, WT, WTL>
        implements DocumentHandler<T, TL, WT, WTL> {

    private final Logger logger = LoggerFactory.getLogger(AbstractDocumentHandlerImpl.class);
    private Map<String, Object> properties = new HashMap<String, Object>();
    private DocumentFilter docFilter = null;
    private ServiceContext serviceContext;

    public AbstractDocumentHandlerImpl() {
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

//    public void initializeDocumentFilter(ServiceContext ctx) {
//    	DocumentFilter docFilter = this.createDocumentFilter(ctx);
//    	this.setDocumentFilter(docFilter);
//    }
    @Override
    public abstract DocumentFilter createDocumentFilter();

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
    final public void prepare(Action action) throws Exception {
        switch (action) {
            case CREATE:
                validate(action);
                prepareCreate();
                break;

            case UPDATE:
                validate(action);
                prepareUpdate();
                break;

            case GET:
                prepareGet();
                break;

            case GET_ALL:
                prepareGetAll();
                break;

            case DELETE:
                prepareDelete();
                break;

        }
    }

    @Override
    public void prepareCreate() throws Exception {
    }

    @Override
    public void prepareUpdate() throws Exception {
    }

    @Override
    public void prepareGet() throws Exception {
    }

    @Override
    public void prepareGetAll() throws Exception {
    }

    @Override
    public void prepareDelete() throws Exception {
    }

    @Override
    final public void handle(Action action, DocumentWrapper<?> wrapDoc) throws Exception {
        switch (action) {
            case CREATE:
                handleCreate((DocumentWrapper<WT>) wrapDoc);
                break;

            case UPDATE:
                handleUpdate((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET:
                handleGet((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET_ALL:
                handleGetAll((DocumentWrapper<WTL>) wrapDoc);
                break;

            case DELETE:
                handleDelete((DocumentWrapper<WT>) wrapDoc);
                break;

        }
    }

    @Override
    public abstract void handleCreate(DocumentWrapper<WT> wrapDoc) throws Exception;

    @Override
    public abstract void handleUpdate(DocumentWrapper<WT> wrapDoc) throws Exception;

    @Override
    public abstract void handleGet(DocumentWrapper<WT> wrapDoc) throws Exception;

    @Override
    public abstract void handleGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception;

    @Override
    public void handleDelete(DocumentWrapper<WT> wrapDoc) throws Exception {
        
    }

    @Override
    final public void complete(Action action, DocumentWrapper<?> wrapDoc) throws Exception {
        switch (action) {
            case CREATE:
                completeCreate((DocumentWrapper<WT>) wrapDoc);
                break;

            case UPDATE:
                completeUpdate((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET:
                completeGet((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET_ALL:
                completeGetAll((DocumentWrapper<WTL>) wrapDoc);
                break;

            case DELETE:
                completeDelete((DocumentWrapper<WT>) wrapDoc);
                break;
        }
    }

    @Override
    public void completeCreate(DocumentWrapper<WT> wrapDoc) throws Exception {
    }

    @Override
    public void completeUpdate(DocumentWrapper<WT> wrapDoc) throws Exception {
        //no specific action needed
    }

    @Override
    public void completeGet(DocumentWrapper<WT> wrapDoc) throws Exception {
    }

    @Override
    public void completeGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception {
    }

    @Override
    public void completeDelete(DocumentWrapper<WT> wrapDoc) throws Exception {
    }

    @Override
    public abstract T extractCommonPart(DocumentWrapper<WT> wrapDoc)
            throws Exception;

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper<WT> wrapDoc)
            throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper<WTL> wrapDoc)
            throws Exception;

    @Override
    final public void fillCommonPartList(TL obj, DocumentWrapper<WTL> wrapDoc) throws Exception {
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
        if (tkz.countTokens() != 2) {
            String msg = "Property must be in the form xxx:yyy, "
                    + "e.g. collectionobjects_common:objectNumber";
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

    private void validate(Action action) throws Exception {
        List<ValidatorHandler> valHandlers = serviceContext.getValidatorHandlers();
        for (ValidatorHandler handler : valHandlers) {
            handler.validate(action, serviceContext);
        }
    }
}
