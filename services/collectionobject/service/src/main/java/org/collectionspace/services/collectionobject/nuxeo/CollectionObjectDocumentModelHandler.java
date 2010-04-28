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

//import org.collectionspace.services.jaxb.AbstractCommonList;

import org.collectionspace.services.CollectionObjectListItemJAXBSchema;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList;
import org.collectionspace.services.collectionobject.CollectionobjectsCommonList.CollectionObjectListItem;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
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
        extends RemoteDocumentModelHandlerImpl<CollectionobjectsCommon, CollectionobjectsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(CollectionObjectDocumentModelHandler.class);
    /**
     * collectionObject is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private CollectionobjectsCommon collectionObject;
    /**
     * collectionObjectList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private CollectionobjectsCommonList collectionObjectList;

    /**
     * getCommonPart get associated CollectionobjectsCommon
     * @return
     */
    @Override
    public CollectionobjectsCommon getCommonPart() {
        return collectionObject;
    }

    /**
     * setCommonPart set associated collectionobject
     * @param collectionObject
     */
    @Override
    public void setCommonPart(CollectionobjectsCommon collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * getCollectionobjectsCommonList get associated CollectionobjectsCommon (for index/GET_ALL)
     * @return
     */
    @Override
    public CollectionobjectsCommonList getCommonPartList() {
        return collectionObjectList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(CollectionobjectsCommonList collectionObjectList) {
        this.collectionObjectList = collectionObjectList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public CollectionobjectsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(CollectionobjectsCommon co, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public CollectionobjectsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        CollectionobjectsCommonList coList = this.extractPagingInfo(new CollectionobjectsCommonList(),
        		wrapDoc);
        List<CollectionobjectsCommonList.CollectionObjectListItem> list = coList.getCollectionObjectListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            CollectionObjectListItem coListItem = new CollectionObjectListItem();
            coListItem.setObjectNumber((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
            		CollectionObjectListItemJAXBSchema.OBJECT_NUMBER));
            coListItem.setTitle((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
            		CollectionObjectListItemJAXBSchema.TITLE));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            coListItem.setUri(getServiceContextPath() + id);
            coListItem.setCsid(id);
            list.add(coListItem);
        }

        return coList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#fillAllParts(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {

        super.fillAllParts(wrapDoc);
        fillDublinCoreObject(wrapDoc); //dublincore might not be needed in future
    }

    /**
     * Fill dublin core object.
     *
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    private void fillDublinCoreObject(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding
        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", CollectionObjectConstants.NUXEO_DC_TITLE);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(String prop) {
        return CollectionObjectConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

