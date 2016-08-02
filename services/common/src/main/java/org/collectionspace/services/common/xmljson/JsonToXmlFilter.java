package org.collectionspace.services.common.xmljson;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * A filter that translates JSON to XML.
 */
public class JsonToXmlFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if (RequestUtils.isJsonContent(httpRequest)) {
            // The request contains a JSON payload. Wrap the request.
            
            RequestWrapper requestWrapper = new RequestWrapper(httpRequest);

            chain.doFilter(requestWrapper, response);
        }
        else {
            // The request doesn't contain a JSON payload. Just pass it along.
            
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
    
    /**
     * A request wrapper that .
     */
    public class RequestWrapper extends HttpServletRequestWrapper {
        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getContentType() {
            return MediaType.APPLICATION_XML;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            try {
                JsonToXmlStreamConverter converter = new JsonToXmlStreamConverter(super.getInputStream(), out);
                converter.convert();
            } catch (XMLStreamException e) {
                throw new IOException("error converting JSON stream to XML", e);
            }
            
            final InputStream xmlInput = out.toInputStream();

            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return xmlInput.read();
                }
            };
        }
    }
}
