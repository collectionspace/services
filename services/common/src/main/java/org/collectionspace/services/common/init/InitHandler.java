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

import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.InitHandler.Params.Field;
import org.collectionspace.services.common.service.InitHandler.Params.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

/** Concrete class which does nothing, but subclasses may override to do
 *  some action on the event onRepositoryInitialized(), such as sending JDBC
 *  calls to the repository to add indices, etc.
 * @author Laramie
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(InitHandler.class);

    /**
     * Callback procedure for performing post-initialization actions.
     *
     * See org.collectionspace.services.common.init.AddIndices for an implementation example.
     *
     * @param sbt a service binding type.
     * @param fields A list of fields and their attributes.
     * @param properties A properties bag for additional properties.
     * @throws Exception
     */
    @Override
    public void onRepositoryInitialized(DataSource dataSource,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> properties) throws Exception {

        // For debugging.
        /*
        for (Field field : fields) {
            System.out.println("InitHandler.fields:"
                    + "\r\n    col: " + field.getCol()
                    + "   table: " + field.getTable()
                    + "   type: " + field.getType()
                    + "   param: " + field.getParam());
        }
        for (Property prop : properties) {
            System.out.println("InitHandler.properties:"
                    + "\r\n    key: " + prop.getKey()
                    + "   value: " + prop.getValue());

        }
         *
         */
    }

}
