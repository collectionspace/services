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
import java.util.Map;

import org.collectionspace.services.IntakeJAXBSchema;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.intake.IntakesCommon;
import org.collectionspace.services.intake.IntakesCommonList;
import org.collectionspace.services.intake.IntakesCommonList.IntakeListItem;
import org.collectionspace.services.nuxeo.client.rest.RepresentationHandler;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntakeDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class IntakeRepresenationHandler
        extends RepresentationHandler<IntakesCommon, IntakesCommonList> {

    private final Logger logger = LoggerFactory.getLogger(IntakeRepresenationHandler.class);
    /**
     * intakeObj is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private IntakesCommon intake;
    /**
     * intakeListObject is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private IntakesCommonList intakeList;

    @Override
    public void prepare(Action action) throws Exception {
        switch(action){
            case CREATE:
            case UPDATE:
                prepare();
        }
    }

    private void prepare() {
        Map<String, String> queryParams = getQueryParams();
        IntakesCommon intakeObject = getCommonPart();
        if(intakeObject.getCurrentOwner() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.CURRENT_OWNER, intakeObject.getCurrentOwner());
        }

        if(intakeObject.getDepositor() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.DEPOSITOR, intakeObject.getDepositor());
        }

        if(intakeObject.getDepositorsRequirements() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS, intakeObject.getDepositorsRequirements());
        }

        if(intakeObject.getEntryDate() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.ENTRY_DATE, intakeObject.getEntryDate());
        }

        if(intakeObject.getEntryMethod() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.ENTRY_METHOD, intakeObject.getEntryMethod());
        }

        if(intakeObject.getEntryNote() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.ENTRY_NOTE, intakeObject.getEntryNote());
        }

        if(intakeObject.getEntryNumber() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.ENTRY_NUMBER, intakeObject.getEntryNumber());
        }

        if(intakeObject.getEntryReason() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.ENTRY_REASON, intakeObject.getEntryReason());
        }

        if(intakeObject.getPackingNote() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.PACKING_NOTE, intakeObject.getPackingNote());
        }

        if(intakeObject.getReturnDate() != null){
            queryParams.put(IntakeConstants.NUXEO_SCHEMA_NAME + ":" +
                    IntakeJAXBSchema.RETURN_DATE, intakeObject.getReturnDate());
        }
    }

    @Override
    public IntakesCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        Document document = (Document) wrapDoc.getWrappedObject();
        IntakesCommon intakeObj = new IntakesCommon();

        //FIXME property get should be dynamically set using schema inspection
        //so it does not require hard coding
        Element root = document.getRootElement();

        // TODO: recognize schema thru namespace uri
        // Namespace ns = new Namespace("intakeObj",
        // "http://collectionspace.org/intakeObj");

        Iterator<Element> siter = root.elementIterator("schema");
        while(siter.hasNext()){

            Element schemaElement = siter.next();
            if(logger.isDebugEnabled()){
                logger.debug("getCommonObject() populating Common Object");
            }
            // TODO: recognize schema thru namespace uri
            if(IntakeConstants.NUXEO_SCHEMA_NAME.equals(schemaElement.attribute("name").getValue())){
                Element ele = schemaElement.element(IntakeJAXBSchema.CURRENT_OWNER);
                if(ele != null){
                    intakeObj.setCurrentOwner((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.DEPOSITOR);
                if(ele != null){
                    intakeObj.setDepositor((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.DEPOSITORS_REQUIREMENTS);
                if(ele != null){
                    intakeObj.setDepositorsRequirements((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.ENTRY_DATE);
                if(ele != null){
                    intakeObj.setEntryDate((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.ENTRY_METHOD);
                if(ele != null){
                    intakeObj.setEntryMethod((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.ENTRY_NOTE);
                if(ele != null){
                    intakeObj.setEntryNote((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.ENTRY_NUMBER);
                if(ele != null){
                    intakeObj.setEntryNumber((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.ENTRY_REASON);
                if(ele != null){
                    intakeObj.setEntryReason((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.PACKING_NOTE);
                if(ele != null){
                    intakeObj.setPackingNote((String) ele.getData());
                }
                ele = schemaElement.element(IntakeJAXBSchema.RETURN_DATE);
                if(ele != null){
                    intakeObj.setReturnDate((String) ele.getData());
                }
            }
        }
        return intakeObj;
    }

    @Override
    public void fillCommonPart(IntakesCommon co, DocumentWrapper wrapDoc)
            throws Exception {
        //Nuxeo REST takes create/update through queryParams, nothing to do here
    }

    @Override
    public IntakesCommonList extractCommonPartList(DocumentWrapper wrapDoc) throws Exception {
        Document document = (Document) wrapDoc.getWrappedObject();
        if(logger.isDebugEnabled()){
            logger.debug(document.asXML());
        }
        IntakesCommonList intakeListObject = new IntakesCommonList();
        List<IntakesCommonList.IntakeListItem> list = intakeListObject.getIntakeListItem();
        Element root = document.getRootElement();
        for(Iterator i = root.elementIterator(); i.hasNext();){

            Element element = (Element) i.next();
            if(logger.isDebugEnabled()){
                logger.debug(element.asXML());
            }
            // set the intakeObj list item entity elements
            IntakeListItem ilistItem = new IntakeListItem();
            ilistItem.setEntryNumber(element.attributeValue("entryNumber"));
            String id = element.attributeValue("id");
            ilistItem.setCsid(id);
            ilistItem.setUri("/intakes/" + id);
            list.add(ilistItem);
        }
        return intakeListObject;
    }


    @Override
    public IntakesCommon getCommonPart() {
        return intake;
    }

    @Override
    public void setCommonPart(IntakesCommon obj) {
        this.intake = obj;
    }

    @Override
    public IntakesCommonList getCommonPartList() {
        return intakeList;
    }

    @Override
    public void setCommonPartList(IntakesCommonList obj) {
        this.intakeList = obj;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getDocumentType()
     */
    @Override
    public String getDocumentType() {
        return IntakeConstants.NUXEO_DOCTYPE;
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

