package org.collectionspace.services.logout;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

@Path(LogoutClient.SERVICE_PATH)
public class LogoutResource {
    final Logger logger = LoggerFactory.getLogger(LogoutResource.class);

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getHtml(/* @Context UriInfo ui, */ @Context HttpServletRequest request) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        Map<String, Object> uiConfig = new HashMap<>();

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            Map<String, Object> csrfConfig = new HashMap<>();

            csrfConfig.put("parameterName", csrfToken.getParameterName());
            csrfConfig.put("token", csrfToken.getToken());

            uiConfig.put("csrf", csrfConfig);
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
}
