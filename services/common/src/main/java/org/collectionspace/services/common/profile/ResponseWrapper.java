package org.collectionspace.services.common.profile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;

public class ResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter output;
    private BufferedServletOutputStream os;
    private boolean gotWriter = false;
    private boolean gotStream = false;
    private Map<String, String> headers = new HashMap<String,String>();
    private int statusCode = 0;
    private String statusMessage = "";

    public String toString() {
        if (gotWriter) return output.toString();
        else {
            String str = os.getAsString();
            return str;
        }
    }

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new CharArrayWriter();
        os = new BufferedServletOutputStream();
    }

    public PrintWriter getWriter() {
        gotWriter = true;
        return new PrintWriter(output);
    }

    public ServletOutputStream getOutputStream() {
        gotStream = true;
        return os;
    }

    public void setHeader(String header, String value){
        System.out.println("###### setHeader ######################## header set: "+header+": "+value);
        headers.put(header, value);
        super.setHeader(header, value);
    }
    public void addHeader(java.lang.String name, java.lang.String value){
        super.addHeader(name, value);
        System.out.println("### addHeader  ########################### header set: "+name+": "+value);

    }
    public void setIntHeader(java.lang.String name, int value){
        super.setIntHeader(name, value);
        System.out.println("### setIntHeader  ########################### header set: "+name+": "+value);
    }
    public void addIntHeader(java.lang.String name, int value){
        super.addIntHeader(name, value);
        System.out.println("### addIntHeader  ########################### header set: "+name+": "+value);
    }


    public void setStatus(int sc, String sm){
        super.setStatus(sc,sm);
        this.statusCode = sc;
        this.statusMessage = sm;
    }
    public void setStatus(int sc){
        super.setStatus(sc);
        this.statusCode = sc;
    }
    public void sendError(int sc) throws java.io.IOException {
        super.sendError(sc);
        this.statusCode = sc;
    }
    public void sendError(int sc, String msg) throws java.io.IOException{
        super.sendError(sc, msg);
        this.statusCode = sc;
        this.statusMessage = msg;
    }

    public String getStatusMessage(){
        return statusMessage;
    }
    public int getStatusCode(){
        return statusCode;
    }
    public Map getHeaders(){
        return headers;
    }
    public String getHeaderBlock(){
        StringBuffer b = new StringBuffer();
        for(Map.Entry<String, String> e : headers.entrySet()){
            b.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        return b.toString();
    }
    public String getResponseLine(){
        return "HTTP/1.1 "+statusCode+' '+statusMessage;
    }

}

/**
 * A custom response wrapper that captures all output in a buffer.
 */
/*
public class ResponseWrapper extends HttpServletResponseWrapper {
    private BufferedServletOutputStream bufferedServletOut = new BufferedServletOutputStream( );
    private PrintWriter printWriter = null;
    private ServletOutputStream outputStream = null;

    public ResponseWrapper(HttpServletResponse origResponse) {
        super(origResponse);
    }

    public String getResponseAsString(){
        return bufferedServletOut.getAsString();
    }

    public byte[] getBuffer( ) {
        return this.bufferedServletOut.getBuffer( );
    }

    public PrintWriter getWriter( ) throws IOException {
        if (this.outputStream != null) {
            throw new IllegalStateException(
                    "The Servlet API forbids calling getWriter( ) after"
                    + " getOutputStream( ) has been called");
        }

        if (this.printWriter == null) {
            this.printWriter = new PrintWriter(this.bufferedServletOut);
        }
        return this.printWriter;
    }

    public ServletOutputStream getOutputStream( ) throws IOException {
        if (this.printWriter != null) {
            throw new IllegalStateException(
                "The Servlet API forbids calling getOutputStream( ) after"
                + " getWriter( ) has been called");
        }

        if (this.outputStream == null) {
            this.outputStream = this.bufferedServletOut;
        }
        return this.outputStream;
    }

    // override methods that deal with the response buffer

    public void flushBuffer( ) throws IOException {
        if (this.outputStream != null) {
            this.outputStream.flush( );
        } else if (this.printWriter != null) {
            this.printWriter.flush( );
        }
    }

    public int getBufferSize( ) {
        return this.bufferedServletOut.getBuffer( ).length;
    }

    public void reset( ) {
        this.bufferedServletOut.reset( );
    }

    public void resetBuffer( ) {
        this.bufferedServletOut.reset( );
    }

    public void setBufferSize(int size) {
        this.bufferedServletOut.setBufferSize(size);
    }
}
*/