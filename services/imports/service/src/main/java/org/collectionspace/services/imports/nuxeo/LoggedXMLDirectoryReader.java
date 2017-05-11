package org.collectionspace.services.imports.nuxeo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.nuxeo.common.utils.FileTreeIterator;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.io.ExportConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.AbstractDocumentReader;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggedXMLDirectoryReader extends AbstractDocumentReader {
    
    private static final int TIMEOUT_NEVER = 0;
	private final Logger logger = LoggerFactory.getLogger(LoggedXMLDirectoryReader.class);
    protected long timeoutMillis = TIMEOUT_NEVER;
    protected int totalTimeLimitSecs = 0; // the number of seconds before we timeout

    protected Document loadXML(File file) throws IOException {
        String filename = file.getCanonicalPath();
        
        if (logger.isTraceEnabled()) {
            logger.trace("~~~~~~~~~~~~~~~~~~~ LoggedXMLDirectoryReader :: "+filename);
        }
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            if (logger.isTraceEnabled()) {
                logger.trace("~~~~~~~~~~~~~~~~~~~ LoggedXMLDirectoryReader :: "+filename+" :: DONE");
            }
            reportList.add("READ: "+filename);
            return new SAXReader().read(in);
        } catch (DocumentException e) {
            IOException ioe = new IOException("Failed to read file document "
                    + file + ": " + e.getMessage());
            ioe.setStackTrace(e.getStackTrace());
            logger.error("~~~~~~~~~~~~~~~~~~~ LoggedXMLDirectoryReader :: "+filename+" :: ERROR");
            reportList.add("ERROR: "+filename);
            throw ioe;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private File source;

    private FileTreeIterator iterator;

    public LoggedXMLDirectoryReader(String sourcePath) {
        this(new File(sourcePath), TIMEOUT_NEVER);
    }

    public LoggedXMLDirectoryReader(File source, int timeout) {
    	if (timeout > 0) {
    		this.totalTimeLimitSecs = timeout;
    		this.timeoutMillis = System.currentTimeMillis() + (timeout * 1000); // set the timeout milliseconds time
    	}
        this.source = source;
        iterator = new FileTreeIterator(source);
        iterator.setFilter(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
    }

    public Object getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void close() {
        source = null;
        iterator = null;
    }

    private List<String> reportList = new ArrayList<String>();
    public String report(){
        StringBuffer result = new StringBuffer();
        for (String s: reportList){
            result.append(s).append("\r\n");
        }
        return result.toString();
    }
    
    /*
     * Returns 'true' if we've timed out.
     */
    protected boolean hasTimedOut() {
    	boolean result = false;
    	
    	if (totalTimeLimitSecs > 0 && System.currentTimeMillis() > timeoutMillis) {
    		result = true;
    	}
    	
    	return result;
    }


    @Override
    public ExportedDocument read() throws IOException {
        if (iterator.hasNext()) {
            File dir = iterator.next();
            if (dir == null) {
                return null;
            }
            // read document files
            ExportedDocument xdoc = new ExportedDocumentImpl();
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                	if (hasTimedOut() == true) { // Check to see if the current transaction has already timed out.
                		TransactionHelper.setTransactionRollbackOnly();
                		String errMsg = String.format("Import transaction timed out by exceeding %d seconds.", this.totalTimeLimitSecs);
                		throw new IOException(errMsg);
                	}
                    String name = file.getName();
                    if (ExportConstants.DOCUMENT_FILE.equals(name)) {
                        Document doc = loadXML(file);
                        xdoc.setDocument(doc);
                        Path relPath = computeRelativePath(dir);
                        xdoc.setPath(relPath);
                        reportList.add(relPath.toString());
                    } else if (name.endsWith(".xml")) {
                        xdoc.putDocument(
                                FileUtils.getFileNameNoExt(file.getName()),
                                loadXML(file));
                    } else { // presume a blob
                        xdoc.putBlob(file.getName(), new FileBlob(file));
                    }
                }
            }
            return xdoc;
        }
        return null;
    }

    /*NXP-1688 Rux: the path was somehow left over when migrated from
    core 1.3.4 to 1.4.0. Pull back.*/
    private Path computeRelativePath(File file) {
        /*NXP-2507 Rux: preserve directory structure with slashes instead OS name separator*/
        String subPathS =
            file.getAbsolutePath().substring(source.getAbsolutePath().length());
        subPathS = subPathS.replace(File.separatorChar, '/');
        return new Path(subPathS);
    }

}