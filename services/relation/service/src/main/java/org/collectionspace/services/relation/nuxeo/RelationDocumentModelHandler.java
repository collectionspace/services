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
package org.collectionspace.services.relation.nuxeo;

import java.util.Iterator;
import java.util.List;
import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.common.relation.nuxeo.RelationUtilsNuxeoImpl;
import org.collectionspace.services.common.relation.RelationsManager;

import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;
import org.collectionspace.services.relation.RelationList.RelationListItem;

import org.collectionspace.services.relation.nuxeo.RelationNuxeoConstants;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RelationDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RelationDocumentModelHandler
        extends DocumentModelHandler<Relation, RelationList> {

    private final Logger logger = LoggerFactory.getLogger(RelationDocumentModelHandler.class);
    /**
     * relation is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private Relation relation;
    /**
     * relationList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private RelationList relationList;

    @Override
    public void prepare(Action action) throws Exception {
        //no specific action needed
    }


    /**
     * getCommonObject get associated Relation
     * @return
     */
    @Override
    public Relation getCommonObject() {
        return relation;
    }

    /**
     * setCommonObject set associated relation
     * @param relation
     */
    @Override
    public void setCommonObject(Relation relation) {
        this.relation = relation;
    }

    /**
     * getRelationList get associated Relation (for index/GET_ALL)
     * @return
     */
    @Override
    public RelationList getCommonObjectList() {
        return relationList;
    }

    @Override
    public void setCommonObjectList(RelationList relationList) {
        this.relationList = relationList;
    }

    @Override
    public Relation extractCommonObject(DocumentWrapper wrapDoc)
            throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        Relation theRelation = new Relation();
        
        RelationUtilsNuxeoImpl.fillRelationFromDocModel(theRelation, docModel);

        return theRelation;
    }

    @Override
    public void fillCommonObject(Relation relation, DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();

        RelationUtilsNuxeoImpl.fillDocModelFromRelation(relation, docModel);
    }

    @Override
    public RelationList extractCommonObjectList(DocumentWrapper wrapDoc) throws Exception {
        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();

        RelationList coList = new RelationList();
        List<RelationList.RelationListItem> list = coList.getRelationListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            RelationListItem coListItem = new RelationListItem();
            
            RelationUtilsNuxeoImpl.fillRelationListItemFromDocModel(coListItem, docModel);
            
            list.add(coListItem);
        }

        return coList;
    }
    
    public String getDocumentType() {
    	return RelationNuxeoConstants.NUXEO_DOCTYPE;
    }

    @Override
    public void fillCommonObjectList(RelationList obj, DocumentWrapper wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    private String getQProperty(String property) {
    	return RelationsManager.getQPropertyName(property);
    }
}

