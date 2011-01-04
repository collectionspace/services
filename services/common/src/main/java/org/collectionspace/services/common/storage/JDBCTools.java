package org.collectionspace.services.common.storage;

import org.collectionspace.services.common.Tools;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class JDBCTools {

    public static final String DEFAULT_REPOSITORY_NAME = "CspaceDS";

    public static Connection getConnection(String repositoryName) throws LoginException, SQLException {
        if (Tools.isEmpty(repositoryName)){
            repositoryName = DEFAULT_REPOSITORY_NAME;
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




}
