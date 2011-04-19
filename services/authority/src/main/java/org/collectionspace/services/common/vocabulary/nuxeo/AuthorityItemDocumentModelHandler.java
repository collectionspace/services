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
import java.util.List;
import java.util.Map;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
//import org.collectionspace.services.common.authority.AuthorityItemRelations;
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

        RelationsCommonList childList = (new RelationResource()).getList(ctx.getUriInfo());    //magically knows all query params because they are in the context.
        for (RelationsCommonList.RelationListItem childListItem : childList.getRelationListItem()) {
           // System.out.println("    childListItem: " + childListItem);
            //todo: if not found in update list, remove from system
            //todo: if update list item not found in child list, add to system.
        }

        //Leave predicate, swap subject and object.
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, itemCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);

        RelationsCommonList parentList = (new RelationResource()).getList(ctx.getUriInfo());
        for (RelationsCommonList.RelationListItem parentListItem : parentList.getRelationListItem()) {
           // System.out.println("    parentListItem: " + parentListItem);
            //todo: if num-parents > 1 then complain.
            //todo: if not found in update list, remove from system
            //todo: if update list item not found in parent list, add to system.
        }
        List<RelationsCommonList.RelationListItem> inboundList = relationsCommonListBody.getRelationListItem();
        for (RelationsCommonList.RelationListItem item : inboundList) {
            RelationsCommon rc = new RelationsCommon();
            rc.setCsid(item.getCsid());
            rc.setDocumentId1(item.getSubjectCsid());
            rc.setDocumentId2(item.getObjectCsid());
            rc.setRelationshipType(item.getPredicate());
            //todo: is an enum:  rc.setPredicate(item.getPredicate());
            rc.setDocumentType1(item.getSubject().getType());
            rc.setDocumentType2(item.getObject().getType());

            PoxPayloadOut payloadOut = new PoxPayloadOut(RelationClient.SERVICE_PAYLOAD_NAME);
            PayloadOutputPart outputPart = new PayloadOutputPart(RelationClient.SERVICE_COMMONPART_NAME, rc);
            payloadOut.addPart(outputPart);

            RelationResource relationResource = new RelationResource();
            Object res = relationResource.create(ctx.getUriInfo(), payloadOut.toXML());    //NOTE ui recycled from above to pass in unknown query params.
        }
        return relationsCommonListBody;
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

