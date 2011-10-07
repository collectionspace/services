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

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.common.api.CommonAPI;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.DocumentWrapperImpl;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.collectionspace.services.relation.RelationshipType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.collectionspace.services.common.authority.AuthorityItemRelations;
/**
 * AuthorityItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityItemDocumentModelHandler<AICommon>
        extends DocHandlerBase<AICommon> {

    private final Logger logger = LoggerFactory.getLogger(AuthorityItemDocumentModelHandler.class);
    private String authorityItemCommonSchemaName;
    /**
     * inVocabulary is the parent Authority for this context
     */
    protected String inAuthority;
    protected String authorityRefNameBase;

    public AuthorityItemDocumentModelHandler(String authorityItemCommonSchemaName) {
        this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }

    public String getInAuthority() {
        return inAuthority;
    }

    public void setInAuthority(String inAuthority) {
        this.inAuthority = inAuthority;
    }

    /** Subclasses may override this to customize the URI segment. */
    public String getAuthorityServicePath() {
        return getServiceContext().getServiceName().toLowerCase();    // Laramie20110510 CSPACE-3932
    }

    @Override
    public String getUri(DocumentModel docModel) {
        // Laramie20110510 CSPACE-3932
        String authorityServicePath = getAuthorityServicePath();
        return "/" + authorityServicePath + '/' + inAuthority + '/' + AuthorityClient.ITEMS + '/' + getCsid(docModel);
    }

    public String getAuthorityRefNameBase() {
        return this.authorityRefNameBase;
    }

    public void setAuthorityRefNameBase(String value) {
        this.authorityRefNameBase = value;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // first fill all the parts of the document
        super.handleCreate(wrapDoc);
        handleInAuthority(wrapDoc.getWrappedObject());
    	handleComputedDisplayNames(wrapDoc.getWrappedObject());
        // CSPACE-3178:
        // Uncomment once debugged and App layer is read to integrate
        // Experimenting with these uncommented now ...
        handleDisplayNameAsShortIdentifier(wrapDoc.getWrappedObject(), authorityItemCommonSchemaName);
        updateRefnameForAuthorityItem(wrapDoc, authorityItemCommonSchemaName, getAuthorityRefNameBase());
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	super.handleUpdate(wrapDoc);
    	handleComputedDisplayNames(wrapDoc.getWrappedObject());
    }

    /**
     * Handle display name.
     *
     * @param docModel the doc model
     * @throws Exception the exception
     */
    protected void handleComputedDisplayNames(DocumentModel docModel) throws Exception {
    	// Do nothing by default.
    }

    private void handleDisplayNameAsShortIdentifier(DocumentModel docModel, String schemaName) throws Exception {
        String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
        String displayName = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.DISPLAY_NAME);
        String shortDisplayName = "";
        try {
            shortDisplayName = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.SHORT_DISPLAY_NAME);
        } catch (PropertyNotFoundException pnfe) {
            // Do nothing on exception. Some vocabulary schemas may not include a short display name.
        }
        if (Tools.isEmpty(shortIdentifier)) {
            String generatedShortIdentifier = AuthorityIdentifierUtils.generateShortIdentifierFromDisplayName(displayName, shortDisplayName);
            docModel.setProperty(schemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER, generatedShortIdentifier);
        }
    }

    protected void updateRefnameForAuthorityItem(DocumentWrapper<DocumentModel> wrapDoc,
            String schemaName,
            String authorityRefBaseName) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String suppliedRefName = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.REF_NAME);
        // CSPACE-3178:
        // Temporarily accept client-supplied refName values, rather than always generating such values.
        // Remove the surrounding 'if' statement when clients should no longer supply refName values.
        if (suppliedRefName == null || suppliedRefName.isEmpty()) {
            String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
            String displayName = (String) docModel.getProperty(schemaName, AuthorityItemJAXBSchema.DISPLAY_NAME);
            if (Tools.isEmpty(authorityRefBaseName)) {
                throw new Exception("Could not create the refName for this authority term, because the refName for its authority parent was empty.");
            }
            RefName.Authority authority = RefName.Authority.parse(authorityRefBaseName);
            String refName = RefName.buildAuthorityItem(authority, shortIdentifier, displayName).toString();
            docModel.setProperty(schemaName, AuthorityItemJAXBSchema.REF_NAME, refName);
        }
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
     * Filters out selected values supplied in an update request.
     * 
     * For example, filters out AuthorityItemJAXBSchema.IN_AUTHORITY, to ensure
     * that the link to the item's parent remains untouched.
     * 
     * @param objectProps the properties filtered out from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
            Map<String, Object> objectProps, ObjectPartType partMeta) {
        super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
        String commonPartLabel = getServiceContext().getCommonPartLabel();
        if (partMeta.getLabel().equalsIgnoreCase(commonPartLabel)) {
            objectProps.remove(AuthorityItemJAXBSchema.IN_AUTHORITY);
            objectProps.remove(AuthorityItemJAXBSchema.CSID);
            objectProps.remove(AuthorityJAXBSchema.SHORT_IDENTIFIER);
            // Enable when clients should no longer supply refName values
            // objectProps.remove(AuthorityItemJAXBSchema.REF_NAME); // CSPACE-3178

        }
    }

    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        super.extractAllParts(wrapDoc);

        String showSiblings = ctx.getQueryParams().getFirst(CommonAPI.showSiblings_QP);
        if (Tools.isTrue(showSiblings)) {
            showSiblings(wrapDoc, ctx);
            return;   // actual result is returned on ctx.addOutputPart();
        }

        String showRelations = ctx.getQueryParams().getFirst(CommonAPI.showRelations_QP);
        if (Tools.isTrue(showRelations)) {
            showRelations(wrapDoc, ctx);
            return;   // actual result is returned on ctx.addOutputPart();
        }

        String showAllRelations = ctx.getQueryParams().getFirst(CommonAPI.showAllRelations_QP);
        if (Tools.isTrue(showAllRelations)) {
            showAllRelations(wrapDoc, ctx);
            return;   // actual result is returned on ctx.addOutputPart();
        }
    }

    /** @return null on parent not found
     */
    protected String getParentCSID(String thisCSID) throws Exception {
        String parentCSID = null;
        try {
            String predicate = RelationshipType.HAS_BROADER.value();
            RelationsCommonList parentListOuter = getRelations(thisCSID, null, predicate);
            List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();
            if (parentList != null) {
                if (parentList.size() == 0) {
                    return null;
                }
                RelationsCommonList.RelationListItem relationListItem = parentList.get(0);
                parentCSID = relationListItem.getObjectCsid();
            }
            return parentCSID;
        } catch (Exception e) {
            logger.error("Could not find parent for this: " + thisCSID, e);
            return null;
        }
    }

    public void showRelations(DocumentWrapper<DocumentModel> wrapDoc,
            MultipartServiceContext ctx) throws Exception {
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
        childrenListOuter.setItemsInPage(childrenListOuter.getItemsInPage() + added);

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, childrenListOuter);
        ctx.addOutputPart(relationsPart);
    }

    public void showSiblings(DocumentWrapper<DocumentModel> wrapDoc,
            MultipartServiceContext ctx) throws Exception {
        String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());
        String parentCSID = getParentCSID(thisCSID);
        if (parentCSID == null) {
            logger.warn("~~~~~\r\n~~~~ Could not find parent for this: " + thisCSID);
            return;
        }

        String predicate = RelationshipType.HAS_BROADER.value();
        RelationsCommonList siblingListOuter = getRelations(null, parentCSID, predicate);
        List<RelationsCommonList.RelationListItem> siblingList = siblingListOuter.getRelationListItem();

        List<RelationsCommonList.RelationListItem> toRemoveList = newList();


        RelationsCommonList.RelationListItem item = null;
        for (RelationsCommonList.RelationListItem sibling : siblingList) {
            if (thisCSID.equals(sibling.getSubjectCsid())) {
                toRemoveList.add(sibling);   //IS_A copy of the main item, i.e. I have a parent that is my parent, so I'm in the list from the above query.
            }
        }
        //rather than create an immutable iterator, I'm just putting the items to remove on a separate list, then looping over that list and removing.
        for (RelationsCommonList.RelationListItem self : toRemoveList) {
            removeFromList(siblingList, self);
        }

        long siblingSize = siblingList.size();
        siblingListOuter.setTotalItems(siblingSize);
        siblingListOuter.setItemsInPage(siblingSize);

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, siblingListOuter);
        ctx.addOutputPart(relationsPart);
    }

    public void showAllRelations(DocumentWrapper<DocumentModel> wrapDoc, MultipartServiceContext ctx) throws Exception {
        String thisCSID = NuxeoUtils.getCsid(wrapDoc.getWrappedObject());

        RelationsCommonList subjectListOuter = getRelations(thisCSID, null, null);   //  nulls are wildcards:  predicate=*, and object=*
        List<RelationsCommonList.RelationListItem> subjectList = subjectListOuter.getRelationListItem();

        RelationsCommonList objectListOuter = getRelations(null, thisCSID, null);   //  nulls are wildcards:  subject=*, and predicate=*
        List<RelationsCommonList.RelationListItem> objectList = objectListOuter.getRelationListItem();

        //  MERGE LISTS:
        subjectList.addAll(objectList);

        //now subjectList actually has records BOTH where thisCSID is subject and object.
        long relatedSize = subjectList.size();
        subjectListOuter.setTotalItems(relatedSize);
        subjectListOuter.setItemsInPage(relatedSize);

        PayloadOutputPart relationsPart = new PayloadOutputPart(RelationClient.SERVICE_COMMON_LIST_NAME, subjectListOuter);
        ctx.addOutputPart(relationsPart);
    }

    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        ServiceContext ctx = getServiceContext();
        PoxPayloadIn input = (PoxPayloadIn) ctx.getInput();
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
        PayloadOutputPart foo = (PayloadOutputPart) ctx.getProperty(RelationClient.SERVICE_COMMON_LIST_NAME);
        ((PoxPayloadOut) ctx.getOutput()).addPart(foo);
    }

    /**  updateRelations strategy:
    
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

        //Run getList() once as sent to get childListOuter:
        String predicate = RelationshipType.HAS_BROADER.value();
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, null);
        queryParams.putSingle(IRelationsManager.SUBJECT_TYPE_QP, null);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, itemCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_TYPE_QP, null);
        RelationsCommonList childListOuter = (new RelationResource()).getList(ctx.getUriInfo());    //magically knows all query params because they are in the context.

        //Now run getList() again, leaving predicate, swapping subject and object, to get parentListOuter.
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, predicate);
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, itemCSID);
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);
        RelationsCommonList parentListOuter = (new RelationResource()).getList(ctx.getUriInfo());

        String HAS_BROADER = RelationshipType.HAS_BROADER.value();

        List<RelationsCommonList.RelationListItem> inboundList = relationsCommonListBody.getRelationListItem();
        List<RelationsCommonList.RelationListItem> actionList = newList();
        List<RelationsCommonList.RelationListItem> childList = childListOuter.getRelationListItem();
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();

        if (parentList.size() > 1) {
            throw new Exception("Too many parents for object: " + itemCSID + " list: " + dumpList(parentList, "parentList"));
        }

        DocumentModel docModel = wrapDoc.getWrappedObject();

        //Do magic replacement of ${itemCSID} and fix URI's.
        fixupInboundListItems(ctx, inboundList, docModel, itemCSID);

        for (RelationsCommonList.RelationListItem inboundItem : inboundList) {
            if (inboundItem.getObject().getCsid().equals(itemCSID) && inboundItem.getPredicate().equals(HAS_BROADER)) {
                //then this is an item that says we have a child.  That child is inboundItem
                RelationsCommonList.RelationListItem childItem = findInList(childList, inboundItem);
                if (childItem != null) {
                    removeFromList(childList, childItem);    //exists, just take it off delete list
                } else {
                    actionList.add(inboundItem);   //doesn't exist as a child, but is a child.  Add to additions list
                }
                ensureChildHasNoOtherParents(ctx, queryParams, inboundItem.getSubject().getCsid());

            } else if (inboundItem.getSubject().getCsid().equals(itemCSID) && inboundItem.getPredicate().equals(HAS_BROADER)) {
                //then this is an item that says we have a parent.  inboundItem is that parent.
                RelationsCommonList.RelationListItem parentItem = findInList(parentList, inboundItem);
                if (parentItem != null) {
                    removeFromList(parentList, parentItem);    //exists, just take it off delete list
                } else {
                    actionList.add(inboundItem);   //doesn't exist as a parent, but is a parent. Add to additions list
                }
            } else {
                logger.warn("Element didn't match parent or child, but may have partial fields that match. inboundItem: " + inboundItem);
                //not dealing with: hasNarrower or any other predicate.
            }
        }
        String dump = dumpLists(itemCSID, parentList, childList, actionList);
        //System.out.println("====dump====="+CR+dump);
        logger.info("~~~~~~~~~~~~~~~~~~~~~~dump~~~~~~~~~~~~~~~~~~~~~~~~" + CR + dump);
        deleteRelations(parentList, ctx, "parentList");               //todo: there are items appearing on both lists....april 20.
        deleteRelations(childList, ctx, "childList");
        createRelations(actionList, ctx);
        //We return all elements on the inbound list, since we have just worked to make them exist in the system
        // and be non-redundant, etc.  That list came from relationsCommonListBody, so it is still attached to it, just pass that back.
        return relationsCommonListBody;
    }

    private void ensureChildHasNoOtherParents(ServiceContext ctx, MultivaluedMap queryParams, String childCSID) {
        queryParams.putSingle(IRelationsManager.SUBJECT_QP, childCSID);
        queryParams.putSingle(IRelationsManager.PREDICATE_QP, RelationshipType.HAS_BROADER.value());
        queryParams.putSingle(IRelationsManager.OBJECT_QP, null);  //null means ANY
        RelationsCommonList parentListOuter = (new RelationResource()).getList(ctx.getUriInfo());
        List<RelationsCommonList.RelationListItem> parentList = parentListOuter.getRelationListItem();
        //logger.warn("ensureChildHasNoOtherParents preparing to delete relations on "+childCSID+"\'s parent list: \r\n"+dumpList(parentList, "duplicate parent list"));
        deleteRelations(parentList, ctx, "parentList-delete");
    }

    private String dumpLists(String itemCSID,
            List<RelationsCommonList.RelationListItem> parentList,
            List<RelationsCommonList.RelationListItem> childList,
            List<RelationsCommonList.RelationListItem> actionList) {
        StringBuffer sb = new StringBuffer();
        sb.append("itemCSID: " + itemCSID + CR);
        sb.append(dumpList(parentList, "parentList"));
        sb.append(dumpList(childList, "childList"));
        sb.append(dumpList(actionList, "actionList"));
        return sb.toString();
    }
    private final static String CR = "\r\n";
    private final static String T = " ";

    private String dumpList(List<RelationsCommonList.RelationListItem> list, String label) {
        StringBuffer sb = new StringBuffer();
        String s;
        if (list.size() > 0) {
            sb.append("=========== " + label + " ==========" + CR);
        }
        for (RelationsCommonList.RelationListItem item : list) {
            s =
                    T + item.getSubject().getCsid() //+T4 + item.getSubject().getUri()
                    + T + item.getPredicate()
                    + T + item.getObject().getCsid() //+T4  + item.getObject().getUri()
                    + CR //+"subject:{"+item.getSubject()+"}\r\n object:{"+item.getObject()+"}"
                    //+ CR + "relation-record: {"+item+"}"
                    ;
            sb.append(s);

        }
        return sb.toString();
    }

    /** Performs substitution for ${itemCSID} (see CommonAPI.AuthorityItemCSID_REPLACE for constant)
     *   and sets URI correctly for related items.
     *   Operates directly on the items in the list.  Does not change the list ordering, does not add or remove any items.
     */
    protected void fixupInboundListItems(ServiceContext ctx,
            List<RelationsCommonList.RelationListItem> inboundList,
            DocumentModel docModel,
            String itemCSID) throws Exception {
        String thisURI = this.getUri(docModel);
        // WARNING:  the two code blocks below are almost identical  and seem to ask to be put in a generic method.
        //                    beware of the little diffs in  inboundItem.setObjectCsid(itemCSID); and   inboundItem.setSubjectCsid(itemCSID); in the two blocks.
        for (RelationsCommonList.RelationListItem inboundItem : inboundList) {
            RelationsDocListItem inboundItemObject = inboundItem.getObject();
            RelationsDocListItem inboundItemSubject = inboundItem.getSubject();

            if (inboundItemObject.getCsid().equalsIgnoreCase(CommonAPI.AuthorityItemCSID_REPLACE)) {
                inboundItem.setObjectCsid(itemCSID);
                inboundItemObject.setCsid(itemCSID);
                inboundItemObject.setUri(getUri(docModel));
            } else {
                String objectCsid = inboundItemObject.getCsid();
                DocumentModel itemDocModel = NuxeoUtils.getDocFromCsid(getRepositorySession(), ctx, objectCsid);    //null if not found.
                DocumentWrapper wrapper = new DocumentWrapperImpl(itemDocModel);
                String uri = this.getRepositoryClient(ctx).getDocURI(wrapper);
                inboundItemObject.setUri(uri);    //CSPACE-4037
            }
            uriPointsToSameAuthority(thisURI, inboundItemObject.getUri());    //CSPACE-4042

            if (inboundItemSubject.getCsid().equalsIgnoreCase(CommonAPI.AuthorityItemCSID_REPLACE)) {
                inboundItem.setSubjectCsid(itemCSID);
                inboundItemSubject.setCsid(itemCSID);
                inboundItemSubject.setUri(getUri(docModel));
            } else {
                String subjectCsid = inboundItemSubject.getCsid();
                DocumentModel itemDocModel = NuxeoUtils.getDocFromCsid(getRepositorySession(), ctx, subjectCsid);    //null if not found.
                DocumentWrapper wrapper = new DocumentWrapperImpl(itemDocModel);
                String uri = this.getRepositoryClient(ctx).getDocURI(wrapper);
                inboundItemSubject.setUri(uri);    //CSPACE-4037
            }
            uriPointsToSameAuthority(thisURI, inboundItemSubject.getUri());  //CSPACE-4042

        }
    }

    public RepositoryClient getRepositoryClient(ServiceContext ctx) {
        RepositoryClient repositoryClient = RepositoryClientFactory.getInstance().getClient(ctx.getRepositoryClientName());
        return repositoryClient;
    }

    // this method calls the RelationResource to have it create the relations and persist them.
    private void createRelations(List<RelationsCommonList.RelationListItem> inboundList, ServiceContext ctx) {
        for (RelationsCommonList.RelationListItem item : inboundList) {
            RelationsCommon rc = new RelationsCommon();
            //rc.setCsid(item.getCsid());
            //todo: assignTo(item, rc);
            RelationsDocListItem itemSubject = item.getSubject();
            RelationsDocListItem itemObject = item.getObject();

            String subjectCsid = itemSubject.getCsid();
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

    private void deleteRelations(List<RelationsCommonList.RelationListItem> list, ServiceContext ctx, String listName) {
        try {
            //if (list.size()>0){ logger.info("==== deleteRelations from : "+listName); }
            for (RelationsCommonList.RelationListItem item : list) {
                RelationResource relationResource = new RelationResource();
                //logger.info("==== TO DELETE: " + item.getCsid() + ": " + item.getSubject().getCsid() + "--" + item.getPredicate() + "-->" + item.getObject().getCsid());
                Object res = relationResource.delete(item.getCsid());
            }
        } catch (Throwable t) {
            String msg = "Unable to deleteRelations: " + Tools.errorToString(t, true);
            logger.error(msg);
        }
    }

    private List<RelationsCommonList.RelationListItem> newList() {
        List<RelationsCommonList.RelationListItem> result = new ArrayList<RelationsCommonList.RelationListItem>();
        return result;
    }

    protected List<RelationsCommonList.RelationListItem> cloneList(List<RelationsCommonList.RelationListItem> inboundList) {
        List<RelationsCommonList.RelationListItem> result = newList();
        for (RelationsCommonList.RelationListItem item : inboundList) {
            result.add(item);
        }
        return result;
    }

    private RelationsCommonList.RelationListItem findInList(List<RelationsCommonList.RelationListItem> list, RelationsCommonList.RelationListItem item) {
        for (RelationsCommonList.RelationListItem listItem : list) {
            if (itemsEqual(listItem, item)) {   //equals must be defined, else
                return listItem;
            }
        }
        return null;
    }

    private boolean itemsEqual(RelationsCommonList.RelationListItem item, RelationsCommonList.RelationListItem item2) {
        if (item == null || item2 == null) {
            return false;
        }
        RelationsDocListItem subj1 = item.getSubject();
        RelationsDocListItem subj2 = item2.getSubject();
        RelationsDocListItem obj1 = item.getObject();
        RelationsDocListItem obj2 = item2.getObject();

        return (subj1.getCsid().equals(subj2.getCsid()))
                && (obj1.getCsid().equals(obj1.getCsid()))
                && ((item.getPredicate().equals(item2.getPredicate()))
                && (item.getRelationshipType().equals(item2.getRelationshipType())))
                && (obj1.getDocumentType().equals(obj2.getDocumentType()))
                && (subj1.getDocumentType().equals(subj2.getDocumentType()));
    }

    private void removeFromList(List<RelationsCommonList.RelationListItem> list, RelationsCommonList.RelationListItem item) {
        list.remove(item);
    }

    /* don't even THINK of re-using this method.
     * String example_uri = "/locationauthorities/7ec60f01-84ab-4908-9a6a/items/a5466530-713f-43b4-bc05";
     */
    private String extractInAuthorityCSID(String uri) {
        String IN_AUTHORITY_REGEX = "/(.*?)/(.*?)/(.*)";
        Pattern p = Pattern.compile(IN_AUTHORITY_REGEX);
        Matcher m = p.matcher(uri);
        if (m.find()) {
            if (m.groupCount() < 3) {
                logger.warn("REGEX-WRONG-GROUPCOUNT looking in " + uri);
                return "";
            } else {
                //String service = m.group(1);
                String inauth = m.group(2);
                //String theRest = m.group(3);
                return inauth;
                //print("service:"+service+", inauth:"+inauth+", rest:"+rest);
            }
        } else {
            logger.warn("REGEX-NOT-MATCHED looking in " + uri);
            return "";
        }
    }

    //ensures CSPACE-4042
    protected void uriPointsToSameAuthority(String thisURI, String inboundItemURI) throws Exception {
        String authorityCSID = extractInAuthorityCSID(thisURI);
        String authorityCSIDForInbound = extractInAuthorityCSID(inboundItemURI);
        if (Tools.isBlank(authorityCSID)
                || Tools.isBlank(authorityCSIDForInbound)
                || (!authorityCSID.equalsIgnoreCase(authorityCSIDForInbound))) {
            throw new Exception("Item URI " + thisURI + " must point to same authority as related item: " + inboundItemURI);
        }
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
    //============================= END TODO refactor ==========================
}
