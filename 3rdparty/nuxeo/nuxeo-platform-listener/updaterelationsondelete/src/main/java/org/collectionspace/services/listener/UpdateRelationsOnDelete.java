package org.collectionspace.services.listener;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateRelationsOnDelete extends AbstractCSEventSyncListenerImpl {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    final Log logger = LogFactory.getLog(UpdateRelationsOnDelete.class);
    
    // FIXME: Get these constant values from external sources rather than redeclaring here
    final static String RELATION_DOCTYPE = "Relation";
    final static String RELATIONS_COMMON_SUBJECT_CSID_FIELD = "relations_common:subjectCsid";
    final static String RELATIONS_COMMON_OBJECT_CSID_FIELD = "relations_common:objectCsid";

    @Override
	public boolean shouldHandleEvent(Event event) {
        EventContext eventContext = event.getContext();

        // Event must be a soft-delete event
        if (isDocumentSoftDeletedEvent(eventContext)) {
        	return true;
        }
        
        // Exclude soft deletion events involving Relation records themselves
        // from handling by this event handler.
        DocumentEventContext docContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docContext.getSourceDocument();
        if (docModel != null && docModel.getType().startsWith(RELATION_DOCTYPE)) {
            return false;
        }
        
        return false;
    }
    
    @Override
    public void handleCSEvent(Event event) {        
        EventContext eventContext = event.getContext();
        DocumentEventContext docContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docContext.getSourceDocument();
        
        // Retrieve a list of relation records, where the soft deleted
        // document provided in the context of the current event is
        // either the subject or object of any relation        
        String csid = docModel.getName();
        String queryString;
        try {
            queryString =
                String.format("SELECT * FROM Relation WHERE ecm:isProxy = 0 AND (%1$s='%3$s' OR %2$s='%3$s')",
                RELATIONS_COMMON_SUBJECT_CSID_FIELD, RELATIONS_COMMON_OBJECT_CSID_FIELD, csid);
            logger.trace("Query string=" + queryString);
        } catch (IllegalFormatException ife) {
            logger.warn("Construction of formatted query string failed: ", ife);
            logger.warn("Actions in this event listener will NOT be performed, as a result of a previous Exception.");
            return;
        }
        
        // Create a filter to exclude from the list results any records
        // that have already been soft deleted or are locked
        List<String> workflowStatesToFilter = new ArrayList<String>();
        workflowStatesToFilter.add(WorkflowClient.WORKFLOWSTATE_DELETED);
        workflowStatesToFilter.add(WorkflowClient.WORKFLOWSTATE_LOCKED);
        workflowStatesToFilter.add(WorkflowClient.WORKFLOWSTATE_LOCKED_DELETED);
        workflowStatesToFilter.add(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED);
        
        LifeCycleFilter workflowStateFilter = new LifeCycleFilter(null, workflowStatesToFilter);
        
        // Perform the filtered query
        CoreSessionInterface session = new CoreSessionWrapper(docModel.getCoreSession());
        DocumentModelList matchingDocuments;
        try {
            matchingDocuments = session.query(queryString.toString(), workflowStateFilter);
        } catch (DocumentException ce) {
            logger.error("Error attempting to retrieve relation records where "
                    + "record of type '" + docModel.getType() + "' with CSID " + csid
                    + " is the subject or object of any relation: " + ce.getMessage());
            return;
        }

        // Cycle through the list results, soft deleting each matching relation record
        logger.info("Attempting to soft delete " + matchingDocuments.size() + " relation records pertaining to a soft deleted record.");
        for (DocumentModel doc : matchingDocuments) {
            doc.followTransition(WorkflowClient.WORKFLOWTRANSITION_DELETE);
        }
    }

    @Override
    public Log getLogger() {
    	return this.logger;
    }
}
