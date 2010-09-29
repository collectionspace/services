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
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;

import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
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
        extends RemoteDocumentModelHandlerImpl<RelationsCommon, RelationsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(RelationDocumentModelHandler.class);
    /**
     * relation is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private RelationsCommon relation;
    /**
     * relationList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private RelationsCommonList relationList;


    /**
     * getCommonObject get associated Relation
     * @return relation
     */
    @Override
    public RelationsCommon getCommonPart() {
        return relation;
    }

    /**
     * setCommonObject set associated relation
     * @param relation
     */
    @Override
    public void setCommonPart(RelationsCommon theRelation) {
        this.relation = theRelation;
    }

    /**
     * getRelationList get associated Relation (for index/GET_ALL)
     * @return relationCommonList
     */
    @Override
    public RelationsCommonList getCommonPartList() {
        return relationList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(RelationsCommonList theRelationList) {
        this.relationList = theRelationList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public RelationsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(RelationsCommon theRelation, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public RelationsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        RelationsCommonList relList = this.extractPagingInfo(new RelationsCommonList(), wrapDoc) ;
        AbstractCommonList commonList = (AbstractCommonList) relList;
        commonList.setFieldsReturned("subjectCsid|relationshipType|predicateDisplayName|objectCsid|uri|csid");
        List<RelationsCommonList.RelationListItem> itemList = relList.getRelationListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            RelationListItem relListItem = getRelationListItem(getServiceContext(),
                    docModel, getServiceContextPath());
            itemList.add(relListItem);
        }
        return relList;
    }

  

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#fillAllParts(org.collectionspace.services.common.document.DocumentWrapper)
    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.fillAllParts(wrapDoc);
    }
     */

    /**
     * Gets the relation list item.
     *
     * @param ctx the ctx
     * @param docModel the doc model
     * @param serviceContextPath the service context path
     * @return the relation list item
     * @throws Exception the exception
     */
    private RelationListItem getRelationListItem(ServiceContext<MultipartInput, MultipartOutput> ctx,
    		DocumentModel docModel,
            String serviceContextPath) throws Exception {
        RelationListItem relationListItem = new RelationListItem();
        String id = NuxeoUtils.extractId(docModel.getPathAsString());
        relationListItem.setCsid(id);
        //
        // Subject
        //
        relationListItem.setSubjectCsid((String) docModel.getProperty(ctx.getCommonPartLabel(),
        		RelationJAXBSchema.DOCUMENT_ID_1));
        //
        // Predicate
        //
        relationListItem.setRelationshipType((String) docModel.getProperty(ctx.getCommonPartLabel(),
        		RelationJAXBSchema.RELATIONSHIP_TYPE));
        relationListItem.setPredicateDisplayName((String) docModel.getProperty(ctx.getCommonPartLabel(),
        		RelationJAXBSchema.RELATIONSHIP_TYPE_DISPLAYNAME));
        //
        // Object
        //
        relationListItem.setObjectCsid((String) docModel.getProperty(ctx.getCommonPartLabel(),
        		RelationJAXBSchema.DOCUMENT_ID_2));
        
        relationListItem.setUri(serviceContextPath + id);
        return relationListItem;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(String prop) {
        return "/" + RelationConstants.NUXEO_SCHEMA_ROOT_ELEMENT + "/" + prop;
    }
}

