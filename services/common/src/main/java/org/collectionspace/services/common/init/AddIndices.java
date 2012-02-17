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

import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.InitHandler.Params.Field;
import org.collectionspace.services.common.service.InitHandler.Params.Property;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;

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
    private final static String INDEX_SUFFIX = "_idx";
    private final static String INDEX_SEP = "_";

    /** See the class javadoc for this class: it shows the syntax supported in the configuration params.
     */
    @Override
    public void onRepositoryInitialized(DataSource dataSource,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> properties) throws Exception {
        //todo: all post-init tasks for services, or delegate to services that override.
        int rows = 0;
        String sql = "";
        logger.info("Creating indicies, as needed, for designated fields in " + sbt.getName()
                + " for repository domain " + sbt.getRepositoryDomain().trim() + "...");

        for (Field field : fields) {
            String tableName = field.getTable();
            String fieldName = field.getCol();
            String param = field.getParam();
            if(Tools.notEmpty(param) && (param.indexOf(',')>-1)){
                String[] fieldNames = param.split(",");
                for (String fn: fieldNames){
                    rows = addOneIndex(dataSource, tableName, fn);
                }
            } else {
                rows = addOneIndex(dataSource, tableName, fieldName);
            }
        }
    }

    private int addOneIndex(DataSource dataSource, String tableName, String columnName){
        int rows = 0;
        String sql = "";
        String indexName = tableName + INDEX_SEP + columnName + INDEX_SUFFIX;
        try {
            DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType();
            if (indexExists(dataSource, databaseProductType, tableName, columnName, indexName)) {
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
                rows = JDBCTools.executeUpdate(dataSource, sql);
                logger.trace("Index added to column ("+columnName+") on table ("+tableName+")");
            }
            return rows;
        } catch (Throwable e) {
            logger.debug("Index NOT added to column ("+columnName+") on table ("+tableName+") SQL: "+sql+" ERROR: "+e);
            return -1;
        }
    }

    private boolean indexExists(DataSource dataSource,
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
            conn = JDBCTools.getConnection(dataSource);
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
