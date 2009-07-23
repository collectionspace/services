/**
 * IDServiceJdbcImpl
 *
 * Implementation of portions of the ID Service that use JDBC
 * for persistence of IDPatterns, IDParts, and ID values.
 *
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
 * Based on work by Richard Millet and Sanjay Dalal.
 *
 * $LastChangedBy$
 * $LastChangedRevision$
 * $LastChangedDate$
 */
 
// @TODO: Revise exception handling to return custom Exceptions,
// perhaps mirroring the subset of HTTP status codes returned.
//
// We're currently overloading existing core and extension Java Exceptions
// in ways that are not consistent with their original semantic meaning.

// @TODO: Retrieve IDPatterns from the database (via JDBC or
// Hibernate) at initialization and refresh time.

// @TODO: Handle concurrency.
//
// Right now, with each new request we're simply instantiating
// a new IDPattern and returning its next ID.  As a result,
// the generated IDs may well duplicate other, previously-generated IDs.
//
// When we start storing IDPatterns and IDs in a database, the
// the current ID associated with each pattern will be stored
// and modified in a single location.
//
// At that point, we'll also need to add code to ensure that only
// one IDPattern object is instantiated for each stored pattern,
// at initialization or reset time, as well as to handle concurrent
// requests.

// @TODO: Verify access (public, protected, or private) to service methods.

// @TODO: As long as we're using JDBC, use PreparedStatements, not Statements.

// @TODO: Re-consider beginnings of method names:
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

public class IDServiceJdbcImpl implements IDService {

	final Logger logger = LoggerFactory.getLogger(IDServiceJdbcImpl.class);

  // @TODO Get the JDBC driver classname and database URL from configuration;
  // better yet, substitute Hibernate for JDBC for accessing database-managed persistence.
  //
  // @TODO: Remove any hard-coded dependencies on MySQL.
  //
  // @TODO: Determine how to restrict access to ID-related tables by role.
  final String JDBC_DRIVER_CLASSNAME = "com.mysql.jdbc.Driver";
  final String DATABASE_URL = "jdbc:mysql://localhost:3306/cspace";
  final String DATABASE_USERNAME = "test";
  final String DATABASE_PASSWORD = "test";

  //////////////////////////////////////////////////////////////////////
  /**
   * Constructor (no-argument).
   */ 
  public void IDServiceJdbcImpl() {
  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Returns the next ID associated with a specified ID pattern.
   *
   * This method has an intentional side-effect: it sets the
   * current ID of that ID pattern to the just-generated ID.
   *
   * @param  csid  An identifier for an ID pattern.
   *
   * @return  The next ID associated with the specified ID pattern.
   * 
	 * @TODO: Implement logic to check for ID availability, after generating
	 * a candidate ID.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public String nextID(String csid) throws
		IllegalArgumentException, IllegalStateException {
		
		logger.debug("> in nextID");

		String nextId = "";
		String lastId = "";
		
		if (csid == null || csid.equals("")) {
			throw new IllegalArgumentException(
				"Identifier for ID pattern must not be null or empty.");
		}

    String serializedPattern = "";
		try {
			serializedPattern = getIDPattern(csid);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
		
		// Guard code - should not be needed.
		if (serializedPattern == null || serializedPattern.equals("")) {
			throw new IllegalArgumentException(
				"Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
		}

    IDPattern pattern;
    try {
      pattern = IDPatternSerializer.deserialize(serializedPattern);
    } catch (IllegalArgumentException e) {
	    throw e;
    }
		
		try {

      // Retrieve the last ID associated with this pattern from persistent storage.
      lastId = getLastID(csid);

 		  // If there was no last generated ID associated with this pattern,
 		  // get the current ID generated by the pattern as the 'next' ID.
		  if (lastId == null || lastId.equals("")) {
		    nextId = pattern.getCurrentID();

      // Otherwise, generate the 'next' ID for this pattern, based on the last ID.
      // (This also sets the current ID for the pattern to this just-generated 'next' ID.)
		  } else {
        nextId = pattern.nextID(lastId);
      }
      
		  // Store the 'next' ID as the last-generated ID for this pattern.
		  updateLastID(csid, nextId);
		  
		  // Store the new state of this ID Pattern, reflecting that one of its
		  // parts may have had its value updated as a result of the generation
		  // of this 'next' ID.
		  updateIDPattern(csid, pattern);
		  
		} catch (IllegalArgumentException e ) {
		  throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
		
		return nextId;

	}
	
  //////////////////////////////////////////////////////////////////////
  /**
   * Stores the last-generated ID, corresponding to a specified ID pattern,
   * into persistent storage.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @param  pattern  An ID Pattern, including the values of its constituent parts.
   *
   * @param  lastId  The value of the last-generated ID associated with that ID pattern.
   *
   * @throws  IllegalArgumentException if the requested ID pattern could not be found.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
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
			  "UPDATE id_patterns SET last_generated_id='" + lastId + "' WHERE id_pattern_csid='" + csid + "'");
			  
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
   * Returns the last-generated ID, corresponding to a specified ID pattern,
   * from persistent storage.
   *
   * @param  csid  An identifier for an ID pattern.
   *
   * @return  The last ID generated that corresponds to the requested ID pattern.
   *
   * @throws  IllegalArgumentException if the requested ID pattern could not be found.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Determine whether to add checks for authorization to perform this operation.
   */
	public String getLastID(String csid) throws IllegalArgumentException, IllegalStateException {

		logger.debug("> in getLastID");

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
			  "SELECT last_generated_id FROM id_patterns WHERE id_pattern_csid='" + csid + "'");
			  
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
   * Adds a new ID pattern to persistent storage.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @param  pattern  An ID pattern, reflecting its current state,
   *                  including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void addIDPattern(String csid, IDPattern pattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in addIDPattern(String, IDPattern)");

	  if (pattern == null) {
	    throw new IllegalArgumentException("New ID pattern to add cannot be null.");
	  }

    String serializedPattern = "";
    try {
      serializedPattern = IDPatternSerializer.serialize(pattern);
    } catch (IllegalArgumentException e) {
	    throw e;
    }

		try {
			addIDPattern(csid, serializedPattern);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}

  }

  //////////////////////////////////////////////////////////////////////
  /**
   * Adds a new ID pattern to persistent storage, from a serialization of that pattern.
   *
   * The serialization method recognized by this method has implementation
   * dependencies.  Currently, this method expects serialization via XStream's
   * out-of-the-box serializer, without custom configuration.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @param  serializedPattern  A serialized ID Pattern, reflecting its current state,
   *                            including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void addIDPattern(String csid, String serializedPattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in addIDPattern(String, String)");
		
		if (serializedPattern == null || serializedPattern.equals("")) {
		  throw new IllegalArgumentException(
	      "Could not understand or parse this representation of an ID pattern.");
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

      // Test whether this ID pattern already exists in the database.
			//
			// @TODO This check should extend further, to other aspects of the pattern,
			// such as its URI, if any, and its structure.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_pattern_csid FROM id_patterns WHERE id_pattern_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idPatternFound = true;
			if (! moreRows) {
        idPatternFound = false;
      }
			
			// If this ID pattern already exists in the database, throw an Exception.
			//
			// @TODO This exception needs to convey the meaning that a conflict has
			// occurred, so that this status can be reported to the client.
			if (idPatternFound) {
        throw new IllegalStateException(
          "Conflict with existing pattern when attempting to add new ID pattern with ID '" +
          csid +
          "' to the database.");
          
 			// Otherwise, add this new ID pattern, as a new record to the database.
      } else {
 
        final String SQL_STATEMENT_STRING =
			    "INSERT INTO id_patterns " +
			    "(" +
			      "id_pattern_csid, " +
			      "id_pattern_state, " + 
			      "last_generated_id" +
			    ")" +
			    " VALUES (?, ?, ?)";
			    
        PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
        ps.setString(1, csid);
        ps.setString(2, serializedPattern);
        ps.setNull(3, java.sql.Types.VARCHAR);
        
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error adding new ID pattern '" + csid + "'" + " to the database.");
        }
        
       } // end if (idPatternFound)

		  logger.debug("> successfully added ID Pattern: " + csid);

    } catch (SQLException e) {
      throw new IllegalStateException("Error adding new ID pattern to the database: " + e.getMessage());
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
   * Updates an existing ID pattern in persistent storage.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @param  pattern  An ID pattern, reflecting its current state,
   *                  including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void updateIDPattern(String csid, IDPattern pattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in updateIDPattern(String, IDPattern)");

	  if (pattern == null) {
	    throw new IllegalArgumentException(
	      "ID pattern supplied in an attempt to update an existing ID pattern cannot be null.");
	  }

    String serializedPattern = "";
    try {
      serializedPattern = IDPatternSerializer.serialize(pattern);
    } catch (IllegalArgumentException e) {
	    throw e;
    }

		try {
			updateIDPattern(csid, serializedPattern);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
 
  }
  
  //////////////////////////////////////////////////////////////////////
  /**
   * Updates an existing ID pattern in persistent storage,
   * from a serialization of the current state of that pattern.
   *
   * The serialization method recognized by this method has implementation
   * dependencies.  Currently, this method expects serialization via XStream's
   * out-of-the-box serializer, without custom configuration.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @param  serializedPattern  A serialized ID Pattern, reflecting its current state,
   *                            including the values of its constituent parts.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void updateIDPattern(String csid, String serializedPattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in updateIDPattern(String, String)");
		
		if (serializedPattern == null || serializedPattern.equals("")) {
		  throw new IllegalArgumentException(
	      "Could not understand or parse this representation of an ID pattern.");
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

      // Test whether this ID pattern already exists in the database.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_pattern_csid FROM id_patterns WHERE id_pattern_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idPatternFound = true;
			if (! moreRows) {
        idPatternFound = false;
      }
			
			// If this ID pattern already exists in the database, update its record.
			if (idPatternFound) {

 			  final String SQL_STATEMENT_STRING =
			    "UPDATE id_patterns SET " + 
			       "id_pattern_state = ?, " + 
			       "last_generated_id = ? " + 
			    "WHERE id_pattern_csid = ?";
			    
			  IDPattern pattern;
        try {
          pattern = IDPatternSerializer.deserialize(serializedPattern);
        } catch (IllegalArgumentException e) {
          throw e;
        }
		    String lastId = pattern.getCurrentID();
			
        PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
        ps.setString(1, serializedPattern);
        ps.setString(2, lastId);
        ps.setString(3, csid);
                
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error updating ID pattern '" + csid + "'" + " in the database.");
        }
        
			// Otherwise, throw an exception, which indicates that the requested
			// ID pattern was not found.
      } else {
 
        throw new IllegalArgumentException(
          "Error updating ID pattern '" + csid + "': pattern could not be found in the database.");
       
       } // end if (idPatternFound)

		  logger.debug("> successfully updated ID Pattern: " + csid);

    } catch (SQLException e) {
      throw new IllegalStateException("Error updating ID pattern in the database: " + e.getMessage());
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
   * Deletes an existing ID pattern from persistent storage.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   *
   * @TODO: Add checks for authorization to perform this operation.
   */
	public void deleteIDPattern(String csid)
	  throws IllegalArgumentException, IllegalStateException {
    
		logger.debug("> in deleteIDPattern");
		
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

      // Test whether this ID pattern already exists in the database.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_pattern_csid FROM id_patterns WHERE id_pattern_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idPatternFound = true;
			if (! moreRows) {
        idPatternFound = false;
      }
			
			// If this ID pattern already exists in the database, update its record.
			if (idPatternFound) {

 			  final String SQL_STATEMENT_STRING =
			    "DELETE FROM id_patterns WHERE id_pattern_csid = ?";
			    
        PreparedStatement ps = conn.prepareStatement(SQL_STATEMENT_STRING);
        ps.setString(1, csid);
                
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error deleting ID pattern '" + csid + "'" + " in the database.");
        }
        
			// Otherwise, throw an exception, which indicates that the requested
			// ID pattern was not found.
      } else {
 
        throw new IllegalArgumentException(
          "Error deleting ID pattern '" + csid + "': pattern could not be found in the database.");
       
       } // end if (idPatternFound)

		  logger.debug("> successfully deleted ID Pattern: " + csid);

    } catch (SQLException e) {
      throw new IllegalStateException("Error deleting ID pattern in database: " + e.getMessage());
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
   * Returns a requested ID pattern from persistent storage.  This pattern will
   * include values for the last-generated IDs of each of its constituent parts,
   * and thus can be used to generate a current or 'next' ID for that pattern.
   *
   * @param  csid  An identifier for an ID pattern.
   *
   * @return  A serialized representation of the requested ID pattern.
   *
   * @throws  IllegalArgumentException if the requested ID pattern could not be found.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   *
   * @TODO: We're currently using simple integer IDs to identify ID patterns
   * in this initial iteration.
   *
   * To uniquely identify ID patterns in production, we'll need to handle
   * both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
   * other form of identifier to be determined, such as URLs or URNs.
   *
   * @TODO: Refactor to remove redundant code that this method shares with other
   * database-using methods in this class.
   */
	public String getIDPattern (String csid) throws IllegalArgumentException, IllegalStateException {

		logger.debug("> in getIDPattern");

    String serializedPattern = null;
    
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
			  "SELECT id_pattern_state FROM id_patterns WHERE id_pattern_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			if (! moreRows) {
        throw new IllegalArgumentException(
          "Pattern with ID " +
          "\'" + csid + "\'" +
          " could not be found.");
      }

			serializedPattern = rs.getString(1);
			
			rs.close();

    } catch (SQLException e) {
      throw new IllegalStateException(
        "Error retrieving ID pattern " +
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
    
	  logger.debug("> retrieved IDPattern: " + serializedPattern);

    return serializedPattern;

  }
  
}
