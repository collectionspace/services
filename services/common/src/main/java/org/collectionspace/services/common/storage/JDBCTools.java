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

import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
    public static String CSPACE_DATASOURCE_NAME = "CspaceDS";
    public static String NUXEO_DATASOURCE_NAME = "NuxeoDS";
    // Default database names
    public static String DEFAULT_CSPACE_DATABASE_NAME = ConfigUtils.DEFAULT_CSPACE_DATABASE_NAME;
    public static String DEFAULT_NUXEO_REPOSITORY_NAME = ConfigUtils.DEFAULT_NUXEO_REPOSITORY_NAME;
    public static String DEFAULT_NUXEO_DATABASE_NAME = ConfigUtils.DEFAULT_NUXEO_DATABASE_NAME;
    public static String NUXEO_MANAGER_DATASOURCE_NAME = "NuxeoMgrDS";
    public static String NUXEO_READER_DATASOURCE_NAME = "NuxeoReaderDS";
    public static String NUXEO_USER_NAME = "nuxeo";
    //
    // Private constants
    //
    private static String DBProductName = null;

    //todo: make sure this will get instantiated in the right order
    final static Logger logger = LoggerFactory.getLogger(JDBCTools.class);
	private static final CharSequence URL_DATABASE_NAME = "${DatabaseName}";
    private static String JDBC_URL_DATABASE_SEPARATOR = "\\/";
        
    public static DataSource getDataSource(String dataSourceName) throws NamingException {
    	DataSource result = null;
    	
    	//
    	// First, see if we already have this DataSource instance cached
    	//
    	result = cachedDataSources.get(dataSourceName);
    	if (result == null) {    	
        	InitialContext ctx = new InitialContext();
        	Context envCtx = null;

        	if (logger.isDebugEnabled() == true) {
	        	logger.debug("Looking up DataSource instance in JNDI with name: " + dataSourceName);
	        }
	            	
	    	try {
		        envCtx = (Context) ctx.lookup("java:comp/env");
		        DataSource ds = (DataSource) envCtx.lookup("jdbc/" + dataSourceName);
		        if (ds == null) {
		            throw new IllegalArgumentException("DataSource instance not found: " + dataSourceName);
		        } else {
		        	result = ds;
		        	// now cache this DataSource instance for future references
		        	cachedDataSources.put(dataSourceName, result);
		        }
	    	} finally {
	            if (ctx != null) {
	                try {
	                    ctx.close();
	                } catch (Exception e) {
	                	logger.error("Error getting DataSource for: " + dataSourceName, e);
	                }
	            }
	            if (envCtx != null) {
	                try {
	                	envCtx.close();
	                } catch (Exception e) {
	                	logger.error("Error getting DataSource for: " + dataSourceName, e);
	                }
	            }
	    	}
    	}
    	
    	if (result != null) {
//    		DataSource resultClone = result.
    	}
    	
    	return result;
    }
    
    public static Connection getConnection(String dataSourceName, String repositoryName) throws NamingException, SQLException {
    	Connection result = null;
    	
    	if (Tools.isEmpty(dataSourceName) || Tools.isEmpty(repositoryName)) {
    		String errMsg = String.format(
    				"The getConnection() method was called with an empty or null repository name = '%s' and/or data source name = '%s'.", 
    				dataSourceName, repositoryName);
            logger.error(errMsg);
            throw new NamingException(errMsg);
        }
    	        
    	/*
    	 * We synch this block as a workaround to not have separate DataSource instances for
    	 * each Nuxeo repo/DB.  Ideally, we should replace the need for this synch block by
    	 * registering a separate DataSource for each repo/db at init/start-up time.
    	 * 
    	 * We need to sync because we're changing the URL of the datasource inorder to get the correct
    	 * connection.  The synch prevents different threads from getting the incorrect connection -i.e., one pointing
    	 * to the wrong URL.
    	 */
    	Connection conn = null;
    	synchronized (JDBCTools.class) {
    		org.apache.tomcat.dbcp.dbcp.BasicDataSource dataSource = 
    				(org.apache.tomcat.dbcp.dbcp.BasicDataSource)getDataSource(dataSourceName);
    		// Get the template URL value from the JNDI datasource and substitute the databaseName
	        String urlTemplate = dataSource.getUrl();
	        String databaseName = getDatabaseName(repositoryName);
	        String connectionUrl = urlTemplate.replace(URL_DATABASE_NAME, databaseName);

	        // ATTENTION!
	        // Turns out the Tomcat BasicDataSource used a connection pool, so changing the url does not
	        // get you a corresponding connection. Use the more basic implementation for now, unless
	        // and until we do things right by creating additional JNDI data sources.
	        
        	//dataSource.setUrl(connectionUrl);
	        String user = dataSource.getUsername();
	        String password = dataSource.getPassword();
	        
	        try {
	        	//conn = dataSource.getConnection();
	        	conn = DriverManager.getConnection(connectionUrl, user, password);
	        	result = conn;
	        	if (logger.isTraceEnabled() == true && conn != null) {
	        		logger.trace(String.format("Connection made to repository = '%s' using datasource = '%s'", repositoryName, dataSourceName));
	        	}
	        } finally {
	        	dataSource.setUrl(urlTemplate); // Reset the data source URL value back to the template value
	        }
    	}
    	        
        return result;
    }
    
    // Regarding the method below, we might instead identify whether we can
    // return a CachedRowSet or equivalent.
    // http://docs.oracle.com/javase/1.5.0/docs/api/javax/sql/rowset/CachedRowSet.html
    // -- ADR 2012-12-06

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

    public static int executeUpdate(String dataSourceName, String repositoryName, String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(dataSourceName, repositoryName);
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
    public static String getDatabaseProductName(String dataSourceName,
    		String repositoryName) {
    	if (DBProductName == null) {
	        Connection conn = null;
	        try {
	            conn = getConnection(dataSourceName, repositoryName);
	            DBProductName = conn.getMetaData().getDatabaseProductName();
	        } catch (Exception e) {
	        	if (logger.isTraceEnabled() == true) {
	        		logger.trace(String.format("Could not open a connection. DataSource='%s' DB='%s'.",
	        				dataSourceName, repositoryName));
	        	}
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
    public static DatabaseProductType getDatabaseProductType(String dataSourceName,
    		String repositoryName) throws Exception {
    	DatabaseProductType result = DatabaseProductType.UNRECOGNIZED;
    	
        String productName = getDatabaseProductName(dataSourceName, repositoryName);
        if (productName.matches("(?i).*mysql.*")) {
        	result = DatabaseProductType.MYSQL;
        } else if (productName.matches("(?i).*postgresql.*")) {
        	result = DatabaseProductType.POSTGRESQL;
        } else {
            throw new Exception("Unrecognized database system " + productName);
        }
    	
        return result;
    }
    
    /*
     * By convention, the repository name and database name are the same.  However, this
     * call encapulates that convention and allows overrides.
     */
    public static String getDatabaseName(String repoName) {
    	String result = repoName;
    	
    	if (result.equalsIgnoreCase(DEFAULT_NUXEO_REPOSITORY_NAME) == true) {
    		result = DEFAULT_NUXEO_DATABASE_NAME;
    	}
    	
    	return result;
    }
    
    /**
     * Returns the catalog/database name for an open JDBC connection.
     * 
     * @param conn an open JDBC Connection
     * @return the catalog name.
     * @throws SQLException 
     */
    public static String getDatabaseName(String dataSourceName,
    		String repositoryName,
    		Connection conn) throws Exception {
        String databaseName = null;
        
        if (conn != null) {
	        DatabaseMetaData metadata = conn.getMetaData();
	        String urlStr = metadata.getURL();
	        
	        // Format of the PostgreSQL JDBC URL:
	        // http://jdbc.postgresql.org/documentation/80/connect.html
	        if (getDatabaseProductType(dataSourceName, repositoryName) == DatabaseProductType.POSTGRESQL) {
	            String tokens[] = urlStr.split(JDBC_URL_DATABASE_SEPARATOR);
	            databaseName = tokens[tokens.length - 1];
	            // Format of the MySQL JDBC URL:
	            // http://dev.mysql.com/doc/refman/5.1/en/connector-j-reference-configuration-properties.html
	            // FIXME: the last token could contain optional parameters, not accounted for here.
	        } else if (getDatabaseProductType(dataSourceName, repositoryName) == DatabaseProductType.MYSQL) {
	            String tokens[] = urlStr.split(JDBC_URL_DATABASE_SEPARATOR);
	            databaseName = tokens[tokens.length - 1];
	        }
        }
        
        return databaseName;
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
    
    /**
     * Prints metadata related to a JDBC ResultSet, such as column names.
     * This is a utility method for use during debugging.
     * 
     * @param rs a ResultSet.
     * @throws SQLException 
     */
    public void printResultSetMetaData(ResultSet rs) throws SQLException {
        if (rs == null) {
            return;
        }
        ResultSetMetaData metadata = rs.getMetaData();
        if (metadata == null) {
            return;
        }
        int numberOfColumns = metadata.getColumnCount();
        for (int i = 1; i <= numberOfColumns; i++) {
            logger.debug(metadata.getColumnName(i));
            // Insert other debug statements to retrieve additional per-column metadata here ...
        }
    }
		
}
