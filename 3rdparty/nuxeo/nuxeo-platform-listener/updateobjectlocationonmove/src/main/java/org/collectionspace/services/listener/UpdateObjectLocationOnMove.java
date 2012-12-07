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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
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
    final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);
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
    private final static String OBJECT_CSID_PROPERTY = "objectCsid"; // FIXME: Get from external constant
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common"; // FIXME: Get from external constant
    final String COLLECTIONOBJECT_DOCTYPE = "CollectionObject"; // FIXME: Get from external constant
    final String COMPUTED_CURRENT_LOCATION_PROPERTY = "computedCurrentLocation"; // FIXME: Create and then get from external constant

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
        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();

        // If this event does not involve an active Movement Document,
        // return without further handling the event.
        if (!(isMovementDocument(docModel) && isActiveDocument(docModel))) {
            return;
        }

        logger.debug("A create or update event for an active Movement document was received by UpdateObjectLocationOnMove ...");

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
            logger.debug("Stored function " + STORED_FUNCTION_NAME + " does NOT exist.");
            String sql = getStringFromResource(SQL_RESOURCE_PATH);
            if (Tools.isBlank(sql)) {
                logger.warn("Could not obtain SQL command to create stored function.");
                logger.warn("Actions in this event listener will NOT be performed, as a result of a previous error.");
                return;
            }

            int result = -1;
            try {
                result = JDBCTools.executeUpdate(JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME), sql);
            } catch (Exception e) {
                // Do nothing here
                // FIXME: Need to verify that the original '-1' value is preserved if an Exception is caught here.
            }
            logger.debug("Result of executeUpdate=" + result);
            if (result < 0) {
                logger.warn("Could not create stored function.");
                logger.warn("Actions in this event listener will NOT be performed, as a result of a previous error.");
                return;
            } else {
                logger.debug("Stored function " + STORED_FUNCTION_NAME + " was successfully created.");
            }
        } else {
            logger.debug("Stored function " + STORED_FUNCTION_NAME + " exists.");
        }

        String movementCsid = NuxeoUtils.getCsid(docModel);
        logger.debug("Movement record CSID=" + movementCsid);

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
        CoreSession coreSession = docEventContext.getCoreSession();
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
            logger.debug("Found " + relatedDocModels.size() + " related documents.");
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
            return;
        } else {
            logger.debug("Found " + collectionObjectCsids.size() + " CollectionObject CSIDs.");
        }

        // Iterate through the list of CollectionObject CSIDs found.
        DocumentModel collectionObjectDocModel = null;
        String computedCurrentLocationRefName = "";
        for (String collectionObjectCsid : collectionObjectCsids) {

            // Verify that the CollectionObject record is active.
            collectionObjectDocModel = getDocModelFromCsid(coreSession, collectionObjectCsid);
            if (!isActiveDocument(collectionObjectDocModel)) {
                continue;
            }

            // Via a JDBC call, invoke the SQL function to obtain the computed
            // current location of that CollectionObject.
            computedCurrentLocationRefName = computeCurrentLocation(collectionObjectCsid);
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
                // computedCurrentLocation value, or if the two values differ,
                // update the CollectionObject record's computedCurrentLocation
                // field with the value returned from the SQL function.
                String existingComputedCurrentLocationRefName =
                        (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
                if (Tools.isBlank(existingComputedCurrentLocationRefName)
                        || !computedCurrentLocationRefName.equals(existingComputedCurrentLocationRefName)) {
                    logger.debug("Existing computedCurrentLocation refName=" + existingComputedCurrentLocationRefName);
                    logger.debug("computedCurrentLocation refName requires updating.");
                    // FIXME: Add update code here
                } else {
                    logger.debug("computedCurrentLocation refName does NOT require updating.");
                }

            }

        }

    }

    /**
     * Identifies whether a document is a Movement document
     *
     * @param docModel a document model
     * @return true if the document is a Movement document; false if it is not.
     */
    private boolean isMovementDocument(DocumentModel docModel) {
        return documentMatchesType(docModel, MovementConstants.NUXEO_DOCTYPE);
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
        try {
            conn = JDBCTools.getConnection(JDBCTools.getDataSource(JDBCTools.NUXEO_REPOSITORY_NAME));
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                storedFunctionExists = true;
            }
            rs.close();
            stmt.close();
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
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                computedCurrentLocation = rs.getString(COMPUTED_CURRENT_LOCATION_COLUMN);
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