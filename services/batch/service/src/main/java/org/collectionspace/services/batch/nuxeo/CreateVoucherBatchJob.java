package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectBotGardenConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.loanout.nuxeo.LoanoutBotGardenConstants;
import org.collectionspace.services.loanout.nuxeo.LoanoutConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.place.nuxeo.PlaceBotGardenConstants;
import org.collectionspace.services.place.nuxeo.PlaceConstants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateVoucherBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(CreateVoucherBatchJob.class);

	public CreateVoucherBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE));
	}

	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			String mode = getInvocationContext().getMode();

			if (!mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				throw new Exception("Unsupported invocation mode: " + mode);
			}

			String csid = getInvocationContext().getSingleCSID();

			if (StringUtils.isEmpty(csid)) {
				throw new Exception("Missing context csid");
			}

			String docType = getInvocationContext().getDocType();

			if (docType.equals(CollectionObjectConstants.NUXEO_DOCTYPE)) {
				setResults(createVoucherFromCataloging(csid));
			}
			else if (docType.equals(MovementConstants.NUXEO_DOCTYPE)) {
				setResults(createVoucherFromCurrentLocation(csid));
			}
			else {
				throw new Exception("Unsupported docType: " + docType);
			}

			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}

	public InvocationResults createVoucherFromCataloging(String collectionObjectCsid) throws ResourceException, URISyntaxException, DocumentException, Exception {
		return createVoucherFromCataloging(collectionObjectCsid, null);
	}

	public InvocationResults createVoucherFromCataloging(String collectionObjectCsid, String movementCsid) throws ResourceException, URISyntaxException, DocumentException, Exception {
		InvocationResults results = new InvocationResults();

		PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(collectionObjectCsid);
		String collectionObjectWorkflowState = getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.WORKFLOW_STATE_SCHEMA_NAME,
				CollectionObjectBotGardenConstants.WORKFLOW_STATE_FIELD_NAME);

		if (collectionObjectWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			logger.debug("skipping deleted collectionobject: collectionObjectCsid=" + collectionObjectCsid);

			results.setNumAffected(0);
			results.setUserNote("skipped deleted record");
		}
		else {
			Map<String, String> botGardenFields = new HashMap<String, String>();

			if (movementCsid == null) {
				movementCsid = findSingleRelatedMovement(collectionObjectCsid);
			}

			if (movementCsid != null) {
				PoxPayloadOut movementPayload = findMovementByCsid(movementCsid);

				if (movementPayload != null) {
					botGardenFields.put("gardenLocation", getFieldValue(movementPayload, MovementConstants.CURRENT_LOCATION_SCHEMA_NAME, MovementConstants.CURRENT_LOCATION_FIELD_NAME));
				}
			}

			botGardenFields.put("fieldCollectionNote", getFieldCollectionNote(collectionObjectPayload));
			botGardenFields.put("annotation", getAnnotation(collectionObjectPayload));
			botGardenFields.put("labelRequested", LoanoutBotGardenConstants.LABEL_REQUESTED_NO_VALUE);

			Map<String, String> naturalHistoryFields = new HashMap<String, String>();
			naturalHistoryFields.put("numLent", "1");

			String voucherCsid = createVoucher(botGardenFields, naturalHistoryFields);
			logger.debug("voucher created: voucherCsid=" + voucherCsid);

			String forwardRelationCsid = createRelation(voucherCsid, LoanoutConstants.NUXEO_DOCTYPE, collectionObjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE, RelationConstants.AFFECTS_TYPE);
			String backwardRelationCsid = createRelation(collectionObjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE, voucherCsid, LoanoutConstants.NUXEO_DOCTYPE, RelationConstants.AFFECTS_TYPE);
			logger.debug("relations created: forwardRelationCsid=" + forwardRelationCsid + " backwardRelationCsid=" + backwardRelationCsid);

			results.setNumAffected(1);
			results.setPrimaryURICreated("/loansout/" + voucherCsid);
			results.setUserNote("Voucher created");
		}

		return results;
	}

	private String getFieldCollectionNote(PoxPayloadOut collectionObjectPayload) throws URISyntaxException, DocumentException, Exception {
		String placeNote = "";
		String reverseFieldCollectionPlace = getReverseFieldCollectionPlace(collectionObjectPayload);

		if (StringUtils.isNotBlank(reverseFieldCollectionPlace)) {
			placeNote = reverseFieldCollectionPlace;
		}
		else {
			String taxonomicRange = this.getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.TAXONOMIC_RANGE_SCHEMA_NAME,
					CollectionObjectBotGardenConstants.TAXONOMIC_RANGE_FIELD_NAME);

			if (StringUtils.isNotBlank(taxonomicRange)) {
				placeNote = "Geographic range " + taxonomicRange;
			}
		}

		String comment = this.getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.COMMENT_SCHEMA_NAME,
				CollectionObjectBotGardenConstants.COMMENT_FIELD_NAME);
		String collectionNote = "";

		if (StringUtils.isNotBlank(placeNote) && StringUtils.isNotBlank(comment)) {
			collectionNote = placeNote + ": " + comment;
		}
		else if (StringUtils.isNotBlank(placeNote)) {
			collectionNote = placeNote;
		}
		else {
			collectionNote = comment;
		}

		return collectionNote;
	}

	private String getReverseFieldCollectionPlace(PoxPayloadOut collectionObjectPayload) throws URISyntaxException, DocumentException, Exception {
		String reverseDisplayName = null;
		String fieldCollectionPlaceRefName = getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.FIELD_COLLECTION_PLACE_SCHEMA_NAME,
				CollectionObjectBotGardenConstants.FIELD_COLLECTION_PLACE_FIELD_NAME);

		if (StringUtils.isNotBlank(fieldCollectionPlaceRefName)) {
			PoxPayloadOut placePayload = null;

			try {
				placePayload = findPlaceByRefName(fieldCollectionPlaceRefName);
			}
			catch (WebApplicationException e) {
				logger.error("Error finding place: refName=" + fieldCollectionPlaceRefName, e);
			}

			if (placePayload != null) {
				List<String> termTypes = getFieldValues(placePayload, PlaceConstants.TERM_TYPE_SCHEMA_NAME, PlaceConstants.TERM_TYPE_FIELD_NAME);
				List<String> displayNames = getFieldValues(placePayload, PlaceConstants.DISPLAY_NAME_SCHEMA_NAME, PlaceConstants.DISPLAY_NAME_FIELD_NAME);

				int index = termTypes.indexOf(PlaceBotGardenConstants.REVERSE_TERM_TYPE);

				if (index < 0) {
					// There's no reverse term. Just use the primary.

					if (displayNames.size() > 0) {
						reverseDisplayName = displayNames.get(0);
					}
				}
				else {
					reverseDisplayName = displayNames.get(index);
				}
			}
		}

		if (reverseDisplayName == null) {
			reverseDisplayName = "";
		}

		return reverseDisplayName;
	}

	private String getAnnotation(PoxPayloadOut collectionObjectPayload) {
		String annotation = "";
		String determinationKind = getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.DETERMINATION_KIND_SCHEMA_NAME,
				CollectionObjectBotGardenConstants.DETERMINATION_KIND_FIELD_NAME);

		if (determinationKind.equals(CollectionObjectBotGardenConstants.DETERMINATION_KIND_DETERMINATION_VALUE)) {
			String determinationBy = getDisplayNameFromRefName(getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.DETERMINATION_BY_SCHEMA_NAME,
					CollectionObjectBotGardenConstants.DETERMINATION_BY_FIELD_NAME));

			if (StringUtils.isNotBlank(determinationBy)) {
				annotation += "det. by " + determinationBy;

				String determinationInstitution = getDisplayNameFromRefName(getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.DETERMINATION_INSTITUTION_SCHEMA_NAME,
						CollectionObjectBotGardenConstants.DETERMINATION_INSTITUTION_FIELD_NAME));
				String determinationDate = getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.DETERMINATION_DATE_SCHEMA_NAME,
						CollectionObjectBotGardenConstants.DETERMINATION_DATE_FIELD_NAME);

				if (StringUtils.isNotBlank(determinationInstitution)) {
					annotation += ", " + determinationInstitution;
				}

				if (StringUtils.isNotBlank(determinationDate)) {
					annotation += ", " + determinationDate;
				}
			}
		}

		return annotation;
	}

	public InvocationResults createVoucherFromCurrentLocation(String movementCsid) throws ResourceException, URISyntaxException, DocumentException, Exception {
		long numAffected = 0;
		String primaryUriCreated = null;

		List<String> collectionObjectCsids = findRelatedCollectionObjects(movementCsid);

		for (String collectionObjectCsid : collectionObjectCsids) {
			InvocationResults innerResults = createVoucherFromCataloging(collectionObjectCsid, movementCsid);

			numAffected = numAffected + innerResults.getNumAffected();

			if (primaryUriCreated == null) {
				primaryUriCreated = innerResults.getPrimaryURICreated();
			}
		}

		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setPrimaryURICreated(primaryUriCreated);

		if (collectionObjectCsids.size() == 0) {
			results.setUserNote("No related cataloging record found");
		}
		else {
			results.setUserNote("Voucher created for " + numAffected + " cataloging " + (numAffected == 1 ? "record" : "records"));
		}

		return results;
	}

	private String createVoucher(Map<String, String> botGardenFields, Map<String, String> naturalHistoryFields) throws ResourceException {
		String voucherCsid = null;

		String createVoucherPayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"loansout\">" +
				"<ns2:loansout_botgarden xmlns:ns2=\"http://collectionspace.org/services/loanout/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml(botGardenFields) +
				"</ns2:loansout_botgarden>" +
				"<ns2:loansout_naturalhistory xmlns:ns2=\"http://collectionspace.org/services/loanout/domain/naturalhistory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml(naturalHistoryFields) +
				"</ns2:loansout_naturalhistory>" +
			"</document>";

		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(LoanoutClient.SERVICE_NAME);
		Response response = resource.create(getServiceContext(), getResourceMap(), null, createVoucherPayload);

		if (response.getStatus() == CREATED_STATUS) {
			voucherCsid = CollectionSpaceClientUtils.extractId(response);
		}
		else {
			throw new ResourceException(response, "Error creating voucher");
		}

		return voucherCsid;
	}
}
