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
 * $LastChangedRevision: $
 * $LastChangedDate: $
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
                // TODO: Consider refactoring this 'if' statement to a general-purpose
                // mechanism for retrieving and populating catalog/DDL-type SQL statements
                // appropriate to a particular database product.
                if (databaseProductType == DatabaseProductType.MYSQL) {
                    sql = "ALTER TABLE " + field.getTable() + " MODIFY COLUMN " + field.getCol() + " " + datatype;
                } else if (databaseProductType == DatabaseProductType.POSTGRESQL) {
                    sql = "ALTER TABLE " + field.getTable() + " ALTER COLUMN " + field.getCol() + " " + datatype;
                } else {
                    throw new Exception("Unrecognized database system.");
                }
                rows = JDBCTools.executeUpdate(sql);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // TODO: Refactor this method to a general-purpose mechanism for retrieving
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
}
