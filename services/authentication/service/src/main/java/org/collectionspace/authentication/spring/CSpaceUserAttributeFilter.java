package org.collectionspace.authentication.spring;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A filter that sets a request attribute containing the username of the authenticated
 * CollectionSpace user. This attribute may be used to log the username via tomcat's standard
 * access log valve.
 *
 * This filter should run before org.springframework.security.web.authentication.logout.LogoutFilter.
 */
public class CSpaceUserAttributeFilter extends OncePerRequestFilter {
    public static final String ATTRIBUTE_NAME = "org.collectionspace.authentication.user";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Get the username before running LogoutFilter, in case this is a logout request that
        // would delete the authenticated user.

        String beforeLogoutUsername = getUsername();

        chain.doFilter(request, response);

        String username = getUsername();

        if (username == null && beforeLogoutUsername != null) {
            username = beforeLogoutUsername;
        }

        if (username != null) {
            request.setAttribute(ATTRIBUTE_NAME, username);
        }
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getName();
        }

        return null;
    }
}
