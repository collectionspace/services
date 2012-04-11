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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.common.init;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ServiceBindingType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ModifyFieldDatatypes, post-init action to configure the database
 * datatypes of individual fields.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class ModifyFieldDatatypes extends InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(ModifyFieldDatatypes.class);

    @Override
    public void onRepositoryInitialized(DataSource dataSource,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> properties) throws Exception {
        //todo: all post-init tasks for services, or delegate to services that override.
        int rows = 0;
        String sql = "";
        if (logger.isInfoEnabled()) {
            logger.info("Modifying field datatypes for " + sbt.getName()
                    + " for repository domain " + sbt.getRepositoryDomain().trim() + "...");
        }
        try {
            DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType();
            String datatype = "";
            for (Field field : fields) {
                datatype = getDatatypeFromLogicalType(databaseProductType, field.getType());
                // If the field is already of the desired datatype, skip it.
                if (fieldHasDesiredDatatype(dataSource, databaseProductType, field, datatype)) {
                    logger.trace("Field " + field.getTable() + "." + field.getCol()
                            + " is already of desired datatype " + datatype);
                    continue;
                }
                // TODO: Consider refactoring this nested 'if' statement to a general-purpose
                // mechanism for retrieving and populating catalog/DDL-type SQL statements
                // appropriate to a particular database product.
                if (databaseProductType == DatabaseProductType.MYSQL) {
                    logger.info("Modifying field " + field.getTable() + "."
                            + field.getCol() + " to datatype " + datatype);
                    sql = "ALTER TABLE " + field.getTable() + " MODIFY COLUMN " 
                    		+ field.getCol() + " " + datatype;
                } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
                    logger.info("Modifying field " + field.getTable() + "."
                            + field.getCol() + " to datatype " + datatype);
                    sql = "ALTER TABLE " + field.getTable() + " ALTER COLUMN " 
                    		+ field.getCol() + " TYPE " + datatype;
                } else {
                    throw new Exception("Unrecognized database system.");
                }
                // Assumes field datatypes will only be modified at post-init
                // time for the Nuxeo repository.
                // 
                // To date, for the CSpace repository, field datatypes have
                // typically been specified during the build process, via
                // manual or generated DDL SQL scripts.
                //
                // If this assumption is no longer valid, we might instead
                // identify the relevant repository from the table name here.
                rows = JDBCTools.executeUpdate(dataSource, sql);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private String getDatatypeFromLogicalType(DatabaseProductType databaseProductType, String logicalDatatype) throws Exception {
        final Map<DatabaseProductType,Map<String,String>> DATABASE_DATATYPES =
                new HashMap<DatabaseProductType,Map<String,String>>();
        // Define the logical datatypes and database-specific datatypes associated
        // with each database system.
        final String LARGE_TEXT_DATATYPE = "LARGETEXT";
        final String[] DATATYPES = { LARGE_TEXT_DATATYPE };
        // final String DATE_DATATYPE = "DATE";
        // final String[] DATATYPES = { LARGE_TEXT_DATATYPE, DATE_DATATYPE };
        final DatabaseProductType[] DATABASE_PRODUCT_TYPES =
            { DatabaseProductType.POSTGRESQL, DatabaseProductType.MYSQL };
        Map<String,String> postgresqlDatatypeMap = new HashMap<String,String>();
        postgresqlDatatypeMap.put("LARGETEXT", "text");
        // postgresqlDatatypeMap.put("DATE", "date");
        DATABASE_DATATYPES.put(DatabaseProductType.POSTGRESQL, postgresqlDatatypeMap);
        Map<String,String> mysqlDatatypeMap = new HashMap<String,String>();
        mysqlDatatypeMap.put("LARGETEXT", "TEXT");
        // mysqlDatatypeMap.put("DATE", "DATE");
        DATABASE_DATATYPES.put(DatabaseProductType.MYSQL, mysqlDatatypeMap);

        String datatype = "";
        if (!Arrays.asList(DATABASE_PRODUCT_TYPES).contains(databaseProductType)) {
            throw new Exception("Unrecognized database system " + databaseProductType);
        }
        if (!Arrays.asList(DATATYPES).contains(logicalDatatype.toUpperCase())) {
            throw new Exception("Unrecognized logical datatype " + logicalDatatype);
        }
        datatype = DATABASE_DATATYPES.get(databaseProductType).get(logicalDatatype.toUpperCase());
        if (datatype == null) {
            throw new Exception("Unrecognized logical datatype " + logicalDatatype
                    + " for database system " + databaseProductType);
        }
        return datatype;
    }

    private boolean fieldHasDesiredDatatype(DataSource dataSource,
    		DatabaseProductType databaseProductType,
            Field field, String datatype) {
        
        // FIXME: Consider instead using the database-agnostic
        // JDBC DatabaseMetaData class to extract metadata, as per
        // http://www.java2s.com/Code/Java/Database-SQL-JDBC/GetColumnType.htm

        boolean fieldHasDesiredDatatype = false;
        int rows = 0;
        String sql = "";
        String currentDatatype = "";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = JDBCTools.getConnection(dataSource);
            stmt = conn.createStatement();
            if (databaseProductType == DatabaseProductType.MYSQL) {
                sql = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS "
                    + "WHERE TABLE_SCHEMA = '" + JDBCTools.getDatabaseName(conn) + "'"
                    + " AND TABLE_NAME = '" + getTableName(field) + "'"
                    + " AND COLUMN_NAME = '" + field.getCol() + "'";
            } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
                sql = "SELECT data_type FROM information_schema.columns "
                    + "WHERE table_catalog = '" + JDBCTools.getDatabaseName(conn) + "'"
                    + " AND table_name = '" + getTableName(field) + "'"
                    + " AND column_name = '" + field.getCol() + "'";
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                currentDatatype = rs.getString(1);
            }
            if (datatype.equalsIgnoreCase(currentDatatype)) {
                fieldHasDesiredDatatype = true;
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            logger.debug("Error when identifying datatype of field " + field.getCol()
                    + " in table " + field.getTable() + ":" + e.getMessage());
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
                logger.debug("SQL Exception closing statement/connection in ModifyFieldDatatypes: " + sqle.getLocalizedMessage());
            }
        }
        return fieldHasDesiredDatatype;
    }

    // FIXME: Hacks to separate the '.'-delimited database/schema name
    // from the table name in field.getTable, where that value is
    // specified in tenant bindings configuration as 'database.table'.
    //
    // Even as the current hack, this logic should reside in Field, not here.

    private String getDatabaseName(Field field) {
        String databaseName = "";
        String[] databaseAndTableNames = field.getTable().split("\\.", 2);
        if (! databaseAndTableNames[0].isEmpty()) {
            databaseName = databaseAndTableNames[0];
        } else {
            databaseName = field.getTable();
        }
        return databaseName;
    }

    private String getTableName(Field field) {
        String tableName = "";
        String[] databaseAndTableNames = field.getTable().split("\\.", 2);
        if (databaseAndTableNames.length>1 && !databaseAndTableNames[1].isEmpty()) {
            tableName = databaseAndTableNames[1];
        } else {
            tableName = field.getTable();
        }
        return tableName;
    }
}
