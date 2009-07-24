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
package org.collectionspace.services.collectionobject.nuxeo;

import java.util.Iterator;
import java.util.List;
import org.collectionspace.services.CollectionObjectJAXBSchema;
import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;
import org.collectionspace.services.collectionobject.CollectionObjectList.CollectionObjectListItem;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class CollectionObjectDocumentModelHandler
        extends DocumentModelHandler<CollectionObject, CollectionObjectList> {

    private final Logger logger = LoggerFactory.getLogger(CollectionObjectDocumentModelHandler.class);
    /**
     * collectionObject is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private CollectionObject collectionObject;
    /**
     * collectionObjectList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private CollectionObjectList collectionObjectList;

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

    /**
     * handleCreate processes create operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleCreate(DocumentWrapper wrapDoc) throws Exception {
        CollectionObject co = getCommonObject();
        if(co == null){
            String msg = "Error creating document: Missing input data";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        //FIXME set other parts as well
        fillCommonObject(co, wrapDoc);
    }

    /**
     * handleUpdate processes update operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleUpdate(DocumentWrapper wrapDoc) throws Exception {
        CollectionObject co = getCommonObject();
        if(co == null){
            String msg = "Error updating document: Missing input data";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        //FIXME set other parts as well
        fillCommonObject(co, wrapDoc);
    }

    /**
     * handleGet processes get operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleGet(DocumentWrapper wrapDoc) throws Exception {
        CollectionObject co = extractCommonObject(wrapDoc);
        setCommonObject(co);

        //FIXME retrive other parts as well
    }

    /**
     * handleGetAll processes index operation response
     * @param wrapDoc
     * @throws Exception
     */
    public void handleGetAll(DocumentWrapper wrapDoc) throws Exception {
        CollectionObjectList coList = extractCommonObjectList(wrapDoc);
        //FIXME, this is unncessarily called on each call from client
        setCommonObjectList(coList);
    }

    /**
     * getCommonObject get associated CollectionObject
     * @return
     */
    @Override
    public CollectionObject getCommonObject() {
        return collectionObject;
    }

    /**
     * setCommonObject set associated collectionobject
     * @param collectionObject
     */
    @Override
    public void setCommonObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * getCollectionObjectList get associated CollectionObject (for index/GET_ALL)
     * @return
     */
    @Override
    public CollectionObjectList getCommonObjectList() {
        return collectionObjectList;
    }

    @Override
    public void setCommonObjectList(CollectionObjectList collectionObjectList) {
        this.collectionObjectList = collectionObjectList;
    }

    @Override
    public CollectionObject extractCommonObject(DocumentWrapper wrapDoc)
            throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        CollectionObject co = new CollectionObject();

        //FIXME property get should be dynamically set using schema inspection
        //so it does not require hard coding

        // CollectionObject core values
        co.setObjectNumber((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.OBJECT_NUMBER)));
        co.setOtherNumber((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.OTHER_NUMBER)));
        co.setBriefDescription((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.BRIEF_DESCRIPTION)));
        co.setComments((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.COMMENTS)));
        co.setDistFeatures((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.DIST_FEATURES)));
        co.setObjectName((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.OBJECT_NAME)));
        co.setResponsibleDept((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.RESPONSIBLE_DEPT)));
        co.setTitle((String) docModel.getPropertyValue(
                getQProperty(CollectionObjectJAXBSchema.TITLE)));

        return co;
    }

    @Override
    public void fillCommonObject(CollectionObject co, DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding

        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", CollectionObjectConstants.CO_NUXEO_DC_TITLE);

        // CollectionObject core values
        if(co.getObjectNumber() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.OBJECT_NUMBER),
                    co.getObjectNumber());
        }
        if(co.getOtherNumber() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.OTHER_NUMBER),
                    co.getOtherNumber());
        }
        if(co.getBriefDescription() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.BRIEF_DESCRIPTION),
                    co.getBriefDescription());
        }
        if(co.getComments() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.COMMENTS),
                    co.getComments());
        }
        if(co.getDistFeatures() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.DIST_FEATURES),
                    co.getDistFeatures());
        }
        if(co.getObjectName() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.OBJECT_NAME),
                    co.getObjectName());
        }
        if(co.getResponsibleDept() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.RESPONSIBLE_DEPT),
                    co.getResponsibleDept());
        }
        if(co.getTitle() != null){
            docModel.setPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.TITLE),
                    co.getTitle());
        }
    }

    @Override
    public CollectionObjectList extractCommonObjectList(DocumentWrapper wrapDoc) throws Exception {
        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();

        CollectionObjectList coList = new CollectionObjectList();
        List<CollectionObjectList.CollectionObjectListItem> list = coList.getCollectionObjectListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            CollectionObjectListItem coListItem = new CollectionObjectListItem();
            coListItem.setObjectNumber((String) docModel.getPropertyValue(
                    getQProperty(CollectionObjectJAXBSchema.OBJECT_NUMBER)));
            //need fully qualified context for URI
            coListItem.setUri("/collectionobjects/" + docModel.getId());
            coListItem.setCsid(docModel.getId());
            list.add(coListItem);
        }

        return coList;
    }

    @Override
    public void fillCommonObjectList(CollectionObjectList obj, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    private String getQProperty(String prop) {
        return CollectionObjectConstants.CO_NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

