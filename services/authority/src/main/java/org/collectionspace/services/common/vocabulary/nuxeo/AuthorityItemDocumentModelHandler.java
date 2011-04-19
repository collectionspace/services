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
package org.collectionspace.services.common.vocabulary.nuxeo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
//import org.collectionspace.services.common.authority.AuthorityItemRelations;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.collectionspace.services.relation.RelationshipType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * AuthorityItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityItemDocumentModelHandler<AICommon, AICommonList>
        extends RemoteDocumentModelHandlerImpl<AICommon, AICommonList> {

    private final Logger logger = LoggerFactory.getLogger(AuthorityItemDocumentModelHandler.class);

	private String authorityItemCommonSchemaName;
	
    //private final Logger logger = LoggerFactory.getLogger(AuthorityItemDocumentModelHandler.class);
    /**
     * item is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    protected AICommon item;
    /**
     * itemList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    protected AICommonList itemList;
    
    /**
     * inVocabulary is the parent Authority for this context
     */
    protected String inAuthority;
    
    public AuthorityItemDocumentModelHandler(String authorityItemCommonSchemaName) {
    	this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }

    public String getInAuthority() {
		return inAuthority;
	}

	public void setInAuthority(String inAuthority) {
		this.inAuthority = inAuthority;
	}

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// first fill all the parts of the document
    	super.handleCreate(wrapDoc);    	
    	handleInAuthority(wrapDoc.getWrappedObject());
    }
    
    /**
     * Check the logic around the parent pointer. Note that we only need do this on
     * create, since we have logic to make this read-only on update. 
     * 
     * @param docModel
     * 
     * @throws Exception the exception
     */
    private void handleInAuthority(DocumentModel docModel) throws Exception {
    	docModel.setProperty(authorityItemCommonSchemaName, 
    			AuthorityItemJAXBSchema.IN_AUTHORITY, inAuthority);
    }


    /**
     * getCommonPart get associated item
     * @return
     */
    @Override
    public AICommon getCommonPart() {
        return item;
    }

    @Override
    public void setCommonPart(AICommon item) {
        this.item = item;
    }

    /**
     * getCommonPartList get associated item (for index/GET_ALL)
     * @return
     */
    @Override
    public AICommonList getCommonPartList() {
        return itemList;
    }

    @Override
    public void setCommonPartList(AICommonList itemList) {
        this.itemList = itemList;
    }

    @Override
    public AICommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(AICommon itemObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);
    	
    	// Add the CSID to the common part
    	if (partMeta.getLabel().equalsIgnoreCase(authorityItemCommonSchemaName)) {
	    	String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
	    	unQObjectProperties.put("csid", csid);
    	}
    	
    	return unQObjectProperties;
    }
    
    /**
     * Filters out AuthorityItemJAXBSchema.IN_AUTHORITY, to ensure that
     * the parent link remains untouched.
     * @param objectProps the properties parsed from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
    		Map<String, Object> objectProps, ObjectPartType partMeta) {
    	super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
    	objectProps.remove(AuthorityItemJAXBSchema.IN_AUTHORITY);
    	objectProps.remove(AuthorityItemJAXBSchema.CSID);
    }

     @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        super.extractAllParts(wrapDoc);

        String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());

        //TODO: add broader, etc. here.
        String predicate = RelationshipType.HAS_BROADER.value();
        RelationsCommonList relationsCommonList = getRelations(thisCSID, null, predicate);
        if (relationsCommonList.getTotalItems() == 0){
            relationsCommonList = getRelations(null, thisCSID, predicate);   //for development... try switching subject and object.  This is not correct, though.
        }
        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, relationsCommonList);
        ctx.addOutputPart(relationsPart);
    }

    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        ServiceContext ctx = getServiceContext();
        PoxPayloadIn input = (PoxPayloadIn)ctx.getInput();
        DocumentModel documentModel = (wrapDoc.getWrappedObject());
        String itemCsid = documentModel.getName();

        //TODO: create all relations....  UPDATE and CREATE will call.   Updates AuthorityItem part
        RelationsCommonList relationsCommonList = updateRelations(itemCsid, input);
        PayloadOutputPart payloadOutputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, relationsCommonList);
        ctx.setProperty(RelationClient.SERVICE_COMMON_LIST_NAME, payloadOutputPart);
    }

    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.completeUpdate(wrapDoc);
        //now we add part for relations list
        ServiceContext ctx = getServiceContext();
        PayloadOutputPart foo = (PayloadOutputPart)ctx.getProperty(RelationClient.SERVICE_COMMON_LIST_NAME);
        ((PoxPayloadOut)ctx.getOutput()).addPart(foo);
    }

    //===================================================================
    /*
        for (RelationsCommonList.RelationListItem parentListItem : parentList.getRelationListItem()) {
            System.out.println("    parentListItems " + parentListItem);
            //todo: if num-parents > 1 then complain.
            //todo: if not found in update list, remove from system
            //todo: if update list item not found in parent list, add to system.
        }
        for (RelationsCommonList.RelationListItem childListItem : childList.getRelationListItem()) {
           System.out.println("    childListItem: " + childListItem);
            //todo: if not found in update list, remove from system
            //todo: if update list item not found in child list, add to system.
        }



     */
    public RelationsCommonList updateRelations(String itemCSID, PoxPayloadIn input) throws Exception {
        PayloadInputPart part = input.getPart(RelationClient.SERVICE_COMMON_LIST_NAME);        //input.getPart("relations_common");
        if (part == null) {
            System.out.println("Nothing to do in updateRelations: " + input);
            return null;
        }
        RelationsCommonList relationsCommonListBody = (RelationsCommonList) part.getBody();

        ServiceContext ctx = getServiceContext();
        MultivaluedMap queryParams = ctx.getQueryParams();
       String predicate = RelationshipType.HAS_BROADER.value();
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, null);
        queryParams.putSingle(IRelationsManager.SUBJECT_TYPE_QP, null);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, itemCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_TYPE_QP, null);

        RelationsCommonList childListOuter = (new RelationResource()).getList(ctx.getUriInfo());    //magically knows all query params because they are in the context.

        //Leave predicate, swap subject and object.
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, itemCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);

        RelationsCommonList parentListOuter = (new RelationResource()).getList(ctx.getUriInfo());
        /*
            go through inboundList, remove anything from childList that matches  from childList
            go through inboundList, remove anything from parentList that matches  from parentList
            go through parentList, delete all remaining
            go through childList, delete all remaining
            go through actionList, add all remaining.
            check for duplicate children
            check for more than one parent.

        inboundList                           parentList                      childList          actionList
        ----------------                          ---------------                  ----------------       ----------------
        child-a                                   parent-c                        child-a             child-b
        child-b                                   parent-d                        child-c
         parent-a
           */
        String HAS_BROADER = RelationshipType.HAS_BROADER.value();

        List<RelationsCommonList.RelationListItem> inboundList = relationsCommonListBody.getRelationListItem();
        List<RelationsCommonList.RelationListItem> actionList = newList();
        List<RelationsCommonList.RelationListItem> childList = childListOuter.getRelationListItem();
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();

        for (RelationsCommonList.RelationListItem inboundItem : inboundList) {
            if (inboundItem.getObjectCsid().equals(itemCSID) && inboundItem.getPredicate().equals(HAS_BROADER)){
                //then this is an item that says we have a child.
                RelationsCommonList.RelationListItem childItem = findInList(childList, inboundItem);
                if (childItem != null){
                    removeFromList(childList,  childItem);    //exists, just take it off delete list
                } else {
                    actionList.add(inboundItem);   //doesn't exist as a child, but is a child.  Add to additions list
                }
            } else if  (inboundItem.getSubjectCsid().equals(itemCSID) && inboundItem.getPredicate().equals(HAS_BROADER)) {
                //then this is an item that says we have a parent
                RelationsCommonList.RelationListItem parentItem = findInList(parentList, inboundItem);
                if (parentItem != null){
                    removeFromList(parentList,  parentItem);    //exists, just take it off delete list
                } else {
                    actionList.add(inboundItem);   //doesn't exist as a parent, but is a parent. Add to additions list
                }
            }  else {
                System.out.println("\r\n\r\n================\r\n    Element didn't match parent or child, but may have partial fields that match. inboundItem: "+inboundItem);
                //not dealing with: hasNarrower or any other predicate.

            }
        }
        deleteRelations(parentList, ctx);
        deleteRelations(childList, ctx);
        createRelations(actionList, ctx);

        return relationsCommonListBody;
    }

    private void createRelations(List<RelationsCommonList.RelationListItem> inboundList, ServiceContext ctx){
         for (RelationsCommonList.RelationListItem item : inboundList) {
            RelationsCommon rc = new RelationsCommon();
            //rc.setCsid(item.getCsid());
            rc.setDocumentId1(item.getSubjectCsid());
            rc.setDocumentId2(item.getObjectCsid());
            rc.setRelationshipType(item.getPredicate());
            //todo: is an enum:  rc.setPredicate(item.getPredicate());
            rc.setDocumentType1(item.getSubject().getDocumentType());
            rc.setDocumentType2(item.getObject().getDocumentType());

            PoxPayloadOut payloadOut = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
            PayloadOutputPart outputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMONPART_NAME, rc);
            payloadOut.addPart(outputPart);
            System.out.println("\r\n==== TO CREATE: "+rc.getDocumentId1()+"==>"+rc.getPredicate()+"==>"+rc.getDocumentId2());
            RelationResource relationResource = new RelationResource();
            Object res = relationResource.create(ctx.getUriInfo(), payloadOut.toXML());    //NOTE ui recycled from above to pass in unknown query params.
        }
    }
     private void deleteRelations(List<RelationsCommonList.RelationListItem> list,ServiceContext ctx){
          try {
              for (RelationsCommonList.RelationListItem inboundItem : list) {
                  RelationResource relationResource = new RelationResource();
                  System.out.println("\r\n==== TO DELETE: "+inboundItem.getCsid());
                  Object res = relationResource.delete(inboundItem.getCsid());
              }
          } catch (Throwable t){
              String msg = "Unable to deleteRelations: "+ Tools.errorToString(t, true);
              logger.error(msg);
          }
     }

    private  List<RelationsCommonList.RelationListItem> newList(){
        List<RelationsCommonList.RelationListItem> result = new ArrayList<RelationsCommonList.RelationListItem>();
        return result;
    }
     protected List<RelationsCommonList.RelationListItem> cloneList(List<RelationsCommonList.RelationListItem> inboundList){
        List<RelationsCommonList.RelationListItem> result = newList();
        for (RelationsCommonList.RelationListItem item: inboundList){
            result.add(item);
        }
        return result;
    }
     private RelationsCommonList.RelationListItem findInList(List<RelationsCommonList.RelationListItem> list, RelationsCommonList.RelationListItem item){
         for (RelationsCommonList.RelationListItem listItem : list) {
             if (itemsEqual(listItem, item)){   //equals must be defined, else
                return listItem;
             }
         }
         return null;
     }

    private boolean itemsEqual(RelationsCommonList.RelationListItem item, RelationsCommonList.RelationListItem item2){
        if (item==null || item2==null){
            return false;
        }
        return     (item.getSubjectCsid().equals(item2.getSubjectCsid()))
                && (item.getObjectCsid().equals(item2.getObjectCsid()))
                && ( (item.getPredicate().equals(item2.getPredicate()))
                && (item.getRelationshipType().equals(item2.getRelationshipType()))   )
                && (item.getObject().getDocumentType().equals(item2.getObject().getDocumentType()))
                && (item.getSubject().getDocumentType().equals(item2.getSubject().getDocumentType())) ;
    }

     private void removeFromList(List<RelationsCommonList.RelationListItem> list, RelationsCommonList.RelationListItem item){
        list.remove(item);
    }
    //================= TODO: move this to common, refactoring this and  CollectionObjectResource.java

    public RelationsCommonList getRelations(String subjectCSID, String objectCSID, String predicate) throws Exception {
        ServiceContext ctx = getServiceContext();
        MultivaluedMap queryParams = ctx.getQueryParams();
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, subjectCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, objectCSID);

        RelationResource relationResource = new RelationResource();
        RelationsCommonList relationsCommonList = relationResource.getList(ctx.getUriInfo());
        return relationsCommonList;
    }

    //============================= END refactor ==========================

}

