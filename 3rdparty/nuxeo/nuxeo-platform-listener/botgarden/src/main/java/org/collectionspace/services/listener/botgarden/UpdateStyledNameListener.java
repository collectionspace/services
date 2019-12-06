package org.collectionspace.services.listener.botgarden;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import org.collectionspace.services.batch.BatchResource;
import org.collectionspace.services.batch.nuxeo.FormatVoucherNameBatchJob;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.loanout.nuxeo.LoanoutBotGardenConstants;
import org.collectionspace.services.loanout.nuxeo.LoanoutConstants;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateStyledNameListener extends AbstractCSEventSyncListenerImpl {
	public static final String RUN_AFTER_MODIFIED_PROPERTY = "UpdateStyledNameListener.RUN_AFTER_MODIFIED";
	static final Log logger = LogFactory.getLog(UpdateStyledNameListener.class);

    @Override
	public boolean shouldHandleEvent(Event event) {
		EventContext ec = event.getContext();

		if (ec instanceof DocumentEventContext) {
			DocumentEventContext context = (DocumentEventContext) ec;
			DocumentModel doc = context.getSourceDocument();

			logger.debug("docType=" + doc.getType());

			if (doc.getType().startsWith(LoanoutConstants.NUXEO_DOCTYPE) &&
					!doc.isVersion() &&
					!doc.isProxy() &&
					!doc.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				return true;
			}
		}
		
		return false;
    }
    
	@Override
	public void handleCSEvent(Event event) {
		EventContext ec = event.getContext();
		DocumentEventContext context = (DocumentEventContext) ec;
		DocumentModel doc = context.getSourceDocument();

		logger.debug("docType=" + doc.getType());

		if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
			DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);

			String previousLabelRequested = (String) previousDoc.getProperty(LoanoutBotGardenConstants.LABEL_REQUESTED_SCHEMA_NAME,
					LoanoutBotGardenConstants.LABEL_REQUESTED_FIELD_NAME);
			String labelRequested = (String) doc.getProperty(LoanoutBotGardenConstants.LABEL_REQUESTED_SCHEMA_NAME,
					LoanoutBotGardenConstants.LABEL_REQUESTED_FIELD_NAME);

			logger.debug("previousLabelRequested=" + previousLabelRequested + " labelRequested=" + labelRequested);

			if ((previousLabelRequested == null || previousLabelRequested.equals(LoanoutBotGardenConstants.LABEL_REQUESTED_NO_VALUE)) &&
					labelRequested.equals(LoanoutBotGardenConstants.LABEL_REQUESTED_YES_VALUE)) {
				// The label request is changing from no to yes, so we should update the styled name.
				ec.setProperty(RUN_AFTER_MODIFIED_PROPERTY, true);
			}
		}
		else {
			boolean doUpdate = false;

			if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
				String labelRequested = (String) doc.getProperty(LoanoutBotGardenConstants.LABEL_REQUESTED_SCHEMA_NAME,
						LoanoutBotGardenConstants.LABEL_REQUESTED_FIELD_NAME);

				doUpdate = (labelRequested != null && labelRequested.equals(LoanoutBotGardenConstants.LABEL_REQUESTED_YES_VALUE));
			} else {
				doUpdate = ec.hasProperty(RUN_AFTER_MODIFIED_PROPERTY) && ((Boolean) ec.getProperty(RUN_AFTER_MODIFIED_PROPERTY));
			}

			if (doUpdate) {
				logger.debug("Updating styled name");

				String voucherCsid = doc.getName();

				try {
					createFormatter(context).formatVoucherName(voucherCsid);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private FormatVoucherNameBatchJob createFormatter(DocumentEventContext context) throws Exception {
		ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
		BatchResource batchResource = (BatchResource) resourceMap.get(BatchClient.SERVICE_NAME);
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext = batchResource.createServiceContext(batchResource.getServiceName());

		serviceContext.setCurrentRepositorySession(new CoreSessionWrapper(context.getCoreSession()));

		FormatVoucherNameBatchJob formatter = new FormatVoucherNameBatchJob();
		formatter.setServiceContext(serviceContext);
		formatter.setResourceMap(resourceMap);

		return formatter;
	}
	
	@Override
	public Log getLogger() {
		return logger;
	}
}