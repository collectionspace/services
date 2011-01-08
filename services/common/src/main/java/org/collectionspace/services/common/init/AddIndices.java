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
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class AddIndices extends InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(AddIndices.class);
    private final static String INDEX_SUFFIX = "_idx";

    @Override
    public void onRepositoryInitialized(ServiceBindingType sbt, List<Field> fields, List<Property> properties) throws Exception {
        //todo: all post-init tasks for services, or delegate to services that override.
        int rows = 0;
        String sql = "";
        if (logger.isInfoEnabled()) {
            logger.info("Modifying field datatypes for " + sbt.getName()
                    + " for repository domain " + sbt.getRepositoryDomain().trim() + "...");
        }
        DatabaseProductType databaseProductType = getDatabaseProductType();
        for (Field field : fields) {
            try {
                // TODO: Consider refactoring this 'if' statement to a general-purpose
                // mechanism for retrieving and populating catalog/DDL-type SQL statements
                // appropriate to a particular database product.
                if (databaseProductType == DatabaseProductType.MYSQL) {
                    sql = "CREATE INDEX " + field.getCol() + INDEX_SUFFIX + " ON " + field.getTable() + " (" + field.getCol() + ")";
                } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
                    sql = "CREATE INDEX ON " + field.getTable() + " (" + field.getCol() + ")";
                } else {
                    throw new Exception("Unrecognized database system " + databaseProductType);
                }
                rows = executeUpdate(sql);
            } catch (Exception e) {
                throw e;
            }

            //call something like this: services.common.storage.DBUtils.addIndex(String tablename, String fields[]);
            //for every field that has an authRef, do ...
            //    --> Connection conn = getConnection();
            //see parameter that you need for adding indices to SQL.

        }
    }

}
