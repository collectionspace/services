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

import org.collectionspace.services.common.Tools;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.InitHandler.Params.Field;
import org.collectionspace.services.common.service.InitHandler.Params.Property;
import org.collectionspace.services.common.storage.DatabaseProductType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class AddIndices extends InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(AddIndices.class);
    private final static String INDEX_SUFFIX = "_idx";

    /** See the class javadoc for this class: it shows the syntax supported in the configuration params.
     */
    @Override
    public void onRepositoryInitialized(ServiceBindingType sbt, List<Field> fields, List<Property> properties) throws Exception {
        //todo: all post-init tasks for services, or delegate to services that override.
        int rows = 0;
        String sql = "";
        logger.info("Creating indicies for designated fields in " + sbt.getName()
                + " for repository domain " + sbt.getRepositoryDomain().trim() + "...");

        for (Field field : fields) {
            String tableName = field.getTable();
            String fieldName = field.getCol();
            String param = field.getParam();
            if(Tools.notEmpty(param) && (param.indexOf(',')>-1)){
                String[] fieldNames = param.split(",");
                for (String fn: fieldNames){
                    addOneIndex(tableName, fn);
                }
            } else {
                addOneIndex(tableName, fieldName);
            }
        }
    }

    private int addOneIndex(String tableName, String columnName){
        int rows = 0;
        String sql = "";
        try {
            DatabaseProductType databaseProductType = getDatabaseProductType();
            // TODO: Consider refactoring this 'if' statement to a general-purpose
            // mechanism for retrieving and populating catalog/DDL-type SQL statements
            // appropriate to a particular database product.
            if (databaseProductType == DatabaseProductType.MYSQL) {
                sql = "CREATE INDEX " + columnName + INDEX_SUFFIX + " ON " + tableName + " (" + columnName + ")";
            } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
                sql = "CREATE INDEX ON " + tableName + " (" + columnName + ")";
            } else {
                throw new Exception("Unrecognized database system " + databaseProductType);
            }
            rows = executeUpdate(sql);
            logger.info("Index added to column ("+columnName+") on table ("+tableName+")");
            return rows;
        } catch (Throwable e) {
            logger.info("Index NOT added to column ("+columnName+") on table ("+tableName+") SQL: "+sql+" ERROR: "+e);
            return -1;
        }
    }

}
