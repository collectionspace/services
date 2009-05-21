/**
 * Copyright 2009 University of California at Berkeley
 */
package org.collectionspace.services.nuxeo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.SingleDocumentReader;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDocumentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities related to Nuxeo 
 * @author 
 */
public class NuxeoUtils {

    private static Logger logger = LoggerFactory.getLogger(NuxeoUtils.class);

    public static Document getDocument(RepositoryInstance repoSession, DocumentModel helloDoc)
            throws Exception {
        Document doc = null;
        DocumentWriter writer = null;
        DocumentReader reader = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try{
            baos = new ByteArrayOutputStream();
            //nuxeo io.impl begin
            reader = new SingleDocumentReader(repoSession, helloDoc);
            writer = new XMLDocumentWriter(baos);
            DocumentPipe pipe = new DocumentPipeImpl();
            //nuxeo io.impl end
            pipe.setReader(reader);
            pipe.setWriter(writer);
            pipe.run();
            bais = new ByteArrayInputStream(baos.toByteArray());
            SAXReader saxReader = new SAXReader();
            doc = saxReader.read(bais);
        }finally{
            if(reader != null){
                reader.close();
            }
            if(writer != null){
                writer.close();
            }
            try{
                if(bais != null){
                    bais.close();
                }
                if(baos != null){
                    baos.close();
                }
            }catch(IOException ioe){
                logger.error("Failed to close io streams with {}", ioe);
                throw new WebApplicationException();
            }
        }
        return doc;
    }
}
