package org.collectionspace.services.imports.nuxeo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.io.ExportConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.io.impl.plugins.XMLZipReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggedXMLZipReader extends XMLZipReader {
	private final Logger logger = LoggerFactory.getLogger(LoggedXMLZipReader.class);
	
    private ZipFile zip;
    Enumeration<? extends ZipEntry> entries = null;


    private LoggedXMLZipReader(ZipFile zip) {
    	super((ZipFile)null);
        this.zip = zip;
    }

    public LoggedXMLZipReader(File source) throws IOException {
        this(new ZipFile(source));
        entries = zip.entries();
    }

    private List<String> reportList = new ArrayList<String>();
    public String report(){
        StringBuffer result = new StringBuffer();
        for (String s: reportList){
            result.append(s).append("\r\n");
        }
        return result.toString();
    }
	
    @Override
    // the zip entry order is the same as one used when creating the zip
    public ExportedDocument read() throws IOException {
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                return createDocument(entry);
            }
        }
        return null;
    }

    private ExportedDocument createDocument(ZipEntry dirEntry) throws IOException {
        ExportedDocument xdoc = new ExportedDocumentImpl();
        String dirPath = dirEntry.getName();
        // read the main document
        ZipEntry entry = zip.getEntry(dirPath + ExportConstants.DOCUMENT_FILE);
        if (entry != null) {
	        InputStream in = zip.getInputStream(entry);
	        try {
	            Document doc = readXML(in);
	            xdoc.setDocument(doc);
	            Path relPath = new Path(dirPath).removeTrailingSeparator();
//	            xdoc.setPath(new Path(dirPath + relPath).removeTrailingSeparator());
	            xdoc.setPath(relPath);
	        } finally {
	            in.close();
	        }
        }

        return xdoc;
    }

    @Override
	public Document readXML(InputStream in) {
    	Document result = null;
    	
        try {
            result = new SAXReader().read(in);
            reportList.add("READ: " + result.getName());
        } catch (DocumentException e) {
            reportList.add("ERROR: " + e.getMessage());
        }
        
        return result;
    }    
}
