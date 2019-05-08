package org.collectionspace.services.common.xmljson;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;

/**
 * <p>
 * A filter that translates JSON requests to XML.
 * </p>
 *
 * <p>
 * This filter only has an effect if the content of the request (determined from
 * the Content-type header) is JSON. If the content is JSON, the request is
 * wrapped.
 * </p>
 *
 * <p>
 * The request wrapper changes the Content-type header to XML, and provides an
 * input stream containing an XML translation of the original JSON request
 * content.
 * </p>
 */
public class JsonToXmlFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (RequestUtils.isJsonContent(httpRequest)) {
            // The request contains a JSON payload. Wrap the request.

            RequestWrapper requestWrapper = new RequestWrapper(httpRequest);

            chain.doFilter(requestWrapper, response);
        } else {
            // The request doesn't contain a JSON payload. Just pass it along.

            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * A request wrapper that has a content type of XML, and replaces the wrapped
     * input stream with an XML translation.
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

            return new InputStreamWrapper(out.toInputStream());
        }
    }

    /**
     * A ServletInputStream that wraps another input stream.
     */
    public class InputStreamWrapper extends ServletInputStream {

        /**
         * The wrapped input stream.
         */
        private InputStream in;

        /**
         * Creates an InputStreamWrapper that wraps a given input stream.
         *
         * @param in the input stream to wrap
         */
        public InputStreamWrapper(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new NotImplementedException();
        }
    }
}
