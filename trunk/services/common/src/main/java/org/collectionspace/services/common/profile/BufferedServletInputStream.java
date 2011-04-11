package org.collectionspace.services.common.profile;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;

/* Subclass of ServletInputStream needed by the servlet engine.
All inputStream methods are wrapped and are delegated to
the ByteArrayInputStream (obtained as constructor parameter)!*/

public class BufferedServletInputStream extends ServletInputStream {
    ByteArrayInputStream bais;
    public BufferedServletInputStream(ByteArrayInputStream bais) {
        this.bais = bais;
    }

    public int available() {
        return bais.available();
    }

    public int read() {
        return bais.read();
    }

    public int read(byte[] buf, int off, int len) {
        return bais.read(buf, off, len);
    }
}

