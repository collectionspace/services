package org.collectionspace.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;

import org.collectionspace.authentication.realm.db.CSpaceDbRealm;
import org.postgresql.util.PSQLState;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CSpaceAuthenticationSuccessEvent implements ApplicationListener<AuthenticationSuccessEvent> {
	
	final private static String UPDATE_USER_SQL =
			"UPDATE users SET lastlogin = now() WHERE username = ?";

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		// TODO Auto-generated method stub
		System.out.println(); //org.springframework.security.authentication.UsernamePasswordAuthenticationToken@8a633e91: Principal: org.collectionspace.authentication.CSpaceUser@b122ec20: Username: admin@core.collectionspace.org; Password: [PROTECTED]; Enabled: true; AccountNonExpired: true; credentialsNonExpired: true; AccountNonLocked: true; Granted Authorities: ROLE_1_TENANT_ADMINISTRATOR,ROLE_SPRING_ADMIN; Credentials: [PROTECTED]; Authenticated: true; Details: {grant_type=password, username=admin@core.collectionspace.org}; Granted Authorities: ROLE_1_TENANT_ADMINISTRATOR, ROLE_SPRING_ADMIN
		String username = null;
		CSpaceDbRealm cspaceDbRealm = new CSpaceDbRealm();
		
		if (event.getSource() instanceof UsernamePasswordAuthenticationToken) {
			UsernamePasswordAuthenticationToken eventSource = (UsernamePasswordAuthenticationToken)event.getSource();
			if (eventSource.getPrincipal() instanceof CSpaceUser) {
				CSpaceUser cspaceUser = (CSpaceUser) eventSource.getPrincipal();
				username = cspaceUser.getUsername();
				try {
					setLastLogin(cspaceDbRealm, username);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void setLastLogin(CSpaceDbRealm cspaceDbRealm, String username) throws AccountException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = cspaceDbRealm.getConnection();
            ps = conn.prepareStatement(UPDATE_USER_SQL);
            ps.setString(1, username);
            int affected = ps.executeUpdate();
            if (affected < 1) {
            	String errMsg = String.format("No matching username '%s' found.", username);
                throw new AccountException(errMsg);
            }
        } catch (SQLException ex) {
        	// Assuming PostgreSQL
            if (PSQLState.UNDEFINED_COLUMN.getState().equals(ex.getSQLState())) {
            	System.err.println("'users' table is missing 'lastlogin' column.");
            } else {
                AccountException ae = new AccountException("Authentication query failed: " + ex.getLocalizedMessage());
                ae.initCause(ex);
                throw ae;
            }
        } catch (AccountNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            AccountException ae = new AccountException("Unknown Exception");
            ae.initCause(ex);
            throw ae;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                }
            }
        }
    }
}
