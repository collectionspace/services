package org.collectionspace.services.batch.nuxeo.botgarden;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.collectionspace.services.batch.nuxeo.AbstractBatchJob;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.common.ResourceMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This batch job updates certain fields in collectionobject records, specified as invocation
 * parameters. Supported fields include: sex in collectionobjects_common, and any top-level
 * (non-repeating) field in collectionobjects_botgarden.
 *
 * It can be invoked in either list or single mode.
 * @author Cesar Villalobos
 *
 */
public class UpdateObjectFieldsBatchJob extends AbstractBatchJob {
    final Logger logger = LoggerFactory.getLogger(UpdateObjectFieldsBatchJob.class);

    public UpdateObjectFieldsBatchJob() {
        this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
    }

    @Override
    public void run() {
        setCompletionStatus(STATUS_MIN_PROGRESS);

        try {
            InvocationContext ctx = getInvocationContext();
            String mode = ctx.getMode();

            ArrayList<String> csids = new ArrayList<String>();

            if (mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
                String csid = ctx.getSingleCSID();
                csids.add(csid);
            } else if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
                csids.addAll(ctx.getListCSIDs().getCsid());
            } else {
                throw new Exception("Unsupported invocation mode: " + mode);
            }

            HashMap<String, String> fieldsToValues = this.getValues();

            if (fieldsToValues.isEmpty()) {
                throw new Exception("There is nothing to update. Aborting...");
            }

            setResults(updateRecords(csids, fieldsToValues));
            setCompletionStatus(STATUS_COMPLETE);
        } catch (Exception e) {
            setCompletionStatus(STATUS_ERROR);
            setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
        }
    }

    public InvocationResults updateRecords(List<String> csids, HashMap<String, String> values) {
        InvocationResults results = new InvocationResults();
        int numAffected = 0;

        try {
            for (String csid : csids) {
                updateRecord(csid, values);
                numAffected += 1;
            }
        } catch (Exception e) {
            setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
        }

        String userNote = "";

        results.setNumAffected(numAffected);
        results.setUserNote(userNote);

        return results;
    }

    public void updateRecord(String csid, HashMap<String, String> values) throws URISyntaxException {
        StringBuilder valuesToUpdate = new StringBuilder();
        String sex = "";

        for (String key : values.keySet()) {
            if (key.equals("sex")) {
                sex = getFieldXml(key, values.get(key));
            } else {
                valuesToUpdate.append(getFieldXml(key, values.get(key)));
            }
        }

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                         "<document name=\"collectionobjects\">" +
                         "<ns2:collectionobjects_common " +
                         "xmlns:ns2=\"http://collectionspace.org/services/collectionobject\" " +
                         "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"  +
                         sex + "</ns2:collectionobjects_common>" +
                         "<ns2:collectionobjects_botgarden " +
                         "xmlns:ns2=\"http://collectionspace.org/services/collectionobject/local/botgarden\" " +
                         "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                         valuesToUpdate +
                         "</ns2:collectionobjects_botgarden>" +
                         "</document>";


        ResourceMap resource = getResourceMap();
        NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resource.get(CollectionObjectClient.SERVICE_NAME);
        byte[] responseBytes = collectionObjectResource.update(getServiceContext(), resource, createUriInfo(), csid, payload);

        logger.debug("Batch resource: Response from CollectionObject (cataloging record) update: {}",
                     new String(responseBytes));
    }

    public HashMap<String, String> getValues() {
        HashMap<String, String> results = new  HashMap<String, String>();
        for (Param param : this.getParams()) {
            if (param.getKey() != null) {
                String value = param.getValue();
                if (value != null && !value.isEmpty()) {
                    results.put(param.getKey(), param.getValue());
                }
            }
        }
        return results;
    }
}
