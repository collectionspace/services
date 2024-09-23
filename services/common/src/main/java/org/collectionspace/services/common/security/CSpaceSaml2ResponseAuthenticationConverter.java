package org.collectionspace.services.common.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.spring.CSpaceSaml2Authentication;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.config.AssertionProbesType;
import org.collectionspace.services.config.SAMLRelyingPartyType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.common.ServiceMain;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

public class CSpaceSaml2ResponseAuthenticationConverter implements Converter<ResponseToken, CSpaceSaml2Authentication> {
  private UserDetailsService userDetailsService;

  public CSpaceSaml2ResponseAuthenticationConverter(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public CSpaceSaml2Authentication convert(ResponseToken responseToken) {
    Saml2Authentication authentication = OpenSamlAuthenticationProvider
      .createDefaultResponseAuthenticationConverter()
      .convert(responseToken);

    String registrationId = responseToken.getToken().getRelyingPartyRegistration().getRegistrationId();
    ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();
    SAMLRelyingPartyType registration = ConfigUtils.getSAMLRelyingPartyRegistration(serviceConfig, registrationId);

    AssertionProbesType assertionProbes = (
      registration != null
        ? registration.getAssertionUsernameProbes()
        : null
    );

    List<String> attemptedUsernames = new ArrayList<>();

    for (Assertion assertion : responseToken.getResponse().getAssertions()) {
      Set<String> candidateUsernames = SecurityUtils.findSamlAssertionCandidateUsernames(assertion, assertionProbes);

      for (String candidateUsername : candidateUsernames) {
        try {
          CSpaceUser user = (CSpaceUser) userDetailsService.loadUserByUsername(candidateUsername);

          return new CSpaceSaml2Authentication(user, authentication);
        }
        catch(UsernameNotFoundException e) {
        }
      }

      attemptedUsernames.addAll(candidateUsernames);
    }

    String errorMessage = attemptedUsernames.size() == 0
      ? "The SAML assertion did not contain a CollectionSpace username."
      : "No CollectionSpace account found for " + StringUtils.join(attemptedUsernames, " / ") + ".";

    throw(new UsernameNotFoundException(errorMessage));
  }
}
