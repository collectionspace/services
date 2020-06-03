package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.collectionspace.services.batch.BatchCommon;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionObjectFactory;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.InventoryStatusList;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateInventoryStatusBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(UpdateInventoryStatusBatchJob.class);

	public UpdateInventoryStatusBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_LIST));
	}

	@Override
	public void run() {
		run(null);
	}

	@Override
	public void run(BatchCommon batchCommon) {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			String mode = getInvocationContext().getMode();

			if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
				List<String> csids = getInvocationContext().getListCSIDs().getCsid();
				List<String> values = this.getValues();

				setResults(updateRecords(csids, values));
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

	private List<String> getValues() {
		List<String> values = new ArrayList<String>();

		for (Param param : this.getParams()) {
			if (param.getKey().equals("inventoryStatus")) {
				values.add(param.getValue());
			}
		}

		return values;
	}

	public InvocationResults updateRecords(List<String> csids, List<String> values) throws Exception {
		InvocationResults results = new InvocationResults();
		int numAffected = 0;

		ArrayList<String> displayNames = new ArrayList<String>();

		for (String value : values) {
			AuthorityTermInfo termInfo = RefNameUtils.parseAuthorityTermInfo(value);
			String displayName = termInfo.displayName;

			displayNames.add(displayName);
		}

		for (String csid : csids) {
			updateRecord(csid, values);

			numAffected = numAffected + 1;
		}

		String userNote;

		if (displayNames.size() > 0) {
			userNote = "Inventory status changed to " + StringUtils.join(displayNames, ", ") + ".";
		} else {
			userNote = "Inventory status values removed.";
		}

		results.setNumAffected(numAffected);
		results.setUserNote(userNote);

		return results;
	}

	private void updateRecord(String csid, List<String> values) throws Exception {
		CollectionobjectsCommon collectionObject = new CollectionobjectsCommon();
		InventoryStatusList inventoryStatusList = new InventoryStatusList();

		inventoryStatusList.getInventoryStatus().addAll(values);
		collectionObject.setInventoryStatusList(inventoryStatusList);

		CollectionObjectClient client = new CollectionObjectClient();
		PoxPayloadOut payload = CollectionObjectFactory.createCollectionObjectInstance(client.getCommonPartName(), collectionObject);
		NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(CollectionObjectClient.SERVICE_NAME);

		resource.update(getResourceMap(), createUriInfo(), csid, payload.asXML());
	}
}
