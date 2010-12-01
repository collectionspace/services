package org.collectionspace.services.common.profile;

import java.io.*;
import javax.servlet.*;

public class BufferedServletOutputStream extends ServletOutputStream {
    // the actual buffer
    private ByteArrayOutputStream bos = new ByteArrayOutputStream( );

    public String getAsString(){
        byte[] buf = bos.toByteArray();
        return new String(buf);
    }

    /**
     * @return the contents of the buffer.
     */
    public byte[] getBuffer( ) {
        return this.bos.toByteArray( );
    }

    /**
     * This method must be defined for custom servlet output streams.
     */
    public void write(int data) {
        this.bos.write(data);
    }

    // BufferedHttpResponseWrapper calls this method
    public void reset( ) {
        this.bos.reset( );
    }

    // BufferedHttpResponseWrapper calls this method
    public void setBufferSize(int size) {
        // no way to resize an existing ByteArrayOutputStream
        this.bos = new ByteArrayOutputStream(size);
    }
}