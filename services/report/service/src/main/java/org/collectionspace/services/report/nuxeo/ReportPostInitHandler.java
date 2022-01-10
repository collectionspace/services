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
package org.collectionspace.services.report.nuxeo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.init.IInitHandler;
import org.collectionspace.services.common.init.InitHandler;
import org.collectionspace.services.common.storage.DatabaseProductType;
import org.collectionspace.services.common.storage.JDBCTools;

import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ServiceBindingType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReportPostInitHandler, post-init action to add grant reader access to DB
 * 
 * In the configuration file, looks for a single Field declaration 
 * with a param value that has the name of the reader account/role.
 * If not specified, it will assume 'reader'; 
 * 
 * $LastChangedRevision: 5103 $
 * $LastChangedDate: 2011-06-23 16:50:06 -0700 (Thu, 23 Jun 2011) $
 */
public class ReportPostInitHandler extends InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(ReportPostInitHandler.class);
   
    public static final String READER_ROLE_NAME_KEY = "readerRoleName";
    public static final String DEFAULT_READER_ROLE_NAME = "reader" + ServiceMain.getInstance().getCspaceInstanceId();
    private String readerRoleName = DEFAULT_READER_ROLE_NAME;
    
    /** See the class javadoc for this class: it shows the syntax supported in the configuration params.
     */
    @Override
    public void onRepositoryInitialized(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		String tenantShortName,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> propertyList) throws Exception {
        //Check for existing privileges, and if not there, grant them
    	for(Property prop : propertyList) {
                if(READER_ROLE_NAME_KEY.equals(prop.getKey())) {
                    String value = prop.getValue();
                    if(Tools.notEmpty(value) && !DEFAULT_READER_ROLE_NAME.equals(value)){
                        readerRoleName = value + ServiceMain.getInstance().getCspaceInstanceId();
                        logger.debug("ReportPostInitHandler: overriding readerRoleName default value to use: "
                                + value);
                }
            }
        }
        String privilegeName = JDBCTools.DATABASE_SELECT_PRIVILEGE_NAME;
        JDBCTools.grantPrivilegeToDatabaseUser(dataSourceName, repositoryName, cspaceInstanceId, privilegeName, readerRoleName);
    }
    

}
