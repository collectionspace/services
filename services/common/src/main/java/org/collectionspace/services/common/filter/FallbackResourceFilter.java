package org.collectionspace.services.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * A filter that forwards requests that obtain a 404 error to a fallback,
 * similar to apache's FallbackResource directive. The path to the fallback file
 * may be specified using the "file" init parameter. It defaults to "/index.html".
 */
public class FallbackResourceFilter implements Filter {
    String fallbackFile = "/index.html";
 
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String fallbackFile = filterConfig.getInitParameter("file");
        
        if (fallbackFile != null) {
            this.fallbackFile = fallbackFile;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, responseWrapper);
        
        if (responseWrapper.is404()) {
            request.getRequestDispatcher(fallbackFile).forward(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    public class ResponseWrapper extends HttpServletResponseWrapper {
        private boolean is404 = false;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        public boolean is404() {
            return is404;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            if (sc == 404) {
                is404 = true;
            }
            else {
                super.sendError(sc);
            }
        }
    }
}
