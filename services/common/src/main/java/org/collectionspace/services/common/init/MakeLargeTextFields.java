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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MakeLargeTextFields, post-init action to configure text fields
 * that must hold large amounts of text.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class MakeLargeTextFields extends InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(MakeLargeTextFields.class);

    public void onRepositoryInitialized(ServiceBindingType sbt, List<Field> fields, List<Property> properties) throws Exception {
          //todo: all post-init tasks for services, or delegate to services that override.
          System.out.println("\r\n\r\n~~~~~~~~~~~~~ in MakeLargeTextFields.onRepositoryInitialized with ServiceBindingType: "+sbt);

        String tableName = "nuxeo.collectionobjects_common_comments";
        // String columnName = "item";
        String columnDataType = "TEXT";

        int rows = 0;
        try {
            for (Field field : fields) {
                // MySQL
                String sql = "ALTER TABLE " + field.getTable() + " MODIFY COLUMN " + field.getCol() + " " + field.getType();
                // PostgreSQL
                // String sql = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " TYPE " + columnDataType;
                rows = executeUpdate(sql);
            }
        } catch (Exception e){
            throw e;
        }
        //call something like this: services.common.storage.DBUtils.addIndex(String tablename, String fields[]);
        //for every field that has an authRef, do ...
        //    --> Connection conn = getConnection();
        //see parameter that you need for adding indices to SQL.

    }
}
