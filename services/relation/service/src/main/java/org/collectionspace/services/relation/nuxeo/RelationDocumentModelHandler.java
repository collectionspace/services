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
import java.util.Map;


import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.common.relation.nuxeo.RelationsUtils;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;

import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
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
     * @return
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
    public void setCommonPart(RelationsCommon relation) {
        this.relation = relation;
    }

    /**
     * getRelationList get associated Relation (for index/GET_ALL)
     * @return
     */
    @Override
    public RelationsCommonList getCommonPartList() {
        return relationList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(RelationsCommonList relationList) {
        this.relationList = relationList;
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
    public void fillCommonPart(RelationsCommon relation, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public RelationsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        Map propsFromResource = this.getProperties();
        String subjectCsid = (String) propsFromResource.get(IRelationsManager.SUBJECT);
        String predicate = (String) propsFromResource.get(IRelationsManager.PREDICATE);
        String objectCsid = (String) propsFromResource.get(IRelationsManager.OBJECT);

        //FIXME - Need to change this into a NXQL on subject, predicate, object terms.  Currently,
        //FIXME - we're performing a post query filter which is far from ideal and not scalable.
        RelationsCommonList relList = this.extractPagingInfo(new RelationsCommonList(), wrapDoc) ;
        List<RelationsCommonList.RelationListItem> itemList = relList.getRelationListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            if (RelationsUtils.isQueryMatch(docModel, subjectCsid,
                    predicate, objectCsid) == true){
                RelationListItem relListItem = RelationsUtils.getRelationListItem(getServiceContext(),
                        docModel, getServiceContextPath());
                itemList.add(relListItem);
            }
        }
        return relList;
    }

  

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#fillAllParts(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.fillAllParts(wrapDoc);
        fillDublinCoreObject(wrapDoc); //dublincore might not be needed in future
    }

    /**
     * Fill dublin core object.
     *
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    private void fillDublinCoreObject(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding
        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", RelationConstants.NUXEO_DC_TITLE);
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(String prop) {
        return "/" + RelationConstants.NUXEO_SCHEMA_ROOT_ELEMENT + "/" + prop;
    }
}

