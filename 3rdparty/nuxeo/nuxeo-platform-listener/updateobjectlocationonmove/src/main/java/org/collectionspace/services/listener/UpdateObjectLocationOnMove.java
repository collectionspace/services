package org.collectionspace.services.listener;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateObjectLocationOnMove implements EventListener {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    private final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);
    // FIXME: Make the following message, or its equivalent, a constant usable by all event listeners
    private final String NO_FURTHER_PROCESSING_MESSAGE =
            "This event listener will not continue processing this event ...";
    private final List<String> relevantDocTypesList = new ArrayList<String>();
    GregorianCalendar EARLIEST_COMPARISON_DATE = new GregorianCalendar(1600, 1, 1);
    private final static String RELATIONS_COMMON_SCHEMA = "relations_common"; // FIXME: Get from external constant
    private final static String RELATION_DOCTYPE = "Relation"; // FIXME: Get from external constant
    private final static String SUBJECT_CSID_PROPERTY = "subjectCsid"; // FIXME: Get from external constant
    private final static String OBJECT_CSID_PROPERTY = "objectCsid"; // FIXME: Get from external constant
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common"; // FIXME: Get from external constant
    private final static String COLLECTIONOBJECT_DOCTYPE = "CollectionObject"; // FIXME: Get from external constant
    private final static String COMPUTED_CURRENT_LOCATION_PROPERTY = "computedCurrentLocation"; // FIXME: Create and then get from external constant
    private final static String MOVEMENTS_COMMON_SCHEMA = "movements_common"; // FIXME: Get from external constant
    private final static String MOVEMENT_DOCTYPE = MovementConstants.NUXEO_DOCTYPE;
    private final static String LOCATION_DATE_PROPERTY = "locationDate"; // FIXME: Get from external constant
    private final static String CURRENT_LOCATION_PROPERTY = "currentLocation"; // FIXME: Get from external constant
    private final static String ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT =
            "AND (ecm:currentLifeCycleState <> 'deleted') "
            + "AND ecm:isProxy = 0 "
            + "AND ecm:isCheckedInVersion = 0";

    // FIXME: Per Rick, what happens if a relation record is created,
    // updated, deleted, or transitioned to a different workflow state,
    // that effectively either adds or removes a relation between a Movement
    // record and a CollectionObject record?  Do we need to listen
    // for that event as well and update the CollectionObject record's
    // computedCurrentLocation accordingly?
    //
    // The following code is currently only handling events affecting
    // Movement records.  One possible approach is to add a companion
    // event handler for Relation records, also registered as an event listener
    // via configuration in the same document bundle as this event handler.
    
    // FIXME: The following code handles the computation of current locations
    // for CollectionObject records on creation, update, and soft deletion
    // (workflow transition to 'deleted' state) of related Movement records.
    // It does not yet been configured or tested to also handle outright
    // deletion of related Movement records.
    @Override
    public void handleEvent(Event event) throws ClientException {

        logger.trace("In handleEvent in UpdateObjectLocationOnMove ...");

        EventContext eventContext = event.getContext();
        if (eventContext == null) {
            return;
        }

        if (!(eventContext instanceof DocumentEventContext)) {
            return;
        }
        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();

        // If this event does not involve one of the relevant doctypes
        // designated as being handled by this event listener, return
        // without further handling the event.
        //
        // FIXME: We will likely need to add the Relation doctype here,
        // along with additional code to handle such changes. (See comment above.)
        // As an alternative, we could create a second event handler for
        // doing so, also registered as an event listener via configuration
        // in the same document bundle as this event handler. If so, the
        // code below could be simplified to be a single check, rather than
        // iterating through a list.
        boolean involvesRelevantDocType = false;
        relevantDocTypesList.add(MovementConstants.NUXEO_DOCTYPE);
        for (String docType : relevantDocTypesList) {
            if (documentMatchesType(docModel, docType)) {
                involvesRelevantDocType = true;
                break;
            }
        }
        if (!involvesRelevantDocType) {
            return;
        }
        
        if (logger.isTraceEnabled()) {
            logger.trace("An event involving a document of the relevant type(s) was received by UpdateObjectLocationOnMove ...");
        }
        
        // Note: currently, all Document lifecycle transitions on
        // the relevant doctype(s) are handled by this event handler,
        // not just transitions between 'soft deleted' and active states.
        // We are assuming that we'll want to re-compute current locations
        // for related CollectionObjects on any such transitions.
        //
        // If we need to filter out some of those lifecycle transitions,
        // we can add additional checks for doing so at this point.


        // Find CollectionObject records that are related to this Movement record:
        //
        // Via an NXQL query, get a list of active relation records where:
        // * This movement record's CSID is the subject CSID of the relation,
        //   and its object document type is a CollectionObject doctype;
        // or
        // * This movement record's CSID is the object CSID of the relation,
        //   and its subject document type is a CollectionObject doctype.
        String movementCsid = NuxeoUtils.getCsid(docModel);
        CoreSession coreSession = docEventContext.getCoreSession();
        // Some values below are hard-coded for readability, rather than
        // being obtained from constants.
        String query = String.format(
                "SELECT * FROM %1$s WHERE " // collectionspace_core:tenantId = 1 "
                + "("
                + "  (%2$s:subjectCsid = '%3$s' "
                + "  AND %2$s:objectDocumentType = '%4$s') "
                + " OR "
                + "  (%2$s:objectCsid = '%3$s' "
                + "  AND %2$s:subjectDocumentType = '%4$s') "
                + ")"
                + ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT,
                RELATION_DOCTYPE, RELATIONS_COMMON_SCHEMA, movementCsid, COLLECTIONOBJECT_DOCTYPE);
        DocumentModelList relatedDocModels = coreSession.query(query);
        if (relatedDocModels == null || relatedDocModels.isEmpty()) {
            return;
        }

        // Iterate through the list of Relation records found and build
        // a list of CollectionObject CSIDs, by extracting the object CSIDs
        // from those Relation records.

        // FIXME: The following code might be refactored into a generic 'get
        // values of a single property from a list of document models' method,
        // if this doesn't already exist.
        String csid = "";
        List<String> collectionObjectCsids = new ArrayList<String>();
        for (DocumentModel relatedDocModel : relatedDocModels) {
            csid = (String) relatedDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
            if (Tools.notBlank(csid)) {
                collectionObjectCsids.add(csid);
            }
        }
        if (collectionObjectCsids == null || collectionObjectCsids.isEmpty()) {
            logger.warn("Could not obtain any CSIDs of related CollectionObject records.");
            logger.warn(NO_FURTHER_PROCESSING_MESSAGE);
            return;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Found " + collectionObjectCsids.size() + " CSIDs of related CollectionObject records.");
            }
        }

        // Iterate through the list of CollectionObject CSIDs found.
        DocumentModel collectionObjectDocModel = null;
        String computedCurrentLocationRefName = "";
        Map<DocumentModel, String> docModelsToUpdate = new HashMap<DocumentModel, String>();
        for (String collectionObjectCsid : collectionObjectCsids) {

            collectionObjectDocModel = getDocModelFromCsid(coreSession, collectionObjectCsid);
            // Verify that the CollectionObject record is active.
            if (!isActiveDocument(collectionObjectDocModel)) {
                continue;
            }
            // Obtain the computed current location of that CollectionObject.
            computedCurrentLocationRefName = computeCurrentLocation(coreSession, collectionObjectCsid);
            if (logger.isTraceEnabled()) {
                logger.trace("computedCurrentLocation refName=" + computedCurrentLocationRefName);
            }

            // Check that the value returned, which is expected to be a
            // reference (refName) to a storage location authority term,
            // is, at a minimum:
            // * Non-null and non-blank. (We need to verify this assumption; can a
            //   CollectionObject's computed current location meaningfully be 'un-set'?)
            // * Capable of being successfully parsed by an authority item parser;
            //   that is, returning a non-null parse result.
            if ((Tools.notBlank(computedCurrentLocationRefName)
                    && (RefNameUtils.parseAuthorityTermInfo(computedCurrentLocationRefName) != null))) {
                if (logger.isTraceEnabled()) {
                    logger.trace("refName passes basic validation tests.");
                }

                // If the value returned from the function passes validation,
                // compare it to the value in the computedCurrentLocation
                // field of that CollectionObject.
                //
                // If the CollectionObject does not already have a
                // computedCurrentLocation value, or if the two values differ ...
                String existingComputedCurrentLocationRefName =
                        (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
                if (Tools.isBlank(existingComputedCurrentLocationRefName)
                        || !computedCurrentLocationRefName.equals(existingComputedCurrentLocationRefName)) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Existing computedCurrentLocation refName=" + existingComputedCurrentLocationRefName);
                        logger.trace("computedCurrentLocation refName requires updating.");
                    }
                    // ... set aside this CollectionObject's docModel and its new
                    // computed current location value for subsequent updating
                    docModelsToUpdate.put(collectionObjectDocModel, computedCurrentLocationRefName);
                }
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("computedCurrentLocation refName does NOT require updating.");
                }
            }

        }

        // For each CollectionObject docModel that has been set aside for updating,
        // update the value of its computedCurrentLocation field with its new,
        // computed current location.
        int collectionObjectsUpdated = 0;
        for (Map.Entry<DocumentModel, String> entry : docModelsToUpdate.entrySet()) {
            DocumentModel dmodel = entry.getKey();
            String newCurrentLocationValue = entry.getValue();
            dmodel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY, newCurrentLocationValue);
            coreSession.saveDocument(dmodel);
            collectionObjectsUpdated++;
            if (logger.isTraceEnabled()) {
                String afterUpdateComputedCurrentLocationRefName =
                        (String) dmodel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
                logger.trace("Following update, new computedCurrentLocation refName value=" + afterUpdateComputedCurrentLocationRefName);

            }
        }
        logger.info("Updated " + collectionObjectsUpdated + " CollectionObject record(s) with new computed current location(s).");
    }

    // FIXME: Generic methods like many of those below might be split off,
    // into an event utilities class, base classes, or otherwise. - ADR 2012-12-05
    //
    // FIXME: Identify whether the equivalent of the documentMatchesType utility
    // method is already implemented and substitute a call to the latter if so.
    // This may well already exist.
    /**
     * Identifies whether a document matches a supplied document type.
     *
     * @param docModel a document model.
     * @param docType a document type string.
     * @return true if the document matches the supplied document type; false if
     * it does not.
     */
    private boolean documentMatchesType(DocumentModel docModel, String docType) {
        if (docModel == null || Tools.isBlank(docType)) {
            return false;
        }
        if (docModel.getType().startsWith(docType)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Identifies whether a document is an active document; that is, if it is
     * not a versioned record; not a proxy (symbolic link to an actual record);
     * and not in the 'deleted' workflow state.
     *
     * (A note relating the latter: Nuxeo appears to send 'documentModified'
     * events even on workflow transitions, such when records are 'soft deleted'
     * by being transitioned to the 'deleted' workflow state.)
     *
     * @param docModel
     * @return true if the document is an active document; false if it is not.
     */
    private boolean isActiveDocument(DocumentModel docModel) {
        if (docModel == null) {
            return false;
        }
        boolean isActiveDocument = false;
        try {
            if (!docModel.isVersion()
                    && !docModel.isProxy()
                    && !docModel.getCurrentLifeCycleState().equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
                isActiveDocument = true;
            }
        } catch (ClientException ce) {
            logger.warn("Error while identifying whether document is an active document: ", ce);
        }
        return isActiveDocument;
    }

    /**
     * Returns a document model for a record identified by a CSID.
     *
     * @param session a repository session.
     * @param collectionObjectCsid a CollectionObject identifier (CSID)
     * @return a document model for the record identified by the supplied CSID.
     */
    private DocumentModel getDocModelFromCsid(CoreSession session, String collectionObjectCsid) {
        DocumentModelList collectionObjectDocModels = null;
        try {
            final String query = "SELECT * FROM "
                    + NuxeoUtils.BASE_DOCUMENT_TYPE
                    + " WHERE "
                    + NuxeoUtils.getByNameWhereClause(collectionObjectCsid);
            collectionObjectDocModels = session.query(query);
        } catch (Exception e) {
            logger.warn("Exception in query to get document model for CollectionObject: ", e);
        }
        if (collectionObjectDocModels == null || collectionObjectDocModels.isEmpty()) {
            logger.warn("Could not get document models for CollectionObject(s).");
        } else if (collectionObjectDocModels.size() != 1) {
            logger.debug("Found more than 1 document with CSID=" + collectionObjectCsid);
        }
        return collectionObjectDocModels.get(0);
    }

    // FIXME: A quick first pass, using an only partly query-based technique for
    // getting the current location, augmented by procedural code.
    //
    // Should be replaced by a more performant method, based entirely, or nearly so,
    // on a query.
    //
    // E.g. the following is a sample CMIS query for retrieving Movement records
    // related to a CollectionObject, which might serve as the basis for that query.
    /*
     "SELECT DOC.nuxeo:pathSegment, DOC.dc:title, REL.dc:title,"
     + "REL.relations_common:objectCsid, REL.relations_common:subjectCsid FROM Movement DOC "
     + "JOIN Relation REL ON REL.relations_common:objectCsid = DOC.nuxeo:pathSegment "
     + "WHERE REL.relations_common:subjectCsid = '5b4c617e-53a0-484b-804e' "
     + "AND DOC.nuxeo:isVersion = false "
     + "ORDER BY DOC.collectionspace_core:updatedAt DESC";
     */
    /**
     * Returns the computed current location for a CollectionObject.
     *
     * @param session a repository session.
     * @param collectionObjectCsid a CollectionObject identifier (CSID)
     * @throws ClientException
     * @return the computed current location for the CollectionObject identified
     * by the supplied CSID.
     */
    private String computeCurrentLocation(CoreSession session, String collectionObjectCsid)
            throws ClientException {
        String computedCurrentLocation = "";
        // Get Relation records for Movements related to this CollectionObject.
        //
        // Some values below are hard-coded for readability, rather than
        // being obtained from constants.
        String query = String.format(
                "SELECT * FROM %1$s WHERE " // collectionspace_core:tenantId = 1 "
                + "("
                + "  (%2$s:subjectCsid = '%3$s' "
                + "  AND %2$s:objectDocumentType = '%4$s') "
                + " OR "
                + "  (%2$s:objectCsid = '%3$s' "
                + "  AND %2$s:subjectDocumentType = '%4$s') "
                + ")"
                + ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT,
                RELATION_DOCTYPE, RELATIONS_COMMON_SCHEMA, collectionObjectCsid, MOVEMENT_DOCTYPE);
        DocumentModelList relatedDocModels = session.query(query);
        if (relatedDocModels == null || relatedDocModels.isEmpty()) {
            return computedCurrentLocation;
        } else {
        }
        // Iterate through related movement records, to get the CollectionObject's
        // computed current location from the related Movement record with the
        // most recent location date.
        GregorianCalendar mostRecentLocationDate = EARLIEST_COMPARISON_DATE;
        DocumentModel movementDocModel = null;
        String csid = "";
        String location = "";
        for (DocumentModel relatedDocModel : relatedDocModels) {
            // Due to the 'OR' operator in the query above, related Movement
            // record CSIDs may reside in either the subject or object CSID fields
            // of the relation record. Whichever CSID value doesn't match the
            // CollectionObject's CSID is inferred to be the related Movement record's CSID.
            csid = (String) relatedDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
            if (csid.equals(collectionObjectCsid)) {
                csid = (String) relatedDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
            }
            movementDocModel = getDocModelFromCsid(session, csid);
            // Verify that the Movement record is active. This will also exclude
            // versioned Movement records from the computation of the current
            // location, for tenants that are versioning such records.
            if (!isActiveDocument(movementDocModel)) {
                continue;
            }
            GregorianCalendar locationDate =
                    (GregorianCalendar) movementDocModel.getProperty(MOVEMENTS_COMMON_SCHEMA, LOCATION_DATE_PROPERTY);
            if (locationDate == null) {
                continue;
            }
            if (locationDate.after(mostRecentLocationDate)) {
                mostRecentLocationDate = locationDate;
                location = (String) movementDocModel.getProperty(MOVEMENTS_COMMON_SCHEMA, CURRENT_LOCATION_PROPERTY);
            }
            if (Tools.notBlank(location)) {
                computedCurrentLocation = location;
            }
        }
        return computedCurrentLocation;
    }
}