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
import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.query.UriInfoImpl;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.dom4j.DocumentException;
//import org.jboss.resteasy.specimpl.UriInfoImpl;
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
    private final static String UPDATE_DATE_ELEMENT_NAME = "updatedAt";
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
	private static final int DEFAULT_PAGE_SIZE = 1000;
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
        NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        NuxeoBasedResource movementResource = (NuxeoBasedResource) resourcemap.get(MovementClient.SERVICE_NAME);
        long numUpdated = 0;
        long processed = 0;

        long recordsToProcess = csids.size();
        long logInterval = recordsToProcess / 10 + 2;
        try {

            // For each CollectionObject record
            for (String collectionObjectCsid : csids) {

            	// Log progress at INFO level
            	if (processed % logInterval == 0) {
	            	logger.info(String.format("Recalculated computed location for %d of %d cataloging records.",
	            			processed, recordsToProcess));
            	}
            	processed++;

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

    //
    // Returns the number of distinct/unique CSID values in the list
    //
    private int getNumberOfDistinceRecords(AbstractCommonList abstractCommonList) {
    	Set<String> resultSet = new HashSet<String>();

        for (AbstractCommonList.ListItem listItem : abstractCommonList.getListItem()) {
	        String csid = AbstractCommonListUtils.ListItemGetElementValue(listItem, CSID_ELEMENT_NAME);
	        if (!Tools.isBlank(csid)) {
	            resultSet.add(csid);
	        }
        }

        return resultSet.size();
    }

    private AbstractCommonList.ListItem getMostRecentMovement(AbstractCommonList relatedMovements) {
        Set<String> alreadyProcessedMovementCsids = new HashSet<String>();
        AbstractCommonList.ListItem mostRecentMovement = null;
        String movementCsid;
        String currentLocation;
        String locationDate;
        String updateDate;
        String mostRecentLocationDate = "";
        String comparisonUpdateDate = "";

        //
        // If there is only one related movement record, then return it as the most recent
        // movement record -if it's current location element is not empty.
        //
        if (getNumberOfDistinceRecords(relatedMovements) == 1) {
        	mostRecentMovement = relatedMovements.getListItem().get(0);
            currentLocation = AbstractCommonListUtils.ListItemGetElementValue(mostRecentMovement, CURRENT_LOCATION_ELEMENT_NAME);
            if (Tools.isBlank(currentLocation)) {
            	mostRecentMovement = null;
            }
            return mostRecentMovement;
        }

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
            updateDate = AbstractCommonListUtils.ListItemGetElementValue(movementListItem, UPDATE_DATE_ELEMENT_NAME);
            if (Tools.isBlank(updateDate)) {
                continue;
            }
            currentLocation = AbstractCommonListUtils.ListItemGetElementValue(movementListItem, CURRENT_LOCATION_ELEMENT_NAME);
            if (Tools.isBlank(currentLocation)) {
                continue;
            }
            // Validate that this Movement record's currentLocation value parses
            // successfully as an item refName, before identifying that record
            // as the most recent Movement.
            //
            // TODO: Consider making this optional validation, in turn dependent on the
            // value of a parameter passed in during batch job invocation.
            if (RefNameUtils.parseAuthorityTermInfo(currentLocation) == null) {
                logger.warn(String.format("Could not parse current location refName '%s' in Movement record",
                    currentLocation));
                 continue;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Location date value = " + locationDate);
                logger.trace("Update date value = " + updateDate);
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
                comparisonUpdateDate = updateDate;
            } else if (locationDate.compareTo(mostRecentLocationDate) == 0) {
                // If the two location dates match, then use a tiebreaker
                if (updateDate.compareTo(comparisonUpdateDate) > 0) {
                    // The most recent location date value doesn't need to be
                    // updated here, as the two records' values are identical
                    mostRecentMovement = movementListItem;
                    comparisonUpdateDate = updateDate;
                }
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
    protected long updateCollectionObjectValues(NuxeoBasedResource collectionObjectResource,
            String collectionObjectCsid,
            AbstractCommonList.ListItem mostRecentMovement,
            ResourceMap resourcemap, long numUpdated)
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

        // At this point in the code, the most recent related Movement record
        // should not have a null current location, as such records are
        // excluded from consideration altogether in getMostRecentMovement().
        // This is a redundant fallback check, in case that code somehow fails
        // or is modified or deleted.
        if (computedCurrentLocation == null) {
            return numUpdated;
        }

        // Update the location.
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

        UriInfo uriInfo = this.setupQueryParamForUpdateRecords(); // Determines if we'll updated the updateAt and updatedBy core values
        byte[] responseBytes = collectionObjectResource.update(getServiceContext(), resourcemap, uriInfo, collectionObjectCsid,
                collectionObjectUpdatePayload);
        if (logger.isDebugEnabled()) {
	        logger.debug(String.format("Batch resource: Resonse from collectionobject (cataloging record) update: %s", new String(responseBytes)));
        }
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
        NuxeoBasedResource resource = (NuxeoBasedResource) getResourceMap().get(serviceName);
        return findByCsid(resource, csid);
    }

    protected PoxPayloadOut findByCsid(NuxeoBasedResource resource, String csid) throws URISyntaxException, DocumentException {
    	PoxPayloadOut result = null;

    	try {
			result = resource.getWithParentCtx(getServiceContext(), csid);
		} catch (Exception e) {
			String msg = String.format("UpdateObjectLocation batch job could find/get resource CSID='%s' of type '%s'",
					csid, resource.getServiceName());
			if (logger.isDebugEnabled()) {
				logger.debug(msg, e);
			} else {
				logger.error(msg);
			}
		}

    	return result;
    }

    protected UriInfo createUriInfo() throws URISyntaxException {
        return createUriInfo("");
    }

    private UriInfo createUriInfo(String queryString) throws URISyntaxException {
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

    protected UriInfo setupQueryParamForUpdateRecords() throws URISyntaxException {
    	UriInfo result = null;

    	//
    	// Check first to see if we've got a query param.  It will override any invocation context value
    	//
    	String updateCoreValues = (String) getServiceContext().getQueryParams().getFirst(IClientQueryParams.UPDATE_CORE_VALUES);
    	if (Tools.isBlank(updateCoreValues)) {
    		//
    		// Since there is no query param, let's check the invocation context
    		//
    		updateCoreValues = getInvocationContext().getUpdateCoreValues();
    	}

    	//
    	// If we found a value, then use it to create a query parameter
    	//
    	if (Tools.notBlank(updateCoreValues)) {
        	result = createUriInfo(IClientQueryParams.UPDATE_CORE_VALUES + "=" + updateCoreValues);
    	}

    	return result;
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

    private boolean isRecordDeleted(NuxeoBasedResource resource, String collectionObjectCsid)
            throws URISyntaxException, DocumentException {
        boolean isDeleted = false;

        byte[] workflowResponse = resource.getWorkflowWithExistingContext(getServiceContext(), createUriInfo(), collectionObjectCsid);
        if (workflowResponse != null) {
            PoxPayloadOut payloadOut = new PoxPayloadOut(workflowResponse);
            String workflowState =
                    getFieldElementValue(payloadOut, WORKFLOW_COMMON_SCHEMA_NAME,
                    WORKFLOW_COMMON_NAMESPACE, LIFECYCLE_STATE_ELEMENT_NAME);
            if (Tools.notBlank(workflowState) && workflowState.contains(WorkflowClient.WORKFLOWSTATE_DELETED)) {
                isDeleted = true;
            }
        }

        return isDeleted;
    }

    private UriInfo addFilterToExcludeSoftDeletedRecords(UriInfo uriInfo) throws URISyntaxException {
        if (uriInfo == null) {
            uriInfo = createUriInfo();
        }
        uriInfo.getQueryParameters().add(WorkflowClient.WORKFLOW_QUERY_DELETED_QP, Boolean.FALSE.toString());
        return uriInfo;
    }

    private UriInfo addFilterForPageSize(UriInfo uriInfo, long startPage, long pageSize) throws URISyntaxException {
    	if (uriInfo == null) {
            uriInfo = createUriInfo();
        }
        uriInfo.getQueryParameters().addFirst(IClientQueryParams.START_PAGE_PARAM, Long.toString(startPage));
        uriInfo.getQueryParameters().addFirst(IClientQueryParams.PAGE_SIZE_PARAM, Long.toString(pageSize));

        return uriInfo;
    }

    private AbstractCommonList getRecordsRelatedToCsid(NuxeoBasedResource resource, String csid,
            String relationshipDirection, boolean excludeDeletedRecords) throws URISyntaxException {
        UriInfo uriInfo = createUriInfo();
        uriInfo.getQueryParameters().add(relationshipDirection, csid);
        if (excludeDeletedRecords) {
            uriInfo = addFilterToExcludeSoftDeletedRecords(uriInfo);
        }
        // The 'resource' type used here identifies the record type of the
        // related records to be retrieved
        AbstractCommonList relatedRecords = resource.getList(getServiceContext(), uriInfo);
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
    private AbstractCommonList getRecordsRelatedToObjectCsid(NuxeoBasedResource resource, String csid, boolean excludeDeletedRecords) throws URISyntaxException {
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
    private AbstractCommonList getRecordsRelatedToSubjectCsid(NuxeoBasedResource resource, String csid, boolean excludeDeletedRecords) throws URISyntaxException {
        return getRecordsRelatedToCsid(resource, csid, IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT, excludeDeletedRecords);
    }

    private AbstractCommonList getRelatedRecords(NuxeoBasedResource resource, String csid, boolean excludeDeletedRecords)
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

    private void appendItemsToCsidsList(List<String> existingList, AbstractCommonList abstractCommonList) {
        for (AbstractCommonList.ListItem listitem : abstractCommonList.getListItem()) {
        	existingList.add(AbstractCommonListUtils.ListItemGetCSID(listitem));
        }
    }

    private List<String> getMemberCsidsFromGroup(String serviceName, String groupCsid) throws URISyntaxException, DocumentException {
        ResourceMap resourcemap = getResourceMap();
        NuxeoBasedResource resource = (NuxeoBasedResource) resourcemap.get(serviceName);
        return getMemberCsidsFromGroup(resource, groupCsid);
    }

    private List<String> getMemberCsidsFromGroup(NuxeoBasedResource resource, String groupCsid) throws URISyntaxException, DocumentException {
        // The 'resource' type used here identifies the record type of the
        // related records to be retrieved
        AbstractCommonList relatedRecords =
                getRelatedRecords(resource, groupCsid, EXCLUDE_DELETED);
        List<String> memberCsids = getCsidsList(relatedRecords);
        return memberCsids;
    }

    private List<String> getNoContextCsids() throws URISyntaxException {
        ResourceMap resourcemap = getResourceMap();
        NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        UriInfo uriInfo = createUriInfo();
        uriInfo = addFilterToExcludeSoftDeletedRecords(uriInfo);

        boolean morePages = true;
        long currentPage = 0;
        long pageSize = DEFAULT_PAGE_SIZE;
        List<String> noContextCsids = new ArrayList<String>();

        while (morePages == true) {
	        uriInfo = addFilterForPageSize(uriInfo, currentPage, pageSize);
	        AbstractCommonList collectionObjects = collectionObjectResource.getList(getServiceContext(), uriInfo);
	        appendItemsToCsidsList(noContextCsids, collectionObjects);

	        if (collectionObjects.getItemsInPage() == pageSize) { // We know we're at the last page when the number of items returned in the last request is less than the page size.
	        	currentPage++;
	        } else {
	        	morePages = false;
	        }
        }

        return noContextCsids;
    }
}
