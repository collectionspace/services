package org.collectionspace.services.common.xmljson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A filter that translates XML responses to JSON.
 * </p>
 *
 * <p>
 * This filter only has an effect if the preferred content type of the response
 * (determined from the Accept header) is JSON. If JSON is preferred, both the
 * request and response are wrapped.
 * </p>
 *
 * <p>
 * The request wrapper modifies the Accept header, ensuring that XML is accepted
 * in addition to (but at a lower quality factor than) JSON. This handles the
 * case where the original request only accepts JSON. In that case, XML should
 * also be accepted, so that the XML response may be translated to JSON on the
 * way back.
 * </p>
 *
 * <p>
 * The response wrapper provides a buffered output stream, so that XML output is
 * captured before being sent over the network. If the content type of the
 * response is XML, the content type is changed to JSON, the content of the
 * buffer is translated to JSON, and the JSON is written to the original output
 * stream. If the content type of the response is not XML, the content type is
 * not changed, and the content of the buffer is written to the original output
 * stream unchanged.
 * </p>
 */
public class XmlToJsonFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (RequestUtils.isJsonPreferred((HttpServletRequest) request)) {
            // The request prefers a JSON response. Wrap the request and response.

            RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
            ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);

            chain.doFilter(requestWrapper, responseWrapper);

            if (StringUtils.equals(responseWrapper.getContentType(), MediaType.APPLICATION_XML)) {
                // Got an XML response. Translate it to JSON.

                response.setContentType(MediaType.APPLICATION_JSON);

                try {
                    InputStream xmlInputStream = responseWrapper.getBuffer().toInputStream();

                    if (xmlInputStream != null) {
                        OutputStream jsonOutputStream = response.getOutputStream();
                        XmlToJsonStreamConverter converter = new XmlToJsonStreamConverter(xmlInputStream,
                                jsonOutputStream);

                        converter.convert();
                    }
                } catch (XMLStreamException e) {
                    throw new WebApplicationException("Error generating JSON", e);
                }
            } else {
                // Didn't get an XML response. Just pass it along.

                if (responseWrapper.getBuffer() != null) {
                    InputStream inputStream = responseWrapper.getBuffer().toInputStream();

                    IOUtils.copy(inputStream, response.getOutputStream());
                }
            }
        } else {
            // The request doesn't prefer a JSON response. Just pass it along.

            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * A request wrapper that enhances the original Accept header so that it accepts
     * XML (if it did not already accept XML).
     */
    public class RequestWrapper extends HttpServletRequestWrapper {
        private String xmlEnsuredAccept;

        public RequestWrapper(HttpServletRequest request) {
            super(request);

            xmlEnsuredAccept = RequestUtils.getXmlEnsuredAccept(request);
        }

        @Override
        public Enumeration getHeaders(String name) {
            if (name.compareToIgnoreCase(HttpHeaders.ACCEPT) == 0) {
                return Collections.enumeration(Arrays.asList(xmlEnsuredAccept));
            }

            return super.getHeaders(name);
        }
    }

    /**
     * A response wrapper that replaces the wrapped output stream with a buffered
     * stream so that output is captured.
     */
    public class ResponseWrapper extends HttpServletResponseWrapper {
        private BufferedServletOutputStream outputStream;
        private PrintWriter writer;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null) {
                throw new IllegalStateException("getWriter has already been called on this response");
            }

            if (outputStream == null) {
                outputStream = new BufferedServletOutputStream();
            }

            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (outputStream != null && writer == null) {
                throw new IllegalStateException("getOutputStream has already been called on this response");
            }

            if (outputStream == null) {
                outputStream = new BufferedServletOutputStream();
            }

            if (writer == null) {
                writer = new PrintWriter(outputStream);
            }

            return writer;
        }

        /**
         * Returns the internal buffer stream.
         *
         * @return the buffer stream
         */
        public ByteArrayOutputStream getBuffer() {
            if (outputStream == null) {
                return null;
            }

            return outputStream.getBuffer();
        }
    }

    /**
     * A ServletOutputStream that wraps a ByteArrayOutputStream. Any bytes written
     * are sent to the ByteArrayOutputStream, which acts as a buffer.
     */
    public class BufferedServletOutputStream extends ServletOutputStream {
        /*
         * The buffer.
         *
         * ByteArrayOutputStream from commons-io provides better performance than the
         * one from java.io.
         */
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        /**
         * Returns the buffer stream.
         *
         * @return the buffer stream
         */
        public ByteArrayOutputStream getBuffer() {
            return buffer;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
            throw new NotImplementedException();
        }
    }
}
