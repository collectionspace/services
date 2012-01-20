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
// @TODO As long as we're using JDBC, use PreparedStatements, not Statements,
// throughout the code below.
// @TODO Re-consider beginnings of method names:
// - "store/get" versus:
// - "store/retrieve"
// - "save/read" (appears to be used by Hibernate),
// - "persist/find" (used by JPA)
// - or?
// For now have used CRUD-like names, consistent with other classes.
package org.collectionspace.services.id;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
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

    final Logger logger = LoggerFactory.getLogger(IDServiceJdbcImpl.class);
    final String TABLE_NAME = "id_generator";
    boolean jdbcDriverInstantiated = false;
    boolean hasPreconditions = true;

    //////////////////////////////////////////////////////////////////////
    /**
     * Constructor (no-argument).
     */
    public void IDServiceJdbcImpl() {
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
    public String createID(String csid) throws Exception {

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
            IDGeneratorInstance generator = readIDGenerator(csid);
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
            lastId = readLastID(csid);

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
            updateLastID(csid, newId);

            // Store the new state of this ID generator, reflecting that
            // one of its parts may possibly have had its value updated as
            // a result of the generation of this 'new' ID.
            updateIDGenerator(csid, generator);

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
     * @param  generator  An ID generator, including the values of its constituent parts.
     *
     * @param  lastId  The value of the last-generated ID associated with that ID generator.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     *      *
     * @throws  DocumentNotFoundException if the requested ID generator could not be found.
     */
    public void updateLastID(String csid, String lastId)
            throws IllegalStateException, DocumentNotFoundException, NamingException, SQLException {

        logger.debug("> in updateLastID");

        // @TODO Where relevant, implement logic to check for ID availability,
        // after generating a candidate ID.

        // @TODO: Add checks for authorization to perform this operation.

        Connection conn = null;
        try {

            conn = getJdbcConnection();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            // Test whether this ID generator already exists in the database.
            // Using a 'SELECT ... FOR UPDATE' statement will temporarily
            // lock this row for updates from any other connection, until
            // the UPDATE is committed, below.
            ResultSet rs = stmt.executeQuery(
                    "SELECT csid "
                    + "FROM id_generators "
                    + "WHERE csid='"
                    + csid
                    + "' FOR UPDATE");

            boolean moreRows = rs.next();

            boolean idGeneratorFound = true;
            if (!moreRows) {
                idGeneratorFound = false;
            }

            // If this ID generator was not found in the
            // database, an update can't be performed.
            // Close the connection and throw an exception.
            if (!idGeneratorFound) {
                conn.close();
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

            PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
            ps.setString(1, lastId);
            ps.setString(2, csid);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated != 1) {
                throw new IllegalStateException(
                        "Error updating last-generated ID in the database "
                        + "for ID generator '" + csid + "'");
            }

            conn.commit();
            conn.close();

            logger.debug("Successfully updated last-generated ID: " + lastId);

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException("Error updating last-generated "
                    + "ID in the database: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
            	logger.error("Error closing JDBC connection: ", e);
                // Do nothing here
            }
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
    public String readLastID(String csid) throws Exception {

        logger.debug("> in readLastID");

        // @TODO Where relevant, implement logic to check for ID availability,
        // after generating a candidate ID.

        // @TODO: Add checks for authorization to perform this operation.

        String lastId = null;
        Connection conn = null;
        try {

            conn = getJdbcConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT last_generated_id FROM id_generators "
                    + "WHERE csid='" + csid + "'");

            boolean moreRows = rs.next();
            if (!moreRows) {
                throw new DocumentNotFoundException(
                        "ID generator " + "\'" + csid + "\'" + " could not be found.");
            }
            lastId = (rs.getString(1) != null ? rs.getString(1) : "");
            logger.debug("> retrieved ID: " + lastId);

            rs.close();

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException("Error retrieving last ID "
                    + "from the database: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Do nothing here
            }
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
    public void createIDGenerator(String csid, SettableIDGenerator generator)
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
            createIDGenerator(csid, serializedGenerator);
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
     * @param  serializedGenerator  A serialized ID generator, reflecting its current state,
     *                              including the values of its constituent parts.
     *
     * @throws  BadRequestException if the provided representation of an
     *          ID generator instance is not in the correct format, contains
     *          invalid values, or otherwise cannot be used.
     *
     * @throws  IllegalStateException if a storage-related error occurred.
     */
    @Override
    public void createIDGenerator(String csid, String serializedGenerator)
            throws Exception {

        logger.debug("> in createIDGenerator(String, String)");

        // @TODO Add checks for authorization to perform this operation.

        if (serializedGenerator == null || serializedGenerator.equals("")) {
            throw new BadRequestException(
                    "Could not understand or parse this representation "
                    + "of an ID generator.");
        }

        Connection conn = null;
        try {

            conn = getJdbcConnection();
            Statement stmt = conn.createStatement();

            // Test whether this ID generator already exists in the database.
            //
            // @TODO This check should extend further, to other aspects
            // of the generator, such as its URI, if any, and its structure,
            // so we avoid duplication based on content as well as identifier.
            ResultSet rs = stmt.executeQuery(
                    "SELECT csid FROM id_generators "
                    + "WHERE csid='" + csid + "'");

            boolean moreRows = rs.next();

            boolean idGeneratorFound = true;
            if (!moreRows) {
                idGeneratorFound = false;
            }

            // If this ID generator already exists in the database,
            // throw an Exception.
            //
            // @TODO This exception needs to convey the meaning that a
            // conflict has occurred, so that this status can be reported
            // to the client.
            if (idGeneratorFound) {
                throw new IllegalStateException(
                        "Conflict with existing generator when attempting to add "
                        + "new ID generator with ID '"
                        + csid
                        + "' to the database.");

                // Otherwise, add this new ID generator, as a new record to
                // the database.
            } else {

                final String SQL_STATEMENT_STRING =
                        "INSERT INTO id_generators "
                        + "("
                        + "csid, "
                        + "id_generator_state, "
                        + "last_generated_id"
                        + ")"
                        + " VALUES (?, ?, ?)";

                PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
                ps.setString(1, csid);
                ps.setString(2, serializedGenerator);
                ps.setNull(3, java.sql.Types.VARCHAR);

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated != 1) {
                    throw new IllegalStateException(
                            "Error adding new ID generator '" + csid
                            + "'" + " to the database.");
                }

            } // end if (idGeneratorFound)

            logger.debug("> successfully added ID generator: " + csid);

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException("Error adding new ID "
                    + "generator to the database: " + e.getMessage());
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
    public IDGeneratorInstance readIDGenerator(String csid) throws Exception {

        logger.debug("> in readIDGenerator");

        IDGeneratorInstance instance = null;

        Connection conn = null;
        try {

            conn = getJdbcConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT csid, displayname, description, "
                    + "id_generator_state, last_generated_id FROM id_generators "
                    + "WHERE csid='" + csid + "'");

            boolean moreRows = rs.next();
            if (!moreRows) {
                throw new DocumentNotFoundException(
                        "ID generator with ID "
                        + "\'" + csid + "\'"
                        + " could not be found.");
            }

            instance = new IDGeneratorInstance();
            instance.setDisplayName(rs.getString(2) != null ? rs.getString(2) : "");
            instance.setDescription(rs.getString(3) != null ? rs.getString(3) : "");
            instance.setGeneratorState(rs.getString(4) != null ? rs.getString(4) : "");
            instance.setLastGeneratedID(rs.getString(5) != null ? rs.getString(5) : "");

            rs.close();

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error retrieving ID generator "
                    + "\'" + csid + "\'"
                    + " from database: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Do nothing here
            }
        }

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
    public Map<String, IDGeneratorInstance> readIDGeneratorsList()
            throws Exception {

        logger.debug("> in readIDGeneratorsList");

        Map<String, IDGeneratorInstance> generators =
                new LinkedHashMap<String, IDGeneratorInstance>();

        Connection conn = null;
        try {

            conn = getJdbcConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT csid, displayname, description, "
                    + "id_generator_state, last_generated_id FROM id_generators "
                    + "ORDER BY displayname ASC"); // , priority ASC");

            boolean moreRows = rs.next();
            if (!moreRows) {
                return generators;
            }

            IDGeneratorInstance instance = null;
            while (moreRows = rs.next()) {
                instance = new IDGeneratorInstance();
                instance.setDisplayName(rs.getString(2) != null ? rs.getString(2) : "[No display name]");
                instance.setDescription(rs.getString(3) != null ? rs.getString(3) : "[No description]");
                instance.setGeneratorState(rs.getString(4) != null ? rs.getString(4) : "[No generator state]");
                instance.setLastGeneratedID(rs.getString(5) != null ? rs.getString(5) : "[No last generated ID]");
                generators.put(rs.getString(1), instance);
            }

            rs.close();

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error retrieving ID generators "
                    + " from database: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Do nothing here
            }
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
    public void updateIDGenerator(String csid, SettableIDGenerator generator)
            throws Exception {

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
            updateIDGenerator(csid, serializedGenerator);
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
     * @param  serializedGenerator
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
    public void updateIDGenerator(String csid, String serializedGenerator)
            throws Exception {

        logger.debug("> in updateIDGenerator(String, String)");

        // @TODO: Add checks for authorization to perform this operation.

        if (serializedGenerator == null || serializedGenerator.equals("")) {
            throw new BadRequestException(
                    "Could not understand or parse this representation of an ID generator.");
        }

        SettableIDGenerator generator;
        try {
            generator = IDGeneratorSerializer.deserialize(serializedGenerator);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        String lastId = generator.getCurrentID();

        Connection conn = null;
        try {

            conn = getJdbcConnection();
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            // Test whether this ID generator already exists in the database.
            // Using a 'SELECT ... FOR UPDATE' statement will temporarily
            // lock this row for updates from any other connection, until
            // the UPDATE is committed, below.
            ResultSet rs = stmt.executeQuery(
                    "SELECT csid "
                    + "FROM id_generators "
                    + "WHERE csid='"
                    + csid
                    + "' FOR UPDATE");

            boolean moreRows = rs.next();

            boolean idGeneratorFound = true;
            if (!moreRows) {
                idGeneratorFound = false;
            }

            // If this ID generator was not found in the
            // database, an update can't be performed.
            // Close the connection and throw an exception.
            if (!idGeneratorFound) {
                conn.close();
                throw new DocumentNotFoundException(
                        "Error updating ID generator '" + csid
                        + "': generator could not be found in the database.");
            } // end if (idGeneratorFound)

            // Otherwise, if this ID generator exists in the database,
            // update its record.
            final String SQL_STATEMENT_STRING =
                    "UPDATE id_generators SET "
                    + "id_generator_state = ?, "
                    + "last_generated_id = ? "
                    + "WHERE csid = ?";

            PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
            ps.setString(1, serializedGenerator);
            ps.setString(2, lastId);
            ps.setString(3, csid);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated != 1) {
                throw new IllegalStateException(
                        "Error updating ID generator '" + csid
                        + "'" + " in the database.");
            }

            conn.commit();
            conn.close();

            logger.debug("Successfully updated ID Generator: " + csid);

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error updating ID generator in the database: " + e.getMessage());
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
    public void deleteIDGenerator(String csid)
            throws Exception {

        logger.debug("> in deleteIDGenerator");

        // @TODO: Add checks for authorization to perform this operation.

        Connection conn = null;
        try {

            conn = getJdbcConnection();
            Statement stmt = conn.createStatement();

            // Test whether this ID generator already exists in the database.
            ResultSet rs = stmt.executeQuery(
                    "SELECT csid FROM id_generators "
                    + "WHERE csid='"
                    + csid + "'");
            boolean moreRows = rs.next();

            boolean idGeneratorFound = true;
            if (!moreRows) {
                idGeneratorFound = false;
            }

            // If this ID generator already exists in the database,
            // update its record.
            if (idGeneratorFound) {

                final String SQL_STATEMENT_STRING =
                        "DELETE FROM id_generators WHERE csid = ?";

                PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
                ps.setString(1, csid);

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated != 1) {
                    throw new IllegalStateException(
                            "Error deleting ID generator '" + csid
                            + "'" + " in the database.");
                }

                // Otherwise, throw an exception, which indicates that the requested
                // ID generator was not found.
            } else {
                throw new DocumentNotFoundException(
                        "Error deleting ID generator '" + csid
                        + "': generator could not be found in the database.");
            } // end if (idGeneratorFound)

            logger.debug("Successfully deleted ID generator: " + csid);

        } catch (IllegalStateException ise) {
            throw ise;
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Error deleting ID generator in database: " + e.getMessage());
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

    // -------------------
    // Database operations
    // -------------------

    //////////////////////////////////////////////////////////////////////
    /**
     * Opens a connection to the database and returns a JDBC Connection object.
     *
     * @return  A JDBC database Connection object.
     *
     * @throws  LoginException
     * @throws  SQLException if a storage-related error occurred.
     */
    public Connection getJdbcConnection() throws NamingException, SQLException {

        logger.debug("> in getJdbcConnection");
        
        Connection conn = null;
        try {
            conn = JDBCTools.getConnection(JDBCTools.NUXEO_REPOSITORY_NAME);
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
    public boolean hasTable(String tablename) throws Exception {

        logger.debug("> in hasTable");

        if (tablename == null || tablename.equals("")) {
            return false;
        }

        Connection conn = null;
        try {

            conn = getJdbcConnection();

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
}
