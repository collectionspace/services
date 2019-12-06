package org.collectionspace.services.listener.botgarden;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.batch.BatchResource;
import org.collectionspace.services.batch.nuxeo.UpdateRareFlagBatchJob;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectBotGardenConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;
import org.collectionspace.services.taxonomy.nuxeo.TaxonBotGardenConstants;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.collectionspace.services.taxonomy.nuxeo.TaxonomyAuthorityConstants;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * A listener that updates the rare flag on collectionobjects when collectionobjects
 * are created or modified, and when taxon records are modified.
 *
 * @see org.collectionspace.services.batch.nuxeo.UpdateRareFlagBatchJob
 * @author ray
 *
 */
public class UpdateRareFlagListener extends AbstractCSEventSyncListenerImpl {
	static final Log logger = LogFactory.getLog(UpdateRareFlagListener.class);

	public static final String PREVIOUS_TAXON_PROPERTY_NAME = "UpdateRareFlagListener.previousTaxon";
	public static final String PREVIOUS_HAS_RARE_CONSERVATION_CATEGORY_PROPERTY_NAME = "UpdateRareFlagListener.previousHasRareConservationCategory";

	private static final String[] CONSERVATION_CATEGORY_PATH_ELEMENTS = TaxonBotGardenConstants.CONSERVATION_CATEGORY_FIELD_NAME.split("/");
	private static final String PLANT_ATTRIBUTES_GROUP_LIST_FIELD_NAME = CONSERVATION_CATEGORY_PATH_ELEMENTS[0];
	private static final String CONSERVATION_CATEGORY_FIELD_NAME = CONSERVATION_CATEGORY_PATH_ELEMENTS[2];

    @Override
	public boolean shouldHandleEvent(Event event) {
    	return event.getContext() instanceof DocumentEventContext;
    }
    
	@Override
	public void handleCSEvent(Event event) {
		EventContext ec = event.getContext();
		DocumentEventContext context = (DocumentEventContext) ec;
		DocumentModel doc = context.getSourceDocument();
		String docType = doc.getType();

		logger.debug("docType=" + docType);

		if (docType.startsWith(CollectionObjectConstants.NUXEO_DOCTYPE) &&
				!doc.isVersion() &&
				!doc.isProxy() &&
				!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {

			if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
				// Stash the previous primary taxonomic ident, so it can be retrieved in the documentModified handler.

				DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
				String previousTaxon = (String) previousDoc.getProperty(CollectionObjectBotGardenConstants.TAXON_SCHEMA_NAME,
						CollectionObjectBotGardenConstants.PRIMARY_TAXON_FIELD_NAME);

				context.setProperty(PREVIOUS_TAXON_PROPERTY_NAME, previousTaxon);
			}
			else {
				boolean updateRequired = false;

				if (event.getName().equals(DocumentEventTypes.DOCUMENT_UPDATED)) {
					// A collectionobject was modified. As an optimization, check if the primary taxonomic determination
					// of the collectionobject has changed. We only need to update the rare flag if it has.

					String previousTaxon = (String) context.getProperty(PREVIOUS_TAXON_PROPERTY_NAME);
					String currentTaxon = (String) doc.getProperty(CollectionObjectBotGardenConstants.TAXON_SCHEMA_NAME,
							CollectionObjectBotGardenConstants.PRIMARY_TAXON_FIELD_NAME);

					if (previousTaxon == null) {
						previousTaxon = "";
					}

					if (currentTaxon == null) {
						currentTaxon = "";
					}

					if (previousTaxon.equals(currentTaxon)) {
						logger.debug("update not required: previousTaxon=" + previousTaxon + " currentTaxon=" + currentTaxon);
					}
					else {
						logger.debug("update required: previousTaxon=" + previousTaxon + " currentTaxon=" + currentTaxon);
						updateRequired = true;
					}
				}
				else if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
					// A collectionobject was created. Always update the rare flag.

					updateRequired = true;
				}

				if (updateRequired) {
					String collectionObjectCsid = doc.getName();

					try {
						InvocationResults results = createUpdater(context).updateRareFlag(collectionObjectCsid);

						logger.debug("updateRareFlag complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		else if (docType.startsWith(TaxonConstants.NUXEO_DOCTYPE) &&
				!docType.startsWith(TaxonomyAuthorityConstants.NUXEO_DOCTYPE) &&
				!doc.isVersion() &&
				!doc.isProxy() &&
				!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {

			if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
				// Stash whether there was previously a non-empty conservation category, so it can be retrieved in the documentModified handler.

				DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
				boolean previousHasRareConservationCategory = hasRareConservationCategory(previousDoc);

				context.setProperty(PREVIOUS_HAS_RARE_CONSERVATION_CATEGORY_PROPERTY_NAME, new Boolean(previousHasRareConservationCategory));
			}
			else {
				boolean updateRequired = false;

				if (event.getName().equals(DocumentEventTypes.DOCUMENT_UPDATED)) {
					// A taxon record was modified. As an optimization, check if there is now a rare
					// conservation category when there wasn't before, or vice versa. We only need to update
					// the rare flags of referencing collectionobjects if there was a change.

					boolean previousHasRareConservationCategory = (Boolean) context.getProperty(PREVIOUS_HAS_RARE_CONSERVATION_CATEGORY_PROPERTY_NAME);
					boolean currentHasRareConservationCategory = hasRareConservationCategory(doc);

					if (previousHasRareConservationCategory == currentHasRareConservationCategory) {
						logger.debug("update not required: previousHasRareConservationCategory=" + previousHasRareConservationCategory +
								" currentHasRareConservationCategory=" + currentHasRareConservationCategory);
					}
					else {
						logger.debug("update required: previousHasRareConservationCategory=" + previousHasRareConservationCategory +
								" currentHasRareConservationCategory=" + currentHasRareConservationCategory);
						updateRequired = true;
					}
				}

				if (updateRequired) {
					String taxonCsid = doc.getName();
					String vocabularyCsid = (String) doc.getProperty(TaxonConstants.IN_AUTHORITY_SCHEMA_NAME, TaxonConstants.IN_AUTHORITY_FIELD_NAME);

					try {
						InvocationResults results = createUpdater(context).updateReferencingRareFlags(taxonCsid, vocabularyCsid);

						logger.debug("updateReferencingRareFlags complete: numAffected=" + results.getNumAffected() + " userNote=" + results.getUserNote());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
	}

	private boolean hasRareConservationCategory(DocumentModel doc) {
		List<Map<String, Object>> plantAttributesGroupList = (List<Map<String, Object>>) doc.getProperty(TaxonBotGardenConstants.CONSERVATION_CATEGORY_SCHEMA_NAME,
				PLANT_ATTRIBUTES_GROUP_LIST_FIELD_NAME);
		boolean hasRareConservationCategory = false;

		// UCBG-369: Changing this so that it only checks the primary conservation category.

		if (plantAttributesGroupList.size() > 0) {
			Map<String, Object> plantAttributesGroup = plantAttributesGroupList.get(0);
			String conservationCategory = (String) plantAttributesGroup.get(CONSERVATION_CATEGORY_FIELD_NAME);

			if (UpdateRareFlagBatchJob.isRare(conservationCategory)) {
				hasRareConservationCategory = true;
			}
		}

//		for (Map<String, Object> plantAttributesGroup : plantAttributesGroupList) {
//			String conservationCategory = (String) plantAttributesGroup.get(CONSERVATION_CATEGORY_FIELD_NAME);
//
//			if (UpdateRareFlagBatchJob.isRare(conservationCategory)) {
//				hasRareConservationCategory = true;
//				break;
//			}
//		}

		return hasRareConservationCategory;
	}

	private UpdateRareFlagBatchJob createUpdater(DocumentEventContext context) throws Exception {
		ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
		BatchResource batchResource = (BatchResource) resourceMap.get(BatchClient.SERVICE_NAME);
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext = batchResource.createServiceContext(batchResource.getServiceName());

		serviceContext.setCurrentRepositorySession(new CoreSessionWrapper(context.getCoreSession()));

		UpdateRareFlagBatchJob updater = new UpdateRareFlagBatchJob();
		updater.setServiceContext(serviceContext);
		updater.setResourceMap(resourceMap);

		return updater;
	}
	
	@Override
	public Log getLogger() {
		return logger;
	}
}
