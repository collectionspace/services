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
package org.collectionspace.services.intake.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.IntakeJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.intake.IntakesCommonList;
import org.collectionspace.services.intake.IntakesCommonList.IntakeListItem;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntakeDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class IntakeDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<IntakesCommon, IntakesCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(IntakeDocumentModelHandler.class);
    /**
     * intake is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private IntakesCommon intake;
    /**
     * intakeList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private IntakesCommonList intakeList;


    /**
     * getCommonPart get associated intake
     * @return
     */
    @Override
    public IntakesCommon getCommonPart() {
        return intake;
    }

    /**
     * setCommonPart set associated intake
     * @param intake
     */
    @Override
    public void setCommonPart(IntakesCommon intake) {
        this.intake = intake;
    }

    /**
     * getCommonPartList get associated intake (for index/GET_ALL)
     * @return
     */
    @Override
    public IntakesCommonList getCommonPartList() {
        return intakeList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(IntakesCommonList intakeList) {
        this.intakeList = intakeList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public IntakesCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(IntakesCommon intakeObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public IntakesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        IntakesCommonList coList = this.extractPagingInfo(new IntakesCommonList(), wrapDoc);
        List<IntakesCommonList.IntakeListItem> list = coList.getIntakeListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        String label = getServiceContext().getCommonPartLabel();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            IntakeListItem ilistItem = new IntakeListItem();
            ilistItem.setEntryNumber((String) docModel.getProperty(label,
                    IntakeJAXBSchema.ENTRY_NUMBER));
            ilistItem.setDepositor((String) docModel.getProperty(label,
                    IntakeJAXBSchema.DEPOSITOR));
            ilistItem.setCurrentOwner((String) docModel.getProperty(label,
                    IntakeJAXBSchema.CURRENT_OWNER));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
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
        return IntakeConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

