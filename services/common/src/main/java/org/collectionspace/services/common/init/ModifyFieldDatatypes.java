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
import java.util.List;

import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.InitHandler.Params.Field;
import org.collectionspace.services.common.service.InitHandler.Params.Property;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;

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
    public void onRepositoryInitialized(ServiceBindingType sbt, List<Field> fields, List<Property> properties) throws Exception {
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
                if (fieldHasDesiredDatatype(databaseProductType, field, datatype)) {
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
                rows = JDBCTools.executeUpdate(JDBCTools.NUXEO_REPOSITORY_NAME, sql);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // TODO: Consider refactoring this method to a general-purpose mechanism for retrieving
    // datatypes appropriate to a particular database product, that corresponds
    // to logical datatypes such as "LARGETEXT".
    //
    // Currently, this is hard-coded to a single logical datatype.
    private String getDatatypeFromLogicalType(DatabaseProductType databaseProductType, String logicalDatatype) throws Exception {
        final String LARGE_TEXT_DATATYPE = "LARGETEXT";
        String datatype = "";
        if (!logicalDatatype.equalsIgnoreCase(LARGE_TEXT_DATATYPE)) {
            throw new Exception("Unrecognized logical datatype " + logicalDatatype);
        }
        if (databaseProductType == DatabaseProductType.MYSQL) {
            datatype = "TEXT";
        } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
            datatype = "TEXT";
        } else {
            throw new Exception("Unrecognized database system " + databaseProductType);
        }
        return datatype;
    }

    private boolean fieldHasDesiredDatatype(DatabaseProductType databaseProductType,
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

        if (databaseProductType == DatabaseProductType.MYSQL) {
            sql = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS "
                    + "WHERE TABLE_SCHEMA = '" + getDatabaseName(field) + "'"
                    + " AND TABLE_NAME = '" + getTableName(field) + "'"
                    + " AND COLUMN_NAME = '" + field.getCol() + "'";
        } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
            sql = "SELECT data_type FROM information_schema.columns "
                    + "WHERE table_catalog = '" + getDatabaseName(field) + "'"
                    + " AND table_name = '" + getTableName(field) + "'"
                    + " AND column_name = '" + field.getCol() + "'";
        }

        try {
            conn = JDBCTools.getConnection(JDBCTools.NUXEO_REPOSITORY_NAME);
            stmt = conn.createStatement();
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
        if (! databaseAndTableNames[1].isEmpty()) {
            tableName = databaseAndTableNames[1];
        } else {
            tableName = field.getTable();
        }
        return tableName;
    }
}
