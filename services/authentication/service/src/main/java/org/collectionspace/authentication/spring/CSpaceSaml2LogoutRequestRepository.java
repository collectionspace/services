package org.collectionspace.authentication.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.saml2.provider.service.authentication.logout.Saml2LogoutRequest;
import org.springframework.security.saml2.provider.service.web.authentication.logout.HttpSessionLogoutRequestRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestRepository;

/**
 * A Saml2LogoutRequestRepository that saves the redirect parameter from the logout request to a
 * request attribute. This allows CSpaceLogoutSuccessHandler to have access to the parameter value
 * following the logout request to the IdP.
 */
public class CSpaceSaml2LogoutRequestRepository implements Saml2LogoutRequestRepository {
  public static final String REDIRECT_ATTRIBUTE_NAME = "org.collectionspace.authentication.logout.redirect";

  private HttpSessionLogoutRequestRepository repository = new HttpSessionLogoutRequestRepository();

  @Override
  public Saml2LogoutRequest loadLogoutRequest(HttpServletRequest request) {
    return repository.loadLogoutRequest(request);
  }

  @Override
  public void saveLogoutRequest(
    Saml2LogoutRequest logoutRequest,
    HttpServletRequest request,
    HttpServletResponse response)
  {
    repository.saveLogoutRequest(logoutRequest, request, response);

    String redirect = request.getParameter("redirect");

    request.getSession().setAttribute(REDIRECT_ATTRIBUTE_NAME, redirect);
  }

  @Override
  public Saml2LogoutRequest removeLogoutRequest(HttpServletRequest request, HttpServletResponse response) {
    return repository.removeLogoutRequest(request, response);
  }
}
