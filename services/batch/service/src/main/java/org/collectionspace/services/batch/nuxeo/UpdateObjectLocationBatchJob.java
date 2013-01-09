package org.collectionspace.services.batch.nuxeo;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.dom4j.DocumentException;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob extends AbstractBatchInvocable {

    // FIXME: Where appropriate, get from existing constants rather than local declarations
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA_NAME = "collectionobjects_common";
    private final static String CSID_ELEMENT_NAME = "csid";
    private final static String CURRENT_LOCATION_ELEMENT_NAME = "currentLocation";
    private final static String LOCATION_DATE_ELEMENT_NAME = "locationDate";
    private final static String OBJECT_NUMBER_ELEMENT_NAME = "objectNumber";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX = "ns2";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_URI =
            "http://collectionspace.org/services/collectionobject";
    private final static Namespace COLLECTIONOBJECTS_COMMON_NAMESPACE =
            Namespace.getNamespace(
            COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX,
            COLLECTIONOBJECTS_COMMON_NAMESPACE_URI);
    private final String CLASSNAME = this.getClass().getSimpleName();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
            if (logger.isTraceEnabled()) {
                logger.trace("Invoking " + CLASSNAME + " ...");
                logger.trace("Invocation context is: " + getInvocationContext().getMode());
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
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                }
                csids.addAll(listCsids);
            } else if (requestIsForInvocationModeGroup()) {
                String groupCsid = getInvocationContext().getGroupCSID();
                // FIXME: Get individual CSIDs from the group
                // and add them to the list
            }

            if (csids.isEmpty()) {
                throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
            }

            // Update the computed current location field for each CollectionObject
            setResults(updateComputedCurrentLocations(csids));
            setCompletionStatus(STATUS_COMPLETE);

        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
        }

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

    protected UriInfo createRelatedRecordsUriInfo(String query) throws URISyntaxException {
        URI uri = new URI(null, null, null, query, null);
        return createUriInfo(uri.getRawQuery());
    }

    protected String getFieldElementValue(PoxPayloadOut payload, String partLabel, Namespace partNamespace, String fieldPath) {
        String value = null;
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new StringReader(payload.toXML()));
            Element root = document.getRootElement();
            // The part element is always expected to have an explicit namespace.
            Element part = root.getChild(partLabel, partNamespace);
            // Try getting the field element both with and without a namespace.
            // Even though a field element that lacks a namespace prefix
            // may yet inherit its namespace from a parent, JDOM may require that
            // the getChild() call be made without a namespace.
            Element field = part.getChild(fieldPath, partNamespace);
            if (field == null) {
                field = part.getChild(fieldPath);
            }
            if (field != null) {
                value = field.getText();
            }
        } catch (Exception e) {
            logger.error("Error getting value from field path " + fieldPath
                    + " in schema part " + partLabel);
            return null;
        }
        return value;
    }

    private InvocationResults updateComputedCurrentLocations(List<String> csids) {

        ResourceMap resourcemap = getResourceMap();
        ResourceBase collectionObjectResource = resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        ResourceBase movementResource = resourcemap.get(MovementClient.SERVICE_NAME);
        PoxPayloadOut collectionObjectPayload;
        String computedCurrentLocation;
        String objectNumber;
        String movementCsid;
        int numAffected = 0;

        try {

            // For each CollectionObject record
            for (String collectionObjectCsid : csids) {

                // Get the movement records related to this record

                // Get movement records related to this record where the CollectionObject
                // record is the subject of the relation
                String queryString = "rtObj=" + collectionObjectCsid; // FIXME: Get from constant
                UriInfo uriInfo = createRelatedRecordsUriInfo(queryString);

                AbstractCommonList relatedMovements = movementResource.getList(uriInfo);
                if (logger.isTraceEnabled()) {
                    logger.trace("Identified " + relatedMovements.getTotalItems()
                            + " Movement records related to the object CollectionObject record " + collectionObjectCsid);
                }

                // Get movement records related to this record where the CollectionObject
                // record is the object of the relation
                queryString = "rtSbj=" + collectionObjectCsid; // FIXME: Get from constant
                uriInfo = createRelatedRecordsUriInfo(queryString);

                AbstractCommonList reverseRelatedMovements = movementResource.getList(uriInfo);
                if (logger.isTraceEnabled()) {
                    logger.trace("Identified " + reverseRelatedMovements.getTotalItems()
                            + " Movement records related to the subject CollectionObject record " + collectionObjectCsid);
                }

                if ((relatedMovements.getTotalItems() == 0) && reverseRelatedMovements.getTotalItems() == 0) {
                    continue;
                }

                // Merge the two lists of related movement records
                relatedMovements.getListItem().addAll(reverseRelatedMovements.getListItem());

                // Get the latest movement record from among those, and extract
                // its current location value
                Set<String> alreadyProcessedMovementCsids = new HashSet<String>();
                computedCurrentLocation = "";
                String currentLocation;
                String locationDate;
                String mostRecentLocationDate = "";
                for (AbstractCommonList.ListItem movementRecord : relatedMovements.getListItem()) {
                    movementCsid = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, CSID_ELEMENT_NAME);
                    if (Tools.isBlank(movementCsid)) {
                        continue;
                    }
                    // Avoid processing any related Movement record more than once,
                    // regardless of the directionality of its relation(s) to this
                    // CollectionObject record.
                    if (alreadyProcessedMovementCsids.contains(movementCsid)) {
                        continue;
                    } else {
                        alreadyProcessedMovementCsids.add(movementCsid);
                    }
                    locationDate = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, LOCATION_DATE_ELEMENT_NAME);
                    if (Tools.isBlank(locationDate)) {
                        continue;
                    }
                    currentLocation = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, CURRENT_LOCATION_ELEMENT_NAME);
                    if (Tools.isBlank(currentLocation)) {
                        continue;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Location date value = " + locationDate);
                        logger.trace("Current location value = " + currentLocation);
                    }
                    // Assumes that all values for this element/field will be consistent ISO 8601
                    // date/time representations, each of which can be ordered via string comparison.
                    //
                    // If this is *not* the case, we can instead parse and convert these values
                    // to date/time objects.
                    if (locationDate.compareTo(mostRecentLocationDate) > 0) {
                        mostRecentLocationDate = locationDate;
                        // FIXME: Add optional validation here that the currentLocation value
                        // parses successfully as an item refName
                        computedCurrentLocation = currentLocation;
                    }

                }

                // Update the computed current location value in the CollectionObject record
                collectionObjectPayload = findByCsid(collectionObjectResource, collectionObjectCsid);
                if (Tools.notBlank(collectionObjectPayload.toXML())) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Payload: " + "\n" + collectionObjectPayload);
                    }
                    objectNumber = getFieldElementValue(collectionObjectPayload,
                            COLLECTIONOBJECTS_COMMON_SCHEMA_NAME, COLLECTIONOBJECTS_COMMON_NAMESPACE,
                            OBJECT_NUMBER_ELEMENT_NAME);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Object number: " + objectNumber);
                    }
                    if (Tools.notBlank(objectNumber)) {
                        String collectionObjectUpdatePayload =
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<document name=\"collectionobject\">"
                                + "  <ns2:collectionobjects_common "
                                + "      xmlns:ns2=\"http://collectionspace.org/services/collectionobject\">"
                                + "    <objectNumber>" + objectNumber + "</objectNumber>"
                                + "    <computedCurrentLocation>" + computedCurrentLocation + "</computedCurrentLocation>"
                                + "  </ns2:collectionobjects_common>"
                                + "</document>";
                        if (logger.isTraceEnabled()) {
                            logger.trace("Update payload: " + "\n" + collectionObjectUpdatePayload);
                        }
                        byte[] response = collectionObjectResource.update(resourcemap, null, collectionObjectCsid,
                                collectionObjectUpdatePayload);
                        numAffected++;
                        if (logger.isTraceEnabled()) {
                            logger.trace("Computed current location value for CollectionObject " + collectionObjectCsid
                                    + " was set to " + computedCurrentLocation);
                        }
                    }

                }
            }
        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage() + " ";
            errMsg = errMsg + "Successfully updated " + numAffected + " CollectionObject record(s) prior to error.";
            logger.error(errMsg);
            setErrorResult(errMsg);
            getResults().setNumAffected(numAffected);
            return getResults();
        }

        logger.info("Updated computedCurrentLocation values in " + numAffected + " CollectionObject records.");
        getResults().setNumAffected(numAffected);
        return getResults();
    }
}
