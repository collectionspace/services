package org.collectionspace.services.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.api.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.storage.JDBCTools;
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
    private final String DATABASE_RESOURCE_DIRECTORY_NAME = "db";
    // FIXME: Currently hard-coded; get this database name value from JDBC utilities or equivalent
    private final String DATABASE_SYSTEM_NAME = "postgresql";
    private final static String STORED_FUNCTION_NAME = "computecurrentlocation";
    private final static String SQL_FILENAME_EXTENSION = ".sql";
    private final String SQL_RESOURCE_PATH =
            DATABASE_RESOURCE_DIRECTORY_NAME + "/"
            + DATABASE_SYSTEM_NAME + "/"
            + STORED_FUNCTION_NAME + SQL_FILENAME_EXTENSION;
    // The name of the relevant column in the JDBC ResultSet is currently identical
    // to the function name, regardless of the 'SELECT ... AS' clause in the SQL query.
    private final static String COMPUTED_CURRENT_LOCATION_COLUMN = STORED_FUNCTION_NAME;
    // FIXME: Get this line separator value from already-declared constant elsewhere, if available
    private final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final static String RELATIONS_COMMON_SCHEMA = "relations_common"; // FIXME: Get from external constant
    final String RELATION_DOCTYPE = "Relation"; // FIXME: Get from external constant
    private final static String SUBJECT_CSID_PROPERTY = "subjectCsid"; // FIXME: Get from external constant
    private final static String OBJECT_CSID_PROPERTY = "objectCsid"; // FIXME: Get from external constant
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common"; // FIXME: Get from external constant
    final String COLLECTIONOBJECT_DOCTYPE = "CollectionObject"; // FIXME: Get from external constant
    final String COMPUTED_CURRENT_LOCATION_PROPERTY = "computedCurrentLocation"; // FIXME: Create and then get from external constant
    final String MOVEMENTS_COMMON_SCHEMA = "movements_common"; // FIXME: Get from external constant
    private final static String MOVEMENT_DOCTYPE = "Movement"; // FIXME: Get from external constant
    final String LOCATION_DATE_PROPERTY = "locationDate"; // FIXME: Get from external constant
    final String CURRENT_LOCATION_PROPERTY = "currentLocation"; // FIXME: Get from external constant

    // ####################################################################
    // FIXME: Per Rick, what happens if a relation record is updated,
    // that either adds or removes a relation between a Movement
    // record and a CollectionObject record?  Do we need to listen
    // for that event as well and update the CollectionObject record's
    // computedCurrentLocation accordingly?
    //
    // The following code is currently only handling create and
    // update events affecting Movement records.
    // ####################################################################
    // FIXME: We'll likely also need to handle workflow state transition and
    // deletion events, where the soft or hard deletion of a Movement or
    // Relation record effectively changes the current location for a CollectionObject.
    @Override
    public void handleEvent(Event event) throws ClientException {

        logger.trace("In handleEvent in UpdateObjectLocationOnMove ...");

        // FIXME: Check for database product type here.
        // If our database type is one for which we don't yet
        // have tested SQL code to perform this operation, return here.

        EventContext eventContext = event.getContext();
        if (eventContext == null) {
            return;
        }

        if (!(eventContext instanceof DocumentEventContext)) {
            logger.debug("This event does not involve a document ...");
            logger.debug(NO_FURTHER_PROCESSING_MESSAGE);
            return;
        }
        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();

        // If this event does not involve one of our relevant doctypes,
        // return without further handling the event.
        boolean involvesRelevantDocType = false;
        relevantDocTypesList.add(MovementConstants.NUXEO_DOCTYPE);
        // FIXME: We will likely need to add the Relation doctype here,
        // along with additional code to handle such changes.
        for (String docType : relevantDocTypesList) {
            if (documentMatchesType(docModel, docType)) {
                involvesRelevantDocType = true;
                break;
            }
        }
        logger.debug("This event involves a document of type " + docModel.getDocumentType().getName());
        if (!involvesRelevantDocType) {
            logger.debug("This event does not involve a document of a relevant type ...");
            logger.debug(NO_FURTHER_PROCESSING_MESSAGE);
            return;
        }
        if (!isActiveDocument(docModel)) {
            logger.debug("This event does not involve an active document ...");
            logger.debug(NO_FURTHER_PROCESSING_MESSAGE);
            return;
        }

        logger.debug("An event involving an active document of the relevant type(s) was received by UpdateObjectLocationOnMove ...");

        // Test whether a SQL function exists to supply the computed
        // current location of a CollectionObject.
        //
        // If the function does not exist in the database, load the
        // SQL command to create that function from a resource
        // available to this class, and run a JDBC command to create
        // that function in the database.
        //
        // For now, assume this function will be created in the
        // 'nuxeo' database.
        //
        // FIXME: Future work to create per-tenant repositories will
        // likely require that our JDBC statements connect to the
        // appropriate tenant-specific database.
        //
        // It doesn't appear we can reliably create this function via
        // 'ant create_nuxeo db' during the build process, because
        // there's a substantial likelihood at that point that
        // tables referred to by the function (e.g. movements_common
        // and collectionobjects_common) will not yet exist.
        // (PostgreSQL will not permit the function to be created if
        // any of its referred-to tables do not exist.)
        if (!storedFunctionExists(STORED_FUNCTION_NAME)) {
            logger.trace("Stored function " + STORED_FUNCTION_NAME + " does NOT exist in the database.");
            String sql = getStringFromResource(SQL_RESOURCE_PATH);
            if (Tools.isBlank(sql)) {
                logger.warn("Could not obtain SQL command to create stored function.");
                logger.debug(NO_FURTHER_PROCESSING_MESSAGE);
                return;
            }

            int result = -1;
            try {
                result = JDBCTools.executeUpdate(JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME), sql);
            } catch (Exception e) {
                // Do nothing here
                // FIXME: Need to verify that the original '-1' value is preserved if an Exception is caught here.
            }
            logger.trace("Result of executeUpdate=" + result);
            if (result < 0) {
                logger.warn("Could not create stored function in the database.");
                logger.debug(NO_FURTHER_PROCESSING_MESSAGE);
                return;
            } else {
                logger.info("Stored function " + STORED_FUNCTION_NAME + " was successfully created in the database.");
            }
        } else {
            logger.trace("Stored function " + STORED_FUNCTION_NAME + " already exists in the database.");
        }

        String movementCsid = NuxeoUtils.getCsid(docModel);
        logger.debug("Movement record CSID=" + movementCsid);

        // FIXME: Temporary, for debugging: check whether the movement record's
        // location date field value is stale in Nuxeo at this point
        GregorianCalendar cal = (GregorianCalendar) docModel.getProperty(MOVEMENTS_COMMON_SCHEMA, LOCATION_DATE_PROPERTY);
        logger.debug("location date=" + GregorianCalendarDateTimeUtils.formatAsISO8601Date(cal));

        // Find CollectionObject records that are related to this Movement record:
        //
        // Via an NXQL query, get a list of (non-deleted) relation records where:
        // * This movement record's CSID is the subject CSID of the relation.
        // * The object document type is a CollectionObject doctype.
        //
        // Note: this assumes that every such relation is captured by
        // relations with Movement-as-subject and CollectionObject-as-object,
        // logic that matches that of the SQL function to obtain the computed
        // current location of the CollectionObject.
        //
        // That may NOT always be the case; it's possible some such relations may
        // exist only with CollectionObject-as-subject and Movement-as-object.
        CoreSession coreSession1 = docEventContext.getCoreSession();
        CoreSession coreSession = docModel.getCoreSession();
        if (coreSession1 == coreSession || coreSession1.equals(coreSession)) {
            logger.debug("Core sessions are equal.");
        } else {
            logger.debug("Core sessions are NOT equal.");
        }

        // Check whether closing and opening a transaction here might
        // flush any hypothetical caching that Nuxeo is doing at this point

        String query = String.format(
                "SELECT * FROM %1$s WHERE " // collectionspace_core:tenantId = 1 "
                + "(relations_common:subjectCsid = '%2$s' "
                + "AND relations_common:objectDocumentType = '%3$s') "
                + "AND (ecm:currentLifeCycleState <> 'deleted') "
                + "AND ecm:isProxy = 0 "
                + "AND ecm:isCheckedInVersion = 0", RELATION_DOCTYPE, movementCsid, COLLECTIONOBJECT_DOCTYPE);
        DocumentModelList relatedDocModels = coreSession.query(query);
        if (relatedDocModels == null || relatedDocModels.isEmpty()) {
            return;
        } else {
            logger.trace("Found " + relatedDocModels.size() + " related documents.");
        }

        // Iterate through the list of Relation records found and build
        // a list of CollectionObject CSIDs, by extracting the object CSIDs
        // from those Relation records.

        // FIXME: The following code might be refactored into a generic 'get property
        // values from a list of document models' method, if this doesn't already exist.
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
            logger.debug(NO_FURTHER_PROCESSING_MESSAGE);
            return;
        } else {
            logger.debug("Found " + collectionObjectCsids.size() + " CSIDs of related CollectionObject records.");
        }

        // Iterate through the list of CollectionObject CSIDs found.
        DocumentModel collectionObjectDocModel = null;
        String computedCurrentLocationRefName = "";
        Map<DocumentModel, String> docModelsToUpdate = new HashMap<DocumentModel, String>();
        for (String collectionObjectCsid : collectionObjectCsids) {

            // Verify that the CollectionObject record is active.
            collectionObjectDocModel = getDocModelFromCsid(coreSession, collectionObjectCsid);
            if (!isActiveDocument(collectionObjectDocModel)) {
                continue;
            }

            // Obtain the computed current location of that CollectionObject.
            //
            // JDBC/SQL query method:
            // computedCurrentLocationRefName = computeCurrentLocation(collectionObjectCsid);
            //
            // Nuxeo (NXQL or CMIS) query method, currently with some
            // non-performant procedural augmentation:
            computedCurrentLocationRefName = computeCurrentLocation(coreSession, collectionObjectCsid, movementCsid);
            logger.debug("computedCurrentLocation refName=" + computedCurrentLocationRefName);

            // Check that the value returned from the SQL function, which
            // is expected to be a reference (refName) to a storage location
            // authority term, is, at a minimum:
            // * Non-null and non-blank. (We need to verify this assumption; can a
            //   CollectionObject's computed current location meaningfully be 'un-set'?)
            // * Capable of being successfully parsed by an authority item parser;
            //   that is, returning a non-null parse result.
            if ((Tools.notBlank(computedCurrentLocationRefName)
                    && (RefNameUtils.parseAuthorityTermInfo(computedCurrentLocationRefName) != null))) {
                logger.debug("refName passes basic validation tests.");

                // If the value returned from the function passes validation,
                // compare that value to the value in the computedCurrentLocation
                // field of that CollectionObject
                //
                // If the CollectionObject does not already have a
                // computedCurrentLocation value, or if the two values differ ...
                String existingComputedCurrentLocationRefName =
                        (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
                if (Tools.isBlank(existingComputedCurrentLocationRefName)
                        || !computedCurrentLocationRefName.equals(existingComputedCurrentLocationRefName)) {
                    logger.debug("Existing computedCurrentLocation refName=" + existingComputedCurrentLocationRefName);
                    logger.debug("computedCurrentLocation refName requires updating.");
                    // ... identify this CollectionObject's docModel and new field value for subsequent updating
                    docModelsToUpdate.put(collectionObjectDocModel, computedCurrentLocationRefName);
                }
            } else {
                logger.debug("computedCurrentLocation refName does NOT require updating.");
            }

        }

        // For each CollectionObject record that has been identified for updating,
        // update its computedCurrentLocation field with its computed current
        // location value returned from the SQL function.
        for (Map.Entry<DocumentModel, String> entry : docModelsToUpdate.entrySet()) {
            DocumentModel dmodel = entry.getKey();
            String newCurrentLocationValue = entry.getValue();
            dmodel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY, newCurrentLocationValue);
            coreSession.saveDocument(dmodel);
            if (logger.isTraceEnabled()) {
                String afterUpdateComputedCurrentLocationRefName =
                        (String) dmodel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
                logger.trace("Following update, new computedCurrentLocation refName value=" + afterUpdateComputedCurrentLocationRefName);

            }
        }
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

    // FIXME: The following method is specific to PostgreSQL, because of
    // the SQL command executed; it may need to be generalized.
    // Note: It may be necessary in some cases to provide additional
    // parameters beyond a function name (such as a function signature)
    // to uniquely identify a function. So far, however, this need
    // hasn't arisen in our specific use case here.
    /**
     * Identifies whether a stored function exists in a database.
     *
     * @param functionname the name of the function.
     * @return true if the function exists in the database; false if it does
     * not.
     */
    private boolean storedFunctionExists(String functionname) {
        if (Tools.isBlank(functionname)) {
            return false;
        }
        boolean storedFunctionExists = false;
        String sql = "SELECT proname FROM pg_proc WHERE proname='" + functionname + "'";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean storedAutoCommitState = true;
        try {
            conn = JDBCTools.getConnection(JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME));
            stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE, ResultSet.CLOSE_CURSORS_AT_COMMIT);
            storedAutoCommitState = conn.getAutoCommit();
            conn.setAutoCommit(true);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                storedFunctionExists = true;
            }
            rs.close();
            stmt.close();
            conn.setAutoCommit(storedAutoCommitState);
            conn.close();
        } catch (Exception e) {
            logger.debug("Error when identifying whether stored function " + functionname + "exists :", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(storedAutoCommitState);
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in "
                        + "UpdateObjectLocationOnMove.storedFunctionExists: "
                        + sqle.getLocalizedMessage());
            }
        }
        return storedFunctionExists;
    }

    /**
     * Returns the computed current location of a CollectionObject (aka
     * Cataloging) record.
     *
     * @param csid the CSID of a CollectionObject record.
     * @return the computed current location of the CollectionObject record.
     */
    private String computeCurrentLocation(String csid) {
        String computedCurrentLocation = "";
        if (Tools.isBlank(csid)) {
            return computedCurrentLocation;
        }
        String sql = String.format("SELECT %1$s('%2$s')", STORED_FUNCTION_NAME, csid);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = JDBCTools.getConnection(JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME));
            stmt = conn.createStatement(ResultSet.CONCUR_UPDATABLE, ResultSet.CLOSE_CURSORS_AT_COMMIT);
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                computedCurrentLocation = rs.getString(COMPUTED_CURRENT_LOCATION_COLUMN);
                logger.debug("computedCurrentLocation first=" + computedCurrentLocation);
            }
            // Experiment with performing an update before the query
            // as a possible means of refreshing data.
            String updateSql = getStringFromResource(SQL_RESOURCE_PATH);
            int result = -1;
            try {
                result = JDBCTools.executeUpdate(JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME), updateSql);
            } catch (Exception e) {
            }
            logger.trace("Result of executeUpdate=" + result);
            // String randomSql = String.format("SELECT now()");
            // rs = stmt.executeQuery(randomSql);
            // rs.close();
            stmt.close();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                computedCurrentLocation = rs.getString(COMPUTED_CURRENT_LOCATION_COLUMN);
                logger.debug("computedCurrentLocation second=" + computedCurrentLocation);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            logger.debug("Error when attempting to obtain the computed current location of an object :", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in "
                        + "UpdateObjectLocationOnMove.computeCurrentLocation: "
                        + sqle.getLocalizedMessage());
            }
        }
        return computedCurrentLocation;
    }

    // FIXME: A quick first pass, using an only partly query-based technique for
    // getting the current location, augmented by procedural code.
    //
    // Should be replaced by a more performant method, based entirely, or nearly so,
    // on a query.
    //
    // E.g. a sample CMIS query for retrieving Movement records related to a CollectionObject;
    // we can see if the ORDER BY clause can refer to a Movement locationDate field.
    /*
     "SELECT DOC.nuxeo:pathSegment, DOC.dc:title, REL.dc:title,"
     + "REL.relations_common:objectCsid, REL.relations_common:subjectCsid FROM Movement DOC "
     + "JOIN Relation REL ON REL.relations_common:objectCsid = DOC.nuxeo:pathSegment "
     + "WHERE REL.relations_common:subjectCsid = '5b4c617e-53a0-484b-804e' "
     + "AND DOC.nuxeo:isVersion = false "
     + "ORDER BY DOC.collectionspace_core:updatedAt DESC";
     */
    private String computeCurrentLocation(CoreSession session, String collectionObjectCsid,
            String movementCsid) throws ClientException {
        String computedCurrentLocation = "";
        // Get Relation records for Movments related to this CollectionObject
        String query = String.format(
                "SELECT * FROM %1$s WHERE " // collectionspace_core:tenantId = 1 "
                + "(relations_common:subjectCsid = '%2$s' "
                + "AND relations_common:objectDocumentType = '%3$s') "
                + "AND (ecm:currentLifeCycleState <> 'deleted') "
                + "AND ecm:isProxy = 0 "
                + "AND ecm:isCheckedInVersion = 0 ",
                RELATION_DOCTYPE, collectionObjectCsid, MOVEMENT_DOCTYPE, movementCsid, COLLECTIONOBJECT_DOCTYPE);
        logger.debug("query=" + query);
        DocumentModelList relatedDocModels = session.query(query);
        if (relatedDocModels == null || relatedDocModels.isEmpty()) {
            logger.trace("Found " + relatedDocModels.size() + " related documents.");
            return "";
        } else {
            logger.trace("Found " + relatedDocModels.size() + " related documents.");
        }
        // Get the CollectionObject's current location from the related Movement
        // record with the most recent location date.
        GregorianCalendar mostRecentLocationDate = EARLIEST_COMPARISON_DATE;
        DocumentModel movementDocModel = null;
        String csid = "";
        String location = "";
        for (DocumentModel relatedDocModel : relatedDocModels) {
            // The object CSID in the relation is the related Movement record's CSID
            csid = (String) relatedDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
            movementDocModel = getDocModelFromCsid(session, csid);
            GregorianCalendar locationDate = (GregorianCalendar) movementDocModel.getProperty(MOVEMENTS_COMMON_SCHEMA, LOCATION_DATE_PROPERTY);
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

    /**
     * Returns a string representation of the contents of an input stream.
     *
     * @param instream an input stream.
     * @return a string representation of the contents of the input stream.
     * @throws an IOException if an error occurs when reading the input stream.
     */
    private String stringFromInputStream(InputStream instream) throws IOException {
        if (instream == null) {
        }
        BufferedReader bufreader = new BufferedReader(new InputStreamReader(instream));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while (line != null) {
            sb.append(line);
            line = bufreader.readLine();
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of a resource available to the current
     * class.
     *
     * @param resourcePath a path to the resource.
     * @return a string representation of the resource. Returns null if the
     * resource cannot be read, or if it cannot be successfully represented as a
     * string.
     */
    private String getStringFromResource(String resourcePath) {
        String str = "";
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream instream = classLoader.getResourceAsStream(resourcePath);
        if (instream == null) {
            logger.warn("Could not read from resource from path " + resourcePath);
            return null;
        }
        try {
            str = stringFromInputStream(instream);
        } catch (IOException ioe) {
            logger.warn("Could not create string from stream: ", ioe);
            return null;
        }
        return str;
    }

    private DocumentModel getDocModelFromCsid(CoreSession coreSession, String collectionObjectCsid) {
        DocumentModelList collectionObjectDocModels = null;
        try {
            final String query = "SELECT * FROM "
                    + NuxeoUtils.BASE_DOCUMENT_TYPE
                    + " WHERE "
                    + NuxeoUtils.getByNameWhereClause(collectionObjectCsid);
            collectionObjectDocModels = coreSession.query(query);
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
}