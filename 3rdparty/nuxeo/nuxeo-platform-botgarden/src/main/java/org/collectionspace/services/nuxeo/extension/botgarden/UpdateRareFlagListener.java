package org.collectionspace.services.nuxeo.extension.botgarden;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.batch.nuxeo.UpdateRareFlagBatchJob;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateRareFlagListener implements EventListener {
	final Log logger = LogFactory.getLog(UpdateRareFlagListener.class);

	public static final String UPDATE_REQUIRED_PROPERTY_NAME = "UpdateRareFlagListener.UPDATE_REQUIRED";
	
	private static final String[] CONSERVATION_CATEGORY_PATH_ELEMENTS = TaxonConstants.CONSERVATION_CATEGORY_FIELD_NAME.split("/");
	private static final String PLANT_ATTRIBUTES_GROUP_LIST_FIELD_NAME = CONSERVATION_CATEGORY_PATH_ELEMENTS[0];
	private static final String CONSERVATION_CATEGORY_FIELD_NAME = CONSERVATION_CATEGORY_PATH_ELEMENTS[2];

	/* 
	 * Set the rare flag on collectionobjects when the primary taxonomic determination changes.
	 */
	public void handleEvent(Event event) throws ClientException {
		EventContext ec = event.getContext();

		if (ec instanceof DocumentEventContext) {
			DocumentEventContext context = (DocumentEventContext) ec;
			DocumentModel doc = context.getSourceDocument();

			logger.debug("docType=" + doc.getType());

			if (doc.getType().startsWith(CollectionObjectConstants.NUXEO_DOCTYPE) &&
					!doc.isVersion() && 
					!doc.isProxy() && 
					!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				
				/*
				 * As an optimization, check if the primary taxonomic determination of the collectionobject has
				 * changed. We only need to update the rare flag if it has.
				 */
				if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {					
					DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);	            	
					String previousTaxon = (String) previousDoc.getProperty(CollectionObjectConstants.TAXON_SCHEMA_NAME, CollectionObjectConstants.PRIMARY_TAXON_FIELD_NAME);
					String currentTaxon = (String) doc.getProperty(CollectionObjectConstants.TAXON_SCHEMA_NAME, CollectionObjectConstants.PRIMARY_TAXON_FIELD_NAME);

					if (previousTaxon.equals(currentTaxon)) {
						logger.debug("update not required: previousTaxon=" + previousTaxon + " currentTaxon=" + currentTaxon);
					}
					else {
						logger.debug("update required: previousTaxon=" + previousTaxon + " currentTaxon=" + currentTaxon);
						
						event.getContext().setProperty(UPDATE_REQUIRED_PROPERTY_NAME, true);
					}
				}
				/*
				 * In the documentModified event, check if we need to update the rare flag, using the 
				 * property that was set in the beforeDocumentModification handler.
				 */
				else if (event.getContext().hasProperty(UPDATE_REQUIRED_PROPERTY_NAME)) {				
					String collectionObjectCsid = doc.getName();
		
					try {
						InvocationResults results = createUpdater().updateRareFlag(collectionObjectCsid);
		
						logger.debug("updateRareFlag complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			else if (doc.getType().startsWith(TaxonConstants.NUXEO_DOCTYPE) &&
					!doc.isVersion() && 
					!doc.isProxy() && 
					!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				/*
				 * As an optimization, check if there is now a non-empty conservation category when
				 * there wasn't before, or vice versa. We only need to update the rare flags of
				 * referencing collectionobjects if there was a change.
				 */
				if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {					
					DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);	            	
					boolean previousHasNonEmptyConservationCategory = hasNonEmptyConservationCategory(previousDoc);
					boolean currentHasNonEmptyConservationCategory = hasNonEmptyConservationCategory(doc);
					
					if (previousHasNonEmptyConservationCategory == currentHasNonEmptyConservationCategory) {
						logger.debug("update not required: previousHasNonEmptyConservationCategory=" + previousHasNonEmptyConservationCategory + " currentHasNonEmptyConservationCategory=" + currentHasNonEmptyConservationCategory);						
					}
					else {
						logger.debug("update required: previousHasNonEmptyConservationCategory=" + previousHasNonEmptyConservationCategory + " currentHasNonEmptyConservationCategory=" + currentHasNonEmptyConservationCategory);						

						event.getContext().setProperty(UPDATE_REQUIRED_PROPERTY_NAME, true);
					}
				}
				/*
				 * In the documentModified event, check if we need to update the rare flag, using the 
				 * property that was set in the beforeDocumentModification handler.
				 */
				else if (event.getContext().hasProperty(UPDATE_REQUIRED_PROPERTY_NAME)) {
					String taxonCsid = doc.getName();
	
					try {
						InvocationResults results = createUpdater().updateReferencingRareFlags(taxonCsid);
	
						logger.debug("updateReferencingRareFlags complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	private boolean hasNonEmptyConservationCategory(DocumentModel doc) throws ClientException {
		List<Map<String, Object>> plantAttributesGroupList = (List<Map<String, Object>>) doc.getProperty(TaxonConstants.CONSERVATION_CATEGORY_SCHEMA_NAME, PLANT_ATTRIBUTES_GROUP_LIST_FIELD_NAME);
		boolean hasNonEmptyConservationCategory = false;

		for (Map<String, Object> plantAttributesGroup : plantAttributesGroupList) {
			String conservationCategory = (String) plantAttributesGroup.get(CONSERVATION_CATEGORY_FIELD_NAME);

			if (StringUtils.isNotEmpty(conservationCategory)) {
				hasNonEmptyConservationCategory = true;
				break;
			}
		}		
		
		return hasNonEmptyConservationCategory;
	}

	private UpdateRareFlagBatchJob createUpdater() {
		ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);

		UpdateRareFlagBatchJob updater = new UpdateRareFlagBatchJob();
		updater.setResourceMap(resourceMap);

		return updater;
	}
}