package org.collectionspace.authentication.spring;

import java.util.Objects;

import org.collectionspace.authentication.CSpaceUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * A JwtAuthenticationToken whose principal is a CSpaceUser.
 */
public class CSpaceJwtAuthenticationToken extends JwtAuthenticationToken {
  private final CSpaceUser user;

  public CSpaceJwtAuthenticationToken(Jwt jwt, CSpaceUser user) {
    super(jwt, user.getAuthorities(), user.getUsername());

    this.user = Objects.requireNonNull(user);

    this.setAuthenticated(true);
  }

  @Override
  public Object getPrincipal() {
    return user;
  }
}
