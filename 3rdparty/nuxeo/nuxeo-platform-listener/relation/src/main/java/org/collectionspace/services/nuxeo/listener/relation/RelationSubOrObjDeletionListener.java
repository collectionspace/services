package org.collectionspace.services.nuxeo.listener.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.common.document.DocumentFilter;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class RelationSubOrObjDeletionListener implements EventListener {

    // FIXME: Consider adding the following constant to
    // org.collectionspace.services.common.workflow.jaxb.WorkflowJAXBSchema
    // and referencing it from there.
    private static final String WORKFLOWTRANSITION_TO = "to";
    // FIXME: Consider substituting existing constant WorkflowClient.WORKFLOWSTATE_DELETED
    private static final String WORKFLOWSTATE_DELETED = "deleted";
    // FIXME: Consider substituting existing constant WorkflowClient.WORKFLOWSTATE_LOCKED
    private static final String WORKFLOWSTATE_LOCKED = "locked";
    // FIXME: Consider substituting existing constant WorkflowClient.WORKFLOWTRANSITION_DELETE
    private static final String WORKFLOWTRANSITION_DELETE = "delete";

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    final Log logger = LogFactory.getLog(RelationSubOrObjDeletionListener.class);

    public void handleEvent(Event event) throws ClientException {
        logger.info("In handleEvent in RelationSubOrObjDeletionListener ...");

        EventContext eventContext = event.getContext();

        if (isDocumentSoftDeletedEvent(eventContext)) {
            
            DocumentEventContext docContext = (DocumentEventContext) eventContext;
            DocumentModel docModel = docContext.getSourceDocument();
            
            // Retrieve a list of relation records, where the soft deleted
            // document provided in the context of the current event is
            // either the subject or object of any relation
            
            // Build a query string
            String csid = docModel.getName();
            StringBuilder queryString = new StringBuilder("");
            queryString.append("SELECT * FROM Relation WHERE ");
            // FIXME: Obtain and add tenant ID to the query here
            // queryString.append("collectionspace_core:tenantId = 1 ");
            // queryString.append(" AND ");
            // queryString.append("ecm:currentLifeCycleState <> 'deleted' ");
            queryString.append("ecm:isProxy = 0 ");
            queryString.append(" AND ");
            queryString.append("(");
            queryString.append("relations_common:subjectCsid = ");
            queryString.append("'");
            queryString.append(csid);
            queryString.append("'");
            queryString.append(" OR ");
            queryString.append("relations_common:objectCsid = ");
            queryString.append("'");
            queryString.append(csid);
            queryString.append("'");
            queryString.append(")");
                        
            // Create a filter to exclude from the list results any records
            // that have already been soft deleted or are locked
            List<String> workflowStatesToFilter = new ArrayList<String>();
            workflowStatesToFilter.add(WORKFLOWSTATE_DELETED);
            workflowStatesToFilter.add(WORKFLOWSTATE_LOCKED);
            LifeCycleFilter workflowStateFilter = new LifeCycleFilter(null, workflowStatesToFilter);
            
            // Perform the filtered query
            CoreSession session = docModel.getCoreSession();
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
            logger.trace("Attempting to soft delete " + matchingDocuments.size() + " relation records.");
            for (DocumentModel doc : matchingDocuments) {
                doc.followTransition(WORKFLOWTRANSITION_DELETE);
            }

        }

    }

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
            if (eventContext.getProperties().containsKey(WORKFLOWTRANSITION_TO)
                    && eventContext.getProperties().get(WORKFLOWTRANSITION_TO).equals(WORKFLOWSTATE_DELETED)) {
                isSoftDeletedEvent = true;
            }
        }
        return isSoftDeletedEvent;
    }
}
