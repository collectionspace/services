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
 */

// *IMPORTANT*
// @TODO: This class is in an early state of a refactoring to
// reflect a change from IDPatterns to IDGenerators at the top level
// of the ID Service.  As a result, there will be some naming
// inconsistencies throughout this source file.

// @TODO Revise exception handling to return custom Exceptions,
// perhaps mirroring the subset of HTTP status codes returned.
//
// We're currently overloading existing core and extension Java Exceptions
// in ways that are not consistent with their original semantic meaning.

// @TODO Get the JDBC driver classname and database URL from configuration;
// better yet, substitute Hibernate for JDBC for accessing database-managed persistence.

// @TODO Remove any hard-coded dependencies on MySQL.

// @TODO Determine how to restrict access to ID-related tables by role.

// @TODO Retrieve IDGenerators from the database (via JDBC or
// Hibernate) at initialization and refresh time.

// @TODO Remove redundancy.  If we're using JDBC, a great deal of JDBC code
// is replicated in each method below.

// @TODO Handle concurrency.
//
// Right now, with each new request we're simply instantiating
// a new IDPattern and returning its next ID.  As a result,
// the generated IDs may well duplicate other, previously-generated IDs.
//
// When we start storing ID generators and IDs in a database, the
// the current ID associated with each generator will be stored
// and modified in a single location.
//
// At that point, we'll also need to add code to handle concurrent requests.

// @TODO Verify access (public, protected, or private) to service methods.

// @TODO As long as we're using JDBC, use PreparedStatements, not Statements,
// throughout the code below.

// @TODO Re-consider beginnings of method names:
// - "store/get" versus:
// - "store/retrieve"
// - "save/read" (appears to be used by Hibernate),
// - "persist/find" (used by JPA)
// - or?

package org.collectionspace.services.id;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDServiceJdbcImpl
 *
 * Manages the storage of ID generators and persistence of their
 * current state, via a JDBC interface to an underlying database.
 *
 * $LastChangedBy$
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

  //////////////////////////////////////////////////////////////////////
  /**
   * Constructor (no-argument).
   */ 
  public void IDServiceJdbcImpl() {
  
    // @TODO Decide when and how to fail at startup, or else to correct
    // failure conditions automatically, when preconditions are not met.
    
    // init();
  }
  
  // @TODO init() and hasTable() are currently UNTESTED as of 2009-08-11T13:00-0700.

  //////////////////////////////////////////////////////////////////////
  /**
   * Initializes the service.
   *
   * @throws  IllegalStateException if one or more of the required preconditions
   *          for the service is not present, or is not in its required state.
   */
  public void init() throws IllegalStateException {
  
    try {
      boolean hasTable = hasTable(TABLE_NAME);
      if (! hasTable) {
        throw new IllegalStateException(
          "Table " + "\'" + TABLE_NAME + "\'" + " could not be found in the database.");
      }
    } catch (IllegalStateException e) {
      throw e;
    }
  
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

    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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
    
    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();
      
      final String CATALOG_NAME = null;
      final String SCHEMA_NAME_PATTERN = null;
      final String[] TABLE_TYPES = null;
      ResultSet tablesMatchingTableName =
        conn.getMetaData().getTables(
          CATALOG_NAME, SCHEMA_NAME_PATTERN, tablename, TABLE_TYPES);

			boolean moreRows = tablesMatchingTableName.next();
			if (! moreRows) {
        return false;
      } else {
        return true;
      }

    } catch (SQLException e) {
      throw new IllegalStateException(
        "Error while checking for existance of tablebase table: " + e.getMessage());
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
	public String newID(String csid) throws
		IllegalArgumentException, IllegalStateException {
		
		logger.debug("> in newID");

    // @TODO Where relevant, implement logic to check for ID availability,
    // after generating a candidate ID.
   
    // @TODO We're currently using simple integer IDs to identify ID generators
    // in this initial iteration.
    //
    // To uniquely identify ID generators in production, we'll need to handle
    // both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
    // other form of identifier to be determined, such as URLs or URNs.
    //
    // @TODO: Add checks for authorization to perform this operation.

		String newId = "";
		String lastId = "";
		
		if (csid == null || csid.equals("")) {
			throw new IllegalArgumentException(
				"Identifier for ID generator must not be null or empty.");
		}

    String serializedGenerator = "";
		try {
			serializedGenerator = getIDGenerator(csid);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
		
		// Guard code - should not be needed.
		if (serializedGenerator == null || serializedGenerator.equals("")) {
			throw new IllegalArgumentException(
				"Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
		}

    IDPattern pattern;
    try {
      pattern = IDPatternSerializer.deserialize(serializedGenerator);
    } catch (IllegalArgumentException e) {
	    throw e;
    }
		
		try {

      // Retrieve the last ID associated with this pattern from persistent storage.
      lastId = getLastID(csid);

 		  // If there was no last generated ID associated with this pattern,
 		  // get the current ID generated by the ID generator as the 'new' ID.
		  if (lastId == null || lastId.equals("")) {
		    newId = pattern.getCurrentID();

      // Otherwise, generate a new ID, potentially based on the last ID.
      // (This also sets the current ID of the ID generator's state
      // to this just-generated 'new' ID.)
		  } else {
        newId = pattern.nextID(lastId);
      }
      
		  // Store the 'new' ID as the last-generated ID for this pattern.
		  updateLastID(csid, newId);
		  
		  // Store the new state of this ID generator, reflecting that
		  // one of its parts may possibly have had its value updated as
		  // a result of the generation of this 'new' ID.
		  updateIDGenerator(csid, pattern);
		  
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
   * @param  pattern  An ID Pattern, including the values of its constituent parts.
   *
   * @param  lastId  The value of the last-generated ID associated with that ID generator.
   *
   * @throws  IllegalArgumentException if the requested ID generator could not be found.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID generators
   * in this initial iteration.
   *
   * To uniquely identify ID generators in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void updateLastID(String csid, String lastId)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in updateLastID");

    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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
    
    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();
			
			int rowsUpdated = stmt.executeUpdate(
			  "UPDATE id_generators SET last_generated_id='" + lastId + "' WHERE id_generator_csid='" + csid + "'");
			  
			if (rowsUpdated != 1) {
        throw new IllegalStateException(
          "Error updating last-generated ID in the database for ID Pattern '" + csid + "'");
      }

		  logger.debug("> successfully updated last-generated ID: " + lastId);

    } catch (SQLException e) {
      throw new IllegalStateException("Error updating last-generated ID in the database: " + e.getMessage());
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
	public String getLastID(String csid) throws IllegalArgumentException, IllegalStateException {

		logger.debug("> in getLastID");

    // @TODO: We're currently using simple integer IDs to identify ID generators
    // in this initial iteration.
    //
    // To uniquely identify ID generators in production, we'll need to handle
    // both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
    // other form of identifier to be determined, such as URLs or URNs.
    //
    // @TODO: Refactor to remove redundant code that this method shares with other
    // database-using methods in this class.
    //
    // @TODO: Determine whether to add checks for authorization to perform this operation.

    String lastId = null;
    
    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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

    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(
			  "SELECT last_generated_id FROM id_generators WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			if (! moreRows) {
        throw new IllegalArgumentException(
          "Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
      }

			lastId = rs.getString(1);

		  logger.debug("> retrieved ID: " + lastId);

			rs.close();

    } catch (SQLException e) {
      throw new IllegalStateException("Error retrieving last ID from the database: " + e.getMessage());
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

  //////////////////////////////////////////////////////////////////////
  /**
   * Adds a new ID generator to persistent storage.
   *
   * @param  csid     An identifier for an ID generator.
   *
   * @param  pattern  An ID generator, reflecting its current state,
   *                  including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   */
	public void addIDGenerator(String csid, IDPattern pattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in addIDGenerator(String, IDPattern)");

    // @TODO: We're currently using simple integer IDs to identify ID generators
    // in this initial iteration.
    //
    // To uniquely identify ID generators in production, we'll need to handle
    // both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
    // other form of identifier to be determined, such as URLs or URNs.

    // @TODO: Refactor to remove redundant code that this method shares with other
    // database-using methods in this class.

    // @TODO: Add checks for authorization to perform this operation.

	  if (pattern == null) {
	    throw new IllegalArgumentException("New ID generator to add cannot be null.");
	  }

    String serializedGenerator = "";
    try {
      serializedGenerator = IDPatternSerializer.serialize(pattern);
    } catch (IllegalArgumentException e) {
	    throw e;
    }

		try {
			addIDGenerator(csid, serializedGenerator);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
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
   *                            including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   */
	public void addIDGenerator(String csid, String serializedGenerator)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in addIDGenerator(String, String)");

    // @TODO We're currently using simple integer IDs to identify ID generators
    // in this initial iteration.
    //
    // To uniquely identify ID generators in production, we'll need to handle
    // both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
    // other form of identifier to be determined, such as URLs or URNs.
    
    // @TODO Refactor to remove redundant code that this method shares with other
    // database-using methods in this class.
    
    // @TODO Add checks for authorization to perform this operation.
		
		if (serializedGenerator == null || serializedGenerator.equals("")) {
		  throw new IllegalArgumentException(
	      "Could not understand or parse this representation of an ID generator.");
    }

    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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
    
    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();

      // Test whether this ID generator already exists in the database.
			//
			// @TODO This check should extend further, to other aspects of the pattern,
			// such as its URI, if any, and its structure.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_csid FROM id_generators WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idPatternFound = true;
			if (! moreRows) {
        idPatternFound = false;
      }
			
			// If this ID generator already exists in the database, throw an Exception.
			//
			// @TODO This exception needs to convey the meaning that a conflict has
			// occurred, so that this status can be reported to the client.
			if (idPatternFound) {
        throw new IllegalStateException(
          "Conflict with existing pattern when attempting to add new ID generator with ID '" +
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
            "Error adding new ID generator '" + csid + "'" + " to the database.");
        }
        
       } // end if (idPatternFound)

		  logger.debug("> successfully added ID Pattern: " + csid);

    } catch (SQLException e) {
      throw new IllegalStateException("Error adding new ID generator to the database: " + e.getMessage());
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
   * Updates an existing ID generator in persistent storage.
   *
   * @param  csid     An identifier for an ID generator.
   *
   * @param  pattern  An ID generator, reflecting its current state,
   *                  including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID generators
   * in this initial iteration.
   *
   * To uniquely identify ID generators in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void updateIDGenerator(String csid, IDPattern pattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in updateIDGenerator(String, IDPattern)");

	  if (pattern == null) {
	    throw new IllegalArgumentException(
	      "ID generator supplied in an attempt to update an existing ID generator cannot be null.");
	  }

    String serializedGenerator = "";
    try {
      serializedGenerator = IDPatternSerializer.serialize(pattern);
    } catch (IllegalArgumentException e) {
	    throw e;
    }

		try {
			updateIDGenerator(csid, serializedGenerator);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
 
  }
  
  //////////////////////////////////////////////////////////////////////
  /**
   * Updates an existing ID generator in persistent storage,
   * from a serialization of the current state of that pattern.
   *
   * The serialization method recognized by this method has implementation
   * dependencies.  Currently, this method expects serialization via XStream's
   * out-of-the-box serializer, without custom configuration.
   *
   * @param  csid     An identifier for an ID generator.
   *
   * @param  serializedGenerator  A serialized ID Pattern, reflecting its current state,
   *                            including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID generators
   * in this initial iteration.
   *
   * To uniquely identify ID generators in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void updateIDGenerator(String csid, String serializedGenerator)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in updateIDGenerator(String, String)");
		
		if (serializedGenerator == null || serializedGenerator.equals("")) {
		  throw new IllegalArgumentException(
	      "Could not understand or parse this representation of an ID generator.");
    }

    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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
    
    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();

      // Test whether this ID generator already exists in the database.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_csid FROM id_generators WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idGeneratorFound = true;
			if (! moreRows) {
        idGeneratorFound = false;
      }
			
			// If this ID generator already exists in the database, update its record.
			if (idGeneratorFound) {

 			  final String SQL_STATEMENT_STRING =
			    "UPDATE id_generators SET " + 
			       "id_generator_state = ?, " + 
			       "last_generated_id = ? " + 
			    "WHERE id_generator_csid = ?";
			    
			  IDPattern pattern;
        try {
          pattern = IDPatternSerializer.deserialize(serializedGenerator);
        } catch (IllegalArgumentException e) {
          throw e;
        }
		    String lastId = pattern.getCurrentID();
			
        PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
        ps.setString(1, serializedGenerator);
        ps.setString(2, lastId);
        ps.setString(3, csid);
                
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error updating ID generator '" + csid + "'" + " in the database.");
        }
        
			// Otherwise, throw an exception, which indicates that the requested
			// ID generator was not found.
      } else {
 
        throw new IllegalArgumentException(
          "Error updating ID generator '" + csid + "': generator could not be found in the database.");
       
       } // end if (idGeneratorFound)

		  logger.debug("> successfully updated ID Generator: " + csid);

    } catch (SQLException e) {
      throw new IllegalStateException("Error updating ID generator in the database: " + e.getMessage());
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
   *
   * @TODO: We're currently using simple integer IDs to identify ID generators
   * in this initial iteration.
   *
   * To uniquely identify ID generators in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void deleteIDGenerator(String csid)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in deleteIDGenerator");
		
    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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
    
    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();

      // Test whether this ID generator already exists in the database.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_csid FROM id_generators WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idGeneratorFound = true;
			if (! moreRows) {
        idGeneratorFound = false;
      }
			
			// If this ID generator already exists in the database, update its record.
			if (idGeneratorFound) {

 			  final String SQL_STATEMENT_STRING =
			    "DELETE FROM id_generators WHERE id_generator_csid = ?";
			    
        PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
        ps.setString(1, csid);
                
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error deleting ID generator '" + csid + "'" + " in the database.");
        }
        
			// Otherwise, throw an exception, which indicates that the requested
			// ID generator was not found.
      } else {
 
        throw new IllegalArgumentException(
          "Error deleting ID generator '" + csid + "': generator could not be found in the database.");
       
       } // end if (idGeneratorFound)

		  logger.debug("> successfully deleted ID generator: " + csid);

    } catch (SQLException e) {
      throw new IllegalStateException("Error deleting ID generator in database: " + e.getMessage());
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
   * @throws  IllegalArgumentException if the requested ID generator could not be found.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID generators
   * in this initial iteration.
   *
   * To uniquely identify ID generators in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   */
	public String getIDGenerator (String csid) throws IllegalArgumentException, IllegalStateException {

		logger.debug("> in getIDGenerator");

    String serializedGenerator = null;
    
    try {
      Class.forName(JDBC_DRIVER_CLASSNAME).newInstance();
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

    Connection conn = null;
    try {
    
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_generator_state FROM id_generators WHERE id_generator_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			if (! moreRows) {
        throw new IllegalArgumentException(
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
    
	  logger.debug("> retrieved IDGenerator: " + serializedGenerator);

    return serializedGenerator;

  }
  
}
