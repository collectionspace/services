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

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;

import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
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

    @Override
    public RelationsCommon getCommonPart() {
        return relation;
    }

    @Override
    public void setCommonPart(RelationsCommon theRelation) {
        this.relation = theRelation;
    }

    /**get associated Relation (for index/GET_ALL)
     */
    @Override
    public RelationsCommonList getCommonPartList() {
        return relationList;
    }

    @Override
    public void setCommonPartList(RelationsCommonList theRelationList) {
        this.relationList = theRelationList;
    }

    @Override
    public RelationsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(RelationsCommon theRelation, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public RelationsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        RelationsCommonList relList = this.extractPagingInfo(new RelationsCommonList(), wrapDoc) ;
        relList.setFieldsReturned("subjectCsid|relationshipType|predicateDisplayName|objectCsid|uri|csid|subject|object");
        ServiceContext ctx = getServiceContext();
        String serviceContextPath = getServiceContextPath();

        TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        String serviceName = getServiceContext().getServiceName().toLowerCase();
        ServiceBindingType sbt = tReader.getServiceBinding(ctx.getTenantId(), serviceName);

        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            RelationListItem relListItem = getRelationListItem(ctx, sbt, tReader, docModel, serviceContextPath);
            relList.getRelationListItem().add(relListItem);
        }
        return relList;
    }

    /** Gets the relation list item, looking up the subject and object documents, and getting summary
     *  info via the objectName and objectNumber properties in tenant-bindings.
     * @param ctx the ctx
     * @param sbt the ServiceBindingType of Relations service
     * @param tReader the tenant-bindings reader, for looking up docnumber and docname
     * @param docModel the doc model
     * @param serviceContextPath the service context path
     * @return the relation list item, with nested subject and object summary info.
     * @throws Exception the exception
     */
    private RelationListItem getRelationListItem(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
                                                                        ServiceBindingType sbt,
                                                                        TenantBindingConfigReaderImpl tReader,
                                                                        DocumentModel docModel,
                                                                        String serviceContextPath) throws Exception {
        RelationListItem relationListItem = new RelationListItem();
        String id = getCsid(docModel);
        relationListItem.setCsid(id);

        relationListItem.setSubjectCsid((String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.DOCUMENT_ID_1));

        String predicate = (String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.RELATIONSHIP_TYPE);
        relationListItem.setRelationshipType(predicate);
        relationListItem.setPredicate(predicate); //predicate is new name for relationshipType.
        relationListItem.setPredicateDisplayName((String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.RELATIONSHIP_TYPE_DISPLAYNAME));

        relationListItem.setObjectCsid((String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.DOCUMENT_ID_2));
        
        relationListItem.setUri(serviceContextPath + id);

        //Now fill in summary info for the related docs: subject and object.
        String subjectCsid = relationListItem.getSubjectCsid();
        String documentType = (String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.DOCUMENT_TYPE_1);
        RelationsDocListItem subject = createRelationsDocListItem(ctx, sbt, subjectCsid, tReader, documentType);

        //Object o1 =  docModel.getProperty(ctx.getCommonPartLabel(), "subject");
        //Object o2 =  docModel.getProperty(ctx.getCommonPartLabel(), "object");

        String subjectUri = (String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.subjectUri);
        subject.setUri(subjectUri);
        relationListItem.setSubject(subject);

        String objectCsid = relationListItem.getObjectCsid();
        documentType = (String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.DOCUMENT_TYPE_2);
        RelationsDocListItem object = createRelationsDocListItem(ctx, sbt, objectCsid, tReader, documentType);

        String objectUri = (String) docModel.getProperty(ctx.getCommonPartLabel(), RelationJAXBSchema.objectUri);
        object.setUri(objectUri);
        relationListItem.setObject(object);

        return relationListItem;
    }

     // DocumentModel itemDocModel = docModelFromCSID(ctx, itemCsid);

    protected RelationsDocListItem createRelationsDocListItem(ServiceContext  ctx, 
                                                                                                 ServiceBindingType sbt,
                                                                                                 String itemCsid,
                                                                                                 TenantBindingConfigReaderImpl tReader,
                                                                                                 String documentType) throws Exception {
        RelationsDocListItem item = new RelationsDocListItem();
        item.setDocumentType(documentType);//this one comes from the record, as documentType1, documentType2.
        item.setService(documentType);//this one comes from the record, as documentType1, documentType2.   Current app seems to use servicename for this.
        item.setCsid(itemCsid);

        DocumentModel itemDocModel =  NuxeoUtils.getDocFromCsid(getRepositorySession(), ctx, itemCsid);    //null if not found.
        if (itemDocModel!=null){
            String itemDocType = itemDocModel.getDocumentType().getName();
            item.setDocumentTypeFromModel(itemDocType);           //this one comes from the nuxeo documentType

            //TODO: ensure that itemDocType is really the entry point, i.e. servicename==doctype
            //ServiceBindingType itemSbt2 = tReader.getServiceBinding(ctx.getTenantId(), itemDocType);
            ServiceBindingType itemSbt = tReader.getServiceBindingForDocType(ctx.getTenantId(), itemDocType);
            try {
                String itemDocname = ServiceBindingUtils.getMappedFieldInDoc(itemSbt, ServiceBindingUtils.OBJ_NAME_PROP, itemDocModel);
                item.setName(itemDocname);
                //System.out.println("\r\n\r\n\r\n=================\r\n~~found prop : "+ServiceBindingUtils.OBJ_NAME_PROP+" in :"+itemDocname);
            } catch (Throwable t){
                 System.out.println("\r\n\r\n\r\n=================\r\n NOTE: "+itemDocModel+" field "+ServiceBindingUtils.OBJ_NAME_PROP+" not found in DocModel: "+itemDocModel.getName()+" inner: "+t.getMessage());
            }
            try {
                String itemDocnumber = ServiceBindingUtils.getMappedFieldInDoc(itemSbt, ServiceBindingUtils.OBJ_NUMBER_PROP, itemDocModel);
                item.setNumber(itemDocnumber);
                //System.out.println("\r\n\r\n\r\n=================\r\n~~found prop : "+ServiceBindingUtils.OBJ_NUMBER_PROP+" in :"+itemDocnumber);
            } catch (Throwable t){
                System.out.println("\r\n\r\n\r\n=================\r\n NOTE:  field "+ServiceBindingUtils.OBJ_NUMBER_PROP+" not found in DocModel: "+itemDocModel.getName()+" inner: "+t.getMessage());
            }
        } else {
            item.setError("INVALID: related object is absent");
        }
        return item;
    }

    @Override
    public String getQProperty(String prop) {
        return "/" + RelationConstants.NUXEO_SCHEMA_ROOT_ELEMENT + "/" + prop;
    }
}

