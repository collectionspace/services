package org.collectionspace.services.common;
import java.io.*;
import java.net.*;
import java.util.*;

// This class downloads a file from a URL.
@Deprecated // - see JIRA DRYD-832
public class Download extends Observable implements Runnable {
    
    public static URL verifyUrl(String url) {
        // Only allow HTTP URLs.
        if (!url.toLowerCase().startsWith("http://"))
            return null;
        
        // Verify format of URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        
        // Make sure URL specifies a file.
        if (verifiedUrl.getFile().length() < 2)
            return null;
        
        return verifiedUrl;
    }
    
    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;
    
    // These are the status names.
    public static final String STATUSES[] = {"Downloading",
    "Paused", "Complete", "Cancelled", "Error"};
    
    // These are the status codes.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    private String errorMessage = null;
    private String filePath = null;
    private File downloadedFile = null;
    private String destDir = null; // the location of the downloaded file
    private URL url; // download URL
    private int size; // size of download in bytes
    private int downloaded; // number of bytes downloaded
    private int status; // current status of download
    
    private void doDownload(URL url, String destDir) {
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        this.url = url;
        setDestDir(destDir);
    	
        // Begin the download.
        download();
    }
    
    public Download(URL url, String destDir) {
        doDownload(url, destDir);
    }

    // Constructor for Download.  File is uploaded to system temp directory.
    public Download(URL url) {
    	String tmpdir = System.getProperty("java.io.tmpdir");
    	if (tmpdir.endsWith(File.separator) == false) {
    		tmpdir = tmpdir + File.separator;
    	}
    	doDownload(url, tmpdir
    			+ UUID.randomUUID() + File.separator);
    }
    
    private void setDestDir(String destDir) {
        if (destDir.endsWith(File.separator) != true) {
        	destDir = destDir + File.separator;
        }
        //
        // Check if the destDir exists, if not try to create it.
        //
        File destDirFile = new File(destDir);
        boolean destDirExists = destDirFile.exists();
        if (destDirExists == false) {
        	destDirExists = destDirFile.mkdir();
        } else {
        	destDirExists = destDirFile.isDirectory();
        }
        //
        // If we couldn't create the directory or if it is already a file then fail.
        //
        if (destDirExists = true) {
	    	this.destDir = destDir;
        } else {
        	error("Could not download file to: " + destDir);
        }
    }
    
    
    public File getFile() {
    	return this.downloadedFile;
    }
    
    public String getFilePath() {
    	return this.filePath;
    }
    
    public String getDestDir() {
    	return this.destDir;
    }
    
    // Get this download's URL.
    public String getUrl() {
        return url.toString();
    }
    
    // Get this download's size.
    public int getSize() {
        return size;
    }
    
    // Get this download's progress.
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }
    
    // Get this download's status.
    public int getStatus() {
        return status;
    }
    
    // Pause this download.
    public void pause() {
        status = PAUSED;
        stateChanged();
    }
    
    // Resume this download.
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }
    
    // Cancel this download.
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }
    
    // Mark this download as having an error.
    private void error() {
        status = ERROR;
        stateChanged();
    }
    
    // Mark this download as having an error.
    private void error(String message) {
        this.errorMessage = message;
        error();
    }    
    
    // Start or resume downloading.
    private void download() {
//        Thread thread = new Thread(this);
//        thread.start();
    	run();
    }
    
    // Get file name portion of URL.
    private String getFileName(URL url) {
    	String result = null;
    	
    	String destDir = this.getDestDir();
        String fileName = url.getFile();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        result = filePath = destDir + fileName;
        
        return result;
    }
    
    // Download file.
    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;
        
        try {
            // Open connection to URL.
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            
            // Specify what portion of file to download.
            connection.setRequestProperty("Range",
                    "bytes=" + downloaded + "-");
            
            // Connect to server.
            connection.connect();
            
            // Make sure response code is in the 200 range.
            int responseCode = connection.getResponseCode();
            if (responseCode / 100 != 2) {
                error();
            }
            
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }
            
      /* Set the size for this download if it
         hasn't been already set. */
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }
            
            // Open file and seek to the end of it.
            File javaFile = new File(getFileName(url));
            file = new RandomAccessFile(javaFile, "rw");
            file.seek(downloaded);
            
            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
        /* Size buffer according to how much of the
           file is left to download. */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                
                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1)
                    break;
                
                // Write buffer to file.
                file.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }
            
      /* Change status to complete if this point was
         reached because downloading has finished. */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                downloadedFile = new File(this.getFilePath());
                stateChanged();
            }
        } catch (Exception e) {
        	e.printStackTrace();
            error();
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
            
            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        }
    }
    
    // Notify observers that this download's status has changed.
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}