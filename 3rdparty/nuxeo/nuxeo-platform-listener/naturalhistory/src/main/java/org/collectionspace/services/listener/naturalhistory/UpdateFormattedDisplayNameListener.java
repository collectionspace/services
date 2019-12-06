package org.collectionspace.services.listener.naturalhistory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.TaxonFormatter;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;
import org.collectionspace.services.taxonomy.nuxeo.TaxonBotGardenConstants;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.collectionspace.services.taxonomy.nuxeo.TaxonomyAuthorityConstants;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateFormattedDisplayNameListener extends AbstractCSEventSyncListenerImpl {
	public static final String RUN_AFTER_MODIFIED_PROPERTY = "UpdateFormattedDisplayNameListener.RUN_AFTER_MODIFIED";
	static final Log logger = LogFactory.getLog(UpdateFormattedDisplayNameListener.class);

	private static final String[] DISPLAY_NAME_PATH_ELEMENTS = TaxonConstants.DISPLAY_NAME_FIELD_NAME.split("/");
	private static final String TERM_GROUP_LIST_FIELD_NAME = DISPLAY_NAME_PATH_ELEMENTS[0];
	private static final String DISPLAY_NAME_FIELD_NAME = DISPLAY_NAME_PATH_ELEMENTS[2];

	private static final String[] FORMATTED_DISPLAY_NAME_PATH_ELEMENTS = TaxonConstants.FORMATTED_DISPLAY_NAME_FIELD_NAME.split("/");
	private static final String FORMATTED_DISPLAY_NAME_FIELD_NAME = FORMATTED_DISPLAY_NAME_PATH_ELEMENTS[2];


    @Override
	public boolean shouldHandleEvent(Event event) {
		EventContext ec = event.getContext();

		if (ec instanceof DocumentEventContext) {
			DocumentEventContext context = (DocumentEventContext) ec;
			DocumentModel doc = context.getSourceDocument();
			String docType = doc.getType();

			logger.debug("docType=" + docType);

			if (docType.startsWith(TaxonConstants.NUXEO_DOCTYPE) &&
					!docType.startsWith(TaxonomyAuthorityConstants.NUXEO_DOCTYPE) &&
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
		
		String docType = doc.getType();
		logger.debug("docType=" + docType);

		String refName = (String) doc.getProperty(TaxonConstants.REFNAME_SCHEMA_NAME, TaxonConstants.REFNAME_FIELD_NAME);
		RefName.AuthorityItem item = RefName.AuthorityItem.parse(refName);
		String parentShortId = item.getParentShortIdentifier();

		logger.debug("parentShortId=" + parentShortId);

		if (!parentShortId.equals(TaxonBotGardenConstants.COMMON_VOCABULARY_SHORTID)) {
			if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED)) {
				// Save the document, to get the BEFORE_DOC_UPDATE branch to run.
				doc.getCoreSession().saveDocument(doc);
			}
			else if (event.getName().equals(DocumentEventTypes.BEFORE_DOC_UPDATE)) {
				DocumentModel previousDoc = (DocumentModel) context.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);

				updateFormattedDisplayNames(doc, previousDoc);
			}
		}
	}

	private void updateFormattedDisplayNames(DocumentModel doc, DocumentModel previousDoc) {
		//Set<String> previousDisplayNames = getDisplayNames(previousDoc);
		TaxonFormatter formatter = new TaxonFormatter();
		List<Map<String, Object>> termGroupList = (List<Map<String, Object>>) doc.getProperty(TaxonConstants.DISPLAY_NAME_SCHEMA_NAME, TERM_GROUP_LIST_FIELD_NAME);

		for (Map<String, Object> termGroup : termGroupList) {
			String displayName = (String) termGroup.get(DISPLAY_NAME_FIELD_NAME);
			String formattedDisplayName = (String) termGroup.get(FORMATTED_DISPLAY_NAME_FIELD_NAME);

			if (StringUtils.isBlank(formattedDisplayName)) {
				formattedDisplayName = "";

				if (StringUtils.isNotBlank(displayName)) {
					formattedDisplayName = formatter.format(displayName);
				}

				termGroup.put(FORMATTED_DISPLAY_NAME_FIELD_NAME, formattedDisplayName);
			}
		}

		Map<String, Object> updateMap = new HashMap<String, Object>();
		updateMap.put(TERM_GROUP_LIST_FIELD_NAME, termGroupList);

		doc.setProperties(TaxonConstants.DISPLAY_NAME_SCHEMA_NAME, updateMap);
	}

	/*
	private Set<String> getDisplayNames(DocumentModel doc) throws ClientException {
		Set<String> displayNames = new HashSet<String>();
		List<Map<String, Object>> termGroupList = (List<Map<String, Object>>) doc.getProperty(TaxonConstants.DISPLAY_NAME_SCHEMA_NAME, TERM_GROUP_LIST_FIELD_NAME);

		for (Map<String, Object> termGroup : termGroupList) {
			String displayName = (String) termGroup.get(DISPLAY_NAME_FIELD_NAME);

			if (displayName != null) {
				displayNames.add(displayName);
			}
		}

		return displayNames;
	}
	*/
	
	@Override
	public Log getLogger() {
		return logger;
	}
}