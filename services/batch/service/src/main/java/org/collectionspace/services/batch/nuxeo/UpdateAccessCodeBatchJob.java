package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectBotGardenConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.taxonomy.nuxeo.TaxonBotGardenConstants;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A batch job that sets the access code on taxonomy records. The single CSID context is supported.
 *
 * If the document is a taxon record, the access codes of the taxon record and all of its descendant
 * (narrower context) records are updated.
 *
 * If the document is a collectionobject, the access codes of all taxon records referenced by the
 * collectionobject's taxonomic identification are updated, and propagated up the taxon
 * hierarchy to the ancestors (broader contexts) of each taxon record.
 *
 * @author ray
 *
 */
public class UpdateAccessCodeBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(UpdateAccessCodeBatchJob.class);

	private final String[] TAXON_FIELD_NAME_PARTS = CollectionObjectBotGardenConstants.TAXON_FIELD_NAME.split("\\/");
	private final String TAXON_FIELD_NAME_WITHOUT_PATH = TAXON_FIELD_NAME_PARTS[TAXON_FIELD_NAME_PARTS.length - 1];

	public UpdateAccessCodeBatchJob() {
		this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE));
	}

	@Override
	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			if (this.requestIsForInvocationModeSingle()) {
				String csid = getInvocationContext().getSingleCSID();

				if (StringUtils.isEmpty(csid)) {
					throw new Exception("Missing context csid");
				}

				String docType = getInvocationContext().getDocType();

				if (docType.equals(TaxonConstants.NUXEO_DOCTYPE)) {
					setResults(updateAccessCode(csid, true));
					//setResults(updateParentAccessCode(csid, true));
				}
				else if (docType.equals(CollectionObjectConstants.NUXEO_DOCTYPE)) {
					setResults(updateReferencedAccessCodes(csid, true));
				}
				else {
					throw new Exception("Unsupported document type: " + docType);
				}
			}
			else {
				throw new Exception("Unsupported invocation mode: " + this.getInvocationContext().getMode());
			}

			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}


	/**
	 * Updates the access code of the specified taxon record.
	 *
	 * @param taxonRefNameOrCsid	The refname or csid of the taxon record.
	 * @param deep					If true, update the access codes of all descendant (narrower context)
	 * 								taxon records. On a deep update, the access codes of all descendant
	 * 								records are updated first, before calculating the access code of the parent.
	 * 								This ensures that the access codes of children are up-to-date, and can be
	 * 								used to calculate an up-to-date value for the parent.
	 *
	 * 								If false, only the specified taxon record is updated. The calculation
	 * 								of the access code uses the access codes of child taxon records, so
	 * 								an accurate result depends on the accuracy of the children's access codes.
	 * @return						The results of the invocation.
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateAccessCode(String taxonRefNameOrCsid, boolean deep) throws URISyntaxException, DocumentException, Exception {
		UpdateAccessCodeResults updateResults = updateAccessCode(taxonRefNameOrCsid, deep, false);

		InvocationResults results = new InvocationResults();
		results.setNumAffected(updateResults.getNumAffected());
		results.setUserNote(updateResults.isChanged() ? "access code changed to " + updateResults.getAccessCode() : "access code not changed");

		return results;
	}

	/**
	 * Updates the access code of the parent (broader context) of the specified taxon record.
	 *
	 * @param taxonCsid				The csid of the taxon record.
	 * @param propagate				If true, propagate the access code up the taxon hierarchy to
	 * 								all ancestors of the taxon record. The propagation stops when
	 * 								the new value of the access code is the same as the old value,
	 * 								or when a root node (a node with no broader context) is reached.
	 *
	 * 								If false, update only the access code of the parent.
	 * @return						The results of the invocation.
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateParentAccessCode(String taxonCsid, boolean propagate) throws URISyntaxException, DocumentException, Exception {
		PoxPayloadOut taxonPayload = findTaxonByCsid(taxonCsid);
		String taxonRefName = getFieldValue(taxonPayload, TaxonConstants.REFNAME_SCHEMA_NAME, TaxonConstants.REFNAME_FIELD_NAME);
		String accessCode = getFieldValue(taxonPayload, TaxonBotGardenConstants.ACCESS_CODE_SCHEMA_NAME, TaxonBotGardenConstants.ACCESS_CODE_FIELD_NAME);

		logger.debug("updating parent access code: taxonRefName=" + taxonRefName + " propagate=" + propagate + " accessCode=" + accessCode);

		UpdateAccessCodeResults updateResults = updateParentAccessCode(taxonCsid, accessCode, propagate);

		InvocationResults results = new InvocationResults();
		results.setNumAffected(updateResults.getNumAffected());
		results.setUserNote(results.getNumAffected() + " access codes changed");

		return results;
	}

	/**
	 * Updates the access codes of all taxon records that are referenced in the taxonomic identification
	 * field of the specified collectionobject.
	 *
	 * @param collectionObjectCsid	The csid of the collectionobject.
	 * @param propagate				If true, propagate the access code up the taxon hierarchy to
	 * 								the ancestors of each referenced taxon record. The propagation stops when
	 * 								the new value of the access code is the same as the old value,
	 * 								or when a root node (a node with no broader context) is reached.
	 *
	 * 								If false, update only the access codes of the taxon records
	 * 								that are directly referenced.
	 * @return						The results of the invocation.
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateReferencedAccessCodes(String collectionObjectCsid, boolean propagate) throws URISyntaxException, DocumentException, Exception {
		PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(collectionObjectCsid);

		String deadFlag = getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.DEAD_FLAG_SCHEMA_NAME,
				CollectionObjectBotGardenConstants.DEAD_FLAG_FIELD_NAME);
		boolean isAlive = (deadFlag == null) || (!deadFlag.equalsIgnoreCase("true"));

		logger.debug("updating referenced access codes: collectionObjectCsid=" + collectionObjectCsid + " propagate=" + propagate + " isAlive=" + isAlive);

		List<String> taxonRefNames = getFieldValues(collectionObjectPayload, CollectionObjectBotGardenConstants.TAXON_SCHEMA_NAME,
				CollectionObjectBotGardenConstants.TAXON_FIELD_NAME);
		long numAffected = 0;

		for (String taxonRefName : taxonRefNames) {
			PoxPayloadOut taxonPayload = findTaxonByRefName(taxonRefName);
			if (taxonPayload != null) {
				UpdateAccessCodeResults updateResults = updateAccessCode(taxonPayload, false, isAlive);
				if (updateResults.isChanged()) {
					numAffected += updateResults.getNumAffected();
	
					if (propagate) {
						UpdateAccessCodeResults parentUpdateResults = updateParentAccessCode(getCsid(taxonPayload), updateResults.getAccessCode(), true);
	
						numAffected += parentUpdateResults.getNumAffected();
					}
				}
			} else {
				if (Tools.isBlank(taxonRefName) == false) {
					String msg = String.format("%s found that cataloging/object record CSID=%s references taxon '%s' which could not be found.",
							getClass().getName(), collectionObjectCsid, taxonRefName);
					logger.warn(msg);
				}
			}
		}

		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setUserNote(numAffected + " access codes changed");

		return results;
	}

	/**
	 * Updates the access code of the specified taxon record. The access code is determined by
	 * examining all collectionobjects that have a taxonomic identification that matches the
	 * refname of the taxon record, as well as the access codes of child (narrower context)
	 * taxon records. If all referencing collectionobjects are dead (as determined
	 * by the dead flag), and all child taxon records are dead (as determined by their access
	 * codes), then the access code is set to Dead. If any matching collectionobjects
	 * are not dead, or any child taxons are not dead, and the access code is currently Dead,
	 * the access code is set to Unrestricted. Otherwise, the access code is not changed.
	 *
	 * @param taxonPayload	The services payload of the taxon record.
	 * @param deep			If true, update the access code of all descendant taxon records.
	 * 						On a deep update, the access codes of all descendant
	 * 						records are updated first, before calculating the access code of the parent.
	 * 						This ensures that the access codes of children are up-to-date, and can be
	 * 						used to calculate an up-to-date value for the parent.

	 * 						If false, only the specified taxon record is updated. The calculation
	 * 						of the access code uses the access codes of child taxon records, so
	 * 						an accurate result depends on the accuracy of the children's access codes.
	 * @param knownAlive	A hint that a child taxon of the specified taxon is known to be
	 *                      alive, or that a collectionobject of the specified taxon is known to be
	 *                      alive. This parameter allows for optimization when propagating
	 *                      access code changes up the hierarchy; if a child taxon or
	 *                      referencing collectionobject is known to be alive, and the
	 *                      current access code is Dead, then the access code can be changed
	 *                      to Unrestricted without examining any other records.
	 * @return				The results of the update.
	 * @throws DocumentException
	 * @throws URISyntaxException
	 */
	public UpdateAccessCodeResults updateAccessCode(PoxPayloadOut taxonPayload, boolean deep, boolean knownAlive) throws URISyntaxException, DocumentException, Exception {
		UpdateAccessCodeResults results = new UpdateAccessCodeResults();
		boolean foundAlive = knownAlive;

		String taxonCsid = getCsid(taxonPayload);
		String taxonRefName = getFieldValue(taxonPayload, TaxonConstants.REFNAME_SCHEMA_NAME, TaxonConstants.REFNAME_FIELD_NAME);
		String accessCode = getFieldValue(taxonPayload, TaxonBotGardenConstants.ACCESS_CODE_SCHEMA_NAME, TaxonBotGardenConstants.ACCESS_CODE_FIELD_NAME);

		logger.debug("updating access code: taxonRefName=" + taxonRefName + " deep=" + deep + " knownAlive=" + knownAlive);

		if (accessCode == null) {
			accessCode = "";
		}

		List<String> childTaxonCsids = findNarrower(taxonCsid);

		if (deep) {
			long numChildrenChanged = 0;

			// Update the access code on all the children, and track whether any are alive.

			for (String childTaxonCsid : childTaxonCsids) {
				UpdateAccessCodeResults childResults = updateAccessCode(childTaxonCsid, true, false);

				if (!childResults.isSoftDeleted()) {
					String childAccessCode = childResults.getAccessCode();
					boolean isChildAlive = !childAccessCode.equals(TaxonBotGardenConstants.ACCESS_CODE_DEAD_VALUE);

					if (isChildAlive) {
						foundAlive = true;
					}

					if (childResults.isChanged()) {
						numChildrenChanged++;
					}
				}
			}

			results.setNumAffected(numChildrenChanged);
		}
		else {
			if (!foundAlive) {
				// Check if any of the children are alive.

				for (String childTaxonCsid : childTaxonCsids) {
					PoxPayloadOut childTaxonPayload = findTaxonByCsid(childTaxonCsid);

					String childAccessCode = getFieldValue(childTaxonPayload, TaxonBotGardenConstants.ACCESS_CODE_SCHEMA_NAME,
							TaxonBotGardenConstants.ACCESS_CODE_FIELD_NAME);
					boolean isChildAlive = !childAccessCode.equals(TaxonBotGardenConstants.ACCESS_CODE_DEAD_VALUE);

					if (isChildAlive) {
						foundAlive = true;
						break;
					}
				}
			}
		}

		if (!foundAlive) {
			// Check if any examples of this taxon are alive.

			RefName.AuthorityItem item = RefName.AuthorityItem.parse(taxonRefName);
			String vocabularyShortId = item.getParentShortIdentifier();

			List<String> collectionObjectCsids = findReferencingCollectionObjects(TaxonomyAuthorityClient.SERVICE_NAME, vocabularyShortId, taxonCsid,
					CollectionObjectBotGardenConstants.TAXON_SCHEMA_NAME + ":" + TAXON_FIELD_NAME_WITHOUT_PATH);

			for (String collectionObjectCsid : collectionObjectCsids) {
				PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(collectionObjectCsid);

				String deadFlag = getFieldValue(collectionObjectPayload, CollectionObjectBotGardenConstants.DEAD_FLAG_SCHEMA_NAME,
						CollectionObjectBotGardenConstants.DEAD_FLAG_FIELD_NAME);
				boolean isDead = (deadFlag != null) && (deadFlag.equalsIgnoreCase("true"));

				if (!isDead) {
					foundAlive = true;
					break;
				}
			}
		}

		String newAccessCode;

		// The access code only needs to be changed if:
		//
		// 1. There is a living example of the taxon, but the access code is dead.
		// 2. There are no living examples, but the access code is not dead.
		//
		// Otherwise, the access code should stay the same. In particular, if there is a
		// living example, and the access code is not dead, the current value of unrestricted
		// or restricted should be retained.

		if (foundAlive && (StringUtils.isEmpty(accessCode) || accessCode.equals(TaxonBotGardenConstants.ACCESS_CODE_DEAD_VALUE))) {
			newAccessCode = TaxonBotGardenConstants.ACCESS_CODE_UNRESTRICTED_VALUE;
		}
		else if (!foundAlive) {
			newAccessCode = TaxonBotGardenConstants.ACCESS_CODE_DEAD_VALUE;
		}
		else {
			newAccessCode = accessCode;
		}

		if (!newAccessCode.equals(accessCode)) {
			String inAuthority = getFieldValue(taxonPayload, TaxonConstants.IN_AUTHORITY_SCHEMA_NAME, TaxonConstants.IN_AUTHORITY_FIELD_NAME);

			setAccessCode(inAuthority, taxonCsid, newAccessCode);

			results.setChanged(true);
			results.setNumAffected(results.getNumAffected() + 1);
		}

		results.setAccessCode(newAccessCode);

		return results;
	}

	/**
	 * Updates the access code of the taxon record with the specified refname or csid.
	 *
	 * @param taxonRefNameOrCsid
	 * @param deep
	 * @param knownAlive
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public UpdateAccessCodeResults updateAccessCode(String taxonRefNameOrCsid, boolean deep, boolean knownAlive) throws URISyntaxException, DocumentException, Exception {
		PoxPayloadOut taxonPayload;

		if (RefName.AuthorityItem.parse(taxonRefNameOrCsid) == null) {
			taxonPayload = findTaxonByCsid(taxonRefNameOrCsid);
		}
		else {
			taxonPayload = findTaxonByRefName(taxonRefNameOrCsid);
		}

		return updateAccessCode(taxonPayload, deep, knownAlive);
	}

	/**
	 * Updates the access code of the taxon record with the specified refname or csid, when a new
	 * child taxon is known to have been added.
	 *
	 * @param taxonRefNameOrCsid
	 * @param deep
	 * @param newChildCsid       The csid of the newly added child.
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public UpdateAccessCodeResults updateAccessCode(String taxonRefNameOrCsid, boolean deep, String newChildTaxonCsid) throws URISyntaxException, DocumentException, Exception {
		PoxPayloadOut newChildTaxonPayload = findTaxonByCsid(newChildTaxonCsid);
		String newChildTaxonAccessCode = getFieldValue(newChildTaxonPayload, TaxonBotGardenConstants.ACCESS_CODE_SCHEMA_NAME, TaxonBotGardenConstants.ACCESS_CODE_FIELD_NAME);

		if (newChildTaxonAccessCode == null) {
			newChildTaxonAccessCode = "";
		}

		boolean knownAlive = !newChildTaxonAccessCode.equals(TaxonBotGardenConstants.ACCESS_CODE_DEAD_VALUE);

		return updateAccessCode(taxonRefNameOrCsid, deep, knownAlive);
	}

	/**
	 * Updates the access code of the parent (broader context) of the specified taxon record,
	 * whose access code is assumed to be a specified value.
	 *
	 * @param taxonCsid				The csid of the taxon record.
	 * @param accessCode			The access code of the taxon record.
	 * @param propagate				If true, propagate the access code up the taxon hierarchy to
	 * 								all ancestors of the taxon record. The propagation stops when
	 * 								the new value of the access code is the same as the old value,
	 * 								or when a root node (a node with no broader context) is reached.
	 *
	 * 								If false, update only the access code of the parent.
	 * @return						The results of the update.
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public UpdateAccessCodeResults updateParentAccessCode(String taxonCsid, String accessCode, boolean propagate) throws URISyntaxException, DocumentException, Exception {
		UpdateAccessCodeResults results = new UpdateAccessCodeResults();
		String parentTaxonCsid = findBroader(taxonCsid);
		long numAffected = 0;

		logger.debug("updating parent access code: taxonCsid=" + taxonCsid + " accessCode=" + accessCode + " propagate=" + propagate);

		if (parentTaxonCsid != null) {
			boolean isAlive = (accessCode == null) || !accessCode.equals(TaxonBotGardenConstants.ACCESS_CODE_DEAD_VALUE);

			UpdateAccessCodeResults parentUpdateResults = updateAccessCode(parentTaxonCsid, false, isAlive);

			if (parentUpdateResults.isChanged()) {
				// Except for numAffected, the result fields are probably not all that useful in this situation.
				// Set the changed flag to whether the immediate parent was changed, and the access code to
				// the immediate parent's.

				results.setAccessCode(parentUpdateResults.getAccessCode());
				results.setChanged(true);

				numAffected += parentUpdateResults.getNumAffected();

				if (propagate) {
					UpdateAccessCodeResults grandparentUpdateResults = updateParentAccessCode(parentTaxonCsid, parentUpdateResults.getAccessCode(), true);
					numAffected += grandparentUpdateResults.getNumAffected();
				}
			}
		}

		results.setNumAffected(numAffected);

		return results;
	}

	/**
	 * Sets the access code of the specified taxon record to the specified value.
	 *
	 * @param authorityCsid			The csid of the authority containing the taxon record.
	 * @param taxonCsid				The csid of the taxon record.
	 * @param accessCode			The value of the access code.
	 * @throws URISyntaxException
	 */
	private void setAccessCode(String authorityCsid, String taxonCsid, String accessCode) throws URISyntaxException {
		String updatePayload =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<document name=\"taxon\">" +
					"<ns2:taxon_naturalhistory xmlns:ns2=\"http://collectionspace.org/services/taxonomy/domain/naturalhistory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
						getFieldXml(TaxonBotGardenConstants.ACCESS_CODE_FIELD_NAME, accessCode) +
					"</ns2:taxon_naturalhistory>" +
				"</document>";

		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(TaxonomyAuthorityClient.SERVICE_NAME);
		resource.updateAuthorityItem(getServiceContext(), getResourceMap(), createUriInfo(), authorityCsid, taxonCsid, updatePayload);
	}

	public class UpdateAccessCodeResults {
		private boolean isSoftDeleted = false;
		private boolean isChanged = false;
		private String accessCode = null;
		private long numAffected = 0;

		public boolean isSoftDeleted() {
			return isSoftDeleted;
		}

		/**
		 * @param isSoftDeleted
		 */
		public void setSoftDeleted(boolean isSoftDeleted) {
			this.isSoftDeleted = isSoftDeleted;
		}

		public boolean isChanged() {
			return isChanged;
		}

		public void setChanged(boolean isChanged) {
			this.isChanged = isChanged;
		}

		public String getAccessCode() {
			return accessCode;
		}

		public void setAccessCode(String accessCode) {
			this.accessCode = accessCode;
		}

		public long getNumAffected() {
			return numAffected;
		}

		public void setNumAffected(long numAffected) {
			this.numAffected = numAffected;
		}
	}
}
