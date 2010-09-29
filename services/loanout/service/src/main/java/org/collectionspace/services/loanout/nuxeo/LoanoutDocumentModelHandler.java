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
import org.collectionspace.services.jaxb.AbstractCommonList;
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
 * The Class LoanoutDocumentModelHandler.
 */
public class LoanoutDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<LoansoutCommon, LoansoutCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(LoanoutDocumentModelHandler.class);
    
    /** The loanout. */
    private LoansoutCommon loanout;
    
    /** The loanout list. */
    private LoansoutCommonList loanoutList;


    /**
     * Gets the common part.
     *
     * @return the common part
     */
    @Override
    public LoansoutCommon getCommonPart() {
        return loanout;
    }

    /**
     * Sets the common part.
     *
     * @param loanout the new common part
     */
    @Override
    public void setCommonPart(LoansoutCommon loanout) {
        this.loanout = loanout;
    }

    /**
     * Gets the common part list.
     *
     * @return the common part list
     */
    @Override
    public LoansoutCommonList getCommonPartList() {
        return loanoutList;
    }

    /**
     * Sets the common part list.
     *
     * @param loanoutList the new common part list
     */
    @Override
    public void setCommonPartList(LoansoutCommonList loanoutList) {
        this.loanoutList = loanoutList;
    }

    /**
     * Extract common part.
     *
     * @param wrapDoc the wrap doc
     * @return the loansout common
     * @throws Exception the exception
     */
    @Override
    public LoansoutCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Fill common part.
     *
     * @param loanoutObject the loanout object
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    @Override
    public void fillCommonPart(LoansoutCommon loanoutObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Extract common part list.
     *
     * @param wrapDoc the wrap doc
     * @return the loansout common list
     * @throws Exception the exception
     */
    @Override
    public LoansoutCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        LoansoutCommonList coList = extractPagingInfo(new LoansoutCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("loanOutNumber|borrower|loanReturnDate|uri|csid");
        List<LoansoutCommonList.LoanoutListItem> list = coList.getLoanoutListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        String label = getServiceContext().getCommonPartLabel();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            LoanoutListItem ilistItem = new LoanoutListItem();
            ilistItem.setLoanOutNumber((String) docModel.getProperty(label,
                    LoanoutJAXBSchema.LOAN_OUT_NUMBER));
            ilistItem.setBorrower((String) docModel.getProperty(label,
                    LoanoutJAXBSchema.BORROWER));
            ilistItem.setLoanReturnDate((String) docModel.getProperty(label,
                    LoanoutJAXBSchema.LOAN_RETURN_DATE));
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
        return LoanoutConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

