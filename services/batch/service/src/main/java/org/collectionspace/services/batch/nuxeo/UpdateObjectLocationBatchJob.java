package org.collectionspace.services.batch.nuxeo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.api.Tools;
import org.dom4j.DocumentException;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob extends AbstractBatchInvocable {

    final String CLASSNAME = this.getClass().getSimpleName();
    final Logger logger = LoggerFactory.getLogger(UpdateObjectLocationBatchJob.class);

    // Initialization tasks
    public UpdateObjectLocationBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
    }

    /**
     * The main work logic of the batch job. Will be called after setContext.
     */
    @Override
    public void run() {

        setCompletionStatus(STATUS_MIN_PROGRESS);

        try {
            // FIXME: Placeholder during early development
            if (logger.isInfoEnabled()) {
                logger.info("Invoking " + CLASSNAME + " ...");
                logger.info("Invocation context is: " + getInvocationContext().getMode());
            }

            if (!requestedInvocationModeIsSupported()) {
                setInvocationModeNotSupportedResult();
            }

            List<String> csids = new ArrayList<String>();
            if (requestIsForInvocationModeSingle()) {
                String singleCsid = getInvocationContext().getSingleCSID();
                if (Tools.notBlank(singleCsid)) {
                    csids.add(singleCsid);
                }
            } else if (requestIsForInvocationModeList()) {
                List<String> listCsids = (getInvocationContext().getListCSIDs().getCsid());
                if (listCsids == null || listCsids.isEmpty()) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT_MESSAGE);
                }
                csids.addAll(listCsids);
            } else if (requestIsForInvocationModeGroup()) {
                String groupCsid = getInvocationContext().getGroupCSID();
                // FIXME: Get individual CSIDs from the group
                // and add them to the list
            }

            if (csids.isEmpty()) {
                throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT_MESSAGE);
            }

            ResourceBase collectionObjectResource = getResourceMap().get(CollectionObjectClient.SERVICE_NAME);
            PoxPayloadOut collectionObjectPayload;
            // For each CollectionObject record:
            for (String csid : csids) {
                // Get the movement records related to this record
                // Get the latest movement record from among those
                // Extract its current location value
                // Update the computed current location value in the CollectionObject record
                collectionObjectPayload = findByCsid(collectionObjectResource, csid);
                if (logger.isInfoEnabled()) {
                    logger.info("Payload: " + "\n" + collectionObjectPayload);
                }
            }
        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
        }

        setCompletionStatus(STATUS_COMPLETE);
    }

    // Ray's convenience methods from his AbstractBatchJob class for the UC Berkeley Botanical Garden v2.4 implementation.
    
    protected PoxPayloadOut findByCsid(String serviceName, String csid) throws URISyntaxException, DocumentException {
        ResourceBase resource = getResourceMap().get(serviceName);
        return findByCsid(resource, csid);
    }

    protected PoxPayloadOut findByCsid(ResourceBase resource, String csid) throws URISyntaxException, DocumentException {
        byte[] response = resource.get(null, createUriInfo(), csid);
        PoxPayloadOut payload = new PoxPayloadOut(response);
        return payload;
    }

    protected UriInfo createUriInfo() throws URISyntaxException {
        return createUriInfo("");
    }

    protected UriInfo createUriInfo(String queryString) throws URISyntaxException {
        URI absolutePath = new URI("");
        URI baseUri = new URI("");
        return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment>emptyList());
    }
}
