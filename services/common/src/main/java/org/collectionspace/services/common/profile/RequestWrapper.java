package org.collectionspace.services.common.profile;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

public class RequestWrapper extends HttpServletRequestWrapper {

    ByteArrayInputStream bais;
    ByteArrayOutputStream baos;
    BufferedServletInputStream bufInputStream;
    byte[] buffer;
    HttpServletRequest originalRequest;

    public RequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        this.originalRequest = req;
        // Read InputStream and store its content in a buffer.
        java.io.InputStream is = req.getInputStream();
        baos = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buf)) > 0) baos.write(buf, 0, bytesRead);
        buffer = baos.toByteArray();
    }

    public ServletInputStream getInputStream() {
        try {
            // Generate a new InputStream by stored buffer
            bais = new ByteArrayInputStream(buffer);
            bufInputStream = new BufferedServletInputStream(bais); //BufferedServletInputStream is our custom class.
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            return bufInputStream;
        }
    }
    public String getRequestAsString(){
        return new String(buffer);
    }

    public String getHeaderBlock() {
        StringBuffer b = new StringBuffer();
        for (Enumeration headernames = originalRequest.getHeaderNames(); headernames.hasMoreElements();) {
            String headername = (String) headernames.nextElement();
            b.append(headername + ": " + originalRequest.getHeader(headername) + "\r\n");
        }
        return b.toString();
    }

}


 /*
public class POSTFilter {
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            RequestWrapper bufferedRequest = new RequestWrapper(httpRequest);

//Here obtain InputStream to process POST data!

            InputStream is = bufferedRequest.getInputStream();

//... some kind of processing on "is"......

//chain.doFilter using wrapped request!!!!

            chain.doFilter(bufferedRequest, response);

//When a chained servlet call request.getInputStream()

//then the getInputStream() method of RequestWrapper will be invoked

//and a new readable copy of the original inputStream will be returned!!!

        }

        catch (Exception ex) {

            ex.printStackTrace();

        }

    }

}  */





