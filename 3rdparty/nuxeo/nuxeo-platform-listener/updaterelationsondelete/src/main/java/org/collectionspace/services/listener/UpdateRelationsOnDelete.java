package org.collectionspace.services.listener;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateRelationsOnDelete implements EventListener {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    final Log logger = LogFactory.getLog(UpdateRelationsOnDelete.class);
    
    // FIXME: Get these constant values from external sources rather than redeclaring here
    final static String RELATION_DOCTYPE = "Relation";
    final static String RELATIONS_COMMON_SUBJECT_CSID_FIELD = "relations_common:subjectCsid";
    final static String RELATIONS_COMMON_OBJECT_CSID_FIELD = "relations_common:objectCsid";

    @Override
    public void handleEvent(Event event) throws ClientException {
        logger.trace("In handleEvent in UpdateRelationsOnDelete ...");
        
        EventContext eventContext = event.getContext();

        if (isDocumentSoftDeletedEvent(eventContext)) {
            
            logger.trace("A soft deletion event was received by UpdateRelationsOnDelete ...");
            
            DocumentEventContext docContext = (DocumentEventContext) eventContext;
            DocumentModel docModel = docContext.getSourceDocument();
            
            // Exclude soft deletion events involving Relation records themselves
            // from handling by this event handler.
            if (docModel != null && docModel.getType().startsWith(RELATION_DOCTYPE)) {
                return;
            }
  
            // Retrieve a list of relation records, where the soft deleted
            // document provided in the context of the current event is
            // either the subject or object of any relation
            
            // Build a query string
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
            } catch (ClientException ce) {
                logger.warn("Error attempting to retrieve relation records where "
                        + "record of type '" + docModel.getType() + "' with CSID " + csid
                        + " is the subject or object of any relation: " + ce.getMessage());
                throw ce;
            }

            // Cycle through the list results, soft deleting each matching relation record
            logger.info("Attempting to soft delete " + matchingDocuments.size() + " relation records pertaining to a soft deleted record.");
            for (DocumentModel doc : matchingDocuments) {
                doc.followTransition(WorkflowClient.WORKFLOWTRANSITION_DELETE);
            }

        }

    }
    
    // FIXME: Generic methods like the following might be split off
    // into an event utilities class. - ADR 2012-12-05

    /**
     * Identifies whether a supplied event concerns a document that has
     * been transitioned to the 'deleted' workflow state.
     * 
     * @param eventContext an event context
     * 
     * @return true if this event concerns a document that has
     * been transitioned to the 'deleted' workflow state.
     */
    private boolean isDocumentSoftDeletedEvent(EventContext eventContext) {
        boolean isSoftDeletedEvent = false;
        
        if (eventContext instanceof DocumentEventContext) {
            if (eventContext.getProperties().containsKey(WorkflowClient.WORKFLOWTRANSITION_TO)
                    &&
                (eventContext.getProperties().get(WorkflowClient.WORKFLOWTRANSITION_TO).equals(WorkflowClient.WORKFLOWSTATE_DELETED)
                		||
                eventContext.getProperties().get(WorkflowClient.WORKFLOWTRANSITION_TO).equals(WorkflowClient.WORKFLOWSTATE_LOCKED_DELETED))) {
                isSoftDeletedEvent = true;
            }
        }
        
        return isSoftDeletedEvent;
    }
}
