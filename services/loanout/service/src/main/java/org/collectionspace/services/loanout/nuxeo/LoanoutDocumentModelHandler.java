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
package org.collectionspace.services.loanout.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.LoanoutJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.loanout.LoansoutCommon;
import org.collectionspace.services.loanout.LoansoutCommonList;
import org.collectionspace.services.loanout.LoansoutCommonList.LoanoutListItem;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoanoutDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class LoanoutDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<LoansoutCommon, LoansoutCommonList> {

    private final Logger logger = LoggerFactory.getLogger(LoanoutDocumentModelHandler.class);
    /**
     * loanout is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private LoansoutCommon loanout;
    /**
     * loanoutList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private LoansoutCommonList loanoutList;


    /**
     * getCommonPart get associated loanout
     * @return
     */
    @Override
    public LoansoutCommon getCommonPart() {
        return loanout;
    }

    /**
     * setCommonPart set associated loanout
     * @param loanout
     */
    @Override
    public void setCommonPart(LoansoutCommon loanout) {
        this.loanout = loanout;
    }

    /**
     * getCommonPartList get associated loanout (for index/GET_ALL)
     * @return
     */
    @Override
    public LoansoutCommonList getCommonPartList() {
        return loanoutList;
    }

    @Override
    public void setCommonPartList(LoansoutCommonList loanoutList) {
        this.loanoutList = loanoutList;
    }

    @Override
    public LoansoutCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(LoansoutCommon loanoutObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoansoutCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        DocumentModelList docList = wrapDoc.getWrappedObject();

        LoansoutCommonList coList = new LoansoutCommonList();
        List<LoansoutCommonList.LoanoutListItem> list = coList.getLoanoutListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            LoanoutListItem ilistItem = new LoanoutListItem();
            ilistItem.setLoanOutNumber((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    LoanoutJAXBSchema.LOAN_OUT_NUMBER));
            ilistItem.setLoanReturnDate((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    LoanoutJAXBSchema.LOAN_RETURN_DATE));
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
        return LoanoutConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

