package org.collectionspace.authentication.spring;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.jackson2.Saml2AuthenticatedCSpaceUserDeserializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A CSpaceUser that is also a Saml2AuthenticatedPrincipal. This is needed because various parts of
 * Spring Security use instanceof Saml2AuthenticatedPrincipal to determine if the currently
 * authenticated user logged in via SAML.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(using = Saml2AuthenticatedCSpaceUserDeserializer.class)
@JsonAutoDetect(
	fieldVisibility = JsonAutoDetect.Visibility.ANY,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Saml2AuthenticatedCSpaceUser extends CSpaceUser implements Saml2AuthenticatedPrincipal {
	private Saml2AuthenticatedPrincipal principal;

	public Saml2AuthenticatedCSpaceUser(Saml2AuthenticatedPrincipal principal, CSpaceUser user) {
		this(
			principal,
			user.getUsername(),
			user.getPassword(),
			user.getSalt(),
			user.getSsoId(),
			user.isRequireSSO(),
			user.getTenants(),
			(Set<GrantedAuthority>) user.getAuthorities()
		);
	}

	public Saml2AuthenticatedCSpaceUser(
		Saml2AuthenticatedPrincipal principal,
		String username,
		String password,
		String salt,
		String ssoId,
		boolean requireSSO,
		Set<CSpaceTenant> tenants,
		Set<? extends GrantedAuthority> authorities
	) {
		super(username, password, salt, ssoId, requireSSO, tenants, authorities);

		this.principal = principal;
	}

	@Override
	public String getName() {
		return principal.getName();
	}

	@Override
	public <A> A getFirstAttribute(String name) {
		return principal.getFirstAttribute(name);
	}

	@Override
	public <A> List<A> getAttribute(String name) {
		return principal.getAttribute(name);
	}

	@Override
	public Map<String, List<Object>> getAttributes() {
		return principal.getAttributes();
	}

	@Override
	public String getRelyingPartyRegistrationId() {
		return principal.getRelyingPartyRegistrationId();
	}

	@Override
	public List<String> getSessionIndexes() {
		return principal.getSessionIndexes();
	}
}
