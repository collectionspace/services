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
package org.collectionspace.services.acquisition.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.AcquisitionJAXBSchema;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.acquisition.Acquisition;
import org.collectionspace.services.acquisition.AcquisitionList;
import org.collectionspace.services.acquisition.AcquisitionList.AcquisitionListItem;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.acquisition.nuxeo.AcquisitionConstants;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcquisitionDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class AcquisitionDocumentModelHandler
        extends DocumentModelHandler<Acquisition, AcquisitionList> {

    private final Logger logger = LoggerFactory.getLogger(AcquisitionDocumentModelHandler.class);
    /**
     * acquisition is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private Acquisition acquisition;
    /**
     * acquisitionList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private AcquisitionList acquisitionList;

    @Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

    /**
     * getCommonObject get associated acquisition
     * @return
     */
    @Override
    public Acquisition getCommonObject() {
        return acquisition;
    }

    /**
     * setCommonObject set associated acquisition
     * @param acquisition
     */
    @Override
    public void setCommonObject(Acquisition acquisition) {
        this.acquisition = acquisition;
    }

    /**
     * getAcquisitionList get associated acquisition (for index/GET_ALL)
     * @return
     */
    @Override
    public AcquisitionList getCommonObjectList() {
        return acquisitionList;
    }

    @Override
    public void setCommonObjectList(AcquisitionList acquisitionList) {
        this.acquisitionList = acquisitionList;
    }

    @Override
    public Acquisition extractCommonObject(DocumentWrapper wrapDoc)
            throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        Acquisition acquisitionObject = new Acquisition();

        //FIXME property get should be dynamically set using schema inspection
        //so it does not require hard coding

        // acquisition core values
        acquisitionObject.setAccessiondate((String)docModel.getPropertyValue(
                getQProperty(AcquisitionJAXBSchema.ACCESSIONDATE)));

        return acquisitionObject;
    }

    @Override
    public void fillCommonObject(Acquisition acquisitionObject, DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding

        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", AcquisitionConstants.NUXEO_DC_TITLE);

        // acquisition core values
        if(acquisitionObject.getAccessiondate() != null){
            docModel.setPropertyValue(getQProperty(
                    AcquisitionJAXBSchema.ACCESSIONDATE), acquisitionObject.getAccessiondate());
        }
    }

    @Override
    public AcquisitionList extractCommonObjectList(DocumentWrapper wrapDoc) throws Exception {
        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();

        AcquisitionList coList = new AcquisitionList();
        List<AcquisitionList.AcquisitionListItem> list = coList.getAcquisitionListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            AcquisitionListItem listItem = new AcquisitionListItem();
            listItem.setAccessiondate((String)docModel.getPropertyValue(
                    getQProperty(AcquisitionJAXBSchema.ACCESSIONDATE)));
            //need fully qualified context for URI
            String id = docModel.getId();
            listItem.setUri("/acquisitions/" + id);
            listItem.setCsid(id);
            list.add(listItem);
        }

        return coList;
    }

    @Override
    public void fillCommonObjectList(AcquisitionList obj, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getDocumentType()
     */
    public String getDocumentType() {
    	return AcquisitionConstants.NUXEO_DOCTYPE;
    }
    
    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    private String getQProperty(String prop) {
        return AcquisitionConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

