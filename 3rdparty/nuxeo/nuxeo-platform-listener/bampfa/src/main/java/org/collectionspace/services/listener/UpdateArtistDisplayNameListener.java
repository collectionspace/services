package org.collectionspace.services.listener.bampfa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.batch.BatchResource;
import org.collectionspace.services.batch.nuxeo.UpdateObjectFromPersonsAuthorityBatchJob;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.event.EventListener;


/**
 * This listener updates the collectionobjects_bampfa:computedArtistDisplayName field whenever a persons authority is updated, and there are changes to the persons_common:displayName
 *
 * @author Cesar Villalobos
 */
public class UpdateArtistDisplayNameListener implements EventListener {

    private final static Log logger = LogFactory.getLog(UpdateArtistDisplayNameListener.class);
    private final static String NO_FURTHER_PROCESSING_MESSAGE =
            "This event listener will not continue processing this event ...";

    private final static String COLLECTIONOBJECT_DOCTYPE = "CollectionObject";
    private final static String COLLECTIONOBJECTS_BAMPFA_SCHEMA = "collectionobjects_bampfa";

    private final static String PERSON_DOCTYPE = "Person";
    private final static String PERSONS_SCHEMA = "persons_common";
    private final static String ARTISTDISPLAYNAME_FIELD = "computedartistname";

    public static final String PREVIOUS_DISPLAYNAME_PROPERTY = "UpdateArtistDisplayNameListener.previousName";

    public static final String DISPLAYNAME_FIELD = "personTermGroupList";

    @Override
    public void handleEvent(Event event) throws ClientException {
        logger.trace("In handleEvent in update nationalities listener.");

        EventContext eventContext = event.getContext();
        if (eventContext == null || !(eventContext instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();
        String docType = docModel.getType();

        // Check if the event involves a person authority
        if (documentMatchesType(docModel, PERSON_DOCTYPE) &&
					!documentMatchesType(docModel, "Personauthority") &&
					!docModel.isVersion() &&
					!docModel.isProxy() &&
					!docModel.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            
            if (logger.isTraceEnabled()) {
                logger.trace("The update involved a person authority record. Now checking if updating a collection object is required");
            }

            if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
                // Store the previous display name in order to retrieve them in the after the document is created. 

                DocumentModel previousDoc = (DocumentModel) docEventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
                // ArrayList previousDisplayName = (ArrayList) previousDoc.getProperty(PERSONS_SCHEMA, DISPLAYNAME_FIELD);
                String previousDisplayName = (String) previousDoc.getProperty(PERSONS_SCHEMA, DISPLAYNAME_FIELD);
                
                docEventContext.setProperty(PREVIOUS_DISPLAYNAME_PROPERTY, previousDisplayName);
            } else {
                String previousDisplayName = (String) docEventContext.getProperty(PREVIOUS_DISPLAYNAME_PROPERTY);
                String newDisplayName = (String) docModel.getProperty(PERSONS_SCHEMA, DISPLAYNAME_FIELD);

                /*
                 * IMPORTANT: This following bit of code is trying to be smart and is short-circuiting the listener in the event
                 *          that there is an update to a persons record, but the displayName has not changed. This is to avoid
                 *          useless updates to collectionobjects records. If a person is used by 30,000 records.
                 */
                
                // if they are equal, we don't need to update the lists
                if (previousDisplayName.equals(newDisplayName)) {
                    logger.warn("The version of the Artist Display Name listener currently in use is the optimized version");

                    if (logger.isTraceEnabled()) {
                        logger.trace("There are no changes to the display field in this record. No updates to any collection object required. " + NO_FURTHER_PROCESSING_MESSAGE);
                    }
                    return;
                }
              
                
                try {
                    String personCsid = (String) docModel.getName();
                    // InvocationResults results = updateCollectionObjectsFromPerson(docEventContext).updateNationalitiesFromPerson(personCsid, nationalitiesToUpdate);
                    InvocationResults results = updateCollectionObjectsFromPerson(docEventContext).UpdateComputedDisplayName(personCsid, newDisplayName);
                    if (logger.isDebugEnabled()) {
                    logger.debug("updateParentAccessCode complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }  else {
            if (logger.isTraceEnabled()) {
                logger.trace("No persons record was involved.");
            }
            return;
        }
    }

    protected static boolean documentMatchesType(DocumentModel docModel, String docType) {
        if (docModel == null || Tools.isBlank(docType)) {
            return false;
        }
        if (docModel.getType().startsWith(docType)) {
            return true;
        }
        return false;
    }
    
    /**
     * Creates an UpdateObjectFromPersonsAuthorityBatchJob that can be called to update any collection objects
     * affected by adding/removing a nationality from a person record.
     * 
     * @param context The document event context associated with this event
     * @return An UpdateObjectFromPersonsAuthorityBatchJob object that can be used to propagate changes
     * to collection object records.
     */
    private UpdateObjectFromPersonsAuthorityBatchJob updateCollectionObjectsFromPerson(DocumentEventContext context) throws Exception {

        ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
		BatchResource batchResource = (BatchResource) resourceMap.get(BatchClient.SERVICE_NAME);
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext = batchResource.createServiceContext(batchResource.getServiceName());

		serviceContext.setCurrentRepositorySession(new CoreSessionWrapper(context.getCoreSession()));

        UpdateObjectFromPersonsAuthorityBatchJob updater = new UpdateObjectFromPersonsAuthorityBatchJob();

		updater.setServiceContext(serviceContext);
        updater.setResourceMap(resourceMap);
        
        return updater;
    }

}
