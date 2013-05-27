package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateAccessCodeBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(UpdateRareFlagBatchJob.class);

	private final String[] TAXON_FIELD_NAME_PARTS = CollectionObjectConstants.TAXON_FIELD_NAME.split("\\/");
	private final String TAXON_FIELD_NAME_WITHOUT_PATH = TAXON_FIELD_NAME_PARTS[TAXON_FIELD_NAME_PARTS.length - 1];

	public UpdateAccessCodeBatchJob() {
		this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST, INVOCATION_MODE_NO_CONTEXT));
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
					//setResults(updateAccessCode(csid));
					setResults(updateParentAccessCodes(csid));
				}
				else if (docType.equals(CollectionObjectConstants.NUXEO_DOCTYPE)) {
					setResults(updateReferencedAccessCodes(csid));
				}
				else {
					throw new Exception("Unsupported document type: " + docType);
				}				
			}
			else if (this.requestIsForInvocationModeList()) {
				throw new Exception("Invocation mode not yet implemented: " + this.getInvocationContext().getMode());
			}
			else if (this.requestIsForInvocationModeNoContext()) {
				throw new Exception("Invocation mode not yet implemented: " + this.getInvocationContext().getMode());
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
	
	public InvocationResults updateAccessCode(String taxonCsid) throws URISyntaxException, DocumentException {
		UpdateAccessCodeResults updateResults = updateAccessCode(taxonCsid, true, false);
		
		InvocationResults results = new InvocationResults();
		results.setNumAffected(updateResults.getNumAffected());
		results.setUserNote(updateResults.isChanged() ? "access code changed to " + updateResults.getAccessCode() : "access code not changed");
		
		return results;
	}
	
	public InvocationResults updateParentAccessCodes(String taxonCsid) throws URISyntaxException, DocumentException {
		PoxPayloadOut taxonPayload = findTaxonByCsid(taxonCsid);
		String taxonRefName = getFieldValue(taxonPayload, TaxonConstants.REFNAME_SCHEMA_NAME, TaxonConstants.REFNAME_FIELD_NAME);
		String accessCode = getFieldValue(taxonPayload, TaxonConstants.ACCESS_CODE_SCHEMA_NAME, TaxonConstants.ACCESS_CODE_FIELD_NAME);

		logger.debug("updating parent access codes: taxonRefName=" + taxonRefName + " accessCode=" + accessCode);

		UpdateAccessCodeResults updateResults = updateParentAccessCodes(taxonCsid, accessCode);
		
		InvocationResults results = new InvocationResults();
		results.setNumAffected(updateResults.getNumAffected());
		results.setUserNote(results.getNumAffected() + " access codes changed");
		
		return results;
	}
	
	public InvocationResults updateReferencedAccessCodes(String collectionObjectCsid) throws URISyntaxException, DocumentException {
		PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(collectionObjectCsid);
		
		String deadFlag = getFieldValue(collectionObjectPayload, CollectionObjectConstants.DEAD_FLAG_SCHEMA_NAME, CollectionObjectConstants.DEAD_FLAG_FIELD_NAME);
		boolean isAlive = (deadFlag == null) || (!deadFlag.equalsIgnoreCase("true"));

		logger.debug("updating referenced access codes: collectionObjectCsid=" + collectionObjectCsid + " isAlive=" + isAlive);

		List<String> taxonRefNames = getFieldValues(collectionObjectPayload, CollectionObjectConstants.TAXON_SCHEMA_NAME, CollectionObjectConstants.TAXON_FIELD_NAME);
		long numAffected = 0;
		
		for (String taxonRefName : taxonRefNames) {
			PoxPayloadOut taxonPayload = findTaxonByRefName(taxonRefName);
			UpdateAccessCodeResults updateResults = updateAccessCode(taxonPayload, false, isAlive);
			
			if (updateResults.isChanged()) {
				UpdateAccessCodeResults parentUpdateResults = updateParentAccessCodes(getCsid(taxonPayload), updateResults.getAccessCode());

				numAffected += updateResults.getNumAffected() + parentUpdateResults.getNumAffected();
			}
		}
		
		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setUserNote(numAffected + " access codes changed");
		
		return results;
	}
	
	/**
	 * Updates the access code of the specified taxon record. The access code is determined by
	 * examining all collectionobjects that have a taxonomic determination that matches the
	 * refname of the given taxon record. If all matching collectionobjects are dead, then
	 * the access code is set to dead. If any matching collectionobjects are not dead, and
	 * the access code is dead, the access code is set to unrestricted.
	 * 
	 * @param taxonPayload	The services payload for the taxon record
	 * @param deep			If true, updates the access code of all descendant taxon records
	 * @param knownAlive	A hint that a child taxon of the specified taxon is known to be
	 *                      alive, or that an example of the specified taxon is known to be
	 *                      alive. This parameter allows for optimization when propagating
	 *                      access code changes up the hiearchy; if a child taxon or
	 *                      referencing collectionobject is known to be alive, and the
	 *                      current access code is dead, then the access code can be changed
	 *                      to unrestricted without looking at any other records.
	 * @return
	 * @throws DocumentException 
	 * @throws URISyntaxException 
	 */
	public UpdateAccessCodeResults updateAccessCode(PoxPayloadOut taxonPayload, boolean deep, boolean knownAlive) throws URISyntaxException, DocumentException {
		UpdateAccessCodeResults results = new UpdateAccessCodeResults();
		boolean foundAlive = knownAlive;
		
		String taxonCsid = getCsid(taxonPayload);
		String taxonRefName = getFieldValue(taxonPayload, TaxonConstants.REFNAME_SCHEMA_NAME, TaxonConstants.REFNAME_FIELD_NAME);
		String accessCode = getFieldValue(taxonPayload, TaxonConstants.ACCESS_CODE_SCHEMA_NAME, TaxonConstants.ACCESS_CODE_FIELD_NAME);

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
					boolean isChildAlive = !childAccessCode.equals(TaxonConstants.ACCESS_CODE_DEAD_VALUE);
					
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
					
					String childAccessCode = getFieldValue(childTaxonPayload, TaxonConstants.ACCESS_CODE_SCHEMA_NAME, TaxonConstants.ACCESS_CODE_FIELD_NAME);
					boolean isChildAlive = !childAccessCode.equals(TaxonConstants.ACCESS_CODE_DEAD_VALUE);
					
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
	
			List<String> collectionObjectCsids = findReferencingCollectionObjects(TaxonomyAuthorityClient.SERVICE_NAME, vocabularyShortId, taxonCsid, CollectionObjectConstants.TAXON_SCHEMA_NAME + ":" + TAXON_FIELD_NAME_WITHOUT_PATH);
			
			for (String collectionObjectCsid : collectionObjectCsids) {
				PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(collectionObjectCsid);
	
				String deadFlag = getFieldValue(collectionObjectPayload, CollectionObjectConstants.DEAD_FLAG_SCHEMA_NAME, CollectionObjectConstants.DEAD_FLAG_FIELD_NAME);
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

		if (foundAlive && (StringUtils.isEmpty(accessCode) || accessCode.equals(TaxonConstants.ACCESS_CODE_DEAD_VALUE))) {
			newAccessCode = TaxonConstants.ACCESS_CODE_UNRESTRICTED_VALUE; 
		}
		else if (!foundAlive) {
			newAccessCode = TaxonConstants.ACCESS_CODE_DEAD_VALUE;
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
	
	public UpdateAccessCodeResults updateAccessCode(String taxonCsid, boolean deep, boolean knownAlive) throws URISyntaxException, DocumentException {
		return updateAccessCode(findTaxonByCsid(taxonCsid), deep, knownAlive);
	}
	
	public UpdateAccessCodeResults updateParentAccessCodes(String taxonCsid, String accessCode) throws URISyntaxException, DocumentException {
		UpdateAccessCodeResults results = new UpdateAccessCodeResults();
		String parentTaxonCsid = findBroader(taxonCsid);

		if (parentTaxonCsid != null) {
			boolean isAlive = (accessCode == null) || !accessCode.equals(TaxonConstants.ACCESS_CODE_DEAD_VALUE);

			UpdateAccessCodeResults parentUpdateResults = updateAccessCode(parentTaxonCsid, false, isAlive);	
	
			if (parentUpdateResults.isChanged()) {
				UpdateAccessCodeResults grandparentUpdateResults = updateParentAccessCodes(parentTaxonCsid, parentUpdateResults.getAccessCode());
				
				// Except for numAffected, the result fields are probably not all that useful in this situation.
				// Set the changed flag to whether the immediate parent was changed, and the access code to
				// the immediate parent's.
				
				results.setChanged(true);
				results.setNumAffected(parentUpdateResults.getNumAffected() + grandparentUpdateResults.getNumAffected());
				results.setAccessCode(parentUpdateResults.getAccessCode());
			}
		}
		
		return results;
	}
	
	/**
	 * Sets the access code of the specified taxon record to the specified value.
	 * 
	 * @param authorityCsid			the csid of the authority containing the taxon record
	 * @param taxonCsid				the csid of the taxon record
	 * @param accessCode			the value of the access code
	 * @throws URISyntaxException 
	 */
	private void setAccessCode(String authorityCsid, String taxonCsid, String accessCode) throws URISyntaxException {
		String updatePayload = 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<document name=\"taxon\">" +
					"<ns2:taxon_naturalhistory xmlns:ns2=\"http://collectionspace.org/services/taxonomy/domain/naturalhistory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
						getFieldXml(TaxonConstants.ACCESS_CODE_FIELD_NAME, accessCode) +
					"</ns2:taxon_naturalhistory>" +
				"</document>";

		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(TaxonomyAuthorityClient.SERVICE_NAME);
		resource.updateAuthorityItem(getResourceMap(), createUriInfo(), authorityCsid, taxonCsid, updatePayload);
	}
	
	private class UpdateAccessCodeResults {
		private boolean isSoftDeleted = false;
		private boolean isChanged = false;
		private String accessCode = null;
		private long numAffected = 0;

		public boolean isSoftDeleted() {
			return isSoftDeleted;
		}

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
