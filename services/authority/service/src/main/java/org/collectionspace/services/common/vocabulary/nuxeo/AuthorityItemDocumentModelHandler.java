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
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.UriTemplateRegistry;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentReferenceException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.AuthorityServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.vocabulary.VocabularyItemJAXBSchema;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import org.collectionspace.services.common.authority.AuthorityItemRelations;
/**
 * AuthorityItemDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AuthorityItemDocumentModelHandler<AICommon>
        extends NuxeoDocumentModelHandler<AICommon> {

    private final Logger logger = LoggerFactory.getLogger(AuthorityItemDocumentModelHandler.class);
    
    private static final Integer PAGE_SIZE_FROM_QUERYPARAMS = null;
    private static final Integer PAGE_NUM_FROM_QUERYPARAMS = null;
    
    protected String authorityCommonSchemaName;
    protected String authorityItemCommonSchemaName;
    private String authorityItemTermGroupXPathBase;
    
    private boolean shouldUpdateSASFields = true;
    private boolean syncHierarchicalRelationships = false;
    private boolean isProposed = false; // used by local authority to propose a new shared item. Allows local deployments to use new terms until they become official
    private boolean isSAS = false; // used to indicate if the authority item originated as a SAS item
    private boolean shouldUpdateRevNumber = true; // by default we should update the revision number -not true on synchronization with SAS
    /**
     * inVocabulary is the parent Authority for this context
     */
    protected String inAuthority = null;
    protected boolean wildcardedAuthorityRequest = false;
    protected String authorityRefNameBase = null;
    // Used to determine when the displayName changes as part of the update.
    protected String oldDisplayNameOnUpdate = null;
    private final static String LIST_SUFFIX = "List";
    private final static String ZERO_OR_MORE_ANY_CHAR_REGEX = ".*";

    public AuthorityItemDocumentModelHandler(String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
        this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
    }
    
    abstract public String getParentCommonSchemaName();
    
    //
    // Getter and Setter for 'shouldUpdateSASFields'
    //
    public boolean getShouldUpdateSASFields() {
        return shouldUpdateSASFields;
    }
    
    public void setshouldUpdateSASFields(boolean flag) {
        shouldUpdateSASFields = flag;
    }
    
    //
    // Getter and Setter for 'proposed'
    //
    public boolean getIsProposed() {
        return this.isProposed;
    }
    
    public void setIsProposed(boolean flag) {
        this.isProposed = flag;
    }
    
    //
    // Getter and Setter for 'isSAS'
    //
    public boolean getIsSASItem() {
        return this.isSAS;
    }

    public void setIsSASItem(boolean flag) {
        this.isSAS = flag;
    }
    
    //
    // Getter and Setter for 'shouldUpdateRevNumber'
    //
    public boolean getShouldUpdateRevNumber() {
        return this.shouldUpdateRevNumber;
    }
    
    public void setShouldUpdateRevNumber(boolean flag) {
        this.shouldUpdateRevNumber = flag;
    }

    //
    // Getter and Setter for deciding if we need to synch hierarchical relationships
    //
    public boolean getShouldSyncHierarchicalRelationships() {
        return this.syncHierarchicalRelationships;
    }
    
    public void setShouldSyncHierarchicalRelationships(boolean flag) {
        this.syncHierarchicalRelationships = flag;
    }

    @Override
    public void prepareSync() throws Exception {
        this.setShouldUpdateRevNumber(AuthorityServiceUtils.DONT_UPDATE_REV);  // Never update rev nums on sync operations
    }

    @Override
    protected String getRefnameDisplayName(DocumentWrapper<DocumentModel> docWrapper) {
        String result = null;
        
        DocumentModel docModel = docWrapper.getWrappedObject();
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
        RefName.AuthorityItem refname = (RefName.AuthorityItem)getRefName(ctx, docModel);
        result = refname.getDisplayName();
        
        return result;
    }
    
    /*
     * After calling this method successfully, the document model will contain an updated refname and short ID
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getRefName(org.collectionspace.services.common.context.ServiceContext, org.nuxeo.ecm.core.api.DocumentModel)
     */
    @Override
    public RefName.RefNameInterface getRefName(ServiceContext ctx,
            DocumentModel docModel) {
        RefName.RefNameInterface refname = null;
        
        try {
            String displayName = getPrimaryDisplayName(docModel, authorityItemCommonSchemaName,
                    getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
            if (Tools.isEmpty(displayName)) {
                throw new Exception("The displayName for this authority term was empty or not set.");
            }
        
            String shortIdentifier = (String) docModel.getProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
            if (Tools.isEmpty(shortIdentifier)) {
                // We didn't find a short ID in the payload request, so we need to synthesize one.
                shortIdentifier = handleDisplayNameAsShortIdentifier(docModel); // updates the document model with the new short ID as a side-effect
            }
            
            String authorityRefBaseName = getAuthorityRefNameBase();
            if (Tools.isEmpty(authorityRefBaseName)) {
                throw new Exception("Could not create the refName for this authority term, because the refName for its authority parent was empty.");
            }
            
            // Create the items refname using the parent's as a base
            RefName.Authority parentsRefName = RefName.Authority.parse(authorityRefBaseName);
            refname = RefName.buildAuthorityItem(parentsRefName, shortIdentifier, displayName);
            // Now update the document model with the refname value
            String refNameStr = refname.toString();
            docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REF_NAME, refNameStr); // REM - This field is deprecated now that the refName is part of the collection_space core schema

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return refname;
    }
    
    public void setInAuthority(String inAuthority) {
        this.inAuthority = inAuthority;
    }
    
   public String getInAuthorityCsid() {
        return this.inAuthority;
    }

    /** Subclasses may override this to customize the URI segment. */
    public String getAuthorityServicePath() {
        return getServiceContext().getServiceName().toLowerCase();    // Laramie20110510 CSPACE-3932
    }

    @Override
    public String getUri(DocumentModel docModel) {
        // Laramie20110510 CSPACE-3932
        String authorityServicePath = getAuthorityServicePath();
        if(inAuthority==null) {    // Only true with the first document model received, on queries to wildcarded authorities
            wildcardedAuthorityRequest = true;
        }
        // If this search crosses multiple authorities, get the inAuthority value
        // from each record, rather than using the cached value from the first record
        if(wildcardedAuthorityRequest) {
            try {
                inAuthority = (String) docModel.getProperty(authorityItemCommonSchemaName,
                    AuthorityItemJAXBSchema.IN_AUTHORITY);
            } catch (ClientException pe) {
                throw new RuntimeException("Could not get parent specifier for item!");
            }
        }
        return "/" + authorityServicePath + '/' + inAuthority + '/' + AuthorityClient.ITEMS + '/' + getCsid(docModel);
    }

    protected String getAuthorityRefNameBase() {
        return this.authorityRefNameBase;
    }

    public void setAuthorityRefNameBase(String value) {
        this.authorityRefNameBase = value;
    }

    /*
     * Note: the Vocabulary service's VocabularyItemDocumentModelHandler class overrides this method.
     */
    protected ListResultField getListResultsDisplayNameField() {
        ListResultField result = new ListResultField();
        // Per CSPACE-5132, the name of this element remains 'displayName'
        // for backwards compatibility, although its value is obtained
        // from the termDisplayName field.
        //
        // Update: this name is now being changed to 'termDisplayName', both
        // because this is the actual field name and because the app layer
        // work to convert over to this field is underway. Per Patrick, the
        // app layer treats lists, in at least some context(s), as sparse record
        // payloads, and thus fields in list results must all be present in
        // (i.e. represent a strict subset of the fields in) record schemas.
        // - ADR 2012-05-11
        // 
        //
        // In CSPACE-5134, these list results will change substantially
        // to return display names for both the preferred term and for
        // each non-preferred term (if any).
        result.setElement(AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
        result.setXpath(NuxeoUtils.getPrimaryXPathPropertyName(
                authorityItemCommonSchemaName, getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_DISPLAY_NAME));
        
        return result;
    }
    
    /*
     * Note: the Vocabulary service's VocabularyItemDocumentModelHandler class overrides this method.
     */    
    protected ListResultField getListResultsTermStatusField() {
        ListResultField result = new ListResultField();
        
        result.setElement(AuthorityItemJAXBSchema.TERM_STATUS);
        result.setXpath(NuxeoUtils.getPrimaryXPathPropertyName(
                authorityItemCommonSchemaName, getItemTermInfoGroupXPathBase(), AuthorityItemJAXBSchema.TERM_STATUS));

        return result;
    }    
    
    private boolean isTermDisplayName(String elName) {
        return AuthorityItemJAXBSchema.TERM_DISPLAY_NAME.equals(elName) || VocabularyItemJAXBSchema.DISPLAY_NAME.equals(elName);
    }
    
    /*
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocHandlerBase#getListItemsArray()
     * 
     * Note: We're updating the "global" service and tenant bindings instance here -the list instance here is
     * a reference to the tenant bindings instance in the singleton ServiceMain.
     */
    @Override
    public List<ListResultField> getListItemsArray() throws DocumentException {
        List<ListResultField> list = super.getListItemsArray();
        
        // One-time initialization for each authority item service.
        if (isListItemArrayExtended() == false) {
            synchronized(AuthorityItemDocumentModelHandler.class) {
                if (isListItemArrayExtended() == false) {                    
                    int nFields = list.size();
                    // Ensure that each item in a list of Authority items includes
                    // a set of common fields, so we do not depend upon configuration
                    // for general logic.
                     List<Integer> termDisplayNamePositionsInList = new ArrayList<>();
                           boolean hasShortId = false;
                    boolean hasTermStatus = false;
                    for (int i = 0; i < nFields; i++) {
                        ListResultField field = list.get(i);
                        String elName = field.getElement();
                        if (isTermDisplayName(elName) == true) {
                            termDisplayNamePositionsInList.add(i);
                        } else if (AuthorityItemJAXBSchema.SHORT_IDENTIFIER.equals(elName)) {
                            hasShortId = true;
                        } else if (AuthorityItemJAXBSchema.TERM_STATUS.equals(elName)) {
                            hasTermStatus = true;
                        }
                    }
                    
                    ListResultField field;
                        
                        // Certain fields in authority item list results
                        // are handled specially here
                        
                        // Term display name
                        //
                        // Ignore (throw out) any configuration entries that
                        // specify how the termDisplayName field should be
                        // emitted in authority item lists. This field will
                        // be handled in a standardized manner (see block below).
                        if (termDisplayNamePositionsInList.isEmpty() == false) {
                            // Remove matching items starting at the end of the list
                            // and moving towards the start, so that reshuffling of
                            // list order doesn't alter the positions of earlier items
                            Collections.sort(termDisplayNamePositionsInList, Collections.reverseOrder());
                            for (int i : termDisplayNamePositionsInList) {
                                list.remove(i);
                            }
                        }
                        // termDisplayName values in authority item lists
                        // will be handled via code that emits display names
                        // for both the preferred term and all non-preferred
                        // terms (if any). The following is a placeholder
                        // entry that will trigger this code. See the
                        // getListResultValue() method in this class.
                    field = getListResultsDisplayNameField();
                    list.add(field);
                        
                        // Short identifier
                    if (!hasShortId) {
                        field = new ListResultField();
                        field.setElement(AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
                        field.setXpath(AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
                        list.add(field);
                    }
                        
                        // Term status
                    if (!hasTermStatus) {
                        field = getListResultsTermStatusField();
                        list.add(field);
                    }
                        
                }
     
                setListItemArrayExtended(true);
            } // end of synchronized block
        }

        return list;
    }
    
    /**
     * We consider workflow state changes as a change that should bump the revision number.
     * Warning: This method might change the transitionDef's transtionName value
     */
    @Override
    public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef) throws Exception {
        // Decide whether or not to update the revision number
        if (this.getShouldUpdateRevNumber() == true) { // We don't update the rev number of synchronization requests
            updateRevNumbers(wrapDoc);
        }
        //
        // We can't delete an authority item that has referencing records.
        //
        DocumentModel docModel = wrapDoc.getWrappedObject();
        if (transitionDef.getName().equalsIgnoreCase(WorkflowClient.WORKFLOWTRANSITION_DELETE)) {
            AuthorityRefDocList refsToAllObjects = getReferencingObjectsForStateTransitions(ctx, docModel, RefObjsSearchType.ALL);
            AuthorityRefDocList refsToSoftDeletedObjects = getReferencingObjectsForStateTransitions(ctx, docModel, RefObjsSearchType.DELETED_ONLY);
            if (refsToAllObjects.getTotalItems() > 0) {
                if (refsToAllObjects.getTotalItems() > refsToSoftDeletedObjects.getTotalItems()) {
                    //
                    // If the number of refs to active objects is greater than the number of refs to
                    // soft deleted objects then we can't delete the item.
                    //
                    logger.error(String.format("Cannot delete authority item CSID='%s' because it still has records in the system that are referencing it.",
                            docModel.getName()));
                    if (logger.isWarnEnabled() == true) {
                        logReferencingObjects(docModel, refsToAllObjects);
                    }

                    throw new DocumentReferenceException(String.format("Cannot delete authority item '%s' because it still has records in the system that are referencing it.  See the service layer log file for details.",
                            docModel.getName()));
                }
            }
        }
    }
    
    /**
     * 
     * @param wrapDoc
     * @return
     * @throws Exception
     */
    protected boolean handleRelationsSync(DocumentWrapper<Object> wrapDoc) throws Exception {
        boolean result = false;
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();

        //
        // Get information about the local authority item so we can compare with corresponding item on the shared authority server
        //
        AuthorityItemSpecifier authorityItemSpecifier = (AuthorityItemSpecifier) wrapDoc.getWrappedObject();
        DocumentModel itemDocModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), getAuthorityItemCommonSchemaName(), 
                authorityItemSpecifier);
        if (itemDocModel == null) {
            throw new DocumentNotFoundException(String.format("Could not find authority item resource with CSID='%s'",
                    authorityItemSpecifier.getItemSpecifier().value));
        }
        Long localItemRev = (Long) NuxeoUtils.getProperyValue(itemDocModel, AuthorityItemJAXBSchema.REV);
        Boolean localIsProposed = (Boolean) NuxeoUtils.getProperyValue(itemDocModel, AuthorityItemJAXBSchema.PROPOSED);
        String localItemCsid = itemDocModel.getName();
        String localItemWorkflowState = itemDocModel.getCurrentLifeCycleState();
        String itemShortId = (String) NuxeoUtils.getProperyValue(itemDocModel, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
        
        //
        // Now get the item's Authority (the parent) information
        //
        DocumentModel authorityDocModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), authorityCommonSchemaName,
                authorityItemSpecifier.getParentSpecifier());
        String authorityShortId = (String)NuxeoUtils.getProperyValue(authorityDocModel, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        String localParentCsid = authorityDocModel.getName();
        String remoteClientConfigName = (String)NuxeoUtils.getProperyValue(authorityDocModel, AuthorityJAXBSchema.REMOTECLIENT_CONFIG_NAME);
        //
        // Using the short IDs of the local authority and item, create URN specifiers and retrieve the SAS authority item
        //
        AuthorityItemSpecifier sasAuthorityItemSpecifier = new AuthorityItemSpecifier(SpecifierForm.URN_NAME, authorityShortId, itemShortId);
        // Get the shared authority server's copy
        PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.requestPayloadInFromRemoteServer(sasAuthorityItemSpecifier, 
                remoteClientConfigName, getAuthorityServicePath(), getEntityResponseType(), AuthorityClient.INCLUDE_RELATIONS);
        
        //
        // Get the RelationsCommonList and remove the CSIDs since they are for remote items only. We'll use
        // the refnames in the payload instead to find the local CSIDs
        //
        PayloadInputPart relationsCommonListPart = sasPayloadIn.getPart(RelationClient.SERVICE_COMMON_LIST_NAME);
        relationsCommonListPart.clearElementBody(); // clear the existing DOM element that was created from the incoming XML payload
        RelationsCommonList rcl = (RelationsCommonList) relationsCommonListPart.getBody();  // Get the JAX-B object and clear the CSID values
        for (RelationsCommonList.RelationListItem listItem : rcl.getRelationListItem()) {
            // clear the remote relation item's CSID
            listItem.setCsid(null);
            // clear the remote subject's CSID
            listItem.setSubjectCsid(null);
            listItem.getSubject().setCsid(null);
            listItem.getSubject().setUri(null);
            // clear the remote object's CSID
            listItem.setObjectCsid(null);
            listItem.getObject().setCsid(null);
            listItem.getObject().setUri(null);
        }
        
        //
        // Remove all the payload parts except the relations part since we only want to sync the relationships
        //
        ArrayList<PayloadInputPart> newPartList = new ArrayList<PayloadInputPart>();
        newPartList.add(relationsCommonListPart); // add our CSID filtered RelationsCommonList part
        sasPayloadIn.setParts(newPartList);
        sasPayloadIn = new PoxPayloadIn(sasPayloadIn.toXML()); // Builds a new payload using the current set of parts -i.e., just the relations part
        
        sasPayloadIn = AuthorityServiceUtils.filterRefnameDomains(ctx, sasPayloadIn); // We need to filter the domain name part of any and all refnames in the payload
        AuthorityResource authorityResource = (AuthorityResource) ctx.getResource(getAuthorityServicePath());
        PoxPayloadOut payloadOut = authorityResource.updateAuthorityItem(ctx, 
                ctx.getResourceMap(),                     
                ctx.getUriInfo(),
                localParentCsid,                         // parent's CSID
                localItemCsid,                             // item's CSID
                sasPayloadIn,                            // the payload from the remote SAS
                AuthorityServiceUtils.DONT_UPDATE_REV,    // don't update the parent's revision number
                AuthorityServiceUtils.NOT_PROPOSED,        // The items is not proposed, make it a real SAS item now
                AuthorityServiceUtils.SAS_ITEM);        // Since we're sync'ing, this must be a SAS item
        if (payloadOut != null) {    
            ctx.setOutput(payloadOut);
            result = true;
        }        
        
        return result;
    }
        
    @Override
    public boolean handleSync(DocumentWrapper<Object> wrapDoc) throws Exception {
        boolean result = false;

        if (this.getShouldSyncHierarchicalRelationships() == true) {
            result = handleRelationsSync(wrapDoc);
        } else {
            result = handlePayloadSync(wrapDoc);
        }
        
        return result;
    }
    
    /**
     * 
     * @param wrapDoc
     * @return
     * @throws Exception
     */
    protected boolean handlePayloadSync(DocumentWrapper<Object> wrapDoc) throws Exception {
        boolean result = false;
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        
        //
        // Get information about the local authority item so we can compare with corresponding item on the shared authority server
        //
        AuthorityItemSpecifier authorityItemSpecifier = (AuthorityItemSpecifier) wrapDoc.getWrappedObject();
        DocumentModel itemDocModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), getAuthorityItemCommonSchemaName(), 
                authorityItemSpecifier);
        if (itemDocModel == null) {
            throw new DocumentNotFoundException(String.format("Could not find authority item resource with CSID='%s'",
                    authorityItemSpecifier.getItemSpecifier().value));
        }
        Long localItemRev = (Long) NuxeoUtils.getProperyValue(itemDocModel, AuthorityItemJAXBSchema.REV);
        Boolean localIsProposed = (Boolean) NuxeoUtils.getProperyValue(itemDocModel, AuthorityItemJAXBSchema.PROPOSED);
        String localItemCsid = itemDocModel.getName();
        String localItemWorkflowState = itemDocModel.getCurrentLifeCycleState();
        String itemShortId = (String) NuxeoUtils.getProperyValue(itemDocModel, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);
        
        //
        // Now get the item's Authority (the parent) information
        //
        DocumentModel authorityDocModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), authorityCommonSchemaName,
                authorityItemSpecifier.getParentSpecifier());
        String authorityShortId = (String) NuxeoUtils.getProperyValue(authorityDocModel, AuthorityJAXBSchema.SHORT_IDENTIFIER);
        String localParentCsid = authorityDocModel.getName();
        String remoteClientConfigName = (String)NuxeoUtils.getProperyValue(authorityDocModel, AuthorityJAXBSchema.REMOTECLIENT_CONFIG_NAME);

        //
        // Using the short IDs of the local authority and item, create URN specifiers and retrieve the SAS authority item
        //
        AuthorityItemSpecifier sasAuthorityItemSpecifier = new AuthorityItemSpecifier(SpecifierForm.URN_NAME, authorityShortId, itemShortId);
        // Get the shared authority server's copy
        PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.requestPayloadInFromRemoteServer(sasAuthorityItemSpecifier, 
                remoteClientConfigName, getAuthorityServicePath(), getEntityResponseType(), AuthorityClient.DONT_INCLUDE_RELATIONS);
        Long sasRev = getRevision(sasPayloadIn);
        String sasWorkflowState = getWorkflowState(sasPayloadIn);
        //
        // If the shared authority item is newer, update our local copy
        //
        if (sasRev > localItemRev || localIsProposed || ctx.shouldForceSync()) {
            sasPayloadIn = AuthorityServiceUtils.filterRefnameDomains(ctx, sasPayloadIn); // We need to filter the domain name part of any and all refnames in the payload
            AuthorityResource authorityResource = (AuthorityResource) ctx.getResource(getAuthorityServicePath());
            PoxPayloadOut payloadOut = authorityResource.updateAuthorityItem(ctx, 
                    ctx.getResourceMap(),                     
                    ctx.getUriInfo(),
                    localParentCsid,                         // parent's CSID
                    localItemCsid,                             // item's CSID
                    sasPayloadIn,                            // the payload from the remote SAS
                    AuthorityServiceUtils.DONT_UPDATE_REV,    // don't update the parent's revision number
                    AuthorityServiceUtils.NOT_PROPOSED,        // The items is not proposed, make it a real SAS item now
                    AuthorityServiceUtils.SAS_ITEM);        // Since we're sync'ing, this must be a SAS item
            if (payloadOut != null) {    
                ctx.setOutput(payloadOut);
                result = true;
            }
        }
        //
        // Check to see if we need to update the local items's workflow state to reflect that of the remote's
        //
        List<String> transitionList = getTransitionList(sasWorkflowState, localItemWorkflowState);
        if (transitionList.isEmpty() == false) {
            AuthorityResource authorityResource = (AuthorityResource) ctx.getResource(getAuthorityServicePath()); // Get the authority (parent) client not the item client
            //
            // We need to move the local item to the SAS workflow state.  This might involve multiple transitions.
            //
            for (String transition:transitionList) {
                try {
                    authorityResource.updateItemWorkflowWithTransition(ctx, localParentCsid, localItemCsid, transition, AuthorityServiceUtils.DONT_UPDATE_REV);
                } catch (DocumentReferenceException de) {
                    //
                    // This exception means we tried unsuccessfully to soft-delete (workflow transition 'delete') an item that still has references to it from other records.
                    //
                    AuthorityServiceUtils.setAuthorityItemDeprecated(ctx, itemDocModel, authorityItemCommonSchemaName, AuthorityServiceUtils.DEPRECATED);  // Since we can't sof-delete it, we need to mark it as deprecated since it is soft-deleted on the SAS
                    logger.warn(String.format("Could not transition item CSID='%s' from workflow state '%s' to '%s'.  Check the services log file for details.",
                            localItemCsid, localItemWorkflowState, sasWorkflowState));
                }
            }
            result = true;
        }
        
        return result;
    }
    
    /**
     * We need to change the local item's state to one that maps to the replication server's workflow
     * state.  This might involve making multiple transitions.
     * 
     * WIKI:
     *     See table at https://wiki.collectionspace.org/pages/viewpage.action?pageId=162496564
     * 
     */
    private List<String> getTransitionList(String sasWorkflowState, String localItemWorkflowState) throws DocumentException {
        List<String> result = new ArrayList<String>();        
        //
        // The first set of conditions maps a replication-server "project" state to a local client state of "replicated"
        //
        if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
            // Do nothing.  We're good with this state
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
        //
        // The second set of conditions maps a replication-server "deleted" state to a local client state of "deleted"
        //
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
            result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
            // Do nothing.  We're good with this state
        //
        // The third set of conditions maps a replication-server "replicated" state to a local state of "replicated"
        //
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
            // Do nothing.  We're good with this state
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
        //
        // The last set of conditions maps a replication-server "replicated_deleted" state to a local client state of "deleted"
        //
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
            result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
            result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
        } else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
            // Do nothing.  We're good with this state            
        } else {
            //
            // If we get here, we've encountered a SAS workflow state that we don't recognize.
            //
            throw new DocumentException(String.format("Encountered an invalid workflow state of '%s' on a SAS authority item.", sasWorkflowState));
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // first fill all the parts of the document, refname and short ID get set as well
        super.handleCreate(wrapDoc);
        // Ensure we have required fields set properly
        handleInAuthority(wrapDoc.getWrappedObject());        
    }

    enum RefObjsSearchType {
        ALL, NON_DELETED, DELETED_ONLY
    }
    
    /*
     * This method gets called after the primary update to an authority item has happened.  If the authority item's refName
     * has changed, then we need to updated all the records that use that refname with the new/updated version
     * 
     * (non-Javadoc)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean handleDelete(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        boolean result = true;
        
        ServiceContext ctx = getServiceContext();
        DocumentModel docModel = wrapDoc.getWrappedObject();
        
        AuthorityRefDocList refsToAllObjects = getReferencingObjectsForStateTransitions(ctx, docModel, RefObjsSearchType.ALL);
        AuthorityRefDocList refsToSoftDeletedObjects = getReferencingObjectsForStateTransitions(ctx, docModel, RefObjsSearchType.DELETED_ONLY);
        if (refsToAllObjects.getTotalItems() > 0) {
            if (refsToAllObjects.getTotalItems() > refsToSoftDeletedObjects.getTotalItems()) {
                //
                // If the number of refs to active objects is greater than the number of refs to
                // soft deleted objects then we can't delete the item.
                //
                logger.error(String.format("Cannot delete authority item CSID='%s' because it still has %d records in the system that are referencing it.",
                        docModel.getName(), refsToSoftDeletedObjects.getTotalItems()));
                if (logger.isWarnEnabled() == true) {
                    logReferencingObjects(docModel, refsToAllObjects);
                }

                throw new DocumentReferenceException(String.format("Cannot delete authority item '%s' because it still has records in the system that are referencing it.  See the service layer log file for details.",
                        docModel.getName()));
            } else {
                //
                // If all the refs are to soft-deleted objects, we should soft-delete this authority item instead of hard-deleting it and instead of failing.
                //
                String parentCsid = (String) NuxeoUtils.getProperyValue(docModel, AuthorityItemJAXBSchema.IN_AUTHORITY);
                String itemCsid = docModel.getName();
                AuthorityResource authorityResource = (AuthorityResource) ctx.getResource(getAuthorityServicePath());
                authorityResource.updateItemWorkflowWithTransition(ctx, parentCsid, itemCsid, WorkflowClient.WORKFLOWTRANSITION_DELETE, 
                        this.getShouldUpdateRevNumber());
                result = false; // Don't delete since we just soft-deleted it.                
            }
        }
        
        //
        // Since we've changed the state of the parent by deleting (or soft-deleting) one of its items, we might need to update the parent rev number
        //
        if (getShouldUpdateRevNumber() == true) {
            updateRevNumbers(wrapDoc);
        }
        
        return result;
    }
    
    /**
     * Checks to see if an authority item has referencing objects.
     * 
     * @param ctx
     * @param docModel
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private AuthorityRefDocList getReferencingObjectsForStateTransitions(
            ServiceContext ctx, 
            DocumentModel docModel, 
            RefObjsSearchType searchType) throws Exception {
        AuthorityRefDocList referenceList = null;
        
        if (ctx.getUriInfo() == null) {
            //
            // We need a UriInfo object so we can pass "query" params to the AuthorityResource's getReferencingObjects() method
            //
            ctx.setUriInfo(this.getServiceContext().getUriInfo()); // try to get a UriInfo instance from the handler's context
        }
        
        //
        // Since the call to get referencing objects might indirectly use the WorkflowClient.WORKFLOW_QUERY_NONDELETED query param, we need to
        // temporarily remove that query param if it is set.  If set, we'll save the value and reset once we're finished.
        //
        boolean doesContainValue = ctx.getUriInfo().getQueryParameters().containsKey(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        String previousValue = ctx.getUriInfo().getQueryParameters().getFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        
        try {
            if (doesContainValue) {
                ctx.getUriInfo().getQueryParameters().remove(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
            }
            AuthorityResource authorityResource = (AuthorityResource)ctx.getResource(getAuthorityServicePath());
            referenceList = getReferencingObjects(authorityResource, ctx, docModel, searchType, PAGE_NUM_FROM_QUERYPARAMS, PAGE_SIZE_FROM_QUERYPARAMS, true, true); // useDefaultOrderByClause=true, computeTotal=true
        } finally {
            if (doesContainValue) {
                ctx.getUriInfo().getQueryParameters().addFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP, previousValue);
            }
        }
        
        return referenceList;
    }
    
    @SuppressWarnings("rawtypes")
    private AuthorityRefDocList getReferencingObjectsForMarkingTerm(
            ServiceContext ctx, 
            DocumentModel docModel, 
            RefObjsSearchType searchType) throws Exception {
        AuthorityRefDocList referenceList = null;
        
        if (ctx.getUriInfo() == null) {
            //
            // We need a UriInfo object so we can pass "query" params to the AuthorityResource's getReferencingObjects() method
            //
            ctx.setUriInfo(this.getServiceContext().getUriInfo()); // try to get a UriInfo instance from the handler's context
        }
        
        //
        // Since the call to get referencing objects might indirectly use the WorkflowClient.WORKFLOW_QUERY_NONDELETED query param, we need to
        // temporarily remove that query param if it is set.  If set, we'll save the value and reset once we're finished.
        //
        boolean doesContainValue = ctx.getUriInfo().getQueryParameters().containsKey(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        String previousValue = ctx.getUriInfo().getQueryParameters().getFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        
        try {
            if (doesContainValue) {
                ctx.getUriInfo().getQueryParameters().remove(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
            }
            AuthorityResource authorityResource = (AuthorityResource)ctx.getResource(getAuthorityServicePath());
            referenceList = getReferencingObjects(authorityResource, ctx, docModel, searchType, 0, 1, false, false);  // pageNum=0, pageSize=1, useDefaultOrderClause=false, computeTotal=false
        } finally {
            if (doesContainValue) {
                ctx.getUriInfo().getQueryParameters().addFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP, previousValue);
            }
        }
        
        return referenceList;
    }    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private AuthorityRefDocList getReferencingObjects(
            AuthorityResource authorityResource,
            ServiceContext ctx,
            DocumentModel docModel,
            RefObjsSearchType searchType,
            Integer pageNum, 
            Integer pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws Exception {
        AuthorityRefDocList result = null;
        
        String inAuthorityCsid = (String) docModel.getProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.IN_AUTHORITY);
        String itemCsid = docModel.getName();
        
        try {
            switch (searchType) {
                case ALL:
                    // By default, get get everything
                    break;
                case NON_DELETED:
                    // Get only non-deleted objects
                    ctx.getUriInfo().getQueryParameters().addFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP, Boolean.FALSE.toString());  // Add the wf_deleted=false query param to exclude soft-deleted items
                    break;
                case DELETED_ONLY:
                    // Get only deleted objects
                    ctx.getUriInfo().getQueryParameters().addFirst(WorkflowClient.WORKFLOW_QUERY_ONLY_DELETED_QP, Boolean.TRUE.toString());  // Add the wf_only_deleted query param to get only soft-deleted items
                    break;
            }
            result = authorityResource.getReferencingObjects(ctx, inAuthorityCsid, itemCsid, ctx.getUriInfo(), pageNum, pageSize, useDefaultOrderByClause, computeTotal);

        } finally {
            //
            // Cleanup query params
            //
            switch (searchType) {
                case ALL:
                    break;
                case NON_DELETED:
                    ctx.getUriInfo().getQueryParameters().remove(WorkflowClient.WORKFLOWSTATE_DELETED);
                    break;
                case DELETED_ONLY:
                    ctx.getUriInfo().getQueryParameters().remove(WorkflowClient.WORKFLOW_QUERY_ONLY_DELETED_QP);
                    break;
            }
        }

        return result;
    }    
    
    private void logReferencingObjects(DocumentModel docModel, AuthorityRefDocList refObjs) {
        List<AuthorityRefDocList.AuthorityRefDocItem> items = refObjs.getAuthorityRefDocItem();
        logger.warn(String.format("The authority item CSID='%s' has the following references:", docModel.getName()));
        int i = 0;
        for (AuthorityRefDocList.AuthorityRefDocItem item : items) {
            if (item.getWorkflowState().contains(WorkflowClient.WORKFLOWSTATE_DELETED) == false) {
                logger.warn(docModel.getName() + " referenced by : list-item[" + i + "] "
                        + item.getDocType() + "("
                        + item.getDocId() + ") Name:["
                        + item.getDocName() + "] Number:["
                        + item.getDocNumber() + "] in field:["
                        + item.getSourceField() + "]");
                i++;
            }
        }
    }

    /*
     * This method gets called after the primary update to an authority item has happened.  If the authority item's refName
     * has changed, then we need to updated all the records that use that refname with the new/updated version
     * 
     * (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // Must call our super class' version first
        super.completeUpdate(wrapDoc);
        
        //
        // Look for and update authority references with the updated refName
        //
        if (hasRefNameUpdate() == true) {
            // We have work to do.
            if (logger.isDebugEnabled()) {
                final String EOL = System.getProperty("line.separator");
                logger.debug("Need to find and update references to authority item." + EOL
                        + "   Old refName" + oldRefNameOnUpdate + EOL
                        + "   New refName" + newRefNameOnUpdate);
            }
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient = getRepositoryClient(ctx);
            CoreSessionInterface repoSession = this.getRepositorySession();
            
            // Update all the existing records that have a field with the old refName in it
            int nUpdated = RefNameServiceUtils.updateAuthorityRefDocs(ctx, repoClient, repoSession,
                    oldRefNameOnUpdate, newRefNameOnUpdate, getRefPropName());
            
            // Finished so log a message.
            if (logger.isDebugEnabled()) {
                logger.debug("Updated " + nUpdated + " instances of oldRefName to newRefName");
            }
        }
    }
    
    /*
     * Note that the Vocabulary service's document-model for items overrides this method.
     */
    protected String getPrimaryDisplayName(DocumentModel docModel, String schema,
            String complexPropertyName, String fieldName) {
        String result = null;

        result = getStringValueInPrimaryRepeatingComplexProperty(docModel, schema, complexPropertyName, fieldName);
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    // FIXME: Once we remove the refName field from the authority item schemas, we can remove this override method since our super does everthing for us now.
    @Deprecated
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // Must call our super's version first, this updates the core schema and the relationship records to deal with possible refName changes/update
        super.handleUpdate(wrapDoc);
        if (this.hasRefNameUpdate() == true) {
            DocumentModel docModel = wrapDoc.getWrappedObject();
            docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REF_NAME, this.newRefNameOnUpdate); // This field is deprecated since it is now a duplicate of what is in the collectionspace_core:refName field            
        }
    }
    
    //
    // Handles both update calls (PUTS) AND create calls (POSTS)
    //
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        DocumentModel documentModel = wrapDoc.getWrappedObject();

        //
        // Update the record's revision number on both CREATE and UPDATE actions (as long as it is NOT a SAS authority item)
        //
        Boolean propertyValue = (Boolean) documentModel.getProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SAS);
        boolean isMarkedAsSASItem = propertyValue != null ? propertyValue : false;
        if (this.getShouldUpdateRevNumber() == true && !isMarkedAsSASItem) { // We won't update rev numbers on synchronization with SAS items and on local changes to SAS items
            updateRevNumbers(wrapDoc);
        }
        
        if (getShouldUpdateSASFields() == true) {
            //
            // If this is a proposed item (not part of the SAS), mark it as such
            //
            documentModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.PROPOSED,
                    new Boolean(this.getIsProposed()));
            //
            // If it is a SAS authority item, mark it as such
            //
            documentModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SAS,
                    new Boolean(this.getIsSASItem()));
        }
    }
    
    /**
     * Update the revision number of both the item and the item's parent.
     * @param wrapDoc
     * @throws Exception
     */
    protected void updateRevNumbers(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        DocumentModel documentModel = wrapDoc.getWrappedObject();
        Long rev = (Long)documentModel.getProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REV);
        if (rev == null) {
            rev = (long)0;
        } else {
            rev++;
        }
        documentModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.REV, rev);
        //
        // Next, update the inAuthority (the parent's) rev number
        //
        String inAuthorityCsid = this.getInAuthorityCsid();
        if (inAuthorityCsid == null) {
            // When inAuthorityCsid is null, it usually means we're performing and update or synch with the SAS
            inAuthorityCsid = (String)documentModel.getProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.IN_AUTHORITY);
        }
        DocumentModel inAuthorityDocModel = NuxeoUtils.getDocFromCsid(getServiceContext(), getRepositorySession(), inAuthorityCsid);
        if (inAuthorityDocModel != null) {
            Long parentRev = (Long)inAuthorityDocModel.getProperty(getParentCommonSchemaName(), AuthorityJAXBSchema.REV);
            if (parentRev == null) {
                parentRev = new Long(0);
            }
               parentRev++;
               inAuthorityDocModel.setProperty(getParentCommonSchemaName(), AuthorityJAXBSchema.REV, parentRev);
               getRepositorySession().saveDocument(inAuthorityDocModel);
        } else {
            logger.warn(String.format("Containing authority '%s' for item '%s' has been deleted.  Item is orphaned, so revision numbers can't be updated.",
                    inAuthorityCsid, documentModel.getName()));
        }
    }    
    
    /**
     * If no short identifier was provided in the input payload, generate a
     * short identifier from the preferred term display name or term name.
     */
    private String handleDisplayNameAsShortIdentifier(DocumentModel docModel) throws Exception {
        String result = (String) docModel.getProperty(authorityItemCommonSchemaName,
                AuthorityItemJAXBSchema.SHORT_IDENTIFIER);

        if (Tools.isEmpty(result)) {
            String termDisplayName = getPrimaryDisplayName(
                    docModel, authorityItemCommonSchemaName,
                    getItemTermInfoGroupXPathBase(),
                    AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);

            String termName = getPrimaryDisplayName(
                    docModel, authorityItemCommonSchemaName,
                    getItemTermInfoGroupXPathBase(),
                    AuthorityItemJAXBSchema.TERM_NAME);

            String generatedShortIdentifier = AuthorityIdentifierUtils.generateShortIdentifierFromDisplayName(termDisplayName,
                            termName);
            docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER,
                    generatedShortIdentifier);
            result = generatedShortIdentifier;
        }
        
        return result;
    }

    /**
     * Generate a refName for the authority item from the short identifier
     * and display name.
     * 
     * All refNames for authority items are generated.  If a client supplies
     * a refName, it will be overwritten during create (per this method) 
     * or discarded during update (per filterReadOnlyPropertiesForPart).
     * 
     * @see #filterReadOnlyPropertiesForPart(Map<String, Object>, org.collectionspace.services.common.service.ObjectPartType)
     * 
     */
    protected String updateRefnameForAuthorityItem(DocumentModel docModel,
            String schemaName) throws Exception {
        String result = null;
        
        RefName.RefNameInterface refname = getRefName(getServiceContext(), docModel);
        String refNameStr = refname.toString();
        docModel.setProperty(schemaName, AuthorityItemJAXBSchema.REF_NAME, refNameStr);
        result = refNameStr;
        
        return result;
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
        if (inAuthority == null) { // Only happens on queries to wildcarded authorities
            throw new IllegalStateException("Trying to Create an object with no inAuthority value!");
        }
        docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.IN_AUTHORITY, inAuthority);
    }
    
    /**
     * Returns a list of records that reference this authority item
     * 
     * @param ctx
     * @param uriTemplateRegistry
     * @param serviceTypes
     * @param propertyName
     * @param itemcsid
     * @return
     * @throws Exception
     */
    public AuthorityRefDocList getReferencingObjects(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            List<String> serviceTypes,
            String propertyName,
            String itemcsid,
            Integer pageNum,
            Integer pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws Exception {
        AuthorityRefDocList authRefDocList = null;
        CoreSessionInterface repoSession = (CoreSessionInterface) ctx.getCurrentRepositorySession();
        boolean releaseRepoSession = false;
        
        try {
            NuxeoRepositoryClientImpl repoClient = (NuxeoRepositoryClientImpl)this.getRepositoryClient(ctx);
            repoSession = this.getRepositorySession();
            if (repoSession == null) {
                repoSession = repoClient.getRepositorySession(ctx);
                releaseRepoSession = true;
            }
            DocumentFilter myFilter = getDocumentFilter();
            if (pageSize != null) {
                myFilter.setPageSize(pageSize);
            }
            if (pageNum != null) {
                myFilter.setStartPage(pageNum);
            }
            myFilter.setUseDefaultOrderByClause(useDefaultOrderByClause);

            try {
                DocumentWrapper<DocumentModel> wrapper = repoClient.getDoc(repoSession, ctx, itemcsid);
                DocumentModel docModel = wrapper.getWrappedObject();
                String refName = (String) NuxeoUtils.getProperyValue(docModel, AuthorityItemJAXBSchema.REF_NAME); //docModel.getPropertyValue(AuthorityItemJAXBSchema.REF_NAME);
                authRefDocList = RefNameServiceUtils.getAuthorityRefDocs(
                        repoSession, 
                        ctx, 
                        repoClient,
                        serviceTypes,
                        refName,
                        propertyName,
                        myFilter, 
                        useDefaultOrderByClause,
                        computeTotal /*computeTotal*/);
            } catch (PropertyException pe) {
                throw pe;
            } catch (DocumentException de) {
                throw de;
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Caught exception ", e);
                }
                throw new DocumentException(e);
            } finally {
                // If we got/aquired a new seesion then we're responsible for releasing it.
                if (releaseRepoSession && repoSession != null) {
                    repoClient.releaseRepositorySession(ctx, repoSession);
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }
        
        return authRefDocList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);

        // Add the CSID to the common part, since they may have fetched via the shortId.
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
            objectProps.remove(AuthorityItemJAXBSchema.REF_NAME);
        }
    }
    
    /**
     * Returns the items in a list of term display names whose names contain
     * a partial term (as might be submitted in a search query, for instance).
     * @param termDisplayNameList a list of term display names.
     * @param partialTerm a partial term display name; that is, a portion
     * of a display name that might be expected to match 0-n terms in the list.
     * @return a list of term display names that matches the partial term.
     * Matches are case-insensitive. As well, before matching is performed, any
     * special-purpose characters that may appear in the partial term (such as
     * wildcards and anchor characters) are filtered out from both compared terms.
     */
    protected List<String> getPartialTermDisplayNameMatches(List<String> termDisplayNameList, String partialTerm) {
        List<String> result = new ArrayList<>();
        String partialTermMatchExpression = filterAnchorAndWildcardChars(partialTerm).toLowerCase();
        try {
            for (String termDisplayName : termDisplayNameList) {
                if (termDisplayName.toLowerCase()
                        .matches(partialTermMatchExpression) == true) {
                        result.add(termDisplayName);
                }
            }
        } catch (PatternSyntaxException pse) {
            logger.warn("Error in regex match pattern '%s' for term display names: %s",
                    partialTermMatchExpression, pse.getMessage());
        }
        return result;
    }
    
    /**
     * Filters user-supplied anchor and wildcard characters in a string,
     * replacing them with equivalent regular expressions.
     * @param term a term in which to filter anchor and wildcard characters.
     * @return the term with those characters filtered.
     */
    protected String filterAnchorAndWildcardChars(String term) {
        if (Tools.isBlank(term)) {
            return term;
        }
        if (term.length() < 3) {
            return term;
        }
        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Term = %s", term));
        }
        Boolean anchorAtStart = false;
        Boolean anchorAtEnd = false;
        String filteredTerm;
        StringBuilder filteredTermBuilder = new StringBuilder(term);
        // Term contains no anchor or wildcard characters.
        if ( (! term.contains(NuxeoRepositoryClientImpl.USER_SUPPLIED_ANCHOR_CHAR))
                && (! term.contains(NuxeoRepositoryClientImpl.USER_SUPPLIED_WILDCARD)) ) {
            filteredTerm = term;
        } else {
            // Term contains at least one such character.
            try {
                // Filter the starting anchor or wildcard character, if any.
                String firstChar = filteredTermBuilder.substring(0,1);
                switch (firstChar) {
                    case NuxeoRepositoryClientImpl.USER_SUPPLIED_ANCHOR_CHAR:
                        anchorAtStart = true;
                        break;
                    case NuxeoRepositoryClientImpl.USER_SUPPLIED_WILDCARD:
                        filteredTermBuilder.deleteCharAt(0);
                        break;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(String.format("After first char filtering = %s", filteredTermBuilder.toString()));
                }
                // Filter the ending anchor or wildcard character, if any.
                int lastPos = filteredTermBuilder.length() - 1;
                String lastChar = filteredTermBuilder.substring(lastPos);
                switch (lastChar) {
                    case NuxeoRepositoryClientImpl.USER_SUPPLIED_ANCHOR_CHAR:
                        filteredTermBuilder.deleteCharAt(lastPos);
                        filteredTermBuilder.insert(filteredTermBuilder.length(), NuxeoRepositoryClientImpl.ENDING_ANCHOR_CHAR);
                        anchorAtEnd = true;
                        break;
                    case NuxeoRepositoryClientImpl.USER_SUPPLIED_WILDCARD:
                        filteredTermBuilder.deleteCharAt(lastPos);
                        break;
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(String.format("After last char filtering = %s", filteredTermBuilder.toString()));
                }
                filteredTerm = filteredTermBuilder.toString();
                // Filter all other wildcards, if any.
                filteredTerm = filteredTerm.replaceAll(NuxeoRepositoryClientImpl.USER_SUPPLIED_WILDCARD_REGEX, ZERO_OR_MORE_ANY_CHAR_REGEX);
                if (logger.isTraceEnabled()) {
                    logger.trace(String.format("After replacing user wildcards = %s", filteredTerm));
                }
            } catch (Exception e) {
                logger.warn(String.format("Error filtering anchor and wildcard characters from string: %s", e.getMessage()));
                return term;
            }
        }
        // Wrap the term in beginning and ending regex wildcards, unless a
        // starting or ending anchor character was present.
        return (anchorAtStart ? "" : ZERO_OR_MORE_ANY_CHAR_REGEX)
                + filteredTerm
                + (anchorAtEnd ? "" : ZERO_OR_MORE_ANY_CHAR_REGEX);
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getPartialTermDisplayNameMatches(DocumentModel docModel, // REM - CSPACE-5133
            String schema, ListResultField field, String partialTerm) {
        List<String> result = null;
          
        String xpath = field.getXpath(); // results in something like "persons_common:personTermGroupList/[0]/termDisplayName"
        int endOfTermGroup = xpath.lastIndexOf("/[0]/");
        String propertyName = endOfTermGroup != -1 ? xpath.substring(0, endOfTermGroup) : xpath; // it may not be multivalued so the xpath passed in would be the property name
        Object value = null;
        
        try {
            value = docModel.getProperty(schema, propertyName);
        } catch (Exception e) {
            logger.error("Could not extract term display name with property = "
                    + propertyName, e);
        }
        
        if (value != null && value instanceof ArrayList) {
            ArrayList<HashMap<String, Object>> termGroupList = (ArrayList<HashMap<String, Object>>)value;
            int arrayListSize = termGroupList.size();
            if (arrayListSize > 1) { // if there's only 1 element in the list then we've already matched the primary term's display name
                List<String> displayNameList = new ArrayList<String>();
                for (int i = 1; i < arrayListSize; i++) { // start at 1, skip the primary term's displayName since we will always return it
                    HashMap<String, Object> map = (HashMap<String, Object>)termGroupList.get(i);
                    String termDisplayName = (String) map.get(AuthorityItemJAXBSchema.TERM_DISPLAY_NAME);
                    displayNameList.add(i - 1, termDisplayName);
                }
                
                result = getPartialTermDisplayNameMatches(displayNameList, partialTerm);
            }
        }

        return result;
    }
    
    private boolean isTermReferenced(DocumentModel docModel) throws Exception {
        boolean result = false;
        
        AuthorityRefDocList referenceList = null;

        String wf_deletedStr = (String) getServiceContext().getQueryParams().getFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        if (wf_deletedStr != null && Tools.isFalse(wf_deletedStr)) {
            //
            // if query param 'wf_deleted=false', we won't count references to soft-deleted records
            //
            referenceList = getReferencingObjectsForMarkingTerm(getServiceContext(), docModel, RefObjsSearchType.NON_DELETED);
        } else {
            //
            // if query param 'wf_deleted=true' or missing, we count references to soft-deleted and active records
            //
            referenceList = getReferencingObjectsForMarkingTerm(getServiceContext(), docModel, RefObjsSearchType.ALL);
        }
        
        if (referenceList.getTotalItems() > 0) {
            result = true;
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getListResultValue(DocumentModel docModel, // REM - CSPACE-5133
            String schema, ListResultField field) throws DocumentException {
        Object result = null;
        String fieldXPath = field.getXpath();
        
        if (fieldXPath.equalsIgnoreCase(AuthorityClient.REFERENCED) == false) {
            result = NuxeoUtils.getXPathValue(docModel, schema, field.getXpath());
        } else {
            //
            // Check to see if the request is asking us to mark terms as referenced or not.
            //
            String markIfReferencedStr = (String) getServiceContext().getQueryParams().getFirst(AuthorityClient.MARK_IF_REFERENCED_QP);
            if (Tools.isTrue(markIfReferencedStr) == false) {
                return null; // leave the <referenced> element as null since they're not asking for it
            } else try {
                return Boolean.toString(isTermReferenced(docModel)); // set the <referenced> element to either 'true' or 'false'
            } catch (Exception e) {
                String msg = String.format("Failed while trying to find records referencing term CSID='%s'.", docModel.getName());
                throw new DocumentException(msg, e);
            }
        }
                
        //
        // Special handling of list item values for authority items (only)
        // takes place here:
        //
        // If the list result field is the termDisplayName element,
        // check whether a partial term matching query was made.
        // If it was, emit values for both the preferred (aka primary)
        // term and for all non-preferred terms, if any.
        //
        String elName = field.getElement();
        if (isTermDisplayName(elName) == true) {
            MultivaluedMap<String, String> queryParams = this.getServiceContext().getQueryParams();
            String partialTerm = queryParams != null ? queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM) : null;
            if (partialTerm != null && partialTerm.trim().isEmpty() == false) {
                String primaryTermDisplayName = (String)result;
                List<String> matches = getPartialTermDisplayNameMatches(docModel, schema, field, partialTerm);
                if (matches != null && matches.isEmpty() == false) {
                    matches.add(0, primaryTermDisplayName); // insert the primary term's display name at the beginning of the list
                    result = matches; // set the result to a list of matching term display names with the primary term's display name at the beginning
                }
            }
        }
        
        return result;
    }
    
    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        super.extractAllParts(wrapDoc);
    }

    protected List<RelationsCommonList.RelationListItem> cloneList(List<RelationsCommonList.RelationListItem> inboundList) {
        List<RelationsCommonList.RelationListItem> result = newRelationsCommonList();
        for (RelationsCommonList.RelationListItem item : inboundList) {
            result.add(item);
        }
        return result;
    }


    /* don't even THINK of re-using this method.
     * String example_uri = "/locationauthorities/7ec60f01-84ab-4908-9a6a/items/a5466530-713f-43b4-bc05";
     */
    @Deprecated
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

    public String getItemTermInfoGroupXPathBase() {
        return authorityItemTermGroupXPathBase;
    }
        
    public void setItemTermInfoGroupXPathBase(String itemTermInfoGroupXPathBase) {
        authorityItemTermGroupXPathBase = itemTermInfoGroupXPathBase;
    }
    
    protected String getAuthorityItemCommonSchemaName() {
        return authorityItemCommonSchemaName;
    }
    
    // @Override
    public boolean isJDBCQuery() {
        boolean result = false;
        
        MultivaluedMap<String, String> queryParams = getServiceContext().getQueryParams();
        //
        // Look the query params to see if we need to make a SQL query.
        //
        String partialTerm = queryParams.getFirst(IQueryManager.SEARCH_TYPE_PARTIALTERM);
        if (partialTerm != null && partialTerm.trim().isEmpty() == false) {
            result = true;
        }
        
        return result;
    }
    
    // By convention, the name of the database table that contains
    // repeatable term information group records is derivable from
    // an existing XPath base value, by removing a suffix and converting
    // to lowercase
    protected String getTermGroupTableName() {
        String termInfoGroupListName = getItemTermInfoGroupXPathBase();
        return termInfoGroupListName.substring(0, termInfoGroupListName.lastIndexOf(LIST_SUFFIX)).toLowerCase();
    }
    
    protected String getInAuthorityValue() {
        String inAuthorityValue = getInAuthorityCsid();
        if (Tools.notBlank(inAuthorityValue)) {
            return inAuthorityValue;
        } else {
            return AuthorityResource.PARENT_WILDCARD;
        }
    }
    
    @Override
    public Map<String,String> getJDBCQueryParams() {
        // FIXME: Get all of the following values from appropriate external constants.
        // At present, these are duplicated in both RepositoryClientImpl
        // and in AuthorityItemDocumentModelHandler.
        final String TERM_GROUP_LIST_NAME = "TERM_GROUP_LIST_NAME";
        final String TERM_GROUP_TABLE_NAME_PARAM = "TERM_GROUP_TABLE_NAME";
        final String IN_AUTHORITY_PARAM = "IN_AUTHORITY";
        
        Map<String,String> params = super.getJDBCQueryParams();
        params.put(TERM_GROUP_LIST_NAME, getItemTermInfoGroupXPathBase());
        params.put(TERM_GROUP_TABLE_NAME_PARAM, getTermGroupTableName());
        params.put(IN_AUTHORITY_PARAM, getInAuthorityValue());
        return params;
    }
    
}
