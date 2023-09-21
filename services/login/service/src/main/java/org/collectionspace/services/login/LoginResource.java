package org.collectionspace.services.login;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.config.SAMLRelyingPartyType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

@Path("/")
public class LoginResource {
    final Logger logger = LoggerFactory.getLogger(LoginResource.class);

    @GET
    public Response rootRedirect() throws URISyntaxException, MalformedURLException {
        String tenantId = AuthN.get().getCurrentTenantId();
        TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        TenantBindingType tenantBinding = tenantBindingConfigReader.getTenantBinding(tenantId);
        URI uri = new URI(ConfigUtils.getUILoginSuccessUrl(tenantBinding));

        return Response.temporaryRedirect(uri).build();
    }

    @GET
    @Path(LoginClient.SERVICE_PATH)
    @Produces(MediaType.TEXT_HTML)
    public String getHtml(@Context HttpServletRequest request) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        ServiceConfig serviceConfig = ServiceMain.getInstance().getServiceConfig();
        List<SAMLRelyingPartyType> samlRegistrations = ConfigUtils.getSAMLRelyingPartyRegistrations(serviceConfig);

        Map<String, Object> uiConfig = new HashMap<>();
        Map<String, Object> ssoConfig = new HashMap<>();

        if (samlRegistrations != null) {
            for (SAMLRelyingPartyType samlRegistration : samlRegistrations) {
                Map<String, String> registrationConfig = new HashMap<>();
                String name = samlRegistration.getName();

                if (name == null || name.length() == 0) {
                    name = samlRegistration.getId();
                }

                registrationConfig.put("name", name);

                if (samlRegistration.getIcon() != null) {
                    registrationConfig.put("icon", samlRegistration.getIcon().getLocation());
                }

                String url = "/cspace-services/saml2/authenticate/" + samlRegistration.getId();

                ssoConfig.put(url, registrationConfig);
            }
        }

        if (!ssoConfig.isEmpty()) {
            uiConfig.put("sso", ssoConfig);
        }

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            Map<String, Object> csrfConfig = new HashMap<>();

            csrfConfig.put("parameterName", csrfToken.getParameterName());
            csrfConfig.put("token", csrfToken.getToken());

            uiConfig.put("csrf", csrfConfig);
        }

        String tid = null;
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, null);

        if (savedRequest != null) {
            String[] tidValues = savedRequest.getParameterValues(AuthN.TENANT_ID_QUERY_PARAM);

            if (tidValues != null && tidValues.length > 0) {
                tid = tidValues[0];
            }
        }

        if (tid != null) {
            uiConfig.put("tenantId", tid);
        }

        if (request.getParameter("error") != null) {
            uiConfig.put("error", getLoginErrorMessage(request));
        }

        if (request.getParameter("logout") != null) {
            uiConfig.put("isLogoutSuccess", true);
        }

        String uiConfigJS;

        try {
            uiConfigJS = new ObjectMapper().writeValueAsString(uiConfig);
        } catch (JsonProcessingException e) {
            logger.error("Error generating login page UI configuration", e);

            uiConfigJS = "";
        }

        Map<String, String> dataModel = new HashMap<>();

        dataModel.put("uiConfig", uiConfigJS);

        Configuration freeMarkerConfig = ServiceMain.getInstance().getFreeMarkerConfig();
        Template template = freeMarkerConfig.getTemplate("service-ui.ftlh");
        Writer out = new StringWriter();

        template.process(dataModel, out);

        out.close();

        return out.toString();
    }

    private String getLoginErrorMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Object exception = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

            if (exception != null && exception instanceof AuthenticationException) {
                AuthenticationException authException = (AuthenticationException) exception;

                return authException.getMessage();
            }
        }

        return "Invalid credentials";
    }
}
