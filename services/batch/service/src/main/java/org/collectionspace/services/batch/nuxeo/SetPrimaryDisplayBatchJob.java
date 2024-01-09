package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.collectionspace.services.client.MediaClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author RJ Li
 */
public class SetPrimaryDisplayBatchJob extends AbstractBatchJob {
    final Logger log = LoggerFactory.getLogger(SetPrimaryDisplayBatchJob.class);

    public SetPrimaryDisplayBatchJob() {
        setSupportedInvocationModes(Collections.singletonList(INVOCATION_MODE_SINGLE));
    }

    @Override
    public void run() {
        try {
            if (requestIsForInvocationModeSingle()) {
                String docType = getInvocationContext().getDocType();

                if (!docType.equals("Media")) {
                    throw new Exception("Unsupported document type");
                }

                String mediaCsid = getInvocationContext().getSingleCSID();

                if (mediaCsid == null) {
                    throw new Exception("Missing context csid");
                }

                int numAffected = makePrimary(mediaCsid);

                InvocationResults results = new InvocationResults();
                results.setNumAffected(numAffected);
                results.setUserNote(numAffected + " media records updated");
                results.setPrimaryURICreated("media.html?csid=" + mediaCsid);

                setResults(results);
            }
        } catch (Exception e) {
            setErrorResult(e.getMessage());
        }
    }

    /**
     * Makes the current media record the primary by setting primaryDisplay to true,
     * and setting primaryDisplay of other media records related to the same
     * collectionobject to false.
     *
     * @param mediaCsid the csid of the media record to make primary
     */
    private int makePrimary(String mediaCsid) throws URISyntaxException {
        int affectedCount = 0;

        // Set the given media record to be primary.

        log.debug("Setting media to be primary: {}", mediaCsid);

        setPrimaryDisplay(mediaCsid, true);
        affectedCount++;

        // Find all media records related to the same collectionobject(s)
        // as the given media record.

        Set<String> nonPrimaryMediaCsids = new HashSet<String>();

        for (String collectionObjectCsid : findRelatedCollectionObjects(mediaCsid)) {
            nonPrimaryMediaCsids.addAll(findRelatedMedia(collectionObjectCsid));
        }

        // Remove the record we're making primary.

        nonPrimaryMediaCsids.remove(mediaCsid);

        // Set the remaining media to be non-primary.

        for (String nonPrimaryMediaCsid : nonPrimaryMediaCsids) {
            log.debug("Setting media to be non-primary: {}", nonPrimaryMediaCsid);

            setPrimaryDisplay(nonPrimaryMediaCsid, false);
            affectedCount++;
        }

        return affectedCount;
    }

    private void setPrimaryDisplay(String mediaCsid, boolean primaryDisplay) throws URISyntaxException {
        String updatePayload =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<document name=\"media\">" +
            "<ns2:media_pahma xmlns:ns2=\"http://collectionspace.org/services/media/local/pahma\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<primaryDisplay>" + primaryDisplay + "</primaryDisplay>" +
            "</ns2:media_pahma>" +
            "</document>";

        NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(MediaClient.SERVICE_NAME);
        resource.update(this.getServiceContext(), getResourceMap(), createUriInfo(), mediaCsid, updatePayload);
    }
}
