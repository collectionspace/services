package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.collectionspace.services.batch.BatchCommon;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityInfo;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.relation.RelationResource;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.relation.RelationsCommonList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A batch job that merges authority items. The single and list contexts are
 * supported.
 *
 * The merge target is a record into which one or more source records will be
 * merged. A merge source is a record that will be merged into the target, as
 * follows: Each term in a source record is added to the target as a non-
 * preferred term, if that term does not already exist in the target. If a term
 * in the source already exists in the target, each non-blank term field is
 * copied to the target, if that field is empty in the target. If the field is
 * non-empty in the target, and differs from the source field, a warning is
 * emitted and no action is taken. If a source is successfully merged into the
 * target, all references to the source are transferred to the target, and the
 * source record is soft-deleted.
 *
 * The context (singleCSID or listCSIDs of the batch invocation payload
 * specifies the source record(s).
 *
 * The following parameters are allowed:
 *
 * targetCSID: The csid of the target record. Only one target may be supplied.
 *
 * @author ray
 */
public class MergeAuthorityItemsBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(MergeAuthorityItemsBatchJob.class);

	public MergeAuthorityItemsBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
	}

	@Override
	public void run() {
		run(null);
	}

	@Override
	public void run(BatchCommon batchCommon) {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			String target = null;
			Set<String> sourceCsids = new LinkedHashSet<String>();
			String docType = this.getDocType();

			if (this.requestIsForInvocationModeSingle()) {
				String singleCsid = this.getSingleCsid();

				if (singleCsid != null) {
					sourceCsids.add(singleCsid);
				}
			} else if (this.requestIsForInvocationModeList()) {
				sourceCsids.addAll(this.getListCsids());
			}

			for (Param param : this.getParams()) {
				String key = param.getKey();

				// I don't want this batch job to appear in the UI, since it won't run successfully without parameters.
				// That means it can't be registered with any docType. But if the invocation payload contains a docType,
				// it will be checked against the null registered docType, and will fail. So docType should be passed as a
				// parameter instead.

				if (key.equals("docType")) {
					docType = param.getValue();
				}
				else if (key.equals("target")) {
					target = param.getValue();
				}
				else if (key.equals("targetCSID")) {
					target = param.getValue();
				}
				else if (key.equals("sourceCSID")) {
					sourceCsids.add(param.getValue());
				}
			}

			if (target == null || target.equals("")) {
				throw new Exception("a target or targetCSID parameter must be supplied");
			}

			if (sourceCsids.size() == 0) {
				throw new Exception("a source csid must be supplied");
			}

			InvocationResults results = merge(docType, target, sourceCsids);

			setResults(results);
			setCompletionStatus(STATUS_COMPLETE);
		}
		catch (Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}

	public InvocationResults merge(String docType, String target, String sourceCsid) throws URISyntaxException, DocumentException, Exception {
		return merge(docType, target, new LinkedHashSet<String>(Arrays.asList(sourceCsid)));
	}

	public InvocationResults merge(String docType, String target, Set<String> sourceCsids) throws URISyntaxException, DocumentException, Exception {
		logger.debug("Merging docType=" + docType + " target=" + target + " sourceCsids=" + StringUtils.join(sourceCsids, ","));

		String serviceName = getAuthorityServiceNameForDocType(docType);
		PoxPayloadOut targetItemPayload;

		if (RefNameUtils.isTermRefname(target)) {
			AuthorityTermInfo termInfo = RefNameUtils.parseAuthorityTermInfo(target);
			AuthorityInfo authorityInfo = termInfo.inAuthority;
			String targetServiceName = authorityInfo.resource;

			if (!targetServiceName.equals(serviceName)) {
				throw new DocumentException("Source item and target item must be the same record type.");
			}

			targetItemPayload	= findAuthorityItemByRefName(serviceName, target);
		} else {
			targetItemPayload = findAuthorityItemByCsid(serviceName, target);
		}

		String targetItemCsid = getCsid(targetItemPayload);

		for (String sourceCsid : sourceCsids) {
			if (sourceCsid.equals(targetItemCsid)) {
				throw new DocumentException("Can't merge a record into itself.");
			}
		}

		String targetDocName = getFieldValue(targetItemPayload, "/document/@name");

		List<PoxPayloadOut> sourceItemPayloads = new ArrayList<PoxPayloadOut>();

		for (String sourceCsid : sourceCsids) {
			PoxPayloadOut sourceItemPayload = findAuthorityItemByCsid(serviceName, sourceCsid);
			String sourceDocName = getFieldValue(sourceItemPayload, "/document/@name");

			if (!sourceDocName.equals(targetDocName)) {
				throw new DocumentException("Source item and target item must be the same record type.");
			}

			sourceItemPayloads.add(sourceItemPayload);
		}

		return merge(docType, targetItemPayload, sourceItemPayloads);
	}

	private InvocationResults merge(String docType, PoxPayloadOut targetItemPayload, List<PoxPayloadOut> sourceItemPayloads) throws URISyntaxException, DocumentException, Exception {
		int numAffected = 0;
		List<String> userNotes = new ArrayList<String>();

		Element targetTermGroupListElement = getTermGroupListElement(targetItemPayload);
		Element mergedTermGroupListElement = targetTermGroupListElement.createCopy();

		String targetCsid = getCsid(targetItemPayload);
		String targetRefName = getRefName(targetItemPayload);
		String inAuthority = getFieldValue(targetItemPayload, "inAuthority");

		logger.debug("Merging term groups");

		for (PoxPayloadOut sourceItemPayload : sourceItemPayloads) {
			String sourceCsid = getCsid(sourceItemPayload);
			Element sourceTermGroupListElement = getTermGroupListElement(sourceItemPayload);

			logger.debug("Merging term groups from source " + sourceCsid + " into target " + targetCsid);

			try {
				mergeTermGroupLists(mergedTermGroupListElement, sourceTermGroupListElement);
			}
			catch(RuntimeException e) {
				throw new RuntimeException("Error merging source record " + sourceCsid + " into target record " + targetCsid + ": " + e.getMessage(), e);
			}
		}

		logger.debug("Updating target: docType=" + docType + " inAuthority=" + inAuthority + " targetCsid=" + targetCsid);

		updateAuthorityItem(docType, inAuthority, targetCsid, getUpdatePayload(targetTermGroupListElement, mergedTermGroupListElement));

		String targetDisplayName = RefNameUtils.getDisplayName(targetRefName);

		userNotes.add("Updated the target record, " + targetDisplayName + ".");
		numAffected++;

		String serviceName = getAuthorityServiceNameForDocType(docType);

		logger.debug("Updating references");

		for (PoxPayloadOut sourceItemPayload : sourceItemPayloads) {
			String sourceCsid = getCsid(sourceItemPayload);
			String sourceRefName = getRefName(sourceItemPayload);

			InvocationResults results = updateReferences(serviceName, inAuthority, sourceCsid, sourceRefName, targetRefName);

			userNotes.add(results.getUserNote());
			numAffected += results.getNumAffected();
		}

		logger.debug("Deleting source items");

		for (PoxPayloadOut sourceItemPayload : sourceItemPayloads) {
			String sourceCsid = getCsid(sourceItemPayload);
			String sourceRefName = getRefName(sourceItemPayload);

			InvocationResults results = deleteAuthorityItem(docType, getFieldValue(sourceItemPayload, "inAuthority"), sourceCsid, sourceRefName);

			userNotes.add(results.getUserNote());
			numAffected += results.getNumAffected();
		}

		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setUserNote(StringUtils.join(userNotes, "\n"));

		return results;
	}

	private InvocationResults updateReferences(String serviceName, String inAuthority, String sourceCsid, String sourceRefName, String targetRefName) throws URISyntaxException, DocumentException, Exception {
		logger.debug("Updating references: serviceName=" + serviceName + " inAuthority=" + inAuthority + " sourceCsid=" + sourceCsid + " sourceRefName=" + sourceRefName + " targetRefName=" + targetRefName);

		String sourceDisplayName = RefNameUtils.getDisplayName(sourceRefName);

		int pageNum = 0;
		int pageSize = 100;
		List<AuthorityRefDocList.AuthorityRefDocItem> items;

		int loopCount = 0;
		int numUpdated = 0;

		logger.debug("Looping with pageSize=" + pageSize);

		do {
			loopCount++;

			// The pageNum/pageSize parameters don't work properly for refobj requests!
			// It should be safe to repeatedly fetch page 0 for a large-ish page size,
			// and update that page, until no references are left.

			items = findReferencingFields(serviceName, inAuthority, sourceCsid, null, pageNum, pageSize);
			Map<String, ReferencingRecord> referencingRecordsByCsid = new LinkedHashMap<String, ReferencingRecord>();

			logger.debug("Loop " + loopCount + ": " + items.size() + " items found");

			for (AuthorityRefDocList.AuthorityRefDocItem item : items) {
				// If a record contains a reference to the record multiple times, multiple items are returned,
				// but only the first has a non-null workflow state. A bug?

				String itemCsid = item.getDocId();
				ReferencingRecord record = referencingRecordsByCsid.get(itemCsid);

				if (record == null) {
					if (item.getWorkflowState() != null && !item.getWorkflowState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
						record = new ReferencingRecord(item.getUri());
						referencingRecordsByCsid.put(itemCsid, record);
					}
				}

				if (record != null) {
					String[] sourceFieldElements = item.getSourceField().split(":");
					String partName = sourceFieldElements[0];
					String fieldName = sourceFieldElements[1];

					Map<String, Set<String>> fields = record.getFields();
					Set<String> fieldsInPart = fields.get(partName);

					if (fieldsInPart == null) {
						fieldsInPart = new HashSet<String>();
						fields.put(partName, fieldsInPart);
					}

					fieldsInPart.add(fieldName);
				}
			}

			List<ReferencingRecord> referencingRecords = new ArrayList<ReferencingRecord>(referencingRecordsByCsid.values());

			logger.debug("Loop " + loopCount + ": updating " + referencingRecords.size() + " records");

			for (ReferencingRecord record : referencingRecords) {
				InvocationResults results = updateReferencingRecord(record, sourceRefName, targetRefName);
				numUpdated += results.getNumAffected();
			}
		}
		while (items.size() > 0);

		InvocationResults results = new InvocationResults();
		results.setNumAffected(numUpdated);

		if (numUpdated > 0) {
			results.setUserNote(
				"Updated "
				+ numUpdated
				+ (numUpdated == 1 ? " record " : " records ")
				+ "that referenced the source record, "
				+ sourceDisplayName + "."
			);
		} else {
			results.setUserNote("No records referenced the source record, " + sourceDisplayName + ".");
		}

		return results;
	}

	private InvocationResults updateReferencingRecord(ReferencingRecord record, String fromRefName, String toRefName) throws URISyntaxException, DocumentException {
		String fromRefNameStem = RefNameUtils.stripAuthorityTermDisplayName(fromRefName);
		// String toRefNameStem = RefNameUtils.stripAuthorityTermDisplayName(toRefName);

		logger.debug("Updating references: record.uri=" + record.getUri() + " fromRefName=" + fromRefName + " toRefName=" + toRefName);

		Map<String, Set<String>> fields = record.getFields();

		PoxPayloadOut recordPayload = findByUri(record.getUri());
		Document recordDocument = recordPayload.getDOMDocument();
		Document newDocument = (Document) recordDocument.clone();
		Element rootElement = newDocument.getRootElement();

		for (Element partElement : (List<Element>) rootElement.elements()) {
			String partName = partElement.getName();

			if (fields.containsKey(partName)) {
				for (String fieldName : fields.get(partName)) {
					List<Node> nodes = partElement.selectNodes("descendant::" + fieldName);

					for (Node node : nodes) {
						String text = node.getText();
						String refNameStem = null;

						try {
							refNameStem = RefNameUtils.stripAuthorityTermDisplayName(text);
						}
						catch(IllegalArgumentException e) {}

						if (refNameStem != null && refNameStem.equals(fromRefNameStem)) {
							AuthorityTermInfo termInfo = RefNameUtils.parseAuthorityTermInfo(text);
							// String newRefName = toRefNameStem + "'" + termInfo.displayName + "'";
							String newRefName = toRefName;

							node.setText(newRefName);
						}
					}
				}
			}
			else {
				rootElement.remove(partElement);
			}
		}

		String payload = newDocument.asXML();

		return updateUri(record.getUri(), payload);
	}

	private InvocationResults updateUri(String uri, String payload) throws URISyntaxException {
		String[] uriParts = uri.split("/");

		if (uriParts.length == 3) {
			String serviceName = uriParts[1];
			String csid = uriParts[2];

			NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(serviceName);

			resource.update(getServiceContext(), getResourceMap(), createUriInfo(), csid, payload);
		}
		else if (uriParts.length == 5) {
			String serviceName = uriParts[1];
			String vocabularyCsid = uriParts[2];
			String items = uriParts[3];
			String csid = uriParts[4];

			if (items.equals("items")) {
				AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(serviceName);

				resource.updateAuthorityItem(getServiceContext(), getResourceMap(), createUriInfo(), vocabularyCsid, csid, payload);
			}
		}
		else {
			throw new IllegalArgumentException("Invalid uri " + uri);
		}

		logger.debug("Updated referencing record " + uri);

		InvocationResults results = new InvocationResults();
		results.setNumAffected(1);
		results.setUserNote("Updated referencing record " + uri);

		return results;
	}

	private void updateAuthorityItem(String docType, String inAuthority, String csid, String payload) throws URISyntaxException {
		String serviceName = getAuthorityServiceNameForDocType(docType);
		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(serviceName);

		resource.updateAuthorityItem(getServiceContext(), getResourceMap(), createUriInfo(), inAuthority, csid, payload);
	}

	private InvocationResults deleteAuthorityItem(String docType, String inAuthority, String csid, String refName) throws URISyntaxException, Exception {
		int numAffected = 0;
		List<String> userNotes = new ArrayList<String>();
		String displayName = RefNameUtils.getDisplayName(refName);

		// If the item is the broader context of any items, warn and do nothing.

		List<String> narrowerItemCsids = findNarrower(csid);

		if (narrowerItemCsids.size() > 0) {
			logger.debug("Item " + csid + " has narrower items -- not deleting");

			userNotes.add("The source record, " + displayName + ", was not deleted because it has narrower items in its hierarchy.");
		}
		else {
			// If the item has a broader context, delete the relation.

			List<RelationsCommonList.RelationListItem> relationItems = new ArrayList<RelationsCommonList.RelationListItem>();

			for (RelationsCommonList.RelationListItem item : findRelated(csid, null, "hasBroader", null, null)) {
				relationItems.add(item);
			}

			if (relationItems.size() > 0) {
				RelationResource relationResource = (RelationResource) getResourceMap().get(RelationClient.SERVICE_NAME);

				for (RelationsCommonList.RelationListItem item : relationItems) {
					String relationCsid = item.getCsid();

					String subjectRefName = item.getSubject().getRefName();
					String subjectDisplayName = RefNameUtils.getDisplayName(subjectRefName);

					String objectRefName = item.getObject().getRefName();
					String objectDisplayName = RefNameUtils.getDisplayName(objectRefName);

					logger.debug("Deleting hasBroader relation " + relationCsid);

					relationResource.deleteWithParentCtx(getServiceContext(), relationCsid);

					userNotes.add("Deleted the \"has broader\" relation from " + subjectDisplayName + " to " + objectDisplayName + ".");
					numAffected++;
				}
			}

			String serviceName = getAuthorityServiceNameForDocType(docType);
			AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(serviceName);

			logger.debug("Soft deleting: docType=" + docType + " inAuthority=" + inAuthority + " csid=" + csid);

			resource.updateItemWorkflowWithTransition(getServiceContext(), createUriInfo(), inAuthority, csid, "delete");

			userNotes.add("Deleted the source record, " + displayName + ".");
			numAffected++;
		}

		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setUserNote(StringUtils.join(userNotes, "\n"));

		return results;
	}

	/**
	 * @param Returns a map of the term groups in term group list, keyed by display name.
	 *        If multiple groups have the same display name, an exception is thrown.
	 * @return The term groups.
	 */
	private Map<String, Element> getTermGroups(Element termGroupListElement) {
		Map<String, Element> termGroups = new LinkedHashMap<String, Element>();
		Iterator<Element> childIterator = termGroupListElement.elementIterator();

		while (childIterator.hasNext()) {
			Element termGroupElement = childIterator.next();
			String displayName = getDisplayName(termGroupElement);

			if (termGroups.containsKey(displayName)) {
				// Two term groups in the same item have identical display names.

				throw new RuntimeException("multiple terms have display name \"" + displayName + "\"");
			}
			else {
				termGroups.put(displayName, termGroupElement);
			}
		}

		return termGroups;
	}

	private String getDisplayName(Element termGroupElement) {
		Node displayNameNode = termGroupElement.selectSingleNode("termDisplayName");
		String displayName = (displayNameNode == null) ? "" : displayNameNode.getText();

		return displayName;
	}

	private Element getTermGroupListElement(PoxPayloadOut itemPayload) {
		Element termGroupListElement = null;
		Element commonPartElement = findCommonPartElement(itemPayload);

		if (commonPartElement != null) {
			termGroupListElement = findTermGroupListElement(commonPartElement);
		}

		return termGroupListElement;
	}

	private Element findCommonPartElement(PoxPayloadOut itemPayload) {
		Element commonPartElement = null;

		for (PayloadOutputPart candidatePart : itemPayload.getParts()) {
			Element candidatePartElement = candidatePart.asElement();

			if (candidatePartElement.getName().endsWith("_common")) {
				commonPartElement = candidatePartElement;
				break;
			}
		}

		return commonPartElement;
	}

	private Element findTermGroupListElement(Element contextElement) {
		Element termGroupListElement = null;
		Iterator<Element> childIterator = contextElement.elementIterator();

		while (childIterator.hasNext()) {
			Element candidateElement = childIterator.next();

			if (candidateElement.getName().endsWith("TermGroupList")) {
				termGroupListElement = candidateElement;
				break;
			}
		}

		return termGroupListElement;
	}

	private void mergeTermGroupLists(Element targetTermGroupListElement, Element sourceTermGroupListElement) {
		Map<String, Element> sourceTermGroups;

		try {
			sourceTermGroups = getTermGroups(sourceTermGroupListElement);
		}
		catch(RuntimeException e) {
			throw new RuntimeException("a problem was found in the source record: " + e.getMessage(), e);
		}

		for (Element targetTermGroupElement : (List<Element>) targetTermGroupListElement.elements()) {
			String displayName = getDisplayName(targetTermGroupElement);

			if (sourceTermGroups.containsKey(displayName)) {
				logger.debug("Merging in existing term \"" + displayName + "\"");

				try {
					mergeTermGroups(targetTermGroupElement, sourceTermGroups.get(displayName));
				}
				catch(RuntimeException e) {
					throw new RuntimeException("could not merge term groups with display name \"" + displayName + "\": " + e.getMessage(), e);
				}

				sourceTermGroups.remove(displayName);
			}
		}

		for (Element sourceTermGroupElement : sourceTermGroups.values()) {
			logger.debug("Adding new term \"" + getDisplayName(sourceTermGroupElement) + "\"");

			targetTermGroupListElement.add(sourceTermGroupElement.createCopy());
		}
	}

	private void mergeTermGroups(Element targetTermGroupElement, Element sourceTermGroupElement) {
		// This function assumes there are no nested repeating groups.

		for (Element sourceChildElement : (List<Element>) sourceTermGroupElement.elements()) {
			String sourceValue = sourceChildElement.getText();

			if (sourceValue == null) {
				sourceValue = "";
			}

			if (sourceValue.length() > 0) {
				String name = sourceChildElement.getName();
				Element targetChildElement = targetTermGroupElement.element(name);

				if (targetChildElement == null) {
					targetTermGroupElement.add(sourceChildElement.createCopy());
				}
				else {
					String targetValue = targetChildElement.getText();

					if (targetValue == null) {
						targetValue = "";
					}

					if (!targetValue.equals(sourceValue)) {
						if (targetValue.length() > 0) {
							throw new RuntimeException("merge conflict in field " + name + ": source value \"" + sourceValue + "\" differs from target value \"" + targetValue +"\"");
						}

						targetTermGroupElement.remove(targetChildElement);
						targetTermGroupElement.add(sourceChildElement.createCopy());
					}
				}
			}
		}
	}

	private String getUpdatePayload(Element originalTermGroupListElement, Element updatedTermGroupListElement) {
		List<Element> parents = new ArrayList<Element>();

		for (Element e = originalTermGroupListElement; e != null; e = e.getParent()) {
			parents.add(e);
		}

		Collections.reverse(parents);

		// Remove the original termGroupList element
		parents.remove(parents.size() - 1);

		// Remove the root
		Element rootElement = parents.remove(0);

		// Copy the root to a new document
		Document document = DocumentHelper.createDocument(copyElement(rootElement));
		Element current = document.getRootElement();

		// Copy the remaining parents
		for (Element parent : parents) {
			Element parentCopy = copyElement(parent);

			current.add(parentCopy);
			current = parentCopy;
		}

		// Add the updated termGroupList element

		current.add(updatedTermGroupListElement);

		String payload = document.asXML();

		return payload;
	}

	private Element copyElement(Element element) {
		Element copy = DocumentHelper.createElement(element.getQName());
		copy.appendAttributes(element);

		return copy;
	}

	private class ReferencingRecord {
		private String uri;
		private Map<String, Set<String>> fields;

		public ReferencingRecord(String uri) {
			this.uri = uri;
			this.fields = new HashMap<String, Set<String>>();
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public Map<String, Set<String>> getFields() {
			return fields;
		}
	}
}
