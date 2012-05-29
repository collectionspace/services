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
package org.collectionspace.services.dimension.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.dimension.DimensionJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.dimension.DimensionsCommon;
import org.collectionspace.services.dimension.DimensionsCommonList;
import org.collectionspace.services.dimension.DimensionsCommonList.DimensionListItem;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DimensionDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class DimensionDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<DimensionsCommon, DimensionsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(DimensionDocumentModelHandler.class);
    /**
     * dimension is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private DimensionsCommon dimension;
    /**
     * intakeList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private DimensionsCommonList intakeList;


    /**
     * getCommonPart get associated dimension
     * @return
     */
    @Override
    public DimensionsCommon getCommonPart() {
        return dimension;
    }

    /**
     * setCommonPart set associated dimension
     * @param dimension
     */
    @Override
    public void setCommonPart(DimensionsCommon dimension) {
        this.dimension = dimension;
    }

    /**
     * getCommonPartList get associated dimension (for index/GET_ALL)
     * @return
     */
    @Override
    public DimensionsCommonList getCommonPartList() {
        return intakeList;
    }

    @Override
    public void setCommonPartList(DimensionsCommonList intakeList) {
        this.intakeList = intakeList;
    }

    @Override
    public DimensionsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(DimensionsCommon intakeObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public DimensionsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        DimensionsCommonList coList = extractPagingInfo(new DimensionsCommonList(), wrapDoc) ;
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("dimension|uri|csid");
        List<DimensionsCommonList.DimensionListItem> list = coList.getDimensionListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            DimensionListItem ilistItem = new DimensionListItem();
            ilistItem.setDimension((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    DimensionJAXBSchema.DIMENSION));
            String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri(getServiceContextPath() + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
        }

        return coList;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return DimensionConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
    
}

