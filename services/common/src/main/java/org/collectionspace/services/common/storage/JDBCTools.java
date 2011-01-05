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

package org.collectionspace.services.common.storage;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class JDBCTools {

    //todo: make sure this will get instantiated in the right order
    final static Logger logger = LoggerFactory.getLogger(JDBCTools.class);

    public static Connection getConnection(String repositoryName) throws LoginException, SQLException {
        if (Tools.isEmpty(repositoryName)){
            repositoryName = ServiceMain.DEFAULT_REPOSITORY_NAME;
        }
        InitialContext ctx = null;
        Connection conn = null;
        try {
            ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(repositoryName);
            if (ds == null) {
                throw new IllegalArgumentException("datasource not found: " + repositoryName);
            }
            conn = ds.getConnection();
            return conn;
        } catch (NamingException ex) {
            LoginException le = new LoginException("Error looking up DataSource from: " + repositoryName);
            le.initCause(ex);
            throw le;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static ResultSet openResultSet(String sql) throws Exception {
        Connection conn = null;
    	Statement stmt = null;
        try {
        	conn = JDBCTools.getConnection(ServiceMain.DEFAULT_REPOSITORY_NAME);
        	stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			stmt.close();
            return rs;  //don't call rs.close() here ... Let caller close and catch any exceptions.
        } catch (RuntimeException rte) {
            logger.debug("Exception in createDefaultAccounts: "+rte.getLocalizedMessage());
            logger.debug(rte.getStackTrace().toString());
            throw rte;
        } catch (SQLException sqle) {
            SQLException tempException = sqle;
            while (null != tempException) {       // SQLExceptions can be chained. Loop to log all.
                logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
                tempException = tempException.getNextException();
            }
            logger.debug(sqle.getStackTrace().toString());
            throw new RuntimeException("SQL problem in openResultSet: ", sqle);
        } finally {
        	try {
            	if(conn!=null) conn.close();
            	if(stmt!=null) stmt.close();
            } catch (SQLException sqle) {
                logger.debug("SQL Exception closing statement/connection in openResultSet: "+ sqle.getLocalizedMessage());
                return null;
        	}
        }

    }




}
