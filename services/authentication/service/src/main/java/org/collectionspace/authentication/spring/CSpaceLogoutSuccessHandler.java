package org.collectionspace.authentication.spring;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

/**
 * A LogoutSuccessHandler that reads the post-logout redirect URL from a parameter in the logout
 * request. As an anti-phishing security measure, the URL is checked against a list of permitted
 * redirect URLs (originating from tenant binding configuration or OAuth client configuration).
 *
 * For SAML logouts, the redirect URL is saved to a request attribute, which is also checked, if
 * the redirect parameter is not present.
 */
public class CSpaceLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
	final Logger logger = LoggerFactory.getLogger(CSpaceLogoutSuccessHandler.class);

  public static final String REDIRECT_PARAMETER_NAME = "redirect";

  private Set<String> permittedRedirectUris;

  public CSpaceLogoutSuccessHandler(String defaultTargetUrl, Set<String> permittedRedirectUris) {
    super();

    this.setDefaultTargetUrl(defaultTargetUrl);

    this.permittedRedirectUris = permittedRedirectUris;
  }

  @Override
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
    String redirectUrl = request.getParameter(REDIRECT_PARAMETER_NAME);

    if (redirectUrl == null) {
      redirectUrl = (String) request.getSession().getAttribute(CSpaceSaml2LogoutRequestRepository.REDIRECT_ATTRIBUTE_NAME);
    }

    if (redirectUrl != null && !isPermitted(redirectUrl)) {
      logger.warn("Logout redirect url not permitted: {}", redirectUrl);

      redirectUrl = null;
    }

    return (redirectUrl != null)
      ? redirectUrl
      : super.determineTargetUrl(request, response);
  }

  private boolean isPermitted(String redirectUrl) {
    return permittedRedirectUris.contains(redirectUrl);
  }
}
