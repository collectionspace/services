/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.storage;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class JDBCTools {
	public static HashMap<String, DataSource> cachedDataSources = new HashMap<String, DataSource>();
    public static String CSPACE_REPOSITORY_NAME = "CspaceDS";
    public static String NUXEO_REPOSITORY_NAME = "NuxeoDS";
    //
    // Private constants
    //
    private static String DEFAULT_REPOSITORY_NAME = NUXEO_REPOSITORY_NAME;
    private static String DBProductName = null;
    private static DatabaseProductType DBProductType = DatabaseProductType.UNRECOGNIZED;

    //todo: make sure this will get instantiated in the right order
    final static Logger logger = LoggerFactory.getLogger(JDBCTools.class);
        
    public static DataSource getDataSource(String repositoryName) throws NamingException {
    	DataSource result = null;
    	
    	//
    	// First, see if we already have this DataSource instance cached
    	//
    	result = cachedDataSources.get(repositoryName);
    	if (result == null) {    	
        	InitialContext ctx = new InitialContext();
        	Context envCtx = null;

        	if (logger.isDebugEnabled() == true) {
	        	logger.debug("Looking up DataSource instance in JNDI with name: " + repositoryName);
	        }
	            	
	    	try {
		        envCtx = (Context) ctx.lookup("java:comp/env");
		        DataSource ds = (DataSource) envCtx.lookup("jdbc/" + repositoryName);
		        if (ds == null) {
		            throw new IllegalArgumentException("DataSource instance not found: " + repositoryName);
		        } else {
		        	result = ds;
		        	// now cache this DataSource instance for future references
		        	cachedDataSources.put(repositoryName, result);
		        }
	    	} finally {
	            if (ctx != null) {
	                try {
	                    ctx.close();
	                } catch (Exception e) {
	                	logger.error("Error getting DataSource for: " + repositoryName, e);
	                }
	            }
	            if (envCtx != null) {
	                try {
	                	envCtx.close();
	                } catch (Exception e) {
	                	logger.error("Error getting DataSource for: " + repositoryName, e);
	                }
	            }
	    	}
    	}
    	
    	return result;
    }
    
    /*
     * This is a wrapper around DataSource's getConnectionMethod -mainly exists modularize all connection related code to JDBCTool class.
     */
    public static Connection getConnection(DataSource dataSource) throws SQLException {
    	Connection result = null;
    	result = dataSource.getConnection();
    	return result;
    }

    public static Connection getConnection(String repositoryName) throws NamingException, SQLException {
    	Connection result = null;
    	
    	if (Tools.isEmpty(repositoryName)) {
            repositoryName = getDefaultRepositoryName();
            if (logger.isWarnEnabled() == true) {
            	logger.warn("getConnection() method was called with an empty or null repository name.  Using " + repositoryName + " instead.");
            }
        }
        
        DataSource ds = getDataSource(repositoryName);
        Connection conn = getConnection(ds);
        result = conn;
        
        return result;
    }

    /* THIS IS BROKEN - If you close the statement, it closes the ResultSet!!!
    public static ResultSet executeQuery(String repoName, String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(repoName);	// If null, uses default
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            stmt.close();
            return rs;  //don't call rs.close() here ... Let caller close and catch any exceptions.
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            throw new RuntimeException("SQL problem in executeQuery: ", sqle);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in executeQuery: " + sqle.getLocalizedMessage());
                return null;
            }
        }
    } */

    public static int executeUpdate(DataSource dataSource, String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(dataSource);
            stmt = conn.createStatement();
            int rows = stmt.executeUpdate(sql);
            stmt.close();
            return rows;
        } catch (RuntimeException rte) {
            logger.debug("Exception in executeUpdate: " + rte.getLocalizedMessage());
            logger.debug(rte.getStackTrace().toString());
            throw rte;
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            String msg = "";
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                if (!msg.isEmpty()) {
                    msg = msg + "::next::";
                }
                msg = msg + sqle.getLocalizedMessage();
                logger.debug("SQL Exception: " + msg);
                tempException = tempException.getNextException();
            }
            throw new RuntimeException("SQL problem in executeUpdate: " + msg, sqle);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in executeUpdate: " + sqle.getLocalizedMessage());
                return -1;
            }
        }
    }

    /**
     * Returns the database product name, from the metadata for a
     * JDBC connection to the default repository.
     * 
     * Assumes that the database product name will be the same for the
     * default repository and for all other repositories to which JDBC
     * connections will be made, through the methods of this class.
     * 
     * @return the database product name
     */
    public static String getDatabaseProductName() {
    	if(DBProductName==null) {
	        Connection conn = null;
	        try {
	            conn = getConnection(getDefaultRepositoryName()); //FIXME: REM - getDefaultRepositoryName returns the Nuxeo repo name -we should be using the "cspace" repo name
	            DBProductName = conn.getMetaData().getDatabaseProductName();
	        } catch (Exception e) {
	        } finally {
	            try {
	                if (conn != null) {
	                    conn.close();
	                }
	            } catch (SQLException sqle) {
	                logger.debug("SQL Exception closing statement/connection in getDatabaseProductName: "
	                		+ sqle.getLocalizedMessage());
	            }
	        }
    	}
        return DBProductName;
    }

    /**
     * Returns an enumerated value uniquely identifying the database product type;
     * e.g. MySQL, PostgreSQL, based on the database product name.
     * 
     * @return an enumerated value identifying the database product type
     * @throws Exception 
     */
    public static DatabaseProductType getDatabaseProductType() throws Exception {
    	if(DBProductType == DatabaseProductType.UNRECOGNIZED) {
	        String productName = getDatabaseProductName();
	        if (productName.matches("(?i).*mysql.*")) {
	        	DBProductType = DatabaseProductType.MYSQL;
	        } else if (productName.matches("(?i).*postgresql.*")) {
	        	DBProductType = DatabaseProductType.POSTGRESQL;
	        } else {
	            throw new Exception("Unrecognized database system " 
	            					+ productName);
	        }
    	}
        return DBProductType;
    }

    private static String getDefaultRepositoryName() {
        return DEFAULT_REPOSITORY_NAME;
    }

    /**
     * Prints metadata, such as database username and connection URL,
     * for an open JDBC connection.  This is a utility method for use
     * during debugging.
     * 
     * @param conn an open JDBC Connection
     * @throws SQLException 
     */
    private static void printConnectionMetaData(Connection conn) throws SQLException {
        if (conn != null) {
            DatabaseMetaData metadata = conn.getMetaData();
            // FIXME: Outputs via System.out, rather than Logger, for
            // cases where this may be called during server startup.
            System.out.println("username=" + metadata.getUserName());
            System.out.println("database url=" + metadata.getURL());
        }
    }
		
}
