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
package org.collectionspace.services.report.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.ReportJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.report.ReportsCommon;
import org.collectionspace.services.report.ReportsCommonList;
import org.collectionspace.services.report.ReportsCommonList.ReportListItem;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReportDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ReportDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<ReportsCommon, ReportsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ReportDocumentModelHandler.class);
    /**
     * report is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private ReportsCommon report;
    /**
     * reportList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private ReportsCommonList reportList;


    /**
     * getCommonPart get associated report
     * @return
     */
    @Override
    public ReportsCommon getCommonPart() {
        return report;
    }

    /**
     * setCommonPart set associated report
     * @param report
     */
    @Override
    public void setCommonPart(ReportsCommon report) {
        this.report = report;
    }

    /**
     * getCommonPartList get associated report (for index/GET_ALL)
     * @return
     */
    @Override
    public ReportsCommonList getCommonPartList() {
        return reportList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(ReportsCommonList reportList) {
        this.reportList = reportList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public ReportsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(ReportsCommon reportObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public ReportsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        ReportsCommonList coList = this.extractPagingInfo(new ReportsCommonList(), wrapDoc);
        List<ReportsCommonList.ReportListItem> list = coList.getReportListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
				String label = getServiceContext().getCommonPartLabel();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            ReportListItem ilistItem = new ReportListItem();
            ilistItem.setName((String) docModel.getProperty(label,
                    ReportJAXBSchema.NAME));
            ilistItem.setOutputMIME((String) docModel.getProperty(label,
                    ReportJAXBSchema.OUTPUT_MIME));
            ilistItem.setForSingleDoc((Boolean) docModel.getProperty(label,
                    ReportJAXBSchema.FOR_SINGLE_DOC));
            ilistItem.setForDocType((String) docModel.getProperty(label,
                    ReportJAXBSchema.FOR_DOC_TYPE));
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
        return ReportConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
 
}

