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
package org.collectionspace.services.common.relation.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.common.repository.DocumentException;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RelationsUtils
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RelationsUtils {

    private static final Logger logger = LoggerFactory.getLogger(RelationsUtils.class);

    public static RelationsCommonList extractCommonPartList(DocumentWrapper wrapDoc,
            String serviceContextPath)
            throws Exception {
        DocumentModelList docList = (DocumentModelList) wrapDoc.getWrappedObject();

        RelationsCommonList relList = new RelationsCommonList();
        List<RelationsCommonList.RelationListItem> list = relList.getRelationListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            RelationListItem relationListItem = getRelationListItem(docModel,
                    serviceContextPath);
            list.add(relationListItem);
        }
        return relList;
    }

    public static RelationListItem getRelationListItem(DocumentModel docModel,
            String serviceContextPath) throws Exception {
        RelationListItem relationListItem = new RelationListItem();
        relationListItem.setUri(serviceContextPath + docModel.getId());
        relationListItem.setCsid(docModel.getId());
        return relationListItem;
    }

    public static void fillDublinCoreObject(DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        //FIXME property setter should be dynamically set using schema inspection
        //so it does not require hard coding
        // a default title for the Dublin Core schema
        docModel.setPropertyValue("dublincore:title", RelationConstants.NUXEO_DC_TITLE);
    }

    /**
     * Checks if is subject of relation.
     *
     * @param csid the csid
     * @param documentModel the document model
     *
     * @return true, if is subject of relation
     *
     * @throws Exception 
     */
    private static boolean isSubjectOfRelation(String csid, DocumentModel documentModel)
            throws Exception {
        boolean result = false;
        Object valueObject = documentModel.getProperty(RelationConstants.NUXEO_SCHEMA_NAME, RelationJAXBSchema.DOCUMENT_ID_1);
        if(valueObject != null && csid != null){
            String subjectID = (String) valueObject;
            result = subjectID.equals(csid);
        }

        return result;
    }

    /**
     * Checks if is object of relation.
     *
     * @param csid the csid
     * @param documentModel the document model
     *
     * @return true, if is object of relation
     *
     * @throws Exception 
     */
    private static boolean isObjectOfRelation(String csid, DocumentModel documentModel)
            throws Exception {
        boolean result = false;

        Object valueObject = documentModel.getProperty(RelationConstants.NUXEO_SCHEMA_NAME,
                RelationJAXBSchema.DOCUMENT_ID_2);
        if(valueObject != null && csid != null){
            String subjectID = (String) valueObject;
            result = subjectID.equals(csid);
        }

        return result;
    }

    /**
     * Checks if is predicate of relation.
     *
     * @param predicate the predicate
     * @param documentModel the document model
     *
     * @return true, if is predicate of relation
     *
     * @throws Exception 
     */
    private static boolean isPredicateOfRelation(String predicate,
            DocumentModel documentModel) throws Exception {
        boolean result = false;

        Object valueObject = documentModel.getProperty(RelationConstants.NUXEO_SCHEMA_NAME,
                RelationJAXBSchema.RELATIONSHIP_TYPE);
        if(valueObject != null && predicate != null){
            String relationType = (String) valueObject;
            result = predicate.equalsIgnoreCase(relationType);
        }

        return result;
    }

    /**
     * Gets the object from subject.
     *
     * @param csid the csid
     * @param documentModel the document model
     *
     * @return the object from subject
     *
     * @throws Exception 
     */
    private static String getObjectFromSubject(String csid, DocumentModel documentModel)
            throws Exception {
        String result = null;

        Object valueObject = documentModel.getProperty(RelationConstants.NUXEO_SCHEMA_NAME,
                RelationJAXBSchema.DOCUMENT_ID_1);
        if(valueObject != null){
            String subjectID = (String) valueObject;
            if(subjectID.equals(csid) == true){
                valueObject = documentModel.getProperty(RelationConstants.NUXEO_SCHEMA_NAME,
                        RelationJAXBSchema.DOCUMENT_ID_2);
                if(valueObject != null){
                    result = (String) valueObject;
                }
            }
        }

        return result;
    }

    /**
     * Checks if is query match.
     *
     * @param documentModel the document model
     * @param subjectCsid the subject csid
     * @param predicate the predicate
     * @param objectCsid the object csid
     *
     * @return true, if is query match
     *
     * @throws ClientException the client exception
     */
    public static boolean isQueryMatch(DocumentModel documentModel,
            String subjectCsid,
            String predicate,
            String objectCsid) throws DocumentException {
        boolean result = true;

        try{
            block:
            {
                if(subjectCsid != null){
                    if(isSubjectOfRelation(subjectCsid, documentModel) == false){
                        result = false;
                        break block;
                    }
                }
                if(predicate != null){
                    if(isPredicateOfRelation(predicate, documentModel) == false){
                        result = false;
                        break block;
                    }
                }
                if(objectCsid != null){
                    if(isObjectOfRelation(objectCsid, documentModel) == false){
                        result = false;
                        break block;
                    }
                }
            }
        }catch(Exception e){
            if(logger.isDebugEnabled() == true){
                e.printStackTrace();
            }
            throw new DocumentException(e);
        }

        return result;
    }

    /**
     * Gets the rel url.
     *
     * @param repo the repo
     * @param uuid the uuid
     *
     * @return the rel url
     */
    private static String getRelURL(String repo, String uuid) {
        return '/' + repo + '/' + uuid;
    }
}

