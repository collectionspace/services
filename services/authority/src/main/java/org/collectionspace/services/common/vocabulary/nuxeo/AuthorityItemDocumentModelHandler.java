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

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
//import org.collectionspace.services.common.authority.AuthorityItemRelations;
import org.collectionspace.services.common.api.CommonAPI;
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

import javax.management.relation.Relation;
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

    @Override
    public String getUri(DocumentModel docModel) {
        return getServiceContextPath()+inAuthority+"/"+ AuthorityClient.ITEMS+"/"+getCsid(docModel);
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
         String showRelations = ctx.getQueryParams().getFirst(CommonAPI.showRelations_QP);
         if (!Tools.isTrue(showRelations)){
             return;
         }
         String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());

         String predicate = RelationshipType.HAS_BROADER.value();
         RelationsCommonList parentListOuter = getRelations(thisCSID, null, predicate);
         List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();

         RelationsCommonList childrenListOuter = getRelations(null, thisCSID, predicate);
         List<RelationsCommonList.RelationListItem> childrenList = childrenListOuter.getRelationListItem();

         //Assume that there are more children than parents.  Will be true for parent/child, but maybe not for other relations.
         //Now add all parents to our childrenList, to be able to return just one list of consolidated results.
         //Not optimal, but that's the current design spec.
        long added = 0;
        for (RelationsCommonList.RelationListItem parent : parentList) {
             childrenList.add(parent);
             added++;
        }
        long childrenSize = childrenList.size();
        childrenListOuter.setTotalItems(childrenSize);
        childrenListOuter.setItemsInPage(childrenListOuter.getItemsInPage()+added);

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, childrenListOuter);
        ctx.addOutputPart(relationsPart);
    }

    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        ServiceContext ctx = getServiceContext();
        PoxPayloadIn input = (PoxPayloadIn)ctx.getInput();
        DocumentModel documentModel = (wrapDoc.getWrappedObject());
        String itemCsid = documentModel.getName();

        //UPDATE and CREATE will call.   Updates relations part
        RelationsCommonList relationsCommonList = updateRelations(itemCsid, input, wrapDoc);

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

    public RelationsCommonList updateRelations(String itemCSID, PoxPayloadIn input, DocumentWrapper<DocumentModel> wrapDoc)
     throws Exception {
        PayloadInputPart part = input.getPart(RelationClient.SERVICE_COMMON_LIST_NAME);        //input.getPart("relations_common");
        if (part == null) {
            return null;  //nothing to do--they didn't send a list of relations.
        }
        RelationsCommonList relationsCommonListBody = (RelationsCommonList) part.getBody();

        ServiceContext ctx = getServiceContext();
        UriInfo uriInfo = ctx.getUriInfo();
        MultivaluedMap queryParams = uriInfo.getQueryParameters();

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

        DocumentModel docModel = wrapDoc.getWrappedObject();

        for (RelationsCommonList.RelationListItem inboundItem : inboundList) {
            if (inboundItem.getObject().getCsid().equalsIgnoreCase(CommonAPI.AuthorityItemCSID_REPLACE)){
                inboundItem.setObjectCsid(itemCSID);
                inboundItem.getObject().setCsid(itemCSID);
                inboundItem.getObject().setUri(getUri(docModel));
            }
            if (inboundItem.getSubject().getCsid().equalsIgnoreCase(CommonAPI.AuthorityItemCSID_REPLACE)){
                inboundItem.setSubjectCsid(itemCSID);
                inboundItem.getSubject().setCsid(itemCSID);
                 inboundItem.getSubject().setUri(getUri(docModel));
            }
            if (inboundItem.getObject().getCsid().equals(itemCSID) && inboundItem.getPredicate().equals(HAS_BROADER)) {
                //then this is an item that says we have a child.
                RelationsCommonList.RelationListItem childItem = findInList(childList, inboundItem);
                if (childItem != null){
                    removeFromList(childList,  childItem);    //exists, just take it off delete list
                } else {
                    actionList.add(inboundItem);   //doesn't exist as a child, but is a child.  Add to additions list
                }
            } else if  (inboundItem.getSubject().getCsid().equals(itemCSID) && inboundItem.getPredicate().equals(HAS_BROADER)) {
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
        deleteRelations(parentList, ctx);               //todo: there are items appearing on both lists....april 20.
        deleteRelations(childList, ctx);
        createRelations(actionList, ctx);
        //We return all elements on the inbound list, since we have just worked to make them exist in the system
        // and be non-redundant, etc.  That list came from relationsCommonListBody, so it is still attached to it, just pass that back.
        return relationsCommonListBody;
    }

    // this method calls the RelationResource to have it create the relations and persist them.
    private void createRelations(List<RelationsCommonList.RelationListItem> inboundList, ServiceContext ctx){
         for (RelationsCommonList.RelationListItem item : inboundList) {
             RelationsCommon rc = new RelationsCommon();
             //rc.setCsid(item.getCsid());
             //todo: assignTo(item, rc);
             RelationsDocListItem itemSubject = item.getSubject();
             RelationsDocListItem itemObject = item.getObject();

             String subjectCsid =  itemSubject.getCsid();
             rc.setDocumentId1(subjectCsid);
             rc.setSubjectCsid(subjectCsid);

             String objCsid = item.getObject().getCsid();
             rc.setDocumentId2(objCsid);
             rc.setObjectCsid(objCsid);

             rc.setRelationshipType(item.getPredicate());
             //RelationshipType  foo = (RelationshipType.valueOf(item.getPredicate())) ;
             //rc.setPredicate(foo);     //this must be one of the type found in the enum in  services/jaxb/src/main/resources/relations_common.xsd

             rc.setDocumentType1(itemSubject.getDocumentType());
             rc.setDocumentType2(itemObject.getDocumentType());

             rc.setSubjectUri(itemSubject.getUri());
             rc.setObjectUri(itemObject.getUri());


            PoxPayloadOut payloadOut = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
            PayloadOutputPart outputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMONPART_NAME, rc);
            payloadOut.addPart(outputPart);
            //System.out.println("\r\n==== TO CREATE: "+rc.getDocumentId1()+"==>"+rc.getPredicate()+"==>"+rc.getDocumentId2());
            RelationResource relationResource = new RelationResource();
            Object res = relationResource.create(ctx.getUriInfo(), payloadOut.toXML());    //NOTE ui recycled from above to pass in unknown query params.
        }
    }
     private void deleteRelations(List<RelationsCommonList.RelationListItem> list,ServiceContext ctx){
          try {
              for (RelationsCommonList.RelationListItem inboundItem : list) {
                  RelationResource relationResource = new RelationResource();
                  //System.out.println("\r\n==== TO DELETE: "+inboundItem.getCsid());
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
        RelationsDocListItem subj1 = item.getSubject();
        RelationsDocListItem subj2 = item2.getSubject();
        RelationsDocListItem obj1 = item.getObject();
        RelationsDocListItem obj2 = item2.getObject();

        return     (subj1.getCsid().equals(subj2.getCsid()))
                && (obj1.getCsid().equals(obj1.getCsid()))
                && ( (item.getPredicate().equals(item2.getPredicate()))
                && (item.getRelationshipType().equals(item2.getRelationshipType()))   )
                && (obj1.getDocumentType().equals(obj2.getDocumentType()))
                && (subj1.getDocumentType().equals(subj2.getDocumentType())) ;
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

