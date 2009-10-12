/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c)) 2009 Regents of the University of California
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

// @TODO Get the JDBC driver classname and database URL from configuration;
// better yet, substitute JPA or Hibernate for JDBC for accessing
// database-managed persistence.

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

// May at some point instead use
// org.jboss.resteasy.spi.NotFoundException
import java.util.ArrayList;
import java.util.List;
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;

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
	
	final String JDBC_DRIVER_CLASSNAME = "com.mysql.jdbc.Driver";
	final String DATABASE_NAME = "cspace";
	final String DATABASE_URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME;
	final String DATABASE_USERNAME = "test";
	final String DATABASE_PASSWORD = "test";
	final String TABLE_NAME = "id_generator";
	
	boolean hasPreconditions = true;

	//////////////////////////////////////////////////////////////////////
	/**
	* Constructor (no-argument).
	*/ 
	public void IDServiceJdbcImpl() throws IllegalStateException {
	
		// @TODO Decide when and how to fail at startup, or else to correct
		// failure conditions automatically, when preconditions are not met.
		//
		// Currently, errors at initialization are merely informative and
		// result in exceptions that can be persistently logged.
	
		try {
			init();
		} catch (IllegalStateException e) {
			throw e;
		}
	
	}
	
	// @TODO init() is currently UNTESTED as of 2009-08-11T13:00-0700.
	
    //////////////////////////////////////////////////////////////////////
   /**
	* Initializes the service.
	*
	* @throws  IllegalStateException if one or more of the required preconditions
	*          for the service is not present, or is not in its required state.
	*/
	public void init() throws IllegalStateException {
	
		logger.debug("> in init");
	
		try {
			instantiateJdbcDriver(JDBC_DRIVER_CLASSNAME);
		} catch (IllegalStateException e) {
			throw e;
		}
		
		try {
			boolean hasTable = hasTable(TABLE_NAME);
			if (! hasTable) {
			String msg =
				"Required table " +
				"\'" + TABLE_NAME + "\'" +
				" could not be found in the database.";
				logger.warn(msg);
			throw new IllegalStateException(msg);
		  }
		} catch (IllegalStateException e) {
			throw e;
		}
	
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
	public String createID(String csid) throws DocumentNotFoundException,
		BadRequestException, IllegalArgumentException, IllegalStateException {
		
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
			serializedGenerator = readIDGenerator(csid);
		} catch (DocumentNotFoundException e ) {
			throw e;
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
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
		  
		} catch (IllegalArgumentException e ) {
		    throw e;
		} catch (IllegalStateException e ) {
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
	* @throws  IllegalArgumentException if the requested ID generator could not be found.
	*
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
	public void updateLastID(String csid, String lastId)
	  throws IllegalArgumentException, IllegalStateException {
	
		logger.debug("> in updateLastID");
		
		// @TODO Where relevant, implement logic to check for ID availability,
		// after generating a candidate ID.
		
		// @TODO: Add checks for authorization to perform this operation.
		
		Connection conn = null;
		try {
		
			conn = getJdbcConnection();
			Statement stmt = conn.createStatement();
			
			int rowsUpdated = stmt.executeUpdate(
			  "UPDATE id_generators SET last_generated_id='" + lastId +
			  "' WHERE id_generator_csid='" + csid + "'");
			  
			if (rowsUpdated != 1) {
				throw new IllegalStateException(
					"Error updating last-generated ID in the database " +
					"for ID generator '" + csid + "'");
			}
		
		  logger.debug("> successfully updated last-generated ID: " + lastId);
		
		} catch (SQLException e) {
			throw new IllegalStateException("Error updating last-generated " +
			    "ID in the database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
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
	* @throws  IllegalArgumentException if the requested ID generator could not be found.
	*
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
    @Override
	public String readLastID(String csid) throws IllegalArgumentException,
		IllegalStateException {
	
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
			  "SELECT last_generated_id FROM id_generators " + 
			  "WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			if (! moreRows) {
			throw new IllegalArgumentException(
				"ID generator " + "\'" + csid + "\'" + " could not be found.");
			}
		
			lastId = rs.getString(1);
		  	logger.debug("> retrieved ID: " + lastId);
		
			rs.close();
		
		} catch (SQLException e) {
			throw new IllegalStateException("Error retrieving last ID " + 
			    "from the database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
				  conn.close();
				}
			} catch(SQLException e) {
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
	* Adds a new ID generator to persistent storage.
	*
	* @param  csid     An identifier for an ID generator.
	*
	* @param  generator  An ID generator, reflecting its current state,
	*                  including the values of its constituent parts.
	*
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
	public void createIDGenerator(String csid, SettableIDGenerator generator)
	  throws IllegalArgumentException, IllegalStateException {
	
		logger.debug("> in createIDGenerator(String, SettableIDGenerator)");
	
		// @TODO: Add checks for authorization to perform this operation.
		
		if (generator == null) {
			throw new IllegalArgumentException("New ID generator " + 
			    "to add cannot be null.");
		}
		
		String serializedGenerator = "";
		try {
			serializedGenerator = IDGeneratorSerializer.serialize(generator);
		} catch (IllegalArgumentException e) {
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
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
	@Override
	public void createIDGenerator(String csid, String serializedGenerator)
	  throws IllegalArgumentException, IllegalStateException {
	
		logger.debug("> in createIDGenerator(String, String)");
	
		// @TODO Add checks for authorization to perform this operation.
			
		if (serializedGenerator == null || serializedGenerator.equals("")) {
			throw new IllegalArgumentException(
				"Could not understand or parse this representation " + 
				"of an ID generator.");
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
			  "SELECT id_generator_csid FROM id_generators " + 
			  "WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idGeneratorFound = true;
			if (! moreRows) {
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
					"Conflict with existing generator when attempting to add " +
					"new ID generator with ID '" +
					csid +
					"' to the database.");
			  
			// Otherwise, add this new ID generator, as a new record to the database.
			} else {
			
				final String SQL_STATEMENT_STRING =
					"INSERT INTO id_generators " +
					"(" +
					  "id_generator_csid, " +
					  "id_generator_state, " + 
					  "last_generated_id" +
					")" +
					" VALUES (?, ?, ?)";
					
				PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
				ps.setString(1, csid);
				ps.setString(2, serializedGenerator);
				ps.setNull(3, java.sql.Types.VARCHAR);
				
				int rowsUpdated = ps.executeUpdate();
				if (rowsUpdated != 1) {
					throw new IllegalStateException(
						"Error adding new ID generator '" + csid +
						"'" + " to the database.");
				}
			
			} // end if (idGeneratorFound)
			
			  logger.debug("> successfully added ID generator: " + csid);
			
			} catch (SQLException e) {
				throw new IllegalStateException("Error adding new ID " +
				    "generator to the database: " + e.getMessage());
			} finally {
				try {
					if (conn != null) {
					    conn.close();
					}
				} catch(SQLException e) {
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
	* @throws  IllegalArgumentException if the requested ID generator could
	*          not be found.
	*
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
    @Override
	public String readIDGenerator (String csid) throws
	    DocumentNotFoundException, IllegalArgumentException,
        IllegalStateException {
	
		logger.debug("> in readIDGenerator");
		
		String serializedGenerator = null;
		
		Connection conn = null;
		try {
		
			conn = getJdbcConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_state FROM id_generators " + 
			  "WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			if (! moreRows) {
				throw new DocumentNotFoundException(
				    "ID generator with ID " +
				    "\'" + csid + "\'" +
				    " could not be found.");
			}
		
			serializedGenerator = rs.getString(1);
			
			rs.close();
		
		} catch (SQLException e) {
			throw new IllegalStateException(
				"Error retrieving ID generator " +
				"\'" + csid + "\'" +
				" from database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
				// Do nothing here
			}
		}
		
		logger.debug("> retrieved SettableIDGenerator: " + serializedGenerator);
		
		return serializedGenerator;
		  
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
    public List readIDGeneratorsList() throws IllegalStateException {

		logger.debug("> in readIDGeneratorsList");

		List<String> generators = new ArrayList<String>();

		Connection conn = null;
		try {

			conn = getJdbcConnection();
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_state FROM id_generators");

			boolean moreRows = rs.next();
			if (! moreRows) {
				return generators;
			}

            while (moreRows = rs.next()) {
                generators.add(rs.getString(1));
            }

			rs.close();

		} catch (SQLException e) {
			throw new IllegalStateException(
				"Error retrieving ID generators " +
				" from database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
				// Do nothing here
			}
		}

		logger.debug("> retrieved list: ");
        for (String generator : generators) {
            logger.debug("generator=\n" + generator);
        }

		return generators;
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
    public List readIDGeneratorsSummaryList() throws IllegalStateException {

		logger.debug("> in readIDGeneratorsSummaryList");

		List<String> generators = new ArrayList<String>();

		Connection conn = null;
		try {

			conn = getJdbcConnection();
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_csid FROM id_generators");

			boolean moreRows = rs.next();
			if (! moreRows) {
				return generators;
			}

            while (moreRows = rs.next()) {
                generators.add(rs.getString(1));
            }

			rs.close();

		} catch (SQLException e) {
			throw new IllegalStateException(
				"Error retrieving ID generators " +
				" from database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
				// Do nothing here
			}
		}

		logger.debug("> retrieved list: ");
        for (String generator : generators) {
            logger.debug("generator=\n" + generator);
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
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
	public void updateIDGenerator(String csid, SettableIDGenerator generator)
	  throws BadRequestException, IllegalArgumentException, IllegalStateException, DocumentNotFoundException {
	
		logger.debug("> in updateIDGenerator(String, SettableIDGenerator)");
	
		// @TODO: Add checks for authorization to perform this operation.
	
		if (generator == null) {
			throw new BadRequestException(
				"ID generator provided in update operation cannot be null.");
		}
		
		String serializedGenerator = "";
		try {
			serializedGenerator = IDGeneratorSerializer.serialize(generator);
		} catch (IllegalArgumentException e) {
			throw e;
		}
	
		try {
			updateIDGenerator(csid, serializedGenerator);
		} catch (DocumentNotFoundException e ) {
			throw e;
		} catch (BadRequestException e ) {
			throw e;
		} catch (IllegalStateException e ) {
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
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
	@Override
	public void updateIDGenerator(String csid, String serializedGenerator)
	  throws DocumentNotFoundException, BadRequestException,
        IllegalArgumentException, IllegalStateException {
	
		logger.debug("> in updateIDGenerator(String, String)");
	
		// @TODO: Add checks for authorization to perform this operation.
		
		if (serializedGenerator == null || serializedGenerator.equals("")) {
		  throw new BadRequestException(
		  	"Could not understand or parse this representation of an ID generator.");
		}
	
		Connection conn = null;
		try {
		
			conn = getJdbcConnection();
			Statement stmt = conn.createStatement();
		
		  	// Test whether this ID generator already exists in the database.
			ResultSet rs = stmt.executeQuery(
				"SELECT id_generator_csid FROM id_generators " +
				"WHERE id_generator_csid='" +
				csid + "'");
				  
			boolean moreRows = rs.next();
				
			boolean idGeneratorFound = true;
			if (! moreRows) {
				idGeneratorFound = false;
		  	}
				
			// If this ID generator already exists in the database,
			// update its record.
			if (idGeneratorFound) {
	
		    	final String SQL_STATEMENT_STRING =
				  "UPDATE id_generators SET " + 
				   "id_generator_state = ?, " + 
				   "last_generated_id = ? " + 
				  "WHERE id_generator_csid = ?";
				  
				SettableIDGenerator generator;
				try {
					generator = IDGeneratorSerializer.deserialize(serializedGenerator);
				} catch (IllegalArgumentException e) {
					throw e;
				}
				String lastId = generator.getCurrentID();
					
				PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
				ps.setString(1, serializedGenerator);
				ps.setString(2, lastId);
				ps.setString(3, csid);
						
				int rowsUpdated = ps.executeUpdate();
				if (rowsUpdated != 1) {
					throw new IllegalStateException(
						"Error updating ID generator '" + csid +
						"'" + " in the database.");
				}
				
			// Otherwise, throw an exception, which indicates that the requested
			// ID generator was not found.
			} else {
				throw new DocumentNotFoundException(
				  "Error updating ID generator '" + csid +
				  "': generator could not be found in the database.");
			} // end if (idGeneratorFound)
		
			logger.debug("> successfully updated ID Generator: " + csid);
		
		} catch (SQLException e) {
			throw new IllegalStateException(
				"Error updating ID generator in the database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
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
	* @throws  IllegalStateException if a storage-related error occurred.
	*/
	public void deleteIDGenerator(String csid)
	  throws DocumentNotFoundException, IllegalArgumentException,
        IllegalStateException {
	
		logger.debug("> in deleteIDGenerator");
		
		// @TODO: Add checks for authorization to perform this operation.
		
		Connection conn = null;
		try {
		
			conn = getJdbcConnection();
			Statement stmt = conn.createStatement();
			
			// Test whether this ID generator already exists in the database.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_csid FROM id_generators " +
			  "WHERE id_generator_csid='" +
			  csid + "'");
			boolean moreRows = rs.next();
			
			boolean idGeneratorFound = true;
			if (! moreRows) {
				idGeneratorFound = false;
			}
			
			// If this ID generator already exists in the database,
			// update its record.
			if (idGeneratorFound) {
		
			   final String SQL_STATEMENT_STRING =
			       "DELETE FROM id_generators WHERE id_generator_csid = ?";
				
				PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
				ps.setString(1, csid);
						
				int rowsUpdated = ps.executeUpdate();
				if (rowsUpdated != 1) {
				throw new IllegalStateException(
					"Error deleting ID generator '" + csid +
					"'" + " in the database.");
				}
		
			// Otherwise, throw an exception, which indicates that the requested
			// ID generator was not found.
			} else {
				throw new DocumentNotFoundException(
				  "Error deleting ID generator '" + csid +
				  "': generator could not be found in the database.");
			} // end if (idGeneratorFound)
		
			logger.debug("> successfully deleted ID generator: " + csid);
		
		} catch (SQLException e) {
			throw new IllegalStateException(
				"Error deleting ID generator in database: " + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
				// Do nothing here
			}
		}
	
	}
	
	// -------------------
    // Database operations
    // -------------------
    
   //////////////////////////////////////////////////////////////////////
   /**
	* Creates a new instance of the specified JDBC driver class.
	*
	* @param   jdbcDriverClassname  The name of a JDBC driver class.
	*
	* @throws  IllegalStateException if a new instance of the specified
	*          JDBC driver class cannot be created.
	*/
	public void instantiateJdbcDriver(String jdbcDriverClassname)
	throws IllegalStateException {
	
		logger.debug("> in instantiateJdbcDriver(String)");
	
		try {
			Class.forName(jdbcDriverClassname).newInstance();
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(
				"Error finding JDBC driver class '" +
				JDBC_DRIVER_CLASSNAME +
				"' to set up database connection.");
		} catch (InstantiationException e) {
			throw new IllegalStateException(
				"Error instantiating JDBC driver class '" +
				JDBC_DRIVER_CLASSNAME +
				"' to set up database connection.");
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(
				"Error accessing JDBC driver class '" +
				JDBC_DRIVER_CLASSNAME +
				"' to set up database connection.");
		}
	  
	}

	//////////////////////////////////////////////////////////////////////
	/**
	* Opens a connection to the database and returns a JDBC Connection object.
	*
	* @return  A JDBC database Connection object.
	*
	* @throws  SQLException if a storage-related error occurred.
	*/
	public Connection getJdbcConnection() throws SQLException {
	
		logger.debug("> in getJdbcConnection");
		
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(DATABASE_URL,
			    DATABASE_USERNAME, DATABASE_PASSWORD);
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
	public boolean hasTable(String tablename) throws IllegalStateException {
	
		logger.debug("> in hasTable");
	
		if (tablename == null || tablename.equals("")) {
			return false;
		}
	  
		Connection conn = null;
		try {
		
			conn = getJdbcConnection();
			
			// Retrieve a list of tables in the current database. 
			final String CATALOG_NAME = null;
			final String SCHEMA_NAME_PATTERN = null;
			final String[] TABLE_TYPES = null;
			ResultSet tablesMatchingTableName =
				conn.getMetaData().getTables(
					CATALOG_NAME, SCHEMA_NAME_PATTERN, tablename, TABLE_TYPES);
		
			// Check whether a table with the specified name is in that list. 
			boolean moreRows = tablesMatchingTableName.next();
			if (! moreRows) {
				return false;
			} else {
				return true;
			}
		
		} catch (SQLException e) {
			throw new IllegalStateException(
				"Error while checking for existence of database table: " +
				e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
				// Do nothing here
			}
		}
	
	}

		
}