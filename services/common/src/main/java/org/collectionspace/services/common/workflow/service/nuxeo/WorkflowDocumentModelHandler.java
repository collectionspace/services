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
package org.collectionspace.services.common.workflow.service.nuxeo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.workflow.jaxb.WorkflowJAXBSchema;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.workflow.WorkflowCommon;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowDocumentModelHandler
        extends NuxeoDocumentModelHandler<WorkflowCommon> {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(WorkflowDocumentModelHandler.class);
    /*
     * Workflow transitions
     *
     * See the "Nuxeo core default life cycle definition", an XML configuration
     * for Nuxeo's "lifecycle" extension point that specifies valid workflow
     * states and the operations that transition documents to those states, via:
     *
     * org.nuxeo.ecm.core.LifecycleCoreExtensions--lifecycle (as opposed to --types)
     */
    private static final String TRANSITION_UNKNOWN = "unknown";

    
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	//
    	// First, call the parent document handler to give it a chance to handle the workflow transition -otherwise, call
    	// the super/parent handleUpdate() method.
    	//
    	ServiceContext ctx = this.getServiceContext();
    	DocumentModelHandler targetDocHandler = (DocumentModelHandler)ctx.getProperty(WorkflowClient.TARGET_DOCHANDLER);
    	
    	// We need to make sure the repo session is available to the handler and its service context
    	targetDocHandler.setRepositorySession(this.getRepositorySession()); // Make sure the target doc handler has a repository session to work with
    	targetDocHandler.getServiceContext().setCurrentRepositorySession(this.getRepositorySession());
    	
    	TransitionDef transitionDef =  (TransitionDef)ctx.getProperty(WorkflowClient.TRANSITION_ID);
    	targetDocHandler.handleWorkflowTransition(ctx, wrapDoc, transitionDef);  // Call the target resouce's handler first
    	//
    	// If no exception occurred, then call the super's method
    	//
    	super.handleUpdate(wrapDoc);
    }
    
    @Override
    protected void handleRefNameChanges(ServiceContext ctx, DocumentModel docModel) throws ClientException {
    	//
    	// We are intentionally overriding this method to do nothing since the Workflow resource is a meta-resource without a refname
    	//
    }    
    
    /*
     * Handle read (GET)
     */
    @Override
    protected Map<String, Object> extractPart(DocumentModel docModel,
            String schema,
            ObjectPartType partMeta,
            Map<String, Object> addToMap)
            throws Exception {
        Map<String, Object> result = null;

        MediaType mt = MediaType.valueOf(partMeta.getContent().getContentType()); //FIXME: REM - This is no longer needed.  Everything is POX
        if (mt.equals(MediaType.APPLICATION_XML_TYPE)) {
            Map<String, Object> unQObjectProperties =
                    (addToMap != null) ? addToMap : (new HashMap<String, Object>());
            unQObjectProperties.put(WorkflowJAXBSchema.WORKFLOW_LIFECYCLEPOLICY, docModel.getLifeCyclePolicy());
            unQObjectProperties.put(WorkflowJAXBSchema.WORKFLOW_CURRENTLIFECYCLESTATE, docModel.getCurrentLifeCycleState());
            result = unQObjectProperties;
        } //TODO: handle other media types

        return result;
    }

    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String[] schemas = {WorkflowClient.SERVICE_COMMONPART_NAME};
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        for (String schema : schemas) {
            ObjectPartType partMeta = partsMetaMap.get(schema);
            if (partMeta == null) {
                continue; // unknown part, ignore
            }
            Map<String, Object> unQObjectProperties = extractPart(docModel, schema, partMeta);
            addOutputPart(unQObjectProperties, schema, partMeta);
        }
    }
    
    /*
     * Maps the transition name to handle existing states like "replicated" and "deleted".  This allows us to do things like replicate "deleted"
     * records, delete "replicated" records, etc.  For example, this code maps the transition name "delete" to "delete_replicated" on records in the "replicated" state.
     * As another example, it would map "undelete" to "undelete_replicated" for replicated records and just "undelete" for records in any other state.
     * 
     * Essentially, this mapping allows REST API clients to use the "delete", "undelete", "replicate", "unreplicate", etc transitions on records no matter what
     * their current state.  Without this mapping, REST API clients would need to calculate this on their own and use the longer forms like:
     * "delete_replicated", "undelete_replicated", "lock_deleted", "unlock_deleted", etc. 
     */
    public static String getQualifiedTransitionName(DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef) {
    	String result = null;
    	
    	String currentTransitionName = result = transitionDef.getName(); // begin with result set to the current name
    	DocumentModel docModel = wrapDoc.getWrappedObject();
    	Collection<String> allowedTransitionList = docModel.getAllowedStateTransitions();
    	for (String allowedTransitionName:allowedTransitionList) {
    		if (allowedTransitionName.startsWith(currentTransitionName)) {
    			result = allowedTransitionName;
    			break; // we found a mapping
    		}
    	}

    	return result;
    }
    
    /*
     * Handle Update (PUT)
     */

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
    	String transitionToFollow = null;
    	
        DocumentModel docModel = wrapDoc.getWrappedObject();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
    	
    	try {
    		TransitionDef transitionDef = (TransitionDef)this.getServiceContext().getProperty(WorkflowClient.TRANSITION_ID);
    		transitionToFollow = getQualifiedTransitionName(wrapDoc, transitionDef);
	        docModel.followTransition(transitionToFollow);
    	} catch (Exception e) {
    		String msg = "Unable to follow workflow transition to state = "
    				+ transitionToFollow;
    		logger.error(msg, e);
    		ClientException ce = new ClientException("Unable to follow workflow transition: " + transitionToFollow);
    		throw ce;
    	}
    }
}

