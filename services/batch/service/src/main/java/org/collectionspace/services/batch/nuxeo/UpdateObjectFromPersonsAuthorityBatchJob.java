package org.collectionspace.services.batch.nuxeo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;

import org.dom4j.DocumentException;
import java.net.URISyntaxException;

/**
 * This batch job updates the nationalities of all collection objects records that use the person record with personCsid.
 * The only current use case is when called by UpdateNationalitiesListener.java to update the above mentioned collection objects.
 *
 * @author Cesar Villalobos
 *
 */
public class UpdateObjectFromPersonsAuthorityBatchJob extends AbstractBatchJob {
    
    public UpdateObjectFromPersonsAuthorityBatchJob() {
		this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_GROUP));
    }
    
    @Override
    /** 
     * The only context during which this batch handler is called is through the UpdateNationalitiesListener, thus this
     * method is not implemented.
    */
    public void run() {
        return;
    }

    /**
     * This method finds and updates the nationalities field of any record that references the record with csid equal to personCsid.
     *
     * @param personCsid The persons authority record csid whose persons_common:nationalities field was changed. Used to find all collection objects affected
     * @param nationalitiesToUpdate A Map<String, List> containing the keys "add" and "del", each corresponding to a list of nationalities
     * that need to be either added or deleted.
     */
    public InvocationResults updateNationalitiesFromPerson(String personCsid, Map<String, List<String>> nationalitiesToUpdate) throws URISyntaxException, DocumentException, Exception {
        InvocationResults results = new InvocationResults();

        String sourceField = "collectionobjects_bampfa:bampfaObjectProductionPerson";
        String serviceName = "personauthorities";

        List<String> collectionObjectsList =  findReferencingCollectionObjects(serviceName, personCsid, sourceField);

        long numAffected = 0;

        // These are the CSIDs of all the documents that we need to update
        for (String csid : collectionObjectsList) {
            PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(csid);

            // Make sure you skip over soft deleted records
            String workflowState = getFieldValue(collectionObjectPayload, CollectionObjectConstants.WORKFLOW_STATE_SCHEMA_NAME, CollectionObjectConstants.WORKFLOW_STATE_FIELD_NAME);

			if (workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				continue;
			}

            numAffected++;
            List<String> oldNationalities = getFieldValues(collectionObjectPayload, "collectionobjects_bampfa", "nationalities/nationality");
            try {
                updateNationalities(oldNationalities, nationalitiesToUpdate, csid);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        results.setNumAffected(numAffected);
        return results;
    }

    /**
     * Helper method. This method compiles a list of the nationalities that need to be included in a given collection object and updates the document.
     * 
     * @param collectionObjNationalities A List containing the nationalities that are currently associated with the collection object record with csid equal to
     * objCsid.
     * @param nationalitiesToUpdate A Map<String, List> containing the keys "add" and "del", each corresponding to a list of nationalities
     * that need to be either added or deleted.
     * @param objCsid The csid of the collection object record that is to be updated.
     */
    public void updateNationalities(List<String> collectionObjNationalities, Map<String, List<String>> nationalitiesToUpdate, String objCsid) throws URISyntaxException {
        if (logger.isTraceEnabled()) {
            logger.trace("Updating collection object record with csid=" + objCsid);
        }

        // Assemble a list of what the new nationalities repeating field should look like for this collection object
        List<String> nationalitiesToAdd = nationalitiesToUpdate.get("add");
        List<String> nationalitiesToDelete = nationalitiesToUpdate.get("del");

        collectionObjNationalities.addAll(nationalitiesToAdd);
        collectionObjNationalities.removeAll(nationalitiesToDelete);
        
        if (logger.isTraceEnabled()) {
            logger.trace("Adding the following nationalities to the collection object=" + nationalitiesToAdd.toString() + 
                ". And removing the following nationalities from it=" 
                + nationalitiesToDelete.toString());
        }

        // So now collectionObjNationalities is the most updated version of what we need to have. Time to assemble them into an XML-friendly string
        String nationalitiesString = "";
        for (String n : collectionObjNationalities) {
            nationalitiesString += "<nationality>" + n + "</nationality>\n";
        }
        
        String nationalitiesGroupString  =  "<nationalities>\n" + 
                                            nationalitiesString + 
                                            "</nationalities>";

        String updatePayload = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<document name=\"collectionobjects\">" +
            "<ns2:collectionobjects_bampfa xmlns:ns2=\"http://collectionspace.org/services/collectionobject/local/bampfa\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            nationalitiesGroupString + 
            "</ns2:collectionobjects_bampfa>" +
            "</document>";
        
        logger.trace("Updating record with the following payload: \n" + updatePayload);

        // Perform the update.
        ResourceMap resourcemap = getResourceMap();
        NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resourcemap.get(CollectionObjectClient.SERVICE_NAME);

        byte[] responseBytes = collectionObjectResource.update(getServiceContext(), resourcemap, createUriInfo(), objCsid, updatePayload);

        if (logger.isDebugEnabled()) {
	        logger.debug(String.format("Batch resource: Resonse from collectionobject (cataloging record) update: %s", new String(responseBytes)));
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Computed current location value for CollectionObject nationalities was set to " + collectionObjNationalities.toString());

        }

    }

    public InvocationResults UpdateComputedDisplayName(String personCsid, String newDisplayName) throws URISyntaxException, DocumentException, Exception {
        InvocationResults results = new InvocationResults();

        String sourceField = "collectionobjects_bampfa:bampfaObjectProductionPerson";
        String serviceName = "personauthorities";

        List<String> collectionObjectsList =  findReferencingCollectionObjects(serviceName, personCsid, sourceField);

        long numAffected = 0;

        // These are the CSIDs of all the documents that we need to update
        for (String csid : collectionObjectsList) {
            PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(csid);

            // Make sure you skip over soft deleted records
            String workflowState = getFieldValue(collectionObjectPayload, CollectionObjectConstants.WORKFLOW_STATE_SCHEMA_NAME, CollectionObjectConstants.WORKFLOW_STATE_FIELD_NAME);

			if (workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				continue;
			}

            numAffected++;

            // List<String> oldNationalities = getFieldValues(collectionObjectPayload, "collectionobjects_bampfa", "nationalities/nationality");
            try {
                updateDisplayName(newDisplayName, csid);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        results.setNumAffected(numAffected);
        return results;
    }

    public void updateDisplayName(String displayName, String objCsid) throws URISyntaxException {
        if (logger.isTraceEnabled()) {
            logger.trace("Updating collection object record with csid=" + objCsid);
        }

        String updatePayload = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<document name=\"collectionobjects\">" +
            "<ns2:collectionobjects_bampfa xmlns:ns2=\"http://collectionspace.org/services/collectionobject/local/bampfa\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<computedArtistName>" +
            displayName +
            "</computedArtistName>" +
            "</ns2:collectionobjects_bampfa>" +
            "</document>";
        
        // Perform the update.
        ResourceMap resourcemap = getResourceMap();
        NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resourcemap.get(CollectionObjectClient.SERVICE_NAME);

        byte[] responseBytes = collectionObjectResource.update(getServiceContext(), resourcemap, createUriInfo(), objCsid, updatePayload);

        if (logger.isDebugEnabled()) {
	        logger.debug(String.format("Batch resource: Resonse from collectionobject (cataloging record) update: %s", new String(responseBytes)));
        }

    }
}



