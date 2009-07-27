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
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.intake.IntakeList;
import org.collectionspace.services.intake.IntakeList.IntakeListItem;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
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
        extends DocumentModelHandler<Intake, IntakeList> {

    private final Logger logger = LoggerFactory.getLogger(IntakeDocumentModelHandler.class);
    /**
     * intake is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private Intake intake;
    /**
     * intakeList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private IntakeList intakeList;

    @Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }

    /**
     * getCommonObject get associated intake
     * @return
     */
    @Override
    public Intake getCommonObject() {
        return intake;
    }

    /**
     * setCommonObject set associated intake
     * @param intake
     */
    @Override
    public void setCommonObject(Intake intake) {
        this.intake = intake;
    }

    /**
     * getIntakeList get associated intake (for index/GET_ALL)
     * @return
     */
    @Override
    public IntakeList getCommonObjectList() {
        return intakeList;
    }

    @Override
    public void setCommonObjectList(IntakeList intakeList) {
        this.intakeList = intakeList;
    }

    @Override
    public Intake extractCommonObject(DocumentWrapper wrapDoc)
            throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        Intake intakeObject = new Intake();

        //FIXME property get should be dynamically set using schema inspection
        //so it does not require hard coding

        // intake core values
        intakeObject.setCurrentOwner((String)docModel.getPropertyValue(
                getQProperty(IntakeJAXBSchema.CURRENT_OWNER)));

        intakeObject.setDepositor((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.DEPOSITOR)));

        intakeObject.setDepositorsRequirements((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS)));

        intakeObject.setEntryDate((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.ENTRY_DATE)));

        intakeObject.setEntryMethod((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.ENTRY_METHOD)));

        intakeObject.setEntryNote((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.ENTRY_NOTE)));

        intakeObject.setEntryNumber((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.ENTRY_NUMBER)));

        intakeObject.setEntryReason((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.ENTRY_REASON)));

        intakeObject.setPackingNote((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.PACKING_NOTE)));

        intakeObject.setReturnDate((String)docModel.getPropertyValue(getQProperty(
                IntakeJAXBSchema.RETURN_DATE)));

        return intakeObject;
    }

    @Override
    public void fillCommonObject(Intake intakeObject, DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding

        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", IntakeConstants.INTAKE_NUXEO_DC_TITLE);

        // intake core values
        if(intakeObject.getCurrentOwner() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.CURRENT_OWNER), intakeObject.getCurrentOwner());
        }

        if(intakeObject.getDepositor() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.DEPOSITOR), intakeObject.getDepositor());
        }

        if(intakeObject.getDepositorsRequirements() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS), intakeObject.getDepositorsRequirements());
        }

        if(intakeObject.getEntryDate() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.ENTRY_DATE), intakeObject.getEntryDate());
        }

        if(intakeObject.getEntryMethod() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.ENTRY_METHOD), intakeObject.getEntryMethod());
        }

        if(intakeObject.getEntryNote() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.ENTRY_NOTE), intakeObject.getEntryNote());
        }

        if(intakeObject.getEntryNumber() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.ENTRY_NUMBER), intakeObject.getEntryNumber());
        }

        if(intakeObject.getEntryReason() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.ENTRY_REASON), intakeObject.getEntryReason());
        }

        if(intakeObject.getPackingNote() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.PACKING_NOTE), intakeObject.getPackingNote());
        }

        if(intakeObject.getReturnDate() != null){
            docModel.setPropertyValue(getQProperty(
                    IntakeJAXBSchema.RETURN_DATE), intakeObject.getReturnDate());
        }

    }

    @Override
    public IntakeList extractCommonObjectList(DocumentWrapper wrapDoc) throws Exception {
        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();

        IntakeList coList = new IntakeList();
        List<IntakeList.IntakeListItem> list = coList.getIntakeListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            IntakeListItem ilistItem = new IntakeListItem();
            ilistItem.setEntryNumber((String)docModel.getPropertyValue(
                    getQProperty(IntakeJAXBSchema.ENTRY_NUMBER)));
            //need fully qualified context for URI
            String id = docModel.getId();
            ilistItem.setUri("/intakes/" + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
        }

        return coList;
    }

    @Override
    public void fillCommonObjectList(IntakeList obj, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    private String getQProperty(String prop) {
        return IntakeConstants.INTAKE_NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

