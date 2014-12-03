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
    public static final String DATABASE_SELECT_PRIVILEGE_NAME = "SELECT";
   
    // Currently retained for backward compatibility
    public static final String READER_ROLE_NAME_KEY = "readerRoleName";
    public static final String DEFAULT_READER_ROLE_NAME = "reader" + ServiceMain.getInstance().getCspaceInstanceId();
    private String readerRoleName = DEFAULT_READER_ROLE_NAME;
   
    public static final String REPORTER_ROLE_NAME_KEY = "reporterRoleName";
    public static final String DEFAULT_REPORTER_ROLE_NAME = "reporter" + ServiceMain.getInstance().getCspaceInstanceId();
    private String reporterRoleName = DEFAULT_REPORTER_ROLE_NAME;
    
    /** See the class javadoc for this class: it shows the syntax supported in the configuration params.
     */
    @Override
    public void onRepositoryInitialized(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> propertyList) throws Exception {
        //Check for existing privileges, and if not there, grant them
    	for(Property prop : propertyList) {
                if(REPORTER_ROLE_NAME_KEY.equals(prop.getKey())) {
                    String value = prop.getValue();
                    if(Tools.notEmpty(value) && !DEFAULT_REPORTER_ROLE_NAME.equals(value)){
                        reporterRoleName = value + ServiceMain.getInstance().getCspaceInstanceId();
                        logger.debug("ReportPostInitHandler: overriding reporterRoleName default value to use: "
                                + value);
                    }
                }
                // FIXME: Currently retained for backward compatibility; remove this block when appropriate
                if(READER_ROLE_NAME_KEY.equals(prop.getKey())) {
                    String value = prop.getValue();
                    if(Tools.notEmpty(value) && !DEFAULT_READER_ROLE_NAME.equals(value)){
                        readerRoleName = value + ServiceMain.getInstance().getCspaceInstanceId();
                        logger.debug("ReportPostInitHandler: overriding readerRoleName default value to use: "
                                + value);
                }
            }
        }
        String privilegeName = DATABASE_SELECT_PRIVILEGE_NAME;
        grantPrivilegeToDatabaseRole(dataSourceName, repositoryName, cspaceInstanceId, privilegeName, reporterRoleName);
        // FIXME: Currently retained for backward compatibility; remove the following line when appropriate
        grantPrivilegeToDatabaseRole(dataSourceName, repositoryName, cspaceInstanceId, privilegeName, readerRoleName);
    }

    // FIXME: This method might be refactorable / movable to the
    // org.collectionspace.services.common.storage.JDBCTools class.
    // If so, any database privilege constants here should be moved with it.
    private void grantPrivilegeToDatabaseRole(String dataSourceName, String repositoryName, String cspaceInstanceId,
            String privilegeName, String roleName) {
        Connection conn = null;
        Statement stmt = null;
        String sql = "";
        try {
            DatabaseProductType databaseProductType = JDBCTools.getDatabaseProductType(dataSourceName, repositoryName,
            		cspaceInstanceId);
            if (databaseProductType == DatabaseProductType.MYSQL) {
                    // Nothing to do: MYSQL already does wildcard grants in init_db.sql
            } else if(databaseProductType != DatabaseProductType.POSTGRESQL) {
                throw new Exception("Unrecognized database system " + databaseProductType);
            } else {
                String databaseName = JDBCTools.getDatabaseName(repositoryName, cspaceInstanceId);
                conn = JDBCTools.getConnection(dataSourceName, databaseName);
                stmt = conn.createStatement();                
                // FIXME: Check first that role exists before executing the grant
                sql = String.format("GRANT %s ON ALL TABLES IN SCHEMA public TO %s", privilegeName, roleName);
                stmt.execute(sql);
            }
            
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            logger.debug("ReportPostInitHandler: SQL problem in executeQuery: ", sqle);
        } catch (Throwable e) {
            logger.debug("ReportPostInitHandler: problem checking/adding grant for reader: "+readerRoleName+") SQL: "+sql+" ERROR: "+e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in executeQuery: " + sqle.getLocalizedMessage());
            }
        }
    }
    

}
