package org.collectionspace.authentication.spring;

import org.collectionspace.authentication.CSpaceUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A DaoAuthenticationProvider that checks if the user being authenticated is required to log in
 * via single sign-on.
 */
public class CSpaceDaoAuthenticationProvider extends DaoAuthenticationProvider {
  private boolean isSsoAvailable = false;

  /**
   * Checks if the user is required to log in using SSO. If so, SSORequiredException is thrown.
   */
  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    CSpaceUser user = (CSpaceUser) userDetails;

    if (this.isSsoAvailable() && user.isRequireSSO()) {
      throw new SSORequiredException("Single sign-on is required for " + user.getUsername() + ". Please sign in through an SSO provider.");
    }

    super.additionalAuthenticationChecks(userDetails, authentication);
  }

  public boolean isSsoAvailable() {
    return this.isSsoAvailable;
  }

  public void setSsoAvailable(boolean isSsoAvailable) {
    this.isSsoAvailable = isSsoAvailable;
  }
}
