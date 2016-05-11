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
package org.collectionspace.services.common.init;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AddIndices, post-init action to add indexes to the database.
 * 
 * In the configuration file, you may have sets of table names and column names, or you may use params to send a 
 * comma-separated list of column names and a table name.  That is, both of these are equivalent:
 * 
 *   The single column per element version:
 *
 *
 *        &lt;s:initHandler xmlns:s='http://collectionspace.org/services/common/service'>
                &lt;s:classname>org.collectionspace.services.common.init.AddIndices&lt;/s:classname>
                &lt;s:params>
                    &lt;s:field>
                        &lt;s:table>nuxeo.organizations_common&lt;/s:table>
                        &lt;s:col>inAuthority&lt;/s:col>
                    &lt;/s:field>
                    &lt;s:field>
                        &lt;s:table>nuxeo.organizations_common&lt;/s:table>
                        &lt;s:col>displayName&lt;/s:col>
                    &lt;/s:field>
                    &lt;s:field>
                        &lt;s:table>nuxeo.organizations_common&lt;/s:table>
                        &lt;s:col>shortIdentifier&lt;/s:col>
                    &lt;/s:field>
                &lt;/s:params>
            &lt;/s:initHandler>

     The csv version:

            &lt;s:initHandler xmlns:s='http://collectionspace.org/services/common/service'>
                &lt;s:classname>org.collectionspace.services.common.init.AddIndices&lt;/s:classname>
                &lt;s:params>
                    &lt;s:field>
                        &lt;s:table>nuxeo.organizations_common&lt;/s:table>
                        &lt;s:param>inAuthority,displayName,shortIdentifier&lt;/s:param>
                    &lt;/s:field>
                &lt;/s:params>
            &lt;/s:initHandler>
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class AddIndices extends InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(AddIndices.class);
    private final static String INDEX_SEP = "_";
    private final static String INDEX_SUFFIX = INDEX_SEP + "idx";
    private final static String SHORT_ID = AuthorityClient.SHORT_IDENTIFIER.toLowerCase();
    private final static String IN_AUTHORITY = AuthorityClient.IN_AUTHORITY.toLowerCase();
    
    private final static String AUTHORITY_TYPE = ServiceBindingUtils.SERVICE_TYPE_AUTHORITY;
    private final static String VOCABULARY_TYPE = ServiceBindingUtils.SERVICE_TYPE_VOCABULARY;


    /** See the class javadoc for this class: it shows the syntax supported in the configuration params.
     */
    @Override
    public void onRepositoryInitialized(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> properties) throws Exception {
        //todo: all post-init tasks for services, or delegate to services that override.
        int rows = 0;
        String sql = "";
        if (logger.isInfoEnabled() && sbt != null) {
            logger.info("Creating indicies, as needed, for designated fields in " + sbt.getName()
                    + " for repository domain " + sbt.getRepositoryDomain().trim() + "...");
        }

        for (Field field : fields) {
            String tableName = field.getTable();
            String fieldName = field.getCol();
            String param = field.getParam();
            if(Tools.notEmpty(param) && (param.indexOf(',')>-1)){
                String[] fieldNames = param.split(",");
                for (String fn: fieldNames){
                    rows = addOneIndex(dataSourceName, repositoryName, cspaceInstanceId, tableName, fn);
                }
            } else {
                rows = addOneIndex(dataSourceName, repositoryName, cspaceInstanceId, tableName, fieldName);
            }
        }
        //
        // Add a uniqueness constraint on the short ID field of authority and authority item tables
        //
        if (sbt != null && sbt.isRequiresUniqueShortId()) {
        	ensureShortIdConstraintOnAuthority(dataSourceName,
            		repositoryName,
            		cspaceInstanceId,
            		sbt);
        }
    }
    
    /**
     * Checks to see if the uniqueness constraint already exists on this table.
     * 
     * @param dataSourceName
     * @param repositoryName
     * @param cspaceInstanceId
     * @param tableName
     * @return
     * @throws Exception
     */
    private boolean shortIdConstraintExists(ServiceBindingType sbt,
    		String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		String tableName) throws Exception {
    	boolean result = false;

    	//
		// e.g., SELECT constraint_name FROM information_schema.constraint_column_usage WHERE table_name = 'persons_common' AND constraint_name = 'persons_shortid_unique';
    	//
        String sql;
    	DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType(dataSourceName, repositoryName);
        if (databaseProductType == DatabaseProductType.POSTGRESQL) {
        	String constraintName = String.format("%s_%s_unique", tableName, SHORT_ID);
        	sql = String.format("SELECT constraint_name FROM information_schema.constraint_column_usage WHERE table_name = '%s' AND constraint_name = '%s'",
        			tableName, constraintName);
        } else {
            String errorMsg = String.format("Database server type '%s' is not supported by CollectionSpace.  Could not create constraint on column '%s' of table '%s'.",
            		databaseProductType.getName(), SHORT_ID, tableName);
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;        
        try {
            conn = JDBCTools.getConnection(dataSourceName, repositoryName, cspaceInstanceId);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
            	result = true;
            }
        } catch (Exception e) {
        	String errorMsg = e.getLocalizedMessage();
            logger.error(String.format("Error when identifying whether constraint on column '%s' exists in table '%s': %s",
            		SHORT_ID, tableName, e != null ? e : "Unknown error."));
            throw e; // rethrow it.
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.error("SQL Exception closing statement/connection in AddIndices: " + sqle.getLocalizedMessage());
            }
        }
    	
    	return result;
    }
    
    private boolean createShortIdConstraint(ServiceBindingType sbt,
    		String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		String tableName) {
    	boolean result = false;

        String errorMsg = null;
    	try {
	        String sql;
	    	DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType(dataSourceName, repositoryName);
	        if (databaseProductType == DatabaseProductType.POSTGRESQL) {
	        	String constraintName = String.format("%s_%s_unique", tableName, SHORT_ID);
	        	//
	        	// Check if we've got a parent or an item.  Parents need the constraint on the short id, but items
	        	// have the constraint on the combined shortidentifier and inauthority (parent's CSID) columns
	        	//
	        	String serviceType = sbt.getType();
	        	if (serviceType.equalsIgnoreCase(AUTHORITY_TYPE) || serviceType.equalsIgnoreCase(VOCABULARY_TYPE)) { 
		        	sql = String.format("ALTER TABLE %s add CONSTRAINT %s UNIQUE (%s, %s)",		// constraint for an item
		        			tableName, constraintName, SHORT_ID, IN_AUTHORITY);
	        	} else {
		        	sql = String.format("ALTER TABLE %s add CONSTRAINT %s UNIQUE (%s)",			// constraint for a parent
		        			tableName, constraintName, SHORT_ID);     		
	        	}
	        } else {
	            errorMsg = String.format("Database server type '%s' is not supported by CollectionSpace.  Could not create constraint on column '%s' of table '%s'.",
	            		databaseProductType.getName(), SHORT_ID, tableName);
	            throw new Exception(errorMsg);
	        }
	
	    	//
			// e.g., ALTER TABLE persons_common add CONSTRAINT persons_shortid_unique UNIQUE (shortidentifier);
	    	//
	        
	        try {
	        	int rowsCreated = JDBCTools.executeUpdate(dataSourceName, repositoryName, cspaceInstanceId, sql); // This should return '0' since ALTER statements don't return row counts
	        	if (rowsCreated != 0) {
	        		throw new Exception(String.format("No rows created on SQL update: %s", sql));
	        	} else {
	        		result = true;
	        	}
	        } catch (SQLException sqle) {
	        	 String errorState = sqle.getSQLState();
	        	 if (errorState != null && errorState.equals(JDBCTools.POSTGRES_UNIQUE_VIOLATION)) {
	        		 errorMsg = String.format("*** WARNING *** - The value of the '%s' column in the '%s' table of the '%s' repository should be unique, but is not!  Therefore, "
	         		 		+ "we cannot create the NECESSARY database constraint.  Please remove the duplicate '%s' value(s) and restart CollectionSpace.",
	         		 		SHORT_ID, tableName, repositoryName, SHORT_ID);
	        	 } else {
	        		 errorMsg = String.format("Unexpected %s error=%s : %s", databaseProductType.getName(), errorState, sqle.getLocalizedMessage());
	        	 }
	        } catch (Exception e) {
	        	errorMsg = e.getLocalizedMessage();
	        }
    	} catch (Exception e) {
        	errorMsg = e.getLocalizedMessage();
    	}
        //
    	// If we failed to create the constraint, log the reason.
    	//
        if (result == false) {
        	if (errorMsg != null) {
        		logger.error(errorMsg);
        	}
            logger.error(String.format("*** ERROR *** Encountered problems when trying to create a uniqueness constraint on column '%s' of table '%s' in repository '%s'.", 
            		SHORT_ID, tableName, repositoryName));
        } else {
        	Log.debug(String.format("Created a uniqueness constraint on column '%s' of table '%s' in repository '%s'.", 
            		SHORT_ID, tableName, repositoryName));
        }
    	
    	return result;
    }    
    
    /**
     * 
     * Ensure a database level uniqueness constraint exists on the "shortIdentifier" column of this service's common part table.
     * 
     * @param dataSourceName
     * @param repositoryName
     * @param cspaceInstanceId
     * @param sbt
     * @throws Exception 
     */
    private void ensureShortIdConstraintOnAuthority(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		ServiceBindingType sbt) {
    	String tableName = null;
    	String errMessage = null;
    	
        try {
	    	//
	    	// Find the common part table name for this service.  It's the one with the short ID column
	    	//
	        List<ObjectPartType> objectPartTypes = sbt.getObject().getPart();
	        for (ObjectPartType objectPartType : objectPartTypes) {
	        	if (objectPartType.getId().equalsIgnoreCase(ServiceBindingUtils.SERVICE_COMMONPART_ID) == true) {
	        		tableName = objectPartType.getLabel();
	        		break;
	        	}
	        }
	        
	        //
	        // Get an error message ready in case we hit trouble.
	        //
	        errMessage = String.format("*** IMPORTANT *** - Encountered problems trying to ensure a uniqueness constraint exists for the '%s' column of table '%s' in repository '%s'.  Check the CollectionSpace services logs for details.",
	    			SHORT_ID, tableName, repositoryName);
	        //
	        // If the constraint doesn't exist, create it.
	        //
	        if (shortIdConstraintExists(sbt, dataSourceName, repositoryName, cspaceInstanceId, tableName) == false) {
	        	if (createShortIdConstraint(sbt, dataSourceName, repositoryName, cspaceInstanceId, tableName) == true) {
	        		logger.info(String.format("Created uniqueness constraint on '%s' column of table '%s' in repository '%s'.",
			    			SHORT_ID, tableName, repositoryName));	        	
	        	} else {
	        		logger.error(errMessage);
	        	}
	        } else {
        		logger.debug(String.format("Uniqueness constraint already exists on '%s' column of table '%s' in repository '%s'.",
		    			SHORT_ID, tableName, repositoryName));	        	
	        }
        } catch (Exception e) {
        	logger.error(errMessage);
        }
    }

	private int addOneIndex(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		String tableName, 
    		String columnName) {
        int rows = 0;
        String sql = "";
        String indexName = tableName + INDEX_SEP + columnName + INDEX_SUFFIX;
        try {
        	DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType(dataSourceName, repositoryName);
            if (indexExists(dataSourceName, repositoryName, cspaceInstanceId, databaseProductType,
            		tableName, columnName, indexName)) {
                logger.trace("Index already exists for column " + columnName
                        + " in table " + tableName);
                // FIXME: Can add the option to drop and re-create an index here.
                // For example, see MySQL documentation on DROP INDEX.
                return 0;
            }
            // TODO: Consider refactoring this 'if' statement to a general-purpose
            // mechanism for retrieving and populating catalog/DDL-type SQL statements
            // appropriate to a particular database product.
            logger.info("Creating index for column " + columnName + " in table " + tableName);
            if (databaseProductType == DatabaseProductType.MYSQL
            		|| databaseProductType == DatabaseProductType.POSTGRESQL) {
                 sql = "CREATE INDEX " + indexName + " ON " 
                 		+ tableName + " (" + columnName + ")";
            } else {
                throw new Exception("Unrecognized database system " + databaseProductType);
            }
            if (sql != null && ! sql.trim().isEmpty()) {
                // Assumes indicies will only be created at post-init time
                // for the Nuxeo repository.
                // 
                // To date, for the CSpace repository, indices have typically been
                // created during the build process, via manual or generated
                // DDL SQL scripts.
                //
                // If this assumption is no longer valid, we might instead
                // identify the relevant repository from the table name here.
                rows = JDBCTools.executeUpdate(dataSourceName, repositoryName, cspaceInstanceId, sql);
                logger.trace("Index added to column ("+columnName+") on table ("+tableName+")");
            }
            return rows;
        } catch (Throwable e) {
            logger.debug("Index NOT added to column ("+columnName+") on table ("+tableName+") SQL: "+sql+" ERROR: "+e);
            return -1;
        }
    }

    private boolean indexExists(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		DatabaseProductType databaseProductType,
            String tableName, 
            String colName, 
            String indexName) {
        
        // FIXME: May need to qualify table name by database/catalog,
        // as table names likely will not be globally unique across same
        // (although index names *may* be unique within scope).
        //
        // If we do need to do this, we might:
        // - Pass in the database name as a parameter to this method, retrieved
        //   via getDatabaseName(field) in onRepositoryInitialized, above.
        // - Add 'IN databaseName' after tableName in MySQL variant, below.
        //   (PostgreSQL variant, below, uses a view that doesn't include
        //   a foreign key for associating a database/catalog to the index.)
        
        // FIXME: Consider instead substituting a database-agnostic
        // JDBC mechanism for retrieving indexes; e.g.
        // java.sql.DatabaseMetaData.getIndexInfo()
        
        boolean indexExists = false;
        String sql = "";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        if (databaseProductType == DatabaseProductType.MYSQL) {
            sql = "SHOW INDEX FROM " + tableName + " WHERE key_name='" + indexName + "'";
        } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
        	// We want to see if any index on that column exists, not just ours...
            sql = "SELECT indexname FROM pg_catalog.pg_indexes "
                    + "WHERE tablename = '" + tableName 
                    + "' AND indexdef ILIKE '%("+colName+")'";
        }

        try {
            // Assumes indices will only be created at post-init time
            // for the Nuxeo repository.
            // 
            // To date, for the CSpace repository, indices have typically been
            // created during the build process, via manual or generated
            // DDL SQL scripts.
            //
            // If this assumption is no longer valid, we might instead
            // identify the relevant repository from the table name here.
            conn = JDBCTools.getConnection(dataSourceName, repositoryName, cspaceInstanceId);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                indexExists = true;
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            logger.debug("Error when identifying whether index exists in table "
                    + tableName + ":" + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in AddIndices: " + sqle.getLocalizedMessage());
            }
        }
        return indexExists;
    }

}
