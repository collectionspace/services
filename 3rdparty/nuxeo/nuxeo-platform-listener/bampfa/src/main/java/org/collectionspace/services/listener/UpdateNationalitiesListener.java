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
 * This listener updates the collectionobjects_bampfa:nationalities field whenever either 1) A new bampfaObjectProductionPerson is
 * added to a collection object record, or 2) A persons authority is updated, and there are changes to the persons_common:nationalities
 * field. In the first case, it simply fetches the nationalities belonging to the persons found in the collectionobjects_bampfa:bampfaObjectProductionPerson 
 * field. In the second case, a batch job is run in order to update any collection objects records that are affected by changes to 
 * the persons_common:nationalities field.
 *
 * @author Cesar Villalobos
 */
public class UpdateNationalitiesListener implements EventListener {

    private final static Log logger = LogFactory.getLog(UpdateNationalitiesListener.class);
    private final static String NO_FURTHER_PROCESSING_MESSAGE =
            "This event listener will not continue processing this event ...";

    private final static String COLLECTIONOBJECT_DOCTYPE = "CollectionObject";
    private final static String COLLECTIONOBJECTS_BAMPFA_SCHEMA = "collectionobjects_bampfa";

    private final static String PERSON_DOCTYPE = "Person";
    private final static String PERSONS_SCHEMA = "persons_common";
    private final static String NATIONALITIES_FIELD = "nationalities";

    public static final String PREVIOUS_NATIONALITIES_PROPERTY_NAME = "UpdateNationalitiesListener.prevousNationalities";



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

        // Check if the event involves a person authority, a collection object, or neither
        if (documentMatchesType(docModel, PERSON_DOCTYPE) &&
					!documentMatchesType(docModel, "Personauthority") &&
					!docModel.isVersion() &&
					!docModel.isProxy() &&
					!docModel.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
            
            if (logger.isTraceEnabled()) {
                logger.trace("The update involved a person authority record. Now checking if updating a collection object is required");
            }

            if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
                // If the document has just beeen created, it will definitely not be used by any collection object record
                if (logger.isTraceEnabled()) {
                    logger.trace("It is not necessary to update any collection objects. " + NO_FURTHER_PROCESSING_MESSAGE);
                }
                return;
            } else if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
                // Store the previous nationalities in order to retrieve them in the after the document is created. 

                DocumentModel previousDoc = (DocumentModel) docEventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
                ArrayList previousNationalities = (ArrayList) previousDoc.getProperty(PERSONS_SCHEMA, NATIONALITIES_FIELD);
                if (previousNationalities.size() == 0) {
                    return;
                }
                docEventContext.setProperty(PREVIOUS_NATIONALITIES_PROPERTY_NAME, previousNationalities);
            } else {
                List previousNationalities = (List) docEventContext.getProperty(PREVIOUS_NATIONALITIES_PROPERTY_NAME);
                List newNationalities = (List) docModel.getProperty(PERSONS_SCHEMA, NATIONALITIES_FIELD);
                List newNationalitiesCopy = newNationalities;
                
                if (previousNationalities == null || newNationalities == null || previousNationalities.size() == 0 || newNationalities.size() == 0) {
                    logger.trace("There are no nationalities involved in this record. No updates to any collection object required. " + NO_FURTHER_PROCESSING_MESSAGE);
                    return;
                }

                Collections.sort(previousNationalities);
                Collections.sort(newNationalitiesCopy);

                /*
                 * IMPORTANT: This following bit of code is trying to be smart and is short-circuiting the listener in the event
                 *          that there is an update to a persons record, but the nationalities have not changed. This is to avoid
                 *          useless updates to collectionobjects records. If a person is used by 30,000 records, we don't want 
                 *          all of these records to be updated every time. HOWEVER, in the event that the links between the two
                 *          records haven't been made, this piece of code will cause the links to fail to go through and update
                 *          collection objects records. If this is something that needs to be done, comment out the next 6 lines of code.
                 */    
                // if they are equal, we don't need to update the lists
                if (newNationalities.equals(previousNationalities)) {
                    logger.warn("The version of the Nationalities listener currently in use is the optimized version");
                    if (logger.isTraceEnabled()) {
                        logger.trace("There are no changes to the nationalities field in this record. No updates to any collection object required. " + NO_FURTHER_PROCESSING_MESSAGE);
                    }
                    return;
                }
                
                // Get a list of the nationalities that need to be deleted, and those that need to be added.
                Map<String, List> nationalitiesToUpdate = findNationalitiesToUpdate(previousNationalities, newNationalities);
                
                try {
                    String personCsid = (String) docModel.getName();
                    InvocationResults results = updateCollectionObjectsFromPerson(docEventContext).updateNationalitiesFromPerson(personCsid, nationalitiesToUpdate);
                    if (logger.isDebugEnabled()) {
                    logger.debug("updateParentAccessCode complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

        } else if (documentMatchesType(docModel, COLLECTIONOBJECT_DOCTYPE)) {
            if (event.getName().equals((DocumentEventTypes.DOCUMENT_CREATED))) {
                // If the document is created, we simply call saveDocument in order to trigger the beforeDocumentSaved event
                docModel.getCoreSession().saveDocument(docModel);
                return;
            }

            CoreSession coreSession = docEventContext.getCoreSession();
            List<String> nationalities = getNationalitiesFromPersonAuthority(docModel, coreSession);

            docModel.setProperty(COLLECTIONOBJECTS_BAMPFA_SCHEMA, NATIONALITIES_FIELD, nationalities);
            if (logger.isTraceEnabled()) {
                logger.trace("Updated collection object with csid=" + docModel.getName());
            }
            return;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("No persons or collection object record was involved.");
            }
            return;
        }
    }

    /**
     * In the case that one or more collection objects need to have their nationalities list updated, this
     * method is called to determine which fields need to be added, and which need to be deleted.
     * 
     * @param oldNationalities The nationalities found in the persons authority before it was altered.
     * @param newNationalities The nationalities found in the persons authority after it was altered.
     * @return A Map<String, List> containing the keys "add" and "del", each corresponding to a list of nationalities
     * that need to be either added or deleted.
     */
    public Map<String, List> findNationalitiesToUpdate(List oldNationalities, List newNationalities) {
        Map<String, List> nationalities =  new HashMap<String, List>();

        List<String> fieldsToDelete = new ArrayList<String>();
        List<String> fieldsToAdd = new ArrayList<String>();

        for (Object n : oldNationalities) {
            String nationality = (String) n;
            if (!newNationalities.contains(nationality)) {
                fieldsToDelete.add(nationality);
            }
        }

        for (Object n : newNationalities) {
            String nationality = (String) n;
            if (!oldNationalities.contains(nationality)) {
                fieldsToAdd.add(nationality);
            }
        }
        nationalities.put("add", fieldsToAdd);
        nationalities.put("del", fieldsToDelete);

        return nationalities;
    }

    /**
     * This method finds the nationalities related to all of the artists that are involved in the bampfaObjectProductionPersonGroupList.
     * 
     * @param docModel The current document model.
     * @param coreSession A session that allows us to retrieve the nationalities from person authority records.
     * 
     * @return A list of nationalities that are to be inserted into this collection object record.
     */
    public List<String> getNationalitiesFromPersonAuthority(DocumentModel docModel, CoreSession coreSession) {
        String fieldRequested = "bampfaObjectProductionPersonGroupList";
        List<Map<String, Object>> bampfaObjectProductionPersonGroupList = (List<Map<String, Object>>) docModel.getProperty(COLLECTIONOBJECTS_BAMPFA_SCHEMA, fieldRequested);

        List<String> allNationalities = new ArrayList<String>();

        for (Map<String, Object> bampfaObjectProductionGroup : bampfaObjectProductionPersonGroupList) {
            String currRefName = (String) bampfaObjectProductionGroup.get("bampfaObjectProductionPerson");

            String query = String.format(
                            "SELECT * FROM %1$s WHERE %2$s:refName=\"%3$s\"", PERSON_DOCTYPE, PERSONS_SCHEMA, currRefName); 

            if (coreSession.query(query).size() == 0) {
                continue;
            }
            List<String> nationalities = (List) coreSession.query(query).get(0).getProperty(PERSONS_SCHEMA, NATIONALITIES_FIELD);
            
            for (Object n : nationalities) {
                String nationality = (String) n;
                if (n != null && !allNationalities.contains(nationality)) {
                    allNationalities.add(nationality);
                }
            }
        }

        return allNationalities;

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
