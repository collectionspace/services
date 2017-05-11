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
 * A filter that sets a request attribute containing the username of the
 * authenticated CollectionSpace user. This attribute may then be used
 * to log the username via tomcat's standard access log valve.
 */
public class CSpaceUserAttributeFilter extends OncePerRequestFilter {
    public static final String ATTRIBUTE_NAME = "org.collectionspace.authentication.user";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            if (principal != null) {
                username = principal.toString();
            }
        }
        
        if (username != null) {
            request.setAttribute(ATTRIBUTE_NAME, username);
        }
        
        chain.doFilter(request, response);
    }
}
