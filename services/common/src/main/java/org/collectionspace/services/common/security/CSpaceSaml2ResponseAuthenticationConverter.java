package org.collectionspace.services.common.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.spring.CSpaceSaml2Authentication;
import org.collectionspace.authentication.spring.CSpaceUserDetailsService;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.config.AssertionProbesType;
import org.collectionspace.services.config.SAMLRelyingPartyType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.common.ServiceMain;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

public class CSpaceSaml2ResponseAuthenticationConverter implements Converter<ResponseToken, CSpaceSaml2Authentication> {
  private final Logger logger = LoggerFactory.getLogger(CSpaceSaml2ResponseAuthenticationConverter.class);

  private CSpaceUserDetailsService userDetailsService;

  public CSpaceSaml2ResponseAuthenticationConverter(CSpaceUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Override
  public CSpaceSaml2Authentication convert(ResponseToken responseToken) {
    Saml2Authentication authentication = OpenSamlAuthenticationProvider
      .createDefaultResponseAuthenticationConverter()
      .convert(responseToken);

    String registrationId = responseToken.getToken().getRelyingPartyRegistration().getRegistrationId();
    ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();
    SAMLRelyingPartyType relyingPartyRegistration = ConfigUtils.getSAMLRelyingPartyRegistration(serviceConfig, registrationId);
    CSpaceUser user = findUser(relyingPartyRegistration, responseToken);

    if (user != null) {
      return new CSpaceSaml2Authentication(user, authentication);
    }

    return null;
  }

  /**
   * Attempt to find a CSpace user for a SAML response.
   *
   * @param relyingPartyRegistration
   * @param responseToken
   * @return
   */
  private CSpaceUser findUser(SAMLRelyingPartyType relyingPartyRegistration, ResponseToken responseToken) {
    AssertionProbesType assertionSsoIdProbes = (
      relyingPartyRegistration != null
        ? relyingPartyRegistration.getAssertionSsoIdProbes()
        : null
    );

    AssertionProbesType assertionUsernameProbes = (
      relyingPartyRegistration != null
        ? relyingPartyRegistration.getAssertionUsernameProbes()
        : null
    );

    List<String> attemptedUsernames = new ArrayList<>();
    List<Assertion> assertions = responseToken.getResponse().getAssertions();

    SecurityUtils.logSamlAssertions(assertions);

    for (Assertion assertion : assertions) {
      CSpaceUser user = null;
      String ssoId = SecurityUtils.getSamlAssertionSsoId(assertion, assertionSsoIdProbes);

      // First, look for a CSpace user whose SSO ID is the ID in the assertion.

      if (ssoId != null) {
        try {
          user = (CSpaceUser) userDetailsService.loadUserBySsoId(ssoId);
        }
        catch (UsernameNotFoundException e) {
        }
      }

      if (user != null) {
        return user;
      }

      // Next, look for a CSpace user whose username is the email address in the assertion.

      Set<String> candidateUsernames = SecurityUtils.findSamlAssertionCandidateUsernames(assertion, assertionUsernameProbes);

      for (String candidateUsername : candidateUsernames) {
        try {
          user = (CSpaceUser) userDetailsService.loadUserByUsername(candidateUsername);

          if (user != null) {
            String expectedSsoId = user.getSsoId();

            if (expectedSsoId == null) {
              // Store the ID from the IdP to use in future log ins. Note that this does not save
              // the SSO ID to the database. That happens in CSpaceAuthenticationSuccessEvent.

              user.setSsoId(ssoId);

              // TODO: If the email address in the assertion differs from the CSpace user's email,
              // update the CSpace user.
            } else if (!StringUtils.equals(expectedSsoId, ssoId)) {
              // If the user previously logged in via SSO, but they had a different ID from the
              // IdP, something's wrong. (Did an account on the IdP get assigned an email that
              // previously belonged to a different account on the IdP?)

              logger.warn("User with username {} has expected SSO ID {}, but received {} in SAML assertion",
                candidateUsername, expectedSsoId, ssoId);

              user = null;
            }

            if (user != null) {
              return user;
            }
          }
        }
        catch(UsernameNotFoundException e) {
        }
      }

      attemptedUsernames.addAll(candidateUsernames);
    }

    // No CSpace user was found for this SAML response.
    // TODO: Auto-create a CSpace user, using the display name, email address, and ID in the response.

    String errorMessage = attemptedUsernames.size() == 0
      ? "The SAML response did not contain a CollectionSpace username."
      : "No CollectionSpace account found for " + StringUtils.join(attemptedUsernames, " / ") + ".";

    throw(new UsernameNotFoundException(errorMessage));
  }
}
