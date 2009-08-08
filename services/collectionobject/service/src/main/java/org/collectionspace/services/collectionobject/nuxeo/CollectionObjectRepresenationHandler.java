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
import java.util.Map;

import org.collectionspace.services.CollectionObjectJAXBSchema;
import org.collectionspace.services.collectionobject.CollectionObject;
import org.collectionspace.services.collectionobject.CollectionObjectList;
import org.collectionspace.services.collectionobject.CollectionObjectList.CollectionObjectListItem;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.rest.RepresentationHandler;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionObjectDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class CollectionObjectRepresenationHandler
        extends RepresentationHandler<CollectionObject, CollectionObjectList>
{

    private final Logger logger = LoggerFactory.getLogger(CollectionObjectRepresenationHandler.class);
    /**
     * collectionObject is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private CollectionObject collectionObject;
    /**
     * collectionObjectList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private CollectionObjectList collectionObjectList;

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
        CollectionObject co = getCommonObject();
        // todo: intelligent merge needed
        if(co.getObjectNumber() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.OBJECT_NUMBER, co.getObjectNumber());
        }

        if(co.getOtherNumber() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.OTHER_NUMBER, co.getOtherNumber());
        }

        if(co.getBriefDescription() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.BRIEF_DESCRIPTION, co.getBriefDescription());
        }

        if(co.getComments() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.COMMENTS, co.getComments());
        }

        if(co.getDistFeatures() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.DIST_FEATURES, co.getDistFeatures());
        }

        if(co.getObjectName() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.OBJECT_NAME, co.getObjectName());
        }

        if(co.getResponsibleDept() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.RESPONSIBLE_DEPT, co.getResponsibleDept());
        }

        if(co.getTitle() != null){
            queryParams.put(CollectionObjectConstants.NUXEO_SCHEMA_NAME +
                    ":" + CollectionObjectJAXBSchema.TITLE, co.getTitle());
        }
    }

    @Override
    public CollectionObject extractCommonObject(DocumentWrapper wrapDoc)
            throws Exception {
        Document document = (Document) wrapDoc.getWrappedObject();
        CollectionObject co = new CollectionObject();

        //FIXME property get should be dynamically set using schema inspection
        //so it does not require hard coding
        Element root = document.getRootElement();

        // TODO: recognize schema thru namespace uri
        // Namespace ns = new Namespace("collectionobject",
        // "http://collectionspace.org/collectionobject");

        Iterator<Element> siter = root.elementIterator("schema");
        while(siter.hasNext()){

            Element schemaElement = siter.next();
            if(logger.isDebugEnabled()){
                logger.debug("getCommonObject() populating Common Object");
            }
            // TODO: recognize schema thru namespace uri
            if(CollectionObjectConstants.NUXEO_SCHEMA_NAME.equals(schemaElement.attribute("name").getValue())){
                Element ele = schemaElement.element(CollectionObjectJAXBSchema.OBJECT_NUMBER);
                if(ele != null){
                    co.setObjectNumber((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.OTHER_NUMBER);
                if(ele != null){
                    co.setOtherNumber((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.BRIEF_DESCRIPTION);
                if(ele != null){
                    co.setBriefDescription((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.COMMENTS);
                if(ele != null){
                    co.setComments((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.DIST_FEATURES);
                if(ele != null){
                    co.setDistFeatures((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.OBJECT_NAME);
                if(ele != null){
                    co.setObjectName((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.RESPONSIBLE_DEPT);
                if(ele != null){
                    co.setResponsibleDept((String) ele.getData());
                }
                ele = schemaElement.element(CollectionObjectJAXBSchema.TITLE);
                if(ele != null){
                    co.setTitle((String) ele.getData());
                }
            }
        }
        return co;
    }

    @Override
    public void fillCommonObject(CollectionObject co, DocumentWrapper wrapDoc)
            throws Exception {
        //Nuxeo REST takes create/update through queryParams, nothing to do here
    }

    @Override
    public CollectionObjectList extractCommonObjectList(DocumentWrapper wrapDoc) throws Exception {
        Document document = (Document) wrapDoc.getWrappedObject();
        // debug
        if(logger.isDebugEnabled()){
            logger.debug(document.asXML());
        }
        CollectionObjectList coList = new CollectionObjectList();
        List<CollectionObjectList.CollectionObjectListItem> list = coList.getCollectionObjectListItem();
        Element root = document.getRootElement();
        for(Iterator i = root.elementIterator(); i.hasNext();){

            Element element = (Element) i.next();
            if(logger.isDebugEnabled()){
                logger.debug(element.asXML());
            }
            // set the CollectionObject list item entity elements
            CollectionObjectListItem coListItem = new CollectionObjectListItem();
            coListItem.setObjectNumber(element.attributeValue("title"));
            String id = element.attributeValue("id");
            coListItem.setCsid(id);
            coListItem.setUri("/collectionobjects/" + id);

            list.add(coListItem);
        }
        return coList;
    }

    @Override
    public void fillCommonObjectList(CollectionObjectList obj, DocumentWrapper wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public CollectionObject getCommonObject() {
        return collectionObject;
    }

    @Override
    public void setCommonObject(CollectionObject obj) {
        this.collectionObject = obj;
    }

    @Override
    public CollectionObjectList getCommonObjectList() {
        return collectionObjectList;
    }

    @Override
    public void setCommonObjectList(CollectionObjectList obj) {
        this.collectionObjectList = obj;
    }
    
    public String getDocumentType() {
    	return CollectionObjectConstants.NUXEO_DOCTYPE;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    private String getQProperty(String prop) {
        return CollectionObjectConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

