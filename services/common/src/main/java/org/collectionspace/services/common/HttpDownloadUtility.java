package org.collectionspace.services.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;

public class HttpDownloadUtility {
    private static final Logger logger = LoggerFactory.getLogger(HttpDownloadUtility.class);

    private static String getDestDir(String destDir) {
        if (!destDir.endsWith(File.separator)) {
        	destDir = destDir + File.separator;
        }
        //
        // Check if the destDir exists, if not try to create it.
        //
        File destDirFile = new File(destDir);
        boolean destDirExists = destDirFile.exists();
        boolean triedToCreateDir = false;
        if (!destDirExists) {
        	triedToCreateDir = true;
        	destDirExists = destDirFile.mkdir();
        } else {
        	destDirExists = destDirFile.isDirectory();
        }
        //
        // If we couldn't create the directory or if it is already a file then fail.
        //
        if (!destDirExists) {
        	if (triedToCreateDir) {
        		logger.error("Tried and failed to create a temp directory '{}' for storing a downloaded file.",
							 destDir);
        	}
        	return null;
        }

        return destDir;
    }

	private static boolean isProtocolHttp(URL url) {
		return url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https");
	}

	public static File downloadFile(String url) throws MalformedURLException {
		URI uri = URI.create(url);
		if (!isProtocolHttp(uri.toURL())) {
			throw new MalformedURLException("Could not create a blob file from: " + url);
		}

		File result = null;
		try {
        	String tmpdir = System.getProperty("java.io.tmpdir");
        	if (!tmpdir.endsWith(File.separator)) {
        		tmpdir = tmpdir + File.separator;
        	}

        	String destDir = getDestDir(tmpdir + UUID.randomUUID() + File.separator);
        	String filePath = downloadFile(uri, destDir);
        	result = new File(filePath);
    	} catch (Exception e) {
    		logger.error("Could not download file use this URL: {}", url, e);
    	}

    	return result;
	}

	/**
	 * Get and validate a URI for a given url string. Checks that a URI is not a link local or site local.
	 *
	 * @param uri the uri
	 * @return InetAddress of the uri
	 * @throws IllegalArgumentException if the URI is for an unknown host, link local, site local, or loopback address
	 */
	@VisibleForTesting
	protected static InetAddress addressFor(URI uri) {
        InetAddress address;
        try {
            address = InetAddress.getByName(uri.getHost());
			if (address.isLinkLocalAddress()
				|| address.isMulticastAddress()
				|| address.isSiteLocalAddress()
				|| address.isAnyLocalAddress()
				|| address.isLoopbackAddress()) {
				throw new IllegalArgumentException("Invalid hostname");
			}
        } catch (UnknownHostException e) {
			logger.warn("Invalid host {} for download", uri, e);
            throw new IllegalArgumentException(e);
        }

		return address;
	}


	/**
	 * Extract the filename from an HttpResponse. In order of precedence:
	 * * Use the Content-Disposition header
	 * * Use the final path part of the URI
	 * * Use a UUID
	 * @param uri The uri of the request
	 * @param response The HttpResponse
	 * @return The extracted filename
	 */
	private static String getFilename(URI uri, CloseableHttpResponse response) {
		if (response.containsHeader(HttpHeaders.CONTENT_DISPOSITION)) {
			Header header = response.getFirstHeader(HttpHeaders.CONTENT_DISPOSITION);
			String headerValue = header.getValue();
			ContentDisposition contentDisposition = ContentDisposition.parse(headerValue);
			if (contentDisposition.getFilename() != null) {
				return contentDisposition.getFilename();
			}
		}

		String path = uri.getPath();
		if (path != null) {
			int lastSlashIndex = path.lastIndexOf("/");
			String pathPart = lastSlashIndex == -1 ? path : path.substring(lastSlashIndex + 1);
			if (!pathPart.isEmpty()) {
				return pathPart;
			}
		}

		return UUID.randomUUID().toString();
	}

	/**
	 * Downloads a file from a URL. Exceptions propagate up to the downloadFile above which handles them.
	 *
	 * @param uri The URI of the file to be downloaded
	 * @param saveDir path of the directory to save the file
	 * @throws IOException if there's an error with the http request or with the file writing
	 */
	private static String downloadFile(URI uri, String saveDir) throws IOException {
		final InetAddress address = addressFor(uri);

		// we already resolved the InetAddress when validating the address is not a local address, so we can use a
		// custom resolver to forward that to the http client
		try (CloseableHttpClient client = HttpClients.custom()
				.disableRedirectHandling()
				.setDnsResolver(s -> new InetAddress[] {address})
				.build()) {
			HttpGet get = new HttpGet(uri);
			Path savePath;
			try (CloseableHttpResponse response = client.execute(get)) {
				String file = getFilename(uri, response);
				savePath = Path.of(saveDir, file);
				try (OutputStream outputStream = Files.newOutputStream(savePath);
					 InputStream inputStream = response.getEntity().getContent()) {
					inputStream.transferTo(outputStream);
				}
			}

			return savePath.toString();
		}
	}
}
