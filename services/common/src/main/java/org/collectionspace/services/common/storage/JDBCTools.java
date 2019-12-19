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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class JDBCTools {
    public static HashMap<String, DataSource> cachedDataSources = new HashMap<String, DataSource>();
    public static String CSPACE_DATASOURCE_NAME = "CspaceDS";
    public static String NUXEO_DATASOURCE_NAME = "NuxeoDS_CS"; // Starting with v4.2 release we renamed this from NuxeoDS to NuxeoDS_CS to void namespace conflict with Nuxeo EP
    // Default database names
    // public static String DEFAULT_CSPACE_DATABASE_NAME = ConfigUtils.DEFAULT_CSPACE_DATABASE_NAME;
    public static String DEFAULT_NUXEO_REPOSITORY_NAME = ConfigUtils.DEFAULT_NUXEO_REPOSITORY_NAME;
    public static String DEFAULT_NUXEO_DATABASE_NAME = ConfigUtils.DEFAULT_NUXEO_DATABASE_NAME;
    public static String CSADMIN_DATASOURCE_NAME = "CsadminDS";
    public static String NUXEO_READER_DATASOURCE_NAME = "NuxeoReaderDS";
    public static String NUXEO_USER_NAME = "nuxeo";
    public static String SQL_WILDCARD = "%";
    public static String DATABASE_SELECT_PRIVILEGE_NAME = "SELECT";
    public static String POSTGRES_UNIQUE_VIOLATION = "23505";

    //
    // Private constants
    //
    private static String DBProductName = null;

    //todo: make sure this will get instantiated in the right order
    final static Logger logger = LoggerFactory.getLogger(JDBCTools.class);
	private static final CharSequence URL_DATABASE_NAME = "${DatabaseName}";
    private static String JDBC_URL_DATABASE_SEPARATOR = "\\/";
        
	//
	// As a side-effect of calling JDBCTools.getDataSource(...), the DataSource instance will be
	// cached in a static hash map of the JDBCTools class.  This will speed up lookups as well as protect our
	// code from JNDI lookup problems -for example, if the JNDI context gets stepped on or corrupted.
	//    
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
	    	} finally { // We should explicitly close both context instances
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
    
    //
    // Use this version of the getConnection() method when you don't want to qualify the database name
    // with a CollectionSpace instance ID.
    //
    public static Connection getConnection(String dataSourceName,
    		String databaseName) throws NamingException, SQLException {
    	return getConnection(dataSourceName, databaseName, null);
    }
    
    public static Connection getConnection(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId) throws NamingException, SQLException {
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
    		BasicDataSource dataSource = (BasicDataSource)getDataSource(dataSourceName);
    		// Get the template URL value from the JNDI datasource and substitute the databaseName
	        String urlTemplate = dataSource.getUrl();
	        String databaseName = getDatabaseName(repositoryName, cspaceInstanceId);
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

    public static CachedRowSet executeQuery(String dataSourceName, String repositoryName, String cspaceInstanceId, String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
            stmt = conn.createStatement();
             
            RowSetFactory rowSetFactory = RowSetProvider.newFactory();
            CachedRowSet crs = rowSetFactory.createCachedRowSet();

            stmt = conn.createStatement();
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                crs.populate(resultSet);
            }
            return crs;
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            throw new RuntimeException("SQL Exception in executeQuery: ", sqle);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in executeQuery: " + sqle.getLocalizedMessage());
                return null;
            }
        }
    }
    
    public static CachedRowSet executePreparedQuery(final PreparedStatementBuilder builder,
            String dataSourceName, String repositoryName, String cspaceInstanceId) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
            RowSetFactory rowSetFactory = RowSetProvider.newFactory();
            CachedRowSet crs = rowSetFactory.createCachedRowSet();
            ps = builder.build(conn);
            // FIXME: transition this log statement to DEBUG level when appropriate
            if (logger.isInfoEnabled()) {
                logger.info("prepared statement=" + ps.toString());
            }
            try (ResultSet resultSet = ps.executeQuery()) {
                crs.populate(resultSet);
            }
            return crs;
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            throw new RuntimeException("SQL Exception in executePreparedQuery: ", sqle);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in executePreparedQuery: " + sqle.getLocalizedMessage());
                return null;
            }
        }
    }
    
    // FIXME: This method's code significantly overlaps that of executePrepareQuery(), above,
    // and the two could be refactored into a single method, if desired.
    public static List<CachedRowSet> executePreparedQueries(final List<PreparedStatementBuilder> builders,
            String dataSourceName, String repositoryName, String cspaceInstanceId, Boolean executeWithinTransaction) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        List<CachedRowSet> results = new ArrayList<>();
        try {
            conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
            if (executeWithinTransaction) {
                conn.setAutoCommit(false);
            }
            RowSetFactory rowSetFactory = RowSetProvider.newFactory();
            CachedRowSet crs = rowSetFactory.createCachedRowSet();
            int statementCount = 0;
            for (PreparedStatementBuilder builder : builders) {
                ps = builder.build(conn);
                // FIXME: transition this log statement to DEBUG level when appropriate
                if (logger.isInfoEnabled()) {
                    statementCount++;
                    logger.info("prepared statement " + statementCount + "=" + ps.toString());
                }
                // Try executing each statement, first as a query, then as an update
                try {
                    ResultSet resultSet = ps.executeQuery();
                    if (resultSet != null) {
                        crs.populate(resultSet);
                        results.add(crs);
                    }
                } catch (Exception e) {
                    int rowcount = ps.executeUpdate();
                	logger.debug(String.format("Row count for builder %s is %d", ps.toString(), rowcount));
                    // Throw uncaught exception here if update attempt also fails
                }
            }
            return results;
        } catch (SQLException sqle) {
            if (executeWithinTransaction && conn != null) {
                conn.rollback();
            }
            SQLException tempException = sqle;
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            throw new RuntimeException("SQL Exception in executePreparedQueries: ", sqle);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    if (executeWithinTransaction) {
                        conn.commit();
                    }
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in executePreparedQueries: " + sqle.getLocalizedMessage());
                return null;
            }
        }
    }

    public static int executeUpdate(String dataSourceName,
    		String repositoryName, 
    		String cspaceInstanceId, 
    		String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
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
            while (tempException != null) {       // SQLExceptions can be chained. Loop to log all.
                if (!msg.isEmpty()) {
                    msg = msg + "::next::";
                }
                msg = msg + sqle.getLocalizedMessage();
                logger.debug("SQL Exception: " + msg);
                tempException = tempException.getNextException();
            }
            throw sqle; // rethrow the exception
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
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
    		String repositoryName,
    		String cspaceInstanceId) throws Exception {
    	
    	@SuppressWarnings("unused")
		Object driver = Class.forName("org.postgresql.Driver"); // For some reason, we need to make sure the org.postgresql.Driver class in on the classpath
    	
    	if (DBProductName == null) {
	        Connection conn = null;
	        try {
	            conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
	            DBProductName = conn.getMetaData().getDatabaseProductName();
	        } catch (Exception e) {
	        	if (logger.isTraceEnabled() == true) {
	        		logger.trace(String.format("Could not open a connection. DataSource='%s' DB='%s'.",
	        				dataSourceName, repositoryName));
	                throw e;
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
    		String repositoryName,
    		String cspaceInstanceId) throws Exception {
    	DatabaseProductType result = DatabaseProductType.UNRECOGNIZED;
    	
        String productName = getDatabaseProductName(dataSourceName, repositoryName, cspaceInstanceId);
        if (productName.matches("(?i).*mysql.*")) {
        	result = DatabaseProductType.MYSQL;
        } else if (productName.matches("(?i).*postgresql.*")) {
        	result = DatabaseProductType.POSTGRESQL;
        } else {
            throw new Exception("Unrecognized database system " + productName);
        }
    	
        return result;
    }
    
    //
    // Same as method above except the cspace instance ID is not needed.
    //
    public static DatabaseProductType getDatabaseProductType(String dataSourceName,
    		String repositoryName) throws Exception {
    	DatabaseProductType result = DatabaseProductType.UNRECOGNIZED;
    	
        String productName = getDatabaseProductName(dataSourceName, repositoryName, null);
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
    public static String getDatabaseName(String repoName, String cspaceInstanceId) {
    	String result = repoName;
    	
    	//
    	// Insert code here if you want to map the repo name to a database name -otherwise
    	// we'll assume they are the same thing.
    	//
    	if (repoName.equalsIgnoreCase(DEFAULT_NUXEO_REPOSITORY_NAME)) {
    		result = DEFAULT_NUXEO_DATABASE_NAME;
    	}
    	
    	//
    	// If we have a non-null 'cspaceInstanceId' instance ID then we need to append it
    	// as a suffix to the database name.
    	//
    	if (cspaceInstanceId != null && !cspaceInstanceId.trim().isEmpty()) {
    		if (result.endsWith(cspaceInstanceId) == false) { // ensure we don't already have the suffix
    			result = result + cspaceInstanceId;
    		}
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
    		String cspaceInstanceId,
    		Connection conn) throws Exception {
        String databaseName = null;
        
        if (conn != null) {
	        DatabaseMetaData metadata = conn.getMetaData();
	        String urlStr = metadata.getURL();
	        
	        // Format of the PostgreSQL JDBC URL:
	        // http://jdbc.postgresql.org/documentation/80/connect.html
	        if (getDatabaseProductType(dataSourceName, repositoryName, cspaceInstanceId) == DatabaseProductType.POSTGRESQL) {
	            String tokens[] = urlStr.split(JDBC_URL_DATABASE_SEPARATOR);
	            databaseName = tokens[tokens.length - 1];
	            // Format of the MySQL JDBC URL:
	            // http://dev.mysql.com/doc/refman/5.1/en/connector-j-reference-configuration-properties.html
	            // FIXME: the last token could contain optional parameters, not accounted for here.
	        } else if (getDatabaseProductType(dataSourceName, repositoryName, cspaceInstanceId) == DatabaseProductType.MYSQL) {
	            String tokens[] = urlStr.split(JDBC_URL_DATABASE_SEPARATOR);
	            databaseName = tokens[tokens.length - 1];
	        }
        }
        
        return databaseName;
    }
    
    /**
     * Grant a specified privilege to a database user. This privilege will
     * be applied to all 'public' schema tables within the specified repository.
     * 
     * @param dataSourceName a JDBC datasource name.
     * @param repositoryName a repository (e.g. RDBMS database) name.
     * @param cspaceInstanceId a CollectionSpace instance identifier.
     * @param privilegeName a database privilege (e.g. SELECT) to be granted.
     * @param databaseUserName a database user to receive the privilege grant.
     */
    public static void grantPrivilegeToDatabaseUser(String dataSourceName, String repositoryName,
             String cspaceInstanceId, String privilegeName, String databaseUserName) {
        Statement stmt = null;
        Connection conn = null;
        String sql = String.format("GRANT %s ON ALL TABLES IN SCHEMA public TO %s", privilegeName, databaseUserName);
        try {
            DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType(dataSourceName, repositoryName,
            		cspaceInstanceId);
            if (databaseProductType == DatabaseProductType.MYSQL) {
                // Nothing to do here: MYSQL already does wildcard grants in init_db.sql
            } else if(databaseProductType != DatabaseProductType.POSTGRESQL) {
                throw new Exception("Unrecognized database system " + databaseProductType);
            } else {
                String databaseName = JDBCTools.getDatabaseName(repositoryName, cspaceInstanceId);
                // Verify that the database user exists before executing the grant
                if (hasDatabaseUser(dataSourceName, repositoryName, cspaceInstanceId,
                        databaseProductType, databaseUserName)) {
                    conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
                    stmt = conn.createStatement();                
                    stmt.execute(sql);
                }
            }
            
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            // SQLExceptions can be chained. Loop to log all.
            while (null != tempException) {
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            logger.debug("SQL problem in executeQuery: ", sqle);
        } catch (Throwable e) {
            logger.debug(String.format("Problem granting privileges to database user: %s SQL: %s ERROR: %s",
                    databaseUserName, sql, e));
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                 if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                // nothing we can do here except log
                logger.warn("SQL Exception when closing statement/connection: " + sqle.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Create a database user, if that user doesn't already exist.
     * 
     * @param conn a database connection.
     * @param dbType a database product type.
     * @param username the name of the database user to create.
     * @param userPW the initial password for that database user.
     */
    public static void createNewDatabaseUser(String dataSourceName, String repositoryName,
             String cspaceInstanceId, DatabaseProductType dbType, String username, String userPW) throws Exception {
        Statement stmt = null;
        Connection conn = null;
        String sql = null;
        if (dbType != DatabaseProductType.POSTGRESQL) {
            throw new UnsupportedOperationException("createNewDatabaseUser only supports PostgreSQL");
        }
        try {
            if (hasDatabaseUser(dataSourceName, repositoryName, cspaceInstanceId, dbType, username)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("User: " + username + " already exists.");
                }
            } else {
                conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
                stmt = conn.createStatement();
                sql = "CREATE ROLE " + username + " WITH PASSWORD '" + userPW + "' LOGIN";
                stmt.executeUpdate(sql);
                if (logger.isDebugEnabled()) {
                    logger.debug("Created User: " + username);
                }
            }
        } catch (Exception e) {
            logger.error("createNewDatabaseUser failed on exception: " + e.getLocalizedMessage());
            logger.error(String.format("The following SQL statement failed using credentials from datasource named '%s': '%s'", dataSourceName, sql));
            throw e;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                // nothing we can do here except log
                logger.warn("SQL Exception when closing statement/connection: " + sqle.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Identify whether a database exists.
     * 
     * @param dbType a database product type.
     * @param dbName a database product name.
     * @return true if a database with that name exists, false if that database does not exit.
     * @throws Exception
     */
    public static boolean hasDatabase(DatabaseProductType dbType, String dbName) throws Exception {
        PreparedStatement pstmt = null;
        Connection conn = null;
        String dbExistsQuery = "";
        if (dbType == DatabaseProductType.POSTGRESQL) {
           dbExistsQuery = "SELECT 1 AS result FROM pg_database WHERE datname=?";
        } else if (dbType == DatabaseProductType.MYSQL) {
           dbExistsQuery = "SELECT 1 AS result FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?";
        } else {
            throw new UnsupportedOperationException("hasDatabase encountered unknown database product type");
        }
        try {
            DataSource csadminDataSource = JDBCTools.getDataSource(JDBCTools.CSADMIN_DATASOURCE_NAME);
            conn = csadminDataSource.getConnection();
            pstmt = conn.prepareStatement(dbExistsQuery); // create a statement
            pstmt.setString(1, dbName); // set dbName param
            ResultSet rs = pstmt.executeQuery();
            // extract data from the ResultSet
            boolean dbExists = rs.next();  // Will return a value of 1 if database exists
            rs.close();
            return dbExists;
        } catch (Exception e) {
            logger.error("hasDatabase failed on exception: " + e.getLocalizedMessage());
            throw e;
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                // nothing we can do here except log
                logger.warn("SQL Exception when closing statement/connection: " + sqle.getLocalizedMessage());
           }
        }
    }
    
    /**
     * Identify whether a database user exists.
     * 
     * @param dataSourceName a JDBC datasource name.
     * @param repositoryName a repository (e.g. RDBMS database) name.
     * @param cspaceInstanceId a CollectionSpace instance identifier.
     * @param dbType a database product type.
     * @param username the name of the database user to create.
     * @param userPW the initial password for that database user.
     * @return true if a database user with that name exists, false if that user does not exist.
     * @throws Exception
     */
    public static boolean hasDatabaseUser(String dataSourceName, String repositoryName,
             String cspaceInstanceId, DatabaseProductType dbType, String username) throws Exception {
        PreparedStatement pstmt = null;
        Statement stmt = null;
        Connection conn = null;
        final String USER_EXISTS_QUERY_POSTGRESQL = "SELECT 1 AS result FROM pg_roles WHERE rolname=?";
        if (dbType != DatabaseProductType.POSTGRESQL) {
            throw new UnsupportedOperationException("hasDatabaseUser only supports PostgreSQL");
        }
        try {
            conn = getConnection(dataSourceName, repositoryName, cspaceInstanceId);
            pstmt = conn.prepareStatement(USER_EXISTS_QUERY_POSTGRESQL);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            boolean userExists = rs.next(); // Will return a value of 1 if user exists
            rs.close();
            return userExists;
        } catch (Exception e) {
            logger.error("hasDatabaseUser failed on exception: " + e.getLocalizedMessage());
            throw e;
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                // nothing we can do here except log
                logger.warn("SQL Exception when closing statement/connection: " + sqle.getLocalizedMessage());
           }
        }
    }
    
    
    // -----------------------------
    // Utility methods for debugging
    // -----------------------------

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
    public static void printResultSetMetaData(ResultSet rs) throws SQLException {
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
