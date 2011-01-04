package org.collectionspace.services.common.init;

import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class InitHandler implements IInitHandler {

    final Logger logger = LoggerFactory.getLogger(InitHandler.class);

    public void onRepositoryInitialized(ServiceBindingType sbt, List<String> fields) throws Exception {
        // see org.collectionspace.services.common.init.AddIndices for a real implementation example.
        System.out.println("\r\n\r\n~~~~~~~~~~~~~ in InitHandler.onRepositoryInitialized with ServiceBindingType: "+sbt);
    }

    public ResultSet openResultSet(String sql) throws Exception {
        Connection conn = null;
    	Statement stmt = null;
        try {
        	conn = JDBCTools.getConnection(JDBCTools.DEFAULT_REPOSITORY_NAME);
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

    public void closeResultSet(ResultSet rs) throws SQLException {
        rs.close();
    }


}
