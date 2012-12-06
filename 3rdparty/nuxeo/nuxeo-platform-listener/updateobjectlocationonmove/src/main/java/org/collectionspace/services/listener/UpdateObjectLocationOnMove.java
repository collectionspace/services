package org.collectionspace.services.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.movement.nuxeo.MovementConstants;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateObjectLocationOnMove implements EventListener {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);
    private final String DATABASE_RESOURCE_DIRECTORY_NAME = "db";
    // FIXME: Currently hard-coded; get this from JDBC utilities or equivalent
    private final String DATABASE_SYSTEM_NAME = "postgresql";
    private final String STORED_FUNCTION_NAME = "computecurrentlocation";

    // ####################################################################
    // FIXME: Per Rick, what happens if a relation record is updated,
    // that either adds or removes a relation between a Movement
    // record and a CollectionObject record?  Do we need to listen
    // for that event as well and update the CollectionObject record's
    // computedCurrentLocation accordingly?
    //
    // The following code is currently checking only for creates or
    // updates to Movement records.
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
        if (isMovementDocument(docModel) && isActiveDocument(docModel)) {
            logger.debug("A create or update event for an active Movement document was received by UpdateObjectLocationOnMove ...");

            // Pseudocode:

            // Test whether a SQL function exists to supply the computed
            // current location of a CollectionObject.
            if (storedFunctionExists(STORED_FUNCTION_NAME)) {
                logger.debug("Stored function " + STORED_FUNCTION_NAME + "exists.");
            } else {
                logger.debug("Stored function " + STORED_FUNCTION_NAME + "does NOT exist.");
            }
            
            // FIXME: For incremental debugging, as we work through implementing
            // this pseudocode.
            return;

            // If it does not, load the function from the resources available
            // to this class, and run a JDBC command to create that function.

            // At the moment, that function is named computeCurrentLocation(),
            // and resides in the resources of the current module.
            //
            // For now, assume this function will exist in the 'nuxeo' database;
            // future work to create per-tenant repositories will likely require that
            // our JDBC statements connect to the appropriate tenant-specific database.

            // It doesn't appear we can create this function via 'ant create_nuxeo db'
            // during the build process, because there's a substantial likelihood at
            // that point that tables referred to by the function (movements_common
            // and collectionobjects_common) will not exist, and PostgreSQL will not
            // permit the function to be created if that is the case.

            /*
            ClassLoader classLoader = getClass().getClassLoader();
            String functionResourcePath =
                    DATABASE_RESOURCE_DIRECTORY_NAME + "/"
                    + DATABASE_SYSTEM_NAME + "/"
                    + STORED_FUNCTION_NAME + ".sql";
            classLoader.getResourceAsStream(functionResourcePath);
            */

            // If the create attempt fails, bail (return) from this method.

            // Get this Movement record's CSID via the document model.

            // Find every CollectionObject record related to this Movement record:
            //
            // Via an NXQL query, get a list of (non-deleted) relation records where:
            // * This movement record's CSID is the subject CSID of the relation.
            // * The object document type is a CollectionObject doctype.

            // Iterate through that list of Relation records and build a list of
            // CollectionObject CSIDs, by extracting the object CSIDs of those records.

            // For each such CollectionObject:

            // Verify that the CollectionObject record is active (use isActiveDocument(), below).

            // Via a JDBC call, invoke the SQL function to supply the last
            // identified location of that CollectionObject, giving it the CSID
            // of the CollectionObject record as an argument.

            // Check that the SQL function's returned value, which is expected
            // to be a reference (refName) to a storage location authority term,
            // is at a minimum:
            // * Non-null
            // * Capable of being successfully parsed by an authority item parser,
            //   returning a non-null parse result.

            // Compare that returned value to the value in the
            // lastIdentifiedLocation field of that CollectionObject

            // If the two values differ, update the CollectionObject record,
            // setting the value of the lastIdentifiedLocation field of that
            // CollectionObject record to the value returned from the SQL function.
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

    // FIXME: Generic methods like the following might be split off
    // into an event utilities class. - ADR 2012-12-05
    // FIXME: Identify whether the equivalent of this utility method is
    // already implemented and substitute a call to the latter if so.
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
                logger.debug("SQL Exception closing statement/connection in UpdateObjectLocationOnMove.storedFunctionExists: " + sqle.getLocalizedMessage());
            }
        }
        return storedFunctionExists;
    }
}