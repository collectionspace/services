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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class JDBCTools {
    public static String CSPACE_REPOSITORY_NAME = "CspaceDS";
    public static String NUXEO_REPOSITORY_NAME = "NuxeoDS";
    public static String DEFAULT_REPOSITORY_NAME = NUXEO_REPOSITORY_NAME;
    private static String DBProductName = null;
    private static DatabaseProductType DBProductType = DatabaseProductType.UNRECOGNIZED;

    //todo: make sure this will get instantiated in the right order
    final static Logger logger = LoggerFactory.getLogger(JDBCTools.class);

    public static Connection getConnection(String repositoryName) throws LoginException, SQLException {
        if (Tools.isEmpty(repositoryName)) {
            repositoryName = getDefaultRepositoryName();
        }
        InitialContext ctx = null;
        Connection conn = null;
        try {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(repositoryName);
            if (ds == null) {
                throw new IllegalArgumentException("datasource not found: " + repositoryName);
            }
            conn = ds.getConnection();
            return conn;
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + repositoryName);
            le.initCause(ex);
            throw le;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Do nothing with exception in 'finally' clause.
                }
            }
        }
    }


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
    }

    public static int executeUpdate(String repoName, String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(repoName);	// If null, uses default
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
	            conn = getConnection(getDefaultRepositoryName());
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

    public static String getDefaultRepositoryName() {
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
