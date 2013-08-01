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
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
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
    private final static String COMPUTED_CURRENT_LOCATION_ELEMENT_NAME = "computedCurrentLocation";
    private final static String CSID_ELEMENT_NAME = "csid";
    private final static String CURRENT_LOCATION_ELEMENT_NAME = "currentLocation";
    private final static String LIFECYCLE_STATE_ELEMENT_NAME = "currentLifeCycleState";
    private final static String LOCATION_DATE_ELEMENT_NAME = "locationDate";
    private final static String OBJECT_NUMBER_ELEMENT_NAME = "objectNumber";
    private final static String WORKFLOW_COMMON_SCHEMA_NAME = "workflow_common";
    private final static String WORKFLOW_COMMON_NAMESPACE_PREFIX = "ns2";
    private final static String WORKFLOW_COMMON_NAMESPACE_URI =
            "http://collectionspace.org/services/workflow";
    private final static Namespace WORKFLOW_COMMON_NAMESPACE =
            Namespace.getNamespace(
            WORKFLOW_COMMON_NAMESPACE_PREFIX,
            WORKFLOW_COMMON_NAMESPACE_URI);
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA_NAME = "collectionobjects_common";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX = "ns2";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_URI =
            "http://collectionspace.org/services/collectionobject";
    private final static Namespace COLLECTIONOBJECTS_COMMON_NAMESPACE =
            Namespace.getNamespace(
            COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX,
            COLLECTIONOBJECTS_COMMON_NAMESPACE_URI);
    private final boolean EXCLUDE_DELETED = true;
    private final String CLASSNAME = this.getClass().getSimpleName();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Initialization tasks
    public UpdateObjectLocationBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST,
                INVOCATION_MODE_GROUP, INVOCATION_MODE_NO_CONTEXT));
    }

    /**
     * The main work logic of the batch job. Will be called after setContext.
     */
    @Override
    public void run() {

        setCompletionStatus(STATUS_MIN_PROGRESS);

        try {

            List<String> csids = new ArrayList<String>();

            // Build a list of CollectionObject records to process via this
            // batch job, depending on the invocation mode requested.
            if (requestIsForInvocationModeSingle()) {
                String singleCsid = getInvocationContext().getSingleCSID();
                if (Tools.isBlank(singleCsid)) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                } else {
                    csids.add(singleCsid);
                }
            } else if (requestIsForInvocationModeList()) {
                List<String> listCsids = getListCsids();
                if (listCsids.isEmpty()) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                }
                csids.addAll(listCsids);
            } else if (requestIsForInvocationModeGroup()) {
                String groupCsid = getInvocationContext().getGroupCSID();
                if (Tools.isBlank(groupCsid)) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                }
                List<String> groupMemberCsids = getMemberCsidsFromGroup(CollectionObjectClient.SERVICE_NAME, groupCsid);
                if (groupMemberCsids.isEmpty()) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                }
                csids.addAll(groupMemberCsids);
            } else if (requestIsForInvocationModeNoContext()) {
                List<String> noContextCsids = getNoContextCsids();
                if (noContextCsids.isEmpty()) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT);
                }
                csids.addAll(noContextCsids);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Identified " + csids.size() + " total CollectionObject(s) to be processed via the " + CLASSNAME + " batch job");
            }

            // Update the value of the computed current location field for each CollectionObject
            setResults(updateComputedCurrentLocations(csids));
            setCompletionStatus(STATUS_COMPLETE);

        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
        }

    }

    private InvocationResults updateComputedCurrentLocations(List<String> csids) {
        ResourceMap resourcemap = getResourceMap();
        ResourceBase collectionObjectResource = resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        ResourceBase movementResource = resourcemap.get(MovementClient.SERVICE_NAME);
        String computedCurrentLocation;
        int numUpdated = 0;

        try {

            // For each CollectionObject record
            for (String collectionObjectCsid : csids) {

                // FIXME: Optionally set competition status here to
                // indicate what percentage of records have been processed.

                // Skip over soft-deleted CollectionObject records
                //
                // (Invocations using the 'no context' mode have already
                // filtered out soft-deleted records.)
                if (!requestIsForInvocationModeNoContext()) {
                    if (isRecordDeleted(collectionObjectResource, collectionObjectCsid)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Skipping soft-deleted CollectionObject record with CSID " + collectionObjectCsid);
                        }
                        continue;
                    }
                }
                // Get the Movement records related to this CollectionObject record
                AbstractCommonList relatedMovements =
                        getRelatedRecords(movementResource, collectionObjectCsid, EXCLUDE_DELETED);
                // Skip over CollectionObject records that have no related Movement records
                if (relatedMovements.getListItem().isEmpty()) {
                    continue;
                }
                // Get the most recent 'suitable' Movement record, one which
                // contains both a location date and a current location value
                AbstractCommonList.ListItem mostRecentMovement = getMostRecentMovement(relatedMovements);
                // Skip over CollectionObject records where no suitable
                // most recent Movement record can be identified.
                //
                // FIXME: Clarify: it ever necessary to 'unset' a computed
                // current location value, by setting it to a null or empty value,
                // if that value is no longer obtainable from related Movement
                // records, if any?
                if (mostRecentMovement == null) {
                    continue;
                }
                // Update the value of the computed current location field
                // (and, via subclasses, this and/or other relevant fields)
                // in the CollectionObject record
                numUpdated = updateCollectionObjectValues(collectionObjectResource,
                        collectionObjectCsid, mostRecentMovement, resourcemap, numUpdated);
            }

        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage() + " ";
            errMsg = errMsg + "Successfully updated " + numUpdated + " CollectionObject record(s) prior to error.";
            logger.error(errMsg);
            setErrorResult(errMsg);
            getResults().setNumAffected(numUpdated);
            return getResults();
        }

        logger.info("Updated computedCurrentLocation values in " + numUpdated + " CollectionObject record(s).");
        getResults().setNumAffected(numUpdated);
        return getResults();
    }

    private AbstractCommonList.ListItem getMostRecentMovement(AbstractCommonList relatedMovements) {
        Set<String> alreadyProcessedMovementCsids = new HashSet<String>();
        AbstractCommonList.ListItem mostRecentMovement = null;
        String movementCsid;
        String currentLocation;
        String locationDate;
        String mostRecentLocationDate = "";
        for (AbstractCommonList.ListItem movementListItem : relatedMovements.getListItem()) {
            movementCsid = AbstractCommonListUtils.ListItemGetElementValue(movementListItem, CSID_ELEMENT_NAME);
            if (Tools.isBlank(movementCsid)) {
                continue;
            }
            // Skip over any duplicates in the list, such as records that might
            // appear as the subject of one relation record and the object of
            // its reciprocal relation record
            if (alreadyProcessedMovementCsids.contains(movementCsid)) {
                continue;
            } else {
                alreadyProcessedMovementCsids.add(movementCsid);
            }
            locationDate = AbstractCommonListUtils.ListItemGetElementValue(movementListItem, LOCATION_DATE_ELEMENT_NAME);
            if (Tools.isBlank(locationDate)) {
                continue;
            }
            currentLocation = AbstractCommonListUtils.ListItemGetElementValue(movementListItem, CURRENT_LOCATION_ELEMENT_NAME);
            if (Tools.isBlank(currentLocation)) {
                continue;
            }
            // FIXME: Add optional validation here that this Movement record's
            // currentLocation value parses successfully as an item refName,
            // before identifying that record as the most recent Movement.
            // Consider making this optional validation, in turn dependent on the
            // value of a parameter passed in during batch job invocation.
            if (logger.isTraceEnabled()) {
                logger.trace("Location date value = " + locationDate);
                logger.trace("Current location value = " + currentLocation);
            }
            // If this record's location date value is more recent than that of other
            // Movement records processed so far, set the current Movement record
            // as the most recent Movement.
            //
            // The following comparison assumes that all values for this element/field
            // will be consistent ISO 8601 date/time representations, each of which can
            // be ordered via string comparison.
            //
            // If this is *not* the case, we should instead parse and convert these values
            // to date/time objects.
            if (locationDate.compareTo(mostRecentLocationDate) > 0) {
                mostRecentLocationDate = locationDate;
                mostRecentMovement = movementListItem;
            }

        }
        return mostRecentMovement;
    }

    // This method can be overridden and extended to update a custom set of
    // values in the CollectionObject record by pulling in values from its
    // most recent related Movement record.
    //
    // Note: any such values must first be exposed in Movement list items,
    // in turn via configuration in Services tenant bindings ("listResultsField").
    protected int updateCollectionObjectValues(ResourceBase collectionObjectResource,
            String collectionObjectCsid, AbstractCommonList.ListItem mostRecentMovement,
            ResourceMap resourcemap, int numUpdated)
            throws DocumentException, URISyntaxException {
        PoxPayloadOut collectionObjectPayload;
        String computedCurrentLocation;
        String objectNumber;
        String previousComputedCurrentLocation;

        collectionObjectPayload = findByCsid(collectionObjectResource, collectionObjectCsid);
        if (Tools.isBlank(collectionObjectPayload.toXML())) {
            return numUpdated;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Payload: " + "\n" + collectionObjectPayload);
            }
        }
        // Perform the update only if the computed current location value will change
        // as a result of the update
        computedCurrentLocation =
                AbstractCommonListUtils.ListItemGetElementValue(mostRecentMovement, CURRENT_LOCATION_ELEMENT_NAME);
        previousComputedCurrentLocation = getFieldElementValue(collectionObjectPayload,
                COLLECTIONOBJECTS_COMMON_SCHEMA_NAME, COLLECTIONOBJECTS_COMMON_NAMESPACE,
                COMPUTED_CURRENT_LOCATION_ELEMENT_NAME);
        if (!shouldUpdateLocation(previousComputedCurrentLocation, computedCurrentLocation)) {
            return numUpdated;
        }
    
        // Perform the update only if there is a non-blank object number available.
        //
        // In the default CollectionObject validation handler, the object number
        // is a required field and its (non-blank) value must be present in update
        // payloads to successfully perform an update.
        objectNumber = getFieldElementValue(collectionObjectPayload,
                COLLECTIONOBJECTS_COMMON_SCHEMA_NAME, COLLECTIONOBJECTS_COMMON_NAMESPACE,
                OBJECT_NUMBER_ELEMENT_NAME);
        if (logger.isTraceEnabled()) {
            logger.trace("Object number: " + objectNumber);
        }
        // FIXME: Consider making the requirement that a non-blank object number
        // be present dependent on the value of a parameter passed in during
        // batch job invocation, as some implementations may have turned off that
        // validation requirement.
        if (Tools.isBlank(objectNumber)) {
            return numUpdated;
        }

        // Update the location.
        // (Updated location values can legitimately be blank, to 'null out' existing locations.)  
        if (computedCurrentLocation == null) {
            computedCurrentLocation = "";
        }

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
        numUpdated++;
        if (logger.isTraceEnabled()) {
            logger.trace("Computed current location value for CollectionObject " + collectionObjectCsid
                    + " was set to " + computedCurrentLocation);

        }
        return numUpdated;
    }
    
    protected boolean shouldUpdateLocation(String previousLocation, String currentLocation) {
        boolean shouldUpdate = true;
        if (Tools.isBlank(previousLocation) && Tools.isBlank(currentLocation)) {
            shouldUpdate = false;
        } else if (Tools.notBlank(previousLocation) && previousLocation.equals(currentLocation)) {
            shouldUpdate = false;
        }
        return shouldUpdate;
    }

    // #################################################################
    // Ray Lee's convenience methods from his AbstractBatchJob class for the
    // UC Berkeley Botanical Garden v2.4 implementation.
    // #################################################################
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

    // #################################################################
    // Other convenience methods
    // #################################################################
    protected UriInfo createRelatedRecordsUriInfo(String queryString) throws URISyntaxException {
        URI uri = new URI(null, null, null, queryString, null);
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

    private boolean isRecordDeleted(ResourceBase resource, String collectionObjectCsid)
            throws URISyntaxException, DocumentException {
        boolean isDeleted = false;
        byte[] workflowResponse = resource.getWorkflow(createUriInfo(), collectionObjectCsid);
        if (workflowResponse != null) {
            PoxPayloadOut payloadOut = new PoxPayloadOut(workflowResponse);
            String workflowState =
                    getFieldElementValue(payloadOut, WORKFLOW_COMMON_SCHEMA_NAME,
                    WORKFLOW_COMMON_NAMESPACE, LIFECYCLE_STATE_ELEMENT_NAME);
            if (Tools.notBlank(workflowState) && workflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
                isDeleted = true;
            }
        }
        return isDeleted;
    }

    private UriInfo addFilterToExcludeSoftDeletedRecords(UriInfo uriInfo) throws URISyntaxException {
        if (uriInfo == null) {
            uriInfo = createUriInfo();
        }
        uriInfo.getQueryParameters().add(WorkflowClient.WORKFLOW_QUERY_NONDELETED, Boolean.FALSE.toString());
        return uriInfo;
    }

    private AbstractCommonList getRecordsRelatedToCsid(ResourceBase resource, String csid,
            String relationshipDirection, boolean excludeDeletedRecords) throws URISyntaxException {
        UriInfo uriInfo = createUriInfo();
        uriInfo.getQueryParameters().add(relationshipDirection, csid);
        if (excludeDeletedRecords) {
            uriInfo = addFilterToExcludeSoftDeletedRecords(uriInfo);
        }
        // The 'resource' type used here identifies the record type of the
        // related records to be retrieved
        AbstractCommonList relatedRecords = resource.getList(uriInfo);
        if (logger.isTraceEnabled()) {
            logger.trace("Identified " + relatedRecords.getTotalItems()
                    + " record(s) related to the object record via direction " + relationshipDirection + " with CSID " + csid);
        }
        return relatedRecords;
    }

    /**
     * Returns the records of a specified type that are related to a specified
     * record, where that record is the object of the relation.
     *
     * @param resource a resource. The type of this resource determines the type
     * of related records that are returned.
     * @param csid a CSID identifying a record
     * @param excludeDeletedRecords true if 'soft-deleted' records should be
     * excluded from results; false if those records should be included
     * @return a list of records of a specified type, related to a specified
     * record
     * @throws URISyntaxException
     */
    private AbstractCommonList getRecordsRelatedToObjectCsid(ResourceBase resource, String csid, boolean excludeDeletedRecords) throws URISyntaxException {
        return getRecordsRelatedToCsid(resource, csid, IQueryManager.SEARCH_RELATED_TO_CSID_AS_OBJECT, excludeDeletedRecords);
    }

    /**
     * Returns the records of a specified type that are related to a specified
     * record, where that record is the subject of the relation.
     *
     * @param resource a resource. The type of this resource determines the type
     * of related records that are returned.
     * @param csid a CSID identifying a record
     * @param excludeDeletedRecords true if 'soft-deleted' records should be
     * excluded from results; false if those records should be included
     * @return a list of records of a specified type, related to a specified
     * record
     * @throws URISyntaxException
     */
    private AbstractCommonList getRecordsRelatedToSubjectCsid(ResourceBase resource, String csid, boolean excludeDeletedRecords) throws URISyntaxException {
        return getRecordsRelatedToCsid(resource, csid, IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT, excludeDeletedRecords);
    }

    private AbstractCommonList getRelatedRecords(ResourceBase resource, String csid, boolean excludeDeletedRecords)
            throws URISyntaxException, DocumentException {
        AbstractCommonList relatedRecords = new AbstractCommonList();
        AbstractCommonList recordsRelatedToObjectCSID = getRecordsRelatedToObjectCsid(resource, csid, excludeDeletedRecords);
        AbstractCommonList recordsRelatedToSubjectCSID = getRecordsRelatedToSubjectCsid(resource, csid, excludeDeletedRecords);
        // If either list contains any related records, merge in its items
        if (recordsRelatedToObjectCSID.getListItem().size() > 0) {
            relatedRecords.getListItem().addAll(recordsRelatedToObjectCSID.getListItem());
        }
        if (recordsRelatedToSubjectCSID.getListItem().size() > 0) {
            relatedRecords.getListItem().addAll(recordsRelatedToSubjectCSID.getListItem());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Identified a total of " + relatedRecords.getListItem().size()
                    + " record(s) related to the record with CSID " + csid);
        }
        return relatedRecords;
    }

    private List<String> getCsidsList(AbstractCommonList list) {
        List<String> csids = new ArrayList<String>();
        for (AbstractCommonList.ListItem listitem : list.getListItem()) {
            csids.add(AbstractCommonListUtils.ListItemGetCSID(listitem));
        }
        return csids;
    }

    private List<String> getMemberCsidsFromGroup(String serviceName, String groupCsid) throws URISyntaxException, DocumentException {
        ResourceMap resourcemap = getResourceMap();
        ResourceBase resource = resourcemap.get(serviceName);
        return getMemberCsidsFromGroup(resource, groupCsid);
    }

    private List<String> getMemberCsidsFromGroup(ResourceBase resource, String groupCsid) throws URISyntaxException, DocumentException {
        // The 'resource' type used here identifies the record type of the
        // related records to be retrieved
        AbstractCommonList relatedRecords =
                getRelatedRecords(resource, groupCsid, EXCLUDE_DELETED);
        List<String> memberCsids = getCsidsList(relatedRecords);
        return memberCsids;
    }

    private List<String> getNoContextCsids() throws URISyntaxException {
        ResourceMap resourcemap = getResourceMap();
        ResourceBase collectionObjectResource = resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        UriInfo uriInfo = createUriInfo();
        uriInfo = addFilterToExcludeSoftDeletedRecords(uriInfo);
        AbstractCommonList collectionObjects = collectionObjectResource.getList(uriInfo);
        List<String> noContextCsids = getCsidsList(collectionObjects);
        return noContextCsids;
    }
}
