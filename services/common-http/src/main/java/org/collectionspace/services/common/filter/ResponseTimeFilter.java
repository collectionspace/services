package org.collectionspace.services.common.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class ResponseTimeFilter implements Filter {

    // note: two types of uuids show up - a normal uuidv4 and one which has been truncated
    // (see: NuxeoRepositoryClientUtils#create)
    private static final String UUID_REGEX = "(?<=/)[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}(-[0-9a-fA-F]{12})?";
    private static final String URN_REGEX = "(?<=/)urn:cspace:name([^)]*\\))";

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws ServletException, IOException {

        // skip if we don't have http requests
        if (!(servletRequest instanceof HttpServletRequest request
              && servletResponse instanceof HttpServletResponse response)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // skip if metrics aren't enabled OR we're on the /metrics path (maybe include /health?)
        boolean enabled = Boolean.parseBoolean(System.getProperty("cspace.metrics.enabled"));
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        if (!enabled || requestPath.startsWith("/metrics")) {
            chain.doFilter(request, response);
            return;
        }

        var registry = getRegistry(request);
        var timer = registry != null ? Timer.start(registry) : null;

        try {
            chain.doFilter(request, response);
        } finally {
            if (timer != null) {
                timer.stop(Timer.builder("http.server.requests")
                                .description("")
                                .tag("method", request.getMethod())
                                .tag("uri", normalizeUri(requestPath))
                                .tag("status", getResponseStatus(response))
                                .register(registry)
                          );
            }
        }
    }

    public String normalizeUri(String uri) {
        return uri.replaceAll(UUID_REGEX, ":csid")
            .replaceAll(URN_REGEX, ":refname");
    }

    private String getResponseStatus(HttpServletResponse response) {
        var statusFamily = Response.Status.Family.familyOf(response.getStatus());
        return statusFamily.toString();
    }

    private MeterRegistry getRegistry(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        Object attribute = servletContext.getAttribute("cspace.metrics.registry");
        if (!(attribute instanceof MeterRegistry meterRegistry)) {
            return null;
        }

        return meterRegistry;
    }

}
