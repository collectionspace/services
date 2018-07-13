package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectBotGardenConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.api.TaxonFormatter;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.loanout.LoanoutResource;
import org.collectionspace.services.loanout.nuxeo.LoanoutBotGardenConstants;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatVoucherNameBatchJob extends AbstractBatchJob {
	public static final String HYBRID_SEPARATOR = " x ";

	final Logger logger = LoggerFactory.getLogger(FormatVoucherNameBatchJob.class);

	private TaxonFormatter taxonFormatter;

	public FormatVoucherNameBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST, INVOCATION_MODE_NO_CONTEXT));
		this.taxonFormatter = new TaxonFormatter();
	}

	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			String mode = getInvocationContext().getMode();

			if (mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				String csid = getInvocationContext().getSingleCSID();

				if (StringUtils.isEmpty(csid)) {
					throw new Exception("Missing context csid");
				}

				setResults(formatVoucherName(csid));
			}
			else if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
				setResults(formatVoucherNames(getInvocationContext().getListCSIDs().getCsid()));
			}
			else if (mode.equalsIgnoreCase(INVOCATION_MODE_NO_CONTEXT)) {
				setResults(formatQueuedVoucherNames());
			}
			else {
				throw new Exception("Unsupported invocation mode: " + mode);
			}

			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}

	public InvocationResults formatQueuedVoucherNames() throws URISyntaxException, DocumentException {
		return formatVoucherNames(findLabelRequests());
	}

	public InvocationResults formatVoucherName(String voucherCsid) throws URISyntaxException, DocumentException {
		return formatVoucherNames(Arrays.asList(voucherCsid));
	}

	public InvocationResults formatVoucherNames(List<String> voucherCsids) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		int numAffected = 0;
		List<String> formattedNames = new ArrayList<String>();

		for (String voucherCsid : voucherCsids) {
			VoucherName name = getVoucherName(voucherCsid);
			String formattedName = formatVoucherName(name);

			logger.debug("formattedName=" + formattedName);

			setStyledName(voucherCsid, formattedName);

			formattedNames.add(formattedName);
			numAffected = numAffected + 1;
		}

		results.setNumAffected(numAffected);
		results.setUserNote("Updated " + numAffected + " " + (numAffected == 1 ? "voucher" : "vouchers") + (numAffected == 1 ? ": " + formattedNames.get(0) : ""));

		return results;
	}

	private List<String> findLabelRequests() throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		LoanoutResource loanoutResource = (LoanoutResource) getResourceMap().get(LoanoutClient.SERVICE_NAME);
		AbstractCommonList loanoutList = loanoutResource.getList(getServiceContext(), createLabelRequestSearchUriInfo());

		for (AbstractCommonList.ListItem item : loanoutList.getListItem()) {
			for (org.w3c.dom.Element element : item.getAny()) {
				if (element.getTagName().equals("csid")) {
					csids.add(element.getTextContent());
					break;
				}
			}
		}

		return csids;
	}

	public VoucherName getVoucherName(String voucherCsid) throws URISyntaxException, DocumentException {
		VoucherName name = null;
		List<String> collectionObjectCsids = findRelatedCollectionObjects(voucherCsid);
		PoxPayloadOut collectionObjectPayload = null;

		for (String candidateCsid : collectionObjectCsids) {
			PoxPayloadOut candidatePayload = findCollectionObjectByCsid(candidateCsid);
			String workflowState = getFieldValue(candidatePayload, CollectionObjectConstants.WORKFLOW_STATE_SCHEMA_NAME, CollectionObjectConstants.WORKFLOW_STATE_FIELD_NAME);

			if (!workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				collectionObjectPayload = candidatePayload;
			}
		}

		if (collectionObjectPayload != null) {
			name = new VoucherName();

			name.setName(getDisplayNameFromRefName(getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.TAXON_SCHEMA_NAME,
					CollectionObjectBotGardenConstants.TAXON_FIELD_NAME)));
			name.setHybrid(getBooleanFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.HYBRID_FLAG_SCHEMA_NAME,
					CollectionObjectBotGardenConstants.HYBRID_FLAG_FIELD_NAME));

			if (name.isHybrid()) {
				List<String> hybridParents = this.getFieldValues(collectionObjectPayload, CollectionObjectBotGardenConstants.HYBRID_PARENT_SCHEMA_NAME,
						CollectionObjectBotGardenConstants.HYBRID_PARENT_FIELD_NAME);
				List<String> hybridQualifiers = this.getFieldValues(collectionObjectPayload, CollectionObjectBotGardenConstants.HYBRID_QUALIFIER_SCHEMA_NAME,
						CollectionObjectBotGardenConstants.HYBRID_QUALIFIER_FIELD_NAME);

				int femaleIndex = hybridQualifiers.indexOf(CollectionObjectBotGardenConstants.HYBRID_QUALIFIER_FEMALE_VALUE);
				int maleIndex = hybridQualifiers.indexOf(CollectionObjectBotGardenConstants.HYBRID_QUALIFIER_MALE_VALUE);

				if (femaleIndex >= 0) {
					name.setFemaleParentName(getDisplayNameFromRefName(hybridParents.get(femaleIndex)));
				}

				if (maleIndex >= 0) {
					name.setMaleParentName(getDisplayNameFromRefName(hybridParents.get(maleIndex)));
				}
			}
		}

		return name;
	}

	public String formatVoucherName(VoucherName name) {
		String formattedName = "";

		if (name != null) {
			if (name.isHybrid()) {
				if (name.getFemaleParentName() != null) {
					formattedName += taxonFormatter.format(name.getFemaleParentName());
				}

				formattedName += HYBRID_SEPARATOR;

				if (name.getMaleParentName() != null) {
					formattedName += taxonFormatter.format(name.getMaleParentName());
				}
			}
			else {
				if (name.getName() != null) {
					formattedName = taxonFormatter.format(name.getName());
				}
			}
		}

		return formattedName;
	}

	private void setStyledName(String loanoutCsid, String styledName) throws URISyntaxException {
		final String updatePayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"loansout\">" +
				"<ns2:loansout_botgarden xmlns:ns2=\"http://collectionspace.org/services/loanout/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml("styledName", styledName) +
				"</ns2:loansout_botgarden>" +
			"</document>";

		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(LoanoutClient.SERVICE_NAME);
		resource.update(getServiceContext(), getResourceMap(), createUriInfo(), loanoutCsid, updatePayload);
	}

	private UriInfo createLabelRequestSearchUriInfo() throws URISyntaxException {
		return createKeywordSearchUriInfo(LoanoutBotGardenConstants.LABEL_REQUESTED_SCHEMA_NAME, LoanoutBotGardenConstants.LABEL_REQUESTED_FIELD_NAME,
				LoanoutBotGardenConstants.LABEL_REQUESTED_YES_VALUE);
	}

	public class VoucherName {
		private boolean isHybrid = false;
		private String name;
		private String femaleParentName;
		private String maleParentName;

		public boolean isHybrid() {
			return isHybrid;
		}

		public void setHybrid(boolean isHybrid) {
			this.isHybrid = isHybrid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFemaleParentName() {
			return femaleParentName;
		}

		public void setFemaleParentName(String femaleParentName) {
			this.femaleParentName = femaleParentName;
		}

		public String getMaleParentName() {
			return maleParentName;
		}

		public void setMaleParentName(String maleParentName) {
			this.maleParentName = maleParentName;
		}
	}
}
