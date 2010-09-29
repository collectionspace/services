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
package org.collectionspace.services.objectexit.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.ObjectexitJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.objectexit.ObjectexitCommon;
import org.collectionspace.services.objectexit.ObjectexitCommonList;
import org.collectionspace.services.objectexit.ObjectexitCommonList.ObjectexitListItem;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ObjectExitDocumentModelHandler.
 */
public class ObjectExitDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<ObjectexitCommon, ObjectexitCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ObjectExitDocumentModelHandler.class);
    
    /** The objectexit. */
    private ObjectexitCommon objectexit;
    
    /** The objectexit list. */
    private ObjectexitCommonList objectexitList;


    /**
     * Gets the common part.
     *
     * @return the common part
     */
    @Override
    public ObjectexitCommon getCommonPart() {
        return objectexit;
    }

    /**
     * Sets the common part.
     *
     * @param objectexit the new common part
     */
    @Override
    public void setCommonPart(ObjectexitCommon objectexit) {
        this.objectexit = objectexit;
    }

    /**
     * Gets the common part list.
     *
     * @return the common part list
     */
    @Override
    public ObjectexitCommonList getCommonPartList() {
        return objectexitList;
    }

    /**
     * Sets the common part list.
     *
     * @param objectexitList the new common part list
     */
    @Override
    public void setCommonPartList(ObjectexitCommonList objectexitList) {
        this.objectexitList = objectexitList;
    }

    /**
     * Extract common part.
     *
     * @param wrapDoc the wrap doc
     * @return the objectexit common
     * @throws Exception the exception
     */
    @Override
    public ObjectexitCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Fill common part.
     *
     * @param objectexitObject the objectexit object
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    @Override
    public void fillCommonPart(ObjectexitCommon objectexitObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Extract common part list.
     *
     * @param wrapDoc the wrap doc
     * @return the objectexit common list
     * @throws Exception the exception
     */
    @Override
    public ObjectexitCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        ObjectexitCommonList coList = extractPagingInfo(new ObjectexitCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("currentOwner|depositor|exitDate|exitMethod|exitNote|exitNumber|exitReason|packingNote|uri|csid");
        List<ObjectexitCommonList.ObjectexitListItem> list = coList.getObjectexitListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            ObjectexitListItem ilistItem = new ObjectexitListItem();

            String label = getServiceContext().getCommonPartLabel();
            ilistItem.setExitNumber((String) docModel.getProperty(label, ObjectexitJAXBSchema.OBJECT_EXIT_NUMBER));
            ilistItem.setExitDate((String) docModel.getProperty(label, ObjectexitJAXBSchema.OBJECT_EXIT_DATE));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri(getServiceContextPath() + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
        }

        return coList;
    }

    /**
     * Gets the q property.
     *
     * @param prop the prop
     * @return the q property
     */
    @Override
    public String getQProperty(String prop) {
        return ObjectExitConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

