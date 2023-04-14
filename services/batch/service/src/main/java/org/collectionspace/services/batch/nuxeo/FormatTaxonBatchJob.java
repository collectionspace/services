package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.common.api.TaxonFormatter;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatTaxonBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(FormatTaxonBatchJob.class);

	private TaxonFormatter taxonFormatter;

	public FormatTaxonBatchJob() {
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

				setResults(formatTaxon(csid));
			}
			else if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
				setResults(formatTaxons(getInvocationContext().getListCSIDs().getCsid()));
			}
			else if (mode.equalsIgnoreCase(INVOCATION_MODE_NO_CONTEXT)) {
				setResults(formatAllTaxons());
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

	public InvocationResults formatAllTaxons() throws URISyntaxException, DocumentException {
		return formatTaxons(findAllTaxonRecords());
	}

	public InvocationResults formatTaxon(String taxonCsid) throws URISyntaxException, DocumentException {
		return formatTaxons(Arrays.asList(taxonCsid));
	}

	public InvocationResults formatTaxons(List<String> taxonCsids) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		int numAffected = 0;

		for (String taxonCsid : taxonCsids) {
			formatDisplayNames(taxonCsid);

			numAffected = numAffected + 1;
		}

		results.setNumAffected(numAffected);
		results.setUserNote("Updated " + numAffected + " taxonomy " + (numAffected == 1 ? "record" : "records"));

		return results;
	}

	private List<String> formatDisplayNames(String taxonCsid) throws URISyntaxException, DocumentException {
		List<String> formattedDisplayNames = new ArrayList<String>();

		PoxPayloadOut taxonPayload = findTaxonByCsid(taxonCsid);
		String inAuthority = getFieldValue(taxonPayload, TaxonConstants.IN_AUTHORITY_SCHEMA_NAME, TaxonConstants.IN_AUTHORITY_FIELD_NAME);

		String[] displayNamePathElements = TaxonConstants.DISPLAY_NAME_FIELD_NAME.split("/");
		String termGroupListFieldName = displayNamePathElements[0];
		String termGroupFieldName = displayNamePathElements[1];
		String displayNameFieldName = displayNamePathElements[2];

		String[] formattedDisplayNamePathElements = TaxonConstants.FORMATTED_DISPLAY_NAME_FIELD_NAME.split("/");
		String formattedDisplayNameFieldName = formattedDisplayNamePathElements[2];

		PayloadOutputPart part = taxonPayload.getPart(TaxonConstants.DISPLAY_NAME_SCHEMA_NAME);

		if (part != null) {
			Element element = part.asElement();
			Node termGroupListNode = element.selectSingleNode(termGroupListFieldName);
			List<Node> termGroupNodes = termGroupListNode.selectNodes(termGroupFieldName);

			for (Node termGroupNode : termGroupNodes) {
				Element termGroupElement = (Element) termGroupNode;
				Node displayNameNode = termGroupElement.selectSingleNode(displayNameFieldName);
				String displayName = (displayNameNode == null) ? "" : displayNameNode.getText();
				String formattedDisplayName = taxonFormatter.format(displayName);

				Element formattedDisplayNameElement = (Element) termGroupElement.selectSingleNode(formattedDisplayNameFieldName);

				if (formattedDisplayNameElement == null) {
					formattedDisplayNameElement = termGroupElement.addElement(formattedDisplayNameFieldName);
				}

				formattedDisplayNameElement.setText(formattedDisplayName);
				formattedDisplayNames.add(formattedDisplayName);
			}

			String updatePayload =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<document name=\"taxon\">" +
					"<ns2:taxon_common xmlns:ns2=\"http://collectionspace.org/services/taxonomy\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
						termGroupListNode.asXML() +
					"</ns2:taxon_common>" +
				"</document>";

			AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(TaxonomyAuthorityClient.SERVICE_NAME);
			resource.updateAuthorityItem(getResourceMap(), createUriInfo(), inAuthority, taxonCsid, updatePayload);
		}

		return formattedDisplayNames;
	}

	private List<String> findAllTaxonRecords() {
		// TODO
		return Collections.emptyList();
	}
}
