package org.collectionspace.authentication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;

import org.collectionspace.authentication.realm.db.CSpaceDbRealm;
import org.collectionspace.authentication.spring.CSpaceSaml2Authentication;
import org.postgresql.util.PSQLState;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class CSpaceAuthenticationSuccessEvent implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final String UPDATE_USER_SSO_ID_SQL =
        "UPDATE users SET sso_id = ? WHERE username = ?";

    private static final String UPDATE_USER_LAST_LOGIN_SQL =
            "UPDATE users SET lastlogin = now() WHERE username = ?";

    private static final String DELETE_EXPIRED_AUTHORIZATIONS_SQL =
            "DELETE FROM oauth2_authorization WHERE access_token_expires_at < now()";

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object eventSource = event.getSource();

        if (
            eventSource instanceof Authentication
            // Ignore authentication via JWT token, since this indicates a continuing session -- not what a user would consider a "log in"
            && !(eventSource instanceof JwtAuthenticationToken)
            // Ignore authorization code requests
            && !(eventSource instanceof OAuth2AuthorizationCodeRequestAuthenticationToken)
        ) {
            Authentication authentication = (Authentication) eventSource;

            if (authentication.getPrincipal() instanceof CSpaceUser) {
                CSpaceDbRealm cspaceDbRealm = new CSpaceDbRealm();
                CSpaceUser cspaceUser = (CSpaceUser) authentication.getPrincipal();
                String username = cspaceUser.getUsername();

                if (authentication instanceof CSpaceSaml2Authentication) {
                    try {
                        setSsoId(cspaceDbRealm, username, cspaceUser.getSsoId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    setLastLogin(cspaceDbRealm, username);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    deleteExpiredAuthorizations(cspaceDbRealm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setSsoId(CSpaceDbRealm cspaceDbRealm, String username, String ssoId) throws AccountException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cspaceDbRealm.getConnection();
            ps = conn.prepareStatement(UPDATE_USER_SSO_ID_SQL);
            ps.setString(1, ssoId);
            ps.setString(2, username);
            int affected = ps.executeUpdate();
            if (affected < 1) {
                String errMsg = String.format("No matching username '%s' found.", username);
                throw new AccountException(errMsg);
            }
        } catch (SQLException ex) {
            // Assuming PostgreSQL
            if (PSQLState.UNDEFINED_COLUMN.getState().equals(ex.getSQLState())) {
                System.err.println("'users' table is missing 'sso_id' column.");
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

	private void setLastLogin(CSpaceDbRealm cspaceDbRealm, String username) throws AccountException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cspaceDbRealm.getConnection();
            ps = conn.prepareStatement(UPDATE_USER_LAST_LOGIN_SQL);
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

    private void deleteExpiredAuthorizations(CSpaceDbRealm cspaceDbRealm) throws AccountException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = cspaceDbRealm.getConnection();
            ps = conn.prepareStatement(DELETE_EXPIRED_AUTHORIZATIONS_SQL);
            ps.executeUpdate();
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
