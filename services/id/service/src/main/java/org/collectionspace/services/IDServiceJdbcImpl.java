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

// @TODO: Replace wildcarded import statement for
// org.collectionspace.services.id.* with class-specific
// import statements.  Determine how to properly handle
// what may be a varying set, over time, of subclasses of IDPart.

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

package org.collectionspace.services;

import org.collectionspace.services.IDService;
// The following import statement has been left open-ended
// to accommodate future ID generation components.
import org.collectionspace.services.id.*;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
// import com.thoughtworks.xstream.io.HierarchicalStreamDriver; 

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class IDServiceJdbcImpl implements IDService {

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

  // Constructor.
  public void IDServiceJdbcImpl() {
  }

  // Temporary expedient to populate the database with hard-coded ID Pattens.
  //
  // @TODO: Remove this temporary expedient when possible.
  public void init() {

    Integer integer;
    IDPattern pattern;
    String csid = "";
    
    // Test whether ID patterns 1 and 2 exist in the database;
    // if not, create and populate records for those patterns.
    
    csid = "1";
		try {
			pattern = getIDPattern(csid);
		} catch (IllegalArgumentException e ) {
      pattern = new IDPattern(csid);
      pattern.add(new StringIDPart("E"));
      pattern.add(new NumericIDPart("1"));
      storeIDPattern(csid, pattern);
    }

    csid = "2";
		try {
			pattern = getIDPattern(csid);
		} catch (IllegalArgumentException e ) {
      pattern = new IDPattern(csid);
      pattern.add(new YearIDPart());
      pattern.add(new StringIDPart("."));
      pattern.add(new NumericIDPart("1"));
      pattern.add(new StringIDPart("."));
      pattern.add(new NumericIDPart("1"));
      storeIDPattern(csid, pattern);
    }

  }

  //////////////////////////////////////////////////////////////////////
  /*
   * Returns the next ID associated with a specified ID pattern.
   *
   * Sets the current ID of that ID pattern to the just-generated ID.
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
   */
	public String nextID(String csid) throws
		IllegalArgumentException, IllegalStateException {
		
		IDResource.verbose("> in nextID");

		IDPattern pattern;
		String nextId = "";
		String lastId = "";
		
		// @TODO: Remove this call to init (a temporary expedient) when possible.
		// Even as is, this should be a one-time utility function, not called
		// each time nextID is invoked.
		init();

		if (csid == null || csid.equals("")) {
			throw new IllegalArgumentException(
				"Identifier for ID pattern must not be null or empty.");
		}

		try {
			pattern = getIDPattern(csid);
		} catch (IllegalArgumentException e ) {
			throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
		
		// Guard code - should not be needed.
		if (pattern == null) {
			throw new IllegalArgumentException(
				"Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
		}
		
		try {

      // Retrieve the last ID associated with this pattern from persistent storage.
      lastId = getLastID(csid);

		  IDResource.verbose("> in nextID");

		  IDResource.verbose("> retrieved last ID: " + lastId);
 
 		  // If there was no last generated ID associated with this pattern,
 		  // get the current ID generated by the pattern as the 'next' ID.
		  if (lastId == null || lastId.equals("")) {
		    nextId = pattern.getCurrentID();

		    IDResource.verbose("> getting current ID");

      // Otherwise, generate the 'next' ID for this pattern, based on the last ID.
      // (This also sets the current ID for the pattern to this just-generated 'next' ID.)
		  } else {
		    IDResource.verbose("> generating next ID");
        nextId = pattern.nextID(lastId);
      }
      
		  // Store the 'next' ID as the last-generated ID for this pattern.
		  IDResource.verbose("> storing last-generated ID: " + nextId);
		  storeLastID(csid, nextId);
		  storeIDPattern(csid, pattern);
		  
		} catch (IllegalArgumentException e ) {
		  throw e;
		} catch (IllegalStateException e ) {
			throw e;
		}
		
		return nextId;

	}
	
  //////////////////////////////////////////////////////////////////////
  /*
   * Returns an ID pattern, given an identifier that uniquely identifies that pattern.
   *
   * @param  csid  An identifier for an ID pattern.
   *
   * @return  The requested ID pattern.
   *
   * @throws  IllegalArgumentException if the requested ID pattern could not be found.
   *
   * @TODO: Replace hard-coded IDs and related logic - currently
   * used here for bootstrapping and initial testing - with actual
   * IDPattern identifiers - CSIDs, and URIs or whatever other
   * identifier type we may be using alongside CSIDs - and logic
   * that dynamically retrieves previously-stored patterns.
   *
   * @TODO: Retrieve IDPatterns from the database using JDBC,
   * rather than hard-coding their construction here.
   */

/*	public IDPattern getIDPattern(String csid) throws IllegalArgumentException {
	
	  IDPattern pattern;
   		
   	// Pattern that returns SPECTRUM Entry numbers
   	// (e.g. "Ennnn").
    if (csid.equals("1")) {
    	
    	// Retrieve the pattern.  (In this example, we're
    	// simply hard-coding its construction here.)
  		pattern = new IDPattern();
			pattern.add(new StringIDPart("E"));
			pattern.add(new NumericIDPart("1"));
			
			return pattern;
    	
		// Pattern that returns SPECTRUM Accession numbers, with item numbers
		// (e.g. "YYYY.nnnn.nnnn").
    } else if (csid.equals("2")) {

    	// Retrieve the pattern.(In this example, we're
    	// simply hard-coding its construction here.)
  		pattern = new IDPattern();
			pattern.add(new YearIDPart());
			pattern.add(new StringIDPart("."));
			pattern.add(new NumericIDPart("1"));
			pattern.add(new StringIDPart("."));
			pattern.add(new NumericIDPart("1"));
			
			return pattern;

		} else {
			throw new IllegalArgumentException(
				"Pattern with ID " + "\'" + csid + "\'" + " could not be found.");
		}
		
	}
*/

  //////////////////////////////////////////////////////////////////////
  /*
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
   */
	public String getLastID(String csid) throws IllegalArgumentException, IllegalStateException {

		IDResource.verbose("> in getLastID");

    String lastId = null;
    Connection conn = null;
    
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

    try {
          
      // @TODO: Get these values from configuration; better yet, substitute Hibernate
      // for JDBC for accessing database-managed persistence.
      //
      // @TODO: Remove any hard-coded dependencies on MySQL.
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

		  IDResource.verbose("> retrieved ID: " + lastId);

			rs.close();

    } catch (SQLException e) {
      System.err.println("Exception: " + e.getMessage());
      throw new IllegalStateException("Error retrieving last ID from database: " + e.getMessage());
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch(SQLException e) {
        // Do nothing here
      }
    }

		IDResource.verbose("> returning ID: " + lastId);
    
    return lastId;

  }

  //////////////////////////////////////////////////////////////////////
  /*
   * Returns a requested ID pattern from persistent storage.  This pattern will
   * include values for the last-generated IDs of each of its constituent parts,
   * and thus can be used to generate a current or 'next' ID for that pattern.
   *
   * @param  csid  An identifier for an ID pattern.
   *
   * @return  The requested ID pattern.
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
	public IDPattern getIDPattern(String csid) throws IllegalArgumentException, IllegalStateException {

		IDResource.verbose("> in getIDPattern");

    String serializedIDPattern = null;
    Connection conn = null;
    
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

    try {
          
      // @TODO: Get these values from configuration; better yet, substitute Hibernate
      // for JDBC for accessing database-managed persistence.
      //
      // @TODO: Remove any hard-coded dependencies on MySQL.
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

			serializedIDPattern = rs.getString(1);
			
			rs.close();

    } catch (SQLException e) {
      System.err.println("Exception: " + e.getMessage());
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
    
      
	  IDResource.verbose("> retrieved IDPattern: " + serializedIDPattern);

    IDPattern pattern = deserializeIDPattern(serializedIDPattern);
    
    return pattern;

  }

  //////////////////////////////////////////////////////////////////////
  /*
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
   */
	public void storeLastID(String csid, String lastId)
	  throws IllegalArgumentException, IllegalStateException {
    
		IDResource.verbose("> in storeLastID");

    Connection conn = null;

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
    
    try {
    
      // @TODO: Get these values from configuration; better yet, substitute Hibernate
      // for JDBC for accessing database-managed persistence.
      //
      // @TODO: Remove any hard-coded dependencies on MySQL.
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();
			
			int rowsUpdated = stmt.executeUpdate(
			  "UPDATE id_patterns SET last_generated_id='" + lastId + "' WHERE id_pattern_csid='" + csid + "'");
			  
			if (rowsUpdated != 1) {
        throw new IllegalStateException(
          "Error updating last-generated ID in database for ID Pattern '" + csid + "'");
      }

		  IDResource.verbose("> successfully stored ID: " + lastId);

    } catch (SQLException e) {
      System.err.println("Exception: " + e.getMessage());
      throw new IllegalStateException("Error updating last-generated ID in database: " + e.getMessage());
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
  /*
   * Stores a serialized ID pattern, representing the state of that pattern,
   * into persistent storage.
   *
   * @param  csid     An identifier for an ID pattern.
   *
   * @param  pattern  An ID Pattern, reflecting its current state, including the
   * values of its constituent parts.
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
	public void storeIDPattern(String csid, IDPattern pattern)
	  throws IllegalArgumentException, IllegalStateException {
    
		IDResource.verbose("> in storeIDPattern");

    Connection conn = null;
    
    String serializedIDPattern = serializeIDPattern(pattern);

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
    
    try {
    
      // @TODO: Get these values from configuration; better yet, substitute Hibernate
      // for JDBC for accessing database-managed persistence.
      //
      // @TODO: Remove any hard-coded dependencies on MySQL.
      conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);

      Statement stmt = conn.createStatement();

      // Test whether the ID Pattern record already exists in the database.
			ResultSet rs = stmt.executeQuery(
			  "SELECT id_pattern_csid FROM id_patterns WHERE id_pattern_csid='" + csid + "'");
			  
			boolean moreRows = rs.next();
			
			boolean idPatternFound = true;
			if (! moreRows) {
        idPatternFound = false;
      }
			
			// If the ID Pattern already exists in the database, update its record;
			// otherwise, add it as a new record.
			if (idPatternFound) {

			  String stmtStr =
			    "UPDATE id_patterns SET id_pattern_state = ? WHERE id_pattern_csid = ?";
			
        PreparedStatement ps = conn.prepareStatement(stmtStr);
        ps.setString(1, serializedIDPattern);
        ps.setString(2, csid);
        
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error adding new ID pattern '" + csid + "'" + " to the database.");
        }
        
/*			
			  String stmtStr =
			    "UPDATE id_patterns SET id_pattern_state='" + serializedIDPattern + "'" +
			    " WHERE id_pattern_csid='" + csid + "'";
		    IDResource.verbose("> update statement: " + stmtStr);
			
        int rowsUpdated = stmt.executeUpdate(stmtStr);
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error updating state of ID pattern '" + csid + "'" + " in the database.");
        }
*/

      } else {
 
        String stmtStr =
			    "INSERT INTO id_patterns (id_pattern_csid, id_pattern_state) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(stmtStr);
        ps.setString(1, csid);
        ps.setString(2, serializedIDPattern);
        
        int rowsUpdated = ps.executeUpdate();
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error adding new ID pattern '" + csid + "'" + " to the database.");
        }
        
/*
 			  String stmtStr =
			    "INSERT INTO id_patterns (id_pattern_csid, id_pattern_state) " +
          "VALUES (" + csid + "," + serializedIDPattern + ")";
		    IDResource.verbose("> insert statement: " + stmtStr);
          
        int rowsUpdated = stmt.executeUpdate(stmtStr);
        if (rowsUpdated != 1) {
          throw new IllegalStateException(
            "Error adding new ID pattern '" + csid + "'" + " to the database.");
        }
*/

       } // end if (idPatternFound)

		  IDResource.verbose("> successfully stored ID Pattern: " + csid);

    } catch (SQLException e) {
      System.err.println("Exception: " + e.getMessage());
      throw new IllegalStateException("Error updating last-generated ID in database: " + e.getMessage());
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
  /*
   * Serializes an ID Pattern.
   *
   * @param  pattern  An ID pattern.
   *
   * @return  A serialized representation of that ID pattern.
   *
   * @throws  IllegalStateException if a storage-related error occurred.
   */
	public static String serializeIDPattern(IDPattern pattern) {
  
    // @TODO: Add Exception handling
    XStream xstream = new XStream(new DomDriver()); 
    String serializedIDPattern = xstream.toXML(pattern);
    
    return serializedIDPattern;
  
  }

  //////////////////////////////////////////////////////////////////////
  /*
   * Deerializes an ID Pattern.
   *
   * @param   serializedIDPattern  A serialized representation of an ID pattern.
   *
   * @return  The ID pattern as a Java Object.
   */
	public static IDPattern deserializeIDPattern(String serializedIDPattern) {

    // @TODO: Add Exception handling
    XStream xstream = new XStream(new DomDriver()); 
    IDPattern pattern = (IDPattern) xstream.fromXML(serializedIDPattern);

    return pattern;
  
  }
  
}
