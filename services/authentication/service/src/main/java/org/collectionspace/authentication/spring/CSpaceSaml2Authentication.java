package org.collectionspace.authentication.spring;

import java.util.Collection;

import org.collectionspace.authentication.CSpaceUser;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A Saml2Authentication whose principal is a CSpaceUser.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonAutoDetect(
  fieldVisibility = JsonAutoDetect.Visibility.ANY,
  getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(value = { "authenticated" }, ignoreUnknown = true)
public class CSpaceSaml2Authentication extends Saml2Authentication {
  private final CSpaceUser user;
  private final AuthenticatedPrincipal principal;

  public CSpaceSaml2Authentication(CSpaceUser user, Saml2Authentication authentication) {
    this(
      user,
      (Saml2AuthenticatedPrincipal) authentication.getPrincipal(),
      authentication.getSaml2Response(),
      authentication.getAuthorities()
    );
  }

  public CSpaceSaml2Authentication(
    CSpaceUser user,
    AuthenticatedPrincipal principal,
    java.lang.String saml2Response,
    java.util.Collection<? extends GrantedAuthority> authorities
  ) {
    this(
      new Saml2AuthenticatedCSpaceUser((Saml2AuthenticatedPrincipal) principal, user),
      principal,
      saml2Response,
      authorities
    );
  }

  @JsonCreator
  public CSpaceSaml2Authentication(
    @JsonProperty("user") Saml2AuthenticatedCSpaceUser user,
    @JsonProperty("principal") AuthenticatedPrincipal principal,
    @JsonProperty("saml2Response") java.lang.String saml2Response,
    @JsonProperty("authorities") java.util.Collection<? extends GrantedAuthority> authorities
  ) {
    super(principal, saml2Response, authorities);

    this.user = user;
    this.principal = principal;

    this.setAuthenticated(true);
  }

  @Override
  public Object getPrincipal() {
    return user;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return user.getAuthorities();
  }

  public Saml2Authentication getWrappedAuthentication() {
    return new Saml2Authentication(this.principal, getSaml2Response(), getAuthorities());
  }
}
