/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// *IMPORTANT*
// @TODO Revise exception handling to return custom Exceptions,
// perhaps mirroring the subset of HTTP status codes returned.
//
// We're currently overloading existing core and extension Java Exceptions
// in ways that are not consistent with their original semantic meaning.
// @TODO Remove any hard-coded dependencies on MySQL.
// @TODO Determine how to restrict access to ID-related tables by role.
// @TODO Retrieve IDGenerators from the database (via JDBC or
// Hibernate) at initialization and refresh time.
// @TODO Handle both CollectionSpace IDs and URIs as identifiers
// for ID generators, replacing simple integer IDs.
//
// We're currently using simple integer IDs to identify ID generators
// in this initial iteration.
//
// To uniquely identify ID generators in production, we'll need to handle
// both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
// other form of identifier to be determined, such as URLs or URNs.
// @TODO Handle concurrency.
//
// Right now, with each new request we're simply instantiating
// a new SettableIDGenerator and returning its next ID.  As a result,
// the generated IDs may well duplicate other, previously-generated IDs.
//
// When we start storing ID generators and IDs in a database,
// the state associated with each generator will be stored
// and modified in a single location.
//
// At that point, we'll also need to add code to handle concurrent
// requests to modify that state.
// @TODO Verify access (public, protected, or private) to service methods.
// @TODO Re-consider beginnings of method names:
// - "store/get" versus:
// - "store/retrieve"
// - "save/read" (appears to be used by Hibernate),
// - "persist/find" (used by JPA)
// - or?
// For now have used CRUD-like names, consistent with other classes.
package org.collectionspace.services.id;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.storage.JDBCTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDServiceJdbcImpl
 *
 * Manages the storage of ID generators and persistence of their
 * current state, via a JDBC interface to an underlying database.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class IDServiceJdbcImpl implements IDService {

    private static final String SELECT_LAST_ID = "SELECT last_generated_id FROM id_generators WHERE csid = ?";
    private static final String SELECT_LAST_ID_FOR_UPDATE =
        "SELECT last_generated_id FROM id_generators WHERE csid = ? FOR UPDATE";

    final Logger logger = LoggerFactory.getLogger(IDServiceJdbcImpl.class);
    final String TABLE_NAME = "id_generator";
    boolean jdbcDriverInstantiated = false;
    boolean hasPreconditions = true;
    
    final static String CSPACE_INSTANCE_ID = ServiceMain.getInstance().getCspaceInstanceId();


    //////////////////////////////////////////////////////////////////////
    /**
     * Constructor (no-argument).
     */
    public IDServiceJdbcImpl() {
    	// Empty
    }

    // -----------------
    // Operations on IDs
    // -----------------
    //////////////////////////////////////////////////////////////////////
    /**
     * Generates and returns a new ID associated with a specified ID generator.
     *
     * This method has an intentional side-effect: it sets the
     * current ID of that ID generator to the just-generated ID.
     *
     * @param  csid  An identifier for an ID generator.
     *
     * @return  A new ID associated with the specified ID generator.
     *
     * @throws  IllegalArgumentException if the provided csid is null or empty,
     *          or if the specified ID generator can't be found.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public String createID(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid) throws Exception {

        logger.debug("> in createID");

        // @TODO Where relevant, implement logic to check for ID availability,
        // after generating a candidate ID.

        // @TODO: Add checks for authorization to perform this operation.

        String newId = "";
        String lastId = "";

        if (csid == null || csid.equals("")) {
            throw new DocumentNotFoundException(
                    "Identifier for ID generator must not be null or empty.");
        }

        String serializedGenerator = "";
        try {
            IDGeneratorInstance generator = readIDGenerator(ctx, csid);
            serializedGenerator = generator.getGeneratorState();
            // serializedGenerator = readIDGenerator(csid).getGeneratorState();
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }

        // Guard code - should not be needed.
        if (serializedGenerator == null || serializedGenerator.equals("")) {
            throw new BadRequestException(
                    "ID generator " + "\'" + csid + "\'" + " could not be found.");
        }

        SettableIDGenerator generator;
        try {
            generator = IDGeneratorSerializer.deserialize(serializedGenerator);
        } catch (IllegalArgumentException e) {
            throw e;
        }

        try {

            // Retrieve the last ID associated with this generator
            // from persistent storage.
            lastId = readLastID(ctx, csid);

            // If there was no last generated ID associated with this generator,
            // get a new ID.
            if (lastId == null || lastId.equals("")) {
                newId = generator.newID();

                // Otherwise, generate a new ID, potentially based on the last ID.
                // (This also sets the current ID of the ID generator's state
                // to this just-generated 'new' ID.)
            } else {
                newId = generator.newID(lastId);
            }

            // Store the 'new' ID as the last-generated ID for this generator.
            updateLastID(ctx, csid, newId);

            // Store the new state of this ID generator, reflecting that
            // one of its parts may possibly have had its value updated as
            // a result of the generation of this 'new' ID.
            updateIDGenerator(ctx, csid, generator);

        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }

        return newId;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Stores the last-generated ID, corresponding to a specified ID generator,
     * into persistent storage.
     *
     * @param  csid     An identifier for an ID generator.
     *
     * @param  lastId  The value of the last-generated ID associated with that ID generator.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     *
     * @throws  DocumentNotFoundException if the requested ID generator could not be found.
     */
    public void updateLastID(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid, String lastId)
            throws IllegalStateException, DocumentNotFoundException, NamingException, SQLException, Exception {

        logger.debug("> in updateLastID");

        // @TODO Where relevant, implement logic to check for ID availability,
        // after generating a candidate ID.

        // @TODO: Add checks for authorization to perform this operation.

        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName))) {
            conn.setAutoCommit(false);
            boolean idGeneratorFound;

            // Test whether this ID generator already exists in the database.
            // Using a 'SELECT ... FOR UPDATE' statement will temporarily
            // lock this row for updates from any other connection, until
            // the UPDATE is committed, below.
            try (PreparedStatement select = conn.prepareStatement(SELECT_LAST_ID_FOR_UPDATE)) {
                select.setString(1, csid);
                try (ResultSet rs = select.executeQuery()) {
                    idGeneratorFound = rs.next();
                }
            }

            // If this ID generator was not found in the
            // database, an update can't be performed.
            // Close the connection and throw an exception.
            if (!idGeneratorFound) {
                throw new DocumentNotFoundException(
                        "Error updating ID generator '" + csid
                        + "': generator could not be found in the database.");
            }

            // Otherwise, if this ID generator exists in the database,
            // update its Last ID value.
            final String SQL_STATEMENT_STRING =
                "UPDATE id_generators SET "
                + "last_generated_id = ? "
                + "WHERE csid = ?";

            int rowsUpdated;
            try (PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING)) {
                ps.setString(1, lastId);
                ps.setString(2, csid);
                rowsUpdated = ps.executeUpdate();
            }

            if (rowsUpdated != 1) {
                throw new IllegalStateException(
                        "Error updating last-generated ID in the database "
                        + "for ID generator '" + csid + "'");
            }

            conn.commit();

            logger.debug("Successfully updated last-generated ID: " + lastId);
        } catch (SQLException e) {
            throw new IllegalStateException("Error updating last-generated "
                    + "ID in the database: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns the last-generated ID, corresponding to a specified ID generator,
     * from persistent storage.
     *
     * @param  csid  An identifier for an ID generator.
     *
     * @return  The last ID generated that corresponds to the requested ID generator.
     *
     * @throws  DocumentNotFoundException if the requested ID generator
     *          could not be found.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public String readLastID(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid) throws Exception {

        logger.debug("> in readLastID");

        // @TODO Where relevant, implement logic to check for ID availability,
        // after generating a candidate ID.

        // @TODO: Add checks for authorization to perform this operation.

        String lastId = null;
        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName));
             PreparedStatement stmt = conn.prepareStatement(SELECT_LAST_ID)) {
            stmt.setString(1, csid);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean moreRows = rs.next();
                if (!moreRows) {
                    throw new DocumentNotFoundException("ID generator '" + csid + "' could not be found.");
                }
                lastId = (rs.getString(1) != null ? rs.getString(1) : "");
                logger.debug("> retrieved ID: " + lastId);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving last ID "
                    + "from the database: " + e.getMessage());
        }

        logger.debug("> returning ID: " + lastId);

        return lastId;

    }

    // ---------------------------
    // Operations on ID Generators
    // ---------------------------
    //////////////////////////////////////////////////////////////////////
    /**
     * Adds a new ID generator instance to persistent storage.
     *
     * @param  csid     An identifier for an ID generator.
     *
     * @param  generator  An ID generator, reflecting its current state,
     *                  including the values of its constituent parts.
     *
     * @throws  BadRequestException if the provided representation of an
     *          ID generator instance is not in the correct format, contains
     *          invalid values, or otherwise cannot be used.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    private void createIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid, SettableIDGenerator generator)
            throws Exception {

        logger.debug("> in createIDGenerator(String, SettableIDGenerator)");

        // @TODO: Add checks for authorization to perform this operation.

        if (generator == null) {
            throw new BadRequestException("New ID generator "
                    + "to add cannot be null.");
        }

        String serializedGenerator = "";
        try {
            serializedGenerator = IDGeneratorSerializer.serialize(generator);
        } catch (BadRequestException e) {
            throw e;
        }

        try {
            createIDGenerator(ctx, csid, serializedGenerator);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Adds a new ID generator to persistent storage, from a serialization
     * of that generator.
     *
     * The serialization method recognized by this method has implementation
     * dependencies.  Currently, this method expects serialization via XStream's
     * out-of-the-box serializer, without custom configuration.
     *
     * @param  csid     An identifier for an ID generator.
     *
     * @param  serializedIDGenerator  A serialized ID generator, reflecting its current state,
     *                                including the values of its constituent parts.
     *
     * @throws  BadRequestException if the provided representation of an
     *          ID generator instance is not in the correct format, contains
     *          invalid values, or otherwise cannot be used.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public void createIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, String serializedIDGenerator) throws Exception {

        logger.debug("> in createIDGenerator(String, String)");

        // @TODO Add checks for authorization to perform this operation.

        if (Tools.isBlank(serializedIDGenerator)) {
            String msg = "ID generator payload is null or empty.";
            logger.warn(msg);
            throw new BadRequestException(msg);
        }

        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName));
             PreparedStatement stmt = conn.prepareStatement(SELECT_LAST_ID)) {
            stmt.setString(1, csid);

            // @TODO This check should extend further, to other aspects

            // Test whether this ID generator already exists in the database.
            // of the generator, such as its URI, if any, and its structure,
            // so we avoid duplication based on content as well as identifier.
            boolean idGeneratorFound;
            try (ResultSet rs = stmt.executeQuery()) {
                idGeneratorFound = rs.next();
            }

            // If this ID generator already exists in the database,
            // throw an Exception.
            //
            // @TODO This exception needs to convey the meaning that a
            // conflict has occurred, so that this status can be reported
            // to the client.
            if (idGeneratorFound) {
                String msg = String.format("Conflict with existing generator when attempting to add "
                        + "new ID generator with ID '%s' to the database.", csid);
                logger.warn(msg);
                throw new IllegalStateException(msg);

                // Otherwise, add this new ID generator, as a new record to
                // the database.
            } else {
                int rowsUpdated;

                final String SQL_STATEMENT_STRING =
                    "INSERT INTO id_generators (csid, id_generator_state, last_generated_id) VALUES (?, ?, ?)";

                try (PreparedStatement insert = conn.prepareStatement(SQL_STATEMENT_STRING)) {
                    insert.setString(1, csid);
                    insert.setString(2, serializedIDGenerator);
                    insert.setNull(3, java.sql.Types.VARCHAR);
                    rowsUpdated = insert.executeUpdate();
                }

                if (rowsUpdated != 1) {
                    String msg = String.format("Error adding new ID generator '%s' to the database.", csid);
                    logger.warn(msg);
                    throw new IllegalStateException(msg);
                }

            } // end if (idGeneratorFound)

            logger.debug("> successfully added ID generator: " + csid);

        } catch (SQLException e) {
            String msg = "Error adding new ID generator to the database: " + e.getMessage();
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a requested ID generator from persistent storage.
     *
     * @param  csid  An identifier for an ID generator.
     *
     * @return  A serialized representation of the requested ID generator.
     *
     * @throws  DocumentNotFoundException if the requested ID generator could
     *          not be found.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public IDGeneratorInstance readIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid) throws Exception {

        logger.debug("> in readIDGenerator");

        IDGeneratorInstance instance = null;

        final String query = "SELECT csid, displayname, description, id_generator_state, last_generated_id " +
                             "FROM id_generators WHERE csid= ?";

        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName));
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, csid);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean moreRows = rs.next();
                if (!moreRows) {
                    throw new DocumentNotFoundException("ID generator with ID '" + csid + "' could not be found.");
                }

                instance = new IDGeneratorInstance();
                instance.setDisplayName(rs.getString(2) != null ? rs.getString(2) : "");
                instance.setDescription(rs.getString(3) != null ? rs.getString(3) : "");
                instance.setGeneratorState(rs.getString(4) != null ? rs.getString(4) : "");
                instance.setLastGeneratedID(rs.getString(5) != null ? rs.getString(5) : "");
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                "Error retrieving ID generator '" + csid + "' from database: " + e.getMessage());
        }
        // Do nothing here

        logger.debug("> retrieved SettableIDGenerator: "
                + instance.getGeneratorState());

        return instance;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a list of ID generator instances from persistent storage.
     *
     * @return  A list of ID generator instances, with each list item
     *          constituting a serialized representation of an
     *          ID generator instance.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public Map<String, IDGeneratorInstance> readIDGeneratorsList(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception {

        logger.debug("> in readIDGeneratorsList");

        Map<String, IDGeneratorInstance> generators =
                new LinkedHashMap<String, IDGeneratorInstance>();

        final String query = "SELECT csid, displayname, description, "
                             + "id_generator_state, last_generated_id FROM id_generators "
                             + "ORDER BY displayname ASC";
        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            IDGeneratorInstance instance;
            while (rs.next()) {
                instance = new IDGeneratorInstance();
                instance.setDisplayName(rs.getString(2) != null ? rs.getString(2) : "[No display name]");
                instance.setDescription(rs.getString(3) != null ? rs.getString(3) : "[No description]");
                instance.setGeneratorState(rs.getString(4) != null ? rs.getString(4) : "[No generator state]");
                instance.setLastGeneratedID(rs.getString(5) != null ? rs.getString(5) : "[No last generated ID]");
                generators.put(rs.getString(1), instance);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                "Error retrieving ID generators  from database: " + e.getMessage());
        }

        return generators;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Updates an existing ID generator in persistent storage.
     *
     * @param  csid     An identifier for an ID generator.
     *
     * @param  generator  An ID generator, reflecting its current state,
     *                  including the values of its constituent parts.
     *
     * @throws  DocumentNotFoundException if the requested ID generator could
     *          not be found.
     *
     * @throws  BadRequestException if the provided representation of an
     *          ID generator instance is not in the correct format, contains
     *          invalid values, or otherwise cannot be used.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    private void updateIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, 
    		SettableIDGenerator generator) throws Exception {

        logger.debug("> in updateIDGenerator(String, SettableIDGenerator)");

        // @TODO: Add checks for authorization to perform this operation.

        if (generator == null) {
            throw new BadRequestException(
                    "ID generator provided in update operation cannot be null.");
        }

        String serializedGenerator = "";
        try {
            serializedGenerator = IDGeneratorSerializer.serialize(generator);
        } catch (BadRequestException e) {
            throw e;
        }

        try {
            updateIDGenerator(ctx, csid, serializedGenerator);
        } catch (DocumentNotFoundException e) {
            throw e;
        } catch (BadRequestException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        }

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Updates an existing ID generator in persistent storage,
     * from a serialization of the current state of that generator.
     *
     * The serialization method recognized by this method has implementation
     * dependencies.  Currently, this method expects serialization via XStream's
     * out-of-the-box serializer, without custom configuration.
     *
     * @param  csid  An identifier for an ID generator.
     *
     * @param  serializedIDGenerator
     *           A serialized ID generator, reflecting its current state,
     *           including the values of its constituent parts.
     *
     * @throws  DocumentNotFoundException if the requested ID generator could
     *          not be found.
     *
     * @throws  BadRequestException if the provided representation of an
     *          ID generator instance is not in the correct format, contains
     *          invalid values, or otherwise cannot be used.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public void updateIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		String csid, 
    		String serializedIDGenerator) throws Exception {

        logger.debug("> in updateIDGenerator(String, String)");

        // @TODO: Add checks for authorization to perform this operation.

        if (serializedIDGenerator == null || serializedIDGenerator.equals("")) {
            throw new BadRequestException(
                    "Could not understand or parse this representation of an ID generator.");
        }

        SettableIDGenerator generator;
        try {
            generator = IDGeneratorSerializer.deserialize(serializedIDGenerator);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        String lastId = generator.getCurrentID();

        // Test whether this ID generator already exists in the database.
        // Using a 'SELECT ... FOR UPDATE' statement will temporarily
        // lock this row for updates from any other connection, until
        // the UPDATE is committed, below.
        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName));
             PreparedStatement select = conn.prepareStatement(SELECT_LAST_ID_FOR_UPDATE)) {
            conn.setAutoCommit(false);
            select.setString(1, csid);

            boolean idGeneratorFound;
            try (ResultSet rs = select.executeQuery()) {
                idGeneratorFound = rs.next();
            }

            // If this ID generator was not found in the
            // database, an update can't be performed.
            // Close the connection and throw an exception.
            if (!idGeneratorFound) {
                throw new DocumentNotFoundException(
                    "Error updating ID generator '" + csid
                    + "': generator could not be found in the database.");
            } // end if (idGeneratorFound)

            // Otherwise, if this ID generator exists in the database,
            // update its record.
            int rowsUpdated;
            final String SQL_STATEMENT_STRING = "UPDATE id_generators SET "
                                      + "id_generator_state = ?, "
                                      + "last_generated_id = ? "
                                      + "WHERE csid = ?";
            try (PreparedStatement update = conn.prepareStatement(SQL_STATEMENT_STRING)) {
                update.setString(1, serializedIDGenerator);
                update.setString(2, lastId);
                update.setString(3, csid);
                rowsUpdated = update.executeUpdate();
            }

            if (rowsUpdated != 1) {
                throw new IllegalStateException("Error updating ID generator '" + csid + "' in the database.");
            }

            conn.commit();

            logger.debug("Successfully updated ID Generator: " + csid);
        } catch (SQLException e) {
            throw new IllegalStateException("Error updating ID generator in the database: " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Deletes an existing ID generator from persistent storage.
     *
     * @param  csid     An identifier for an ID generator.
     *
     * @throws  DocumentNotFoundException if the requested ID generator could
     *          not be found.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public void deleteIDGenerator(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String csid)
            throws Exception {

        logger.debug("> in deleteIDGenerator");

        // @TODO: Add checks for authorization to perform this operation.

        String repositoryName = ctx.getRepositoryName();
        try (Connection conn = getJdbcConnection(getDatabaseName(repositoryName));
             PreparedStatement select = conn.prepareStatement(SELECT_LAST_ID)) {
            select.setString(1, csid);
            // Test whether this ID generator already exists in the database.
            boolean idGeneratorFound;
            try (ResultSet rs = select.executeQuery()) {
                idGeneratorFound = rs.next();
            }

            // If this ID generator already exists in the database,
            // update its record.
            if (idGeneratorFound) {
                int rowsUpdated;
                final String SQL_DELETE = "DELETE FROM id_generators WHERE csid = ?";
                try (PreparedStatement delete = conn.prepareStatement(SQL_DELETE)) {
                    delete.setString(1, csid);
                    rowsUpdated = delete.executeUpdate();
                }
                if (rowsUpdated != 1) {
                    throw new IllegalStateException("Error deleting ID generator '" + csid + "' in the database.");
                }

                // Otherwise, throw an exception, which indicates that the requested
                // ID generator was not found.
            } else {
                throw new DocumentNotFoundException(
                    "Error deleting ID generator '" + csid
                    + "': generator could not be found in the database.");
            } // end if (idGeneratorFound)

            logger.debug("Successfully deleted ID generator: " + csid);

        } catch (SQLException e) {
            throw new IllegalStateException(
                "Error deleting ID generator in database: " + e.getMessage());
        }
    }

    // -------------------
    // Database operations
    // -------------------

    //////////////////////////////////////////////////////////////////////
    /**
     * Opens a connection to the database and returns a JDBC Connection object.
     *
     * @param   databaseName a JDBC database name. 
     * @return  a JDBC database Connection object.
     *
     * @throws  LoginException
     * @throws  SQLException if a storage-related error occurred.
     */
    public Connection getJdbcConnection(String databaseName) throws NamingException, SQLException {

        logger.debug("> in getJdbcConnection");
        
        Connection conn = null;
        try {
            conn = JDBCTools.getConnection(JDBCTools.NUXEO_DATASOURCE_NAME, databaseName);
        } catch (NamingException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        }
        return conn;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Identifies whether a specified table exists in the database.
     *
     * @param   tablename  The name of a database table.
     *
     * @return  True if the specified table exists in the database;
     *          false if the specified table does not exist in the database.
     *
     * @throws  IllegalStateException if an error occurs while checking for the
     *          existence of the specified table.
     */
    private boolean hasTable(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx, String tablename) throws Exception {

        logger.debug("> in hasTable");

        if (tablename == null || tablename.equals("")) {
            return false;
        }

        Connection conn = null;
        try {
            String repositoryName = ctx.getRepositoryName();
            conn = getJdbcConnection(getDatabaseName(repositoryName));

            // Retrieve a list of tables in the current database.
            final String CATALOG_NAME = null;
            final String SCHEMA_NAME_PATTERN = "cspace";
            final String[] TABLE_TYPES = null;
            ResultSet tablesMatchingTableName =
                    conn.getMetaData().getTables(
                    CATALOG_NAME, SCHEMA_NAME_PATTERN, tablename, TABLE_TYPES);

            // Check whether a table with the specified name is in that list.
            boolean moreRows = tablesMatchingTableName.next();
            if (!moreRows) {
                return false;
            } else {
                return true;
            }

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error while checking for existence of database table: "
                    + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Do nothing here
            }
        }

    }

    private String getDatabaseName(String repositoryName) {
        return JDBCTools.getDatabaseName(repositoryName, CSPACE_INSTANCE_ID);
    }
}
