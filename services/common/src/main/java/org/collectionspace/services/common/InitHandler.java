package org.collectionspace.services.common;

import com.sun.media.jai.util.DataBufferUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.IInitHandler;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.service.ServiceBindingType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class InitHandler implements IInitHandler {

    public void onRepositoryInitialized(ServiceBindingType sbt, List<String> fields) throws Exception {
          //todo: all post-init tasks for services, or delegate to services that override.
          System.out.println("\r\n\r\n~~~~~~~~~~~~~ in onRepositoryInitialized with ServiceBindingType: "+sbt);

        //call something like this: services.common.storage.DBUtils.addIndex(String tablename, String fields[]);
        //for every field that has an authRef, do ...
        //    --> Connection conn = getConnection();
        //see parameter that you need for adding indices to SQL.

    }
     /*
    private void doJDBC(){
        Connection conn = null;
        PreparedStatement pstmt = null;
    	Statement stmt = null;
        // First find or create the tenants
        try {
            String queryTenantSQL = "UPDATE TABLE ADD KEY `tablename`.`id`...";
        	conn = getConnection();
        	// First find or create the tenants
        	stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(queryTenantSQL);
	        ArrayList<String> existingTenants = new ArrayList<String>();
			while (rs.next()) {
				String tId = rs.getString("id");
				String tName = rs.getString("name");
				if(tenantInfo.containsKey(tId)) {
					existingTenants.add(tId);
					if(!tenantInfo.get(tId).equalsIgnoreCase(tName)) {
						logger.warn("Configured name for tenant: "
								+tId+" in repository: "+tName
								+" does not match config'd name: "+ tenantInfo.get(tId));
					}
				}
			}
			rs.close();
        	pstmt.close();
			stmt.close();
        } catch (RuntimeException rte) {
        	if (logger.isDebugEnabled()) {
        		logger.debug("Exception in createDefaultAccounts: "+
						rte.getLocalizedMessage());
        		logger.debug(rte.getStackTrace().toString());
        	}
            throw rte;
        } catch (SQLException sqle) {
            // SQLExceptions can be chained. We have at least one exception, so
            // set up a loop to make sure we let the user know about all of them
            // if there happens to be more than one.
        	if (logger.isDebugEnabled()) {
        		SQLException tempException = sqle;
        		while (null != tempException) {
        			logger.debug("SQL Exception: " + sqle.getLocalizedMessage());
        			tempException = tempException.getNextException();
        		}
        		logger.debug(sqle.getStackTrace().toString());
        	}
            throw new RuntimeException("SQL problem in createDefaultAccounts: ", sqle);
        } catch (Exception e) {
        	if (logger.isDebugEnabled()) {
        		logger.debug("Exception in createDefaultAccounts: "+
						e.getLocalizedMessage());
        	}
        } finally {
        	try {
            	if(conn!=null)
                    conn.close();
            	if(pstmt!=null)
                    pstmt.close();
            	if(stmt!=null)
                    stmt.close();
            } catch (SQLException sqle) {
            	if (logger.isDebugEnabled()) {
        			logger.debug("SQL Exception closing statement/connection: "
        					+ sqle.getLocalizedMessage());
            	}
        	}
        }
    }
    */

}
